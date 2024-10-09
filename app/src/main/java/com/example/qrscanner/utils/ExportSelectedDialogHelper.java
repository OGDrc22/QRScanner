package com.example.qrscanner.utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrscanner.R;
import com.example.qrscanner.adapter.ItemAdapter;

public class ExportSelectedDialogHelper {

    private Activity activity;
    private String fileNameToExport;
    private static final int CREATE_FILE_REQUEST_CODE_SELECTED = 16169;
    private static final int STORAGE_PERMISSION_CODE = 22;

    private Context context;

    private Uri selectedFileUri;

    private ItemAdapter adapter;
    private View main;
    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage, topSnackDesc;



    public ExportSelectedDialogHelper(Context context, Activity activity, ItemAdapter adapter, View main, View topSnackView, ImageView topSnack_icon, TextView topSnackMessage, TextView topSnackDesc) {
        this.context = context;
        this.activity = activity;
        this.adapter = adapter;
        this.main = main;
        this.topSnackView = topSnackView;
        this.topSnack_icon = topSnack_icon;
        this.topSnackMessage = topSnackMessage;
        this.topSnackDesc = topSnackDesc;
    }

    public void promptExportWithFileName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_export_dialog, null);
        builder.setView(view);

        // Set up the input
        final EditText input = view.findViewById(R.id.editText);
        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);

        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        // Set up the buttons
        actionOK.setOnClickListener(v -> {
            String fileName = input.getText().toString();
            if (!fileName.isEmpty()) {
                fileNameToExport = fileName;
                checkStoragePermission();
                createFile();
                // Here you would call your export method or pass necessary arguments
                new ExportSelectedItemToExcel(context, main, adapter, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);
                alertDialog.dismiss();
            } else {
                Toast.makeText(activity, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        actionCancel.setOnClickListener(v -> alertDialog.dismiss());
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, fileNameToExport);
        activity.startActivityForResult(intent, CREATE_FILE_REQUEST_CODE_SELECTED);
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (android.os.Environment.isExternalStorageManager()) {
                // Permission is granted
                return;
            } else {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            // Below Android 11
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }
}
