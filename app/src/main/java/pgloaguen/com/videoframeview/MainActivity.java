package pgloaguen.com.videoframeview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import pgloaguen.com.library.VideoFrameView;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_VIDEO = 1000;

    private VideoFrameView videoFrameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoFrameView = (VideoFrameView) findViewById(R.id.videoframe);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("video/*");
                startActivityForResult(photoPickerIntent, SELECT_VIDEO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == SELECT_VIDEO){
            Uri videoUri = data.getData();
            videoFrameView.setVideo(videoUri);

        }
    }
}
