package open.it.com.petit.Register.View;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class ConnectGPS extends Activity {
    private Button cancel;
    private Button confirm;
    private String gps;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_connect_gps);

        cancel = (Button) findViewById(R.id.btn_gps_cancel);
        confirm = (Button) findViewById(R.id.btn_gps_confirm);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // 위치서비스를 불러옴
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); // 위치가 켜져있는지
        if (isGpsEnabled) {
            startActivity(new Intent(this, WifiSearchPopup.class));
            finish();
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 위치 키는 화면을 불러옴
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, 100);
            }
        });
    }

    // 위치 활성화 화면 종료 시 호출
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 0) {
                gps = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
                    // 위치가 꺼져있을 때
                    Toast.makeText(this, "위치서비스를 켜주세요.", Toast.LENGTH_SHORT).show();;
                }else {
                    // 위치가 켜져있을 때
                    startActivity(new Intent(this, WifiSearchPopup.class));
                    finish();
                }
            }
        }
    }

}
