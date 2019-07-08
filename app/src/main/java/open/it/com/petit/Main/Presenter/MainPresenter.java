package open.it.com.petit.Main.Presenter;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import open.it.com.petit.Connection.ConnectCode;
import open.it.com.petit.Main.Callback.ConnectionCallback;
import open.it.com.petit.Main.Model.ConnectionModel;
import open.it.com.petit.Main.Data.Feeder;
import open.it.com.petit.Main.Presenter.Contract.FeederAdapterContract;
import open.it.com.petit.Main.Callback.MainCallback;
import open.it.com.petit.Main.Presenter.Contract.MainContract;
import open.it.com.petit.Main.Model.MqttModel;
import open.it.com.petit.Main.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2018-02-08.
 */

public class MainPresenter implements MainContract.Presenter, MainCallback.MqttCallback, ConnectionCallback, OnItemClickListener {
    private static final String TAG = MainPresenter.class.getSimpleName();

    private Context context;
    private MainContract.View view;

    private ConnectionModel connectionModel; // 서버 연결을 위한 class
    private MqttModel mqttModel; // mqtt 연결을 위한 class
    private FeederAdapterContract.Model adapterModel;
    private FeederAdapterContract.View adapterView;

    public MainPresenter(Context context) {
        this.context = context;
        this.connectionModel = new ConnectionModel(context, this);
        this.mqttModel = new MqttModel(context, this);
    }

    // view callback을 위한 메소드
    @Override
    public void attachView(MainContract.View view) {
        this.view = view;
    }

    // view callback 제거를 위한 메소드
    @Override
    public void detachView() {
        this.view = null;
    }

    // adapterview callback을 위한 메소드
    @Override
    public void setFeederAdapterView(FeederAdapterContract.View adapterView) {
        this.adapterView = adapterView;
        this.adapterView.setOnClickListener(this); // 클릭 리스너 bind
    }

    // adaptermodel callback을 위한 메소드
    @Override
    public void setFeederAdapterModel(FeederAdapterContract.Model adapterModel) {
        this.adapterModel = adapterModel;
    }

    @Override
    public void connectMqtt() {
        mqttModel.connect();
    }

    @Override
    public void disConnectMqtt() {
        mqttModel.disConnect();
    }

