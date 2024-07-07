package com.example.qrscanner.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

    int topPadding;
    int startPadding;
    int endPadding;
    int bottomPadding;

    int noUserMarginTop = 12;


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

        holder.serialNum_id.setText(String.valueOf(data.getSerialNumber()));
        holder.assignedTo_id.setText(String.valueOf(data.getName()));
        holder.department_id.setText(String.valueOf(data.getDepartment()));
        holder.textViewDM.setText(data.getDeviceType());
        holder.deviceModel_id.setText(String.valueOf(data.getDeviceBrand()));
        holder.datePurchased_id.setText(String.valueOf(data.getDatePurchased()));

        holder.suHeader.setText(data.getSerialNumber());

        topPadding = holder.linearLayout2.getPaddingTop();
        startPadding = holder.linearLayout2.getPaddingStart();
        endPadding = holder.linearLayout2.getPaddingEnd();
        bottomPadding = holder.linearLayout2.getPaddingBottom();

        if (data.getName().isEmpty()) {
            holder.imgScan.setImageResource(R.drawable.qr_icon_48);
            holder.suHeader.setVisibility(View.GONE);
            holder.linearLayout2.setPadding(holder.linearLayout2.getPaddingStart(), Utils.dpToPxOrDirectPx(context, noUserMarginTop), endPadding, bottomPadding);
            holder.topUserF.setVisibility(View.VISIBLE);
        } else {
            holder.imgScan.setImageResource(R.drawable.user_bulk_48);
            holder.serialNum_id.setText(data.getName());
            holder.linearLayout2.setPadding(startPadding, topPadding, endPadding, bottomPadding);
            holder.topUserF.setVisibility(View.GONE);
        }

        // Call calculate ExpirationAndStatus method
        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
////            Date inputDate = dateFormat.parse(data.getDatePurchased());
            dateFormat = new SimpleDateFormat("MM/dd/yy");
            inputDate = dateFormat.parse(data.getDatePurchased());
            Utils.calculateExpirationAndStatus(inputDate, holder.dateExpire_id, holder.status_id);

            // Get the status text to determine visibility
            String status = holder.status_id.getText().toString();

            // Set visibility of indicator based on status
            int clExpired = ContextCompat.getColor(context, R.color.clExpired);
            int clLight = ContextCompat.getColor(context, R.color.itemBg);
            int clDark = ContextCompat.getColor(context, R.color.itemBg2);
            if (status.equals("For Refresh")) {
                holder.topExpiration.setVisibility(View.VISIBLE);
                holder.cardViewMain.setCardBackgroundColor(clExpired);
//                holder.cardViewExpanded.setCardBackgroundColor(clExpired);
            } else {
                holder.topExpiration.setVisibility(View.GONE);
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    holder.cardViewMain.setCardBackgroundColor(clDark);
                } else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                    holder.cardViewMain.setCardBackgroundColor(clLight);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.availability_id.setText(String.valueOf(data.getAvailability()));

        // Set the Visibility of thee "topUser" Icon indicator based on the data
//        if (data != null && data.getName() != null && !data.getName().isEmpty()) {
//
//            holder.topUser.setVisibility(View.GONE);
//        } else {
//            holder.topUser.setVisibility(View.VISIBLE);
//        }

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
            holder.deviceIC.setImageResource(R.drawable.laptop_icon);
        } else if (String.valueOf(data.getDeviceType()).equals("Desktop")) {
            holder.deviceIC.setImageResource(R.drawable.ic_pc_computer);
        } else if (String.valueOf(data.getDeviceType()).equals("Phone")) {
            holder.deviceIC.setImageResource(R.drawable.ic_mobile_phone);
        } else if (String.valueOf(data.getDeviceType()).equals("Tablet")) {
            holder.deviceIC.setImageResource(R.drawable.ic_tablet);
        } else if (String.valueOf(data.getDeviceType()).equals(data.getDeviceType())) {
            if (data.image != null){
                // Convert byte array to Bitmap/int
                byte[] imageBytes = data.getImage();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.deviceIC.setImageBitmap(bitmap);
            } else {
                Log.d("ItemAdapter", "onBindViewHolder: Null Image");
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
                holder.deviceIC2.setVisibility(View.VISIBLE);
                holder.deviceIC2.setImageResource(R.drawable.ic_unknown_device_light);
                holder.deviceIC.setVisibility(View.GONE);
            } else {
                holder.deviceIC2.setVisibility(View.VISIBLE);
                holder.deviceIC2.setImageResource(R.drawable.ic_unknown_device);
                holder.deviceIC.setVisibility(View.GONE);
            }
        }


        Utils.smoothHideAndReveal(holder.otherInfo, animationDuration);
        Utils.smoothHideAndReveal(holder.actions, animationDuration);
        Utils.smoothHideAndReveal(holder.linearLayout2, animationDuration);
