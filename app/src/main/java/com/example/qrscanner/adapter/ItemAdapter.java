
package com.example.qrscanner.adapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.models.ItemModel;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.activities.UpdateDataActivity;
import com.example.qrscanner.utils.DeleteSelected;
import com.example.qrscanner.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private String TAG = "ItemAdapter";

    public static final String EXTRA_POSITION = "com.example.qrscanner.adapter.EXTRA_POSITION";
    private static final int YOUR_REQUEST_CODE = 1;
    private static final Logger log = LogManager.getLogger(ItemAdapter.class);
    private int itemLayoutId;
    private Context context;
    private ArrayList<ItemModel> originalData;
    private ArrayList<ItemModel> filteredData;

    private ArrayList serialNum_id, assignedTo_id, department, device, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private ArrayList<ItemModel> deviceList;

    //    public ImageView topUser, rightExpiration, rightIndicator, imgScan;
    private ViewGroup.MarginLayoutParams layoutParams;

    private int position;
    ItemModel data;

    SimpleDateFormat dateFormat;
    Date inputDate;

    private DBHelper dbHelper;

    private int animationDuration = 300;

    private ArrayList imgScanH = new ArrayList();
    private ArrayList imgScanW = new ArrayList();

    private static int imageScanH;
    private static int imageScanW;

    private String stsExtra;

    private ArrayList<ItemModel> selectedItems = new ArrayList<>();
    private boolean multiSelectEnabled = false;
