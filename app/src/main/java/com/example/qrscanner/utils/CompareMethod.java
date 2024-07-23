package com.example.qrscanner.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.ScanQR;
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

    public String compare() {
        ArrayList<Assigned_to_User_Model> dataList = dbHelper.fetchDevice();
        String inputSerial = serialNumber.getText().toString();
        boolean found = false;
        result = "";
        for (Assigned_to_User_Model items : dataList){
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
//                    Toast.makeText(ScanQR.this, "Differences found: " + differences, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        if (!found) {
            result = keyNew;
//            Toast.makeText(ScanQR.this, "New", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public StringBuilder getStringBuilderResult() {
        return stringBuilder;
    }

    public String getKeyResult() {
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
                }
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public static boolean getNextActionRes() {
        boolean currentNextAction = nextAction;
        // Reset nextAction after returning the value
        nextAction = false;
        return currentNextAction;
    }
}
