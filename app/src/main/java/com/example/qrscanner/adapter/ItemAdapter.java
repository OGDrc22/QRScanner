package com.example.qrscanner.adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    //    public ImageView hasUser, leftIndicator, rightIndicator, imgScan;
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

        holder.serialNum_id.setText(String.valueOf(data.getSerialNumber()));
        holder.assignedTo_id.setText(String.valueOf(data.getName()));
        holder.department_id.setText(String.valueOf(data.getDepartment()));
//        holder.device_id.setText(String.valueOf(data.getDevice()));
        holder.textViewDM.setText(data.getDeviceType()); // TODO FIX
        holder.deviceModel_id.setText(String.valueOf(data.getDeviceBrand()));
        holder.datePurchased_id.setText(String.valueOf(data.getDatePurchased()));

        holder.suHeader.setText(data.getSerialNumber());

        if (data.getName().isEmpty()) {
            holder.imgScan.setImageResource(R.drawable.qr_icon_48);
            holder.suHeader.setVisibility(View.GONE);
        } else {
            holder.imgScan.setImageResource(R.drawable.user_bulk_48);
            holder.serialNum_id.setText(data.getName());
        }

        // Call calculateExpirationAndStatus method
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
                holder.leftIndicator.setVisibility(View.VISIBLE);
                holder.cardViewMain.setCardBackgroundColor(clExpired);
