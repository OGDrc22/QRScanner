package com.example.qrscanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrscanner.R;
import com.example.qrscanner.models.Department;

import java.util.List;

public class DepartmentAdapter extends BaseAdapter {

    private Context context;
    private List<Department> departmentList;

    public DepartmentAdapter(Context context, List<Department> departmentList) {
        this.context = context;
        this.departmentList = departmentList;
    }

    @Override
    public int getCount() {
        return departmentList != null ? departmentList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return departmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gadget_option, parent, false);
        }
        TextView dptName = convertView.findViewById(R.id.name);

        ImageView dptImage = convertView.findViewById(R.id.image);
        dptImage.setVisibility(View.GONE);

        dptName.setText(departmentList.get(position).getDepartmentCategoryName());
        return convertView;
    }
}
