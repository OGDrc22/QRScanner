package com.example.qrscanner.activities;


import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.adapter.GadgetAdapterMainUI;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.ItemModel;
import com.example.qrscanner.models.GadgetsList;
import com.example.qrscanner.utils.DeleteSelected;
import com.example.qrscanner.utils.ExportDateBaseToExcel;
import com.example.qrscanner.utils.ExportSelectedDialogHelper;
import com.example.qrscanner.utils.ExportSelectedItemToExcel;
import com.example.qrscanner.utils.ImportDataAsyncTask;
import com.example.qrscanner.utils.Utils;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 16168;
    private static final int CREATE_FILE_REQUEST_CODE_SELECTED = 16169;
    private static final int FILE_REQUEST_CODE = 013;
    private static final int STORAGE_PERMISSION_CODE = 22;

    private String fileNameToExport;
    private Uri selectedFileUri;


    private CardView  laptopBtn, tabletBtn, phoneBtn, pcBtn, unknownBtn;
    private LinearLayout allBtn, importBtn, exportBtn, unknownUserBtn, expiredBtn;
    private TextView titleTextView;

    private ImageView settings, currentActivity, backBtn;
    private SearchView searchView;
    private RecyclerView recyclerView, recyclerViewSearch;
    private ConstraintLayout main, constraintLayout, addBtn;
    private ItemAdapter adapter;
    private GadgetAdapterMainUI gadgetAdapterMainUI;
    private ArrayList<ItemModel> deviceList;
    private ArrayList<String>  serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private View spacerView;
    private LinearLayout deselectID, deleteSelectedID, selectAllID, multiSelectID, exportSelected;
    private TextView textViewItemCount, textViewCountSelection;

    private CardView cardView_options;
    private LinearLayout linearContent;

    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage, topSnackDesc;

    private DBHelper dbHelper;
    private ArrayList<ItemModel> filteredList;

    private boolean hasFocusBoolean = false;


    private Intent intentSettings;
    private static final int SETTINGS_REQUEST_CODE = 147;

    private void applyTheme() {
        SharedPreferences sharedPref = getSharedPreferences("ThemePref", Context.MODE_PRIVATE);
        String themePreference = sharedPref.getString("selectedTheme", "System");  // Default to "System"

        intentSettings = new Intent(MainActivity.this, Settings.class);

        String themes = "themes";
        switch (themePreference) {
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                intentSettings.putExtra(themes, "Light");
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                intentSettings.putExtra(themes, "Dark");
                break;
            case "System":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                intentSettings.putExtra(themes, "System");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.main).bringToFront();


        searchView = findViewById(R.id.search_bar);
        searchView.clearFocus();

        main = findViewById(R.id.main);
        constraintLayout = findViewById(R.id.constraintLayoutOp);


        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);

        titleTextView = findViewById(R.id.titleTextView);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.GONE);
        settings = findViewById(R.id.settingsIcon);

        cardView_options = findViewById(R.id.cardView_options);
        linearContent = findViewById(R.id.linearContent);
        textViewItemCount = findViewById(R.id.textViewItemCount);
        textViewCountSelection = findViewById(R.id.textViewCountSelection);
        spacerView = findViewById(R.id.spacerView);
        selectAllID = findViewById(R.id.selectAllID);
        deselectID = findViewById(R.id.deselectID);
        multiSelectID = findViewById(R.id.multi_selectID);
        exportSelected = findViewById(R.id.exportSelectedID);
        deleteSelectedID = findViewById(R.id.deleteSelectedID);

        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(MainActivity.this.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);

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
        adapter = new ItemAdapter(R.layout.info_layout, this, main, deviceList, serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id, this::onDeleteClick, this::onEditClick);

        recyclerViewSearch = findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewSearch.setAdapter(adapter);


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

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hasFocusBoolean = true;

                } else { //  Clear Focus
                    hasFocusBoolean = false;
                }
                navBar();
            }
        });




        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasFocusBoolean){
                    int duration = 300;
                    Utils.smoothTransition(cardView_options, duration);
                    if (linearContent.getVisibility() == View.GONE) {
                        Utils.rotateUp(settings);
                        linearContent.setVisibility(View.VISIBLE);
                        spacerView.setVisibility(View.VISIBLE);
                        textViewItemCount.setVisibility(View.VISIBLE);
                        Utils.expandCardView(cardView_options, duration);
                    } else {
                        Utils.rotateDown(settings);
                        linearContent.setVisibility(View.GONE);
                        textViewItemCount.setVisibility(View.GONE);
                        spacerView.setVisibility(View.GONE);
                        Utils.collapseCardView(cardView_options, cardView_options, duration);
                    }
                } else {
                    Utils.rotateUp(settings);

                    setResult(RESULT_OK, intentSettings);
                    startActivityForResult(intentSettings, SETTINGS_REQUEST_CODE);
                    Log.d("TAG", "Clicked Success");
                }
            }
        });

        multiSelectID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0) {
                    adapter.enableMultiSelect();
                }
            }
        });

        selectAllID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0) {
                    Log.d("MainActivity", "onClick: filteredlist size " + getFilteredList().size() );;
                    adapter.selectAll(getFilteredList());
                }
            }
        });

        deselectID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0) {
                    adapter.clearSelection();

                    adapter.updateSelectionCount();
                    adapter.refreshSelectionOption();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        deleteSelectedID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0) {
                    DeleteSelected deleteSelected = new DeleteSelected(MainActivity.this, adapter, main);
                    deleteSelected.execute();
                    adapter.disableMultiSelect();
                    Utils.getSelectedItemCounter(MainActivity.this, textViewCountSelection);
                }
            }
        });

        exportSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExportSelectedDialogHelper exportDialogHelper = new ExportSelectedDialogHelper(MainActivity.this, MainActivity.this, adapter, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc);
                exportDialogHelper.promptExportWithFileName();
            }
        });



        Utils.getItemCounterInAdapter(adapter, textViewItemCount);



        addDefaultGadgets(); // For icons of the gadget

        List<GadgetsList> allGadget = dbHelper.getAllGadgetsCategory();
        recyclerView = findViewById(R.id.recyclerViewMainUi);
        gadgetAdapterMainUI = new GadgetAdapterMainUI(R.layout.gadgets_option_main_ui, MainActivity.this, dbHelper, allGadget);

        if (recyclerView != null){
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        } else {
            Log.d("MainActivity", "onCreate: recyclerView is null");
        }
        recyclerView.setAdapter(gadgetAdapterMainUI);

        setUpButtons();
    }

    private void navBar() {
        if (hasFocusBoolean) {
            constraintLayout.setVisibility(View.GONE);
            if (constraintLayout.getVisibility() == View.GONE) {
                recyclerViewSearch.setVisibility(View.VISIBLE);


                settings.setImageResource(R.drawable.drop_down);

                titleTextView.setText("Search");
                currentActivity.setVisibility(View.GONE);

                backBtn.setVisibility(View.VISIBLE);
                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchView.clearFocus();
                        searchView.setQuery("", false);
                        recyclerViewSearch.setVisibility(View.GONE);
                        constraintLayout.setVisibility(View.VISIBLE);

                        settings.setImageResource(R.drawable.gear_out_line);
                        backBtn.setVisibility(View.GONE);
                        titleTextView.setText("QR Scanner");
                        if (constraintLayout.getVisibility() == View.VISIBLE) {
                            adapter.clearSelection();
                            adapter.updateSelectionCount();
                            adapter.refreshSelectionOption();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        searchView.clearFocus();
        searchView.setQuery("", false);
        recyclerViewSearch.setVisibility(View.GONE);
        constraintLayout.setVisibility(View.VISIBLE);

        settings.setImageResource(R.drawable.gear_out_line);
        backBtn.setVisibility(View.GONE);
        titleTextView.setText("QR Scanner");

        if (constraintLayout.getVisibility() == View.VISIBLE) {
            adapter.clearSelection();
            adapter.updateSelectionCount();
            adapter.refreshSelectionOption();
        }
    }


    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Get the intent returned from the second activity
                    Intent data = result.getData();
                    String intentToUpdate = data != null ? data.getStringExtra("gadgetsLists") : null;
                    Log.d("MainActivity", "Received intent: " + intentToUpdate);
                    updateGadgetAdapter();
                }
            }
    );

    private void updateGadgetAdapter() {
        List<GadgetsList> gadgetsList = dbHelper.getAllGadgetsCategory();
        gadgetAdapterMainUI = new GadgetAdapterMainUI(R.layout.gadgets_option_main_ui, MainActivity.this, dbHelper, gadgetsList);
        recyclerView.setAdapter(gadgetAdapterMainUI);
        gadgetAdapterMainUI.notifyDataSetChanged();
    }


    public void filterList(@NonNull String text) {

        filteredList = new ArrayList<>();

        String searchText = text.toLowerCase();

        ArrayList<ItemModel> dataLists = dbHelper.fetchDevice();
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
            for (ItemModel item : dataLists) {
                String serialNum = item.getSerialNumber();
                String serialNumString = String.valueOf(serialNum);
                // Perform case-insensitive search by converting both text and item data to lowercase
                if (serialNumString.toLowerCase().contains(searchText) ) {
                    filteredList.add(item);
                } else if (item.getUserName().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                } else if (item.getDepartment().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                } else if (item.getDeviceType().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                } else if (item.getDeviceBrand().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                } else if (item.getDatePurchased().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                } else if (item.getDateExpired().toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                }

            }

        }
        // Update the dataset used by the adapter with the filtered results
        adapter.setDeviceList(filteredList);

        // Notify the adapter of dataset changes
        adapter.notifyDataSetChanged();

        Utils.getItemCounterInAdapter(adapter, textViewItemCount);
    }

    public ArrayList<ItemModel> getFilteredList() {
        return filteredList;
    }

    private void setUpButtons() {
        addBtn = findViewById(R.id.addBtn);

        importBtn = findViewById(R.id.importBtn);
        exportBtn = findViewById(R.id.exportBtn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ScanQR.class);
                launcher.launch(intent);

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
                promptExportWithFileName();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set MIME type to all types of files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE_SELECTED && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUriSelected = data.getData();
            new ExportSelectedItemToExcel(MainActivity.this, main, adapter, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUriSelected);
        }

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            new ExportDateBaseToExcel(MainActivity.this, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);
        }

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            // Run the AsyncTask to import data
            new ImportDataAsyncTask(MainActivity.this, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Storage permission granted by user");
                new ExportDateBaseToExcel(MainActivity.this, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);  // Retry exporting now that permission is granted
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
                    new ExportDateBaseToExcel(MainActivity.this, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUri);
                    alertDialog.dismiss();
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



    private void addDefaultGadgets() {
        // Add default gadgets if database is empty
        List<GadgetsList> gadgetsList = dbHelper.getAllGadgetsCategory();
        if (gadgetsList.isEmpty()) {
            dbHelper.addGadgetCategory("All Device", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.device_model));
            dbHelper.addGadgetCategory("Unknown", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_unknown_device));
            dbHelper.addGadgetCategory("Unknown User", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.user_unknown_bulk));
            dbHelper.addGadgetCategory("Expired Device", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_expiration));
            dbHelper.addGadgetCategory("Laptop", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.laptop_icon));
            dbHelper.addGadgetCategory("Phone", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_mobile_phone));
            dbHelper.addGadgetCategory("Tablet", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_tablet));
            dbHelper.addGadgetCategory("Desktop", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_pc_computer));
            dbHelper.addGadgetCategory("Monitor", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_monitor));
            dbHelper.addGadgetCategory("Mouse", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_mouse));
            dbHelper.addGadgetCategory("Keyboard", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_keyboard));
            dbHelper.addGadgetCategory("Headset", Utils.getDefaultImageByteArray(MainActivity.this, R.drawable.ic_headset));
        }
    }

}