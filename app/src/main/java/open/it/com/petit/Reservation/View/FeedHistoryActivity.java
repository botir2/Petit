package open.it.com.petit.Reservation.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import open.it.com.petit.Reservation.Adapter.ReserveHistoryAdapter;
import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Reservation.Data.Time;
import open.it.com.petit.Util.Util;
import open.it.com.petit.Reservation.Data.RHChild;
import open.it.com.petit.Reservation.Data.RHParent;
import open.it.com.petit.Mqtt.BaseMqtt;
import open.it.com.petit.R;


/**
 * Created by user on 2017-06-01.
 */

// BaseMqtt 상속
public class FeedHistoryActivity extends BaseMqtt {
    public static final String TAG = FeedHistoryActivity.class.getSimpleName();

    private Intent intent;
    private SharedPreferences sfr; // 알람 테스트용으로 쓴 임시 저장소

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView pet_name;

    private List<RHParent> historyList;

    @Override
    public void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        setContentView(R.layout.petit_feed_history_activity);

        intent = getIntent();
        GUID = getIntent().getStringExtra("GUID");

        subTopic = "$open-it/pet-it/" + GUID + "/status";
        pubTopic = "$open-it/pet-it/" + GUID + "/order";

        pet_name = (TextView) findViewById(R.id.tv_pfm_feed_history_petname);
        pet_name.setText(intent.getStringExtra("petname"));

        recyclerView = (RecyclerView) findViewById(R.id.rv_pfm_feed_history);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        connect(subTopic, pubTopic); // mqtt connect

        sfr = getSharedPreferences("system_setting", MODE_PRIVATE);
        if (sfr.getInt("isAlarm", 0) != 0) { // 알람 on / off 여부
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("message", "급식이 실행되었습니다.");
            map.put("GUID", getIntent().getStringExtra("GUID"));
            map.put("P_NUM", Util.getPhoneNum(this));

            ConnectionController conn = new ConnectionController(this);
            conn.setMethod("POST").setHash(map).setUrl("push_notification.php");
            Thread thread = new Thread(conn);
            thread.start();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        try {
            mqttAndroidClient.subscribe(subTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to topic :" + subTopic);
                    // 예약 내역을 요청
                    String msg = "history request";
                    publish(pubTopic, msg.getBytes());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "topic subscription failed for topic :" + subTopic);
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "topic subscription failed for topic : " + subTopic);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // 예외처리 할 것
        byte buffer[] = message.getPayload();
        reservationByteToList(buffer);
        adapter = new ReserveHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);
    }

    private void reservationByteToList(byte buffer[]) {
        Log.d(TAG, "buffer size " + buffer.length);

        // 예약 데이터(18byte) : 년 월 일 시 분 Type(수동, 예약) 급식량 UserPhone(11byte)
        StringBuilder reserveList[] = new StringBuilder[buffer.length / 18];
        historyList = new ArrayList<>();

        for(int i = 0 ; i < reserveList.length; i ++)
            reserveList[i] = new StringBuilder();

        int i = 0, j = 0;
        for(byte b : buffer) {
            // 한자리수 앞에 0을 붙임
            if ((int)b < 10)
                reserveList[j].append(0);
            reserveList[j].append(b);

            i ++;
            // 18byte로 나눔
            if (i != 0 && i % 18 == 0) {
                String parent = convertParent(reserveList[j].substring(0,14).toString()); // 년 월 일 시 분 Type 급식량
                String child = convertChild(reserveList[j].substring(14, 36).toString()); // 핸드폰 번호

                historyList.add(new RHParent(parent, Arrays.asList(new RHChild(child)), R.drawable.close_list02));

                Log.d(TAG, reserveList[j].substring(0,14).toString());
                Log.d(TAG, reserveList[j].substring(14,36).toString());
                j++;
                Log.d(TAG, "=========================");
            }
        }
    }

    // ExpandableRecyclerView Root item 변환
    private String convertParent(String parent) {
        String item = parent;
        String year = item.substring(0, 2);
        String month = item.substring(2, 4);
        String day = item.substring(4, 6);
        String hour = item.substring(6, 8);
        String min = item.substring(8, 10);
        int type_ascii = Integer.valueOf(item.substring(10,12)); // 수동, 예약, 원격
        String feed_amount = item.substring(12,14);

        String type = Character.toString((char)type_ascii); // 아스키 값 변환
        String rev_type = type.equals("M") ? "수동급식" : (type.equals("A") ? "예약급식" : "원격급식");

        return year + "." + month + "." + day + " " + hour + ":" + min + " " + rev_type + ", 급식량(" + feed_amount + ")";
    }

    // ExpandableRecyclerView Child item 변환
    private String convertChild(String child) {
        // 수동/예약 급식일 시 번호가 00000000000 이기 때문에 아스키코드로 4848484848484848484848
        if (child.equals("4848484848484848484848")) {
            return " : 수동/예약";
        } else {
            // 핸드폰 번호 변환 과정
            int pTmp[] = new int[11];
            StringBuilder str = new StringBuilder();
            int pIdx = 0;
            for (int idx = 0 ; idx < 22 ; idx += 2) {
                pTmp[pIdx] = Integer.valueOf(child.substring(idx, idx + 2));
                str.append(Character.toString((char) pTmp[pIdx++]));
            }
            return " : " + str.toString();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disConnect();
    }
}
