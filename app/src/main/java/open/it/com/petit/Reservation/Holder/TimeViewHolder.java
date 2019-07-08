package open.it.com.petit.Reservation.Holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import open.it.com.petit.R;
import open.it.com.petit.Reservation.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Reservation.Data.Time;

/**
 * Created by user on 2018-02-02.
 */

public class TimeViewHolder extends RecyclerView.ViewHolder{
    private static final String TAG = TimeViewHolder.class.getSimpleName();

    private Context context;
    private OnItemClickListener onItemClickListener;

    @BindView(R.id.tv_pfm_reserve_amORpm)
    TextView tvMeridiem;
    @BindView(R.id.tv_pfm_reserve_time)
    TextView tvTime;
    @BindView(R.id.tv_pfm_reserve_provisions)
    TextView tvProvisions;
    @BindView(R.id.btn_pfm_reserve_time_del)
    ImageView btnTimeDelete;

    public TimeViewHolder(Context context, ViewGroup parent, OnItemClickListener onItemClickListener) {
        super(LayoutInflater.from(context).inflate(R.layout.petit_reserve_timedata, parent, false));
        this.context = context;
        this.onItemClickListener = onItemClickListener;

        ButterKnife.bind(this, itemView);
    }

    public void onBind(Time item, final int position) {

        int meridiem = item.getTime();
        tvProvisions.setText(String.valueOf(item.getMount()));

        if (meridiem <= 12) {
            tvTime.setText(String.valueOf(meridiem));
            tvMeridiem.setText("오전");
        } else {
            meridiem -= 12;
            tvTime.setText(String.valueOf(meridiem));
            tvMeridiem.setText("오후");
        }

        btnTimeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onRemove(position);
                }
            }
        });
    }
}
