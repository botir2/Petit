package open.it.com.petit.Main.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

import open.it.com.petit.Main.Data.Feeder;
import open.it.com.petit.Main.Adapter.Holder.FeederViewHolder;
import open.it.com.petit.Main.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Main.Presenter.Contract.FeederAdapterContract;

/**
 * Created by user on 2018-02-08.
 */

public class FeederAdapter extends RecyclerView.Adapter<FeederViewHolder>
        implements FeederAdapterContract.Model, FeederAdapterContract.View {
    private static final String TAG = FeederAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Feeder> items = new ArrayList(); // Feeder list를 위한 객체
    private OnItemClickListener onItemClickListener; // list data 클릭 리스너

    public FeederAdapter(Context context) {
        this.context = context;
    }

    @Override
    public void setOnClickListener(OnItemClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }

    // recycler view에 변경사항을 알림.
    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }

    // item을 추가함
    @Override
    public void addItems(ArrayList items) {
        this.items = items;
    }

    // item 초기화
    @Override
    public void clearItems() {
        if (items != null)
            items.clear();
    }

    // Holder를 만듦. Holder는 data를 binding 해주는 클래스.
    @Override
    public FeederViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FeederViewHolder(context, parent, onItemClickListener);
    }

    // Holder를 binding 해줌.
    @Override
    public void onBindViewHolder(FeederViewHolder holder, int position) {
        if (holder == null) return;
        holder.onBind(getItem(position), position);
    }

    // list의 개수를 return
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // list를 return
    @Override
    public ArrayList getItems() {
        return items;
    }

    // item를 return
    @Override
    public Feeder getItem(int position) {
        return items.get(position);
    }
}
