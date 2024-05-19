    package com.example.qrscanner;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.cardview.widget.CardView;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.qrscanner.adapter.ItemAdapter;
    import com.example.qrscanner.expiration.ExpirationUtility;
    import com.example.qrscanner.methods.CustomToastMethod;
    import com.example.qrscanner.options.Data;
    import com.example.qrscanner.options.Gadgets;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    public class UpdateData extends AppCompatActivity {

        private EditText assignedTo, department, device, deviceModel, datePurchased;
        private TextView qrText, dateExpired, status, availability, titleTextView;
        private ImageView backBtn, settings, currentActivity;
        private CardView saveBtn, cancelBtn;
        private Spinner spinner;
        private GadgetsAdapter gadgetsAdapter;
        private String gadget;
        private ArrayList<String> serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;
        private DBHelper dbHelper;
        private static final String pattern = "MM/dd/yy";

        private ItemAdapter itemAdapter;
        private RecyclerView recyclerView;

        private CustomToastMethod customToastMethod;

        private void updateAvailabilityStatus() {
            if (!assignedTo.getText().toString().isEmpty()) {
                availability.setText("In Use");
            } else {
                availability.setText("In Stock");
            }
        }

        // Constructor to accept ItemAdapter reference
        public UpdateData() {

        }

        // Method to update RecyclerView data
        public void updateRecyclerViewData() {
            // Update your data here if needed
            // Notify the adapter about the data change
            itemAdapter.notifyDataSetChanged();
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                updateSaveButtonState();
                updateAvailabilityStatus();


            }

        };

        private void updateSaveButtonState() {
            if (!deviceModel.getText().toString().isEmpty() && !datePurchased.getText().toString().isEmpty()) {
                if (assignedTo.getText().toString().isEmpty()) {
                    saveBtn.setEnabled(true);
                } else {
                    saveBtn.setEnabled(true);
                }
            } else {
                saveBtn.setEnabled(false);
            }
        }


        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_update_data);
//            LayoutInflater inflater = LayoutInflater.from(this);;
//            ViewGroup parentViewGroup = findViewById(R.id.main);
//            View infoLayout = inflater.inflate(R.layout.info_layout, parentViewGroup, false);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            customToastMethod = new CustomToastMethod(UpdateData.this);

            currentActivity = findViewById(R.id.currentActivity);
            currentActivity.setImageResource(R.drawable.edit);

            titleTextView = findViewById(R.id.titleTextView);
            titleTextView.setText("Update Data");

            backBtn = findViewById(R.id.backBtn);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomToastMethod customToastMethod = new CustomToastMethod(UpdateData.this);
                    customToastMethod.notify(R.layout.toasty, null, "Canceled", null, null, null);

                    new Handler().postDelayed(() -> {
                        finish();
                    }, 3000);
                }
            });

            settings = findViewById(R.id.settingsIcon);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UpdateData.this, Settings.class);
                    startActivity(intent);
//                    Log.d("TAG", "Clicked Success");
                }
            });



            dbHelper = new DBHelper(this);

            qrText = findViewById(R.id.qrText);


            assignedTo = findViewById(R.id.assignedTo);
            department = findViewById(R.id.department);
            deviceModel = findViewById(R.id.deviceModel);
            datePurchased = findViewById(R.id.textDatePurchased);
            dateExpired = findViewById(R.id.textDateExpired);
            status = findViewById(R.id.status);
            availability = findViewById(R.id.availability);
            saveBtn = findViewById(R.id.saveBtn);
            spinner = findViewById(R.id.spinner);

            ArrayList<Assigned_to_User_Model> deviceList = new ArrayList<>();
            serialNum_id = new ArrayList<>();
            assignedTo_id = new ArrayList<>();
            department_id = new ArrayList<>();
            device_id = new ArrayList<>();
            deviceModel_id = new ArrayList<>();
            datePurchased_id = new ArrayList<>();
            dateExpire_id = new ArrayList<>();
            status_id = new ArrayList<>();
            availability_id = new ArrayList<>();
            itemAdapter = new ItemAdapter(R.layout.info_layout, this, deviceList, serialNum_id, assignedTo_id, department_id, device_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id, null, null);
