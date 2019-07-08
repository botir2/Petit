package open.it.com.petit.NotUse;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import org.freedesktop.gstreamer.GStreamer;

import open.it.com.petit.R;

public class Tutorial4 extends AppCompatActivity implements SurfaceHolder.Callback{
    public final static String TAG = "Tutorial4";
    private final int MEDIA_SAVE_START = 1;
    private final int CAPTURE_STOP = 2;

    private native void nativeInit(String url);     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativeSetUri(String uri); // Set the URI of the media to play
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativeSetPosition(int milliseconds); // Seek to the indicated position, in milliseconds
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface); // A new surface is a3evailable
    private native void nativeSurfaceFinalize(); // Surface about to be destroyed
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private int position;                 // Current position, reported by native code
    private int duration;                 // Current clip duration, reported by native code
    private boolean is_local_media;       // Whether this clip is stored locally or is being streamed
    private int desired_position;         // Position where the users wants to seek to
    private String mediaUri;              // URI of the clip being played

    private ImageButton btn_provide_food;
    private ImageButton btn_flash;
    private ImageButton btn_capture;
    private ImageButton btn_video_record;
    private ImageButton btn_audio_record;
    private ImageButton btn_audio_start;
    private ImageButton btn_close;

    private GStreamerSurfaceView sv;
    private SurfaceHolder sh;
    private GStreamerSurfaceView gsv;

    private static final int REQUEST_CODE = 1000;
    private int screenDensity;

    private MediaHandler handler;
    private PetitMediaRecord recoding;
    private PetitMediaMqtt mqtt;
    private PetitMediaCapture capture;
    private PetitMediaAudio audio;

    private DisplayMetrics displayMetrics;
    private Display display;

    private int devWidth;
    private int devHeight;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_media_activity);
        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewInit();

        handler = new MediaHandler();
        audio = new PetitMediaAudio();
        recoding = new PetitMediaRecord(this, handler, sh, screenDensity);
        capture = new PetitMediaCapture(this, handler, screenDensity, displayMetrics, display);
        mqtt = new PetitMediaMqtt(this);
        mqtt.connectDevice();

        // Retrieve our previous state, or initialize it to default values
        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            position = savedInstanceState.getInt("position");
            duration = savedInstanceState.getInt("duration");
            mediaUri = savedInstanceState.getString("mediaUri");
            Log.i ("GStreamer", "Activity created with saved state:");
        } else {
            is_playing_desired = false;
            position = duration = 0;
            Log.i ("GStreamer", "Activity created with no saved state:");
        }
        is_local_media = false;
        Log.i ("GStreamer", "  playing:" + is_playing_desired + " position:" + position +
                " duration: " + duration + " uri: " + mediaUri);

        nativeInit("rtsp://211.38.86.93:1935/live/myStream");

        btn_provide_food.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, getPhoneNum());
                    mqtt.provideFood("$open-it/pet-it/order/motor", "on" + getPhoneNum());
                    btn_provide_food.setImageResource(R.drawable.food_icon01_on);
                }else if(action == MotionEvent.ACTION_UP) {
                    btn_provide_food.setImageResource(R.drawable.food_icon01_off);
                }
                return true;
            }
        });

        btn_flash.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    mqtt.lightOnOff(mqtt.lightState);
                    btn_flash.setImageResource(R.drawable.food_icon02_off);
                }else if(action == MotionEvent.ACTION_UP) {
                    btn_flash.setImageResource(R.drawable.food_icon02_on);
                }
                return true;
            }
        });

        btn_capture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    capture.prepare(MediaSave.CAPTURE);
                    btn_capture.setImageResource(R.drawable.food_icon03_off);
                    capture.stop("캡쳐");
                }else if(action == MotionEvent.ACTION_UP) {
                    btn_capture.setImageResource(R.drawable.food_icon03_on);
                }
                return true;
            }
        });

        btn_video_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recoding.isRecoding) {
                    recoding.prepare(MediaSave.RECORD);
                    btn_video_record.setImageResource(R.drawable.food_icon04_off);
                } else {
                    recoding.stop("녹화");
                    btn_video_record.setImageResource(R.drawable.food_icon04_on);
                }
            }
        });

        btn_audio_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    btn_audio_record.setImageResource(R.drawable.food_icon05_off);
                }else if(action == MotionEvent.ACTION_UP) {
                    btn_audio_record.setImageResource(R.drawable.food_icon05_on);
                }
                return true;
            }
        });

        btn_audio_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_DOWN) {
                    audio.send_audio_start();
                    btn_audio_start.setImageResource(R.drawable.food_mic_off);
                }else if(action == MotionEvent.ACTION_UP) {
                    audio.send_audio_stop();
                    btn_audio_start.setImageResource(R.drawable.food_mic_on);
                }
                return true;
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void viewInit() {
        btn_provide_food = (ImageButton) findViewById(R.id.btn_provide_food);
        btn_flash = (ImageButton) findViewById(R.id.btn_flash);
        btn_capture = (ImageButton) findViewById(R.id.btn_capture);
        btn_video_record = (ImageButton) findViewById(R.id.btn_video_record);
        btn_audio_record = (ImageButton) findViewById(R.id.btn_audio_record);
        btn_audio_start = (ImageButton) findViewById(R.id.btn_audio_start);
        btn_close = (ImageButton) findViewById(R.id.btn_close);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;*/

        displayMetrics = getResources().getDisplayMetrics();
        screenDensity = displayMetrics.densityDpi;
        display = getWindowManager().getDefaultDisplay();
        Log.d(TAG, "screenDensity : " + screenDensity);
        sv = (GStreamerSurfaceView) this.findViewById(R.id.surface_video);
        sh = sv.getHolder();
        sh.addCallback(this);

        devWidth = displayMetrics.widthPixels;
        devHeight = displayMetrics.heightPixels;
        sh.setFixedSize(devWidth, devHeight + 350);
    }

    private String getPhoneNum() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number().replace("+82", "0");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "권한에러", Toast.LENGTH_SHORT).show();
            return;
        }

        //권한알람 사라질때까지
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (MediaSave.MEDIA_ACT == MediaSave.RECORD) {
            recoding.start(resultCode, data);
        } else if (MediaSave.MEDIA_ACT == MediaSave.CAPTURE) {
            capture.start(resultCode, data);
        } else if (MediaSave.MEDIA_ACT == MediaSave.NONE) {
            Toast.makeText(this, "다시 시도해 주세요", Toast.LENGTH_SHORT).show();
        }
    }

    /**************************** LifeCycle *********************************************/
    @Override
    protected void onStop() {
        super.onStop();
        if (recoding.isRecoding)
            recoding.stop("녹화");

        nativePause();
        //disConnectMqtt();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        nativeInit("rtsp://211.38.86.94:8554/test");
        nativePlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nativeSurfaceFinalize();
        if (sv != null)
            sv = null;
        nativeFinalize();
        mqtt.disConnectMqtt();
    }

    /***************************** native 영역 **********************************/
    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {}

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "GStreamer initialized:");
        Log.i ("GStreamer", "  playing:" + is_playing_desired + " position:" + position + " uri: " + mediaUri);

        // Restore previous playing state
        nativePlay();
    }

    // Called from native code
    private void setCurrentPosition(final int position, final int duration) {}

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("tutorial-4");
        nativeClassInit();
    }

    /******************************* surface onCompute **********************************/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
    }

    // Called from native code when the size of the media changes or is first detected.
    // Inform the video surface about the new size and recalculate the layout.
    private void onMediaSizeChanged (int width, int height) {
       /* Log.i ("GStreamer", "Media size changed to " + width + "x" + height);
        gsv = (GStreamerSurfaceView) this.findViewById(R.id.surface_video);
        gsv.media_width = width;
        gsv.media_height = height;

        runOnUiThread(new Runnable() {
            public void run() {
                gsv.requestLayout();
            }
        });*/
    }

    /* 핸들러 */
    class MediaHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MediaProjectionManager mpManager;
            switch (msg.what) {
                case MEDIA_SAVE_START:
                    mpManager = (MediaProjectionManager) msg.obj;
                    startActivityForResult(mpManager.createScreenCaptureIntent(), REQUEST_CODE);
                    break;
                case CAPTURE_STOP:
                    capture.stop("캡쳐");
                    break;
            }
        }
    }
}
