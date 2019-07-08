package open.it.com.petit.Main.Presenter.Contract;

import java.util.ArrayList;

import open.it.com.petit.Main.Data.Feeder;
import open.it.com.petit.Main.Adapter.Listener.OnItemClickListener;

/**
 * Created by user on 2018-02-08.
 */

public interface FeederAdapterContract {
    interface View {
        void setOnClickListener(OnItemClickListener clickListener);

        void notifyAdapter();
    }

    interface Model {
        void addItems(ArrayList items);

        void clearItems();

        Feeder getItem(int position);

        ArrayList<Feeder> getItems();
    }
}
