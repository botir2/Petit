package open.it.com.petit.Media.Event;

import android.media.projection.MediaProjectionManager;

/**
 * Created by user on 2018-01-15.
 */

public class ProjectionEvent {
    private MediaProjectionManager mpManager;
    private int act;

    public ProjectionEvent(MediaProjectionManager mpManager, int act) {
        this.mpManager = mpManager;
        this.act = act;
    }

    public MediaProjectionManager getMpManager() {
        return mpManager;
    }

    public int getAct() {
        return act;
    }
}
