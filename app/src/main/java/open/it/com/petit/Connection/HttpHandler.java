package open.it.com.petit.Connection;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by user on 2017-10-31.
 */

public class HttpHandler extends Handler implements HttpConnect {
    private Context context;
    protected Message msg;

    public HttpHandler() {
    }

    public HttpHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        this.msg = msg;
        switch (this.msg.what) {
            case ConnectCode.HTTP_OK:
                onHttpOK();
                break;
            case ConnectCode.HTTP_CLIENT_TIMEOUT:
            case ConnectCode.HTTP_NOT_FOUND:
                onHttpError();
                break;
            case ConnectCode.GET_FEEDER_INFO:
                onGetFeederInfo();
                break;
        }
    }

    @Override
    public void onHttpOK() {

    }

    @Override
    public void onHttpError() {
        if (context != null)
            Toast.makeText(context, "서버 연결 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetFeederInfo() {

    }
}