    // 급식기 list update를 위한 메소드
    @Override
    public void getFeederList() {
        String php = "get_feeder_info.php"; // php file name
        view.setProgress(true); // progress 보이게
        connectionModel
                .init()
                .setMethod("GET") // get 방식 사용
                .setClass(Feeder[].class) // data class인 feeder 클래스의 배열을 넘김
                .setPhp(php) // php file name set
                .setQueryString("P_NUM=" + Util.getPhoneNum(context)); // query string set
        Thread thread = new Thread(connectionModel);
        thread.start();
        try {
            thread.join(); // 동기화 작업. 서버 작업이 끝날때까지 기다림.
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setProgress(false); // progress 안보이게

        // http 메세지가 200이 아닐 시 연결 실패.
        if (connectionModel.getStatus() != ConnectCode.HTTP_OK)
            return;

        // 연결 하고 가져온 list를 구성
        ArrayList<Feeder> list = new ArrayList<>(connectionModel.getResult());

        //가져온 list 목록으로 update
        onUpdateList(list);
        //list의 token을 비교, 갱신
        compareToken();
    }

    // token 비교, 갱신을 위한 method
    @Override
    public void compareToken() {
        Log.d(TAG, "compareToken");
        // DB에서 가져온 list
        ArrayList<Feeder> items = adapterModel.getItems();

        if (items == null)
            return;

        if (items.size() <= 0)
            return;

        Log.d(TAG, FirebaseInstanceId.getInstance().getToken());

        for (int i = 0 ; i < items.size() ; i ++) {
            String token = items.get(i).getToken();
            // 토큰이 없을 때.
            if (token == null) {
                setToken(items, i);
            } else {
                // 토큰이 다르거나 공백일 때
                if (!token.equals(FirebaseInstanceId.getInstance().getToken())
                        || token.equals("")) {
                    setToken(items, i);
                }
            }
        }
    }

    // token을 set해줌.
    private void setToken(ArrayList<Feeder> items, int idx) {
        String php = "regist_token.php";
        // 일반 HashMap은 순서를 보장하지 않기 때문에 LinkedHashMap을 사용.
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        //Firebase에서 해당 APP의 토큰을 바로 가져와서 넣음.
        map.put("Token", FirebaseInstanceId.getInstance().getToken());
        //Phone number를 넣음
        map.put("P_NUM", Util.getPhoneNum(context));
        /**
         * Post방식은 Get방식과 다르게 query string을 넘길 수 없어서
         * HashMap등을 사용해서 data를 넘긴다.
         */
        connectionModel
                .init()
                .setMethod("POST")
                .setHash(map)
                .setPhp(php);
        Thread thread = new Thread(connectionModel);
        thread.start();

        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //token이 없는 list에 token을 갱신.
        items.get(idx).setToken(FirebaseInstanceId.getInstance().getToken());
    }

    // List View를 갱신한다.
    @Override
    public void onUpdateList(ArrayList items) {
        Log.d(TAG, "onUpdateList");
        if (items != null) {
            adapterModel.clearItems(); // list item을 전부 지운다.
            adapterModel.addItems(items); // list에 item들을 넣는다.
            adapterView.notifyAdapter(); // recycler view에 item 변경사항이 있다고 알린다.
        }
    }

    /**********************************MqttCallback***************************************/
    // mqtt 연결 실패 method
    @Override
    public void onMqttFailure() {
        view.toast("서버 연결에 실패했습니다. 다시 시도해주세요.");
    }

    // mqtt Share 메세지를 받았을 때 callback method
    @Override
    public void onShare(String msg) {
        // msg : phone number
        // 받은 message가 자신의 번호가 아닐 때 종료
        if (!msg.equals(Util.getPhoneNum(context)))
            return;

        Log.d(TAG, "onShare");
        view.setProgress(true);


        /**
         * Share 메세지는 공유가 성공 되면 발송이 되므로
         * 자신의 튜플에 token을 갱신한다.
         */
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("P_NUM", Util.getPhoneNum(context));
        map.put("Token", FirebaseInstanceId.getInstance().getToken());
        String php = "regist_token.php";
        connectionModel
                .init()
                .setMethod("POST")
                .setHash(map)
                .setPhp(php);
        Thread thread = new Thread(connectionModel);
        thread.start();

        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setProgress(false);
        getFeederList();
    }

    // mqtt MASTER 메세지를 받았을 때 callback method
    @Override
    public void onChangeMaster(String msg) {
        // msg : guid
        Log.d(TAG, "onChangeMaster");
        for (Feeder f : adapterModel.getItems()) {
            // 해당 급식기면 급식기 list를 갱신
            if (f.getGUID().equals(msg)) {
                getFeederList();
                return;
            }
        }
    }

    /**********************************Connection Callback***************************************/
    // 서버 get 성공 callback
    @Override
    public void onGetSuccess() {

    }

    // 서버 post 성공 callback
    @Override
    public void onPostSuccess(String result) {

    }

    // 서버 연결 실패 callback
    @Override
    public void onHttpFailure() {
        view.toast("서버 연결에 실패했습니다. 다시 시도해주세요.");
    }

    /**********************************OnItemClickListener***************************************/
    // media 버튼 눌렀을 때 callback
    @Override
    public void onMedia(Feeder feeder) {
        view.startMediaActivity(feeder);
    }

    // reservation 버튼 눌렀을 때 callback
    @Override
    public void onReservation(Feeder feeder) {
        view.startReservationActivity(feeder);
    }

    // history 버튼 눌렀을 때 callback
    @Override
    public void onHistory(Feeder feeder) {
        view.startHistoryActivity(feeder);
    }

    // setting 버튼 눌렀을 때 callback
    @Override
    public void onSetting(Feeder feeder) {
        view.startSettingActivity(feeder);
    }
}
