package pgloaguen.com.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by root on 12/08/15.
 */
public class VideoFrameView extends RecyclerView {

    private static final String KEY_PARENT_PARCELABLE = "key_parent_parcelable";
    private static final String KEY_PATH_VIDEO = "key_path_video";
    private static final String KEY_URI_VIDEO = "key_uri_video";

    private MyMediaMetadataRetriever mediaMetadataRetriever;
    private Uri uri;
    private String path;
    private LruBitmapCache bitmapCache;
    private float pixelsPerSecond;

    public VideoFrameView(Context context) {
        super(context);
    }

    public VideoFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(((Bundle) state).getParcelable(KEY_PARENT_PARCELABLE));

        path = bundle.getString(KEY_PATH_VIDEO);
        uri = bundle.getParcelable(KEY_URI_VIDEO);

        if (path != null) {
            setVideo(path);
        } else if(uri != null) {
            setVideo(uri);
        } // Else nothing to restore
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PARENT_PARCELABLE, parcelable);
        bundle.putString(KEY_PATH_VIDEO, path);
        bundle.putParcelable(KEY_URI_VIDEO, uri);

        return bundle;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    public void setVideo(Uri uri) {
        release();
        this.uri = uri;
        mediaMetadataRetriever = new MyMediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getContext(), uri);
        initLayout();
    }

    public void setVideo(File file) {
        setVideo(file.getAbsolutePath());
    }

    public void setVideo(String path) {
        release();
        this.path = path;
        mediaMetadataRetriever = new MyMediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        initLayout();
    }

    public void setPixelsPerSecond(float nbPixelsForOneSecond) {
        if (pixelsPerSecond != nbPixelsForOneSecond) {
            pixelsPerSecond = nbPixelsForOneSecond;
            if (getAdapter() != null) {
                ((Adapter)getAdapter()).setPixelsPerSecond(pixelsPerSecond);
            }
        }
    }

    private void initLayout() {
        bitmapCache = new LruBitmapCache((int) (Runtime.getRuntime().maxMemory() / 1024 / 16));
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        setAdapter(new Adapter(mediaMetadataRetriever, bitmapCache, 120));
        ((Adapter)getAdapter()).setPixelsPerSecond(pixelsPerSecond);
    }

    private void release() {
        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
            bitmapCache.evictAll();
        }
    }

    private static class MyMediaMetadataRetriever extends MediaMetadataRetriever {
        private boolean isReleased;

        @Override
        public void release() {
            super.release();
            this.isReleased = true;
        }

        public boolean isReleased() {
            return isReleased;
        }
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private MyMediaMetadataRetriever mediaMetadataRetriever;
        private long videoDurationInMs;
        private LruBitmapCache bitmapCache;
        private float pixelsPerSecond;
        private int photoWidth;

        public Adapter(MyMediaMetadataRetriever mediaMetadataRetriever, LruBitmapCache bitmapCache, int photoWidth) {
            this.mediaMetadataRetriever = mediaMetadataRetriever;
            this.bitmapCache = bitmapCache;
            this.photoWidth = photoWidth;
            videoDurationInMs = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            ImageView photoView = new ImageView(viewGroup.getContext());
            photoView.setLayoutParams(new LayoutParams(photoWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            if (viewHolder.extractBitmap != null) {
                viewHolder.extractBitmap.cancel(true);
            }

            long frameAtInUSecond = Math.min((long) (photoWidth / pixelsPerSecond) * i * 1000000l, videoDurationInMs * 1000l);

            Bitmap bitmap = bitmapCache.get(frameAtInUSecond);
            ((ImageView) viewHolder.itemView).setImageBitmap(bitmap);

            if (bitmap == null) {
                viewHolder.extractBitmap =
                        new ExtractBitmap(mediaMetadataRetriever, bitmapCache, (ImageView) viewHolder.itemView, frameAtInUSecond);

                viewHolder.extractBitmap.execute();
            }
        }

        @Override
        public int getItemCount() {
            return (int) Math.ceil((videoDurationInMs / (photoWidth / pixelsPerSecond) / 1000f));
        }

        public void setPixelsPerSecond(float nbPixelsForOneSecond) {
            if (pixelsPerSecond != nbPixelsForOneSecond) {
                pixelsPerSecond = nbPixelsForOneSecond;
                notifyDataSetChanged();
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ExtractBitmap extractBitmap;
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        private static class ExtractBitmap extends AsyncTask<Void, Void, Bitmap> {

            private final LruBitmapCache bitmapCache;
            private MyMediaMetadataRetriever mediaMetadataRetriever;
            private ImageView imageView;
            private long frameAtTime;

            public ExtractBitmap(MyMediaMetadataRetriever mediaMetadataRetriever, LruBitmapCache bitmapCache, ImageView imageView, long frameAtTime) {
                this.mediaMetadataRetriever = mediaMetadataRetriever;
                this.bitmapCache = bitmapCache;
                this.imageView = imageView;
                this.frameAtTime = frameAtTime;
            }

            private Bitmap scaleBitmapAndKeepRation(Bitmap TargetBmp,int reqHeightInPixels,int reqWidthInPixels)
            {
                Matrix m = new Matrix();
                m.setRectToRect(new RectF(0, 0, TargetBmp.getWidth(), TargetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
                return Bitmap.createBitmap(TargetBmp, 0, 0, TargetBmp.getWidth(), TargetBmp.getHeight(), m, true);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                if (mediaMetadataRetriever.isReleased() || isCancelled()) {
                    return null;
                }

                Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(frameAtTime);
                if (isCancelled()) {
                    bitmap.recycle();
                    return null;
                }

                Bitmap scaled = scaleBitmapAndKeepRation(bitmap, 200, 200);

                if (scaled != bitmap) {
                    bitmap.recycle();
                }

                return scaled;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                bitmapCache.put(frameAtTime, bitmap);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private static class LruBitmapCache extends android.support.v4.util.LruCache<Long, Bitmap> {

        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(Long key, Bitmap value) {
            return (int) (value.getRowBytes() * value.getHeight()/ 1024f);
        }
    }
}
