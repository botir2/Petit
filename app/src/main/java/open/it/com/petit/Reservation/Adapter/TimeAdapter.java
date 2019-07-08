package open.it.com.petit.Reservation.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

import open.it.com.petit.Reservation.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Reservation.Holder.TimeViewHolder;
import open.it.com.petit.Reservation.Presenter.Contract.TimeAdapterContract;
import open.it.com.petit.Reservation.Data.Time;

/**
 * Created by user on 2018-01-31.
 */

public class TimeAdapter extends RecyclerView.Adapter<TimeViewHolder> implements TimeAdapterContract.Model, TimeAdapterContract.View{
    private static final String TAG = TimeAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Time> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public TimeAdapter(Context context) {
        this.context = context;
    }


    @Override
    public void setOnClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }

    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }

    @Override
    public void addItems(ArrayList items) {
        this.items = items;
    }

    @Override
    public void clearItems() {
        if (items != null)
            items.clear();
    }

    @Override
    public TimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TimeViewHolder(context, parent, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(TimeViewHolder holder, int position) {
        if (holder == null) return;
        holder.onBind(getItem(position), position);
    }

    @Override
    public Time getItem(int position) {
        return items.get(position);
    }

    @Override
    public ArrayList getList() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}
