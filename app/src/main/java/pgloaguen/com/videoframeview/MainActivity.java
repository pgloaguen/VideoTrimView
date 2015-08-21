package pgloaguen.com.videoframeview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pgloaguen.com.library.VideoFrameView;
import pgloaguen.com.library.VideoTrimView;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 1000;

    private VideoTrimView videoTrimView;
    private TextView rangeTextView;
    private VideoFrameView videoFrameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoTrimView = (VideoTrimView) findViewById(R.id.videotrim);
        videoFrameView = (VideoFrameView) findViewById(R.id.videoframe);
        rangeTextView = (TextView) findViewById(R.id.range);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("video/*");
                startActivityForResult(photoPickerIntent, SELECT_VIDEO);
            }
        });

        videoTrimView.setOnTrimPositionListener(new VideoTrimView.onTrimPositionListener() {
            @Override
            public void onTrimPositionUpdated(float startInS, float endInS) {
                rangeTextView.setText(startInS + " " + endInS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == SELECT_VIDEO){
            Uri videoUri = data.getData();
            videoTrimView.setVideo(videoUri);
            videoTrimView.setWidthInSecond(15);

            videoFrameView.setVideo(videoUri);
            videoFrameView.setWidthInSecond(15);
            videoFrameView.setDelegateAdapter(new VideoFrameView.FrameAdapterDelegate<FrameViewHolder>() {

                private int selectedPosition = 0;

                @Override
                public FrameViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                    return new FrameViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_frame, viewGroup, false));
                }

                @Override
                public void onBindViewHolder(final FrameViewHolder viewHolder, final int i) {
                    viewHolder.borderView.setVisibility( i != selectedPosition ? View.GONE : View.VISIBLE);
                    viewHolder.imgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedPosition = i;
                            videoFrameView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public ImageView getImageViewToDisplayFrame(FrameViewHolder viewHolder) {
                    return viewHolder.imgView;
                }

                @Override
                public int getPhotoWidth() {
                    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, getResources().getDisplayMetrics());
                }
            });

        }
    }

    private static class FrameViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgView;
        private View borderView;

        public FrameViewHolder(View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.img);
            borderView = itemView.findViewById(R.id.border);
        }
    }
}
