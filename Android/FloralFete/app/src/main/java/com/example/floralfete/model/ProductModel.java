package com.example.floralfete.model;

import java.io.Serializable;
import java.util.List;

public class ProductModel implements Serializable {

    private String id;
    private String name;
    private String description;
    private double price;
    private int qty;
    private String flowerTypeId;
    private List<String> occasionIds;
    private List<String> imageUrls;

    public ProductModel() {

    }

    public ProductModel(String name, String description, double price, int qty,
                        String flowerTypeId, List<String> occasionIds, List<String> imageUrls) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.qty = qty;
        this.flowerTypeId = flowerTypeId;
        this.occasionIds = occasionIds;
        this.imageUrls = imageUrls;
    }

    public ProductModel(String id, String name, String description, double price, int qty, String flowerTypeId, List<String> occasionIds, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.qty = qty;
        this.flowerTypeId = flowerTypeId;
        this.occasionIds = occasionIds;
        this.imageUrls = imageUrls;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }
    public String getFlowerTypeId() { return flowerTypeId; }
    public List<String> getOccasionIds() { return occasionIds; }
    public List<String> getImageUrls() { return imageUrls; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setFlowerTypeId(String flowerTypeId) {
        this.flowerTypeId = flowerTypeId;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }


    public void setOccasionIds(List<String> occasionIds) { this.occasionIds = occasionIds; }
}
