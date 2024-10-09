package com.example.qrscanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.example.qrscanner.utils.DeleteSelected;
import com.example.qrscanner.utils.ExportSelectedDialogHelper;
import com.example.qrscanner.utils.ExportSelectedItemToExcel;
import com.example.qrscanner.utils.FilteredDataLoader;
import com.example.qrscanner.utils.Utils;

import java.util.ArrayList;

public class ExpiredDevicesActivity extends AppCompatActivity {

    private static final int YOUR_REQUEST_CODE = 1;
    private static final int CREATE_FILE_REQUEST_CODE_SELECTED = 16169;
    private RecyclerView recyclerView;
    private ArrayList<String> serialNum, assignedTo, department, device, deviceModel, datePurchased, dateExpire, status, availability;
    private DBHelper dbHelper;

    private ItemAdapter adapter;
    private ArrayList<ItemModel> deviceList;
    private ArrayList<ItemModel> filteredList;

    private TextView textViewInfo, textViewNoData, textViewItemCount, textViewCountSelection;
    private CardView cardView_options;
    private ImageView currentActivity, currentActivity2, settingsIcon, backBtn;
    private LinearLayout linearLayoutDeleteAll;
    private LinearLayout main, linearContent;

    private static View topSnackView;
    private static ImageView topSnack_icon;
    private static TextView topSnackMessage;
    private static TextView topSnackDesc;

    private View spacerView;
    private LinearLayout deselectID, deleteSelectedID, selectAllID, multiSelectID, exportSelected;


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

        main = findViewById(R.id.main);

        settingsIcon = findViewById(R.id.settingsIcon);
        settingsIcon.setImageResource(R.drawable.drop_down);
        backBtn = findViewById(R.id.backBtn);
        cardView_options = findViewById(R.id.cardView_options);
        linearContent = findViewById(R.id.linearContent);
        textViewNoData = findViewById(R.id.textViewNoData);
        linearLayoutDeleteAll = findViewById(R.id.linearLayoutDeleteAll);
        textViewItemCount = findViewById(R.id.textViewItemCount);
        textViewCountSelection = findViewById(R.id.textViewCountSelection);
        spacerView = findViewById(R.id.spacerView);
        selectAllID = findViewById(R.id.selectAllID);
        deselectID = findViewById(R.id.deselectID);
        deleteSelectedID = findViewById(R.id.deleteSelectedID);
        exportSelected = findViewById(R.id.exportSelectedID);
        multiSelectID = findViewById(R.id.multi_selectID);

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

        FilteredDataLoader.ExpiredDevice filter = (FilteredDataLoader.ExpiredDevice) new FilteredDataLoader.ExpiredDevice(ExpiredDevicesActivity.this, dbHelper, main, adapter, deviceList, textViewItemCount);

        textViewInfo = findViewById(R.id.titleTextView);
        textViewInfo.setText("Expired Devices");
        currentActivity = findViewById(R.id.currentActivity);
        currentActivity.setVisibility(View.GONE);
        currentActivity2 = findViewById(R.id.currentActivity2);
        currentActivity2.setVisibility(View.VISIBLE);

        if ((ExpiredDevicesActivity.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            currentActivity2.setImageResource(R.drawable.ic_expiration_light);
        } else {
            currentActivity2.setImageResource(R.drawable.ic_expiration);
        }

        if (adapter.getItemCount() == 0) {
            textViewNoData.setVisibility(View.VISIBLE);
        } else {
            textViewNoData.setVisibility(View.GONE);
        }

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
                adapter.selectAll(filter.getFilteredList());
            }
        });

        multiSelectID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.enableMultiSelect();
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
                DeleteSelected deleteSelected = new DeleteSelected(ExpiredDevicesActivity.this, adapter, main);
                deleteSelected.execute();
                FilteredDataLoader.ExpiredDevice filter = (FilteredDataLoader.ExpiredDevice) new FilteredDataLoader.ExpiredDevice(ExpiredDevicesActivity.this, dbHelper, main, adapter, deviceList, textViewItemCount);
                filter.execute();
                Utils.getSelectedItemCounter(ExpiredDevicesActivity.this, textViewCountSelection);
            }
        });


        exportSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getView(ExpiredDevicesActivity.this);
                ExportSelectedDialogHelper exportDialogHelper = new ExportSelectedDialogHelper(ExpiredDevicesActivity.this, ExpiredDevicesActivity.this, adapter, main, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc);
                exportDialogHelper.promptExportWithFileName();
            }
        });




        linearLayoutDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    if (!filter.isEmpty()) {
                        String identifier = "expired device";
//                        Utils.showDeleteAllDialog(ExpiredDevicesActivity.this, identifier, adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        filter.execute();

        Utils.getItemCounterInAdapter(adapter, textViewItemCount);

    }



    private void onEditClick(int position) {
    }

    public void onDeleteClick(int position) {
    }



    private static void getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        topSnackView = inflater.inflate(R.layout.top_snack_layout, null);
        topSnack_icon = topSnackView.findViewById(R.id.topSnack_icon);
        topSnackMessage = topSnackView.findViewById(R.id.textViewMessage);
        topSnackDesc = topSnackView.findViewById(R.id.textViewDesc);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String keyIdentical = "No differences found";
        String keyDifferent = "Difference found";

        if (requestCode == CREATE_FILE_REQUEST_CODE_SELECTED && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUriSelection = data.getData();
            new ExportSelectedItemToExcel(ExpiredDevicesActivity.this, main, adapter, topSnackView, topSnack_icon, topSnackMessage, topSnackDesc).execute(selectedFileUriSelection);
        }


        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            getView(ExpiredDevicesActivity.this);
            if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
                topSnack_icon.setImageResource(R.drawable.qr_icon_48);
                int getColorLight = ContextCompat.getColor(ExpiredDevicesActivity.this, R.color.txtHeaderLight);
                topSnack_icon.setColorFilter(getColorLight);
            } else {
                topSnack_icon.setImageResource(R.drawable.qr_icon_48);
            }

//            Get the passed Data from UpdateActivity
            String ser = data.getStringExtra("serial");
            String resA = data.getStringExtra("keyIdentical");
            String resB = data.getStringExtra("keyDifferent");

            if (resA != null && resA.equals(keyIdentical)) {

                topSnackMessage.setText(data.getStringExtra("keyIdentical"));
                TopSnack.createCustomTopSnack(ExpiredDevicesActivity.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyIdentical"));

            } else if (resB != null && resB.equals(keyDifferent)) {

                FilteredDataLoader.ExpiredDevice loader = getItemAdapterDataLoader(data, ser);

                loader.execute();

            } else {
                Log.d("result", "onActivityResult: resA & resB is null" );
            }
        }
    }

    private @NonNull FilteredDataLoader.ExpiredDevice getItemAdapterDataLoader(@NonNull Intent data, String ser) {
        FilteredDataLoader.ExpiredDevice loader = new FilteredDataLoader.ExpiredDevice(ExpiredDevicesActivity.this, dbHelper, main, adapter, deviceList, textViewItemCount);

        loader.setOnAfterAsync(new AfterAsyncListener() {
            @Override
            public void after(int delay) {

                topSnackMessage.setText(ser);
                topSnackDesc.setVisibility(View.VISIBLE);
                topSnackDesc.setText("Updated Successfully");
                TopSnack.createCustomTopSnack(ExpiredDevicesActivity.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyDifferent"));

            }
        });
        return loader;
    }


}