//    private boolean isExpanded;

    private View main;

    public ItemAdapter() {
    }


    // Define an interface for delete action
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    private OnDeleteClickListener deleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(int position);
    }
    private OnEditClickListener editClickListener;

    public void clearItems() {
        deviceList.clear();
    }

    public void editItem(Context context, int position) {
        ItemModel device = deviceList.get(position);
        Intent intent = new Intent(context, UpdateDataActivity.class); // Use context instead of this
        intent.putExtra(EXTRA_POSITION, position);
//        intent.putExtra("device", String.valueOf(device));
        intent.putExtra("serialNumber", String.valueOf(device.getSerialNumber()));
        intent.putExtra("name", String.valueOf(device.getUserName()));
        intent.putExtra("department", String.valueOf(device.getDepartment()));
        intent.putExtra("deviceType", String.valueOf(device.getDeviceType()));
        byte[] byteArray = data.getImage();
        intent.putExtra("deviceTypeImg", byteArray);
        intent.putExtra("deviceModel", String.valueOf(device.getDeviceBrand()));
        intent.putExtra("datePurchased", String.valueOf(device.getDatePurchased()));
        intent.putExtra("dateExpired", String.valueOf(device.getDateExpired()));
        intent.putExtra("status", stsExtra);
        intent.putExtra("availability", String.valueOf(device.getAvailability()));
        ((Activity) context).startActivityForResult(intent, YOUR_REQUEST_CODE); // Use context to start activity
        notifyItemChanged(position);
    }


    public ItemAdapter(int itemLayoutId, Context context, View main, ArrayList<ItemModel> deviceList, ArrayList serialNum_id, ArrayList assignedTo_id, ArrayList department_id, ArrayList device_id, ArrayList deviceModel_id, ArrayList datePurchased_id, ArrayList dateExpire_id, ArrayList status_id, ArrayList availability_id, OnDeleteClickListener onDeleteClickListener, OnEditClickListener onEditClickListener) {
        this.filteredData = new ArrayList<>(deviceList);
        this.originalData = new ArrayList<>(deviceList);
        this.editClickListener = onEditClickListener;
        this.deleteClickListener = onDeleteClickListener;
        this.itemLayoutId = itemLayoutId;
        this.context = context;
        this.deviceList = deviceList;
        this.serialNum_id = serialNum_id;
        this.assignedTo_id = assignedTo_id;
        this.department = department_id;
        this.device = device_id;
        this.deviceModel_id = deviceModel_id;
        this.datePurchased_id = datePurchased_id;
        this.dateExpire_id = dateExpire_id;
        this.status_id = status_id;
        this.availability_id = availability_id;
        this.main = main;
    }

    public ItemAdapter(int itemLayoutId, Context context, View main, ArrayList<ItemModel> deviceList, ArrayList serialNum_id, ArrayList assignedTo_id, ArrayList department_id, ArrayList device_id, ArrayList deviceModel_id, ArrayList datePurchased_id, ArrayList dateExpire_id, ArrayList status_id, ArrayList availability_id) {
        this.filteredData = new ArrayList<>(deviceList);
        this.originalData = new ArrayList<>(deviceList);
        this.itemLayoutId = itemLayoutId;
        this.context = context;
        this.deviceList = deviceList;
        this.serialNum_id = serialNum_id;
        this.assignedTo_id = assignedTo_id;
        this.department = department_id;
        this.device = device_id;
        this.deviceModel_id = deviceModel_id;
        this.datePurchased_id = datePurchased_id;
        this.dateExpire_id = dateExpire_id;
        this.status_id = status_id;
        this.availability_id = availability_id;
        this.main = main;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        data = deviceList.get(position);

        viewHolder.serialNum_tv.setText(String.valueOf(data.getSerialNumber()));
        viewHolder.textHolderAssignedTo.setText(String.valueOf(data.getUserName()));
        viewHolder.textHolderDepartment.setText(String.valueOf(data.getDepartment()));
        viewHolder.textHolderDeviceType.setText(data.getDeviceType());
        viewHolder.textHolderDeviceModel.setText(String.valueOf(data.getDeviceBrand()));
        viewHolder.textHolderDatePur.setText(String.valueOf(data.getDatePurchased()));

        viewHolder.subHeader.setText(data.getSerialNumber());

        if (data.getUserName().isEmpty()) {
            viewHolder.imgScan.setImageResource(R.drawable.qr_icon_48);
            viewHolder.topUserF.setVisibility(View.VISIBLE);
            viewHolder.headerF.setVisibility(View.GONE);
            viewHolder.textHolderAvailability.setText("In Stock");

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(viewHolder.constraintHolder);
            constraintSet.setVerticalBias(R.id.subHeaderF, 0.5f);
            constraintSet.applyTo(viewHolder.constraintHolder);

            int color = ContextCompat.getColor(context, R.color.txtHeader);
            int colorInDarkMode = ContextCompat.getColor(context, R.color.txtHeaderLight);
            if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                viewHolder.subHeader.setTextColor(colorInDarkMode);
            } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                viewHolder.subHeader.setTextColor(color);
            }
            viewHolder.subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewHolder.textSize_for_header);

            viewHolder.serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewHolder.textSize_for_textHolderAssigned);

        } else {
            viewHolder.imgScan.setImageResource(R.drawable.user_bulk_48);
            viewHolder.serialNum_tv.setText(data.getUserName());
            viewHolder.topUserF.setVisibility(View.GONE);
            viewHolder.headerF.setVisibility(View.VISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(viewHolder.constraintHolder);

            constraintSet.setVerticalBias(R.id.subHeaderF, 0.8f);

            constraintSet.applyTo(viewHolder.constraintHolder);

            int color2 = ContextCompat.getColor(this.context, R.color.txtSubHeader);
            int colorInDarkMode2 = ContextCompat.getColor(this.context, R.color.txtSubHeaderLight);
            if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                viewHolder.subHeader.setTextColor(colorInDarkMode2);
            } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                viewHolder.subHeader.setTextColor(color2);
            }
            viewHolder.subHeader.setTextSize(0, viewHolder.textSize_for_textHolderAssigned);
        }


        // Call calculate ExpirationAndStatus method
        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
