package com.example.qrscanner.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.utils.AfterAsyncListener;
import com.example.qrscanner.utils.FilteredDataLoader;
import com.example.qrscanner.utils.Utils;

import java.util.ArrayList;

public class GadgetTypeActivity extends AppCompatActivity implements ItemAdapter.OnDeleteClickListener {

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
    private LinearLayout main, cardViewContent;

    private static View topSnackView;
    private static ImageView topSnack_icon;
    private static TextView topSnackMessage;
    private static TextView topSnackDesc;

    private String deviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gadget_type);
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

        Intent intent = getIntent();
        deviceType = intent.getStringExtra("deviceType");
        byte[] byteArray = intent.getByteArrayExtra("img");
        Log.d("GadgetsTypeActivity", "onCreate: " + deviceType);

        FilteredDataLoader.DeviceFilteredDataLoader filter = (FilteredDataLoader.DeviceFilteredDataLoader) new FilteredDataLoader.DeviceFilteredDataLoader(GadgetTypeActivity.this, dbHelper, main, adapter, deviceType.toLowerCase(), deviceList, textViewItemCount);


        textViewInfo = findViewById(R.id.titleTextView);
//        String title = deviceType.substring(0, 1).toUpperCase() + deviceType.substring(1);
        textViewInfo.setText(deviceType);
        currentActivity = findViewById(R.id.currentActivity);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Drawable drawable = new BitmapDrawable(bitmap);
        currentActivity.setImageDrawable(drawable);

        constraintLayoutDeleteAll = findViewById(R.id.constraintDeleteAll);
        constraintLayoutDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper != null) {
                    if (!filter.isEmpty()) {;
                        Utils.deleteDataByDeviceType(GadgetTypeActivity.this, deviceType, adapter, main);
                    } else if (adapter.getItemCount() == 0) {
                        getView(GadgetTypeActivity.this);
                        topSnack_icon.setImageResource(R.drawable.warning_sign);
                        String emptyText = deviceType + " is already empty.";
                        topSnackMessage.setText(emptyText);
                        TopSnack.createCustomTopSnack(GadgetTypeActivity.this, main, topSnackView, null, null, true, "up");
                    } else {
                        Log.d("Laptop", "onClick: adapter" + adapter.getItemCount());
                    }
                }
            }
        });

        filter.execute();

    }


    private void onEditClick(int position) {
    }

    @Override
    public void onDeleteClick(int position) {
    }

    private static void getView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
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

        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            getView(GadgetTypeActivity.this);
            if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
                topSnack_icon.setImageResource(R.drawable.qr_icon_48);
                int getColorLight = ContextCompat.getColor(GadgetTypeActivity.this, R.color.txtHeaderLight);
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
                TopSnack.createCustomTopSnack(GadgetTypeActivity.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyIdentical"));

            } else if (resB != null && resB.equals(keyDifferent)) {

                FilteredDataLoader.DeviceFilteredDataLoader loader = getItemAdapterDataLoader(data, ser);

                loader.execute();

            } else {
                Log.d("result", "onActivityResult: resA & resB is null" );
            }
        }
    }

    //    After the Item data has change, show this Notification
    private @NonNull FilteredDataLoader.DeviceFilteredDataLoader getItemAdapterDataLoader(@NonNull Intent data, String ser) {
        FilteredDataLoader.DeviceFilteredDataLoader loader = new FilteredDataLoader.DeviceFilteredDataLoader(GadgetTypeActivity.this, dbHelper, main, adapter, deviceType.toLowerCase(), deviceList, textViewItemCount);

        loader.setOnAfterAsync(new AfterAsyncListener() {
            @Override
            public void after(int delay) {

                topSnackMessage.setText(ser);
                topSnackDesc.setVisibility(View.VISIBLE);
                String updateSuccess = "Updated Successfully";
                topSnackDesc.setText(updateSuccess);
                TopSnack.createCustomTopSnack(GadgetTypeActivity.this, main, topSnackView, null, null, true, "up");
                Log.d("TAG", "onActivityResult: " + data.getStringExtra("keyDifferent"));

            }
        });
        return loader;
    }

}