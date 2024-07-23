package com.example.qrscanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class UnknownUserActivity extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;
    private ArrayList<Assigned_to_User_Model> filteredList;

    private TextView textViewInfo, textViewNoData, textViewItemCount;
    private CardView cardView_options;
    private ImageView currentActivity, currentActivity2, settingsIcon, backBtn;
    private ConstraintLayout constraintLayoutDeleteAll;
    private LinearLayout cardViewContent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unknown_user);
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
        textViewInfo.setText("No User");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);
        currentActivity2 = findViewById(R.id.currentActivity2);
        currentActivity2.setVisibility(View.VISIBLE);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            currentActivity2.setImageResource(R.drawable.user_unknown);
            currentActivity2.setColorFilter(Color.argb(255, 255, 255, 255));
        } else {
            currentActivity2.setImageResource(R.drawable.user_unknown);
        }

        constraintLayoutDeleteAll = findViewById(R.id.constraintDeleteAll);
        constraintLayoutDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    if (!filteredList.isEmpty()) {
                        String identifier = "unknown user device";
                        Utils.showDeleteAllDialog(UnknownUserActivity.this, identifier);
                        adapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(UnknownUserActivity.this, "Unknown user is empty", Toast.LENGTH_SHORT).show();
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

    private void deleteByFilter() {
        // Iterate through the original list and remove items that match the gadget name
        Iterator<Assigned_to_User_Model> iterator = deviceList.iterator();
        while (iterator.hasNext()) {
            Assigned_to_User_Model device = iterator.next();
            // Assuming getDeviceName() returns the name of the device
            if (device.getName().isEmpty()) {
                // Remove the item from the list
                Log.d("TAG", "deleteByFilter: " + device.getSerialNumber());
                Toast.makeText(this, "Deleted: " + device.getSerialNumber(), Toast.LENGTH_SHORT).show();
                iterator.remove();
                dbHelper.deleteDeviceNoUser(device);
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

    // Show the item that has a no user column
    private void filterDeviceList() {
        filteredList = new ArrayList<>();
        filteredList.clear();
        if (deviceList.isEmpty()) {
            // Show a toast message or handle empty list case
        } else {
            // Iterate through the original list and add items that match the criteria to the filtered list
            for (Assigned_to_User_Model device : deviceList) {
                // Assuming device contains the name of the gadget
                String userName = device.getName();
                // Check if the device name contains "laptop" (case-insensitive)
                if (userName.isEmpty()) {
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

        Collections.reverse(filteredList);
        adapter.setDeviceList(filteredList);
    }

}