package open.it.com.petit.Media.Tmp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import open.it.com.petit.EventBus.BusProvider;
import open.it.com.petit.Media.Model.MediaCapture;
import open.it.com.petit.Media.Event.ProjectionEvent;
import open.it.com.petit.Media.Model.AudioStream;
import open.it.com.petit.Media.Model.BaseMediaProjection;
import open.it.com.petit.Media.Model.MediaRecord;
import open.it.com.petit.R;
import open.it.com.petit.Util.Util;



/**
 * Created by user on 2017-09-25.
 */

public class MediaActivityTmp extends AppCompatActivity
        implements IVLCVout.Callback, MqttCallbackExtended, IMqttActionListener, View.OnClickListener {
    public final static String TAG = MediaActivityTmp.class.getSimpleName();

    private String url;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private FrameLayout videoSurfaceFrame;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private AudioStream audio;

    private MediaRecord mediaRecord;
    private MediaCapture mediaCapture;

    private DisplayMetrics displayMetrics;
    private Display display;

    private int videoWidth;
    private int videoHeight;
    private int screenDensity;

    private boolean isRunning = false;
    private boolean isRecording = false;
    private int count = 0;

    private ImageButton audioStartBtn;
    private ImageButton mediaCloseBtn;
    private ImageButton feedOnceBtn;
    private ImageButton flashBtn;
    private ImageButton captureBtn;
    private ImageButton recordBtn;
    private ImageButton playbackBtn;
    private ProgressBar pb;

    private MqttAndroidClient mqttAndroidClient;
    private String mqttUrl;
    private String clientId = Util.getDate();
    private String GUID;
    private String arriveMsg = ""; //subscribe 받은 msg를 저장

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(saveInstanceState);
        setContentView(R.layout.petit_media_activity_tmp);

        bindView();

        GUID = getIntent().getStringExtra("GUID");
        url = "rtsp://211.38.86.93:1935/live/" + GUID;
        mqttUrl = getResources().getString(R.string.mqtt_host);

        displayMetrics = getResources().getDisplayMetrics();
        screenDensity = displayMetrics.densityDpi;
        display = getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaRecord = new MediaRecord(this, holder, screenDensity, BaseMediaProjection.RECORD);
        }
        audio = new AudioStream(this, GUID);
        audio.initialize();

        BusProvider.getInstance().register(this); // onActivityResult를 불러오기 위한 BUS
        audio.startStream();
        mqttConnect();

        // feed 버튼
        feedOnceBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    publish("feeder1" + Util.getPhoneNum(getApplicationContext()));
                    feedOnceBtn.setImageResource(R.drawable.food_icon01_on);
                } else if (act == MotionEvent.ACTION_UP) {
                    feedOnceBtn.setImageResource(R.drawable.food_icon01_off);
                }
                return true;
            }
        });

        // not define
        flashBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    flashBtn.setImageResource(R.drawable.food_icon02_off);
                } else if (act == MotionEvent.ACTION_UP) {
                    flashBtn.setImageResource(R.drawable.food_icon02_on);
                }
                return true;
            }
        });

        captureBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // API 20 이하
                        Toast.makeText(MediaActivityTmp.this, "해당 기능을 사용할 수 없는 기기 버전입니다. 기기 업그레이드를 해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        captureBtn.setImageResource(R.drawable.food_icon03_off);
                        mediaCapture = new MediaCapture(getApplicationContext(), screenDensity, BaseMediaProjection.CAPTURE, displayMetrics, display);
                        mediaCapture.retrievePermission(); // 권한요구
                    }
                } else if (act == MotionEvent.ACTION_UP) {
                    captureBtn.setImageResource(R.drawable.food_icon03_on);
                }
                return true;
            }
        });

        // not define
        playbackBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int act = event.getAction();
                if (act == MotionEvent.ACTION_DOWN) {
                    publish("voice");
                    playbackBtn.setImageResource(R.drawable.food_icon05_off);
                } else if (act == MotionEvent.ACTION_UP) {
                    playbackBtn.setImageResource(R.drawable.food_icon05_on);
                }
                return true;
            }
        });

        audioStartBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN) {
                    audio.setMute(false);
                    audioStartBtn.setImageResource(R.drawable.food_mic_on);
                } else if(action == MotionEvent.ACTION_UP) {
                    audio.setMute(true);
                    audioStartBtn.setImageResource(R.drawable.food_mic_off);
                }
                return true;
            }
        });
    }

    private void bindView() {
        surfaceView = (SurfaceView) findViewById(R.id.sv_petit_media);
        holder = surfaceView.getHolder();

        pb = (ProgressBar) findViewById(R.id.media_pb);
        videoSurfaceFrame = (FrameLayout) findViewById(R.id.video_surface_frame);

        audioStartBtn = (ImageButton) findViewById(R.id.audio_start);
        mediaCloseBtn = (ImageButton) findViewById(R.id.media_close);
        feedOnceBtn = (ImageButton) findViewById(R.id.feed_once);
        flashBtn = (ImageButton) findViewById(R.id.flash);
        captureBtn = (ImageButton) findViewById(R.id.capture);
        recordBtn = (ImageButton) findViewById(R.id.record);
        playbackBtn = (ImageButton) findViewById(R.id.playback_audio);

        mediaCloseBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        //setButtonEnable(false);
    }

    private void setButtonEnable(boolean b) {
        audioStartBtn.setEnabled(b);
        feedOnceBtn.setEnabled(b);
        flashBtn.setEnabled(b);
        captureBtn.setEnabled(b);
        recordBtn.setEnabled(b);
        playbackBtn.setEnabled(b);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 종료 버튼
            case R.id.media_close:
                finish();
                break;

            // 녹화 버튼
            case R.id.record:
                Log.d(TAG, "레코드");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Log.d(TAG, "low version");
                    Toast.makeText(MediaActivityTmp.this, "해당 기능을 사용할 수 없는 기기 버전입니다. 기기 업그레이드를 해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (!isRecording) {
                    isRecording = true;
                    recordBtn.setImageResource(R.drawable.food_icon04_off);
                    mediaRecord.retrievePermission();
                } else {
                    isRecording = false;
                    recordBtn.setImageResource(R.drawable.food_icon04_on);
                    mediaRecord.stop();
                }
                break;
        }
    }

    // Bus event subscribe
    @Subscribe
    public void recordLoad(ProjectionEvent event) {
        Log.d(TAG, "recordLoad");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            startActivityForResult(event.getMpManager().createScreenCaptureIntent(), event.getAct()); // onActivitResult call
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode : " + requestCode);
        Log.d(TAG, "resultCode : " + resultCode);
        if (requestCode == BaseMediaProjection.RECORD) {
            //녹화
            if (resultCode == 0) {
                Toast.makeText(this, "녹화가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                recordBtn.setImageResource(R.drawable.food_icon04_on);
                return;
            }
            mediaRecord.start(resultCode, data);
        } else  if (requestCode == BaseMediaProjection.CAPTURE){
            //캡쳐
            mediaCapture.start(resultCode, data);
        }
    }

    /******************************************************Life cycle********************************************************************/
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        audio.getAudioManager().setMode(AudioManager.MODE_IN_COMMUNICATION);
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        audio.getAudioManager().setMode(AudioManager.MODE_NORMAL);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        // 화면 정지시 onDestroy 호출
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy endBroadCast");
        if (mediaRecord != null)
            mediaRecord.stop();
        BusProvider.getInstance().unregister(this); // 버스 해제
        audio.getAudioManager().setMode(AudioManager.MODE_NORMAL); // 미디어 모드 변경
        publish("stop video/" + Util.getPhoneNum(this)); // 스트리밍 중지 요청
        mqttDisConnect(); // mqtt 해제
        // media release시 시간이 오래걸려 thread로 작업
        new Thread(new Runnable() {
            @Override
            public void run() {
                audio.stopStream();
                releasePlayer();
            }
        }).start();
    }

    /******************************************************VLC 영역*******************************************************************/
    /* 화면 전환 onCompute */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setSize(videoWidth,videoHeight);
        Log.d(TAG, "onConfigurationChanged");
    }


    /* 새로운 레이아웃을 요청하면 콜백이 호출됨. */
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        /*Log.d(TAG, "onNewVideoLayout");
        Log.d(TAG, "width ? " + width);
        Log.d(TAG, "height ? " + height);
        Log.d(TAG, "vWidth ? " + visibleWidth);
        Log.d(TAG, "vHeight ? " + visibleHeight);*/
        if (width * height == 0)
            return;

        this.videoHeight = height;
        this.videoWidth = width;
        setSize(visibleWidth, visibleHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.d(TAG, "onHardwareAccelerationError");
        //releasePlayer();
    }

    private void setSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;

        if (videoWidth * videoHeight <= 1)
            return ;

        if (holder == null || surfaceView == null)
            return ;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int tmp = w;
            w = h;
            h = tmp;
        }

        float videoAR = (float) videoWidth / (float) videoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR) {
            h = (int) (w / videoAR);
        } else {
            w = (int) (h * videoAR);
        }

        holder.setFixedSize(videoWidth, videoHeight);
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;

        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    private void createPlayer(String media) {
        releasePlayer();
        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libVLC = new LibVLC(this, options);

            //holder.setKeepScreenOn(false); // 에러 발생

            mediaPlayer = new MediaPlayer(libVLC);
            mediaPlayer.setEventListener(eventListener);
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVLC, Uri.parse(media));
            m.setHWDecoderEnabled(true, true);
            m.addOption("network-caching=1000");
            m.addOption(":clock-jitter=0");
            m.addOption(":clock-synchro=0");

            mediaPlayer.setMedia(m);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libVLC == null)
            return;
        mediaPlayer.pause();
        mediaPlayer.stop();

        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;

        libVLC.release();
        libVLC = null;

        videoWidth = 0;
        videoHeight = 0;
    }

    final MediaPlayer.EventListener eventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            count = event.getVoutCount();
            switch (event.type) {
                case MediaPlayer.Event.Opening:
                    Log.d(TAG, "Opening");
                    break;

                case MediaPlayer.Event.Buffering:
                    Log.d(TAG, "Buffering");
                    break;

                case MediaPlayer.Event.Playing:
                    Log.d(TAG, "Playing");
                    break;

                case MediaPlayer.Event.EncounteredError:
                    pb.setVisibility(View.INVISIBLE);
                    setButtonEnable(false);
                    Toast.makeText(MediaActivityTmp.this, "서버연결 오류. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    break;

                case MediaPlayer.Event.Vout:
                    pb.setVisibility(View.INVISIBLE);
                    setButtonEnable(true);
                    break;

                default:
                    //Log.d(TAG, "default" + Integer.toHexString(event.type));
                    break;
            }
        }
    };

    /********************************************MQTT 영역*******************************************************/
    private void mqttConnect() {
        Log.d(TAG, "connect");
        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(this, mqttUrl, clientId);
            mqttAndroidClient.setCallback(this);

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            final int seconds = 10;
            connectOptions.setConnectionTimeout(seconds);
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(true);

            try {
                mqttAndroidClient.connect(connectOptions, null, this);
            } catch (MqttException e) {
                Log.d(TAG, "MqttException");
                e.printStackTrace();
            }
        }
    }

    private void mqttDisConnect() {
        if (mqttAndroidClient != null) {
            if (mqttAndroidClient.isConnected()) {
                try {
                    mqttAndroidClient.unregisterResources();
                    mqttAndroidClient.close();
                    mqttAndroidClient.disconnect(null, this);
                    mqttAndroidClient = null;
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void publish(String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            message.setQos(0);
            if (mqttAndroidClient != null)
                mqttAndroidClient.publish("$open-it/pet-it/" + GUID + "/order", message);
            Log.d(TAG, "Published to topic : $open-it/pet-it/" + GUID + "/order");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**********************************************MQTT Callback**************************************************/
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "onSuccess");
        try {
            mqttAndroidClient.subscribe("$open-it/pet-it/" + GUID + "/status", 0, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribe onSuccess");
                    Log.d(TAG, "subTopic : $open-it/pet-it/" + GUID + "/status");
                    publish("request video/" + Util.getPhoneNum(getApplicationContext()));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "subscribe onMqttFailure");
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        mqttDisConnect();
        Toast.makeText(this, "서버연결에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "connect failure with exception : " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "--------------connectComplete---------------");
        if (reconnect) {
            Log.d(TAG, "reconnect is true . So subscribing to topic again");
        } else {
            Log.d(TAG, "Connected to MQTT server");
        }
        Log.d(TAG, "--------------connectComplete---------------");
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "--------------connectionLost---------------");
        if (cause != null) {
            Log.e(TAG, "Connection to MQtt is lost due to " + cause.getMessage());
        }
        Log.d(TAG, "--------------connectionLost---------------");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "--------------messageArrived---------------");
        Log.d(TAG, "Message arrived : " + message + " from topic : " + topic);
        arriveMsg = message.toString();
        if (message != null && message.toString().equals("start video")) {
            if (!isRunning) {
                createPlayer(url);
                isRunning = true;
            }
        } else {
            Log.d(TAG, "message not exist");
        }
        Log.d(TAG, "--------------messageArrived---------------");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "--------------deliveryComplete---------------");
        try {
            if (token != null && token.getMessage() != null) {
                Log.d(TAG, "Message : " + token.getMessage().toString() + " delivered");
                if (token.getMessage().toString().equals("request video/" + Util.getPhoneNum(this))) {
                    checkDeliveryMessage();
                }
            }
        } catch (MqttException ex) {
            Log.d(TAG, "Message : Not exist topic");
            ex.printStackTrace();
        }
        Log.d(TAG, "--------------deliveryComplete---------------");
    }

    // video connect 메세지를 5초간 기다림.
    private void checkDeliveryMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!arriveMsg.equals("connect video")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i ++ == 5) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.INVISIBLE);
                                Toast.makeText(MediaActivityTmp.this, "장치 오류입니다. 장치를 껐다 켜주세요.", Toast.LENGTH_SHORT).show();
                                mqttDisConnect();
                            }
                        });
                        arriveMsg = "";
                        break;
                    }
                }
            }
        }).start();
    }
}
