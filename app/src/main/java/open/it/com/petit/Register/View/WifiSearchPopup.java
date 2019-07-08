package open.it.com.petit.Register.View;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

import open.it.com.petit.Register.Manager.WifiManager;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-19.
 */

public class WifiSearchPopup extends Activity {
    private static final String TAG = WifiSearchPopup.class.getSimpleName();

    private ArrayList<ScanResult> scanList; // 와이파이 scan 결과 리스트
    private ImageView img;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle saveIntanceState) {
        super.onCreate(saveIntanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_wifi_search);

        img = (ImageView) findViewById(R.id.pb_wifi_search);
        Glide.with(this).load(R.drawable.spin_loader).into(img); // spin gif 이미지를 불러옴

        startScanWifi();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // search 완료
            if (intent.getAction().equals(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                searchFinish();
            }
        }
    };

    private void startScanWifi() {
        wifiManager = WifiManager.getInstance(this);
        wifiManager.getWifiManager().startScan();

        IntentFilter intentFilter = new IntentFilter(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, intentFilter); // wifi scan result에 대한 broadcast receiver를 등록
    }

    private void searchFinish() {
        unregisterReceiver(receiver); // broadcast receiver 해제
        scanList = (ArrayList) wifiManager.getWifiManager().getScanResults();
        if (scanList.isEmpty())
            scanList = null;

        for (ScanResult s : scanList) {
            Log.d(TAG ,s.SSID);
        }

        Intent intent = new Intent(this, WifiListPopup.class);
        intent.putParcelableArrayListExtra("scanList", scanList); // ArrayList를 intent로 보내기 위한 method
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
}
