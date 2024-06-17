package com.example.qrscanner.models;

public class Gadgets {

    private int id;
    private String gadgetName;
    private byte[] image;

    public Gadgets(int id, String gadgetName, byte[] image) {
        this.id = id;
        this.gadgetName = gadgetName;
        this.image = image;
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGadgetCategoryName() { return gadgetName; }
    public void setGadgetName(String gadgetName) { this.gadgetName = gadgetName; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}
