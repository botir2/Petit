package open.it.com.petit.Media.Model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import open.it.com.petit.Media.Callback.Callback;
import open.it.com.petit.R;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-19.
 */

public class MqttModel implements MqttCallbackExtended, IMqttActionListener {
    private static String TAG = MqttModel.class.getSimpleName();

    private MqttAndroidClient client;
    private Context context;

    private String pubTopic;
    private String subTopic;

    private String url;
    private String arriveMsg = "";

    private boolean isRunning = false;

    private Callback.Mqtt callback;

    public MqttModel(Context context, String guid) {
        this.context = context;
        this.pubTopic = "$open-it/pet-it/" + guid + "/order";
        this.subTopic = "$open-it/pet-it/" + guid + "/status";
    }

    public void setCallback(Callback.Mqtt callback) {
        this.callback = callback;
    }

    public void connect() {
        url = context.getString(R.string.mqtt_host);

        client = new MqttAndroidClient(context, url, Util.getDate());
        client.setCallback(this);

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

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit connect successfull. Now Subscribing to topic...");
        int qos = 0;
        try {
            client.subscribe(subTopic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to topic : " + subTopic);
                    // mqtt 연결이 완료되면 request video/[phone number] 를 보내 영상 실행을 요청함
                    String msg = "request video/" + Util.getPhoneNum(context);
                    publish(pubTopic, msg.getBytes());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "topic subscription failed for topic : " + subTopic);
                    Toast.makeText(context, "서버와 연결이 끊겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopic);
            Toast.makeText(context, "알 수 없는 오류입니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void publish(String pubTopic, byte[] msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg);
            message.setQos(0);
            client.publish(pubTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Toast.makeText(context, "서버연결실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        exception.printStackTrace();
        disConnect();
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
        Log.d(TAG, "Message arrived : " + message.toString() + " from topic : " + topic);
        Log.d(TAG, "--------------messageArrived---------------");
        arriveMsg = message.toString();
        if (message != null && arriveMsg.equals("start video")) {
            // 영상 요청 결과로 start video 메세지가 오면 Running이 아닐 시 미디어 start
            if (!isRunning) {
                callback.onStartMedia();
                isRunning = true;
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            if (token == null || token.getMessage() == null) {
                Log.d(TAG, "delivery error");
                return;
            }
            Log.d(TAG, "Message : " + token.getMessage().toString() + " delivered");
            if (token.getMessage().toString().equals("request video/" + Util.getPhoneNum(context))) {
                checkDeliveryMessage();
            }
        } catch (MqttException ex) {
            Log.d(TAG, "Message : Not exist topic");
            ex.printStackTrace();
        }
    }

    public void disConnect() {
        if (client != null) {
            try {
                client.unregisterResources();
                client.close();
                client.disconnect(null, this);
                client = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkDeliveryMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!arriveMsg.equals("start video")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i ++ == 5) {
                        callback.onMqttSetProgress(false);
                        callback.onShowMessageMqtt("장치 오류입니다. 장치를 껐다 켜주세요.");
                        disConnect();
                        arriveMsg = "";
                        break;
                    }
                }
            }
        }).start();
    }
}
