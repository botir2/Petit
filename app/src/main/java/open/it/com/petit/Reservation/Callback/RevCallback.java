package open.it.com.petit.Reservation.Callback;

/**
 * Created by user on 2018-02-05.
 */

public interface RevCallback {
    interface Reservation {
        void onSendMqtt(byte[] bytes);
    }

    interface Mqtt {
        void onCompute(byte[] bytes);

        void onShowMessage(String msg);

        void onConnected(boolean b);
    }
}