//            recyclerView.setAdapter(itemAdapter);
            gadgetsAdapter = new GadgetsAdapter(UpdateData.this, Data.getGadgetsList());
            spinner.setAdapter(gadgetsAdapter);


            // Get Value from intent
            qrText.setText(getIntent().getStringExtra("serialNumber"));
            assignedTo.setText(getIntent().getStringExtra("name"));
            department.setText(getIntent().getStringExtra("department"));

            String device = getIntent().getStringExtra("device");
            if (device != null) {
                Log.d("UpdateData", "Received device value from intent: " + device);
                // Get the list of gadgets from the adapter
                List<Gadgets> gadgetsList = gadgetsAdapter.getGadgetsList();
                for (Gadgets gadget : gadgetsList) {
                    Log.d("UpdateData", "Gadget in spinner list: " + gadget.getGadgetName());
                }

                // Iterate through the list of gadgets to find the position of the device
                int position = -1;
                for (int i = 0; i < gadgetsList.size(); i++) {
                    if (gadgetsList.get(i).getGadgetName().equals(device)) {
                        position = i;
                        break;
                    }
                }
                Log.d("UpdateData", "Position found: " + position);

                // Set the selection of the spinner to the found position
                if (position != -1) {
                    spinner.setSelection(position);
                } else {
                    // Handle the case where the device is not found
                    // You may want to display a default selection or handle it in a different way
                    Log.e("UpdateData", "Device not found in spinner list: " + device);
                }
            }
            deviceModel.setText(getIntent().getStringExtra("deviceModel"));
            datePurchased.setText(getIntent().getStringExtra("datePurchased"));
            dateExpired.setText(getIntent().getStringExtra("dateExpired"));
            status.setText(getIntent().getStringExtra("status"));
            availability.setText(getIntent().getStringExtra("availability"));
            updateAvailabilityStatus();

            assignedTo.addTextChangedListener(textWatcher);
            deviceModel.addTextChangedListener(textWatcher);
            datePurchased.addTextChangedListener(textWatcher);
            dateExpired.addTextChangedListener(textWatcher);
            status.addTextChangedListener(textWatcher);


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Gadgets gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                    gadget = gadgetPosition.getGadgetName();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            cancelBtn = findViewById(R.id.cancelBtn);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customToastMethod.notify(R.layout.toasty, null, "Canceled", null, null, null);
                    new Handler().postDelayed(() -> {
                        finish();
                    }, 1500);
                }
            });


            datePurchased.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    // Parse the input date
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    Date inputDate;

                    try {
                        inputDate = dateFormat.parse(datePurchased.getText().toString());
                    } catch (ParseException e) {
                        customToastMethod.notify(R.layout.toasty, null, "Invalid date format. "  + pattern, null, null, null);
                        return;
                    }

                    // Calculate expiration date and set status
                    ExpirationUtility.calculateExpirationAndStatus(inputDate, dateExpired, status);
                }
            });


            saveBtn.setOnClickListener(v -> {
                if (!deviceModel.getText().toString().isEmpty() && !datePurchased.getText().toString().isEmpty()) {
                    if (assignedTo.getText().toString().isEmpty() || !assignedTo.getText().toString().isEmpty()) {
                        Assigned_to_User_Model assigned = new Assigned_to_User_Model();

                        // Proceed with saving data
                        dbHelper.editDevice(getIntent().getStringExtra("serialNumber"),  assignedTo.getText().toString(), department.getText().toString(), gadget, deviceModel.getText().toString(), datePurchased.getText().toString(), dateExpired.getText().toString(), status.getText().toString(), availability.getText().toString());

                        deviceList.add(assigned);

                        customToastMethod.notify(R.layout.toasty, R.drawable.check, "Saved Success", null, null, null);

                        Intent intent = new Intent();
                        intent.putExtra("dataRefreshed", true);
                        setResult(RESULT_OK, intent);

                        new Handler().postDelayed(() -> {
                            finish();
                        }, 1500);

                        itemAdapter.notifyDataSetChanged();
                    }
                } else {
                    customToastMethod.notify(R.layout.toasty, R.drawable.warning_sign, "Save Failed", "Please fill up all fields", null, null);
                }
            });
        }

    }