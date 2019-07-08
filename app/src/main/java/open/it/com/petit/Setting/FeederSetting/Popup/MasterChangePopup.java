package open.it.com.petit.Setting.FeederSetting.Popup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.LinkedHashMap;

import open.it.com.petit.Main.Callback.ConnectionCallback;
import open.it.com.petit.Main.Model.ConnectionModel;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-10-30.
 */

public class MasterChangePopup extends BaseMqtt implements View.OnClickListener, ConnectionCallback {
    private final static String TAG = MasterChangePopup.class.getSimpleName();

    private EditText pwEdit;
    private Button cancleBtn;
    private Button confirmBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_feeder_master);

        pwEdit = (EditText) findViewById(R.id.petit_feeder_master_pw);
        cancleBtn = (Button) findViewById(R.id.petit_feeder_master_cancle);
        confirmBtn = (Button) findViewById(R.id.petit_feeder_master_confirm);

        cancleBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

        GUID = getIntent().getStringExtra("GUID");
        pubTopic = "$open-it/pet-it/update/";
        connect(null, pubTopic, Util.getDate());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.petit_feeder_master_cancle:
                finish();
                break;
            case R.id.petit_feeder_master_confirm:
                passwordConfirm();
                break;
        }
    }

    private void passwordConfirm() {
        String pw = pwEdit.getText().toString();
        String masterPW = getIntent().getStringExtra("PW");

        Log.d(TAG, masterPW);
        if (pw.equals("")) {
            Toast.makeText(this, "빈칸 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pw.equals(masterPW)) {
            Toast.makeText(this, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConnectionModel conn = new ConnectionModel(this, this);
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("GUID", GUID);
        map.put("P_NUM", Util.getPhoneNum(this));
        String php = "feeder_master_change.php";
        conn.init().setMethod("POST").setHash(map).setPhp(php);
        Thread thread = new Thread(conn);
        thread.start();
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "mqtt petit connect successfull. Now Subscribing to topic..." + subTopic);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
    }

    @Override
    public void onGetSuccess() {

    }

    @Override
    public void onPostSuccess(String result) {
        Log.d(TAG, "result : " + result);
        toast("권한 변경하였습니다.");
        String msg = "MASTER/" + GUID;
        publish(pubTopic, msg.getBytes());
        finish();
    }

    @Override
    public void onHttpFailure() {
        toast("서버 연결에 실패했습니다. 다시 시도해주세요.");
        finish();
    }

    public void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MasterChangePopup.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