////            Date inputDate = dateFormat.parse(data.getDatePurchased());
            dateFormat = new SimpleDateFormat("MM/dd/yy");
            inputDate = dateFormat.parse(data.getDatePurchased());
            Utils.calculateExpirationAndStatus(inputDate, viewHolder.textHolderDateExpired, viewHolder.textHolderStatus);

            // Get the status text to determine visibility
            stsExtra = viewHolder.textHolderStatus.getText().toString();

            // Set visibility of indicator based on status
            int clExpired = ContextCompat.getColor(context, R.color.clExpired);
            int clLight = ContextCompat.getColor(context, R.color.itemBg);
            int clDark = ContextCompat.getColor(context, R.color.itemBg2);
            int clGreen = ContextCompat.getColor(context, R.color.primary);
            int clRed = ContextCompat.getColor(context, R.color.clError);
            if (stsExtra.equals("Fresh")) {
                viewHolder.topExpiration.setVisibility(View.GONE);
                viewHolder.textHolderStatus.setTextColor(clGreen);
                if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    viewHolder.cardViewMain.setCardBackgroundColor(clDark);
                } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    viewHolder.cardViewMain.setCardBackgroundColor(clLight);
                }
//                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(viewHolder.imgVwStatus, "rotation", 0.0f, 180.0f);
//                rotationAnimator.setDuration(0);
//                rotationAnimator.start();
            } else {
                viewHolder.topExpiration.setVisibility(View.VISIBLE);
                viewHolder.textHolderStatus.setTextColor(clRed);

            }

            if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                viewHolder.topExpiration.setImageResource(R.drawable.ic_expiration_light);
            } else {
                viewHolder.topExpiration.setImageResource(R.drawable.ic_expiration);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (Utils.calculateExpiration(inputDate, "For Refresh")){
            ObjectAnimator rotationAnimator2 = ObjectAnimator.ofFloat(viewHolder.imgVwStatus, "rotation", 0.0f, 180.0f);
            rotationAnimator2.setDuration(1000);
            rotationAnimator2.setRepeatCount(ValueAnimator.INFINITE);
            rotationAnimator2.start();
        }
        if (Utils.calculateExpiration(inputDate, "Fresh")) {
            ObjectAnimator rotationAnimator2 = ObjectAnimator.ofFloat(viewHolder.imgVwStatus, "rotation", 0.0f, 0.0001f);
            rotationAnimator2.setDuration(1000);
            rotationAnimator2.setRepeatCount(ValueAnimator.INFINITE);
            rotationAnimator2.start();
        }
        viewHolder.textHolderAvailability.setText(String.valueOf(data.getAvailability()));

//        Delete Button
        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteClickListener != null) {
                    if (!multiSelectEnabled) {
                        showDeleteDialog(context, position);
                    }
                }
            }
        });

