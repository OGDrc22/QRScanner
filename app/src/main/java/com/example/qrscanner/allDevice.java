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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.adapter.ItemAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class allDevice extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<Assigned_to_User_Model> deviceList;

    private TextView textViewInfo, textViewNoData, textViewItemCount;
    private CardView cardView_options, cardViewDeleteAll;
    private ImageView currentActivity, settingsIcon, backBtn;

    private GadgetsAdapter gadgetsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_device);
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
        String deviceCount = String.valueOf(deviceList.size());
        textViewItemCount.setText("Item Count: " + deviceCount);

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

        displayData();

        if (deviceList.isEmpty()) {
            textViewNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        textViewInfo = findViewById(R.id.titleTextView);
        textViewInfo.setText("All Devices");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setImageResource(R.drawable.device_model);

        cardViewDeleteAll = findViewById(R.id.cardViewDeleteAll);
        cardViewDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(allDevice.this, R.style.AlertDialogTheme);
                    View view = LayoutInflater.from(allDevice.this).inflate(R.layout.layout_delete_dialog,(ConstraintLayout)findViewById(R.id.layoutDialogContainer));
                    builder.setView(view);
                    ((TextView) view.findViewById(R.id.titleText)).setText("Delete [ All ]");
                    ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to [ All device ] ?" + "\n" + "\n" + "This action cannot be undone.");
                    ((ImageView) view.findViewById(R.id.icon_action)).setImageResource(R.drawable.trash_can_10416);
                    ((ImageView) view.findViewById(R.id.warning)).setImageResource(R.drawable.warning_sign);
                    ((Button) view.findViewById(R.id.actionDelete)).setText("Delete");
                    ((Button) view.findViewById(R.id.actionCancel)).setText("Cancel");

                    final  AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    // function
                    view.findViewById(R.id.actionDelete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbHelper.deleteAll();
                            deviceList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(allDevice.this, "All Data Deleted", Toast.LENGTH_SHORT).show();
                            alertDialog.hide();
                        }
                    });


                    // For Cancel Button
                    view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(allDevice.this, "Canceled", Toast.LENGTH_SHORT).show();
                            alertDialog.hide();
                        }
                    });

                    if (alertDialog.getWindow() != null) {
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }

                } else {
                    Toast.makeText(allDevice.this, "There's no Data", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    // Show the item that has a "Laptop" in the Device column
    private void displayData() {
        if (deviceList.isEmpty()) {
            Toast.makeText(this, "No Data Available", Toast.LENGTH_SHORT).show();
        } else {

            Collections.reverse(deviceList);
            for (Assigned_to_User_Model devices : deviceList) {
                serialNum.add(String.valueOf(devices.getSerialNumber()));
                assignedTo.add(devices.getName());
                department.add(devices.getDepartment());
                device.add(devices.getDevice());
                deviceModel.add(devices.getDeviceModel());
                datePurchased.add(devices.getDatePurchased());
                dateExpire.add(devices.getDateExpired());
                status.add(devices.getStatus());
                availability.add(devices.getAvailability());
            }
        }
        // Notify the adapter of dataset changes
        adapter.notifyDataSetChanged();
    }



    private void onEditClick(int position) {
    }

    public void onDeleteClick(int position) {
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
        adapter.notifyDataSetChanged();

    }
}