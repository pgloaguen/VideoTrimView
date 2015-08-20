package pgloaguen.com.library;

import android.content.Context;
import android.graphics.Color;
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
    private View mCursorLeftView;
    private View mCursorRightView;

    private GestureDetector gestureDetector;

    private final float minTrimInSecond = 3;
    private final float widthInSecond = 15;
    private final float minBetweenCursorInPercent = minTrimInSecond / widthInSecond;

    private float cursorLeftX = 0f;
    private float cursorRightX = 1.0f;
    private int demiCursorWidth = 40;

    private onTrimPositionListener onTrimPositionListener;


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
        setBackgroundColor(Color.RED);
        addView(videoFrameView);

        mCursorLeftView.setBackgroundColor(Color.GREEN);
        mCursorRightView.setBackgroundColor(Color.YELLOW);

        addView(mCursorLeftView);
        addView(mCursorRightView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int heigth = bottom - top;
        int width = (right - left);

        int rightCursorLeftPosition = (int) ((cursorLeftX * width) + demiCursorWidth);
        mCursorLeftView.layout(rightCursorLeftPosition - width, 0, rightCursorLeftPosition, heigth);

        int leftCursorRightPosition = (int) ((cursorRightX * width) - demiCursorWidth);
        mCursorRightView.layout(leftCursorRightPosition, 0, leftCursorRightPosition + width, heigth);

        videoFrameView.setPixelsPerSecond(width / widthInSecond);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float startInSecond = videoFrameView.getStartInMs();
        onTrimPositionListener.onTrimPositionUpdated(startInSecond + (widthInSecond * cursorLeftX),
                Math.min(videoFrameView.getVideoDurationInMs()/1000f, startInSecond + (widthInSecond * cursorRightX)));

        if(!gestureDetector.onTouchEvent(event)) {
            return super.dispatchTouchEvent(event);
        }

        return true;
    }

    public void setOnTrimPositionListener(onTrimPositionListener listener) {
        onTrimPositionListener = listener;
    }

    public void setVideo(Uri uri) {
        videoFrameView.setVideo(uri);
    }

    public void setVideo(File file) {
        videoFrameView.setVideo(file);
    }

    public void setVideo(String path) {
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
