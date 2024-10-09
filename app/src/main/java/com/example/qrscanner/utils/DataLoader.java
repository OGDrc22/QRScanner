package com.example.qrscanner.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
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

public class DataLoader extends AsyncTask<Void, Integer, Boolean> {

    private Context context;
    private DBHelper dbHelper;
    private ItemAdapter itemAdapter;
    private ArrayList<ItemModel> deviceList;
    private ArrayList<ItemModel> filteredList;

    private Dialog customLoading;

    private TextView textViewItemCount;

    private int currentCount;
    private int delay;
    public LinearLayout main;

    private AfterAsyncListener listener;

    private boolean isEmpty = true;

    private static int delayBeforeClosing = 1000;

    public static View topSnackView;
    public static ImageView topSnack_icon;
    public static TextView topSnackMessage;
    public static TextView topSnackDesc;

    public DataLoader(Context context, LinearLayout main, DBHelper dbHelper, ItemAdapter itemAdapter, TextView textViewItemCount, ArrayList<ItemModel> deviceList) {
        this.main = main;
        this.dbHelper = dbHelper;
        this.context = context;
        this.itemAdapter = itemAdapter;
        this.textViewItemCount = textViewItemCount;
        this.deviceList = deviceList;
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
        titleText.setText("Fetching All Data.");
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
        int prog = 0;
        Log.d("myLoader", "doInBackground: called");
        try {
            deviceList.clear();
            deviceList.addAll(dbHelper.fetchDevice());

            Collections.reverse(deviceList);

            if (deviceList.isEmpty()) {
                return false;
            }

            filteredList = new ArrayList<>();
            filteredList.clear();
//            Log.d("TAG", "doInBackground: filteredList count " + filteredList.size());

            if (filteredList.isEmpty()) {
                for (ItemModel device : deviceList) {
                    filteredList.add(device);
                    itemAdapter.setDeviceList(filteredList);
                    prog += 1;
                    publishProgress(prog);
                    Thread.sleep(1);
                }
            }

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Update UI with current itemCount
        int currentItemCount = values[0];
        TextView loadingText = customLoading.findViewById(R.id.loading_textView);
        loadingText.setText("Processing... [" + currentItemCount + "] items processed");
//        Log.d("TAG", "onProgressUpdate: " + filteredList.size());
        int cl = ContextCompat.getColor(context, R.color.primary);
        int cl1 = ContextCompat.getColor(context, R.color.txtHeaderLight);
        if (currentItemCount == filteredList.size()) {
            loadingText.setText("[" + currentItemCount + "] items processed");
            loadingText.setTextColor(cl);
        } else {
            loadingText.setTextColor(cl1);
        }
//        itemAdapter.notifyDataSetChanged();
    }

    public void setOnAfterAsync(AfterAsyncListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Dismiss loading animation
                if (customLoading.isShowing()) {
                    customLoading.dismiss();
                }

                if (result) {
                    itemAdapter.notifyDataSetChanged();
                }

                if (!result) {
                    getView(context);
                    topSnack_icon.setImageResource(R.drawable.warning_sign);
                    topSnackMessage.setText("No data");
                    topSnackDesc.setVisibility(View.VISIBLE);
                    topSnackDesc.setText("Scan New Device or Import Data");
                    TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
                }
                delay = currentCount + delayBeforeClosing;
                if (listener != null) {
                    listener.after(delay);
                }
            }
        }, delayBeforeClosing);

    }

    public ArrayList<ItemModel> getFilteredList() {
        return filteredList;
    }
}
