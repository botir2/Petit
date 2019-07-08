package open.it.com.petit.Reservation.Data;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by user on 2017-07-21.
 */

// Expandable parent
public class RHParent extends ExpandableGroup<RHChild>{
    private int icon;

    public RHParent(String title, List<RHChild> items, int icon) {
        super(title, items);
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
