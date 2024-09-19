package com.example.qrscanner.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.models.Assigned_to_User_Model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ExportDateBaseToExcel extends AsyncTask<Uri, Integer, Boolean> {

    private Uri selectedFileUri;

    private String fileName;

    private Context context;
    private Dialog customLoading;

    private ConstraintLayout main;
    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage;
    private TextView topSnackDesc;

    public ExportDateBaseToExcel(Context context, ConstraintLayout main, View topSnackView, ImageView topSnack_icon, TextView topSnackMessage, TextView topSnackDesc) {
        this.context = context;
        this.main = main;
        this.topSnackView = topSnackView;
        this.topSnack_icon = topSnack_icon;
        this.topSnackMessage = topSnackMessage;
        this.topSnackDesc = topSnackDesc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Show loading animation
        customLoading = new Dialog(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        ImageView loadingIc = dialogView.findViewById(R.id.loading_icon);
        TextView textView = dialogView.findViewById(R.id.loading_textView);
        textView.setText("Exporting Data.");
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
    protected Boolean doInBackground(Uri... uris) {
        selectedFileUri = uris[0];
        boolean success = false;

        try {
            DBHelper dbHelper = new DBHelper(context);

            Log.d("MainActivity", "Storage permission granted");
            // Fetch data from the database
            ArrayList<Assigned_to_User_Model> deviceList = dbHelper.fetchDevice();
            Log.d("MainActivity", "Fetched " + deviceList.size() + " records from the database");

            // Create an Excel workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Devices");
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
            wrapStyle.setWrapText(true);

            // Arrays to store the calculated widths
            int[] cellLengthHeaders = new int[9];
            int[] cellLengthContents = new int[9];

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Serial Number / Service Tag \n IMEI no. / Sim Number");
//                headerRow.getCell(0).getCellStyle().setAlignment(HorizontalAlignment.CENTER);
            headerRow.createCell(1).setCellValue("Assigned To / User \n Name");
            headerRow.createCell(2).setCellValue("Department");
            headerRow.createCell(3).setCellValue("Device Type \n / \n Asset Description");
            headerRow.createCell(4).setCellValue("Device Model \n / \n Asset Type");
            headerRow.createCell(5).setCellValue("Date Purchased \n / \n Ship Date");
            headerRow.createCell(6).setCellValue("Date Expired");
            headerRow.createCell(7).setCellValue("Status");
            headerRow.createCell(8).setCellValue("Availability");

            // Calculate header row widths
            for (int i = 0; i <= 8; i++) {
                Cell cell = headerRow.getCell(i);
                int length = 0;

                // Check cell type to avoid exceptions
                if (cell.getCellType() == CellType.STRING) {
                    length = cell.getStringCellValue().length() + 2;
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    length = String.valueOf(cell.getNumericCellValue()).length() + 2;
                }

                cellLengthHeaders[i] = length * 256;
                Log.d("TAG", "Header column " + i + " length: " + cellLengthHeaders[i]);
                cell.setCellStyle(wrapStyle);
                headerRow.getCell(i).getCellStyle().setAlignment(HorizontalAlignment.CENTER);
                headerRow.getCell(i).getCellStyle().setVerticalAlignment(VerticalAlignment.CENTER);
            }

            // Fill data rows and calculate content widths
            int rowNum = 1;
            for (Assigned_to_User_Model device : deviceList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(device.getSerialNumber());
                row.createCell(1).setCellValue(device.getUserName());
                row.createCell(2).setCellValue(device.getDepartment());
                row.createCell(3).setCellValue(device.getDeviceType());
                row.createCell(4).setCellValue(device.getDeviceBrand());
                row.createCell(5).setCellValue(device.getDatePurchased());
                row.createCell(6).setCellValue(device.getDateExpired());
                Log.d("MainActivity", "run: date expired " + row.getCell(6).getStringCellValue());
                row.createCell(7).setCellValue(device.getStatus());
                Log.d("MainActivity", "run: status " + row.getCell(7).getStringCellValue());
                row.createCell(8).setCellValue(device.getAvailability());
                Log.d("MainActivity", "Added row " + rowNum + " to the sheet");
                publishProgress(rowNum);

                for (int i = 0; i <= 8; i++) {
                    Cell cell = row.getCell(i);
                    int length = 0;

                    // Check cell type to avoid exceptions
                    if (cell.getCellType() == CellType.STRING) {
                        length = cell.getStringCellValue().length() + 2;
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        length = String.valueOf(cell.getNumericCellValue()).length() + 2;
                    }

                    cellLengthContents[i] = Math.max(cellLengthContents[i], length * 256);
//                        Log.d("TAG", "Content column " + i + " length: " + cellLengthContents[i]);
                }
            }

            // Set the column width to the maximum of header and content width
            for (int i = 0; i <= 8; i++) {
                int columnWidth = Math.max(cellLengthHeaders[i], cellLengthContents[i]);
                sheet.setColumnWidth(i, columnWidth);
                Log.d("TAG", "Final column " + i + " width: " + columnWidth);
            }

            try {
                if (selectedFileUri != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(selectedFileUri);
                    workbook.write(outputStream);
                    outputStream.close();
                    Log.d("ExportDatabaseToExcel", "Excel file saved to: " + selectedFileUri.getPath());

                    success = true;
                }
            } catch (IOException e) {
                Log.e("ExportDatabaseToExcel", "Error exporting database to Excel", e);
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return success; // Indicate successful completion
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int currentItemCount = values[0];
        TextView loadingText = customLoading.findViewById(R.id.loading_textView);
        loadingText.setText("Processing... " + currentItemCount + " items added to file.");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Log.d("ExportDatabaseToExcel", "onPostExecute triggered. Result: " + result); // Debug log to track execution
        // Dismiss loading animation
        if (customLoading.isShowing()) {
            customLoading.dismiss();
        }

        if (result) {
            topSnack_icon.setImageResource(R.drawable.check);
            topSnackMessage.setText("Excel file saved to: " + selectedFileUri.getPath());
            TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
        } else {
            topSnack_icon.setImageResource(R.drawable.warning_sign);
            topSnackMessage.setText("Failed to export data");
            TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
        }
    }
}