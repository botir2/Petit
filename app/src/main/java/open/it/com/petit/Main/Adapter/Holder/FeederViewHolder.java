package open.it.com.petit.Main.Adapter.Holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import open.it.com.petit.Main.Adapter.Listener.OnItemClickListener;
import open.it.com.petit.Main.Data.Feeder;
import open.it.com.petit.R;

/**
 * Created by user on 2018-02-08.
 */

public class FeederViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = FeederViewHolder.class.getSimpleName();

    private Context context;
    private OnItemClickListener onItemClickListener;

    @BindView(R.id.tv_pfm_list_view_petname)
    TextView petNameTextView;
    @BindView(R.id.btn_goto_petit_media)
    FrameLayout mediaBtn;
    @BindView(R.id.btn_pfm_reserve)
    LinearLayout reservationBtn;
    @BindView(R.id.btn_pfm_feed_history)
    LinearLayout historyBtn;
    @BindView(R.id.img_feeder_list_media)
    ImageView feederBtn;
    @BindView(R.id.btn_feeder_setting)
    ImageView settingBtn;

    public FeederViewHolder(Context context, ViewGroup parent, OnItemClickListener onItemClickListener) {
        super(LayoutInflater.from(context).inflate(R.layout.petit_feeder_list_view, parent, false));
        this.context = context;
        this.onItemClickListener = onItemClickListener;

        ButterKnife.bind(this, itemView);
    }

    // Adapter와 Holder를 연결시킴.
    public void onBind(final Feeder feeder, final int position) {
        Glide.with(context).load(feeder.getF_IMG()).into(feederBtn); // Glide : 이미지를 서버에서 빠르게 가져오게 해주는 library
        petNameTextView.setText(feeder.getP_NAME()); // petname을 set

        // feeder가 slave 일 때 예약버튼
        if (feeder.getMS() == 0) {
            reservationBtn.setEnabled(false); // 버튼 비활성화
            reservationBtn.setBackground(context.getResources().getDrawable(R.drawable.feeder_list_revbtn_disable)); // 회색으로 변경
        }
        // feeder가 master 일 때 예약버튼
        else {
            reservationBtn.setEnabled(true); // 버튼 활성화
            reservationBtn.setBackground(context.getResources().getDrawable(R.drawable.feeder_list_revbtn_enable)); // 노란색으로 변경
        }

        // media 버튼을 눌렀을 때 event
        mediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onMedia(feeder);
            }
        });

        // reservation 버튼을 눌렀을 때 event
        reservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onReservation(feeder);
            }
        });

        // history 버튼을 눌렀을 때 event
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onHistory(feeder);
            }
        });

        // setting 버튼을 눌렀을 때 event
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onSetting(feeder);
            }
        });
    }
}
