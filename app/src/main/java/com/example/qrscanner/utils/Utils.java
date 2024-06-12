package com.example.qrscanner.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.qrscanner.DB.DBHelper;
import com.example.qrscanner.R;
import com.example.qrscanner.allDevice;
import com.example.qrscanner.models.Assigned_to_User_Model;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Utils {


    public static byte[] imageViewToByte(Context context, ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            bitmap = getBitmapFromVectorDrawable(context, R.drawable.device_model);
        }

        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] getDefaultImageByteArray(Context context, int drawableId) {
        Bitmap bitmap = getBitmapFromDrawable(context, drawableId);
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    public static Bitmap getBitmapFromDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmapFromVectorDrawable(context, drawableId);
        }
        return null;
    }



    public static void showDeleteAllDialog(Context context, String identifier) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<Assigned_to_User_Model> deviceList;

        deviceList = dbHelper.fetchDevice();

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_delete_dialog, null);
        builder.setView(view);

        ((TextView) view.findViewById(R.id.messageText)).setText("Do you want to [ All " + identifier  +" ] ?" + "\n" + "\n" + "This action cannot be undone.");

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // function
        view.findViewById(R.id.actionDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteAll();
                deviceList.clear();
                Toast.makeText(context, "All Data Deleted", Toast.LENGTH_SHORT).show();
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
    }

    // Expiration calculator
    private static final String pattern = "MM/dd/yy";

    public static void calculateExpirationAndStatus(Date inputDate, TextView dateExpired, TextView status) {
        // Calculate expiration date
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(inputDate);
        expirationDate.add(Calendar.YEAR, 5); // Add 5 years

        // Get the expiration date after adding 5 years
        Date updatedExpirationDate = expirationDate.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        String formattedExpirationDate = dateFormat.format(updatedExpirationDate);

        // Set the expiration date to dateExpired TextView
        dateExpired.setText(formattedExpirationDate);

        // Get current date
        Calendar currentDate = Calendar.getInstance();

        if (currentDate.after(expirationDate)) {
            status.setText("For Refresh");
        } else {
            status.setText("Fresh");
        }
    }
}