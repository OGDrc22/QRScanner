package com.example.qrscanner.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.UpdateData;
import com.example.qrscanner.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public static final String EXTRA_POSITION = "com.example.qrscanner.adapter.EXTRA_POSITION";
    private static final int YOUR_REQUEST_CODE = 1;
    private static final Logger log = LogManager.getLogger(ItemAdapter.class);
    private int itemLayoutId;
    private Context context;
    private ArrayList<Assigned_to_User_Model> originalData;
    private ArrayList<Assigned_to_User_Model> filteredData;

    private ArrayList serialNum_id, assignedTo_id, department, device, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    private ArrayList<Assigned_to_User_Model> deviceList;

    //    public ImageView topUser, rightExpiration, rightIndicator, imgScan;
    private ViewGroup.MarginLayoutParams layoutParams;

    private int position;
    Assigned_to_User_Model data;


    SimpleDateFormat dateFormat;
    Date inputDate;

    private DBHelper dbHelper;

    private int animationDuration = 300;

    // Define an interface for delete action
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
    private OnDeleteClickListener deleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(int position);
    }
    private OnEditClickListener editClickListener;

    public void editItem(Context context, int position) {
        Assigned_to_User_Model device = deviceList.get(position);
        Intent intent = new Intent(context, UpdateData.class); // Use context instead of this
        intent.putExtra(EXTRA_POSITION, position);
//        intent.putExtra("device", String.valueOf(device));
        intent.putExtra("serialNumber", String.valueOf(device.getSerialNumber()));
        intent.putExtra("name", String.valueOf(device.getName()));
        intent.putExtra("department", String.valueOf(device.getDepartment()));
        intent.putExtra("device", String.valueOf(device.getDeviceType()));
        intent.putExtra("deviceModel", String.valueOf(device.getDeviceBrand()));
        intent.putExtra("datePurchased", String.valueOf(device.getDatePurchased()));
        intent.putExtra("dateExpired", String.valueOf(device.getDateExpired()));
        intent.putExtra("status", String.valueOf(device.getStatus()));
        intent.putExtra("availability", String.valueOf(device.getAvailability()));
        ((Activity) context).startActivityForResult(intent, YOUR_REQUEST_CODE); // Use context to start activity
        notifyItemChanged(position);
    }

    public ItemAdapter(int itemLayoutId, Context context, ArrayList<Assigned_to_User_Model> deviceList, ArrayList serialNum_id, ArrayList assignedTo_id, ArrayList department_id, ArrayList device_id, ArrayList deviceModel_id, ArrayList datePurchased_id, ArrayList dateExpire_id, ArrayList status_id, ArrayList availability_id, OnDeleteClickListener onDeleteClickListener, OnEditClickListener onEditClickListener) {
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        data = deviceList.get(position);

        holder.serialNum_tv.setText(String.valueOf(data.getSerialNumber()));
        holder.textHolderAssignedTo.setText(String.valueOf(data.getName()));
        holder.textHolderDepartment.setText(String.valueOf(data.getDepartment()));
        holder.textHolderDeviceType.setText(data.getDeviceType());
        holder.textHolderDeviceModel.setText(String.valueOf(data.getDeviceBrand()));
        holder.textHolderDatePur.setText(String.valueOf(data.getDatePurchased()));

        holder.subHeader.setText(data.getSerialNumber());

        if (data.getName().isEmpty()) {
            holder.imgScan.setImageResource(R.drawable.qr_icon_48);
            holder.topUserF.setVisibility(View.VISIBLE);
            holder.headerF.setVisibility(View.GONE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintHolder);

            constraintSet.setVerticalBias(R.id.subHeaderF, 0.5f);

            constraintSet.applyTo(holder.constraintHolder);

            int color = ContextCompat.getColor(context, R.color.txtHeader);
            holder.subHeader.setTextColor(color);
            holder.subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.textSize_for_header);

        } else {
            holder.imgScan.setImageResource(R.drawable.user_bulk_48);
            holder.serialNum_tv.setText(data.getName());
            holder.topUserF.setVisibility(View.GONE);
            holder.headerF.setVisibility(View.VISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintHolder);

            constraintSet.setVerticalBias(R.id.subHeaderF, 0.8f);

            constraintSet.applyTo(holder.constraintHolder);

            int color = ContextCompat.getColor(context, R.color.txtSubHeader);
            holder.subHeader.setTextColor(color);
            holder.subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.textSize_for_textHolderAssigned);

        }

        // Call calculate ExpirationAndStatus method
        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
