package open.it.com.petit.Reservation.Presenter.Contract;

import android.widget.Button;

import java.util.ArrayList;

import open.it.com.petit.Reservation.Data.Time;

/**
 * Created by user on 2018-01-31.
 */

public interface RevContract {
    interface View {
        void paint(int[] dayBinary, ArrayList times);

        void toast(String msg);

        void setEnabled(boolean b);

        Button[] getDayBtnInfo();
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void setTimeAdapterView(TimeAdapterContract.View adapterView);

        void setTimeAdapterModel(TimeAdapterContract.Model adapterModel);

        byte[] getTimeData();

        ArrayList<Time> getTimeList();

        void addTime(Time item);

        void connectMqtt(String guid);

        void disConnectMqtt();

        void loadItems(ArrayList<Time> map);

        void onUpdate();
    }
}
