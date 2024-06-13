package com.example.qrscanner.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.qrscanner.models.Assigned_to_User_Model;
import com.example.qrscanner.models.Department;
import com.example.qrscanner.models.Gadgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Assigned_To_User";
    private static final int DATABASE_VERSION = 2;


    private static final String TABLE_ASSIGNED_TO_USER = "Assigned_Laptop";
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

    //TABLE FOR GADGET CATEGORY OPTION
    private static final String TABLE_GADGETS_CATEGORY = "Gadgets_Category_Option";
    private static final String KEY_GADGETS_CATEGORY_ID = "gadgets_id";
    private static final String KEY_GADGETS_CATEGORY_NAME = "gadgets_name";
    private static final String KEY_GADGETS_CATEGORY_IMAGE = "gadgets_image";

    // TABLE FOR DEPARTMENT CATEGORY
    private static final String TABLE_DEPARTMENT_CATEGORY = "Department_Category";
    private static final String KEY_DEPARTMENT_CATEGORY_ID = "department_id";
    private static final String KEY_DEPARTMENT_CATEGORY_NAME = "department_name";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TABLE FOR ASSIGNED TO USER
        db.execSQL("CREATE TABLE " + TABLE_ASSIGNED_TO_USER + "("
                + KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SERIAL_NUMBER + " INTEGER, "
                + KEY_NAME + " TEXT, "
                + KEY_DEPARTMENT + " TEXT, "
                + KEY_DEVICE + " TEXT, "
                + KEY_DEVICE_MODEL + " TEXT, "
                + KEY_DATE_PURCHASED + " TEXT, "
                + KEY_DATE_EXPIRED + " TEXT, "
                + KEY_STATUS + " TEXT, "
                + KEY_AVAILABILITY + " TEXT) ");

        // TABLE FOR GADGETS OPTION
        db.execSQL("CREATE TABLE " + TABLE_GADGETS_CATEGORY + "("
                + KEY_GADGETS_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_GADGETS_CATEGORY_NAME + " TEXT, "
                + KEY_GADGETS_CATEGORY_IMAGE + " BLOB)");

        // TABLE FOR DEPARTMENT CATEGORY
        db.execSQL("CREATE TABLE " + TABLE_DEPARTMENT_CATEGORY + "("
                + KEY_DEPARTMENT_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_DEPARTMENT_CATEGORY_NAME + " TEXT)");

//        SQLiteDatabase database = this.getWritableDatabase();

        // Query to Insert

