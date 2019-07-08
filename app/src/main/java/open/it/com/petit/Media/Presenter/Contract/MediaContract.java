package open.it.com.petit.Media.Presenter.Contract;

import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by user on 2018-02-14.
 */

public interface MediaContract {
    interface View {
        void toast(String msg);

        void setButtonEnable(boolean b);

        void setProgress(boolean b);

        int getWindowWidth();

        int getWindowHeight();

        DisplayMetrics getDisplayMetrics();

        boolean getPortrait();
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void startMedia();

        void setSurfaceView(SurfaceView surfaceView);

        void setHolder(SurfaceHolder holder);

        void connectMqtt(String guid);

        void disConnectMqtt();

        void stopVideo();

        void releasePlayer();
    }
}
