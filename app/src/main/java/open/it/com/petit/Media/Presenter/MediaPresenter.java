package open.it.com.petit.Media.Presenter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import open.it.com.petit.Media.Callback.Callback;
import open.it.com.petit.Media.Model.MediaModel;
import open.it.com.petit.Media.Model.MqttModel;
import open.it.com.petit.Media.Presenter.Contract.MediaContract;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-14.
 */

public class MediaPresenter implements MediaContract.Presenter, Callback.Media, Callback.Mqtt {
    private static final String TAG = MediaPresenter.class.getSimpleName();

    private Context context;
    private MediaContract.View view;

    private MqttModel mqttModel;
    private MediaModel mediaModel;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private String guid;

    public MediaPresenter(Context context, String guid) {
        this.context = context;
        this.guid = guid;
        this.mqttModel = new MqttModel(context, guid);
        this.mqttModel.setCallback(this);
        this.mediaModel = new MediaModel(context, guid);
        this.mediaModel.setCallback(this);
    }

    @Override
    public void attachView(MediaContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    /******************************Media**************************************/
    @Override
    public void startMedia() {
        mediaModel.setSurfaceView(surfaceView);
        mediaModel.setHolder(holder);
        mediaModel.createPlayer();
    }

    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void onShowMessageMedia(String msg) {
        view.toast(msg);
    }

    @Override
    public void onMediaSetProgress(boolean b) {
        view.setProgress(b);
    }

    @Override
    public void releasePlayer() {
        mediaModel.releasePlayer();
    }

    @Override
    public int getWidth() {
        //View가 Model보다 빨리 닫혀 onNewLayout에서 에러남.
        return view.getWindowWidth();
    }

    @Override
    public int getHeight() {
        return view.getWindowHeight();
    }

    @Override
    public boolean getPortrait() {
        return view.getPortrait();
    }

    /******************************Mqtt**************************************/
    @Override
    public void connectMqtt(String guid) {
        mqttModel.connect();
    }

    @Override
    public void disConnectMqtt() {
        mqttModel.disConnect();
    }

    @Override
    public void stopVideo() {
        String msg = "stop video/" + Util.getPhoneNum(context);
        mqttModel.publish("$open-it/pet-it/" + guid + "/order", msg.getBytes());
    }

    @Override
    public void onShowMessageMqtt(String msg) {
        view.toast(msg);
    }

    @Override
    public void onMqttSetProgress(boolean b) {
        view.setProgress(false);
    }

    @Override
    public void onStartMedia() {
        startMedia();
    }
}
