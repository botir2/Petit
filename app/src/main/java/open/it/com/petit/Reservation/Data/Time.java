package open.it.com.petit.Reservation.Data;

import android.support.annotation.NonNull;

/**
 * Created by user on 2018-02-02.
 */

public class Time implements Comparable<Time> {
    private int time;
    private int mount;

    public Time(int time, int mount) {
        this.time = time;
        this.mount = mount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getMount() {
        return mount;
    }

    public void setMount(int mount) {
        this.mount = mount;
    }

    @Override
    public int compareTo(@NonNull Time items) {
        return String.valueOf(time).compareTo(String.valueOf(items.getTime()));
    }
}
