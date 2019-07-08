package open.it.com.petit.Main.Callback;

/**
 * Created by user on 2018-02-08.
 */

public interface ConnectionCallback {
    void onGetSuccess();

    void onPostSuccess(String result);

    void onHttpFailure();
}
