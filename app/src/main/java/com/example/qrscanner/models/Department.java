package com.example.qrscanner.models;

public class Department {

    public Department(int departmentCategoryId, String departmentCategoryName) {
        this.departmentCategoryId = departmentCategoryId;
        this.departmentCategoryName = departmentCategoryName;
    }

    public String getDepartmentCategoryName() {
        return departmentCategoryName;
    }

    public void setDepartmentCategoryName(String departmentCategoryName) {
        this.departmentCategoryName = departmentCategoryName;
    }

    public int getDepartmentCategoryId() {
        return departmentCategoryId;
    }

    public void setDepartmentCategoryId(int departmentCategoryId) {
        this.departmentCategoryId = departmentCategoryId;
    }

    private int departmentCategoryId;
    private String departmentCategoryName;
}
