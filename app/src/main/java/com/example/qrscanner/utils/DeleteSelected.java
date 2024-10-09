package com.example.qrscanner.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.ItemModel;

import java.util.ArrayList;
import java.util.Collections;

public class DeleteSelected extends AsyncTask<Void, Integer, Boolean> {

    private Context context;
    private Dialog customLoading;

    private ItemAdapter itemAdapter;
    private ArrayList<ItemModel> deviceList;

    public View main;

    public static View topSnackView;
    public static ImageView topSnack_icon;
    public static TextView topSnackMessage;
    public static TextView topSnackDesc;

    private ArrayList<ItemModel> itemToRemove = new ArrayList<>();


//    public DeleteSelected(Context context, ItemAdapter itemAdapter, ArrayList<ItemModel> deviceList, View main) {
//        this.context = context;
//        this.itemAdapter = itemAdapter;
//        this.deviceList = deviceList;
//        this.main = main;
//    }
    public DeleteSelected(Context context, ItemAdapter itemAdapter, View main) {
        this.context = context;
        this.itemAdapter = itemAdapter;
        this.main = main;
    }

    public static void getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Show loading animation
        customLoading = new Dialog(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        TextView titleText = dialogView.findViewById(R.id.title_textView);
        titleText.setText("Deleting");
        ImageView loadingIc = dialogView.findViewById(R.id.loading_icon);
        Utils.CustomFpsInterpolator fpsInterpolator = new Utils.CustomFpsInterpolator(16);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(loadingIc, "rotation", 0, 360);
        objectAnimator.setDuration(500);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setInterpolator(fpsInterpolator);
        objectAnimator.start();
        customLoading.setContentView(dialogView);
        customLoading.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = customLoading.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        customLoading.setCancelable(false);
        customLoading.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {
            DBHelper dbHelper = new DBHelper(context);
            deviceList = dbHelper.fetchDevice();
            ArrayList<ItemModel> selectedItems = itemAdapter.getSelectedItems();
//            ArrayList<ItemModel> itemToRemove = new ArrayList<>();
            int c = itemAdapter.getSelectedCount();
            Log.d("DeleteSelected", "doInBackground: count to delete: " + c);

            for (ItemModel item : selectedItems) {
                itemToRemove.add(item);
                dbHelper.deleteDeviceBySerialNumber(item.getSerialNumber());
                Log.d("TAG", "doInBackground: Deleting: " + item.getSerialNumber());
                Thread.sleep(1000);
            }

            Log.d("DeleteSelected", "doInBackground: count to delete after: " + itemToRemove.size());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        // Dismiss loading animation
        if (customLoading.isShowing()) {
            customLoading.dismiss();
        }

        if (result) {

            deviceList.removeAll(itemToRemove);

            Collections.reverse(deviceList);

            itemAdapter.setDeviceList(deviceList);

            itemAdapter.clearSelection();
            itemAdapter.updateSelectionCount();
            itemAdapter.refreshSelectionOption();
            itemAdapter.notifyDataSetChanged();

            getView(context);
            topSnack_icon.setImageResource(R.drawable.trash_can_10416);
            topSnack_icon.setColorFilter(ContextCompat.getColor(context, R.color.clDelete));
            topSnackMessage.setText("Deleted " + itemToRemove.size() + " items");
            topSnackDesc.setVisibility(View.VISIBLE);
            topSnackDesc.setText("Selected Items Deleted Successfully");
            TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
        }
    }
}