////            Date inputDate = dateFormat.parse(data.getDatePurchased());
            dateFormat = new SimpleDateFormat("MM/dd/yy");
            inputDate = dateFormat.parse(data.getDatePurchased());
            Utils.calculateExpirationAndStatus(inputDate, holder.textHolderDateExpired, holder.textHolderStatus);

            // Get the status text to determine visibility
            String status = holder.textHolderStatus.getText().toString();

            // Set visibility of indicator based on status
            int clExpired = ContextCompat.getColor(context, R.color.clExpired);
            int clLight = ContextCompat.getColor(context, R.color.itemBg);
            int clDark = ContextCompat.getColor(context, R.color.itemBg2);
            int clGreen = ContextCompat.getColor(context, R.color.primary);
            int clRed = ContextCompat.getColor(context, R.color.clError);
            if (status.equals("For Refresh")) {
                holder.topExpiration.setVisibility(0);
                holder.textHolderStatus.setTextColor(clRed);
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(holder.imgVwStatus, "rotation", new float[]{0.0f, 180.0f});
                rotationAnimator.setDuration(1000);
                rotationAnimator.setRepeatCount(-1);
                rotationAnimator.start();
            } else {
                holder.topExpiration.setVisibility(8);
                holder.textHolderStatus.setTextColor(clGreen);
                if (AppCompatDelegate.getDefaultNightMode() == 2) {
                    holder.cardViewMain.setCardBackgroundColor(clDark);
                } else if (AppCompatDelegate.getDefaultNightMode() == 1) {
                    holder.cardViewMain.setCardBackgroundColor(clLight);
                }
                ObjectAnimator rotationAnimator2 = ObjectAnimator.ofFloat(holder.imgVwStatus, "rotation", new float[]{0.0f, 180.0f});
                rotationAnimator2.setDuration(1000);
                rotationAnimator2.start();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.textHolderAvailability.setText(String.valueOf(data.getAvailability()));

//        Delete Button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteClickListener != null) {
                    // Pass the clicked position to the deleteClickListener
//                    deleteClickListener.onDeleteClick(position);
                    showDeleteDialog(context, position);
                }
            }
        });

//        Edit Button
        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editClickListener != null) {
//                    editClickListener.onEditClick(position);
                    editItem(context, position);

                }
            }
        });

        if (String.valueOf(data.getDeviceType()).equals("Laptop")) {
            holder.deviceTypeIC.setImageResource(R.drawable.laptop_icon);
            holder.deviceModelIC.setImageResource(R.drawable.laptop_icon);
        } else if (String.valueOf(data.getDeviceType()).equals("Desktop")) {
            holder.deviceTypeIC.setImageResource(R.drawable.ic_pc_computer);
            holder.deviceModelIC.setImageResource(R.drawable.ic_pc_computer);
        } else if (String.valueOf(data.getDeviceType()).equals("Phone")) {
            holder.deviceTypeIC.setImageResource(R.drawable.ic_mobile_phone);
            holder.deviceModelIC.setImageResource(R.drawable.ic_mobile_phone);
        } else if (String.valueOf(data.getDeviceType()).equals("Tablet")) {
            holder.deviceTypeIC.setImageResource(R.drawable.ic_tablet);
            holder.deviceModelIC.setImageResource(R.drawable.ic_tablet);
        } else if (String.valueOf(data.getDeviceType()).equals(data.getDeviceType())) {
            if (data.image != null){
                // Convert byte array to Bitmap/int
                byte[] imageBytes = data.getImage();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.deviceTypeIC.setImageBitmap(bitmap);
                holder.deviceModelIC.setImageBitmap(bitmap);
            } else {
                Log.d("ItemAdapter", "onBindViewHolder: Null Image");
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                holder.deviceModelIC.setImageResource(R.drawable.ic_unknown_device_light);
                holder.deviceTypeIC.setVisibility(View.GONE);
            } else {
                holder.deviceModelIC.setImageResource(R.drawable.ic_unknown_device);
                holder.deviceTypeIC.setVisibility(View.GONE);
            }
        }


