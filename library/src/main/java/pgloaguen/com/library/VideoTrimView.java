package pgloaguen.com.library;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by root on 13/08/15.
 */
public class VideoTrimView extends FrameLayout implements GestureDetector.OnGestureListener {

    private VideoFrameView videoFrameView;
    private ImageView mCursorLeftView;
    private ImageView mCursorRightView;

    private GestureDetector gestureDetector;

    private final float minTrimInSecond = 3;
    private float widthInSecond = 15;
    private float minBetweenCursorInPercent = minTrimInSecond / widthInSecond;

    private float cursorLeftX = 0f;
    private float cursorRightX = 1.0f;
    private int demiCursorWidth = 40;

    private onTrimPositionListener onTrimPositionListener;

    private boolean hasAVideo;

    public VideoTrimView(Context context) {
        super(context);
        init();
    }

    public VideoTrimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTrimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        videoFrameView = new VideoFrameView(getContext());
        mCursorLeftView = new ImageView(getContext());
        mCursorRightView = new ImageView(getContext());
        gestureDetector = new GestureDetector(getContext(), this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(videoFrameView);

        View borderView = new View(getContext());
        borderView.setBackgroundResource(R.drawable.border_trim_video);
        addView(borderView);

        mCursorLeftView.setBackgroundResource(R.drawable.trim_video_left);
        mCursorRightView.setBackgroundResource(R.drawable.trim_video_right);

        addView(mCursorLeftView);
        addView(mCursorRightView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int heigth = bottom - top;
        int width = (right - left);
        System.out.println("witdh___"+width);

        int rightCursorLeftPosition = (int) ((cursorLeftX * width) + demiCursorWidth);
        System.out.println("______left____"+rightCursorLeftPosition);
        if (rightCursorLeftPosition<80)
            rightCursorLeftPosition = 30;
        mCursorLeftView.layout(rightCursorLeftPosition - width, 0, rightCursorLeftPosition, heigth);

        int leftCursorRightPosition = (int) ((cursorRightX * width) - demiCursorWidth);
        System.out.println("______right____"+rightCursorLeftPosition);
        if (leftCursorRightPosition>getWidth()-80)
            leftCursorRightPosition = getWidth()-30;
        mCursorRightView.layout(leftCursorRightPosition, 0, leftCursorRightPosition + width, heigth);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (hasAVideo) {
            float startInSecond = videoFrameView.getStartInMs();
            onTrimPositionListener.onTrimPositionUpdated(startInSecond + (widthInSecond * cursorLeftX),
                    Math.min(videoFrameView.getVideoDurationInMs() / 1000f, startInSecond + (widthInSecond * cursorRightX)));
        }

        if(!gestureDetector.onTouchEvent(event)) {
            return super.dispatchTouchEvent(event);
        }

        return true;
    }

    public void setWidthInSecond(float second) {
        widthInSecond = second;
        minBetweenCursorInPercent =  minTrimInSecond / widthInSecond;
        videoFrameView.setWidthInSecond(second);
    }

    public void setOnTrimPositionListener(onTrimPositionListener listener) {
        onTrimPositionListener = listener;
    }

    public void setVideo(Uri uri) {
        hasAVideo = uri != null;
        videoFrameView.setVideo(uri);
    }

    public void setVideo(File file) {
        hasAVideo = file != null;
        videoFrameView.setVideo(file);
    }

    public void setVideo(String path) {
        hasAVideo = path != null;
        videoFrameView.setVideo(path);
    }

    private boolean isCursorLeftTouch(float x) {
        return x < mCursorLeftView.getRight();
    }

    private boolean isCursorRightTouch(float x) {
        return x > mCursorRightView.getLeft();
    }

    private boolean isCursorLeftTriggeredScroll;
    private boolean isCursorRightTriggeredScroll;

    @Override
    public boolean onDown(MotionEvent e) {
        isCursorLeftTriggeredScroll = isCursorLeftTouch(e.getX());
        isCursorRightTriggeredScroll = isCursorRightTouch(e.getX());
        return isCursorLeftTriggeredScroll || isCursorRightTriggeredScroll;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isCursorLeftTriggeredScroll) {
            cursorLeftX -= distanceX/getWidth();
            cursorLeftX = Math.max(Math.min(cursorLeftX, cursorRightX - minBetweenCursorInPercent), 0f);
            requestLayout();
            return true;
        } else if (isCursorRightTriggeredScroll) {
            cursorRightX -= distanceX/getWidth();
            cursorRightX = Math.min(Math.max(cursorRightX, cursorLeftX + minBetweenCursorInPercent), 1.0f);
            requestLayout();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public interface onTrimPositionListener {
        void onTrimPositionUpdated(float startInS, float endInS);
    }
}
