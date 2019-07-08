package open.it.com.petit.Setting.AppSetting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import open.it.com.petit.R;

/**
 * Created by user on 2017-11-09.
 */

public class SettingActivity extends AppCompatActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private final static String TAG = SettingActivity.class.getSimpleName();

    private Switch alarmSwitch;
    private TextView isAlarm;
    private ImageView sound1;
    private ImageView sound2;
    private ImageView sound3;
    private ImageView sound4;
    private ImageView sound5;

    private SharedPreferences sfr;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_system_setting_activity);

        alarmSwitch = (Switch) findViewById(R.id.push_alarm_switch);
        isAlarm = (TextView) findViewById(R.id.push_alarm_switch_text);
        sound1 = (ImageView) findViewById(R.id.push_sound1);
        sound2 = (ImageView) findViewById(R.id.push_sound2);
        sound3 = (ImageView) findViewById(R.id.push_sound3);
        sound4 = (ImageView) findViewById(R.id.push_sound4);
        sound5 = (ImageView) findViewById(R.id.push_sound5);

        alarmSwitch.setOnCheckedChangeListener(this);
        sound1.setOnClickListener(this);
        sound2.setOnClickListener(this);
        sound3.setOnClickListener(this);
        sound4.setOnClickListener(this);
        sound5.setOnClickListener(this);

        sfr = getSharedPreferences("system_setting", MODE_PRIVATE);
        editor = sfr.edit();

        if (sfr.getInt("isAlarm", 0) != 0) {
            alarmSwitch.setChecked(true);
            isAlarm.setText("on");
            if (sfr.getInt("alarm_sound", 0) == 0)
                setPushSound(1);
            else
                setPushSound(setPushSoundEnable(true));
        } else {
            alarmSwitch.setChecked(false);
            isAlarm.setText("off");
            setPushSoundEnable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.push_sound1:
                setPushSound(1);
                break;
            case R.id.push_sound2:
                setPushSound(2);
                break;
            case R.id.push_sound3:
                setPushSound(3);
                break;
            case R.id.push_sound4:
                setPushSound(4);
                break;
            case R.id.push_sound5:
                setPushSound(5);
                break;
        }
    }

    private void setPushSound(int mode) {
        switch (mode) {
            case 1:
            default:
                editor.putInt("alarm_sound", 1);
                setPushSoundImageInit();
                sound1.setImageResource(R.drawable.food_icon01_on);
                break;
            case 2:
                editor.putInt("alarm_sound", 2);
                setPushSoundImageInit();
                sound2.setImageResource(R.drawable.food_icon01_on);
                break;
            case 3:
                editor.putInt("alarm_sound", 3);
                setPushSoundImageInit();
                sound3.setImageResource(R.drawable.food_icon01_on);
                break;
            case 4:
                editor.putInt("alarm_sound", 4);
                setPushSoundImageInit();
                sound4.setImageResource(R.drawable.food_icon01_on);
                break;
            case 5:
                editor.putInt("alarm_sound", 5);
                setPushSoundImageInit();
                sound5.setImageResource(R.drawable.food_icon01_on);
                break;
        }
        editor.commit();
    }

    private int setPushSoundEnable(boolean b) {
        if (!b) {
            sound1.setImageResource(R.drawable.food_icon01_off);
            sound2.setImageResource(R.drawable.food_icon01_off);
            sound3.setImageResource(R.drawable.food_icon01_off);
            sound4.setImageResource(R.drawable.food_icon01_off);
            sound5.setImageResource(R.drawable.food_icon01_off);
        }
        sound1.setEnabled(b);
        sound2.setEnabled(b);
        sound3.setEnabled(b);
        sound4.setEnabled(b);
        sound5.setEnabled(b);

        return sfr.getInt("alarm_sound", 0);
    }

    private void setPushSoundImageInit() {
        sound1.setImageResource(R.drawable.food_icon01_off);
        sound2.setImageResource(R.drawable.food_icon01_off);
        sound3.setImageResource(R.drawable.food_icon01_off);
        sound4.setImageResource(R.drawable.food_icon01_off);
        sound5.setImageResource(R.drawable.food_icon01_off);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "isChecked : " + isChecked);
        if (isChecked) {
            isAlarm.setText("on");
            if (sfr.getInt("alarm_sound", 0) == 0)
                setPushSound(1);
            else
                setPushSound(setPushSoundEnable(true));
            editor.putInt("isAlarm", 1);
            editor.commit();
        } else {
            isAlarm.setText("off");
            setPushSoundEnable(false);
            editor.remove("isAlarm");
            editor.commit();
        }
    }
}
