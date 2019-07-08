package open.it.com.petit.Connection;

import android.os.Message;

/**
 * Created by user on 2017-10-31.
 */

public interface HttpConnect {
    void onHttpOK();
    void onHttpError();
    void onGetFeederInfo();
}
