package open.it.com.petit.NotUse;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017-07-05.
 */

public class PetitMediaRecord extends MediaSave{
    public boolean isRecoding = false;
    public PetitMediaRecord(Context context, Handler handler, SurfaceHolder sh, int screenDensity) {
        super(context, handler, sh, screenDensity);
    }

    @Override
    protected void initMedia() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "외장 메모리가 마운트되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            //EXTERNAL_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        }

        try {
            Log.d(TAG, EXTERNAL_STORAGE_PATH);
            isRecoding = true;

            recorder = new MediaRecorder();

            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoEncodingBitRate(512 * 1000);
            recorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            recorder.setVideoFrameRate(30);
            recorder.setPreviewDisplay(sh.getSurface());
            recorder.setOutputFile(createFileName());
            recorder.prepare();
        } catch (Exception e) {
            Log.d(TAG, "init exception");
            e.printStackTrace();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        return mp.createVirtualDisplay("Tutorial4",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                recorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    @Override
    public void start(int resultCode, Intent data) {
        initMedia();
        mpCallback = new MediaProjectionCallback();
        mp = mpManager.getMediaProjection(resultCode, data);
        mp.registerCallback(mpCallback, null);
        vd = createVirtualDisplay();
        recorder.start();
        Toast.makeText(context, "녹화시작. 버튼을 한번 더 누르면 녹화가 종료됩니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stop(String msg) {
        isRecoding = false;
        if (recorder == null) {
            return;
        }

        recorder.stop();
        recorder.reset();
        recorder = null;
        freeMediaObject(msg);
    }

    @Override
    protected String createFileName() {
        String fName = null;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            fName = "Petit_" + sdf.format(date) + ".mp4";
        } else {
            fName = EXTERNAL_STORAGE_PATH + "/Petit_" + sdf.format(date) + ".mp4";
        }

        return fName;
    }
}
