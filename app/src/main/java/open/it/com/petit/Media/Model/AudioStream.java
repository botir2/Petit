package open.it.com.petit.Media.Model;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import open.it.com.petit.Util.Util;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by user on 2018-01-12.
 */

public class AudioStream implements RtspClient.Callback, Session.Callback{
    private static final String TAG = AudioStream.class.getSimpleName();
    private Context context;

    private Session session; // RtspClient를 위한 session
    private RtspClient rtspClient; // stream을 실행할 객체

    private AudioManager audioManager; // 오디오 mute용
    private int originalMode; // 오디오 모드

    private String GUID;

    public AudioStream(Context context, String GUID) {
        this.context = context;
        this.GUID = GUID;

        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE); // 오디오 서비스를 가져옴
        originalMode = audioManager.getMode(); // 현재 오디오 모드

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); // 통화중 모드 즉 전체 input audio mute
        audioManager.setMicrophoneMute(true); // 마이크 mute
    }

    public void setMute(boolean b) {
        if(!b) {
            audioManager.setMicrophoneMute(false);
            audioManager.setMode(originalMode);
            Log.d(TAG, "클릭");
        } else {
            audioManager.setMicrophoneMute(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            Log.d(TAG, "non 클릭");
        }
    }

    public void initialize() {
        // session for rtspClient
        session = SessionBuilder.getInstance()
                .setContext(context)
                .setVideoEncoder(SessionBuilder.VIDEO_NONE) // 비디오 사용 X
                .setAudioEncoder(SessionBuilder.AUDIO_AAC) // AAC 오디오 사용
                .setAudioQuality(new AudioQuality(8000, 16000)) // 오디오 품질
                .setCallback(this)
                .build();

        rtspClient = new RtspClient(); // 스트림 연결용 객체
        rtspClient.setSession(session);
        rtspClient.setCallback(this);

        rtspClient.setCredentials("root", "niceduri"); // 서버 아이디 비밀번호
        rtspClient.setServerAddress("211.38.86.93", 1935); // 주소 포트
        rtspClient.setStreamPath("/live/" + GUID + "-" + Util.getPhoneNum(context)); // 스트림 경로 ~/live/[guid-phone number]
    }

    // stream start
    public void startStream() {
        if (!rtspClient.isStreaming())
            rtspClient.startStream();
    }

    // stream stop
    public void stopStream() {
        if (rtspClient.isStreaming())
            rtspClient.stopStream();
        if (rtspClient != null)
            rtspClient = null;
        if (session != null)
            session = null;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    public void onRtspUpdate(int i, Exception e) {
        Log.d(TAG, "onRtspUpdate");
        Log.d(TAG, "what ? " + i);
        e.printStackTrace();
    }

    @Override
    public void onBitrateUpdate(long l) {
        //Log.d(TAG, "onBitrateUpdate");
    }

    @Override
    public void onSessionError(int i, int i1, Exception e) {
        Log.d(TAG, "onSessionError");
        e.printStackTrace();
    }

    @Override
    public void onPreviewStarted() {
        Log.d(TAG, "onPreviewStarted");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "onSessionConfigured");
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG, "onSessionStarted");
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "onSessionStopped");
    }
}
