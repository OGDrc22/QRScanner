package com.example.qrscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class UnknownDeviceActivity extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<Assigned_to_User_Model> filteredList;

    private TextView textViewInfo, textViewNoData, textViewItemCount;
    private CardView cardView_options;
    private ImageView currentActivity, settingsIcon, backBtn;
    private ConstraintLayout constraintLayoutDeleteAll;
    private LinearLayout cardViewContent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unknown_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setImageResource(R.drawable.drop_down);
        backBtn = findViewById(R.id.backBtn);
        cardView_options = findViewById(R.id.cardView_options);
        cardViewContent = findViewById(R.id.cardViewContent);
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
                final int initialHeight = cardView_options.getHeight();
                int duration = 300;
                Utils.smoothTransition(cardView_options, duration);
                if (cardViewContent.getVisibility() == View.GONE) {
                    Utils.rotateUp(settingsIcon);
                    cardViewContent.setVisibility(View.VISIBLE);
                    textViewItemCount.setVisibility(View.VISIBLE);
                    Utils.expandCardView(cardView_options, duration);
                } else {
                    Utils.rotateDown(settingsIcon);
                    cardViewContent.setVisibility(View.GONE);
                    textViewItemCount.setVisibility(View.GONE);
                    Utils.collapseCardView(cardView_options, cardView_options, duration);
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
        textViewInfo.setText("Unknown Devices");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setImageResource(R.drawable.device_model);

        constraintLayoutDeleteAll = findViewById(R.id.constraintDeleteAll);
        constraintLayoutDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    if (!filteredList.isEmpty()) {
                        String identifier = "unknown device";
//                        Utils.showDeleteAllDialog(UnknownDeviceActivity.this, identifier, adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(UnknownDeviceActivity.this, "Unknown Device is empty", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the UI here, for example, reload data from the database
            loadDataFromDatabase();
            Collections.reverse(deviceList);
            adapter.notifyDataSetChanged(); // Notify the adapter of dataset changes
        }
    }

    private void loadDataFromDatabase() {
        deviceList.clear();
        deviceList.addAll(dbHelper.fetchDevice());
        filterDeviceList();
        adapter.notifyDataSetChanged();

    }

    // Show the item that has a "Unknown" in the Device column
    private void filterDeviceList() {
        filteredList = new ArrayList<>();
        filteredList.clear();
        if (deviceList.isEmpty()) {
            // Show a toast message or handle empty list case
        } else {
            // Iterate through the original list and add items that match the criteria to the filtered list
            for (Assigned_to_User_Model device : deviceList) {
                // Assuming device contains the name of the gadget
                String deviceName = device.getDeviceType();
                // Check if the device name contains "laptop" (case-insensitive)
                if (deviceName.toLowerCase().contains("unknown") || deviceName.isEmpty()) {
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

        Collections.reverse(deviceList);
        adapter.setDeviceList(filteredList);
    }

}