package com.example.qrscanner.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class CompareMethod {

    private static DBHelper dbHelper;
    private List<String> differences;
    private static StringBuilder stringBuilder;
    private static TextInputEditText serialNumber;
    private static TextInputEditText name;
    private static TextInputEditText department;
    private static TextInputEditText device;
    private static TextInputEditText deviceModel;
    private static TextInputEditText datePurchased;
    private String keyIdentical;
    private String keyDifferent;
    private String keyNew;
    private String result;
    private static boolean nextAction = false;

    static Dialog customLoading;

    public CompareMethod(DBHelper dbHelper, List<String> differences, StringBuilder stringBuilder, TextInputEditText serialNumber, TextInputEditText name, TextInputEditText department, TextInputEditText device, TextInputEditText deviceModel, TextInputEditText datePurchased, String keyIdentical, String keyDifferent, String keyNew) {
        CompareMethod.dbHelper = dbHelper;
        this.differences = differences;
        CompareMethod.stringBuilder = stringBuilder;
        CompareMethod.serialNumber = serialNumber;
        CompareMethod.name = name;
        CompareMethod.department = department;
        CompareMethod.device = device;
        CompareMethod.deviceModel = deviceModel;
        CompareMethod.datePurchased = datePurchased;
        this.keyIdentical = keyIdentical;
        this.keyDifferent = keyDifferent;
        this.keyNew = keyNew;
    }

    public String compare(Context context, CompareCallBack callback) {
        new compareAsync(context, callback).execute();
        return result;
    }



    public static void overrideItem(Context context, TextInputEditText dateExpired, TextInputEditText status, TextInputEditText availability, Runnable onOKClicked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.override_dialog, null);
        builder.setView(view);

        // Set up the input
        final MaterialTextView input = view.findViewById(R.id.diff);

        input.setText(stringBuilder.toString());
        input.setFocusable(false);
        input.setClickable(false);

        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);

        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();


        // Set up the buttons
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.editDevice(
                        serialNumber.getText().toString(),
                        name.getText().toString(),
                        department.getText().toString(),
                        device.getText().toString(),
                        deviceModel.getText().toString(),
                        datePurchased.getText().toString(),
                        dateExpired.getText().toString(),
                        status.getText().toString(),
                        availability.getText().toString()
                );

                // Reset other fields
                serialNumber.setText(null);
                name.setText(null);
                deviceModel.setText(null);
                datePurchased.setText(null);
                dateExpired.setText(null);
                status.setText(null);
                availability.setText(null);

                alertDialog.dismiss();
                nextAction = true;

                Log.d("CompareMethod", "onClick: next action " + nextAction);

                if (onOKClicked != null) {
                    onOKClicked.run();
                    nextAction = false;
                }
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                customLoading.dismiss();
            }
        });
    }

    public static boolean getNextActionResult() {
        boolean currentNextAction = nextAction;
        // Reset nextAction after returning the value

        return currentNextAction;
    }

    public interface CompareCallBack {
        void onCompareComplete(String result);
    }

    private class compareAsync extends AsyncTask<Void, Integer, String> {

        private Context context;
        private CompareCallBack callBack;

        private compareAsync(Context context, CompareCallBack callBack) {
            this.context = context;
            this.callBack = callBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show loading animation
            customLoading = new Dialog(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.loading_dialog, null);
            ImageView loadingIc = dialogView.findViewById(R.id.loading_icon);
            TextView textView = dialogView.findViewById(R.id.title_textView);
            textView.setText("Comparing.");
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
        protected String doInBackground(Void... voids) {
            try {
                ArrayList<Assigned_to_User_Model> dataList = dbHelper.fetchDevice();
                String inputSerial = serialNumber.getText().toString();
                boolean found = false;
                result = "";
                int prog = 0;
                for (Assigned_to_User_Model items : dataList) {
                    if (inputSerial.equals(items.getSerialNumber())) {
                        found = true;

                        differences.clear();
                        stringBuilder.setLength(0);

                        if (!items.getName().equals(name.getText().toString())) {
                            differences.add("Name: " + items.getName() + " -> " + name.getText().toString() + "\n");
                        }
                        if (items.getDepartment() != null && !items.getDepartment().equals(department.getText().toString())) {
                            differences.add("Department: " + items.getDepartment() + " -> " + department.getText().toString() + "\n");
                        }
                        if (!items.getDeviceType().equals(device.getText().toString())) {
                            differences.add("Device Type: " + items.getDeviceType() + " -> " + device.getText().toString() + "\n");
                        }
                        if (!items.getDeviceBrand().equals(deviceModel.getText().toString())) {
                            differences.add("Device Model: " + items.getDeviceBrand() + " -> " + deviceModel.getText().toString() + "\n");
                        }
                        if (!items.getDatePurchased().equals(datePurchased.getText().toString())) {
                            differences.add("Date Purchased: " + items.getDatePurchased() + " -> " + datePurchased.getText().toString() + "\n");
                        }

                        // Combine differences into the StringBuilder
                        for (String difference : differences) {
                            stringBuilder.append(difference).append(", ");

                        }
                        if (stringBuilder.length() > 0) {
                            stringBuilder.setLength(stringBuilder.length() - 2); // Remove the last comma and space
                        }

                        // Output differences
                        if (differences.isEmpty()) {
                            result = keyIdentical;
//                    Toast.makeText(ScanQR.this, "No differences found.", Toast.LENGTH_SHORT).show();
                        } else {
                            result = keyDifferent;
                            switch (differences.size()) {
                                case 1: for (int i = 0; i <= 100; i++){
                                            prog = i;
                                            Thread.sleep(50);
                                            publishProgress(prog);
                                        }
                                        break;
                                case 2: for (int i = 0; i <= 100; i++){
                                            prog = i;
                                            Thread.sleep(70);
                                            publishProgress(prog);
                                        }
                                        break;
                                case 3: for (int i = 0; i <= 100; i++){
                                            prog = i;
                                            Thread.sleep(100);
                                            publishProgress(prog);
                                        }
                                        break;
                                case 4: for (int i = 0; i <= 100; i++){
                                            prog = i;
                                            Thread.sleep(120);
                                            publishProgress(prog);
                                        }
                                        break;
                                case 5: for (int i = 0; i <= 100; i++){
                                            prog = i;
                                            Thread.sleep(140);
                                            publishProgress(prog);
                                        }
                                        break;
                            }
//                    Toast.makeText(ScanQR.this, "Differences found: " + differences, Toast.LENGTH_SHORT).show();
                        }
                        publishProgress(prog);
                        break;
                    }
                }
                if (!found) {
                    result = keyNew;
//            Toast.makeText(ScanQR.this, "New", Toast.LENGTH_SHORT).show();
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int newProgress = values[0];
            TextView loadingText = customLoading.findViewById(R.id.loading_textView);
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("Proccessing...[" + newProgress + "%]");
        }

        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (customLoading.isShowing()) {
                        customLoading.dismiss();
                    }
                }
            }, 5000);

            if (callBack != null) {
                callBack.onCompareComplete(res);
            }
        }

    }
}
