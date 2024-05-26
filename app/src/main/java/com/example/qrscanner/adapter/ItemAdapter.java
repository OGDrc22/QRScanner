package com.example.qrscanner.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.Assigned_to_User_Model;
import com.example.qrscanner.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.UpdateData;
import com.example.qrscanner.expiration.ExpirationUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public static final String EXTRA_POSITION = "com.example.qrscanner.adapter.EXTRA_POSITION";
    private static final int YOUR_REQUEST_CODE = 1;
    private int itemLayoutId;
    private Context context;
    private ArrayList<Assigned_to_User_Model> originalData;
    private ArrayList<Assigned_to_User_Model> filteredData;

    private ArrayList serialNum_id, assignedTo_id, department, device, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id;

    public ArrayList<Assigned_to_User_Model> deviceList;

//    public ImageView hasUser, leftIndicator, rightIndicator, imgScan;
    private ViewGroup.MarginLayoutParams layoutParams;

    private int position;
    Assigned_to_User_Model data;


    SimpleDateFormat dateFormat;
    Date inputDate;

    private DBHelper dbHelper;


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
        intent.putExtra("device", String.valueOf(device));
        intent.putExtra("serialNumber", String.valueOf(device.getSerialNumber()));
        intent.putExtra("name", String.valueOf(device.getName()));
        intent.putExtra("department", String.valueOf(device.getDepartment()));
        intent.putExtra("device", String.valueOf(device.getDevice()));
        intent.putExtra("deviceModel", String.valueOf(device.getDeviceModel()));
        intent.putExtra("datePurchased", String.valueOf(device.getDatePurchased()));
        intent.putExtra("dateExpired", String.valueOf(device.getDateExpired()));
        intent.putExtra("status", String.valueOf(device.getStatus()));
        intent.putExtra("availability", String.valueOf(device.getAvailability()));
        ((Activity) context).startActivityForResult(intent, YOUR_REQUEST_CODE); // Use context to start activity
        notifyItemChanged(position);
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
        ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to delete this device?\n" + "[ " + serialNumText + " ]\n" + "\n" + "This action cannot be undone.");
        ((ImageView) view.findViewById(R.id.icon_action)).setImageResource(R.drawable.trash_can_10416);
        ((ImageView) view.findViewById(R.id.warning)).setImageResource(R.drawable.warning_sign);
        ((Button) view.findViewById(R.id.actionDelete)).setText("Delete");
        ((Button) view.findViewById(R.id.actionCancel)).setText("Cancel");

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
        holder.deviceModel_id.setText(String.valueOf(data.getDeviceModel()));
        holder.datePurchased_id.setText(String.valueOf(data.getDatePurchased()));

        if (data.getStatus() != null) {
            holder.sts2.setText(String.valueOf(data.getStatus()));
        } else {
            holder.sts2.setText("null");
        }

        // Call calculateExpirationAndStatus method
        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