//        Edit Button
        viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editClickListener != null) {
                    if (!multiSelectEnabled) {
                        editItem(context, position);
                    }
                }
            }
        });


        if (String.valueOf(data.getDeviceType()).equals(data.getDeviceType())) {
            if (data.image != null){
                // Convert byte array to Bitmap/int
                byte[] imageBytes = data.getImage();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                viewHolder.deviceTypeIC.setImageBitmap(bitmap);
                viewHolder.deviceModelIC.setImageBitmap(bitmap);
            } else {
//                Log.d(TAG, "onBindViewviewHolder: Null Image");
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                viewHolder.deviceModelIC.setImageResource(R.drawable.ic_unknown_device_light);
                viewHolder.deviceTypeIC.setVisibility(View.GONE);
            } else {
                viewHolder.deviceModelIC.setImageResource(R.drawable.ic_unknown_device);
                viewHolder.deviceTypeIC.setVisibility(View.GONE);
            }
        }


        if (data.isSelected()) {
            viewHolder.cardViewMain.setStrokeColor(ContextCompat.getColor(context, R.color.primary));
            viewHolder.cardViewMain.setStrokeWidth(2);
            viewHolder.selected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cardViewMain.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
            viewHolder.cardViewMain.setStrokeWidth(2);
            viewHolder.selected.setVisibility(View.GONE);
        }

        if (data.isExpanded()) {
            viewHolder.setVisible();
        } else {
            viewHolder.setGone();
        }

        Utils.smoothTransition(viewHolder.actions, animationDuration);
        Utils.smoothTransition(viewHolder.other, animationDuration);
        Utils.smoothTransition(viewHolder.imgScan_Frame, animationDuration);


    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setDeviceList(ArrayList<ItemModel> deviceList) {
        this.deviceList = deviceList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ConstraintLayout constraintHolder, constraintLayoutDepartment;
        LinearLayout imgScan_Frame, linearLayout06;
        TextView txtSN, serialNum_tv, textHolderAssignedTo, textHolderDepartment, textHolderDeviceModel, textHolderDatePur, textHolderDateExpired, textHolderStatus, textHolderAvailability, textHolderDeviceType, subHeader;
        TextView hintAssignedTo, hintDepartment, hintDeviceType, hintDeviceModel, hintDatePurchased, hintExpirationDate, hintAvailability, hintStatus;
        ConstraintLayout actions, other;
        ImageView imgScan, imgVwDepartment, imgVwStatus, imgVwDatePur, imgVwDateExpired, imgVwAvailability, topExpiration, deviceTypeIC, deviceModelIC, dropDownArrow, imageView3, connector, connector2, connector3, connector4, connector5, connector6, connector7;
        CardView editBtn, deleteBtn;
        MaterialCardView cardViewMain;

        View selected;

        FrameLayout topUserF, topExpirationF, headerF, subHeaderF, textSN_F;

        final float textSize_for_textHolderAssigned;
        final float textSize_for_header;

        int imgScan_Frame_paddingStart;
        int imgScan_Frame_paddingTop;
        int imgScan_Frame_paddingBottom;
        int imgScan_Frame_paddingEnd;

        int topUser_marginEnd;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dbHelper = new DBHelper(context);

            cardViewMain = itemView.findViewById(R.id.cardViewMain);

            // TEXT VIEWS
            serialNum_tv = itemView.findViewById(R.id.sn);
            textHolderAssignedTo = itemView.findViewById(R.id.textHolderAssignedTo);

            textHolderDepartment = itemView.findViewById(R.id.textHolderDepartment);
            textHolderDeviceType = itemView.findViewById(R.id.textHolderDeviceType);
            textHolderDeviceModel = itemView.findViewById(R.id.textHolderDeviceModel);
            textHolderDatePur = itemView.findViewById(R.id.textHolderDatePur);
            textHolderDateExpired = itemView.findViewById(R.id.textHolderDateExpired);

            // ICONS
            imgScan = itemView.findViewById(R.id.imageViewScan);
            textHolderStatus = itemView.findViewById(R.id.textHolderStatus);
            imgVwDepartment = itemView.findViewById(R.id.imgVwDepartment);
            imgVwDatePur = itemView.findViewById(R.id.imgVwDatePur);
            imgVwDateExpired = itemView.findViewById(R.id.imgVwDateExpired);
            imgVwStatus = itemView.findViewById(R.id.imgVwStatus);
            imgVwAvailability = itemView.findViewById(R.id.imgVwAvailability);
            deviceTypeIC = itemView.findViewById(R.id.deviceTypeIC);
            deviceModelIC = itemView.findViewById(R.id.deviceModelIC);
            topExpiration = itemView.findViewById(R.id.topExpiration);

            imageView3 = itemView.findViewById(R.id.assignedToIC);
            textHolderAvailability = itemView.findViewById(R.id.textHolderAvailability);
            dropDownArrow = itemView.findViewById(R.id.dropDownArrow);
            connector = itemView.findViewById(R.id.connector);
            connector2 = itemView.findViewById(R.id.connector2);
            connector3 = itemView.findViewById(R.id.connector3);
            connector4 = itemView.findViewById(R.id.connector4);
            connector5 = itemView.findViewById(R.id.connector5);
            connector6 = itemView.findViewById(R.id.connector6);
            connector7 = itemView.findViewById(R.id.connector7);


            hintAssignedTo = itemView.findViewById(R.id.hintAssignedTo);
            hintDepartment = itemView.findViewById(R.id.hintDepartment);
            hintDeviceType = itemView.findViewById(R.id.hintDeviceType);
            hintDeviceModel = itemView.findViewById(R.id.hintDeviceModel);
            hintDatePurchased = itemView.findViewById(R.id.hintDatePurchased);
            hintExpirationDate = itemView.findViewById(R.id.hintExpirationDate);
            hintAvailability = itemView.findViewById(R.id.hintAvailability);
            hintStatus = itemView.findViewById(R.id.hintStatus);

            txtSN = itemView.findViewById(R.id.textSN);

            subHeader = itemView.findViewById(R.id.subHeader);

            selected = itemView.findViewById(R.id.selected);


            editBtn =  itemView.findViewById(R.id.editBtn);
            deleteBtn =  itemView.findViewById(R.id.deleteBtn);

            actions = itemView.findViewById(R.id.actions);
            other = itemView.findViewById(R.id.other);

            // Get the layout parameters of the view
            layoutParams = (ViewGroup.MarginLayoutParams) imgScan.getLayoutParams();

            imgScan_Frame = itemView.findViewById(R.id.imgScan_Frame);
            linearLayout06 = itemView.findViewById(R.id.linearLayout6);


            constraintHolder = itemView.findViewById(R.id.constraintHolder);
            constraintLayoutDepartment = itemView.findViewById(R.id.constraintDepartment);
            topUserF = itemView.findViewById(R.id.topUserF);
            topExpirationF = itemView.findViewById(R.id.topExpirationF);

            headerF = itemView.findViewById(R.id.headerF);
            subHeaderF = itemView.findViewById(R.id.subHeaderF);
            textSN_F = itemView.findViewById(R.id.textSN_F);

            textSize_for_textHolderAssigned =  textHolderAssignedTo.getTextSize();
            textSize_for_header = serialNum_tv.getTextSize();

            imgScan_Frame_paddingStart = imgScan_Frame.getPaddingStart();
            imgScan_Frame_paddingTop = imgScan_Frame.getPaddingTop();
            imgScan_Frame_paddingBottom = imgScan_Frame.getPaddingBottom();
            imgScan_Frame_paddingEnd = imgScan_Frame.getPaddingEnd();

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) topUserF.getLayoutParams();
            topUser_marginEnd = lp.rightMargin;

            // ** ---- Use ArrayList to store the original size of "imgScan" to be able to modify its dimension ---- ** //
            int height = imgScan.getDrawable().getIntrinsicHeight();
            imgScanH.add(height);
            int width = imgScan.getDrawable().getIntrinsicWidth();
            imgScanW.add(width);

            imageScanH = (int) imgScanH.get(imgScanH.size() - 1);
            imageScanW = (int) imgScanW.get(imgScanW.size() - 1);

