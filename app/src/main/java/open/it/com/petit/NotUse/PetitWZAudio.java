package open.it.com.petit.NotUse;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WZBroadcastConfig;
import com.wowza.gocoder.sdk.api.devices.WZAudioDevice;
import com.wowza.gocoder.sdk.api.errors.WZError;
import com.wowza.gocoder.sdk.api.errors.WZStreamingError;
import com.wowza.gocoder.sdk.api.logging.WZLog;
import com.wowza.gocoder.sdk.api.status.WZStatus;
import com.wowza.gocoder.sdk.api.status.WZStatusCallback;

/**
 * Created by user on 2017-10-11.
 */

public class PetitWZAudio implements WZStatusCallback{
    private final static String TAG = PetitWZAudio.class.getSimpleName();
    private static final String SDK_SAMPLE_APP_LICENSE_KEY = "GOSK-A144-0103-6C17-1EF1-FD57";

    private WowzaGoCoder goCoder;
    private WZAudioDevice goCoderAudioDevice;
    private WZBroadcast goCoderBroadcaster;
    private WZBroadcastConfig goCoderBroadcastConfig;

    private Context context;
    private static Object sBroadcastLock = new Object();
    private static boolean sBroadcastEnded = true;

    private String GUID;

    public PetitWZAudio(Context context, String GUID) {
        this.context = context;
        this.GUID = GUID;
    }

    public void executeAudio() {

        init();
        WZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(context, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else {
            goCoderAudioDevice.setAudioEnabled(false);
            goCoderAudioDevice.setAudioPaused(false);

            Log.d(TAG, "방송시작");
            // Start streaming
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
            Log.d(TAG, "audio paused " + goCoderAudioDevice.isAudioPaused());
            Log.d(TAG, "audio enabled " + goCoderAudioDevice.isAudioEnabled());
        }

        Log.d(TAG, "isIdle?" + isIdle());
    }

    private void init() {
        goCoder = WowzaGoCoder.init(context, SDK_SAMPLE_APP_LICENSE_KEY);
        if (goCoder == null) {
            WZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(context,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        goCoderAudioDevice = new WZAudioDevice();
        goCoderBroadcaster = new WZBroadcast();
        goCoderBroadcastConfig = new WZBroadcastConfig();

        goCoderBroadcastConfig.setHostAddress("211.38.86.93");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("live");
        goCoderBroadcastConfig.setStreamName(GUID + "/audio");

        goCoderBroadcastConfig.setVideoEnabled(false);
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);
    }

    public void setMute(boolean mute) {
        goCoderAudioDevice.setAudioPaused(mute);
        Log.d(TAG, "audio " + goCoderAudioDevice.isMuted());
    }

    public boolean isIdle() {
        return goCoderBroadcaster.getStatus().isRunning();
    }

    public synchronized void endBroadcast(boolean appPausing) {
        if (!goCoderBroadcaster.getStatus().isIdle()) {
            if (appPausing) {
                sBroadcastEnded = false;
                goCoderBroadcaster.endBroadcast(new WZStatusCallback() {
                    @Override
                    public void onWZStatus(WZStatus wzStatus) {
                        synchronized (sBroadcastLock) {
                            sBroadcastEnded = true;
                            sBroadcastLock.notifyAll();
                        }
                        Log.d(TAG, "방송종료");
                    }

                    @Override
                    public void onWZError(WZStatus wzStatus) {
                        synchronized (sBroadcastLock) {
                            sBroadcastEnded = true;
                            sBroadcastLock.notifyAll();
                        }
                        Log.d(TAG, "방송종료 err");
                    }
                });

                while (!sBroadcastEnded) {
                    try {
                        sBroadcastLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                goCoderBroadcaster.endBroadcast(this);
                Log.d(TAG, "방송종료 hi");
            }
        } else {
            WZLog.error(TAG, "endBroadcast() called without an active broadcast");
        }
    }

    public void finish() {
        if (goCoder != null)
            goCoder = null;
        if (goCoderAudioDevice != null)
            goCoderAudioDevice = null;
        if (goCoderBroadcastConfig != null)
            goCoderBroadcastConfig = null;
        if (goCoderBroadcaster != null)
            goCoderBroadcastConfig = null;
    }

    @Override
    public void onWZStatus(final WZStatus goCoderStatus) {
        /*new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "음성시작", Toast.LENGTH_SHORT).show();
            }
        });*/
        goCoderAudioDevice.setAudioPaused(true);
        Log.d(TAG, "isIdle??" + isIdle());
    }

    @Override
    public void onWZError(final WZStatus goCoderStatus) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "에러라능..", Toast.LENGTH_SHORT).show();
                WZLog.error(TAG, goCoderStatus.getLastError());
            }
        });
        Log.d(TAG, "error");
    }
}
