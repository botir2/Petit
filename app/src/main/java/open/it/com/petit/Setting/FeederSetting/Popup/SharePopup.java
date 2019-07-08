package open.it.com.petit.Setting.FeederSetting.Popup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedHashMap;

import open.it.com.petit.Main.Callback.ConnectionCallback;
import open.it.com.petit.Main.Model.ConnectionModel;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-12.
 */

public class SharePopup extends BaseMqtt implements View.OnClickListener, ConnectionCallback{
    private final static String TAG = SharePopup.class.getSimpleName();

    private EditText phoneNum;
    private Button registBtn;
    private Button masterBtn;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_share_popup);

        pubTopic = "$open-it/pet-it/update/";
        //subTopic = "$open-it/pet-it/update";

        phoneNum = (EditText) findViewById(R.id.share_phone_num);
        registBtn = (Button) findViewById(R.id.share_add);
        masterBtn = (Button) findViewById(R.id.share_master_btn);
        registBtn.setOnClickListener(this);
        masterBtn.setOnClickListener(this);

        // slave면 버튼 비활성화
        if (getIntent().getIntExtra("MS", -1) == 0) {
            phoneNum.setEnabled(false);
            registBtn.setEnabled(false);
        }

        connect(null, pubTopic, Util.getDate());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_add:
                if (phoneNum.getText().toString().equals("")) {
                    Toast.makeText(this, "빈칸 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (phoneNum.getText().toString().length() != 11) {
                    Toast.makeText(this, "유효한 핸드폰번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                shareFeeder();
                break;
            case R.id.share_master_btn:
                Intent intent = new Intent(SharePopup.this, MasterChangePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                intent.putExtra("PW", getIntent().getStringExtra("PW"));
                startActivity(intent);
                finish();
                break;
        }
    }

    private void shareFeeder() {
        if (Util.getPhoneNum(this).equals(phoneNum.getText().toString())) {
            Toast.makeText(this, "자신의 핸드폰은 등록 불가합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        ConnectionModel conn = new ConnectionModel(this, this);
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("P_NUM", phoneNum.getText().toString());
        map.put("GUID", getIntent().getStringExtra("GUID"));
        map.put("PW", getIntent().getStringExtra("PW"));
        map.put("MS", 0);
        String php2 = "feeder_insert.php";
        conn.init().setMethod("POST").setHash(map).setPhp(php2);
        Thread thread2 = new Thread(conn);
        thread2.start();
    }

    @Override
    public void onGetSuccess() {

    }

    @Override
    public void onPostSuccess(String result) {
        if (result.equals("-1")) {
            toast("등록된 번호가 있습니다.");
            return;
        }

        toast("등록 하였습니다.");
        String msg = "SHARE/" + phoneNum.getText().toString();
        publish(pubTopic, msg.getBytes());
        Log.d(TAG, "shareSuccess : " + pubTopic + " " + msg);
        finish();
    }

    @Override
    public void onHttpFailure() {
        toast("서버 연결 실패. 다시 시도해주세요.");
        finish();
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SharePopup.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
    }
}
