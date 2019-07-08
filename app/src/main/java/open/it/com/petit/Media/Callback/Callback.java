package open.it.com.petit.Media.Callback;

import android.util.DisplayMetrics;

/**
 * Created by user on 2018-02-19.
 */

public interface Callback {
    interface Mqtt {
        void onShowMessageMqtt(String msg);

        void onMqttSetProgress(boolean b);

        void onStartMedia();
    }

    interface Media {
        void onShowMessageMedia(String msg);

        void onMediaSetProgress(boolean b);

        int getWidth();

        int getHeight();

        boolean getPortrait();
    }
}
