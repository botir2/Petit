package open.it.com.petit.Media.View;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import open.it.com.petit.Media.Presenter.Contract.MediaContract;
import open.it.com.petit.Media.Presenter.MediaPresenter;
import open.it.com.petit.R;

/**
 * Created by user on 2018-02-14.
 */

public class MediaActivity extends AppCompatActivity implements MediaContract.View{
    private static final String TAG = MediaActivity.class.getSimpleName();

    @BindView(R.id.audio_start)
    ImageButton audioStartBtn; // audio button
    @BindView(R.id.media_close)
    ImageButton mediaCloseBtn; // close button
    @BindView(R.id.feed_once)
    ImageButton feedOnceBtn; // feed button
    @BindView(R.id.flash)
    ImageButton flashBtn; // flash button
    @BindView(R.id.capture)
    ImageButton captureBtn; // capture button
    @BindView(R.id.record)
    ImageButton recordBtn; // record button
    @BindView(R.id.playback_audio)
    ImageButton playbackBtn; // sound playback button
    @BindView(R.id.media_pb)
    ProgressBar pb;
    @BindView(R.id.sv_petit_media)
    SurfaceView surfaceView; // 영상을 나타내줄 view

    private boolean isRecording = false;
    private boolean isFlash = false;

    private String guid;
    private String mediaUrl;

    private MediaPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_media_activity_tmp);
        ButterKnife.bind(this);
        guid = getIntent().getStringExtra("GUID");
        mediaUrl = "rtsp://211.38.86.93:1935/live/" + guid;

        presenter = new MediaPresenter(this, guid);
        presenter.attachView(this);
        presenter.setSurfaceView(surfaceView);
        presenter.setHolder(surfaceView.getHolder());
        presenter.connectMqtt(guid);
    }

    // audio touch event Listener
    @OnTouch(R.id.audio_start)
    boolean onAudioTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            // 눌렀을 때
            Log.d(TAG, "down");
        } else if (action == MotionEvent.ACTION_UP) {
            // 뗐을 때
            Log.d(TAG, "up");
        }
        return true;
    }

    // feed touch event Listener
    @OnTouch(R.id.feed_once)
    boolean onFeedTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "down");
        } else if (action == MotionEvent.ACTION_UP) {
            Log.d(TAG, "up");
        }
        return true;
    }

    // captuer touch event Listener
    @OnTouch(R.id.capture)
    boolean onCaptureTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "down");
        } else if (action == MotionEvent.ACTION_UP) {
            Log.d(TAG, "up");
        }
        return true;
    }

    // playback touch event Listener
    @OnTouch(R.id.playback_audio)
    boolean onPlaybackTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "down");
        } else if (action == MotionEvent.ACTION_UP) {
            Log.d(TAG, "up");
        }
        return true;
    }

    // record click event Listener
    @OnClick(R.id.record)
    void onRecordClick() {
        if (isRecording) {
            isRecording = false;
        } else if (!isRecording) {
            isRecording = true;
        }
    }

    // flash click event Listener
    @OnClick(R.id.flash)
    void onFlashClick() {
        if (isFlash) {
            isFlash = false;
        } else {
            isFlash = true;
        }
    }

    @OnClick(R.id.media_close)
    void onCloseClick() {
        finish();
    }

    @Override
    public void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MediaActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setProgress(final boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (b)
                    pb.setVisibility(View.VISIBLE);
                else
                    pb.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void setButtonEnable(boolean b) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        presenter.stopVideo();
        presenter.disConnectMqtt();
        presenter.detachView();
        // release 시간이 오래걸리므로 thread로 처리함.
        new Thread(new Runnable() {
            @Override
            public void run() {
                presenter.releasePlayer();
            }
        }).start();
    }

    // 화면 width getter
    @Override
    public int getWindowWidth() {
        return getWindow().getDecorView().getWidth();
    }

    // 화면 height getter
    @Override
    public int getWindowHeight() {
        return getWindow().getDecorView().getHeight();
    }

    // 화면 metrics getter
    @Override
    public DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    // 가로 세로 getter
    @Override
    public boolean getPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
