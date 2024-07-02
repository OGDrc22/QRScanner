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
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class ScanQR extends AppCompatActivity {

    private EditText deviceModel;
    private TextInputEditText qrText, assignedTo,chooserDepartment, chooserDevice, datePurchased, dateExpired, status, availability;
    private TextInputLayout textInputLayoutQRText, textInputLayoutAssignedTo, textInputLayoutDep, textInputLayoutDevice, textInputLayoutDeviceModel, textInputLayoutDatePurchased, textInputLayoutExpired, textInputLayoutStatus;
    private BottomSheetDialog bottomSheetDialog;
    private TextView titleText;
    private GadgetsAdapter gadgetsAdapter;
    private DepartmentAdapter departmentAdapter;
    private CardView saveBtn;
    private PreviewView cameraPreview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private String scannedData, gadgetCategoryName, departmentCategoryName;
    private ImageView settings, backBtn, currentActivity, add_newGadget, currentIcon, currentIcon2, imageViewSave;

    private Gadgets gadgetPosition;
    private Department departmentPosition;

    private ListView listView;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private DBHelper dbHelper;
    private static final String pattern = "MM/dd/yy";

    TextWatcher textWatcherAssignedTo = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!assignedTo.getText().toString().isEmpty()) {
                textInputLayoutAssignedTo.setEndIconDrawable(R.drawable.ic_cancel_24);
            } else if (assignedTo.getText().toString().isEmpty()){
                textInputLayoutAssignedTo.setEndIconDrawable(R.drawable.ic_edit);
            }

            Utils.updateAvailabilityStatus(assignedTo, availability);
        }

    };

    TextWatcher textWatcherDeviceModel = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!deviceModel.getText().toString().isEmpty()) {
                textInputLayoutDeviceModel.setEndIconDrawable(R.drawable.ic_cancel_24);
                textInputLayoutDeviceModel.setError(null);
            } else if (deviceModel.getText().toString().isEmpty()){
                textInputLayoutDeviceModel.setEndIconDrawable(R.drawable.ic_edit);
                textInputLayoutDeviceModel.setHelperText("Required*");
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);


        cameraPreview = findViewById(R.id.cameraPreview);

        bottomSheetDialog = new BottomSheetDialog(ScanQR.this);


        titleText = findViewById(R.id.titleTextView);
        settings = findViewById(R.id.settingsIcon);
        backBtn = findViewById(R.id.backBtn);
        currentActivity = findViewById(R.id.currentActivity);

        titleText.setText("Scan QR Code");

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnShowBottom = findViewById(R.id.showB);
        btnShowBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetDialog();
                bottomSheetDialog.show();
            }
        });

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


        dbHelper = new DBHelper(this);



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
                        openBottomSheetDialog();
                        bottomSheetDialog.show();
                        scannedData = barcode.getRawValue();
                        qrText.setText(scannedData);

                        if (!qrText.getText().toString().isEmpty()) {
                            textInputLayoutQRText.setHelperTextEnabled(false);
                        } else {
                            textInputLayoutQRText.setHelperTextEnabled(true);
                        }

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








    private void openBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        qrText = view.findViewById(R.id.qrText);
        assignedTo = view.findViewById(R.id.assignedTo);
        chooserDepartment = view.findViewById(R.id.chooserDepartment);
        chooserDevice = view.findViewById(R.id.chooserDevice);
        deviceModel = view.findViewById(R.id.deviceModel);
        datePurchased = view.findViewById(R.id.textDatePurchased);
        dateExpired = view.findViewById(R.id.textdateExpired);
        status = view.findViewById(R.id.status);
        availability = view.findViewById(R.id.availability);
        saveBtn = view.findViewById(R.id.saveBtn);

        imageViewSave = view.findViewById(R.id.saveIC);


        textInputLayoutQRText = view.findViewById(R.id.TIL_QRText);
        textInputLayoutAssignedTo = view.findViewById(R.id.TIL_AssignedTo);
        textInputLayoutDep = view.findViewById(R.id.TIl_Department);
        textInputLayoutDevice = view.findViewById(R.id.TIL_Device);
        textInputLayoutDeviceModel = view.findViewById(R.id.TIL_Device_Model);
        textInputLayoutDatePurchased = view.findViewById(R.id.TIL_DatePurchased);
        textInputLayoutExpired = view.findViewById(R.id.TIL_Expiration);
        textInputLayoutStatus = view.findViewById(R.id.TIL_Status);


        assignedTo.addTextChangedListener(textWatcherAssignedTo);
        textInputLayoutAssignedTo.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignedTo.setText("");
            }
        });

        Utils.updateAvailabilityStatus(assignedTo, availability);




        deviceModel.addTextChangedListener(textWatcherDeviceModel);


        textInputLayoutDeviceModel.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceModel.setText("");
            }
        });



        Drawable drawable = textInputLayoutDevice.getStartIconDrawable();
        int parentH = drawable.getMinimumHeight();

        chooserDevice.setOnClickListener(v -> {
            clickOptionGadgetCategory(parentH);
            textInputLayoutDevice.setError(null);
            chooserDevice.setText("Unknown");
            textInputLayoutDevice.setStartIconDrawable(R.drawable.ic_unknown_device);


            textInputLayoutAssignedTo.clearFocus();
            textInputLayoutDeviceModel.clearFocus();

        });



        chooserDepartment.setOnClickListener(v -> {
            clickDepartmentCategory();


            textInputLayoutAssignedTo.clearFocus();
            textInputLayoutDeviceModel.clearFocus();

        });





        datePurchased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textInputLayoutAssignedTo.clearFocus();
                textInputLayoutDeviceModel.clearFocus();

                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .setTheme(R.style.AppDatePicker)
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        String date = new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(selection));
                        datePurchased.setText(date);
                        textInputLayoutDatePurchased.setError(null);
                        Date selectedDate = new Date(selection);
                        Utils.calculateExpirationAndStatus(selectedDate, dateExpired, status);
                    }
                });
                materialDatePicker.show(getSupportFragmentManager(), "date");
            }
        });


        // Save Button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceModel.clearFocus();
                textInputLayoutDeviceModel.clearFocus();

                boolean allFieldsFilled = true;

                if (qrText.getText().toString().isEmpty()) {
                    textInputLayoutQRText.setError("Please fill up");
                    allFieldsFilled = false;
                }

