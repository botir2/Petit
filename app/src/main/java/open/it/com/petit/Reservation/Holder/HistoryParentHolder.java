package open.it.com.petit.Reservation.Holder;

import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import open.it.com.petit.R;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by user on 2017-07-21.
 */

public class HistoryParentHolder extends GroupViewHolder {
    private TextView history;
    private ImageView arrow;

    public HistoryParentHolder(View itemView) {
        super(itemView);
        history = (TextView) itemView.findViewById(R.id.tv_pfm_feed_history);
        arrow = (ImageView) itemView.findViewById(R.id.btn_pfm_feed_history);
    }

    public void setHistoryTitle(String historyText) {
        history.setText(historyText);
    }

    @Override
    public void expand() {
        Log.d("tag", "expand");
        //animateExpand();
        arrow.setImageResource(R.drawable.open_list);
    }

    @Override
    public void collapse() {
        Log.d("tag", "collapse");
        //animateCollapse();
        arrow.setImageResource(R.drawable.close_list02);
    }
}