//        Utils.smoothTransition(holder.otherInfo, animationDuration);
        Utils.smoothTransition(holder.actions, animationDuration);
//        Utils.smoothTransition(holder.linearLayout2, animationDuration);
//        Utils.smoothTransition();(holder.linearLayoutIndicators, animationDuration);
        Utils.smoothTransition(holder.imgScan_Frame, animationDuration);


    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setDeviceList(ArrayList<Assigned_to_User_Model> deviceList) {
        this.deviceList = deviceList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout constraintHolder, sub;
        LinearLayout linearLayoutDeviceType, linearLayoutDeviceModel, imgScan_Frame, linearLayout01, linearLayout06;
        TextView txtSN, serialNum_tv, textHolderAssignedTo, textHolderDepartment, textHolderDeviceModel, textHolderDatePur, textHolderDateExpired, textHolderStatus, textHolderAvailability, textHolderDeviceType, subHeader;
        TextView hintAssignedTo, hintDepartment, hintDeviceType, hintDeviceModel, hintDatePurchased, hintExpirationDate;
        ConstraintLayout actions;
        ImageView imgScan, imgVwDepartment, imgVwStatus, imgVwDatePur, imgVwDateExpired, topExpiration, deviceTypeIC, deviceModelIC, dropDownArrow, imageView3, connector, connector2, connector3, connector4, connector5;
        CardView editBtn, deleteBtn, cardViewMain;

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
            imgVwStatus = itemView.findViewById(R.id.imgVwStatus);
            imgVwDateExpired = itemView.findViewById(R.id.imgVwDateExpired);
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


            hintAssignedTo = itemView.findViewById(R.id.hintAssignedTo);
            hintDepartment = itemView.findViewById(R.id.hintDepartment);
            hintDeviceType = itemView.findViewById(R.id.hintDeviceType);
            hintDeviceModel = itemView.findViewById(R.id.hintDeviceModel);
            hintDatePurchased = itemView.findViewById(R.id.hintDatePurchased);
            hintExpirationDate = itemView.findViewById(R.id.hintExpirationDate);

            txtSN = itemView.findViewById(R.id.textSN);

            subHeader = itemView.findViewById(R.id.subHeader);


            editBtn =  itemView.findViewById(R.id.editBtn);
            deleteBtn =  itemView.findViewById(R.id.deleteBtn);

            actions = itemView.findViewById(R.id.actions);

            // Get the layout parameters of the view
            layoutParams = (ViewGroup.MarginLayoutParams) imgScan.getLayoutParams();

            imgScan_Frame = itemView.findViewById(R.id.imgScan_Frame);
            linearLayout06 = itemView.findViewById(R.id.linearLayout6);


            constraintHolder = itemView.findViewById(R.id.constraintHolder);
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



            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            // Debugging
//            Log.d("ItemAdapter", "onClick triggered");
//            Log.d("ItemAdapter", "getName: " + data.getName());

            position = getAdapterPosition();
            data = deviceList.get(position);

            int originalImgSizeH = imgScan.getHeight();
            int originalImgSizeW = imgScan.getWidth();

            String input = data.getDatePurchased();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
            Date inputDate = null;
            try {
                inputDate = simpleDateFormat.parse(input);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Log.d("ItemAdapter", "onClick: SubHeader TextSZ " + textSize_for_textHolderAssigned);
            Log.d("ItemAdapter", "onClick: Header TextSZ " + textSize_for_header);

            // Debugging
//            Log.d("ItemAdapter", "getSerial: " + data.getSerialNumber());
//            Log.d("ItemAdapter", "getPosition: " + position);

            if (actions != null) {

//                Utils.smoothTransition(otherInfo, animationDuration);
                Utils.smoothTransition(actions, animationDuration);
                Utils.smoothTransition(textSN_F, animationDuration);
                Utils.smoothTransition(topUserF, animationDuration);
                Utils.smoothTransition(topExpirationF, animationDuration);
                Utils.smoothTransition(headerF, animationDuration);
                Utils.smoothTransition(subHeaderF, animationDuration);
                Utils.smoothTransition(imgScan_Frame, animationDuration);

                if (actions.getVisibility() == View.GONE) {
                    Log.d("ItemAdapter", "otherInfo is VISIBLE");

                    connector.setVisibility(View.VISIBLE);
                    connector2.setVisibility(View.VISIBLE);
                    connector3.setVisibility(View.VISIBLE);
                    connector4.setVisibility(View.VISIBLE);
                    connector5.setVisibility(View.VISIBLE);

                    txtSN.setVisibility(View.VISIBLE);
                    textHolderDepartment.setVisibility(View.VISIBLE);
                    textHolderDeviceType.setVisibility(View.VISIBLE);
                    textHolderDeviceModel.setVisibility(View.VISIBLE);
                    textHolderDatePur.setVisibility(View.VISIBLE);
                    textHolderStatus.setVisibility(View.VISIBLE);
                    textHolderAvailability.setVisibility(View.VISIBLE);

                    imageView3.setVisibility(View.VISIBLE);
                    imgVwDepartment.setVisibility(View.VISIBLE);
                    imgVwDatePur.setVisibility(View.VISIBLE);
                    imgVwDateExpired.setVisibility(View.VISIBLE);

                    textHolderDateExpired.setVisibility(View.VISIBLE);
                    actions.setVisibility(View.VISIBLE); // Set ic_edit and delete to VISIBLE

                    hintAssignedTo.setVisibility(View.VISIBLE);
                    hintDepartment.setVisibility(View.VISIBLE);
                    hintDeviceType.setVisibility(View.VISIBLE);
                    hintDeviceModel.setVisibility(View.VISIBLE);
                    hintDatePurchased.setVisibility(View.VISIBLE);
                    hintExpirationDate.setVisibility(View.VISIBLE);

                    deviceTypeIC.setVisibility(View.VISIBLE);
                    deviceModelIC.setVisibility(View.VISIBLE);

                    Utils.rotateUp(dropDownArrow);

                    if (!data.getName().isEmpty()) {

                        imgScan.setImageResource(R.drawable.qr_icon_48);
                        imgScan_Frame.setPadding(
                                imgScan_Frame.getPaddingStart(),
                                imgScan_Frame.getPaddingTop() + Utils.dpToPxOrDirectPx(context, 8),
                                imgScan_Frame.getPaddingEnd(),
                                imgScan_Frame.getPaddingBottom()
                        );

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        constraintSet.clear(R.id.headerF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.headerF, ConstraintSet.START);
                        constraintSet.clear(R.id.headerF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.headerF, ConstraintSet.END);

                        constraintSet.connect(R.id.headerF, ConstraintSet.TOP, R.id.hintAssignedTo, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.headerF, ConstraintSet.BOTTOM, R.id.assignedToIC, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.headerF, ConstraintSet.START, R.id.hintAssignedTo, ConstraintSet.START);
                        constraintSet.setVerticalBias(R.id.headerF, 0.5f);

                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.START);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.END);

                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.TOP, R.id.textSN_F, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.BOTTOM, R.id.imgScan_Frame, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.START, R.id.textSN_F, ConstraintSet.START);
//                        constraintSet.setVerticalBias(R.id.subHeaderF, 0.5f);

                        constraintSet.applyTo(constraintHolder);

                        int color = ContextCompat.getColor(context, R.color.txtHeader);
                        subHeader.setTextColor(color);
                        subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_header);


                        Log.d("ItemAdapter", "Expand: Before Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());
                        serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_textHolderAssigned);
                        Log.d("ItemAdapter", "Expand: After Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());


                    } else {

                        txtSN.setVisibility(View.VISIBLE);

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topUserF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.START);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.END);

                        constraintSet.connect(R.id.topUserF, ConstraintSet.TOP, R.id.assignedToIC, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.BOTTOM, R.id.assignedToIC, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.END, R.id.center, ConstraintSet.START, Utils.dpToPxOrDirectPx(context, 16));

                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.TOP, R.id.textSN_F, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.START, R.id.linearLayout5, ConstraintSet.END);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.BOTTOM, R.id.imgScan_Frame, ConstraintSet.BOTTOM);

                        constraintSet.applyTo(constraintHolder);
                    }

