package open.it.com.petit.NotUse;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import open.it.com.petit.Reservation.Data.SpinItem;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-11.
 */

public class FeederReserveSpnAdapter extends ArrayAdapter<SpinItem> {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<SpinItem> item;

    public FeederReserveSpnAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<SpinItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.item = objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = inflater.inflate(R.layout.test, null);
        TextView tv = (TextView)v.findViewById(R.id.texttv);
        ImageView iv = (ImageView)v.findViewById(R.id.textimg);
        tv.setText(item.get(position).getText());
        iv.setImageResource(item.get(position).getImageId());
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = inflater.inflate(R.layout.test2, null);
        TextView tv = (TextView)v.findViewById(R.id.texttv2);
        tv.setText(item.get(position).getText());

        return v;
    }
}
