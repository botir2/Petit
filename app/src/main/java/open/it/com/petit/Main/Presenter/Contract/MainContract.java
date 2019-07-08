package open.it.com.petit.Main.Presenter.Contract;

import java.util.ArrayList;

import open.it.com.petit.Main.Data.Feeder;

/**
 * Created by user on 2018-02-08.
 */

public interface MainContract {
    interface View {
        void toast(final String msg);

        void setProgress(boolean b);

        void startMediaActivity(Feeder feeder);

        void startReservationActivity(Feeder feeder);

        void startHistoryActivity(Feeder feeder);

        void startSettingActivity(Feeder feeder);
    }

    interface Presenter {
        void attachView(MainContract.View view);

        void detachView();

        void setFeederAdapterView(FeederAdapterContract.View adapterView);

        void setFeederAdapterModel(FeederAdapterContract.Model adapterModel);

        void connectMqtt();

        void disConnectMqtt();

        void getFeederList();

        void onUpdateList(ArrayList items);

        void compareToken();
    }
}