//                    imgScan.setImageResource(R.drawable.qr_icon_24);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(originalImgSizeW / 2, originalImgSizeH / 2);
                    imgScan.setLayoutParams(params);

                    if (Utils.calculateExpiration(inputDate, "For Refresh")) {
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.START);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.END);

                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.TOP, R.id.imgVwDateExpired, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.BOTTOM, R.id.imgVwDateExpired, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.END, R.id.constraintHolder, ConstraintSet.END, Utils.dpToPxOrDirectPx(context, 16));

                        constraintSet.applyTo(constraintHolder);
                    } else {
                        topExpiration.setVisibility(View.GONE);
                    }

                    Utils.expandCardViewItemAdapter(cardViewMain, animationDuration);




//===========================================////===========================================////===========================================////===========================================//
                } else { //===========================================//
//===========================================////===========================================////===========================================////===========================================//

                    connector.setVisibility(View.GONE);
                    connector2.setVisibility(View.GONE);
                    connector3.setVisibility(View.GONE);
                    connector4.setVisibility(View.GONE);
                    connector5.setVisibility(View.GONE);

                    txtSN.setVisibility(View.GONE);
                    textHolderDepartment.setVisibility(View.GONE);
                    textHolderDeviceType.setVisibility(View.GONE);
                    textHolderDeviceModel.setVisibility(View.GONE);
                    textHolderDatePur.setVisibility(View.GONE);
                    textHolderStatus.setVisibility(View.GONE);
                    textHolderAvailability.setVisibility(View.GONE);

                    imageView3.setVisibility(View.GONE);
                    imgVwDepartment.setVisibility(View.GONE);
                    imgVwDatePur.setVisibility(View.GONE);
                    deviceTypeIC.setVisibility(View.GONE);
                    deviceModelIC.setVisibility(View.GONE);
                    imgVwDateExpired.setVisibility(View.GONE);

                    textHolderDateExpired.setVisibility(View.GONE);
                    actions.setVisibility(View.GONE);

                    hintAssignedTo.setVisibility(View.GONE);
                    hintDepartment.setVisibility(View.GONE);
                    hintDeviceType.setVisibility(View.GONE);
                    hintDeviceModel.setVisibility(View.GONE);
                    hintDatePurchased.setVisibility(View.GONE);
                    hintExpirationDate.setVisibility(View.GONE);


                    Utils.rotateDown(dropDownArrow);

                    if (!data.getName().isEmpty()) {

                        imgScan.setImageResource(R.drawable.user_bulk_48);
                        imgScan_Frame.setPadding(
                                imgScan_Frame_paddingStart,
                                imgScan_Frame_paddingTop,
                                imgScan_Frame_paddingEnd,
                                imgScan_Frame_paddingBottom
                        );
                        subHeader.setVisibility(View.VISIBLE);

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        constraintSet.clear(R.id.headerF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.headerF, ConstraintSet.START);
                        constraintSet.clear(R.id.headerF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.headerF, ConstraintSet.END);

                        constraintSet.connect(R.id.headerF, ConstraintSet.TOP, R.id.imgScan_Frame, ConstraintSet.TOP);
                        constraintSet.connect(R.id.headerF, ConstraintSet.BOTTOM, R.id.imgScan_Frame, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.headerF, ConstraintSet.START, R.id.linearLayout5, ConstraintSet.END);
                        constraintSet.setVerticalBias(R.id.headerF, 0.2f);

                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.START);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.subHeaderF, ConstraintSet.END);

                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.TOP, R.id.textSN_F, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.BOTTOM, R.id.imgScan_Frame, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.START, R.id.textSN_F, ConstraintSet.START);
                        constraintSet.setVerticalBias(R.id.subHeaderF, 0.8f);

                        constraintSet.applyTo(constraintHolder);

                        int color = ContextCompat.getColor(context, R.color.txtSubHeader);
                        subHeader.setTextColor(color);
                        subHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_textHolderAssigned);
                        Log.d("ItemAdapter", "Collapsed: Before Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());
                        serialNum_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize_for_header);
                        Log.d("ItemAdapter", "Collapsed: After Setting Text Sizes: serialNum_tv TextSZ " + serialNum_tv.getTextSize());


                    } else {

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topUserF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.START);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.END);

                        constraintSet.connect(R.id.topUserF, ConstraintSet.TOP, R.id.linearLayout6, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.BOTTOM, R.id.linearLayout6, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.END, R.id.linearLayout6, ConstraintSet.START, topUser_marginEnd);

                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.TOP, R.id.imgScan_Frame, ConstraintSet.TOP);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.START, R.id.linearLayout5, ConstraintSet.END);
                        constraintSet.connect(R.id.subHeaderF, ConstraintSet.BOTTOM, R.id.imgScan_Frame, ConstraintSet.BOTTOM);
