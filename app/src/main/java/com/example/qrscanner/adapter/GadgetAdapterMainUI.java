package com.example.qrscanner.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.activities.ExpiredDevicesActivity;
import com.example.qrscanner.activities.GadgetTypeActivity;
import com.example.qrscanner.activities.UnknownUserActivity;
import com.example.qrscanner.activities.allDevice;
import com.example.qrscanner.models.GadgetsList;

import java.util.List;

public class GadgetAdapterMainUI extends RecyclerView.Adapter<GadgetAdapterMainUI.ViewHolder> {

    private int itemLayout_id;
    private Context context;

    private DBHelper dbHelper;

    GadgetsList data;
    private List<GadgetsList> gadgetsLists;

    public GadgetAdapterMainUI(int itemLayout_id, Context context, DBHelper dbHelper, List<GadgetsList> gadgetsLists) {
        this.itemLayout_id = itemLayout_id;
        this.context = context;
        this.dbHelper = dbHelper;
        this.gadgetsLists = gadgetsLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(itemLayout_id, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        data = gadgetsLists.get(position);
//        int newPos = data.getImage();
//        byte[] imageByte = dbHelper.getGadgetCategoryImageInt(position);
        byte[] imageByte = data.getImage();
        if (imageByte != null){
            // Convert byte array to Bitmap/int
//            byte[] imageBytes = dbHelper.getGadgetCategoryImageInt(po);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            Log.d("ItemAdapter", "onBindViewHolder: Null Image");
        }

        holder.textView.setText(data.getGadgetCategoryName());
    }

    @Override
    public int getItemCount() {
        return gadgetsLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewMainUI);
            textView = itemView.findViewById(R.id.gadgetTypeName);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("GadgetsAdapter", "onClick: Item clicked");
            int pos = getAdapterPosition();
            GadgetsList data = gadgetsLists.get(pos);
            String categoryName = data.getGadgetCategoryName();
            Log.d("TAG", "onClick: " + categoryName);

            switch (categoryName) {
                case "All Device": {
                    Intent intent1 = new Intent(context, allDevice.class);
                    context.startActivity(intent1);
                    break;
                }
                case "Unknown User": {
                    Intent intent1 = new Intent(context, UnknownUserActivity.class);
                    context.startActivity(intent1);
                    break;
                }
                case "Expired Device": {
                    Intent intent1 = new Intent(context, ExpiredDevicesActivity.class);
                    context.startActivity(intent1);
                    break;
                }
                default: {
                    Intent intent = new Intent(context, GadgetTypeActivity.class);

                    if (categoryName.equals(data.getGadgetCategoryName())) {
                        intent.putExtra("deviceType", data.getGadgetCategoryName());
                        byte[] byteArray = data.getImage();
                        intent.putExtra("img", byteArray);
                        Log.d("GadgetsAdapter", "onClick: " + categoryName.equals(gadgetsLists.get(pos).toString()) + intent.getStringExtra("deviceType"));

                        if (context instanceof Activity) {
                            ((Activity) context).setResult(Activity.RESULT_OK, intent);
                        }
                        context.startActivity(intent);
                        Log.d("GadgetsAdapter", "onClick: " + categoryName.equals(gadgetsLists.get(pos).toString()) + intent.getStringExtra("deviceType"));
                        Log.d("GadgetsAdapter", "onClick: last " + categoryName);

                    }
                }
            }

        }
    }
}
