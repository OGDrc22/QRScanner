package com.example.qrscanner;


import android.Manifest;
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
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.Assigned_to_User_Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 16168;
    private CardView addBtn, laptopBtn, tabletBtn, phoneBtn, pcBtn, allBtn, unknownBtn, importBtn, exportBtn, unknownUserBtn, expiredBtn;

    private ImageView settings, currentActivity, backBtn;
    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView recyclerView;
    private ConstraintLayout constraintLayout;
    private GridLayout mainOption;
    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<String>  serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private String serialCode;

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
        setContentView(R.layout.activity_main);
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

        constraintLayout = findViewById(R.id.constraintLayoutOp);

        recyclerView = findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

//    Importing excel code block
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            exportDatabaseToExcel(fileNameToExport);
        }

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the selected file here
            Uri selectedFileUri = data.getData();

            // Now you have the URI of the selected file, you can read its data and insert into database
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

                DBHelper dbHelper = new DBHelper(this);

                for (Row row : sheet) {
//                    double serialNum = row.getCell(0).getNumericCellValue();
                    String serialNum;
                    Cell serialNumCell = row.getCell(0);
                    if (serialNumCell != null) {
                        if (serialNumCell.getCellType() == CellType.STRING) {
                            serialNum = serialNumCell.getStringCellValue();
                        } else if (serialNumCell.getCellType() == CellType.NUMERIC) {
                            serialNum = String.valueOf((int) serialNumCell.getNumericCellValue());
                        } else {
                            continue; // Skip if it's neither STRING nor NUMERIC
                        }
                    } else {
                        continue; // Skip if the cell is null
                    }

                    serialCode = serialNum;
                    // Handel Duplicates Serial Number in importing data, in case the data already exist in database
                    if (dbHelper.getAllSerialNumbers().contains(serialCode)) {
                        continue;
                    }

//                    String name = row.getCell(1).getStringCellValue();
                    String name;
                    Cell nameCell = row.getCell(1);
                    if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                        name = nameCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        name = null; // Or any default value you want to use
                    }

//                    String department = row.getCell(2).getStringCellValue();
                    String department;
                    Cell departmentCell = row.getCell(2);
                    if (departmentCell != null && departmentCell.getCellType() == CellType.STRING) {
                        department = departmentCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        department = null; // Or any default value you want to use
                    }

//                    String device = row.getCell(3).getStringCellValue();
                    String device;
                    Cell deviceCell = row.getCell(3);
                    if (deviceCell != null && deviceCell.getCellType() == CellType.STRING) {
                        device = deviceCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        device = "Unknown"; // Or any default value you want to use
                    }

//                    String deviceModel = row.getCell(4).getStringCellValue();
                    String deviceModel;
                    Cell deviceModelCell = row.getCell(4);
                    if (deviceModelCell != null && deviceModelCell.getCellType() == CellType.STRING) {
                        deviceModel = deviceModelCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        deviceModel = null; // Or any default value you want to use
                    }


//                    String datePurchased = row.getCell(4).getStringCellValue();
                    String datePurchased;
                    Cell datePurchasedCell = row.getCell(5);
                    if (datePurchasedCell != null && datePurchasedCell.getCellType() == CellType.STRING) {
                        datePurchased = datePurchasedCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        datePurchased = null; // Or any default value you want to use
                    }

//                    String dateExpired = row.getCell(5).getStringCellValue();
                    String dateExpired;
                    Cell dateExpiredCell = row.getCell(6);
                    if (dateExpiredCell != null && dateExpiredCell.getCellType() == CellType.STRING) {
                        dateExpired = dateExpiredCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        dateExpired = null; // Or any default value you want to use
                    }

//                    String status = row.getCell(6).getStringCellValue();
                    String status;
                    Cell statusCell = row.getCell(7);
                    if (statusCell != null && statusCell.getCellType() == CellType.STRING) {
                        status = statusCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        status = null; // Or any default value you want to use
                    }

//                    String availability = row.getCell(7).getStringCellValue();
                    String availability;
                    Cell availabilityCell = row.getCell(8);
                    if (nameCell != null) {
                        availability = "In Use";
                    } else {
                        // Handle empty or non-string cell
                        availability = "In Stock"; // Or any default value you want to use
                    }

                    dbHelper.addDevice(String.valueOf(serialNum), name, department, device, deviceModel, datePurchased, dateExpired, status, availability);

                    Log.d("TAG", "Existing Serials: " + dbHelper.getAllSerialNumbers());
                    Log.d("TAG", "From Import: " + serialCode);
                }

                if (!dbHelper.getAllSerialNumbers().contains(serialCode) && dbHelper.getAllSerialNumbers().contains(serialCode)) {
                    // Toast "Success", "Successfully imported some of the data"
                } else {
                    // Toast "Success", "Data Imported successfully"
                }
                loadDataFromDatabase();

            } catch (IOException e) {
                e.printStackTrace();
                // Handle any errors that occur during file reading or data insertion
                // Toast "Failed", "Error Importing the data"
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Add this method to export data
    private void exportDatabaseToExcel(String fileName) {
            Log.d("MainActivity", "Storage permission granted");
            // Fetch data from the database
            ArrayList<Assigned_to_User_Model> deviceList = dbHelper.fetchDevice();
            Log.d("MainActivity", "Fetched " + deviceList.size() + " records from the database");

            // Create an Excel workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Devices");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Serial Number");
            headerRow.createCell(1).setCellValue("Assigned To");
            headerRow.createCell(2).setCellValue("Department");
            headerRow.createCell(3).setCellValue("Device");
            headerRow.createCell(4).setCellValue("Device Model");
            headerRow.createCell(5).setCellValue("Date Purchased");
            headerRow.createCell(6).setCellValue("Date Expired");
            headerRow.createCell(7).setCellValue("Status");
            headerRow.createCell(8).setCellValue("Availability");

            // Fill data rows
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
                row.createCell(7).setCellValue(device.getStatus());
                row.createCell(8).setCellValue(device.getAvailability());
                Log.d("MainActivity", "Added row " + rowNum + " to the sheet");
            }

        try {
            if (selectedFileUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(selectedFileUri);
                workbook.write(outputStream);
                outputStream.close();
                Log.d("MainActivity", "Excel file saved to: " + selectedFileUri.getPath());
                Toast.makeText(MainActivity.this, "Data exported successfully to " + selectedFileUri.getPath(), Toast.LENGTH_LONG).show();
            } else {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle any errors that occur during file writing
            Toast.makeText(MainActivity.this, "Failed to export data", Toast.LENGTH_SHORT).show();
        }
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