package open.it.com.petit.Register.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-14.
 */

public class AddWifiPwPopup extends Activity {
    private static final String TAG = AddWifiPwPopup.class.getSimpleName();

    private Button cancel;
    private Button confirm;
    private EditText ed_PW;
    private TextView tv_Wifi;
    private ProgressBar pb;

    private Intent intent;
    private String ssid;
    private android.net.wifi.ScanResult ap;
   
    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_wifi_pw);

        intent = getIntent();

        if (intent.hasExtra("ap")) { // ap가 있을 때
            ap = (android.net.wifi.ScanResult) intent.getExtras().get("ap");
            ssid = ap.SSID;
        } else { // ap가 없을 때
            Toast.makeText(this, "와이파이 오류", Toast.LENGTH_SHORT).show();
            finish();
        }

        cancel = (Button) findViewById(R.id.btn_wifi_pw_cancel);
        confirm = (Button) findViewById(R.id.btn_wifi_pw_confirm);
        ed_PW = (EditText) findViewById(R.id.ed_wifi_pw);
        tv_Wifi = (TextView) findViewById(R.id.tv_pw_wifi_name);
        pb = (ProgressBar) findViewById(R.id.pb_wifi_wait);

        tv_Wifi.setText(ssid);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddWifiPwPopup.this, WifiSearchPopup.class));
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                validatePassword();
            }
        });
    }

    private void validatePassword() {
        if (ed_PW.getText().toString().equals("")) {
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(AddWifiPwPopup.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return ;
        }

        Intent intent = new Intent(AddWifiPwPopup.this, MasterPopup.class);
        intent.putExtra("Ssid", ap.SSID);
        intent.putExtra("WifiPw", ed_PW.getText().toString());
        startActivity(intent);
        finish();
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
        return ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
