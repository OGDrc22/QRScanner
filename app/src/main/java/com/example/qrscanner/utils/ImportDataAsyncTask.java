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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ImportDataAsyncTask extends AsyncTask<Uri, Integer, Boolean> {

    private Context context;

    private Dialog customLoading;

    private ConstraintLayout main;
    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage;
    private TextView topSnackDesc;

    private static final String pattern = "MM/dd/yy";

    private int itemCount = 0;
    private static String serialNum;

    public ImportDataAsyncTask(Context context, ConstraintLayout main, View topSnackView, ImageView topSnack_icon, TextView topSnackMessage, TextView topSnackDesc) {
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
        customLoading = new Dialog (context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
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
    protected Boolean doInBackground(Uri... uris) {
        Uri selectedFileUri = uris[0];

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedFileUri);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            DBHelper dbHelper = new DBHelper(context);

            // Identify header row and map headers to column indices
            Row headerRow = sheet.getRow(0); // Assuming first row is the header
            if (headerRow == null) {
                throw new IllegalArgumentException("No Header row found");
            }

            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING) {
                    String header = cell.getStringCellValue().trim().toLowerCase();
                    headerMap.put(header, cell.getColumnIndex());
                }
            }

            // Check for required headers
            if (!containsKeyword(headerMap, "Serial")) {
                throw new IllegalArgumentException("Missing required header: Serial");
            }
            if (!containsKeyword(headerMap, "User") && !containsKeyword(headerMap, "Assigned To") && !containsKeyword(headerMap, "Name")) {
                throw new IllegalArgumentException("Missing required header: Name or User or ");
            }
            if (!containsKeyword(headerMap, "Device") && !containsKeyword(headerMap, "Asset Type")) {
                throw new IllegalArgumentException("Missing required header: Device or Asset Type");
            }
            if (!containsKeyword(headerMap, "Device Model") && !containsKeyword(headerMap, "Asset Description")) {
                throw new IllegalArgumentException("Missing required header: Device Model or Asset Description");
            }
            if (!containsKeyword(headerMap, "Date Purchased") && !containsKeyword(headerMap, "Ship Date")) {
                throw new IllegalArgumentException("Missing required header: Date Purchased or Ship Date");
            }
            if (!containsKeyword(headerMap, "Date Expired") && !containsKeyword(headerMap, "Expiry Date")) {
                throw new IllegalArgumentException("Missing required header: Date Expired or Expiry Date");
            }

            // Iterate over data rows
            Iterator<Row> rowIterator = sheet.rowIterator();
            rowIterator.next(); // Skip header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Make "serialNum" Global
                serialNum = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "serial")));

                String name;
                if (containsKeyword(headerMap, "user")) {
                    name = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "user")));
                } else if (containsKeyword(headerMap, "assigned to")) {
                    name = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "assigned to")));
                } else if (containsKeyword(headerMap, "name")) {
                    name = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "name")));
                } else {
                    name = "";
                }

                String availability;
                if (!name.isEmpty()) {
                    availability = "In Use";
                } else {
                    availability = "In Stock";
                }

                String department;
                if (containsKeyword(headerMap, "department")) {
                    department = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "department")));
                    if (department.isEmpty()) {
                        department = "Unknown";
                    }
                } else {
                    department = "Unknown";
                }

                String deviceType = "";
                if (containsKeyword(headerMap, "device")) {
                    deviceType = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "device")));
                } else if (containsKeyword(headerMap, "type")) {
                    deviceType = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "type")));
                }
                if (deviceType.isEmpty()) {
                    deviceType = "Unknown";
                }

                String deviceModel;
                if (containsKeyword(headerMap, "device model")) {
                    deviceModel = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "device model")));
                } else if (containsKeyword(headerMap, "description")) {
                    deviceModel = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "description")));
                } else {
                    deviceModel = "";
                }

                String datePurchased;
                if (containsKeyword(headerMap, "date purchased")) {
                    datePurchased = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "date purchased")));
                    if (datePurchased.isEmpty()) {
                        datePurchased = "00/00/00";
                    }
                } else if (containsKeyword(headerMap, "ship date")) {
                    datePurchased = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "ship date")));
                    if (datePurchased.isEmpty()) {
                        datePurchased = "00/00/00";
                    }
                } else {
                    datePurchased = "00/00/00";
                }

                String dateExpired;
                String status = "";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
                Date inputDate = simpleDateFormat.parse(datePurchased);
                Utils.ExpirationResult result = Utils.calculateExpirationString(inputDate, "For Refresh");
                if (containsKeyword(headerMap, "date expired")) {
                    dateExpired = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "date expired")));
//                    Log.d("TAG", "doInBackground_p: " + datePurchased);
//                    Log.d("TAG", "doInBackground:_e " + dateExpired);
                    if (dateExpired.isEmpty() && !datePurchased.isEmpty()) {
                        dateExpired = result.getFormattedExpirationDate();
                        status = result.getStringStatus();
//                        Log.d("TAG", "doInBackground_p: " + datePurchased);
//                        Log.d("TAG", "doInBackground_e " + dateExpired);
                    }
                } else if (containsKeyword(headerMap, "expiry date")) {
                    dateExpired = getCellValueAsString(row.getCell(getColumnIndex(headerMap, "expiry date")));
//                    Log.d("TAG", "doInBackground_p: " + datePurchased);
//                    Log.d("TAG", "doInBackground_e: " + dateExpired);
                    if (dateExpired.isEmpty() && !datePurchased.isEmpty()) {
                        dateExpired = result.getFormattedExpirationDate();
                        status = result.getStringStatus();
//                        Log.d("TAG", "doInBackground_p: " + datePurchased);
//                        Log.d("TAG", "doInBackground_e: " + dateExpired + " " + status);
                    }
                } else {
                    dateExpired = "00/00/00";
                }


                // Handle duplicate serial numbers
                if (dbHelper.getAllSerialNumbers().contains(serialNum)) {
                    continue;
                }

                dbHelper.addDevice(serialNum, name, department, deviceType, deviceModel, datePurchased, dateExpired, status, availability);
                Thread.sleep(50);
                itemCount++;
                publishProgress(itemCount);
            }

            return true; // Indicate successful completion

        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        Log.d("TAG", "onProgressUpdate: " + currentItemCount + serialNum);
        loadingText.setText("Processing... " + currentItemCount + " items processed");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // Dismiss loading animation
        if (customLoading.isShowing()) {
            customLoading.dismiss();
        }

        if (result) {
            topSnack_icon.setImageResource(R.drawable.check);
            topSnackMessage.setText("Data imported successfully");
            String item = "Item";
            if (itemCount >= 1){
                item = "Items";
            }
            topSnackDesc.setVisibility(View.VISIBLE);
            topSnackDesc.setText(itemCount + " " + item + " Imported");
            TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
        } else {
            topSnack_icon.setImageResource(R.drawable.warning_sign);
            topSnackMessage.setText("Error importing data");
            TopSnack.createCustomTopSnack(context, main, topSnackView, null, null, true, "up");
        }
    }

    private boolean containsKeyword(Map<String, Integer> headerMap, String keyword) {
        for (String header : headerMap.keySet()) {
            if (header.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int getColumnIndex(Map<String, Integer> headerMap, String keyword) {
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            if (entry.getKey().contains(keyword.toLowerCase())) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Header not found for keyword: " + keyword);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Parse the date from the cell and format it
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                    return sdf.format(date);
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

}