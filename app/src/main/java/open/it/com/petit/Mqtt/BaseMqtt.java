package open.it.com.petit.Mqtt;

import android.support.v7.app.AppCompatActivity;
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

import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-03.
 */

public class BaseMqtt extends AppCompatActivity
        implements Mqtt, MqttCallbackExtended, IMqttActionListener {
    private final static String TAG = BaseMqtt.class.getSimpleName();

    protected String subTopic;
    protected String pubTopic;

    protected MqttAndroidClient mqttAndroidClient;
    protected String mqttURL;
    protected String clientId;

    protected String GUID;

    @Override
    public void connect(String subTopic, String pubTopic, String clientId) {
        mqttURL = getResources().getString(R.string.mqtt_host);

        mqttAndroidClient = new MqttAndroidClient(this, mqttURL, clientId);
        mqttAndroidClient.setCallback(this);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        final int seconds = 10;
        connectOptions.setConnectionTimeout(seconds);
        connectOptions.setAutomaticReconnect(true); // reconnect true
        connectOptions.setCleanSession(true); // false일 시 예전 메세지까지 다 받음.

        try {
            mqttAndroidClient.connect(connectOptions, null, this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // id 지정 알할 시 현재시간으로 지정하는 오버로딩 메소드
    public void connect(String subTopic, String pubTopic) {
        clientId = Util.getDate();
        connect(subTopic, pubTopic, clientId);
    }

    @Override
    public void disConnect() {
        Log.d(TAG, "MqttDisConnect");
        if (mqttAndroidClient != null) {
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

    @Override
    public void publish(String pubTopic, byte[] bytes) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(bytes);
            message.setQos(0);
            mqttAndroidClient.publish(pubTopic, message);
            Log.d(TAG, "send topic : " + pubTopic + " / msg : " + bytes);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {

    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.e(TAG, "connect to petit failure with exception : " + exception.getMessage());
        Toast.makeText(this, "서버 연결 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        //disConnect();
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
        Log.d(TAG, "--------------messageArrived---------------");
    }

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
