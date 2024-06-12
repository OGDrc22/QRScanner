package com.example.qrscanner.models;

public class Assigned_to_User_Model {


    private int originalPosition;
    public int id, serial_number;
    public byte[] image;
    public String name;
    public String department;
    public String device_brand;
    public String device;
    public String date_purchased;
    public String date_expired;
    public String status;
    public String availability;

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

    public String getDeviceBrand() {
        return device_brand;
    }
    public String getDeviceType() {
        return device;
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

    public byte[] getImage() {
        return image;
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

    public void setDevice_brand(String device_brand) {
        this.device_brand = device_brand;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public void setImage(byte[] image) {
        this.image = image;
    }
}
