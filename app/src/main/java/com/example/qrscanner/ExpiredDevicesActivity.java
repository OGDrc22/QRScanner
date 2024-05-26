package com.example.qrscanner;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.adapter.ItemAdapter;

import java.util.ArrayList;
import java.util.Iterator;

public class ExpiredDevicesActivity extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<Assigned_to_User_Model> filteredList;

    private TextView textViewInfo, textViewNoData, textViewItemCount;
    private CardView cardView_options, cardViewDeleteAll;
    private ImageView currentActivity, currentActivity2, settingsIcon, backBtn;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expired_devices);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setImageResource(R.drawable.drop_down);
        backBtn = findViewById(R.id.backBtn);
        cardView_options = findViewById(R.id.cardView_options);
        textViewNoData = findViewById(R.id.textViewNoData);
        textViewItemCount = findViewById(R.id.textViewItemCount);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardView_options.getVisibility() == View.GONE) {
                    cardView_options.setVisibility(View.VISIBLE);
                    textViewItemCount.setVisibility(View.VISIBLE);
                } else {
                    cardView_options.setVisibility(View.GONE);
                    textViewItemCount.setVisibility(View.GONE);
                }
            }
        });



        dbHelper = new DBHelper(this);
        deviceList = dbHelper.fetchDevice();

        serialNum = new ArrayList<>();
        assignedTo = new ArrayList<>();
        department = new ArrayList<>();
        device = new ArrayList<>();
        deviceModel = new ArrayList<>();
        datePurchased = new ArrayList<>();
        dateExpire = new ArrayList<>();
        status = new ArrayList<>();
        availability = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new ItemAdapter( R.layout.info_layout, this, deviceList, device, serialNum, assignedTo, department, deviceModel, datePurchased, dateExpire, status, availability, this::onDeleteClick, this::onEditClick);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        textViewInfo = findViewById(R.id.titleTextView);
        textViewInfo.setText("Expired Devices");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);
        currentActivity2 = findViewById(R.id.currentActivity2);
        currentActivity2.setVisibility(View.VISIBLE);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            currentActivity2.setImageResource(R.drawable.expiration_light);
        } else {
            currentActivity2.setImageResource(R.drawable.expiration);
        }

        cardViewDeleteAll = findViewById(R.id.cardViewDeleteAll);
        cardViewDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    if (!filteredList.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ExpiredDevicesActivity.this, R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(ExpiredDevicesActivity.this).inflate(R.layout.layout_delete_dialog, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
                        builder.setView(view);
                        ((TextView) view.findViewById(R.id.titleText)).setText("Delete [ All Expired Device ]");
                        ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to [ All Expired device ] ?" + "\n" + "\n" + "This action cannot be undone.");
                        ((ImageView) view.findViewById(R.id.icon_action)).setImageResource(R.drawable.trash_can_10416);
                        ((ImageView) view.findViewById(R.id.warning)).setImageResource(R.drawable.warning_sign);
                        ((Button) view.findViewById(R.id.actionDelete)).setText("Delete");
                        ((Button) view.findViewById(R.id.actionCancel)).setText("Cancel");

                        final AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        // function
                        view.findViewById(R.id.actionDelete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteDataByGadgetName();
                                deviceList.clear();
                                Toast.makeText(ExpiredDevicesActivity.this, "All Expired Device Deleted", Toast.LENGTH_SHORT).show();
                                alertDialog.hide();
                                adapter.notifyDataSetChanged();
                            }
                        });


                        // For Cancel Button
                        view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(ExpiredDevicesActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                                alertDialog.hide();
                            }
                        });

                        if (alertDialog.getWindow() != null) {
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }

                    } else {
                        Toast.makeText(ExpiredDevicesActivity.this, "Expired Devices is empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        filterDeviceList();

    }



    private void onEditClick(int position) {
    }

    public void onDeleteClick(int position) {
    }

    private void deleteDataByGadgetName() {
        // Iterate through the original list and remove items that match the gadget name
        Iterator<Assigned_to_User_Model> iterator = deviceList.iterator();
        while (iterator.hasNext()) {
            Assigned_to_User_Model device = iterator.next();
            // Assuming getDeviceName() returns the name of the device
            if (device.getStatus().equals("For Refresh")) {
                // Remove the item from the list
                iterator.remove();
                dbHelper.deleteExpiredDevice(device);
            }
        }

        // Update the dataset used by the adapter with the modified list
        adapter.setDeviceList(deviceList);

        // Notify the adapter of dataset changes
        adapter.notifyDataSetChanged();
    }




    // For Edit Button and Update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the UI here, for example, reload data from the database
            loadDataFromDatabase();
            adapter.notifyDataSetChanged(); // Notify the adapter of dataset changes
        }
    }

    private void loadDataFromDatabase() {
        deviceList.clear();
        deviceList.addAll(dbHelper.fetchDevice());
        filterDeviceList();
        adapter.notifyDataSetChanged();

    }

    // Show the item that is Expired Device column
    private void filterDeviceList() {
        filteredList = new ArrayList<>();
        filteredList.clear();
        if (deviceList.isEmpty()) {
            // Show a toast message or handle empty list case
        } else {
            // Iterate through the original list and add items that match the criteria to the filtered list
            for (Assigned_to_User_Model device : deviceList) {
                // Assuming device contains the name of the gadget
                String deviceStatus = device.getStatus();
                // Check if the device name contains "laptop" (case-insensitive)
                if (deviceStatus.toLowerCase().contains("for refresh")) {
                    filteredList.add(device);
                }
            }

            String deviceCount = String.valueOf(filteredList.size());
            textViewItemCount.setText("Item Count: " + deviceCount);

            if (filteredList.isEmpty()) {
                textViewNoData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textViewNoData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        adapter.setDeviceList(filteredList);
    }

}