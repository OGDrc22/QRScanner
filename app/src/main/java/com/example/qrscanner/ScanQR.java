package com.example.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.adapter.DepartmentAdapter;
import com.example.qrscanner.adapter.GadgetsAdapter;
import com.example.qrscanner.models.Assigned_to_User_Model;
//import com.example.qrscanner.options.Data;
import com.example.qrscanner.models.Department;
import com.example.qrscanner.models.Gadgets;
import com.example.qrscanner.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.os.Handler;


public class ScanQR extends AppCompatActivity {

    private EditText assignedTo, deviceModel, datePurchased;
    private TextView qrText, dateExpired, status, availability, chooser, chooserDepartment;
    private GadgetsAdapter gadgetsAdapter;
    private DepartmentAdapter departmentAdapter;
    private CardView saveBtn;
    private PreviewView cameraPreview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private String scannedData, gadgetName;
    private ImageView settings, backBtn, currentActivity, add_newGadget, currentIcon, currentIcon2, imageViewGadget;

    private Gadgets gadgetPosition;

    private ListView listView;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

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
            if (assignedTo.getText().toString().isEmpty() || !assignedTo.getText().toString().isEmpty()) {
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
        chooserDepartment = findViewById(R.id.chooserDepartment);
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

        //TODO WHEN A GADGET DELETED PUT "Unknown"

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
                Utils.calculateExpirationAndStatus(inputDate, dateExpired, status);
            }
        });


        imageViewGadget = findViewById(R.id.imageViewGadget);
        chooser = findViewById(R.id.chooser);
        chooser.setOnClickListener(v -> {
            clickOptionGadgetCategory();
            chooser.setText("Unknown");
        });

        chooserDepartment.setOnClickListener(v -> {
            clickDepartmentCategory();
            Log.d("TAG", "onCreate: Call clickDepCatMethod");
            int itemCount = departmentAdapter.getCount();
            int itemCount2 = getDepartmentCategoryFromDatabase().size();

            Log.d("TAG", "department adapter item count " + itemCount);
            Log.d("TAG", "department db item count " + itemCount2);
            if (itemCount > 0) {
                Log.d("TAG", "department item count " + itemCount);
            }
        });

        // What?
