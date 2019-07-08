package open.it.com.petit.Main.Callback;

/**
 * Created by user on 2018-02-08.
 */

public interface MainCallback {
    interface ViewCallback {

    }

    interface MqttCallback {
        void onMqttFailure();

        void onShare(String msg);

        void onChangeMaster(String msg);
    }
}