//        Utils.smoothHideAndReveal(holder.linearLayoutIndicators, animationDuration);
        Utils.smoothHideAndReveal(holder.dummy, animationDuration);


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
        LinearLayout linearLayout2, linearLayoutIndicators, dummy, linearLayout01, linearLayout06;
        TextView txtSN, serialNum_id, assignedTo_id, assignedTo_Text, department_id, deviceModel_id, datePurchased_id, dateExpire_id, dateExpire_Text, status_id, availability_id, textViewDM, suHeader;
        ConstraintLayout otherInfo, otherInfo2, actions;
        ImageView topUser, imgScan, topExpiration, userIndicator, deviceIC, deviceIC2, dropDownArrow, connector, connector5, imageView3, imageView5;
        CardView editBtn, deleteBtn, cardViewMain;

        FrameLayout topUserF, topExpirationF;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dbHelper = new DBHelper(context);

            cardViewMain = itemView.findViewById(R.id.cardViewMain);

            // TEXT VIEWS
            serialNum_id = itemView.findViewById(R.id.sn);
            assignedTo_id = itemView.findViewById(R.id.at);
            assignedTo_Text = itemView.findViewById(R.id.textAT);
            department_id = itemView.findViewById(R.id.dep);
            textViewDM = itemView.findViewById(R.id.textDM);
            deviceModel_id = itemView.findViewById(R.id.dm);
            datePurchased_id = itemView.findViewById(R.id.dp);
            dateExpire_id = itemView.findViewById(R.id.de);
            dateExpire_Text = itemView.findViewById(R.id.textDE);
            status_id = itemView.findViewById(R.id.sts);
            availability_id = itemView.findViewById(R.id.avl);
            connector = itemView.findViewById(R.id.connector);
            connector5 = itemView.findViewById(R.id.connector5);
            imageView3 = itemView.findViewById(R.id.imageView3);
            imageView5 = itemView.findViewById(R.id.imageView5);

            txtSN = itemView.findViewById(R.id.textSN);

            suHeader = itemView.findViewById(R.id.subHeader);

            // ICONS
            imgScan = itemView.findViewById(R.id.imageViewScan);
            userIndicator = itemView.findViewById(R.id.userIndicator);
            topExpiration = itemView.findViewById(R.id.topExpiration);
//            topUser = itemView.findViewById(R.id.topUser);
            deviceIC = itemView.findViewById(R.id.deviceIC);
            deviceIC2 = itemView.findViewById(R.id.deviceIC2);
            dropDownArrow = itemView.findViewById(R.id.dropDownArrow);

            editBtn =  itemView.findViewById(R.id.editBtn);
            deleteBtn =  itemView.findViewById(R.id.deleteBtn);

            otherInfo = itemView.findViewById(R.id.otherInfo);
            otherInfo2 = itemView.findViewById(R.id.otherInfo2);
            actions = itemView.findViewById(R.id.actions);

            // Get the layout parameters of the view
            layoutParams = (ViewGroup.MarginLayoutParams) imgScan.getLayoutParams();

            linearLayout2 = itemView.findViewById(R.id.linearLayoutShown2);
            linearLayoutIndicators = itemView.findViewById(R.id.linearLayoutIndicators);
            dummy = itemView.findViewById(R.id.dummy);
            linearLayout06 = itemView.findViewById(R.id.linearLayout6);

            linearLayout01 = itemView.findViewById(R.id.linearLayout01);

            constraintHolder = itemView.findViewById(R.id.constraintHolder);
            topUserF = itemView.findViewById(R.id.topUserF);
            topExpirationF = itemView.findViewById(R.id.topExpirationF);

            sub = itemView.findViewById(R.id.needToSubtract);

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

            // Debugging
//            Log.d("ItemAdapter", "getSerial: " + data.getSerialNumber());
//            Log.d("ItemAdapter", "getPosition: " + position);

            if (otherInfo != null) {

                Utils.smoothHideAndReveal(otherInfo, animationDuration);
                Utils.smoothHideAndReveal(actions, animationDuration);
                Utils.smoothHideAndReveal(linearLayout2, animationDuration);
                Utils.smoothHideAndReveal(topUserF, animationDuration);
                Utils.smoothHideAndReveal(topExpirationF, animationDuration);
//                Utils.smoothHideAndReveal(linearLayoutIndicators, animationDuration);
                Utils.smoothHideAndReveal(dummy, animationDuration);

                if (otherInfo.getVisibility() == View.GONE) {
                    Log.d("ItemAdapter", "otherInfo is VISIBLE");



                    otherInfo.setVisibility(View.VISIBLE); // Set otherInfo to VISIBLE
                    otherInfo2.setVisibility(View.VISIBLE); // Set otherInfo to VISIBLE
                    connector.setVisibility(View.VISIBLE);
                    connector5.setVisibility(View.VISIBLE);
                    assignedTo_Text.setVisibility(View.VISIBLE);
                    assignedTo_id.setVisibility(View.VISIBLE);
                    imageView3.setVisibility(View.VISIBLE);
                    imageView5.setVisibility(View.VISIBLE);
                    dateExpire_Text.setVisibility(View.VISIBLE);
                    dateExpire_id.setVisibility(View.VISIBLE);
                    actions.setVisibility(View.VISIBLE); // Set ic_edit and delete to VISIBLE



                    Utils.rotateUp(dropDownArrow);

                    serialNum_id.setText(data.getSerialNumber());

                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally visible and getName is not null and topExpiration is not expire
                        imgScan.setImageResource(R.drawable.qr_icon_48);
//                        userIndicator.setVisibility(View.GONE);
                        linearLayout2.setPadding(startPadding, Utils.dpToPxOrDirectPx(context, 2), endPadding, bottomPadding);
                    } else {
                        // otherInfo is finally visible and getName is null  and topExpiration is expired
//                        userIndicator.setVisibility(View.VISIBLE);

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topUserF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.START);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.END);

                        constraintSet.connect(R.id.topUserF, ConstraintSet.TOP, R.id.imageView3, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.BOTTOM, R.id.imageView3, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.END, R.id.constraintHolder, ConstraintSet.END, Utils.dpToPxOrDirectPx(context, 16));

                        constraintSet.applyTo(constraintHolder);
                        linearLayout2.setPadding(startPadding, Utils.dpToPxOrDirectPx(context, 2), endPadding, bottomPadding);
                    }

