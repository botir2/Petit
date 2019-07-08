package open.it.com.petit.Register.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-12.
 */

public class AddPopup extends Activity {
    private Button cancel;
    private Button confirm;
    private String gps;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_add_popup);

        cancel = (Button) findViewById(R.id.btn_feeder_add_cancel);
        confirm = (Button) findViewById(R.id.btn_feeder_add_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED); // gps 정보를 가져옴
                if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
                    // gps가 꺼져 있을 때
                    startActivity(new Intent(AddPopup.this, ConnectGPS.class));
                    finish();
                }else {
                    // gps가 켜져 있을 때
                    startActivity(new Intent(AddPopup.this, WifiSearchPopup.class));
                    finish();
                }
            }
        });
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
}
