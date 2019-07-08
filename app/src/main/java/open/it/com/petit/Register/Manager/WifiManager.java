package open.it.com.petit.Register.Manager;

import android.content.Context;

/**
 * Created by user on 2017-07-31.
 */

// wifimanager sington
public class WifiManager {
    private static final String TAG = "WifiManager";
    private Context context;
    private android.net.wifi.WifiManager wifiManager;

    public static WifiManager instance;

    public static WifiManager getInstance(Context context) {
        if (instance == null) {
            instance = new WifiManager(context);
        }
        return instance;
    }

    private WifiManager(Context context) {
        this.context = context;
        this.wifiManager = (android.net.wifi.WifiManager) context.getSystemService(context.WIFI_SERVICE);
    }

    public android.net.wifi.WifiManager getWifiManager() {
        return wifiManager;
    }
}
