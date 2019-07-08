package open.it.com.petit.Media.Model;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import open.it.com.petit.Media.Callback.Callback;

/**
 * Created by user on 2018-02-19.
 */

public class MediaModel implements IVLCVout.Callback {
    private static final String TAG = MediaModel.class.getSimpleName();

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView; // 영상 view
    private SurfaceHolder holder; // 영상 view holder

    private Context context;
    private String url;
    private String guid;

    private int count = 0; // 영상 상태를 나타내 줄 count

    private DisplayMetrics displayMetrics; // 영상 캡쳐, 녹화 용
    private Display display; // 영상 캡쳐, 녹화용

    private int videoWidth; // 비디오 너비
    private int videoHeight; // 비디오 높이
    private int screenDensity; // 영상 캡쳐, 녹화용

    private Callback.Media callback;

    public MediaModel(Context context, String guid) {
        this.context = context;
        this.url = "rtsp://211.38.86.93:1935/live/" + guid;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    public void setCallback(Callback.Media callback) {
        this.callback = callback;
    }

    // 화면 켜짐 or 변경 시 호출되는 콜백
    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        Log.d(TAG, "onNewLayout width : " + width + "?? height : " + height);
        if (width * height == 0)
            return;
        videoHeight = height;
        videoWidth = width;
        setSize(visibleWidth, visibleHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    // 하드웨어 가속 에러시 호출되는 콜백
    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.d(TAG, "onHardwareAccelerationError");
    }

    // player를 초기화함.
    public void releasePlayer() {
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

    public void createPlayer() {
        releasePlayer();
        try {
            // 미디어 옵션
            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libVLC = new LibVLC(context, options);

            mediaPlayer = new MediaPlayer(libVLC);
            mediaPlayer.setEventListener(eventListener); // 상태 event listener binding
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            vout.addCallback(this);
            vout.attachViews();

            // delay, latency 제어를 위한 설정
            Media m = new Media(libVLC, Uri.parse(url));
            m.addOption("network-caching=1000");
            m.addOption(":clock-jitter=0");
            m.addOption(":clock-synchro=0");

            mediaPlayer.setMedia(m);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 비디오의 사이즈 setting method
    private void setSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;

        if (videoWidth * videoHeight <= 1)
            return;

        if (holder == null || surfaceView == null)
            return;

        if (Integer.valueOf(callback.getWidth()) == null || Integer.valueOf(callback.getHeight()) == null)
            return;

        int w = callback.getWidth();
        int h = callback.getHeight();

        boolean isPortrait = callback.getPortrait();
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

    // 미디어가 실행 될 때 상황을 알려주는 Listener
    final MediaPlayer.EventListener eventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            count = event.getVoutCount();
            switch (event.type) {
                case MediaPlayer.Event.Opening:
                    Log.d(TAG, "Opening");
                    // Opening
                    break;

                case MediaPlayer.Event.Buffering:
                    Log.d(TAG, "Buffering");
                    // Buffering
                    break;

                case MediaPlayer.Event.Playing:
                    Log.d(TAG, "Playing");
                    // Playing
                    break;

                case MediaPlayer.Event.EncounteredError:
                    Log.d(TAG, "EncounteredError");
                    callback.onMediaSetProgress(false);
                    //pb.setVisibility(View.INVISIBLE);
                    //setButtonEnable(false);
                    //Toast.makeText(MediaActivityTmp.this, "서버연결 오류. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    callback.onShowMessageMedia("영상 오류. 다시 시도해주세요.");
                    break;

                case MediaPlayer.Event.Vout:
                    Log.d(TAG, "Vout");
                    callback.onMediaSetProgress(false);
                    //pb.setVisibility(View.INVISIBLE);
                    //setButtonEnable(true);
                    break;

                default:
                    //Log.d(TAG, "default : " + count);
                    //Log.d(TAG, "default" + Integer.toHexString(event.type));
                    break;
            }
        }
    };
}
