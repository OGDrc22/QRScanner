package com.example.qrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrscanner.expiration.ExpirationUtility;
import com.example.qrscanner.options.Data;
import com.example.qrscanner.options.Gadgets;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ScanQR extends AppCompatActivity {

        private EditText assignedTo, department, deviceModel, datePurchased;
        private TextView qrText, dateExpired, status, availability, device1;
        private Spinner spinner;
        private GadgetsAdapter gadgetsAdapter;
        private Button getBtn;
        private CardView saveBtn;
        private PreviewView cameraPreview;
        private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
        private String scannedData, gadget;
        private ImageView settings, backBtn, currentActivity;

        private DBHelper dbHelper;
        private static final String pattern = "MM/dd/yy";

        private void updateAvailabilityStatus() {
            if (!assignedTo.getText().toString().isEmpty()) {
                availability.setText("In Use");
            } else {
                availability.setText("In Stock");
            }
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

        // Function to update the state of the save button
        private void updateSaveButtonState() {
            if ((scannedData != null) && !deviceModel.getText().toString().isEmpty() && !datePurchased.getText().toString().isEmpty()) {
                if (assignedTo.getText().toString().isEmpty()) {
                    saveBtn.setEnabled(true);
                } else {
                    saveBtn.setEnabled(true);
                }
            } else {
                saveBtn.setEnabled(false);
            }
        }



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_qr);

            assignedTo = findViewById(R.id.assignedTo);
            department = findViewById(R.id.department);
            deviceModel = findViewById(R.id.deviceModel);
            datePurchased = findViewById(R.id.textDatePurchased);
            dateExpired = findViewById(R.id.textdateExpired);
            status = findViewById(R.id.status);
            availability = findViewById(R.id.availability);
            saveBtn = findViewById(R.id.saveBtn);
            qrText = findViewById(R.id.qrText);
            cameraPreview = findViewById(R.id.cameraPreview);
            settings = findViewById(R.id.settingsIcon);
            backBtn = findViewById(R.id.backBtn);
            currentActivity = findViewById(R.id.currentActivity);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

//            For Debugging
//            getBtn = findViewById(R.id.getBtn);
//            device1 = findViewById(R.id.device1);
//            getBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    device1.setText(gadget);
//                }
//            });

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ScanQR.this, Settings.class);
                    startActivity(intent);
                    Log.d("TAG", "Clicked Success");
                }
            });

            currentActivity.setImageResource(R.drawable.ic_scan);

            if (ContextCompat.checkSelfPermission(ScanQR.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                ActivityCompat.requestPermissions(ScanQR.this, new String[]{Manifest.permission.CAMERA}, 101);
            }

            updateAvailabilityStatus();

            dbHelper = new DBHelper(this);

            assignedTo.addTextChangedListener(textWatcher);
            deviceModel.addTextChangedListener(textWatcher);
            datePurchased.addTextChangedListener(textWatcher);
            dateExpired.addTextChangedListener(textWatcher);
            status.addTextChangedListener(textWatcher);

            datePurchased.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    // Parse the input date
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    Date inputDate;

                    try {
                        inputDate = dateFormat.parse(datePurchased.getText().toString());
                    } catch (ParseException e) {
                        Toast.makeText(ScanQR.this, "Invalid date format. " + pattern, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Calculate expiration date and set status
                    ExpirationUtility.calculateExpirationAndStatus(inputDate, dateExpired, status);
                }
            });

            spinner = findViewById(R.id.spinner);

            gadgetsAdapter = new GadgetsAdapter(ScanQR.this, Data.getGadgetsList());
            spinner.setAdapter(gadgetsAdapter);

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

            // Save Button
            saveBtn.setOnClickListener(v -> {
                // Check if all fields are filled
                if ((scannedData != null) && !deviceModel.getText().toString().isEmpty() && !datePurchased.getText().toString().isEmpty()) {
                    if (assignedTo.getText().toString().isEmpty() || !assignedTo.getText().toString().isEmpty()) {
                        // Proceed with saving data
                        dbHelper.addDevice(scannedData, assignedTo.getText().toString(), department.getText().toString(), gadget, deviceModel.getText().toString(), datePurchased.getText().toString(), dateExpired.getText().toString(), status.getText().toString(), availability.getText().toString());


                        // Reset other fields
                        scannedData = null;
                        qrText.setText("");
                        assignedTo.setText("");
                        department.setText("");
                        deviceModel.setText("");
                        datePurchased.setText("");
                        Toast.makeText(ScanQR.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(ScanQR.this, "No QR data scanned", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void init() {
            cameraProviderListenableFuture = ProcessCameraProvider.getInstance(ScanQR.this);

            cameraProviderListenableFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, ContextCompat.getMainExecutor(ScanQR.this));
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(ScanQR.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        private void bindImageAnalysis(ProcessCameraProvider processCameraProvider) {
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetResolution(new Size(1280, 1280))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ScanQR.this), image -> {
                Image mediaImage = image.getImage();

                if (mediaImage != null) {
                    InputImage image2 = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                    BarcodeScanner scanner = BarcodeScanning.getClient();
                    Task<List<Barcode>> result = scanner.process(image2);
                    result.addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            scannedData = barcode.getRawValue();
                            qrText.setText(scannedData);
                        }

                        ArrayList<Assigned_to_User_Model> data = dbHelper.fetchDevice();
                        image.close();
                        mediaImage.close();
                    });
                }
            });

            Preview preview = new Preview.Builder().build();
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
            preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
        }
}



//    // Assuming you have the scanned content as 'qrContent'
//    String[] parts = qrContent.split(",");
//    String serialNumber = parts[0].trim();
//    String name = parts[1].trim();
//
//    // Insert into the database
//    SQLiteDatabase db = getWritableDatabase();
//    ContentValues values = new ContentValues();
//    values.put(KEY_SERIAL_NUMBER, serialNumber);
//    values.put(KEY_NAME, name);
//    long newRowId = db.insert(TABLE_NAME, null, values);