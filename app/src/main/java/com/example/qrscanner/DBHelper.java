package com.example.qrscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Assigned_To_User";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_NAME = "Assigned_Laptop";
    // Add Key to add Row
    private static final String KEY_ID = "id";
    private static final String KEY_SERIAL_NUMBER = "serial_number";
    private static final String KEY_NAME = "name";
    private static final String KEY_DEPARTMENT = "department";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_DATE_PURCHASED = "date_purchased";
    private static final String KEY_DATE_EXPIRED = "date_expired";
    private static final String KEY_STATUS = "status";
    private static final String KEY_AVAILABILITY = "availability";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_SERIAL_NUMBER + " INTEGER, " + KEY_NAME + " TEXT, " + KEY_DEPARTMENT + " TEXT, "  + KEY_DEVICE + " TEXT, "  + KEY_DEVICE_MODEL + " TEXT, " + KEY_DATE_PURCHASED + " TEXT, " + KEY_DATE_EXPIRED + " TEXT, " + KEY_STATUS +" TEXT, " + KEY_AVAILABILITY + " TEXT)");

//        SQLiteDatabase database = this.getWritableDatabase();

        // Query to Insert

//        database.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);

    }

    public void addDevice(String serialNum, String name, String department, String device, String deviceModel, String datePurchased, String dateExpired, String status, String availability) { // Get the qr data from the scanner (getValue)

        // Assuming you have the scanned content as 'qrContent'
        // Rules
//        String[] parts = qrContent.split(" ");
//        // Check if there are at least two parts
//        if (parts.length >= 2) {
//            String serialNum = parts[0].trim();
//            String name = parts[1].trim();
//
//            // Insert into the database
//            SQLiteDatabase db = this.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put(KEY_SERIAL_NUMBER, serialNum);
//            values.put(KEY_NAME, name);
//            db.insert(TABLE_NAME, null, values);
//        } else {
//            // Handle case where qrContent does not contain enough parts
//            // For example, log an error or show a message to the user
//        }

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SERIAL_NUMBER, serialNum);
        values.put(KEY_NAME, name);
        values.put(KEY_DEPARTMENT, department);
        values.put(KEY_DEVICE, device);
        values.put(KEY_DEVICE_MODEL, deviceModel);
        values.put(KEY_DATE_PURCHASED, datePurchased);
        values.put(KEY_DATE_EXPIRED, dateExpired);
        values.put(KEY_STATUS, status);
        values.put(KEY_AVAILABILITY, availability);

        database.insert(TABLE_NAME, null, values);

    }

    public ArrayList<Assigned_to_User_Model> fetchDevice() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " +TABLE_NAME, null);
        ArrayList<Assigned_to_User_Model> arrayList = new ArrayList<>();

        while (cursor.moveToNext()) {
            Assigned_to_User_Model assignedToUserModel = new Assigned_to_User_Model();

            assignedToUserModel.id = cursor.getInt(0);
            assignedToUserModel.serial_number = cursor.getInt(1);
            assignedToUserModel.name = cursor.getString(2);
            assignedToUserModel.department = cursor.getString(3);
            assignedToUserModel.device = cursor.getString(4);
            assignedToUserModel.device_model = cursor.getString(5);
            assignedToUserModel.date_purchased = cursor.getString(6);
            assignedToUserModel.date_expired = cursor.getString(7);
            assignedToUserModel.status = cursor.getString(8);
            assignedToUserModel.availability = cursor.getString(9);

            Log.d("FetchDevice", "Serial Number: " + assignedToUserModel.serial_number);
            Log.d("FetchDevice", "Assigned to: " + assignedToUserModel.name);
            Log.d("FetchDevice", "Device Model: " + assignedToUserModel.device_model);
            arrayList.add(assignedToUserModel);
        }

        cursor.close();
        return arrayList;
    }

    public void deleteDeviceBySerialNumber(String serialNumber) {
        Log.d("TAG", "deleteDeviceBySerialNumber: deleted");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_SERIAL_NUMBER + " = ?", new String[]{serialNumber});
        db.close();
    }

    public void editDevice(String serialNumber, String newName, String newDepartment, String newDevice, String newDeviceModel, String newDatePurchased, String newDateExpired, String newStatus, String newAvailability) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (newName != null) values.put(KEY_NAME, newName);
        if (newDepartment != null) values.put(KEY_DEPARTMENT, newDepartment);
        if (newDevice != null) values.put(KEY_DEVICE, newDevice);
        if (newDeviceModel != null) values.put(KEY_DEVICE_MODEL, newDeviceModel);
        if (newDatePurchased != null) values.put(KEY_DATE_PURCHASED, newDatePurchased);
        if (newDateExpired != null) values.put(KEY_DATE_EXPIRED, newDateExpired);
        if (newStatus != null) values.put(KEY_STATUS, newStatus);
        if (newAvailability != null) values.put(KEY_AVAILABILITY, newAvailability);

        db.update(TABLE_NAME, values, KEY_SERIAL_NUMBER + " = ?", new String[]{serialNumber});

        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public void deleteDevice(Assigned_to_User_Model device) {
        // Get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the table name and the WHERE clause for the DELETE statement
        String whereClause = KEY_DEVICE + " = ?";
        String[] whereArgs = {device.getDevice()}; // Provide the device name as the WHERE clause argument

        // Execute the DELETE statement
        db.delete(TABLE_NAME, whereClause, whereArgs);

        // Close the database connection
        db.close();
    }

    public void deleteDeviceNoUser(Assigned_to_User_Model device) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = KEY_NAME + " = ?";
        String[] selectionArgs = {device.getName()};
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }
    public void deleteExpiredDevice(Assigned_to_User_Model device) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = KEY_DATE_EXPIRED + " = ?";
        String[] selectionArgs = {device.getDateExpired()};
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public ArrayList<String> getAllSerialNumbers() {
        ArrayList<String> allSerialNumbers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define a query to select all serial numbers from your table
            String query = "SELECT serial_number FROM Assigned_Laptop";

            // Execute the query
            cursor = db.rawQuery(query, null);

            // Loop through the cursor to retrieve serial numbers
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String serialNumber = cursor.getString(cursor.getColumnIndex("serial_number"));
                    allSerialNumbers.add(serialNumber);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving serial numbers: " + e.getMessage());
        } finally {
            // Close the cursor and database connection
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        Collections.reverse(allSerialNumbers);

        return allSerialNumbers;
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