//
                        constraintSet.applyTo(constraintHolder);

                        imgScan.setImageResource(R.drawable.qr_icon_48);
                    }

                    if (Utils.calculateExpiration(inputDate, "For Refresh")) {

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.START);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.END);

                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.TOP, R.id.topUserF, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.BOTTOM, R.id.topUserF, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.END, R.id.topUserF, ConstraintSet.START, topUser_marginEnd);

                        constraintSet.applyTo(constraintHolder);
                    }


                    // Reset Size
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(originalImgSizeW * 2, originalImgSizeH * 2);
                    imgScan.setLayoutParams(params);

                    Utils.collapseCardViewItemAdapter(cardViewMain, cardViewMain, animationDuration);

                }

            }

        }

    }



    public void showDeleteDialog(Context context, int position) {

        ArrayList<Assigned_to_User_Model> deviceList = this.deviceList;
        ArrayList<String> serialNum = dbHelper.getAllSerialNumbers();
        if (serialNum.isEmpty() || position >= serialNum.size()) {
            // If serialNum is empty or the position is out of bounds, handle the error
            Log.e("ItemAdapter", "SerialNum is empty or position is out of bounds");
            return;
        }

        Assigned_to_User_Model device = deviceList.get(position); // Get the right position
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
                    Assigned_to_User_Model device = deviceList.get(position);
                    dbHelper.deleteDeviceBySerialNumber(String.valueOf(device.getSerialNumber()));
                    deviceList.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    ((Activity) context).recreate();
                    Log.d("ItemAdapter", "onClick: " + device.getSerialNumber());
                    Toast.makeText(context, "SN: " + device.getSerialNumber() + " Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ItemAdapter", "Invalid position: " + position);
                    Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
                }
                alertDialog.hide();
            }
        });

        // For Cancel Button
        view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                alertDialog.hide();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

}