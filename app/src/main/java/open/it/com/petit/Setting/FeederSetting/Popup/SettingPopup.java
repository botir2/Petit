package open.it.com.petit.Setting.FeederSetting.Popup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-12.
 */

public class SettingPopup extends Activity implements View.OnClickListener{
    private final static String TAG = "SettingPopup";
    private LinearLayout setting_name;
    private LinearLayout setting_picture;
    private LinearLayout setting_delete;
    private LinearLayout setting_alarm;
    private LinearLayout setting_general;
    private LinearLayout setting_share;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_setting_popup);

        initView();
    }

    private void initView() {
        setting_name = (LinearLayout) findViewById(R.id.setting_name);
        setting_picture = (LinearLayout) findViewById(R.id.setting_picture);
        setting_delete = (LinearLayout) findViewById(R.id.setting_delete);
        setting_alarm = (LinearLayout) findViewById(R.id.setting_smart_alarm);
        setting_general = (LinearLayout) findViewById(R.id.setting_general);
        setting_share = (LinearLayout) findViewById(R.id.setting_share);

        setting_name.setOnClickListener(this);
        setting_picture.setOnClickListener(this);
        setting_delete.setOnClickListener(this);
        setting_alarm.setOnClickListener(this);
        setting_general.setOnClickListener(this);
        setting_share.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.setting_name:
                intent = new Intent(this, ChangeNamePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                startActivity(intent);
                finish();
                break;
            case R.id.setting_picture:
                intent = new Intent(this, ChangePicturePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                startActivity(intent);
                finish();
                break;
            case R.id.setting_delete:
                intent = new Intent(this, DeletePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                startActivity(intent);
                finish();
                break;
            case R.id.setting_smart_alarm:

                break;
            case R.id.setting_general:

                break;
            case R.id.setting_share:
                intent = new Intent(this, SharePopup.class);
                intent.putExtra("GUID", getIntent().getStringExtra("GUID"));
                intent.putExtra("PW", getIntent().getStringExtra("PW"));
                intent.putExtra("MS", getIntent().getIntExtra("MS", -1));
                startActivity(intent);
                finish();
                break;
        }
    }
}