////            Date inputDate = dateFormat.parse(data.getDatePurchased());
            dateFormat = new SimpleDateFormat("MM/dd/yy");
            inputDate = dateFormat.parse(data.getDatePurchased());
            ExpirationUtility.calculateExpirationAndStatus(inputDate, holder.dateExpire_id, holder.status_id);

            // Get the status text to determine visibility
            String status = holder.status_id.getText().toString();

            // Set visibility of indicator based on status
            if (status.equals("For Refresh")) {
                holder.leftIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.leftIndicator.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.availability_id.setText(String.valueOf(data.getAvailability()));

        if (data != null && data.getName() != null && !data.getName().isEmpty()) {

            holder.hasUser.setVisibility(View.GONE);
        } else {
            holder.hasUser.setVisibility(View.VISIBLE);
        }

//        Edit
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

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editClickListener != null) {
//                    editClickListener.onEditClick(position);
                    editItem(context, position);
                }
            }
        });

        if (String.valueOf(data.getDevice()).equals("Laptop")) {
            holder.deviceIC.setImageResource(R.drawable.laptop_icon);
        } else if (String.valueOf(data.getDevice()).equals("Desktop")) {
            holder.deviceIC.setImageResource(R.drawable.pc_computer_6840);
        } else if (String.valueOf(data.getDevice()).equals("Phone")) {
            holder.deviceIC.setImageResource(R.drawable.mobile_phone_2635);
        } else if (String.valueOf(data.getDevice()).equals("Tablet")) {
            holder.deviceIC.setImageResource(R.drawable.ic_tablet);
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

    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setDeviceList(ArrayList<Assigned_to_User_Model> deviceList) {
        this.deviceList = deviceList;
    }

//    public void filter(String query) {
//        filteredData.clear();
//        if (query.isEmpty()) {
//            filteredData.addAll(originalData);
//        } else {
//            // Filter original data based on search query
//            query = query.toLowerCase();
//            for (Assigned_to_User_Model item : originalData) {
//                if (item.getName().toLowerCase().contains(query)) {
//                    filteredData.add(item);
//                }
//            }
//        }
//        notifyDataSetChanged(); // Notify RecyclerView that dataset has changed
//    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout linearLayout2, linearLayout2Alter;
        TextView serialNum_id, assignedTo_id, department_id, deviceModel_id, datePurchased_id, dateExpire_id, status_id, availability_id, sts2;
        ConstraintLayout otherInfo;
        ImageView hasUser, leftIndicator, imgScan, expiration, userIndicator, deviceIC, deviceIC2;
        CardView editBtn, deleteBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dbHelper = new DBHelper(context);
            serialNum_id = itemView.findViewById(R.id.sn);
            assignedTo_id = itemView.findViewById(R.id.at);
            department_id = itemView.findViewById(R.id.dep);
            deviceIC = itemView.findViewById(R.id.deviceIC);
            deviceIC2 = itemView.findViewById(R.id.deviceIC2);
            deviceModel_id = itemView.findViewById(R.id.dm);
            datePurchased_id = itemView.findViewById(R.id.dp);
            dateExpire_id = itemView.findViewById(R.id.de);
            status_id = itemView.findViewById(R.id.sts);
            availability_id = itemView.findViewById(R.id.avl);
            editBtn =  itemView.findViewById(R.id.editBtn);
            deleteBtn =  itemView.findViewById(R.id.deleteBtn);

            otherInfo = itemView.findViewById(R.id.otherInfo);

            imgScan = itemView.findViewById(R.id.imageViewScan);
            // Get the layout parameters of the view
            layoutParams = (ViewGroup.MarginLayoutParams) imgScan.getLayoutParams();
            leftIndicator = itemView.findViewById(R.id.leftIndicator);
            userIndicator = itemView.findViewById(R.id.userIndicator);
            hasUser = itemView.findViewById(R.id.hasUser);
            expiration = itemView.findViewById(R.id.expirationIndicator);

            sts2 = itemView.findViewById(R.id.sts2);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            Log.d("ItemAdapter", "onClick triggered");
            Log.d("ItemAdapter", "getName: " + data.getName());
            position = getAdapterPosition();
            data = deviceList.get(position);
            Log.d("ItemAdapter", "getSerial: " + data.getSerialNumber());
            Log.d("ItemAdapter", "getPosition: " + position);

            if (otherInfo != null) {
                if (otherInfo.getVisibility() == View.GONE) {
                    Log.d("ItemAdapter", "otherInfo is VISIBLE");
                    // otherInfo is closed
                    otherInfo.setVisibility(View.VISIBLE); // Set otherInfo to VISIBLE
                    editBtn.setVisibility(View.VISIBLE); // Set edit to VISIBLE
                    deleteBtn.setVisibility(View.VISIBLE); // Set delete to VISIBLE

                    hasUser.setVisibility(View.GONE);
                    leftIndicator.setVisibility(View.GONE);

                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally visible and getName is not null and expiration is not expire
                        userIndicator.setVisibility(View.GONE);
                    } else {
                        // otherInfo is finally visible and getName is null  and expiration is expired
                        userIndicator.setVisibility(View.VISIBLE);
                    }

                    try {
                        dateFormat = new SimpleDateFormat("MM/dd/yy");
                        inputDate = dateFormat.parse(data.getDatePurchased());
                        ExpirationUtility.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

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


                } else {


                    // otherInfo is visible
                    otherInfo.setVisibility(View.GONE); // Set otherInfo to GONE
                    editBtn.setVisibility(View.GONE); // Set edit to VISIBLE
                    deleteBtn.setVisibility(View.GONE); // Set delete to VISIBLE
                    if (!data.getName().isEmpty()) {
                        // otherInfo is finally GONE and getName is not null
                        hasUser.setVisibility(View.GONE);
                    } else {
                        // otherInfo is finally GONE and getName is null
                        hasUser.setVisibility(View.VISIBLE);
                    }

                    try {
                        dateFormat = new SimpleDateFormat("MM/dd/yy");
                        inputDate = dateFormat.parse(data.getDatePurchased());
                        ExpirationUtility.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

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
                }



                try {
                    dateFormat = new SimpleDateFormat("MM/dd/yy");
                    inputDate = dateFormat.parse(data.getDatePurchased());
                    ExpirationUtility.calculateExpirationAndStatus(inputDate, dateExpire_id, status_id);

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

}






























