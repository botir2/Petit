package open.it.com.petit.Setting.FeederSetting.Popup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedHashMap;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-26.
 */

public class ChangeNamePopup extends Activity {
    private EditText name;
    private Button cancel;
    private Button confirm;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_setting_name_popup);

        name = (EditText) findViewById(R.id.ed_setting_name);
        cancel = (Button) findViewById(R.id.setting_name_cancel);
        confirm = (Button) findViewById(R.id.setting_name_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().equals("")) {
                    Toast.makeText(ChangeNamePopup.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return ;
                }

                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("P_NUM", Util.getPhoneNum(getApplicationContext()));
                map.put("P_NAME", name.getText().toString());
                map.put("GUID", getIntent().getStringExtra("GUID"));
                String php = "feeder_name_change.php";
                ConnectionController conn = new ConnectionController(getApplicationContext(), handler);
                conn.setMethod("POST").setHash(map).setUrl(php);
                Thread thread = new Thread(conn);
                thread.start();
            }
        });
    }

    final HttpHandler handler = new HttpHandler(this) {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
            Toast.makeText(getApplicationContext(), "이름 변경 완료", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onHttpError() {
            super.onHttpError();
            finish();
        }
    };

}