//            Log.d(TAG, "A Values: H=" + imageScanH + " W=" + imageScanW);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public boolean onLongClick(View v) {

//            checkIfShouldBeExpanded();
            multiSelectEnabled = true;
            position = getAdapterPosition();
            data = deviceList.get(position);

            Log.d(TAG, "onLongClick: isExpanded " + data.isExpanded());

            if (position != RecyclerView.NO_POSITION) {
                toggleSelection(position);
                showSelectionOption();
            }


            return true;
        }


        @Override
        public void onClick(View v) {

            // Debugging
            // Log.d(TAG, "getSerial: " + data.getSerialNumber());
            // Log.d(TAG, "getPosition: " + position);

//            Log.d(TAG, "getName: " + data.getSerialNumber());

            position = getAdapterPosition();

            if (multiSelectEnabled) {
                toggleSelection(position);
                refreshSelectionOption();
                Log.d(TAG, "onClick: multi-select?: " + multiSelectEnabled);
            } else {

//                Log.d(TAG, "onClick: SubHeader TextSZ " + textSize_for_textHolderAssigned);
//                Log.d(TAG, "onClick: Header TextSZ " + textSize_for_header);

                Utils.smoothTransition(other, animationDuration);

                data = deviceList.get(position);

                data.setExpanded(!data.isExpanded());

                notifyItemChanged(position);

            }
        }

        private void setVisible() {

            String input = data.getDatePurchased();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
            Date inputDate = null;
            try {
                inputDate = simpleDateFormat.parse(input);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

//            Log.d(TAG, "otherInfo is VISIBLE");

            actions.setVisibility(View.VISIBLE); // Set ic_edit and delete to VISIBLE
            other.setVisibility(View.VISIBLE);
            textSN_F.setVisibility(View.VISIBLE);
            txtSN.setVisibility(View.VISIBLE);

            Utils.rotateUp(dropDownArrow);

            if (!data.getUserName().isEmpty()) {

                imgScan.setImageResource(R.drawable.qr_icon_48);
                serialNum_tv.setVisibility(View.GONE); // Set Gone For Name

                int color = ContextCompat.getColor(context, R.color.txtHeader);
                int colorInDarkMode = ContextCompat.getColor(context, R.color.txtHeaderLight);
                if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    subHeader.setTextColor(colorInDarkMode);
                } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    subHeader.setTextColor(color);
                }
                subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_header);

                serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_textHolderAssigned);

            }

            Log.d(TAG, "B Values: H=" + imageScanH + " W=" + imageScanW);

        }

        private void setGone() {

//            Log.d(TAG, "setGone: called");

            String input = data.getDatePurchased();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
            Date inputDate = null;
            try {
                inputDate = simpleDateFormat.parse(input);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            actions.setVisibility(View.GONE);
            other.setVisibility(View.GONE);
            textSN_F.setVisibility(View.GONE);
            serialNum_tv.setVisibility(View.VISIBLE);

            Utils.rotateDown(dropDownArrow);

            if (!data.getUserName().isEmpty()) {

                imgScan.setImageResource(R.drawable.user_bulk_48);

                subHeader.setVisibility(View.VISIBLE);

                int color2 = ContextCompat.getColor(ItemAdapter.this.context, R.color.txtSubHeader);
                int colorInDarkMode2 = ContextCompat.getColor(ItemAdapter.this.context, R.color.txtSubHeaderLight);
                if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    this.subHeader.setTextColor(colorInDarkMode2);
                } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    this.subHeader.setTextColor(color2);
                }
//                Log.d(TAG, "Collapsed: Before Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());
                serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_header);
