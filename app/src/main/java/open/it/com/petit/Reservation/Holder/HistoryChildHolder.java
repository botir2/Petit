package open.it.com.petit.Reservation.Holder;

import android.view.View;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import open.it.com.petit.R;

/**
 * Created by user on 2017-07-21.
 */

public class HistoryChildHolder extends ChildViewHolder {
    private TextView child;

    public HistoryChildHolder(View itemView) {
        super(itemView);
        child = (TextView) itemView.findViewById(R.id.tv_feeder_history_child);
    }

    public void setHistoryChild(String childText) {
        child.setText(childText);
    }
}
