package open.it.com.petit.Reservation.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import open.it.com.petit.Reservation.Holder.HistoryChildHolder;
import open.it.com.petit.Reservation.Holder.HistoryParentHolder;
import open.it.com.petit.Reservation.Data.RHChild;
import open.it.com.petit.Reservation.Data.RHParent;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-21.
 */

// 확장 가능한 RecyclerView
public class ReserveHistoryAdapter
        extends ExpandableRecyclerViewAdapter<HistoryParentHolder, HistoryChildHolder> {

    // history list를 받음.
    public ReserveHistoryAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    // create parent holder
    @Override
    public HistoryParentHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_reserve_history_parent_data, parent, false);
        return new HistoryParentHolder(view);
    }

    // create child holder
    @Override
    public HistoryChildHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_reserve_history_child_data, parent, false);
        return new HistoryChildHolder(view);
    }

    // bind parent holder
    @Override
    public void onBindGroupViewHolder(HistoryParentHolder holder, int flatPosition, ExpandableGroup group) {
        Log.d("tag", group.getTitle());
        holder.setHistoryTitle(group.getTitle());
    }

    // bind child holder
    @Override
    public void onBindChildViewHolder(HistoryChildHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final RHChild child = ((RHParent) group).getItems().get(childIndex);
        Log.d("tag", child.getContent());
        holder.setHistoryChild(child.getContent());
    }
}
