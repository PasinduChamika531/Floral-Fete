package com.example.floralfete.model;

public class CategoryModel {
    private String name;
    private int imageRes;

    public CategoryModel(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() {
        return name;
    }

    public int getImageRes() {
        return imageRes;
    }
}
