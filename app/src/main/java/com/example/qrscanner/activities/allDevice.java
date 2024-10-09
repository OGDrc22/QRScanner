package com.example.qrscanner.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drc.mytopsnacklibrary.TopSnack;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.adapter.ItemAdapter;
import com.example.qrscanner.models.ItemModel;
import com.example.qrscanner.utils.AfterAsyncListener;
import com.example.qrscanner.utils.DataLoader;
import com.example.qrscanner.utils.DeleteSelected;
import com.example.qrscanner.utils.ExportSelectedDialogHelper;
import com.example.qrscanner.utils.ExportSelectedItemToExcel;
import com.example.qrscanner.utils.Utils;

import java.util.ArrayList;

public class allDevice extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private static final int CREATE_FILE_REQUEST_CODE_SELECTED = 16169;
    private static final int FILE_REQUEST_CODE = 013;
    private static final int STORAGE_PERMISSION_CODE = 22;

    private String fileNameToExport;
    private Uri selectedFileUri;


    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<ItemModel> deviceList;

    private TextView textViewInfo, textViewNoData, textViewItemCount, textViewCountSelection;
    private CardView cardView_options;
    private ImageView currentActivity, settingsIcon, backBtn;
    private LinearLayout linearLayoutDeleteAll;
    private LinearLayout main, linearContent;

    private View spacerView;
    private LinearLayout deselectID, deleteSelectedID, selectAllID, multiSelectID, exportSelected;

    private View topSnackView;
    private ImageView topSnack_icon;
    private TextView topSnackMessage, topSnackDesc;


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

        main = findViewById(R.id.main);

        settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setImageResource(R.drawable.drop_down);
        backBtn = findViewById(R.id.backBtn);
        cardView_options = findViewById(R.id.cardView_options);
        linearContent = findViewById(R.id.linearContent);
        textViewNoData = findViewById(R.id.textViewNoData);
        textViewItemCount = findViewById(R.id.textViewItemCount);
        textViewCountSelection = findViewById(R.id.textViewCountSelection);
        spacerView = findViewById(R.id.spacerView);
        selectAllID = findViewById(R.id.selectAllID);
        deselectID = findViewById(R.id.deselectID);
        multiSelectID = findViewById(R.id.multi_selectID);
        exportSelected = findViewById(R.id.exportSelectedID);
        deleteSelectedID = findViewById(R.id.deleteSelectedID);

        linearLayoutDeleteAll = findViewById(R.id.linearLayoutDeleteAll);

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

        adapter = new ItemAdapter( R.layout.info_layout, this, main, deviceList, device, serialNum, assignedTo, department, deviceModel, datePurchased, dateExpire, status, availability, this::onDeleteClick, this::onEditClick);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LayoutInflater inflater = (LayoutInflater) allDevice.this.getSystemService(allDevice.this.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);


        DataLoader loader = new DataLoader(allDevice.this, main, dbHelper, adapter, textViewItemCount, deviceList);


        textViewInfo = findViewById(R.id.titleTextView);
        textViewInfo.setText("All Devices");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setImageResource(R.drawable.device_model);


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
                if (linearContent.getVisibility() == View.GONE) {
                    Utils.rotateUp(settingsIcon);
                    linearContent.setVisibility(View.VISIBLE);
                    textViewItemCount.setVisibility(View.VISIBLE);
                    spacerView.setVisibility(View.VISIBLE);
                    Utils.expandCardView(cardView_options, duration);
                } else {
                    Utils.rotateDown(settingsIcon);
                    linearContent.setVisibility(View.GONE);
                    textViewItemCount.setVisibility(View.GONE);
                    spacerView.setVisibility(View.GONE);
                    Utils.collapseCardView(cardView_options, cardView_options, duration);
                }
            }
        });

        selectAllID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAll(loader.getFilteredList());
            }
        });

        multiSelectID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0){
                    adapter.enableMultiSelect();
                }
            }
        });

        deselectID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clearSelection();

                adapter.updateSelectionCount();
                adapter.refreshSelectionOption();
                adapter.notifyDataSetChanged();
            }
        });

        deleteSelectedID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelected deleteSelected = new DeleteSelected(allDevice.this, adapter, main);
                deleteSelected.execute();
                adapter.disableMultiSelect();
                Utils.getSelectedItemCounter(allDevice.this, textViewCountSelection);
            }
        });

        exportSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExportSelectedDialogHelper exportDialogHelper = new ExportSelectedDialogHelper(allDevice.this, allDevice.this, adapter, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc);
                exportDialogHelper.promptExportWithFileName();
            }
        });



        Utils.getItemCounterInAdapter(adapter, textViewItemCount);

        if (adapter.getItemCount() == 0) {
            textViewNoData.setVisibility(View.VISIBLE);
        } else {
            textViewNoData.setVisibility(View.GONE);
        }



        if (deviceList.isEmpty()) {
            textViewNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }





        linearLayoutDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    String identifier = "device";
                    itemCounter();
                    Utils.showDeleteAllDialog(allDevice.this, identifier, adapter, main);
                }else {
                    Toast.makeText(allDevice.this, "There's no Data", Toast.LENGTH_SHORT).show();
                }
            }
        });


        loader.execute();

    }

    private void itemCounter() {
        deviceList = dbHelper.fetchDevice();
        String deviceCount = String.valueOf(deviceList.size());
        textViewItemCount.setText("Item Count: " + deviceCount);
    }


    private void onEditClick(int position) {
    }

    public void onDeleteClick(int position) {
    }




    // For Edit Button and Update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String keyIdentical = "No differences found";
        String keyDifferent = "Difference found";


        if (requestCode == CREATE_FILE_REQUEST_CODE_SELECTED && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUriSelection = data.getData();
            new ExportSelectedItemToExcel(allDevice.this, main, adapter, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUriSelection);
        }


        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

//            Get the passed Data from UpdateActivity
            String ser = data.getStringExtra("serial");
            String resA = data.getStringExtra("keyIdentical");
            String resB = data.getStringExtra("keyDifferent");

            if (resA != null && resA.equals(keyIdentical)) {

                topSnackMessage.setText(data.getStringExtra("keyIdentical"));
                TopSnack.createCustomTopSnack(allDevice.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyIdentical"));

            } else if (resB != null && resB.equals(keyDifferent)) {

                DataLoader loader = getItemAdapterDataLoader(data, ser);

                loader.execute();

            } else {
                Log.d("result", "onActivityResult: resA & resB is null" );
            }
        }
    }

    //    After the Item data has change, show this Notification
    private @NonNull DataLoader getItemAdapterDataLoader(@NonNull Intent data, String ser) {
        DataLoader loader = new DataLoader(allDevice.this, main, dbHelper, adapter, textViewItemCount, deviceList);
        loader.setOnAfterAsync(new AfterAsyncListener() {
            @Override
            public void after(int delay) {

                topSnackMessage.setText(ser);
                topSnackDesc.setVisibility(View.VISIBLE);
                String updateSuccess = "Updated Successfully";
                topSnackDesc.setText(updateSuccess);
                TopSnack.createCustomTopSnack(allDevice.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyDifferent"));

            }
        });
        return loader;
    }

}