//        database.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            // Create the new table without affecting existing data
            String CREATE_GADGETS_OPTION_TABLE = "CREATE TABLE " + TABLE_GADGETS_CATEGORY + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_GADGETS_CATEGORY_NAME + " TEXT, " +
                    KEY_GADGETS_CATEGORY_IMAGE + " INTEGER)";
            db.execSQL(CREATE_GADGETS_OPTION_TABLE);
        }

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSIGNED_TO_USER);
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
//            db.insert(TABLE_ASSIGNED_TO_USER, null, values);
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

        database.insert(TABLE_ASSIGNED_TO_USER, null, values);

    }

    public ArrayList<Assigned_to_User_Model> fetchDevice() {
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT al.*, go." + KEY_GADGETS_CATEGORY_IMAGE + " FROM " + TABLE_ASSIGNED_TO_USER + " al LEFT JOIN " + TABLE_GADGETS_CATEGORY + " go ON al.device = go." + KEY_GADGETS_CATEGORY_NAME;
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<Assigned_to_User_Model> arrayList = new ArrayList<>();

        while (cursor.moveToNext()) {
            Assigned_to_User_Model assignedToUserModel = new Assigned_to_User_Model();

            // TABLE_ASSIGNED_TO_USER TABLE_ASSIGNED_TO_USER
            assignedToUserModel.id = cursor.getInt(0);
            assignedToUserModel.serial_number = cursor.getInt(1);
            assignedToUserModel.name = cursor.getString(2);
            assignedToUserModel.department = cursor.getString(3);
            assignedToUserModel.device = cursor.getString(4);
            assignedToUserModel.device_brand = cursor.getString(5);
            assignedToUserModel.date_purchased = cursor.getString(6);
            assignedToUserModel.date_expired = cursor.getString(7);
            assignedToUserModel.status = cursor.getString(8);
            assignedToUserModel.availability = cursor.getString(9);

            // Gadget Image TABLE_GADGETS_CATEGORY
            // Get the image from the database and pass it in assignedToUserModel object
            assignedToUserModel.image = (cursor.getBlob(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_IMAGE)));

            // Debugging Logs
//            Log.d("FetchDevice", "Serial Number: " + assignedToUserModel.serial_number);
//            Log.d("FetchDevice", "Assigned to: " + assignedToUserModel.name);
//            Log.d("FetchDevice", "Device Model: " + assignedToUserModel.device_brand);
            arrayList.add(assignedToUserModel);
        }

        cursor.close();
        return arrayList;
    }

    public void deleteDeviceBySerialNumber(String serialNumber) {
        Log.d("TAG", "deleteDeviceBySerialNumber: deleted");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASSIGNED_TO_USER, KEY_SERIAL_NUMBER + " = ?", new String[]{serialNumber});
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

        db.update(TABLE_ASSIGNED_TO_USER, values, KEY_SERIAL_NUMBER + " = ?", new String[]{serialNumber});

        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASSIGNED_TO_USER, null, null);
        db.close();
    }

    public void deleteDevice(Assigned_to_User_Model device) {
        // Get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the table name and the WHERE clause for the DELETE statement
        String whereClause = KEY_DEVICE + " = ?";
        String[] whereArgs = {device.getDeviceType()}; // Provide the device name as the WHERE clause argument

        // Execute the DELETE statement
        db.delete(TABLE_ASSIGNED_TO_USER, whereClause, whereArgs);

        // Close the database connection
        db.close();
    }

    public void deleteDeviceNoUser(Assigned_to_User_Model device) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = KEY_NAME + " = ?";
        String[] selectionArgs = {device.getName()};
        db.delete(TABLE_ASSIGNED_TO_USER, selection, selectionArgs);
        db.close();
    }
    public void deleteExpiredDevice(Assigned_to_User_Model device) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = KEY_DATE_EXPIRED + " = ?";
        String[] selectionArgs = {device.getDateExpired()};
        db.delete(TABLE_ASSIGNED_TO_USER, selection, selectionArgs);
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

    public void addGadgetCategory(String name, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_GADGETS_CATEGORY_NAME, name);
        values.put(KEY_GADGETS_CATEGORY_IMAGE, image);
        Log.d("DB", "addGadgetCategory: Called");
        db.insert(TABLE_GADGETS_CATEGORY, null, values);
    }

    public List<Gadgets> getAllGadgetsCategory() {
        List<Gadgets> gadgetsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GADGETS_CATEGORY, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_NAME));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_IMAGE));
                Gadgets gadget = new Gadgets(id, name, image);
                gadgetsList.add(gadget);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return gadgetsList;
    }

    public void updateGadgetCategory(Gadgets gadget, String newName, byte[] newImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_GADGETS_CATEGORY_NAME, newName);
        values.put(KEY_GADGETS_CATEGORY_IMAGE, newImage);

        Log.d("DB", "Updating Gadget ID: " + gadget.getId() + " with Name: " + newName + " and Image: " + newImage);

        int rowsAffected = db.update(TABLE_GADGETS_CATEGORY, values, KEY_GADGETS_CATEGORY_ID + " = ?", new String[]{String.valueOf(gadget.getId())});
        if (rowsAffected > 0){
            Log.d("DB", "updateGadget: updated");
        } else {
            Log.d("DB", "updateGadget: not affected");
        }
    }

    public void deleteGadgetCategory(Gadgets gadget) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_GADGETS_CATEGORY_ID + " = ?";

        Log.d("DB", "Deleting Gadget ID: " + gadget.getId());

        int rowsAffected = db.delete(TABLE_GADGETS_CATEGORY, whereClause, new String[]{String.valueOf(gadget.getId())});
        if (rowsAffected > 0) {
            Log.d("DB", "deleteGadget: deleted");
        } else {
            Log.d("DB", "deleteGadget: not affected");
        }
    }

//    public byte[] getGadgetImage(Bitmap gadgetId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String query = "SELECT " + KEY_GADGETS_CATEGORY_IMAGE + " FROM " + TABLE_GADGETS_CATEGORY + " WHERE " + KEY_GADGETS_CATEGORY_ID + " = ?";
//        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(gadgetId) });
//
//        byte[] image = null;
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                image = cursor.getBlob(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_IMAGE));
//            }
//            cursor.close();
//        }
//        db.close();
//        return image;
//    }

    public byte[] getGadgetCategoryImageInt(int gadgetIntId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_GADGETS_CATEGORY_IMAGE + " FROM " + TABLE_GADGETS_CATEGORY + " WHERE " + KEY_GADGETS_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(gadgetIntId) });

        byte[] image = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                image = cursor.getBlob(cursor.getColumnIndex(KEY_GADGETS_CATEGORY_IMAGE));
            }
            cursor.close();
        }
        db.close();
        return image;
    }

    public void addDepartmentCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_DEPARTMENT_CATEGORY_NAME, name);

        db.insert(TABLE_DEPARTMENT_CATEGORY, null, values);
    }

    public void editDepartmentCategory(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DEPARTMENT_CATEGORY_NAME, name);
        db.update(TABLE_DEPARTMENT_CATEGORY, values, KEY_DEPARTMENT_CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<Department> getAllDepartmentCategory() {
        List<Department> departmentList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + TABLE_DEPARTMENT_CATEGORY, null);
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_DEPARTMENT_CATEGORY_ID);
                int nameIndex = cursor.getColumnIndex(KEY_DEPARTMENT_CATEGORY_NAME);
                do {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    Department department = new Department(id, name);
                    departmentList.add(department);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log the exception or handle it as necessary
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return departmentList;
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
//    long newRowId = db.insert(TABLE_ASSIGNED_TO_USER, null, values);