//                    imgScan.setImageResource(R.drawable.qr_icon_24);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(originalImgSizeW / 2, originalImgSizeH / 2);
                    imgScan.setLayoutParams(params);
//                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(originalImgSizeW / 2, originalImgSizeH / 2);
//                    dummy.setLayoutParams(params);

                    suHeader.setVisibility(View.GONE);

                    linearLayout01.setVisibility(View.VISIBLE);

                    if (Utils.calculateExpiration(inputDate, "For Refresh")) {
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.START);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topExpirationF, ConstraintSet.END);

                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.TOP, R.id.imageView5, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.BOTTOM, R.id.imageView5, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.END, R.id.constraintHolder, ConstraintSet.END, Utils.dpToPxOrDirectPx(context, 16));

                        constraintSet.applyTo(constraintHolder);
                    } else {
                        topExpiration.setVisibility(View.GONE);
                    }

                    Utils.expandCardViewItemAdapter(context, cardViewMain, animationDuration);


                } else {

                    // otherInfo is visible
                    otherInfo.setVisibility(View.GONE); // Set otherInfo to VISIBLE
                    otherInfo2.setVisibility(View.GONE); // Set otherInfo to VISIBLE
                    connector.setVisibility(View.GONE);
                    connector5.setVisibility(View.GONE);
                    assignedTo_Text.setVisibility(View.GONE);
                    assignedTo_id.setVisibility(View.GONE);
                    imageView3.setVisibility(View.GONE);
                    imageView5.setVisibility(View.GONE);
                    dateExpire_Text.setVisibility(View.GONE);
                    dateExpire_id.setVisibility(View.GONE);
                    actions.setVisibility(View.GONE); // Set ic_edit and delete to GONE
//                    dummy.setVisibility(View.GONE);

                    

                    Utils.rotateDown(dropDownArrow);
                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally GONE and getName is not null
//                        topUser.setVisibility(View.GONE);
//                        userIndicator.setVisibility(View.GONE);
                        linearLayout2.setPadding(startPadding, topPadding, endPadding, bottomPadding);

                        imgScan.setImageResource(R.drawable.user_bulk_48);
                        suHeader.setVisibility(View.VISIBLE);
                        serialNum_id.setText(data.getName());
                    } else {
                        // otherInfo is finally visible and getName is null  and topExpiration is expired

                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(constraintHolder);

                        // Remove existing constraints
                        constraintSet.clear(R.id.topUserF, ConstraintSet.TOP);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.START);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.BOTTOM);
                        constraintSet.clear(R.id.topUserF, ConstraintSet.END);

                        constraintSet.connect(R.id.topUserF, ConstraintSet.TOP, R.id.linearLayout6, ConstraintSet.TOP);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.BOTTOM, R.id.linearLayout6, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.topUserF, ConstraintSet.END, R.id.linearLayout6, ConstraintSet.START, Utils.dpToPxOrDirectPx(context, 16));
//
                        constraintSet.applyTo(constraintHolder);

                        linearLayout2.setPadding(startPadding, Utils.dpToPxOrDirectPx(context, noUserMarginTop), endPadding, bottomPadding);

                        imgScan.setImageResource(R.drawable.qr_icon_48);
                        suHeader.setVisibility(View.GONE);
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
                        constraintSet.connect(R.id.topExpirationF, ConstraintSet.END, R.id.topUserF, ConstraintSet.START, Utils.dpToPxOrDirectPx(context, 16));

                        constraintSet.applyTo(constraintHolder);
                    }


                    // Reset Size
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(originalImgSizeW * 2, originalImgSizeH * 2);
                    imgScan.setLayoutParams(params);

                    linearLayout01.setVisibility(View.GONE);

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