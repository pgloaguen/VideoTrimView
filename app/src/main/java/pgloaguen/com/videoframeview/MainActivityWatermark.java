package pgloaguen.com.videoframeview;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.vistrav.ask.Ask;

import java.util.List;

public class MainActivityWatermark extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermark);
        Ask.on(this)
                .forPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) //one or more permissions
                .withRationales(
                        "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission"
                        ) //optional
                .when(new Ask.Permission() {
                    @Override
                    public void granted(List<String> permissions) {
//                        System.out.println("__granted__" + Arrays.asList(permissions).toString());
                        //TODO manage cases
                    }

                    @Override
                    public void denied(List<String> permissions) {
//                        System.out.println("__denied__" + Arrays.asList(permissions).toString());
                        //TODO manage cases
                    }
                }).go();
        loadBinary();
    }


    private FFmpeg ffmpeg;

    private void loadBinary() {

        if (ffmpeg != null && ffmpeg.isFFmpegCommandRunning()) ffmpeg.killRunningProcesses();

        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    System.out.println("___failed ffmpeg");
                }

                @Override
                public void onSuccess() {
                    System.out.println("___success ffmpeg");
                    doOverlay();
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
        }
    }

    private void doOverlay() {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        String gettingFrames = generateFFMPEGForOverlay("", "", 90);
        try {
            ffmpeg.execute(gettingFrames, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    System.out.println("___start");
                }

                @Override
                public void onProgress(String message) {
                    System.out.println("___progress " + message);
                }

                @Override
                public void onFailure(String message) {
                    System.out.println("___failure " + message);
                }

                @Override
                public void onSuccess(String message) {
                    System.out.println("___success " + message);
                }

                @Override
                public void onFinish() {
                    System.out.println("___finish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            System.out.println("___error " + e.getMessage());
        }
    }


    public static String generateFFMPEGForOverlay(String in, String out, int rotation) {
//      return "-i /storage/emulated/0/Movies/tsū/tsu_2016-05-13_115102.mp4 -ss 00:00:03 -vframes 1 /storage/emulated/0/Movies/tsū/out.png";
//      return "–v frames 1 –i /storage/emulated/0/Movies/tsū/tsu_2016-04-06_203955.mp4 –f image2 /storage/emulated/0/Movies/tsū/out.bmp";
//        time for i in {0..39} ; do ffmpeg -accurate_seek -ss `echo $i*60.0 | bc` -i input.mp4   -frames:v 1 period_down_$i.bmp ; done
//        (rotation == 0 ? (rotation == 180 ? "" : "-vf transpose=" + (rotation == 90 ? "1 " : "2 "))

        return "-i /storage/emulated/0/Movies/tsū/tsu_2016-05-13_115102.mp4 " +
                "-i /storage/emulated/0/Movies/tsū/tsu_2016-06-01_152324.mp4 " +
                "-filter_complex " +
                "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2 " +
                "-codec:a copy " +
                "-strict -2 "+
                "/storage/emulated/0/Movies/tsū/tsu_result.mp4";
    }

}
