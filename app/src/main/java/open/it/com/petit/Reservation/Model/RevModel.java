package open.it.com.petit.Reservation.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;

import open.it.com.petit.Reservation.View.RevActivity;
import open.it.com.petit.Reservation.Callback.RevCallback;
import open.it.com.petit.Reservation.Data.Time;

/**
 * Created by user on 2018-01-31.
 */

public class RevModel{
    private static final String TAG = RevModel.class.getSimpleName();

    private Context context;
    private int dayClicked[];
    private ArrayList<Time> items;
    private RevCallback.Reservation callback;

    private SharedPreferences pref;
    private String guid;

    private byte dayByte = 0;
    private byte[] bytes = new byte[48];
    //private byte[] defaultByte = new byte[48];

    public RevModel(Context context, String guid, RevCallback.Reservation callback) {
        this.callback = callback;
        this.guid = guid;
        this.dayClicked = new int[8];
        this.pref = context.getSharedPreferences("revData", Context.MODE_PRIVATE);

        init();
    }

    private void init() {
        /*for (int i = 0 ; i < defaultByte.length ; i ++)
            defaultByte[i] = (byte) 0;*/
        for (int i = 0 ; i < bytes.length ; i ++)
            bytes[i] = (byte) 0;

        dayByte = 0;
    }

    // day button click 여부 set
    public void setDay(Button[] dayBtn) {
        for (int i = 0 ; i < dayBtn.length ; i ++)
            dayClicked[i] = (int) dayBtn[i].getTag();
    }

    // Time data set
    public void setTime(ArrayList<Time> items) {
        this.items = items;
    }

    // view에 있는 요일, time, 급식량 data를 byte로 변환 후 device에 보내는 method
    public void computeAndSendToDevice() {
        init();
        // click 되어 있는 요일 button byte화
        for (int i = 0 ; i < dayClicked.length ; i ++)
            if (dayClicked[i] == RevActivity.CLICK)
                dayByte += (byte) Math.pow(2, i);

        // 모든 요일이 눌려 있을 때 (overflow로 -1이 저장됨)
        if (dayByte == -1)
            dayByte = 127;

        // 요일 byte 번지에 요일byte 저장
        for (int i = 0 ; i < bytes.length ; i += 2)
            bytes[i] = dayByte;

        // 급식량 byte 번지에 급식량 저장
        for (Time time : items)
            bytes[time.getTime() * 2 - 1] = (byte) time.getMount();

        // 급식 data를 device에 보냄.
        callback.onSendMqtt(bytes);
    }
}
