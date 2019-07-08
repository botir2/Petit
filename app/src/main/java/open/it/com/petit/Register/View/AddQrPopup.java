package open.it.com.petit.Register.View;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class AddQrPopup extends BaseMqtt {
    private static final String TAG = "AddQrPopup";
    private ImageView qrCode;
    private Intent intent;
    private String subTopic;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_qr_popup);

        qrCode = (ImageView) findViewById(R.id.QRcode);
        intent = getIntent();
        subTopic = "$open-it/pet-it/" + Util.getPhoneNum(this) + "/status";

        // qrcode에 들어갈 내용
        String reg_info = intent.getStringExtra("Ssid")
                + "/" + intent.getStringExtra("WifiPw")
                + "/" + intent.getStringExtra("MasterPw")
                + "/" + Util.getPhoneNum(this);

        createQRCode(reg_info);

        // 페딧에서 보내는 등록완료 메세지를 받기위해 mqtt연결
        connect(subTopic, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        super.connectComplete(reconnect, serverURI);
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        try {
            mqttAndroidClient.subscribe(subTopic, 0, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribe success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        super.messageArrived(topic, message);
        if (message.toString().equals(Util.getPhoneNum(this))) {
            Log.d(TAG, "success!!!!!!!!!");
            finish();
        }
    }

    // string 정보로 qr code 생성
    private void createQRCode(String info) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            // string과 qrcodewirter를 이용해 비트맵 생성.
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(info, BarcodeFormat.QR_CODE, 250, 250));
            qrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 2차원 배열에 검정, 흰색 정보를 삽입한다.
        for (int x = 0 ; x < width ; x ++) {
            for (int y = 0 ; y < height ; y ++) {
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bmp;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
