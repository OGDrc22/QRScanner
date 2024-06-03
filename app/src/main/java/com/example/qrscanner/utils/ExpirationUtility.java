package com.example.qrscanner.utils;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExpirationUtility {

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