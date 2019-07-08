package open.it.com.petit.Main.Model;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import open.it.com.petit.Main.Callback.MainCallback;
import open.it.com.petit.R;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-08.
 */

public class MqttModel implements MqttCallbackExtended, IMqttActionListener {
    private static final String TAG = MqttModel.class.getSimpleName();

    private Context context;
    private MqttAndroidClient client; // mqtt client

    private String subTopic;
    private String url;

    private MainCallback.MqttCallback callback; // present에 나타날 callback

    public MqttModel(Context context, MainCallback.MqttCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void connect() {
        url = context.getString(R.string.mqtt_host);
        subTopic = "$open-it/pet-it/update/";

        client = new MqttAndroidClient(context, url, Util.getDate());
        client.setCallback(this); // mqtt callback bind

        /**
         * MqttConnect Option 을 set한다.
         * setConnectionTimeout : 연결 시간초과 초를 지정
         * setAutomaticReconnect : 연결이 끊어질 시 다시 연결할지 지정
         * setCleanSession : 최신 메세지만 받음
         */
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        final int seconds = 10;
        connectOptions.setConnectionTimeout(seconds);
        connectOptions.setAutomaticReconnect(true); // reconnect true
        connectOptions.setCleanSession(true); // false일 시 예전 메세지까지 다 받음.

        try {
            client.connect(connectOptions, null, this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // mqtt disconnect method
    public void disConnect() {
        Log.d(TAG, "MqttDisConnect");
        if (client != null) {
            try {
                client.unregisterResources(); // 리소스 해제
                client.close(); // mqtt를 닫음
                client.disconnect(null, this); // mqtt disconnect
                client = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    // mqtt 연결에 성공 callback
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit connect successfull. Now Subscribing to topic..." + subTopic);
        try {
            /**
             * subscribe를 열어줌.
             * @param1 : subTopic
             * @param2 : qos (메세지 등급)
             * @param3 : activity context
             * @param4 : 성공, 실패 리스너
             */
            client.subscribe(subTopic, 0, context, new IMqttActionListener() {
                // subscribe 성공 callback
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {}

                // subcribe 실패 callback
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    callback.onMqttFailure(); // 실패 callback 호출
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopic);
            callback.onMqttFailure();
        }
    }

    // mqtt connect 실패 callback
    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.e(TAG, "connect to petit failure with exception : " + exception.getMessage());
        exception.printStackTrace();
        callback.onMqttFailure();
    }

    // connect 완료 callback
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

    // connect 끊길시 callback
    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "--------------connectionLost---------------");
        if (cause != null) {
            Log.e(TAG, "Connection to MQtt is lost due to " + cause.getMessage());
        }
        Log.d(TAG, "--------------connectionLost---------------");
    }

    // mqtt message 도착 시 callback
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "--------------messageArrived---------------");
        Log.d(TAG, "Message arrived : " + message + " from topic : " + topic);
        Log.d(TAG, "--------------messageArrived---------------");

        /**
         * 메세지가 SHARE/[phone num] or MASTER/[guid] 형태로 들어기 때문에
         * 메세지를 '/' 로 나눔.
         * str1 : SHARE or MASTER
         * str2 : [phone nume] or [guid]
         */
        String str1 = message.toString().split("/")[0];
        String str2 = message.toString().split("/")[1];

        if (str1.equals("MASTER")) {
            callback.onChangeMaster(str2);
        } else if (str1.equals("SHARE")) {
            callback.onShare(str2);
        }
    }

    // 메세지가 제대로 전달 됐을 때 callback
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            if (token != null && token.getMessage() != null) {
                Log.d(TAG, "Message : " + token.getMessage().toString() + " delivered");
            }
        } catch (MqttException ex) {
            Log.d(TAG, "Message : Not exist topic");
            ex.printStackTrace();
        }
    }
}
