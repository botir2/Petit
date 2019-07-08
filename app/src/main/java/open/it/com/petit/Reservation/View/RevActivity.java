package open.it.com.petit.Reservation.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import open.it.com.petit.R;
import open.it.com.petit.Reservation.Adapter.TimeAdapter;
import open.it.com.petit.Reservation.Presenter.RevPresenter;
import open.it.com.petit.Reservation.Presenter.Contract.RevContract;
import open.it.com.petit.Reservation.Data.Time;

/**
 * Created by user on 2018-01-31.
 */

public class RevActivity extends AppCompatActivity implements RevContract.View {
    private static final String TAG = RevActivity.class.getSimpleName();

    public static final int CLICK = R.drawable.feeder_reservation_btn_pink; // 클릭 FLAG
    public static final int UNCLICK = R.drawable.feeder_reservation_btn_gray; // un클릭 FLAG

    // 시간 배열
    private final String spinTime[]
            = {"시간", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00",
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00"};
    // 급식량 배열
    private final String spinProvisions[]
            = {"급식량", "01", "02", "03", "04", "05"};

    public static boolean isConnected = false; // mqtt 연결 flag
    private boolean isAm = true; // am, pm flag

    private RevContract.Presenter presenter;

    // 요일 버튼
    @BindViews({R.id.sunday_btn, R.id.monday_btn, R.id.tuesday_btn, R.id.wednesday_btn,
            R.id.thursday_btn, R.id.friday_btn, R.id.saturday_btn, R.id.everyday_btn})
    Button dayBtn[] = new Button[8];
    // 펫 네임
    @BindView(R.id.tv_pfm_reserve_pet_name)
    TextView tvPetName;
    @BindView(R.id.am_btn)
    Button amBtn; // am버튼
    @BindView(R.id.pm_btn)
    Button pmBtn; // pm버튼
    @BindView(R.id.reserve_day_save)
    LinearLayout daySave; // 저장 버튼
    @BindView(R.id.reserve_time_add)
    LinearLayout timeSave; // 시간 추가 버튼
    @BindView(R.id.spn_pfm_reserver_times)
    Spinner spn_times; // 시간 spinner
    @BindView(R.id.spn_pfm_reserver_provisions)
    Spinner spn_provisions; // 급식량 spinner
    @BindView(R.id.rv_pfm_reserve_list)
    RecyclerView recyclerView;
    @BindView(R.id.reservation_pb)
    ProgressBar pb;

    private TimeAdapter timeAdapter;

    private ArrayAdapter revTimeAdapter; // 시간 선택 adapter
    private ArrayAdapter revProvisionAdapter; // 급식량 선택 adapter

    private Intent intent;
    private String guid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_feeder_reserve_activity);
        ButterKnife.bind(this);
        intent = getIntent();
        guid = intent.getStringExtra("GUID");

        // 급식 데이터 Adapter
        timeAdapter = new TimeAdapter(this);
        recyclerView.setAdapter(timeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        revTimeAdapter = new ArrayAdapter(this, R.layout.petit_reserve_spinner, spinTime);
        revProvisionAdapter = new ArrayAdapter(this, R.layout. petit_reserve_spinner,spinProvisions);

        spn_times.setAdapter(revTimeAdapter);
        spn_provisions.setAdapter(revProvisionAdapter);

        presenter = new RevPresenter(this, guid);
        presenter.attachView(this);
        presenter.setTimeAdapterModel(timeAdapter);
        presenter.setTimeAdapterView(timeAdapter);
        presenter.connectMqtt(guid);
    }

    // 요일 클릭 Listener
    @OnClick({R.id.sunday_btn, R.id.monday_btn, R.id.tuesday_btn, R.id.wednesday_btn,
            R.id.thursday_btn, R.id.friday_btn, R.id.saturday_btn, R.id.everyday_btn})
    void onDayClick(View view) {
        int id = view.getId(); // id 식별

        Log.d(TAG, "CLICK : " + CLICK);
        Log.d(TAG, "UNCLICK : " + UNCLICK);
        switch (id) {
            case R.id.sunday_btn:
                setDayBtn(0);
                break;
            case R.id.monday_btn:
                setDayBtn(1);
                break;
            case R.id.tuesday_btn:
                setDayBtn(2);
                break;
            case R.id.wednesday_btn:
                setDayBtn(3);
                break;
            case R.id.thursday_btn:
                setDayBtn(4);
                break;
            case R.id.friday_btn:
                setDayBtn(5);
                break;
            case R.id.saturday_btn:
                setDayBtn(6);
                break;
            case R.id.everyday_btn:
                setDayBtn(7);
                break;
        }
    }

    // 요일 저장 Listener
    @OnClick(R.id.reserve_day_save)
    void onSaveClick() {
        if (!isConnected) {
            toast("장치가 꺼져있습니다. 다시 시도해주세요.");
            return;
        }
        presenter.onUpdate();
    }

