package com.example.qrscanner;


import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.utils.ImportDataAsyncTask;
import com.example.qrscanner.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 16168;
    private CardView addBtn, laptopBtn, tabletBtn, phoneBtn, pcBtn, allBtn, unknownBtn, importBtn, exportBtn, unknownUserBtn, expiredBtn;

    private ImageView settings, currentActivity, backBtn;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ConstraintLayout main, constraintLayout;
    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<String>  serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage, topSnackDesc;

    private DBHelper dbHelper;

    private static final int FILE_REQUEST_CODE = 013;
    private static final int STORAGE_PERMISSION_CODE = 22;

    private String fileNameToExport;
    private Uri selectedFileUri;

    private void applyTheme() {
        SharedPreferences sharedPref = getSharedPreferences("isDarkMode", Context.MODE_PRIVATE);
        boolean isSwitchThemeChecked = sharedPref.getBoolean("isDark", true);

        if (isSwitchThemeChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        applyTheme();

//        checkStoragePermission();

        findViewById(R.id.main).bringToFront();


        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.GONE);

        settings = findViewById(R.id.settingsIcon);
//        mainOption = findViewById(R.id.gridLayoutMainOption);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
                Log.d("TAG", "Clicked Success");
            }
        });


        searchView = findViewById(R.id.search_bar);
        searchView.clearFocus();

        main = findViewById(R.id.main);
        constraintLayout = findViewById(R.id.constraintLayoutOp);

        recyclerView = findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(MainActivity.this.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);


        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            TextView titleTextView = findViewById(R.id.titleTextView);
            ImageView currentActivity = findViewById(R.id.currentActivity);

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    recyclerView.setVisibility(View.VISIBLE);
                    constraintLayout.setVisibility(View.GONE);

                    ImageView backBtn = findViewById(R.id.backBtn);
                    titleTextView.setText("Search");
                    currentActivity.setVisibility(View.GONE);

                    backBtn.setVisibility(View.VISIBLE);
                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchView.clearFocus();
                            recyclerView.setVisibility(View.GONE);
                            constraintLayout.setVisibility(View.VISIBLE);
                        }
                    });


                } else { //  Clear Focus
                    backBtn.setVisibility(View.GONE);
                    titleTextView.setText("QR Scanner");
                }
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });


        // Database
        dbHelper = new DBHelper(this);
        deviceList = new ArrayList<>();

        serialNum_id = new ArrayList<>();
        assignedTo_id = new ArrayList<>();
        department_id = new ArrayList<>();
        device_id = new ArrayList<>();
        deviceModel_id = new ArrayList<>();
        datePurchased_id = new ArrayList<>();
        dateExpire_id = new ArrayList<>();
        status_id = new ArrayList<>();
        availability_id = new ArrayList<>();
        adapter = new ItemAdapter(R.layout.info_layout, this, deviceList, serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id, this::onDeleteClick, this::onEditClick);

        recyclerView.setAdapter(adapter);

        if (recyclerView.getVisibility() == View.GONE) {
            backBtn.setVisibility(View.GONE);
        } else {
            backBtn.setVisibility(View.VISIBLE);
        }
        setUpButtons();
    }

    public void filterList(@NonNull String text) {

        ArrayList<Assigned_to_User_Model> filteredList = new ArrayList<>();

        String searchText = text.toLowerCase();

        ArrayList<Assigned_to_User_Model> dataLists = dbHelper.fetchDevice();
        Collections.reverse(dataLists);

//        for (int i = 0; i < filteredList.size(); i++) {
//            filteredList.get(i).setOriginalPosition(i);
//        }

        // Iterate through the original list and add items that match the search text to the filtered list
        if (searchText.isEmpty()) {
            // If the search query is empty, clear the filtered list
            filteredList.clear();
        } else {
            // Iterate through the original list and add items that match the search text to the filtered list
            for (Assigned_to_User_Model item : dataLists) {
                String serialNum = item.getSerialNumber();
                String serialNumString = String.valueOf(serialNum);
                // Perform case-insensitive search by converting both text and item data to lowercase
                if (serialNumString.toLowerCase().contains(searchText)
                        || item.getName().toLowerCase().contains(searchText) || item.getName().toLowerCase().contains(".")
                        || item.getDepartment().contains(searchText) || item.getDepartment().contains(".")
                        || item.getDeviceBrand().contains(searchText) || item.getDatePurchased().contains(searchText) || item.getDateExpired().contains(searchText)) {

                    filteredList.add(item);
                }
            }
        }
        // Update the dataset used by the adapter with the filtered results
        adapter.setDeviceList(filteredList);

        // Notify the adapter of dataset changes
        adapter.notifyDataSetChanged();
    }

    private void setUpButtons() {
        addBtn = findViewById(R.id.addBtn);

        importBtn = findViewById(R.id.importBtn);
        exportBtn = findViewById(R.id.exportBtn);

        laptopBtn = findViewById(R.id.laptopBtn);
        tabletBtn = findViewById(R.id.tabletBtn);
        phoneBtn = findViewById(R.id.phoneBtn);
        pcBtn = findViewById(R.id.pcBtn);
        allBtn = findViewById(R.id.allBtn);
        unknownBtn = findViewById(R.id.unknownBtn);
        unknownUserBtn = findViewById(R.id.unknownUserBtn);
        expiredBtn = findViewById(R.id.expiredDevices);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ScanQR.class);
                startActivity(intent);

            }
        });

        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
