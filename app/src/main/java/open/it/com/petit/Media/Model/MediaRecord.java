package open.it.com.petit.Media.Model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;

import open.it.com.petit.EventBus.BusProvider;
import open.it.com.petit.Media.Event.ProjectionEvent;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by user on 2018-01-15.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaRecord extends BaseMediaProjection{
    private static final String TAG = MediaRecord.class.getSimpleName();
    private MediaRecorder recorder;
    private SurfaceHolder sh;

    public MediaRecord(Context context, SurfaceHolder sh, int screenDensity, int act) {
        super(context, screenDensity, act);
        this.sh = sh;
    }

    // 권한요구
    public void retrievePermission() {
        Log.d(TAG, "retrievePermission");
        BusProvider.getInstance().post(new ProjectionEvent(mpManager, BaseMediaProjection.RECORD));
    }

    // media recorder 초기화
    private void initialize() {
        try {
            recorder = new MediaRecorder();
            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoEncodingBitRate(512 * 1000);
            recorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            recorder.setVideoFrameRate(30);
            recorder.setPreviewDisplay(sh.getSurface());
            recorder.setOutputFile(fileName);
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            recorder.release();
            recorder = null;
        }
    }

    // 녹화 시작 method
    @Override
    public void start(int resultCode, Intent data) {
        Toast.makeText(context, "녹화를 시작합니다.", Toast.LENGTH_SHORT).show();
        initialize();
        mp = mpManager.getMediaProjection(resultCode, data);
        mp.registerCallback(mpCallback, null);
        virtualDisplay = createVirtualDisplay();
        recorder.start();
    }

    // 녹화 중지 method
    @Override
    public void stop() {
        if (recorder == null)
            return;

        recorder.stop();
        recorder.reset();
        recorder = null;
        Toast.makeText(context, "녹화를 종료합니다.", Toast.LENGTH_SHORT).show();
        freeMediaObject();
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        return mp.createVirtualDisplay("recording",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                recorder.getSurface(), null, null);
    }

    private void freeMediaObject() {
        if (virtualDisplay == null) {
            return;
        } else {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if(mp != null) {
            mp.unregisterCallback(mpCallback);
            mp.stop();
            mp = null;
        }
    }

    final MediaProjection.Callback mpCallback = new MediaProjection.Callback() {
        // 녹화 중지 시 발생하는 콜백
        @Override
        public void onStop() {
            super.onStop();
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
            }

            if (mp != null) {
                mp = null;
            }
        }
    };

    public MediaRecorder getRecorder() {
        return recorder;
    }
}
