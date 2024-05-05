package com.example.qrscanner.options;

import com.example.qrscanner.R;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public static List<Gadgets> getGadgetsList() {
        List<Gadgets> gadgets = new ArrayList<>();

        Gadgets Unknown = new Gadgets();
        Unknown.setGadgetName("Unknown");
        Unknown.setImage(R.drawable.device_model);
        gadgets.add(Unknown);

        Gadgets Laptop = new Gadgets();
        Laptop.setGadgetName("Laptop");
        Laptop.setImage(R.drawable.laptop_icon);
        gadgets.add(Laptop);

        Gadgets Mobile = new Gadgets();
        Mobile.setGadgetName("Phone");
        Mobile.setImage(R.drawable.mobile_phone_2635);
        gadgets.add(Mobile);

        Gadgets Tablet = new Gadgets();
        Tablet.setGadgetName("Tablet");
        Tablet.setImage(R.drawable.tablet_698);
        gadgets.add(Tablet);

        Gadgets Desktop = new Gadgets();
        Desktop.setGadgetName("Desktop");
        Desktop.setImage(R.drawable.pc_computer_6840);
        gadgets.add(Desktop);

        return gadgets;
    }
}
