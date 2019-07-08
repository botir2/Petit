package open.it.com.petit.NotUse;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

/**
 * Created by user on 2017-07-05.
 */

public abstract class MediaSave {
    public final static String TAG = "MediaSave";
    protected String EXTERNAL_STORAGE_PATH;
    private final int MEDIA_SAVE_START = 1;
    protected final int CAPTURE_STOP = 2;

    public final static int RECORD = 1;
    public final static int CAPTURE = 2;
    public final static int NONE = 3;
    public static int MEDIA_ACT = NONE;

    protected Context context;
    protected Handler handler;
    protected MediaRecorder recorder;
    protected SurfaceHolder sh;

    protected MediaProjectionManager mpManager;
    protected MediaProjection mp;
    protected MediaProjectionCallback mpCallback;
    protected VirtualDisplay vd;

    protected int screenDensity;
    protected static final int DISPLAY_WIDTH = 720;
    protected static final int DISPLAY_HEIGHT = 1280;

    public MediaSave(Context context, Handler handler, SurfaceHolder sh, int screenDensity) {
        this.context = context;
        this.handler = handler;
        this.screenDensity = screenDensity;
        this.sh = sh;
        this.mpManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public MediaSave(Context context, Handler handler, int screenDensity) {
        this.context = context;
        this.handler = handler;
        this.mpManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.v(TAG, "Stopped");
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
            }
            if (mp != null) {
                mp = null;
            }
        }
    }

    public void prepare(int mediaAct) {
        MEDIA_ACT = mediaAct;
        if (mp == null) {
            Message msg = handler.obtainMessage();
            msg.obj = mpManager;
            msg.what = MEDIA_SAVE_START;
            handler.sendMessage(msg);
            return;
        } else {
            Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void freeMediaObject(String msg) {
        if (vd == null) {
            return;
        } else {
            vd.release();
            vd = null;
        }

        if(mp != null) {
            mp.unregisterCallback(mpCallback);
            mp.stop();
            mp = null;
        }
        Toast.makeText(context, msg + "가 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public abstract void start(int resultCode, Intent data);
    public abstract void stop(String msg);
    protected abstract void initMedia();
    protected abstract VirtualDisplay createVirtualDisplay();
    protected abstract String createFileName();

}