//                holder.cardViewExpanded.setCardBackgroundColor(clExpired);
            } else {
                holder.leftIndicator.setVisibility(View.GONE);
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

        // Set the Visibility of thee "hasUser" Icon indicator based on the data
        if (data != null && data.getName() != null && !data.getName().isEmpty()) {

            holder.hasUser.setVisibility(View.GONE);
        } else {
            holder.hasUser.setVisibility(View.VISIBLE);
        }

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
                Toast.makeText(context, "Null Image wtf", Toast.LENGTH_SHORT).show();
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
        Utils.smoothHideAndReveal(holder.linearLayoutIndicators, animationDuration);
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

        LinearLayout linearLayout2, linearLayoutIndicators, dummy;
        TextView txtSN, serialNum_id, assignedTo_id, department_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id, textViewDM, suHeader;
        ConstraintLayout otherInfo, actions;
        ImageView hasUser, leftIndicator, imgScan, expiration, userIndicator, deviceIC, deviceIC2, dropDownArrow;
        CardView editBtn, deleteBtn, cardViewMain;

        int paddingTopIndicators;
        int paddingStartIndicators;
        int paddingEndIndicators;
        int paddingBottomIndicators;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dbHelper = new DBHelper(context);

            cardViewMain = itemView.findViewById(R.id.cardViewMain);

            // TEXT VIEWS
            serialNum_id = itemView.findViewById(R.id.sn);
            assignedTo_id = itemView.findViewById(R.id.at);
            department_id = itemView.findViewById(R.id.dep);
            textViewDM = itemView.findViewById(R.id.textDM);
            deviceModel_id = itemView.findViewById(R.id.dm);
            datePurchased_id = itemView.findViewById(R.id.dp);
            dateExpire_id = itemView.findViewById(R.id.de);
            status_id = itemView.findViewById(R.id.sts);
            availability_id = itemView.findViewById(R.id.avl);

            txtSN = itemView.findViewById(R.id.textSN);

            suHeader = itemView.findViewById(R.id.subHeader);

            // ICONS
            imgScan = itemView.findViewById(R.id.imageViewScan);
            leftIndicator = itemView.findViewById(R.id.leftIndicator);
            userIndicator = itemView.findViewById(R.id.userIndicator);
            expiration = itemView.findViewById(R.id.expirationIndicator);
            hasUser = itemView.findViewById(R.id.hasUser);
            deviceIC = itemView.findViewById(R.id.deviceIC);
            deviceIC2 = itemView.findViewById(R.id.deviceIC2);
            dropDownArrow = itemView.findViewById(R.id.dropDownArrow);

            editBtn =  itemView.findViewById(R.id.editBtn);
            deleteBtn =  itemView.findViewById(R.id.deleteBtn);

            otherInfo = itemView.findViewById(R.id.otherInfo);
            actions = itemView.findViewById(R.id.actions);

            // Get the layout parameters of the view
            layoutParams = (ViewGroup.MarginLayoutParams) imgScan.getLayoutParams();

            linearLayout2 = itemView.findViewById(R.id.linearLayoutShown2);
            linearLayoutIndicators = itemView.findViewById(R.id.linearLayoutIndicators);
            dummy = itemView.findViewById(R.id.dummy);

            paddingTopIndicators = linearLayoutIndicators.getPaddingTop();
            paddingStartIndicators = linearLayoutIndicators.getPaddingStart();
            paddingEndIndicators = linearLayoutIndicators.getPaddingEnd();
            paddingBottomIndicators = linearLayoutIndicators.getPaddingBottom();

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            // Debugging
//            Log.d("ItemAdapter", "onClick triggered");
//            Log.d("ItemAdapter", "getName: " + data.getName());

            position = getAdapterPosition();
            data = deviceList.get(position);

            // Debugging
//            Log.d("ItemAdapter", "getSerial: " + data.getSerialNumber());
//            Log.d("ItemAdapter", "getPosition: " + position);

            if (otherInfo != null) {

                Utils.smoothHideAndReveal(otherInfo, animationDuration);
                Utils.smoothHideAndReveal(actions, animationDuration);
                Utils.smoothHideAndReveal(linearLayout2, animationDuration);
                Utils.smoothHideAndReveal(linearLayoutIndicators, animationDuration);
                Utils.smoothHideAndReveal(dummy, animationDuration);

                if (otherInfo.getVisibility() == View.GONE) {
                    Log.d("ItemAdapter", "otherInfo is VISIBLE");


                    linearLayout2.setGravity(Gravity.TOP);
                    linearLayoutIndicators.setPadding(paddingStartIndicators, 16, paddingEndIndicators,  paddingBottomIndicators);

                    otherInfo.setVisibility(View.VISIBLE); // Set otherInfo to VISIBLE
                    actions.setVisibility(View.VISIBLE); // Set ic_edit and delete to VISIBLE
                    dummy.setVisibility(View.VISIBLE);

                    Utils.rotateUp(dropDownArrow);

                    hasUser.setVisibility(View.GONE);
                    leftIndicator.setVisibility(View.GONE);

                    serialNum_id.setText(data.getSerialNumber());

                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally visible and getName is not null and expiration is not expire
                        userIndicator.setVisibility(View.GONE);
                    } else {
                        // otherInfo is finally visible and getName is null  and expiration is expired
                        userIndicator.setVisibility(View.VISIBLE);
                    }

                    imgScan.setImageResource(R.drawable.qr_icon_24);
                    suHeader.setVisibility(View.GONE);

                    txtSN.setVisibility(View.VISIBLE);

                    try {
                        dateFormat = new SimpleDateFormat("MM/dd/yy");
                        inputDate = dateFormat.parse(data.getDatePurchased());
                        Utils.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

                        // Get the status text to determine visibility
                        String status = status_id.getText().toString();

                        // Set visibility of indicator based on status
                        if (status.equals("For Refresh")) {
                            expiration.setVisibility(View.VISIBLE);
                            leftIndicator.setVisibility(View.GONE);
                        } else {
                            expiration.setVisibility(View.GONE);
                            leftIndicator.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Utils.expandCardViewItemAdapter(cardViewMain, animationDuration);

                } else {


                    linearLayout2.setGravity(Gravity.CENTER_VERTICAL);
                    linearLayoutIndicators.setPadding(paddingStartIndicators, paddingTopIndicators, paddingEndIndicators,  paddingBottomIndicators);

                    // otherInfo is visible
                    otherInfo.setVisibility(View.GONE); // Set otherInfo to GONE
                    actions.setVisibility(View.GONE); // Set ic_edit and delete to GONE
                    dummy.setVisibility(View.GONE);

                    Utils.rotateDown(dropDownArrow);
                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally GONE and getName is not null
                        hasUser.setVisibility(View.GONE);
                    } else {
                        // otherInfo is finally GONE and getName is null
                        hasUser.setVisibility(View.VISIBLE);
                    }


                    imgScan.setImageResource(R.drawable.qr_icon_48);
                    suHeader.setVisibility(View.VISIBLE);


                    txtSN.setVisibility(View.GONE);

                    if (data.getName().isEmpty()) {
                        imgScan.setImageResource(R.drawable.qr_icon_48);
                        suHeader.setVisibility(View.GONE);
                    } else {
                        imgScan.setImageResource(R.drawable.user_bulk_48);
                        serialNum_id.setText(data.getName());
                    }

                    try {
                        dateFormat = new SimpleDateFormat("MM/dd/yy");
                        inputDate = dateFormat.parse(data.getDatePurchased());
                        Utils.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

                        // Get the status text to determine visibility
                        String status = status_id.getText().toString();

                        // Set visibility of indicator based on status
                        if (status.equals("For Refresh")) {
                            leftIndicator.setVisibility(View.VISIBLE);
                        } else {
                            leftIndicator.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Utils.collapseCardViewItemAdapter(cardViewMain, cardViewMain, animationDuration);

                }



                try {
                    dateFormat = new SimpleDateFormat("MM/dd/yy");
                    inputDate = dateFormat.parse(data.getDatePurchased());
                    Utils.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

                    // Get the status text to determine visibility
                    String status = status_id.getText().toString();

                    // Set visibility of indicator based on status
                    if (status.equals("For Refresh")) {

                        expiration.setVisibility(View.VISIBLE);
                    } else {
                        expiration.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
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
                    Log.d("TAG", "onClick: " + device.getSerialNumber());
                    Toast.makeText(context, "SN: " + device.getSerialNumber() + " Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("TAG", "Invalid position: " + position);
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