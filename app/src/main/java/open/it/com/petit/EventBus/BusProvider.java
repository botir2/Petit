package open.it.com.petit.EventBus;

import com.squareup.otto.Bus;

/**
 * Created by user on 2018-01-15.
 */

public class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
