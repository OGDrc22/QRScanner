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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.ItemModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class FilteredDataLoader {

    private static int sleepTime = 1;
    private static int delayBeforeClosing = 1000;

    public static View topSnackView;
    public static ImageView topSnack_icon;
    public static TextView topSnackMessage;
    public static TextView topSnackDesc;

    public static void getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);
    }


    public static class UnknownUserFilteredDataLoader extends AsyncTask<Void, Integer, Boolean> {

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

        public UnknownUserFilteredDataLoader(Context context, DBHelper dbHelper, View main, ItemAdapter itemAdapter, ArrayList<ItemModel> deviceList, TextView textViewItemCount) {
            this.context = context;
            this.dbHelper = dbHelper;
            this.main = (LinearLayout) main;
            this.itemAdapter = itemAdapter;
            this.deviceList = deviceList;
            this.textViewItemCount = textViewItemCount;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading animation
            customLoading = new Dialog(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.loading_dialog, null);
            TextView titleText = dialogView.findViewById(R.id.title_textView);
            titleText.setText("Fetching Data.");
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
                Log.d("TAG", "doInBackground: filteredList count " + filteredList.size());

                if (filteredList.isEmpty()) {
                    boolean hasNoUser = true;
                    // Iterate through the original list and add items that match the criteria to the filtered list
                    for (ItemModel device : deviceList) {
                        // Assuming device contains the name of the gadget
                        String userName = device.getUserName();
                        Log.d("TAG", "userName: " + userName);
                        if (device.getUserName().isEmpty()) {
                            filteredList.add(device);
                            itemAdapter.setDeviceList(filteredList);
                            Log.d("TAG", "userName is empty: " + userName);
                            prog += 1;
                            publishProgress(prog);
                            Thread.sleep(sleepTime);
                            hasNoUser = false;
                            isEmpty = false;
                        }
                    }

                    if (hasNoUser) {
                        deviceList.clear();
                        isEmpty = true;
                        return  false;
                    }

                    String deviceCount = String.valueOf(filteredList.size());
                    textViewItemCount.setText("Item Count: " + deviceCount);

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
            Log.d("TAG", "onProgressUpdate: " + filteredList.size());
            int cl = ContextCompat.getColor(context, R.color.primary);
            int cl1 = ContextCompat.getColor(context, R.color.txtHeaderLight);
            if (currentItemCount == filteredList.size()) {
                loadingText.setText("[" + currentItemCount + "] items processed");
                loadingText.setTextColor(cl);
            } else {
                loadingText.setTextColor(cl1);
            }
            itemAdapter.notifyDataSetChanged();
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

                    if (!result) {
                        getView(context);
                        topSnack_icon.setImageResource(R.drawable.warning_sign);
                        topSnackMessage.setText("No data in NoUser");
                        TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
                    }
                    delay = currentCount + delayBeforeClosing;
                    if (listener != null) {
                        listener.after(delay);
                    }
                }
            }, delayBeforeClosing);

        }

        public Boolean isEmpty() {
            return isEmpty;
        }

        public ArrayList<ItemModel> getFilteredList() {
            return filteredList;
        }

    }







    public static class ExpiredDevice extends AsyncTask<Void, Integer, Boolean> {

        private Context context;
        private DBHelper dbHelper;
        private ItemAdapter itemAdapter;
        private ArrayList<ItemModel> deviceList;
        private ArrayList<ItemModel> filteredList;

        private Dialog customLoading;

        private TextView textViewItemCount;

        private LinearLayout main;

        private int currentCount;
        private int delay;

        private AfterAsyncListener listener;

        private boolean isEmpty = true;

        public ExpiredDevice(Context context, DBHelper dbHelper, View main, ItemAdapter itemAdapter, ArrayList<ItemModel> deviceList, TextView textViewItemCount) {
            this.context = context;
            this.dbHelper = dbHelper;
            this.main = (LinearLayout) main;
            this.itemAdapter = itemAdapter;
            this.deviceList = deviceList;
            this.textViewItemCount = textViewItemCount;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading animation
            customLoading = new Dialog(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.loading_dialog, null);
            TextView titleText = dialogView.findViewById(R.id.title_textView);
            titleText.setText("Fetching Data.");
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
//            filterDeviceList();

                Collections.reverse(deviceList);

                filteredList = new ArrayList<>();
                filteredList.clear();
                if (deviceList.isEmpty()) {
                    // Show a toast message or handle empty list case
                } else {
                    boolean noExpiredDevices = true;
                    // Iterate through the original list and add items that match the criteria to the filtered list
                    for (ItemModel device : deviceList) {
                        String input = device.getDatePurchased();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
                        Date dateInput = dateFormat.parse(input);
                        if (Utils.calculateExpiration(dateInput,  "For Refresh")) {
                            filteredList.add(device);
                            itemAdapter.setDeviceList(filteredList);
                            prog += 1;
                            publishProgress(prog);
                            Thread.sleep(sleepTime);
                            noExpiredDevices = false;
                            isEmpty = false;
                        }
                    }

                    if (noExpiredDevices) {
                        deviceList.clear();
                        isEmpty = true;
                        return false;
                    }


                    String deviceCount = String.valueOf(filteredList.size());
                    textViewItemCount.setText("Item Count: " + deviceCount);

//            if (filteredList.isEmpty()) {
//                textViewNoData.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                textViewNoData.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//            }

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
            Log.d("TAG", "onProgressUpdate: " + filteredList.size());
            int cl = ContextCompat.getColor(context, R.color.primary);
            int cl1 = ContextCompat.getColor(context, R.color.txtHeaderLight);
            if (currentItemCount == filteredList.size()) {
                loadingText.setText("[" + currentItemCount + "] items processed");
                loadingText.setTextColor(cl);
            } else {
                loadingText.setTextColor(cl1);
            }
            itemAdapter.notifyDataSetChanged();
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

                    if (!result) {
                        getView(context);
                        topSnack_icon.setImageResource(R.drawable.warning_sign);
                        topSnackMessage.setText("No data in here [Expired Devices]");
                        TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
                    }

                    delay = currentCount + delayBeforeClosing;
                    if (listener != null) {
                        listener.after(delay);
                    }
                }
            }, delayBeforeClosing);

        }

        public Boolean isEmpty() {
            return isEmpty;
        }

        public ArrayList<ItemModel> getFilteredList() {
            return filteredList;
        }

    }







    public static class DeviceFilteredDataLoader extends AsyncTask<Void, Integer, Boolean> {

        private Context context;
        private DBHelper dbHelper;
        private String filterKey;
        private ItemAdapter itemAdapter;
        private ArrayList<ItemModel> deviceList;
        private ArrayList<ItemModel> filteredList;

        private Dialog customLoading;

        private TextView textViewItemCount;

        private int currentCount;
        private int delay;

        private AfterAsyncListener listener;
        private static LinearLayout main;

        private boolean isEmpty = true;


        public DeviceFilteredDataLoader(Context context, DBHelper dbHelper, View main, ItemAdapter itemAdapter, @NonNull String filterKey, ArrayList<ItemModel> deviceList, TextView textViewItemCount) {
            this.context = context;
            this.filterKey = filterKey;
            this.dbHelper = dbHelper;
            DeviceFilteredDataLoader.main = (LinearLayout) main;
            this.itemAdapter = itemAdapter;
            this.deviceList = deviceList;
            this.textViewItemCount = textViewItemCount;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading animation
            customLoading = new Dialog(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.loading_dialog, null);
            TextView titleText = dialogView.findViewById(R.id.title_textView);
            titleText.setText("Fetching Data.");
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
                if (deviceList.isEmpty()) {
                    // Show a toast message or handle empty list case
                } else {
                    boolean noFilterKey = true;
                    // Iterate through the original list and add items that match the criteria to the filtered list
                    for (ItemModel device : deviceList) {
                        // Assuming device contains the name of the gadget
                        String deviceName = device.getDeviceType();
                        // Check if the device name contains "laptop" (case-insensitive)
                        if (deviceName.toLowerCase().contains(filterKey)) {
                            filteredList.add(device);
                            itemAdapter.setDeviceList(filteredList);
                            prog += 1;
                            publishProgress(prog);
                            Thread.sleep(sleepTime);
                            noFilterKey = false;
                            isEmpty = false;
                        } else if (!deviceName.contains(filterKey)){
                            filteredList.remove(device);
                        }
                    }

                    if (noFilterKey) {
                        deviceList.clear();
                        isEmpty = true;
                        return false;
                    }

                    String deviceCount = String.valueOf(filteredList.size());
                    textViewItemCount.setText("Item Count: " + deviceCount);

//            if (filteredList.isEmpty()) {
//                textViewNoData.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                textViewNoData.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//            }

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
            Log.d("TAG", "onProgressUpdate: " + filteredList.size());
            int cl = ContextCompat.getColor(context, R.color.primary);
            int cl1 = ContextCompat.getColor(context, R.color.txtHeaderLight);
            if (currentItemCount == filteredList.size()) {
                loadingText.setText("[" + currentItemCount + "] items added.");
                loadingText.setTextColor(cl);
            } else {
                loadingText.setTextColor(cl1);
            }
            itemAdapter.notifyDataSetChanged();
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

                    if (!result) {
                        getView(context);
                        topSnack_icon.setImageResource(R.drawable.warning_sign);
                        String activityName = filterKey.substring(0, 1).toUpperCase() + filterKey.substring(1);// Capitalize the first letter of the word
                        topSnackMessage.setText("No data in here [" + activityName + "]");
                        TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
                    }

                    delay = currentCount + delayBeforeClosing;
                    if (listener != null) {
                        listener.after(delay);
                    }
                }
            }, delayBeforeClosing);

        }

        public Boolean isEmpty() {
            return isEmpty;
        }

        public ArrayList<ItemModel> getFilteredList() {
            return filteredList;
        }

    }


}
