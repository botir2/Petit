package open.it.com.petit.Reservation.Presenter.Contract;

import java.util.ArrayList;

import open.it.com.petit.Reservation.Data.Time;
import open.it.com.petit.Reservation.Adapter.Listener.OnItemClickListener;

/**
 * Created by user on 2018-01-31.
 */

public interface TimeAdapterContract {
    interface View {
        void setOnClickListener(OnItemClickListener clickListener);

        void notifyAdapter();
    }

    interface Model {
        void addItems(ArrayList items);

        void clearItems();

        Time getItem(int position);

        ArrayList getList();
    }
}
