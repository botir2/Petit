package open.it.com.petit.NotUse;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by user on 2017-07-12.
 */

public class WifiConnectHandler {
    private static final String TAG = WifiConnectHandler.class.getSimpleName();
    private WifiConfiguration wfc;
    private ScanResult ap;
    private WifiManager wifiManager;
    private String password;
    private Context context;
    private List<WifiConfiguration> list;

    public WifiConnectHandler(ScanResult ap, WifiManager wifiManager, String password, Context context) {
        this.ap = ap;
        this.wifiManager = wifiManager;
        this.password = password;
        this.context = context;
    }

    public WifiConnectHandler(ScanResult ap, WifiManager wifiManager, String password) {
        this.ap = ap;
        this.wifiManager = wifiManager;
        this.password = password;
    }

    public int wifiConnect() {
        Log.d(TAG, "wifiConnect");
        int netid;
        wifiManager.setWifiEnabled(true);

        int i = 0;
        while (!wifiManager.isWifiEnabled()) {
            try {
                Log.d(TAG, "와이파이 연결을 기다립니다.");
                Thread.sleep(100);
                if ((i ++) == 100)
                    return -100;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        list = wifiManager.getConfiguredNetworks();
        if (list == null) {
            Log.d(TAG, "list null");
            return -100;
        }

        for (WifiConfiguration w : list) {
            Log.d(TAG, "w ssid : " + w.SSID);
            Log.d(TAG, "w networkId : " + w.networkId);
            Log.d(TAG, "preSharedKey : " + w.preSharedKey);
            Log.d(TAG, "bssid : " + w.BSSID);
            //boolean b1 = wifiManager.disableNetwork(w.networkId);
            boolean b2 = wifiManager.removeNetwork(w.networkId);
            //Log.d(TAG, "b1 : " + b1);
            Log.d(TAG, "b2 : " + b2);
            wifiManager.saveConfiguration();
        }

        wfc = new WifiConfiguration();
        if (ap.capabilities.contains("WEP")) {
            /*wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.wepKeys[0] = "pw";*/
            return 0;
        } else if (ap.capabilities.contains("WPA")) {
            Log.d(TAG, "WPA");
            Log.d(TAG , ap.SSID);
            return connectWPA(ap.SSID);
        } else {
            return 0;
        }
    }

    private int connectWPA(String ssid) {
        wfc.SSID = "\"".concat(ssid).concat("\"");
        wfc.status = WifiConfiguration.Status.ENABLED;
        wfc.priority = 40; //우선순위를 정해줌.(높을수록 우선순위가 큼)

        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        // WPA용 pairwise 암호집합.
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        wfc.preSharedKey = "\"".concat(password).concat("\"");

        Log.d(TAG, "wfc.SSID : " + wfc.SSID);
        Log.d(TAG, "wfc.preSharedKey : " + wfc.preSharedKey);
        // 저장 돼 있거나 비밀번호가 틀린경우 -1을 return.
        int netId = wifiManager.addNetwork(wfc);

        if (netId == -1) {
            wifiManager.updateNetwork(wfc);
        }
        Log.d(TAG, "networkId : " + wfc.networkId);
        Log.d(TAG, "netId : " + netId);
        Log.d(TAG, "nenenenene : " + wifiManager.getConnectionInfo().getNetworkId());

        return netId;
    }

    public String getSSID() {
        return ap.SSID;
    }
}
