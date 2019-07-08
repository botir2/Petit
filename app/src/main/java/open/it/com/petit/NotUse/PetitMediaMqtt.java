package open.it.com.petit.NotUse;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by user on 2017-06-28.
 */

public class PetitMediaMqtt implements MqttCallbackExtended {
    public static final String TAG = "PetitMediaMqtt";
    private String devicePetitUrl = "tcp://211.38.86.94:1883";
    private String deviceCarkitUrl = "tcp://114.70.144.69:1883";
    private String clientId = "";
    public String lightState;

    private MqttAndroidClient mqttAndroidClientCarkit;
    private MqttAndroidClient mqttAndroidClientPetit;
    private Context context;

    public PetitMediaMqtt(Context context) {
        this.context = context;
    }

    public void connectDevice() {
        try {
            mqttAndroidClientCarkit = new MqttAndroidClient(context, deviceCarkitUrl, clientId);
            mqttAndroidClientCarkit.setCallback(this);

            mqttAndroidClientPetit = new MqttAndroidClient(context, devicePetitUrl, clientId);
            mqttAndroidClientPetit.setCallback(this);

            IMqttToken tokenCarkit = mqttAndroidClientCarkit.connect();
            IMqttToken tokenPetit = mqttAndroidClientPetit.connect();

            tokenCarkit.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "mqtt carkit connect successfull. Now Subscribing to topic...");
                    try {
                        mqttAndroidClientCarkit.subscribe("open-it/status/#", 0, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d(TAG, "Subscribed to topic : open-it/status/#");
                                publishMessageToCarkit("open-it/order/light1", "status");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d(TAG, "topic subscription failed for topic : open-it/status/#");
                            }
                        });
                    } catch (MqttException e) {
                        Log.d(TAG, "구독오류");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (exception != null) {
                        Log.e(TAG, "connect to carkit failure with exception : " + exception.getMessage());
                        Toast.makeText(context, "Device 네트워크가 불안정합니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                }
            });

            tokenPetit.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "mqtt petit connect successfull. Now Subscribing to topic...");
                    try {
                        mqttAndroidClientPetit.subscribe("$open-it/pet-it/status/#", 0, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d(TAG, "Subscribed to topic : $open-it/pet-it/status/#");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d(TAG, "topic subscription failed for topic : $open-it/pet-it/status/#");
                            }
                        });
                    }catch (MqttException e) {
                        Log.d(TAG, "topic subscription failed for topic : $open-it/pet-it/status/#");
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "connect to petit failure with exception : " + exception.getMessage());
                    Toast.makeText(context, "Device 네트워크가 불안정합니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessageToCarkit(String pubTopic, String messageText) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(messageText.getBytes());
            message.setQos(0);
            mqttAndroidClientCarkit.publish(pubTopic, message);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publishMessageToPetit(String pubTopic, String messageText) {
        if (!mqttAndroidClientPetit.isConnected()) {
            Toast.makeText(context, "서버와 연결이 끊겨있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(messageText.getBytes());
            message.setQos(0);
            mqttAndroidClientPetit.publish(pubTopic, message);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void lightOnOff(String state) {
        Log.d(TAG, "light state : " + state);
        if (state != null) {
            if (state.equals("0")) {
                publishMessageToCarkit("open-it/order/light1", "on");
                lightState = "1";
            } else if (state.equals("1")) {
                publishMessageToCarkit("open-it/order/light1", "off");
                lightState = "0";
            }
        } else {
            Toast.makeText(context, "light 연결이 끊겨있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void provideFood(String topic, String messageText) {
        Log.d(TAG, "provideFood : " + topic + ":" + messageText);
        publishMessageToPetit(topic, messageText);
    }

    public void disConnectMqtt() {
        try {
            if (mqttAndroidClientCarkit != null) {
                mqttAndroidClientCarkit.unregisterResources();
                mqttAndroidClientCarkit.close();
                mqttAndroidClientCarkit.disconnect();
            }

            if (mqttAndroidClientPetit != null) {
                mqttAndroidClientPetit.unregisterResources();
                mqttAndroidClientPetit.close();
                mqttAndroidClientPetit.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            Log.d(TAG, "reconnect is true . So subscribing to topic again");
        } else {
            Log.d(TAG, "Connected to MQTT server");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if (cause != null) {
            try {
                if (!mqttAndroidClientCarkit.isConnected())
                    mqttAndroidClientCarkit.disconnect();
                if (!mqttAndroidClientPetit.isConnected())
                    mqttAndroidClientPetit.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "Connection to MQtt is lost due to " + cause.getMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "Message arrived : " + message + " from topic : " + topic);
        String text = message.toString();
        switch(topic) {
            case "open-it/status/light1":
                Log.d(TAG, "message arrived topic open-it/status/light1");
                if (!TextUtils.isEmpty(text))
                    lightState = text;
                break;
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
}
