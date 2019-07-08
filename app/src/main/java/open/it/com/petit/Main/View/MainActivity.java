package open.it.com.petit.Main.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import open.it.com.petit.Main.Data.Feeder;
import open.it.com.petit.Main.Adapter.FeederAdapter;
import open.it.com.petit.Main.Presenter.Contract.MainContract;
import open.it.com.petit.Main.Presenter.MainPresenter;
import open.it.com.petit.Media.Tmp.MediaActivityTmp;
import open.it.com.petit.R;
import open.it.com.petit.Register.View.AddPopup;
import open.it.com.petit.Register.View.ConnectGPS;
import open.it.com.petit.Reservation.View.FeedHistoryActivity;
import open.it.com.petit.Reservation.View.RevActivity;
import open.it.com.petit.Setting.AppSetting.SettingActivity;
import open.it.com.petit.Setting.FeederSetting.Popup.SettingPopup;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-08.
 */

public class MainActivity extends AppCompatActivity implements MainContract.View, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.feeder_list)
    RecyclerView recyclerView; // 급식기 list를 위한 view
    FeederAdapter adapter; // list에 data를 binding 해줄 adapter

    @BindView(R.id.main_drawer)
    DrawerLayout drawerLayout; // 좌측상단 navagation drawer를 위한 layout
    @BindView(R.id.main_navi)
    NavigationView navigationView; // 좌측상단 navagation drawer를 위한 view
    @BindView(R.id.btn_feeder_add)
    ImageButton addBtn; // 우측하단 급식기 추가 버튼
    @BindView(R.id.main_toolbar)
    Toolbar toolbar; // navigation drawer를 위한 toolbar
    @BindView(R.id.main_pb)
    ProgressBar pb; // 로딩 중을 알려줄 view

    private MainPresenter presenter; // MVP model 에서 Presenter
    private ActionBarDrawerToggle toggle; // navigation drawer를 위한 toggle.

    private boolean isFinish = false; // 뒤로가기 버튼을 위한 flag

    /**
     * 가장 처음 실행될 때 호출되는 생명주기.
     * 기본적인 flow. onCreate -> onStart -> onResume -> onPause -> onStop -> onDestory
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_main);
        ButterKnife.bind(this); // View binding library

        // 위에 쓸모 없는 display 제거용
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // navigation을 위한 toggle 할당 및 listener 지정
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // recyclerview 설정.
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // recyclerview adapter 할당 및 설정
        adapter = new FeederAdapter(this);
        recyclerView.setAdapter(adapter);

        // 핸드폰 번호 없는 공기계 예외처리.
        if (Util.getPhoneNum(this) == null) {
            toast("번호 없는 공기계는 사용할 수 없습니다.");
            return;
        }

        // presenter 할당.
        presenter = new MainPresenter(this);
        presenter.attachView(this); // view callback 사용을 위한 메소드
        presenter.setFeederAdapterModel(adapter); // adapter model callback set
        presenter.setFeederAdapterView(adapter); // adapter view callback set
        presenter.connectMqtt(); // mqtt connect
    }

    // 급식기 추가 버튼 Event Listener
    @OnClick(R.id.btn_feeder_add)
    void addBtnClick() {
        if (Util.getPhoneNum(this) == null) {
            toast("번호 없는 공기계는 사용할 수 없습니다.");
            return;
        }
        startActivity(new Intent(MainActivity.this, AddPopup.class));
    }

    // navigation drawer 클릭 Event Listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.naviitem_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.getPhoneNum(this) == null)
            return;
        presenter.getFeederList(); // 급식기 list update
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFinish = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Util.getPhoneNum(this) == null)
            return;
        presenter.detachView(); // callback 해제
        presenter.disConnectMqtt(); // mqtt disconnect
    }

    // progress view set
    @Override
    public void setProgress(final boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (b) pb.setVisibility(View.VISIBLE);
                else pb.setVisibility(View.INVISIBLE);
            }
        });
    }

    // show toast
    @Override
    public void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // go to media activity
    @Override
    public void startMediaActivity(Feeder feeder) {
        Intent intent = new Intent(this, MediaActivityTmp.class);
        intent.putExtra("GUID", feeder.getGUID());
        startActivity(intent);
    }

    // go to reservation activity
    @Override
    public void startReservationActivity(Feeder feeder) {
        Intent intent = new Intent(this, RevActivity.class);
        intent.putExtra("petname", feeder.getP_NAME());
        intent.putExtra("GUID", feeder.getGUID());
        startActivity(intent);
    }

    // go to history activity
    @Override
    public void startHistoryActivity(Feeder feeder) {
        Intent intent = new Intent(this, FeedHistoryActivity.class);
        intent.putExtra("petname", feeder.getP_NAME());
        intent.putExtra("GUID", feeder.getGUID());
        startActivity(intent);
    }

    // go to setting activity
    @Override
    public void startSettingActivity(Feeder feeder) {
        Intent intent = new Intent(this, SettingPopup.class);
        intent.putExtra("GUID", feeder.getGUID());
        intent.putExtra("PW", feeder.getPW());
        intent.putExtra("MS", feeder.getMS());
        startActivity(intent);
    }

    // 뒤로가기 버튼 callback
    @Override
    public void onBackPressed() {
        if (!isFinish) {
            isFinish = true;
            toast("뒤로가기 버튼을 한번 더 누르면 종료됩니다.");
            return ;
        }
        finish();
    }
}
