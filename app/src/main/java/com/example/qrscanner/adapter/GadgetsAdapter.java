package com.example.qrscanner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrscanner.R;
import com.example.qrscanner.models.Gadgets;

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
        convertView = LayoutInflater.from(context).inflate(R.layout.item_gadget_option, parent, false);

        TextView tvGadgetName = convertView.findViewById(R.id.name);
        ImageView ivGadgetImage = convertView.findViewById(R.id.image);

        Gadgets gadget = gadgets.get(position);

        //Convert byte array to Bitmap/int
        tvGadgetName.setText(gadget.getGadgetCategoryName());
        byte[] imageBytes = gadget.getImage();

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ivGadgetImage.setImageBitmap(bitmap);
        } //else {
//            ivGadgetImage.setImageResource(R.drawable.device_model); // Use a default image if no image is provided
        //}

        return convertView;
    }

    public List<Gadgets> getGadgetsList() {
        return gadgets;
    }
}