//        List<Gadgets> gadgetList1 = getGadgetsFromDatabase();
//        gadgetsAdapter = new GadgetsAdapter(ScanQR.this, gadgetList1);


        //Icon picker for ic_edit gadget items
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                // Handle the selected image URI
                currentIcon.setImageURI(uri);
                currentIcon2.setImageURI(uri);


                Log.d("PhotoPicker", "Selected URI: " + uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });



        // Save Button
        saveBtn.setOnClickListener(v -> {
//            Toast.makeText(ScanQR.this, "Null pos", Toast.LENGTH_SHORT).show();
            // Check if all fields are filled
            if ((scannedData != null) && !deviceModel.getText().toString().isEmpty() && !datePurchased.getText().toString().isEmpty()) {
                if (assignedTo.getText().toString().isEmpty() || !assignedTo.getText().toString().isEmpty()) {
                    // Proceed with saving data

                    if (gadgetName == null) {
                       gadgetName = "Unknown";
                    }
                    dbHelper.addDevice(scannedData, assignedTo.getText().toString(), chooserDepartment.getText().toString(), gadgetName, deviceModel.getText().toString(), datePurchased.getText().toString(), dateExpired.getText().toString(), status.getText().toString(), availability.getText().toString());


                    // Reset other fields
                    scannedData = null;
                    qrText.setText("");
                    assignedTo.setText("");
                    deviceModel.setText("");
                    datePurchased.setText("");


                    // Toast Saved Success
                    finish();
                }
            } else {
                // Toast "Save Failed", "Please fill up all fields"
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
            // Toast "Permission Denied", "Please allow camera permission"
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

    //GADGET CHOOSER
    private void clickOptionGadgetCategory() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View customView = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_show_option, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));

        // Find the ListView in your custom layout
        listView = customView.findViewById(R.id.list_item_option);

        // Set the adapter for the ListView
        listView.setAdapter(new GadgetsAdapter(ScanQR.this, getGadgetsCategoryFromDatabase()));

        // Set the custom view to the AlertDialog.Builder
        builder.setView(customView);

        // Create and show the AlertDialog
        AlertDialog itemDialogGadgetsCategory = builder.create();
        if (itemDialogGadgetsCategory.getWindow() != null) {
            itemDialogGadgetsCategory.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        itemDialogGadgetsCategory.show();


        add_newGadget = customView.findViewById(R.id.addGadgetBtn);
        add_newGadget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewGadgetDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                if (gadgetPosition != null) {
                    gadgetName = gadgetPosition.getGadgetName();
                    itemDialogGadgetsCategory.dismiss();
                    chooser.setText(gadgetName);
                        int positionNew = position+1;
                        displayGadgetImageInt(ScanQR.this, imageViewGadget, positionNew);

                    Toast.makeText(ScanQR.this, "Selected " + gadgetName, Toast.LENGTH_SHORT).show();
                } else {
                    gadgetName = "Unknown";
                    Toast.makeText(ScanQR.this, "Null pos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                showEditDialog(gadgetPosition);
                gadgetName = gadgetPosition.getGadgetName();
                Toast.makeText(ScanQR.this, gadgetName, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void showEditDialog(final Gadgets gadgets) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_edit_spinner_item, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);

        // Set up the input
        final EditText input = view.findViewById(R.id.editText);
        gadgetName = gadgetPosition.getGadgetName();
        input.setText(gadgetName);
        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);
        final Button actionDelete = view.findViewById(R.id.actionDelete);
        final ImageView iconICPick = view.findViewById(R.id.addIconGadget);
        ImageView imageViewCurrent = view.findViewById(R.id.currentIconA);

        // Set the ImageView resource Based on what user pick
        currentIcon = iconICPick;
        currentIcon2 = imageViewCurrent;

        byte[] gImage = gadgetPosition.getImage();


        final AlertDialog deviceChooserDialog = builder.create();
        if (deviceChooserDialog.getWindow() != null) {
            deviceChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        deviceChooserDialog.show();

        iconICPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        // Set Image to imageViewCurrent whe the gadget is selected
        byte[] imageBytes = gadgetPosition.getImage();
        if (imageBytes != null) {
            // ImageView To Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageViewCurrent.setImageBitmap(bitmap);
        }

        // Set up the buttons
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGadgetName = input.getText().toString();
                byte[] newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);

//                Toast.makeText(ScanQR.this, "Gadget to ic_edit ID:" + gadgets.getId(), Toast.LENGTH_SHORT).show();

                if (newGadgetName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (newGadgetImage == null) {
                        if (gImage != null) {
                            newGadgetImage = gImage;
                        }
                    } else {
                        newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);
                    }
                    dbHelper.updateGadgetCategory(gadgets, newGadgetName, newGadgetImage);
                    updateGadgetList();
                    deviceChooserDialog.dismiss();

                }
            }
        });

        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanQR.this, "Gadget to delete ID:" + gadgets.getId(), Toast.LENGTH_SHORT).show();
                dbHelper.deleteGadgetCategory(gadgets);
                updateGadgetList();
                deviceChooserDialog.dismiss();
                Toast.makeText(ScanQR.this, "Delete Btn Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceChooserDialog.dismiss();
            }
        });
    }


    private void showAddNewGadgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_add_new_dialog, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);

        final ImageView imageView = view.findViewById(R.id.warning);
        imageView.setImageResource(R.drawable.add_icon);
        // Set up the input
        final EditText input = view.findViewById(R.id.editText);
        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);
        final ImageView iconICPick = view.findViewById(R.id.addIconGadget);
        ImageView imageViewCurrent = view.findViewById(R.id.currentIconA);

        // Set the ImageView resource Based on what user pick
        currentIcon = iconICPick;
        currentIcon2 = imageViewCurrent;

        final AlertDialog deviceChooserDialog = builder.create();
        if (deviceChooserDialog.getWindow() != null) {
            deviceChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        deviceChooserDialog.show();

        iconICPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanQR.this, "Add image clicked", Toast.LENGTH_SHORT).show();
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });


        // Set up the buttons
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGadgetName = input.getText().toString();
                byte[] newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);
                
                if (newGadgetName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    if (newGadgetImage == null) {
                     newGadgetImage = Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.device_model);
                    }
                    dbHelper.addGadgetCategory(newGadgetName, newGadgetImage);
                    updateGadgetList();
                    deviceChooserDialog.dismiss();

                }
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceChooserDialog.dismiss();
            }
        });
    }

    private void updateGadgetList() {
        List<Gadgets> gadgetsList = getGadgetsCategoryFromDatabase();
        gadgetsAdapter = new GadgetsAdapter(ScanQR.this, gadgetsList);
        listView.setAdapter(gadgetsAdapter);
        gadgetsAdapter.notifyDataSetChanged();
    }

    private List<Gadgets> getGadgetsCategoryFromDatabase() {
        List<Gadgets> gadgetsList = dbHelper.getAllGadgetsCategory();

        // Add default gadgets if database is empty
        if (gadgetsList.isEmpty()) {
            dbHelper.addGadgetCategory("Unknown", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.ic_unknown_device));
            Log.d("TAG", "getGadgetsCategoryFromDatabase: Add Item 1");
            dbHelper.addGadgetCategory("Laptop", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.laptop_icon));
            dbHelper.addGadgetCategory("Phone", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.mobile_phone_2635));
            dbHelper.addGadgetCategory("Tablet", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.ic_tablet));
            dbHelper.addGadgetCategory("Desktop", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.pc_computer_6840));

            gadgetsList = dbHelper.getAllGadgetsCategory();
        }

        return gadgetsList;
    }

    public void displayGadgetImageInt(Context context, ImageView imageView, int gadgetId) {
        byte[] imageBytes = dbHelper.getGadgetCategoryImageInt(gadgetId);
        if (imageBytes != null) {
            // ImageView To Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(bitmap);
        } else {
            // Set a default image if no image is found
            imageView.setImageResource(R.drawable.device_model);
        }
    }
    //END GADGET CHOOSER


    private void clickDepartmentCategory() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View customView = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_show_option, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));

        // Find the ListView in your custom layout
        listView = customView.findViewById(R.id.list_item_option);

        // Set the adapter for the ListView
        List<Department> departmentList = getDepartmentCategoryFromDatabase();
        departmentAdapter = new DepartmentAdapter(ScanQR.this, departmentList);
        listView.setAdapter(departmentAdapter);

        // Set the custom view to the AlertDialog.Builder
        builder.setView(customView);

        // Create and show the AlertDialog
        AlertDialog itemDialogGadgetsCategory = builder.create();
        if (itemDialogGadgetsCategory.getWindow() != null) {
            itemDialogGadgetsCategory.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        itemDialogGadgetsCategory.show();


        add_newGadget = customView.findViewById(R.id.addGadgetBtn);
        add_newGadget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: clicked on add in department");
                showAddNewDepartmentDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                if (gadgetPosition != null) {
                    gadgetName = gadgetPosition.getGadgetName();
                    itemDialogGadgetsCategory.dismiss();
                    chooserDepartment.setText(gadgetName);
                    int positionNew = position+1;
                    displayGadgetImageInt(ScanQR.this, imageViewGadget, positionNew);

                    Toast.makeText(ScanQR.this, "Selected " + gadgetName, Toast.LENGTH_SHORT).show();
                } else {
                    gadgetName = "Unknown";
                    Toast.makeText(ScanQR.this, "Null pos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                showEditDialog(gadgetPosition);
                gadgetName = gadgetPosition.getGadgetName();
                Toast.makeText(ScanQR.this, gadgetName, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private List<Department> getDepartmentCategoryFromDatabase() {
        List<Department> departmentList = dbHelper.getAllDepartmentCategory();

        // Add default departments if the list is empty
        if (departmentList.isEmpty()) {
            dbHelper.addDepartmentCategory("HR");
            dbHelper.addDepartmentCategory("Finance");
            dbHelper.addDepartmentCategory("IT");
            dbHelper.addDepartmentCategory("Operations");
            departmentList = dbHelper.getAllDepartmentCategory();
        }

        return departmentList;
    }


    private void updateDepartmentList() {
        List<Department> departmentList = getDepartmentCategoryFromDatabase();
        departmentAdapter = new DepartmentAdapter(ScanQR.this, departmentList);
        listView.setAdapter(departmentAdapter);
        departmentAdapter.notifyDataSetChanged();
    }


    private void showAddNewDepartmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_add_new_dialog, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);

        final ImageView imageView = view.findViewById(R.id.warning);
        imageView.setImageResource(R.drawable.add_icon);
        // Set up the input
        final EditText input = view.findViewById(R.id.editText);
        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);
        final ImageView iconICPick = view.findViewById(R.id.addIconGadget);
        ImageView imageViewCurrent = view.findViewById(R.id.currentIconA);

        iconICPick.setVisibility(View.GONE);
        imageViewCurrent.setVisibility(View.GONE);

        final AlertDialog deviceChooserDialog = builder.create();
        if (deviceChooserDialog.getWindow() != null) {
            deviceChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        deviceChooserDialog.show();


        // Set up the buttons
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGadgetName = input.getText().toString();

                if (newGadgetName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    dbHelper.addDepartmentCategory(newGadgetName);
                    updateDepartmentList();
                    deviceChooserDialog.dismiss();

                }
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceChooserDialog.dismiss();
            }
        });
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