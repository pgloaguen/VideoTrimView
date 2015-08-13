package pgloaguen.com.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by root on 13/08/15.
 */
public class VideoTrimView extends FrameLayout {

    private VideoFrameView videoFrameView;

    public VideoTrimView(Context context) {
        super(context);
    }

    public VideoTrimView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTrimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        videoFrameView = new VideoFrameView(getContext());
        addView(videoFrameView);
    }
}
