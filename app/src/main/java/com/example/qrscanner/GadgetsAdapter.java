package com.example.qrscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrscanner.options.Gadgets;

import java.util.List;

public class GadgetsAdapter extends BaseAdapter {

    private Context context;
    private List<Gadgets> gadgets;

    public GadgetsAdapter(Context context, List<Gadgets> gadgets) {
        this.context = context;
        this.gadgets = gadgets;
    }
    @Override
    public int getCount() {
        return gadgets != null ? gadgets.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return gadgets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rootView = LayoutInflater.from(context).inflate(R.layout.item_gadget_option, parent, false);

        TextView tvGadgetName = rootView.findViewById(R.id.tvGadget);
        ImageView ivGadgetImage = rootView.findViewById(R.id.image);

        tvGadgetName.setText(gadgets.get(position).getGadgetName());
        ivGadgetImage.setImageResource(gadgets.get(position).getImage());

        return rootView;
    }

    public List<Gadgets> getGadgetsList() {
        return gadgets;
    }
}