//                if (assignedTo.getText().toString().isEmpty()) {
//                    textInputLayoutAssignedTo.setError("Please fill up");
//                    textInputLayoutAssignedTo.setErrorEnabled(true);
//                    allFieldsFilled = false;
//                }

                if (chooserDepartment.getText().toString().isEmpty()) {
                    textInputLayoutDep.setError("Please fill up");
                    allFieldsFilled = false;
                }

                if (chooserDevice.getText().toString().isEmpty()) {
                    textInputLayoutDevice.setError("Please pick \"Unknown\", if you don't know what type of device is.");
                    allFieldsFilled = false;
                }

                if (deviceModel.getText().toString().isEmpty()) {
                    textInputLayoutDeviceModel.setError("Please fill up");
                    allFieldsFilled = false;
                } else {
                    textInputLayoutDeviceModel.setError(null);
                }

                if (datePurchased.getText().toString().isEmpty()) {
                    textInputLayoutDatePurchased.setError("Please fill up");
                    allFieldsFilled = false;
                }

//                if (dateExpired.getText().toString().isEmpty()) {
//                    textInputLayoutExpired.setError("Please fill up");
//                    allFieldsFilled = false;
//                }
//
//                if (status.getText().toString().isEmpty()) {
//                    textInputLayoutStatus.setError("Please fill up");
//                    allFieldsFilled = false;
//                }

                if (allFieldsFilled) {
                    // Proceed with saving data
                    dbHelper.addDevice(
                            qrText.getText().toString(),
                            assignedTo.getText().toString(),
                            chooserDepartment.getText().toString(),
                            chooserDevice.getText().toString(),
                            deviceModel.getText().toString(),
                            datePurchased.getText().toString(),
                            dateExpired.getText().toString(),
                            status.getText().toString(),
                            availability.getText().toString()
                    );

                    // Reset other fields
                    qrText.setText(null);
                    assignedTo.setText(null);
                    deviceModel.setText(null);
                    datePurchased.setText(null);
                    dateExpired.setText(null);
                    status.setText(null);
                    availability.setText(null);



                    Drawable[] layers = new Drawable[] {
                            imageViewSave.getDrawable(), getResources().getDrawable(R.drawable.saved)
                    };
                    TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                    imageViewSave.setImageDrawable(transitionDrawable);
                    transitionDrawable.startTransition(700);


                    new Handler().postDelayed(() -> {
                        bottomSheetDialog.dismiss();
                    }, 1000);
                } else {
                    Toast.makeText(ScanQR.this, "Please fill up all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Debug
        boolean check = textInputLayoutAssignedTo.getErrorIconDrawable() != null;
        MaterialCardView debug = view.findViewById(R.id.cancelButton);
        debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanQR.this, "Error Icon Visible: " + check, Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.setContentView(view);

        // Ensure the BottomSheetDialog's height adjusts to the content's height
        View parent = (View) view.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        behavior.setPeekHeight(view.getMeasuredHeight());

        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        parent.requestLayout();
    }




//    private void afterTextChanged(TextInputLayout textInputLayout, TextInputEditText textInputEditText) {
//        while (true){
//            if (textInputEditText.getText().toString().isEmpty()) {
//                textInputLayout.setEndIconDrawable(R.drawable.ic_cancel_24);
//            } else {
//                textInputLayout.setEndIconDrawable(R.drawable.ic_edit);
//            }
//        }
//    }







    //GADGET CHOOSER
    private void clickOptionGadgetCategory(int parentHeight) {

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
                    gadgetCategoryName = gadgetPosition.getGadgetCategoryName();
                    itemDialogGadgetsCategory.dismiss();
                    chooserDevice.setText(gadgetCategoryName);
                        int positionNew = position+1;
                        Utils.displayGadgetImageInt(ScanQR.this, dbHelper, textInputLayoutDevice, positionNew, parentHeight);

                    Toast.makeText(ScanQR.this, "Selected " + gadgetCategoryName, Toast.LENGTH_SHORT).show();
                } else {
                    gadgetCategoryName = "Unknown";
                    Toast.makeText(ScanQR.this, "Null pos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                gadgetPosition = (Gadgets) parent.getItemAtPosition(position);
                showEditDialog(gadgetPosition);
                gadgetCategoryName = gadgetPosition.getGadgetCategoryName();
                Toast.makeText(ScanQR.this, gadgetCategoryName, Toast.LENGTH_SHORT).show();
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
        gadgetCategoryName = gadgetPosition.getGadgetCategoryName();
        input.setText(gadgetCategoryName);
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
                String newgadgetCategoryName = input.getText().toString();
                byte[] newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);

//                Toast.makeText(ScanQR.this, "Gadget to ic_edit ID:" + gadgets.getId(), Toast.LENGTH_SHORT).show();

                if (newgadgetCategoryName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (newGadgetImage == null) {
                        if (gImage != null) {
                            newGadgetImage = gImage;
                        }
                    } else {
                        newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);
                    }
                    dbHelper.updateGadgetCategory(gadgets, newgadgetCategoryName, newGadgetImage);
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
                String newGadgetCategoryName = input.getText().toString();
                byte[] newGadgetImage = Utils.imageViewToByte(ScanQR.this, iconICPick);
                
                if (newGadgetCategoryName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    if (newGadgetImage == null) {
                     newGadgetImage = Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.device_model);
                    }
                    dbHelper.addGadgetCategory(newGadgetCategoryName, newGadgetImage);
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
            dbHelper.addGadgetCategory("Phone", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.ic_mobile_phone));
            dbHelper.addGadgetCategory("Tablet", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.ic_tablet));
            dbHelper.addGadgetCategory("Desktop", Utils.getDefaultImageByteArray(ScanQR.this, R.drawable.ic_pc_computer));

            gadgetsList = dbHelper.getAllGadgetsCategory();
        }

        return gadgetsList;
    }



    //END GADGET CHOOSER


    private void clickDepartmentCategory() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View customView = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_show_option, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));

        TextView titleText = customView.findViewById(R.id.titleText);
        titleText.setText("Select Department");

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

                textInputLayoutDep.setError(null);

                departmentPosition = (Department) parent.getItemAtPosition(position);
                if (departmentPosition != null) {
                    departmentCategoryName = departmentPosition.getDepartmentCategoryName();
                    itemDialogGadgetsCategory.dismiss();
                    chooserDepartment.setText(departmentCategoryName);

                    Toast.makeText(ScanQR.this, "Selected " + departmentCategoryName, Toast.LENGTH_SHORT).show();
                } else {
                    departmentCategoryName = "Unknown";
                    Toast.makeText(ScanQR.this, "Null pos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                departmentPosition = (Department) parent.getItemAtPosition(position);
                showEditDepartmentCategoryDialog(departmentPosition);
                gadgetCategoryName = departmentPosition.getDepartmentCategoryName();
                Toast.makeText(ScanQR.this, departmentCategoryName, Toast.LENGTH_SHORT).show();
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
        input.setHint("Department Name");
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
                String newDepartmentCategoryName = input.getText().toString();

                if (newDepartmentCategoryName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    dbHelper.addDepartmentCategory(newDepartmentCategoryName);
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


    private void showEditDepartmentCategoryDialog(final Department department) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQR.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ScanQR.this).inflate(R.layout.layout_edit_spinner_item, (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);

        // Set up the input
        final EditText input = view.findViewById(R.id.editText);
        departmentCategoryName = departmentPosition.getDepartmentCategoryName();
        input.setText(departmentCategoryName);
        final Button actionOK = view.findViewById(R.id.actionOK);
        final Button actionCancel = view.findViewById(R.id.actionCancel);
        final Button actionDelete = view.findViewById(R.id.actionDelete);
        final ImageView iconICPick = view.findViewById(R.id.addIconGadget);
        ImageView imageViewCurrent = view.findViewById(R.id.currentIconA);
        iconICPick.setVisibility(View.GONE);
        imageViewCurrent.setVisibility(View.GONE);


        final AlertDialog departmentChooserDialog = builder.create();
        if (departmentChooserDialog.getWindow() != null) {
            departmentChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        departmentChooserDialog.show();

        iconICPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        // Set up the buttons
        actionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int departmentId = departmentPosition.getDepartmentCategoryId();
                String newDepartmentCategoryName = input.getText().toString();

                if (newDepartmentCategoryName.isEmpty()) {
                    Toast.makeText(ScanQR.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {

                    dbHelper.editDepartmentCategory(departmentId, newDepartmentCategoryName);
                    updateDepartmentList();
                    departmentChooserDialog.dismiss();

                }
            }
        });

        actionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScanQR.this, "Gadget to delete ID:" + department.getDepartmentCategoryId(), Toast.LENGTH_SHORT).show();
                dbHelper.deleteDepartmentCategory(department);
                updateDepartmentList();
                departmentChooserDialog.dismiss();
                Toast.makeText(ScanQR.this, "Delete Btn Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        actionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                departmentChooserDialog.dismiss();
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