package open.it.com.petit.Reservation.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import java.util.Arrays;

import open.it.com.petit.R;
import open.it.com.petit.Reservation.Callback.RevCallback;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-01.
 */

public class MqttModel implements MqttCallbackExtended, IMqttActionListener {
    private static String TAG = MqttModel.class.getSimpleName();

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private RevCallback.Mqtt mqttCallback; // presenter callback
    private MqttAndroidClient client;
    private Context context;
    private String guid;

    private String[] subTopics = new String[2]; // subscribe가 두개.
    private String pubTopic;
    private byte[] feedBytes;

    private String url;

    public MqttModel(Context context, String guid, byte[] bytes, RevCallback.Mqtt callback) {
        Log.d(TAG, "MqttModel");
        this.context = context;
        this.guid = guid;
        this.feedBytes = bytes;
        this.mqttCallback = callback;
        this.subTopics[0] = "$open-it/pet-it/" + guid + "/alive"; // 장치 on/off 유무 데이터를 받는 topic
        this.subTopics[1] = "$open-it/pet-it/" + guid + "/status"; // 장치의 예약 data를 받는 topic
        this.pubTopic = "$open-it/pet-it/" + guid + "/order"; // 장치에 예약 data를 보내는 topic
        pref = context.getSharedPreferences("revData", Context.MODE_PRIVATE); // app 예약 데이터
        editor = pref.edit();
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
        int[] qos = {0, 0}; // 두 개의 subTopic을 위한 qos
        try {
            client.subscribe(subTopics, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to topic : " + subTopics[0] + "/" + subTopics[1]);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "topic subscription failed for topic : " + subTopics[0] + "/" + subTopics[1]);
                    Toast.makeText(context, "서버와 연결이 끊겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopics[0] + "/" + subTopics[1]);
            Toast.makeText(context, "알 수 없는 오류입니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 일반 메세지를 publish 하는 method
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

    // 예약 데이터를 publish 하는 method
    public void publishReservationByte(String pubTopic, final byte[] msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg);
            message.setQos(0);
            client.publish(pubTopic, message, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // publish 성공 시 app에 data를 저장한다.
                    editor.putString("feedData-" + guid, new String(msg, 0, msg.length));
                    // 저장 후 commit
                    editor.commit();
                    mqttCallback.onShowMessage("변경사항이 저장 되었습니다.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    mqttCallback.onShowMessage("Pet-it 통신 오류입니다. 다시 시도해주세요.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Toast.makeText(context, "서버연결실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
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
        // 받은 data가 48byte 일 때. 즉, 예약 data일 때
        if (message.getPayload().length == 48) {
            Log.d(TAG, "--------------messageArrived---------------");
            Log.d(TAG, "Message arrived : " + Arrays.toString(message.getPayload()) + " from topic : " + topic);
            Log.d(TAG, "app byte : " + Arrays.toString(feedBytes));
            Log.d(TAG, "--------------messageArrived---------------");

            //APP 예약 데이터와 device 예약데이터를 비교.
            compareByte(feedBytes, message.getPayload());
        }
        // 받은 data가 connected 메세지 일 때.
        else if (message.toString().equals("connected")){
            Log.d(TAG, "length != 48 " + Arrays.toString(message.getPayload()));
            Log.d(TAG, message.toString());
            // 연결 flag를 변경.
            mqttCallback.onConnected(true);
            // device에 연결 데이터를 요청하는 publish
            String msg = "reservation request";
            publish(pubTopic, msg.getBytes());
        } else if (message.toString().equals("dead")) {
            mqttCallback.onConnected(false);
            Log.d(TAG, message.toString());
        }
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

    // APP data와 Device data를r 비교
    private void compareByte(byte[] appByte, byte[] devByte) {
        // data가 같을 때
       if (Arrays.equals(appByte, devByte)) {
           Log.d(TAG, "appByte == devByte");
           mqttCallback.onCompute(appByte);
       }
       // data가 같지 않을 때
       else {
           Log.d(TAG, "appByte != devByte");
           // Dialog를 띄운다.
           showDialog(appByte, devByte);
       }
    }

    // 데이터 동기 Dialog
    private void showDialog(final byte[] appByte, final byte[] devByte) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("데이터 동기화")
                .setMessage("어느 데이터에 동기화 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("앱", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // App에 동기 시 App data를 device에 보낸다.
                        publish(pubTopic, appByte);
                        mqttCallback.onCompute(appByte);
                    }
                })
                .setNegativeButton("장치", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Device 동기 시 Device data를 App에 저장한다.
                        editor.putString("feedData-" + guid, new String(devByte, 0, devByte.length));
                        editor.commit();
                        mqttCallback.onCompute(devByte);
                    }
                });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
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
}
