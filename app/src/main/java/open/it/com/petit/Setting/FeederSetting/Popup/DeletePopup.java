package open.it.com.petit.Setting.FeederSetting.Popup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import open.it.com.petit.Main.Callback.ConnectionCallback;
import open.it.com.petit.Main.Model.ConnectionModel;
import open.it.com.petit.Util.Util;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-31.
 */

public class DeletePopup extends AppCompatActivity implements ConnectionCallback {
    private final static String TAG = DeletePopup.class.getSimpleName();

    @BindView(R.id.btn_feeder_delete_cancel)
    Button cancel;
    @BindView(R.id.btn_feeder_delete_confirm)
    Button confirm;

    String GUID;
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_delete_popup);
        ButterKnife.bind(this);

        GUID = getIntent().getStringExtra("GUID");
    }

    @OnClick(R.id.btn_feeder_delete_cancel)
    void cancelClick() {
        finish();
    }

    @OnClick(R.id.btn_feeder_delete_confirm)
    void confirmClick() {
        Log.d(TAG, "????");
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("GUID", GUID);
        map.put("P_NUM", Util.getPhoneNum(getApplicationContext()));
        String php = "feeder_delete.php";
        ConnectionModel conn = new ConnectionModel(getApplicationContext(), this);
        conn.init().setMethod("POST").setHash(map).setPhp(php);
        Thread thread = new Thread(conn);
        thread.start();
    }

    @Override
    public void onGetSuccess() {

    }

    @Override
    public void onPostSuccess(String result) {
        toast("삭제되었습니다");
        String msg = "DELETE:" + GUID;
        finish();
    }

    @Override
    public void onHttpFailure() {
        toast("서버 연결에 실패했습니다. 다시 시도해주세요.");
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DeletePopup.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
