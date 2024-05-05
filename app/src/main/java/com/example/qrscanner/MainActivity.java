package com.example.qrscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.example.qrscanner.adapter.ItemAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;

public class MainActivity extends AppCompatActivity {

    private CardView addBtn, laptopBtn, tabletBtn, phoneBtn, pcBtn, allBtn, unknownBtn, importBtn, exportBtn, unknownUserBtn, expiredBtn;

    private ImageView settings, currentActivity, backBtn;
    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView recyclerView;
//    private ConstraintLayout mainOption;
    private GridLayout mainOption;
    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<String>  serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private int intSerial;

    private DBHelper dbHelper;

    private static final int FILE_REQUEST_CODE = 013;

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

        findViewById(R.id.main).bringToFront();

        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.GONE);

        settings = findViewById(R.id.settingsIcon);
        mainOption = findViewById(R.id.gridLayoutMainOption);


        searchView = findViewById(R.id.search_bar);
        searchView.clearFocus();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
                Log.d("TAG", "Clicked Success");
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    recyclerView.setVisibility(View.VISIBLE);
                    mainOption.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    mainOption.setVisibility(View.VISIBLE);
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

        recyclerView = findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


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
                Log.d("TAG", "Clicked Success");

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

                Toast.makeText(MainActivity.this, "To be Added", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "Clicked Success");

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

    private void filterList(@NonNull String text) {

        ArrayList<Assigned_to_User_Model> filteredList = new ArrayList<>();

        String searchText = text.toLowerCase();

        ArrayList<Assigned_to_User_Model> dataLists = dbHelper.fetchDevice();
        Collections.reverse(dataLists);

        for (int i = 0; i < filteredList.size(); i++) {
            filteredList.get(i).setOriginalPosition(i);
        }

        // Iterate through the original list and add items that match the search text to the filtered list
        if (searchText.isEmpty()) {
            // If the search query is empty, clear the filtered list
            filteredList.clear();
        } else {
            // Iterate through the original list and add items that match the search text to the filtered list
            for (Assigned_to_User_Model item : dataLists) {
                int serialNum = item.getSerialNumber();
                String serialNumString = String.valueOf(serialNum);
                // Perform case-insensitive search by converting both text and item data to lowercase
                if (serialNumString.toLowerCase().contains(searchText)
                        || item.getName().toLowerCase().contains(searchText) || item.getName().toLowerCase().contains(".")
                        || item.getDepartment().contains(searchText) || item.getDepartment().contains(".")
                        || item.getDeviceModel().contains(searchText) || item.getDatePurchased().contains(searchText) || item.getDateExpired().contains(searchText)) {

                    filteredList.add(item);
                }
            }
        }
        // Update the dataset used by the adapter with the filtered results
        adapter.setDeviceList(filteredList);

        // Notify the adapter of dataset changes
        adapter.notifyDataSetChanged();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set MIME type to all types of files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE);
    }

//    Importing excel code block
    // TODO exclude existing data from excel in importation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                    double serialNum;
                    Cell serialNumCell = row.getCell(0);
                    if (serialNumCell != null && serialNumCell.getCellType() == CellType.NUMERIC) {
                        serialNum = serialNumCell.getNumericCellValue();
                    } else if (serialNumCell == null) {
                        serialNum = 0; // Or any default value you want to use
                    } else {
                        // Handle empty or non-numeric cell
                        continue;
                    }

                    intSerial = (int) serialNum;

                    if (dbHelper.getAllSerialNumbers().contains(String.valueOf(intSerial))) {
                        continue;
                    }

//                    String name = row.getCell(1).getStringCellValue();
                    String name;
                    Cell nameCell = row.getCell(1);
                    if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                        name = nameCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        name = ""; // Or any default value you want to use
                    }

//                    String department = row.getCell(2).getStringCellValue();
                    String department;
                    Cell departmentCell = row.getCell(2);
                    if (departmentCell != null && departmentCell.getCellType() == CellType.STRING) {
                        department = departmentCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        department = ""; // Or any default value you want to use
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
                        deviceModel = ""; // Or any default value you want to use
                    }


//                    String datePurchased = row.getCell(4).getStringCellValue();
                    String datePurchased;
                    Cell datePurchasedCell = row.getCell(5);
                    if (datePurchasedCell != null && datePurchasedCell.getCellType() == CellType.STRING) {
                        datePurchased = datePurchasedCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        datePurchased = ""; // Or any default value you want to use
                    }

//                    String dateExpired = row.getCell(5).getStringCellValue();
                    String dateExpired;
                    Cell dateExpiredCell = row.getCell(6);
                    if (dateExpiredCell != null && dateExpiredCell.getCellType() == CellType.STRING) {
                        dateExpired = dateExpiredCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        dateExpired = ""; // Or any default value you want to use
                    }

//                    String status = row.getCell(6).getStringCellValue();
                    String status;
                    Cell statusCell = row.getCell(7);
                    if (statusCell != null && statusCell.getCellType() == CellType.STRING) {
                        status = statusCell.getStringCellValue();
                    } else {
                        // Handle empty or non-string cell
                        status = ""; // Or any default value you want to use
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
                    Log.d("TAG", "From Import: " + intSerial);
                }

                if (!dbHelper.getAllSerialNumbers().contains(String.valueOf(intSerial)) && dbHelper.getAllSerialNumbers().contains(String.valueOf(intSerial))) {
                    Toast.makeText(this, "Successfully imported some of the data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show();
                }
                loadDataFromDatabase();

            } catch (IOException e) {
                e.printStackTrace();
                // Handle any errors that occur during file reading or data insertion
                Toast.makeText(this, "Error importing data", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // TODO implement exportation of data from SQLite to excel format


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
}