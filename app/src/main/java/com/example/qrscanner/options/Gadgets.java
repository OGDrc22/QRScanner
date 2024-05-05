package com.example.qrscanner.options;

import java.io.Serializable;

public class Gadgets implements Serializable {

    private String gadgetName;
    private int image;

    public Gadgets() {
    }

    public String getGadgetName() {
        return gadgetName;
    }

    public void setGadgetName(String gadgetName) {
        this.gadgetName = gadgetName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
