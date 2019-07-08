package open.it.com.petit.Util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by user on 2017-10-30.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static String getPhoneNum(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String phoneNumber = "";
        try {
            phoneNumber = telephonyManager.getLine1Number();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (phoneNumber == null)
            return null;
        else
            return phoneNumber.replace("+82", "0");
    }

    public static String getDate() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        return sdf.format(date).toString();
    }
}