    // 급식데이터 추가 Listener
    @OnClick(R.id.reserve_time_add)
    void onTimeAddClick() {
        if (!isConnected) {
            toast("장치가 꺼져있습니다. 다시 시도해주세요.");
            return;
        }

        if (presenter.getTimeList().size() > 20) {
            toast("최대 예약 횟수를 초과하였습니다.");
            return;
        }

        String time = spn_times.getSelectedItem().toString(); // 선택된 time value
        String provision = spn_provisions.getSelectedItem().toString(); // 선택된 provision value

        if (time.equals("시간")) {
            toast("시간을 선택해주세요.");
            return;
        }

        if (provision.equals("급식량")) {
            toast("급식량을 선택해주세요.");
            return;
        }

        if (!isAm) {
            //오후
            if (time.substring(0, 1).equals("0")) {
                time = time.substring(1, 2); //시간 01 -> 1
            } else if (time.substring(0, 1).equals("1")) {
                time = time.substring(0, 2); // 시간 13 -> 13
            }

            provision = provision.substring(1, 2); // 급식량 03 -> 3
            int timeTmp = Integer.valueOf(time) + 12; // 오후이기 때문에 + 12시간
            time = String.valueOf(timeTmp);
        } else {
            //오전
            if (time.substring(0, 1).equals("0")) {
                time = time.substring(1, 2);
            } else if (time.substring(0, 1).equals("1")) {
                time = time.substring(0, 2);
            }

            provision = provision.substring(1, 2);
        }

        Time item = new Time(Integer.valueOf(time), Integer.valueOf(provision)); // 시간과 급식량
        presenter.addTime(item);
    }

    // am or pm 버튼 Listener
    @OnClick({R.id.am_btn, R.id.pm_btn})
    void onMeridiemClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.am_btn:
                // am 선택시 am컬러 yellow pm컬러 gray
                isAm = true;
                amBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_yellow));
                amBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                pmBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                pmBtn.setTextColor(getResources().getColor(R.color.colorDarkGray));
                break;
            case R.id.pm_btn:
                // pm 선택시 pm컬러 yellow am컬러 gray
                isAm = false;
                amBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                amBtn.setTextColor(getResources().getColor(R.color.colorDarkGray));
                pmBtn.setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_yellow));
                pmBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                break;
        }
    }

    // 요일별 클릭 method
    private void setDayBtn(int idx) {
        if (!isConnected) {
            toast("장치가 꺼져있습니다. 다시 시도해주세요.");
            return;
        }

        // 매일을 누를 때
        if (idx == 7) {
            // 매일이 눌러져 있을 때
            if (dayBtn[idx].getTag().equals(CLICK)) {
                // 모든 버튼 클릭 해제
                for (int i = 0 ; i < 8 ; i ++) {
                    dayBtn[i].setTag(UNCLICK);
                    dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
                }
            }
            // 매일이 안눌러져 있을 때
            else if (dayBtn[idx].getTag().equals(UNCLICK)) {
                // 모든 버튼 클릭 설정
                for (int i = 0 ; i < 8 ; i ++) {
                    dayBtn[i].setTag(CLICK);
                    dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
                }
            }
            return;
        }

        // 누르려는 버튼이 click 일 때
        if (dayBtn[idx].getTag().equals(CLICK)) {
            dayBtn[7].setTag(UNCLICK); // 누르는 버튼이 unclick이므로 sunday 해제
            dayBtn[7].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
            dayBtn[idx].setTag(UNCLICK); // 누르는 버튼 unclick
            dayBtn[idx].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_gray));
        }
        // 누르려는 버튼이 unclick일 때
        else if (dayBtn[idx].getTag().equals(UNCLICK)) {
            dayBtn[idx].setTag(CLICK); // 누르는 버튼 click
            dayBtn[idx].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
            // 버튼이 하나라도 unclick 일때 종료
            for (int i = 0 ; i < 8 ; i ++) {
                if (dayBtn[i].getTag().equals(UNCLICK))
                    return;
            }
            // 모든 버튼이 눌려있으면 일요일 click
            dayBtn[7].setTag(CLICK);
            dayBtn[7].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
        }
    }

    // 장치 활성화 여부 method
    @Override
    public void setEnabled(boolean b) {
        if (b) isConnected = true;
        else {
            isConnected = false;
            toast("장치가 꺼져있습니다. 다시 시도해주세요.");
        }
    }

    @Override
    public void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 장치에서 받아온 데이터로 화면을 그려줌.
     * @param1 : 요일 데이터
     * @param2 : time 데이터
     */
    @Override
    public void paint(int[] dayBinary, ArrayList times) {
        tvPetName.setText(intent.getStringExtra("petname"));
        for (int i = 0 ; i < dayBinary.length ; i ++) {
            if (dayBinary[i] == 1) {
                dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
                dayBtn[i].setTag(CLICK);
                continue;
            }
            dayBtn[i].setTag(UNCLICK);
        }

        if (dayBtn[7].getTag().equals(CLICK)) {
            for (int i = 0; i < dayBinary.length; i++) {
                dayBtn[i].setBackground(getResources().getDrawable(R.drawable.feeder_reservation_btn_pink));
                dayBtn[i].setTag(CLICK);
            }
        }

        presenter.loadItems(times); // item list load
    }

    // 버튼 return
    @Override
    public Button[] getDayBtnInfo() {
        return dayBtn;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.disConnectMqtt();
    }
}
