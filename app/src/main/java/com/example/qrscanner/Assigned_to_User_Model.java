package com.example.qrscanner;

public class Assigned_to_User_Model {
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String device;
    private int originalPosition;
    int id, serial_number;
    String name;
    String department;
    String gadgetName;
    String device_model;
    String date_purchased;
    String date_expired;
    String status;
    String availability;


    // Getter methods
    public int getId() {
        return id;
    }

    public int getSerialNumber() {
        return serial_number;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getDeviceModel() {
        return device_model;
    }

    public String getDatePurchased() {
        return date_purchased;
    }

    public String getDateExpired() {
        return date_expired;
    }

    public String getStatus() {
        return status;
    }

    public String getAvailability() {
        return availability;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setSerial_number(int serial_number) {
        this.serial_number = serial_number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

    public void setDate_purchased(String date_purchased) {
        this.date_purchased = date_purchased;
    }

    public void setDate_expired(String date_expired) {
        this.date_expired = date_expired;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }


    public void setOriginalPosition(int i) {
        this.originalPosition = originalPosition;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

}