//                Dialog dialog = new Dialog(MainActivity.this);
//                LayoutInflater inflater = getLayoutInflater();
//                View dialogView = inflater.inflate(R.layout.loading_dialog, null);
//                ImageView loadingIc = dialogView.findViewById(R.id.loading_icon);
//                Utils.CustomFpsInterpolator fpsInterpolator = new Utils.CustomFpsInterpolator(16);
//                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(loadingIc, "rotation", 0, 360);
//                objectAnimator.setDuration(1000);
//                objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
//                objectAnimator.setInterpolator(fpsInterpolator);
//                objectAnimator.start();
//                dialog.setContentView(dialogView);
//                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//                dialog.getWindow().setDimAmount(0.8f);
//                dialog.setCancelable(true);
//                dialog.show();
            }
        });

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                customToastMethod.notify(R.layout.toasty, R.drawable.warning_sign, "To be added", null, null, null);
                promptExportWithFileName();
            }
        });

        laptopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LaptopActivity.class);
                startActivity(intent);
            }
        });

        tabletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TabletActivity.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
            }
        });

        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MobileActivity.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
            }
        });

        pcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DesktopsActivity.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
            }
        });

        unknownUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnknownUserActivity.class);
                startActivity(intent);
            }
        });

        expiredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpiredDevicesActivity.class);
                startActivity(intent);
            }
        });

        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, allDevice.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
            }
        });

        unknownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnknownDeviceActivity.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set MIME type to all types of files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE);
    }

    // Importing excel code block
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            exportDatabaseToExcel(fileNameToExport);
        }

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            // Run the AsyncTask to import data
            new ImportDataAsyncTask(MainActivity.this, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);
        }
    }


    private void exportDatabaseToExcel(String fileName) {
        int cl1 = ContextCompat.getColor(MainActivity.this, R.color.txtTitleD);
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    row.createCell(1).setCellValue(device.getName());
                    row.createCell(2).setCellValue(device.getDepartment());
                    row.createCell(3).setCellValue(device.getDeviceType());
                    row.createCell(4).setCellValue(device.getDeviceBrand());
                    row.createCell(5).setCellValue(device.getDatePurchased());
                    row.createCell(6).setCellValue(device.getDateExpired());
                    Log.d("MainActivity", "run: date expired " + row.getCell(6).getStringCellValue());
                    row.createCell(7).setCellValue(device.getStatus());
                    row.createCell(8).setCellValue(device.getAvailability());
                    Log.d("MainActivity", "Added row " + rowNum + " to the sheet");

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
                        OutputStream outputStream = getContentResolver().openOutputStream(selectedFileUri);
                        workbook.write(outputStream);
                        outputStream.close();
                        Log.d("MainActivity", "Excel file saved to: " + selectedFileUri.getPath());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                topSnack_icon.setImageResource(R.drawable.check);
                                topSnackMessage.setText("Excel file saved to: " + selectedFileUri.getPath());
                                TopSnack.createCustomTopSnack(MainActivity.this, main, topSnackView, null, null);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle any errors that occur during file writing
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            topSnack_icon.setImageResource(R.drawable.warning_sign);
                            topSnackMessage.setText("Failed to export data");
                            TopSnack.createCustomTopSnack(MainActivity.this, main, topSnackView, null, null);
                        }
                    });
                }
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Storage permission granted by user");
                exportDatabaseToExcel(fileNameToExport);  // Retry exporting now that permission is granted
            } else {
                Log.d("MainActivity", "Storage permission denied by user");
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    Utility functions
    private void onEditClick(int position) {
    }


    public void onDeleteClick(int position) {
    }


    private void loadDataFromDatabase() {
        deviceList.clear();
        deviceList.addAll(dbHelper.fetchDevice());
        adapter.notifyDataSetChanged();

    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission is granted
                return;
            } else {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            // Below Android 11
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    // Show Dialog to put file name
    private void promptExportWithFileName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_export_dialog, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
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
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = input.getText().toString();
                if (!fileName.isEmpty()) {
                    fileNameToExport = fileName;
                    checkStoragePermission();
                    createFile();
                    exportDatabaseToExcel(fileNameToExport);
                    alertDialog.hide();
                } else {
                    Toast.makeText(MainActivity.this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
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


    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, fileNameToExport );
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

}