//                Log.d(TAG, "Collapsed: After Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());


            } else {
                imgScan.setImageResource(R.drawable.qr_icon_48);

                int color = ContextCompat.getColor(context, R.color.txtHeader);
                int colorInDarkMode = ContextCompat.getColor(context, R.color.txtHeaderLight);
                if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    subHeader.setTextColor(colorInDarkMode);
                } else if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    subHeader.setTextColor(color);
                }
                subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_header);

                serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_textHolderAssigned);
            }

        }

    }




    private void toggleSelection(int position) {
        data = deviceList.get(position);
        data.setSelected(!data.isSelected());

        if (data.isSelected()) {
            selectedItems.add(data);
            Log.d(TAG, "Item selected: " + data.getSerialNumber());
        } else {
            selectedItems.remove(data);
            Log.d(TAG, "Item deselected: " + data.getSerialNumber());
        }

        // Update selection count and UI in the parent activity
        updateSelectionCount();

        // Refresh the item view to reflect selection changes
        notifyItemChanged(position);
    }


    public void updateSelectionCount() {
        Log.d(TAG, "updateSelectionCount: called");
        if (context instanceof Activity) {
            int selectedCount = getSelectedCount();
            Activity activity = (Activity) context;

            View includedLayout = activity.findViewById(R.id.nav_bar_id);

            // Then find the TextView inside the included layout
            if (includedLayout != null) {
                TextView textView = includedLayout.findViewById(R.id.textViewCountSelection);

                if (textView != null) {
                    textView.setText("Item Count Selected: " + selectedCount);
                } else {
                    Log.e(TAG, "TextView textViewCountselection not found in included layout");
                }
            } else {
                Log.e(TAG, "Included layout not found");
            }

        }
    }

    public void showSelectionOption() {
        if (context instanceof Activity) {
            int selectedCount = getSelectedCount();
            Activity activity = (Activity) context;
            ConstraintLayout constraintLayout = activity.findViewById(R.id.selectionConstraint);
            if (selectedCount >= 1) {
                if (constraintLayout.getVisibility() == View.GONE) {
                    constraintLayout.setVisibility(View.VISIBLE);
                    MaterialCardView cardViewSelect = ((Activity) context).findViewById(R.id.selectionCardOp);
                    Utils.expandCardView(cardViewSelect, animationDuration);
                }
            } else {
                constraintLayout.setVisibility(View.GONE);
                MaterialCardView cardViewSelect = ((Activity) context).findViewById(R.id.selectionCardOp);
                Utils.collapseCardView(cardViewSelect, cardViewSelect, animationDuration);
                multiSelectEnabled = false;
            }
        }
    }
    public void refreshSelectionOption() {
        if (context instanceof Activity) {
            int selectedCount = getSelectedCount();
            Activity activity = (Activity) context;
            if (selectedCount == 0) {
                ConstraintLayout constraintLayout = activity.findViewById(R.id.selectionConstraint);
                constraintLayout.setVisibility(View.GONE);
                MaterialCardView cardViewSelect = ((Activity) context).findViewById(R.id.selectionCardOp);
                Utils.collapseCardView(cardViewSelect, cardViewSelect, animationDuration);

                multiSelectEnabled = false;
            }
        }
    }

    public ArrayList<ItemModel> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    public void clearSelection() {
        // Reset all items' selected state
        for (ItemModel item : selectedItems) {
            item.setSelected(false);
        }
        selectedItems.clear();
        multiSelectEnabled = false;
        notifyDataSetChanged(); // Refresh the whole RecyclerView
    }

    public void selectAll(ArrayList<ItemModel> filteredData) {
        for (ItemModel item : deviceList) {
            if (item.isSelected()) {
                item.setSelected(false);
            }
        }
        selectedItems.clear();

        for (ItemModel item : deviceList) {
            if (!item.isSelected()) {
                item.setSelected(true);
                selectedItems.add(item);
            }
        }

        multiSelectEnabled = true;
        updateSelectionCount();
        notifyDataSetChanged(); // Refresh the whole RecyclerView
    }

    public void disableMultiSelect() {
        multiSelectEnabled = false;
    }
    public void enableMultiSelect() {
        multiSelectEnabled = true;
        toggleSelection(position);
        showSelectionOption();
    }


    public void showDeleteDialog(Context context, int position) {

        ArrayList<ItemModel> deviceList = this.deviceList;
        ArrayList<String> serialNum = dbHelper.getAllSerialNumbers();
        if (serialNum.isEmpty() || position >= serialNum.size()) {
            // If serialNum is empty or the position is out of bounds, handle the error
            Log.e(TAG, "SerialNum is empty or position is out of bounds");
            return;
        }

        ItemModel device = deviceList.get(position); // Get the right position
        String serialNumText = String.valueOf(device.getSerialNumber());
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_delete_dialog, (ConstraintLayout) ((Activity) context).findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.titleText)).setText("Delete " + "[ " + serialNumText + " ]");
        ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to delete this device?\n" + "Serial Number: [ " + serialNumText + " ]\n" + "\n" + "This action cannot be undone.");

        final AlertDialog alertDialog = builder.create();

        // For Delete Button
        view.findViewById(R.id.actionDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position >= 0 && position < deviceList.size()) {
                    ItemModel device = deviceList.get(position);
                    dbHelper.deleteDeviceBySerialNumber(String.valueOf(device.getSerialNumber()));
                    deviceList.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    ((Activity) context).recreate();
                    Log.d(TAG, "onClick: " + device.getSerialNumber());
                    Toast.makeText(context, "SN: " + device.getSerialNumber() + " Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Invalid position: " + position);
                    Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });

        // For Cancel Button
        view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }


}
