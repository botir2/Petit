package open.it.com.petit.Main.Adapter.Listener;

import open.it.com.petit.Main.Data.Feeder;

/**
 * Created by user on 2018-02-08.
 */

public interface OnItemClickListener {
    void onMedia(Feeder feeder);

    void onReservation(Feeder feeder);

    void onHistory(Feeder feeder);

    void onSetting(Feeder feeder);
}
