package open.it.com.petit.Reservation.Presenter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import open.it.com.petit.Reservation.Model.MqttModel;
import open.it.com.petit.Reservation.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Reservation.View.RevActivity;
import open.it.com.petit.Reservation.Callback.RevCallback;
import open.it.com.petit.Reservation.Presenter.Contract.RevContract;
import open.it.com.petit.Reservation.Model.RevModel;
import open.it.com.petit.Reservation.Data.Time;
import open.it.com.petit.Reservation.Presenter.Contract.TimeAdapterContract;

/**
 * Created by user on 2018-01-31.
 */

public class RevPresenter implements RevContract.Presenter, RevCallback.Reservation, RevCallback.Mqtt, OnItemClickListener {
    private static final String TAG = RevPresenter.class.getSimpleName();

    private Context context;
    private SharedPreferences pref; // APP에 영구적 data 저장 객체

    private RevContract.View view;
    private MqttModel mqttModel;
    private RevModel revModel;

    private TimeAdapterContract.View adapterView;
    private TimeAdapterContract.Model adapterModel;

    private byte[] defaultByte = new byte[48]; // 예약데이터 저장 byte 배열
    private String feedDataStr; // SharedPreferences에 예약 데이터를 저장하기 위한 String
    private String guid;

    public RevPresenter(Context context, String guid) {
        this.context = context;
        this.guid = guid;
        this.pref = context.getSharedPreferences("revData", Context.MODE_PRIVATE); // data 저장소 이름 revData
        // byte initialize
        for (int i = 0 ; i < 48 ; i ++)
            defaultByte[i] = (byte) 0;
        // 예약 data 이름 feedData-[guid]
        this.feedDataStr = pref.getString("feedData-" + guid, new String(defaultByte, 0, defaultByte.length));
        // mqtt connect 모델
        this.mqttModel = new MqttModel(context, guid, feedDataStr.getBytes(), this);
        // reservation 모델
        this.revModel = new RevModel(context, guid, this);
    }

    @Override
    public void attachView(RevContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void setTimeAdapterView(TimeAdapterContract.View adapterView) {
        this.adapterView = adapterView;
        this.adapterView.setOnClickListener(this);
    }

    @Override
    public void setTimeAdapterModel(TimeAdapterContract.Model adapterModel) {
        this.adapterModel = adapterModel;
    }

    // feedData byte array return
    @Override
    public byte[] getTimeData() {
        return feedDataStr.getBytes();
    }

    // list 갱신 method
    @Override
    public void addTime(Time item) {
        // clone : java에서 object는 call by reference이기 때문에
        // 그냥 대입 시 같은 object의 주소 번지를 참조하므로 clone() 메소드를 쓴다.
        ArrayList<Time> items = (ArrayList) adapterModel.getList().clone();

        // 예약데이터 중복 확인
        for (Time t : items) {
            if (item.getTime() == t.getTime()) {
                view.toast("예약이 중복됩니다.");
                return;
            }
        }
        items.add(item);

        // time data sort
        Collections.sort(items, new Comparator<Time>() {
            @Override
            public int compare(Time o1, Time o2) {
                return (o1.getTime() < o2.getTime()) ? -1 : (o1.getTime() > o2.getTime()) ? 1 : 0;
            }
        });

        // data를 보내기 위해 day와 time을 갱신
        revModel.setDay(view.getDayBtnInfo());
        revModel.setTime(items);
        // data를 계산하고 petit에 보낸다
        revModel.computeAndSendToDevice();

        // list update
        loadItems(items);
    }

    @Override
    public void connectMqtt(String guid) {
        mqttModel.connect();
    }

    @Override
    public void disConnectMqtt() {
        mqttModel.disConnect();
    }

    // mqtt연결여부 flag 변경 method
    @Override
    public void onConnected(boolean b) {
        view.setEnabled(b);
    }

    // device에서 받아온 byte data를 객체 data로 파싱하고 view로 보냄.
    @Override
    public void onCompute(byte[] bytes) {
        int day = bytes[0]; // day값은 0, 2, 4 .. 번지가 동일 하기 때문에 0번으로 넣음
        int[] binary = new int[8]; // 8bit day값을 8개의 int로 변환 ex) 0100 0001
        for (int i = 7 , num = day ; i >= 0 ; i --, num >>>= 1)
            binary[7 - i] = num & 1;

        ArrayList<Time> times = new ArrayList<>();
        int j = 0;
        // time 데이터가 0이 아닐 시 Time Object에 저장.
        // 1, 3, 5 ... 번지에 저장이 됨
        for (int i = 1 ; i < bytes.length ; i += 2) {
            if (bytes[i] != 0)
                times.add(new Time(i - j, (int)bytes[i]));
            j += 1;
        }
        // view에 그래픽 처리 요청
        view.paint(binary, times);
    }

    @Override
    public void loadItems(ArrayList<Time> items) {
        if (items != null) {
            adapterModel.clearItems();
            adapterModel.addItems(items);
            adapterView.notifyAdapter();
        }
    }

    // 요일 변경 update
    @Override
    public void onUpdate() {
        revModel.setDay(view.getDayBtnInfo());
        revModel.setTime(adapterModel.getList());
        revModel.computeAndSendToDevice();
    }

    // 예약 data를 지움
    @Override
    public void onRemove(int position) {
        if (!RevActivity.isConnected) {
            onShowMessage("장치가 꺼져있습니다. 다시 시도해주세요.");
            return;
        }

        ArrayList<Time> items = (ArrayList) adapterModel.getList().clone();
        items.remove(position);
        revModel.setDay(view.getDayBtnInfo());
        revModel.setTime(items);
        revModel.computeAndSendToDevice();

        loadItems(items);
    }

    // publish
    @Override
    public void onSendMqtt(byte[] bytes) {
        mqttModel.publishReservationByte("$open-it/pet-it/" + guid + "/order", bytes);
    }

    @Override
    public void onShowMessage(String msg) {
        view.toast(msg);
    }

    // Time list get
    @Override
    public ArrayList<Time> getTimeList() {
        return adapterModel.getList();
    }
}
