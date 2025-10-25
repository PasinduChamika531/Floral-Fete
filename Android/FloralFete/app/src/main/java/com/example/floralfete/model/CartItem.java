package com.example.floralfete.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class CartItem implements Parcelable {
    private String productId;
    private String productName;
    private String productImage;
    private double productPrice;
    private int quantity;


    public CartItem() {
    }

    public CartItem(String productId, String productName, String productImage, double productPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }


    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public int getQuantity() {
        return quantity;
    }


    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("productImage", productImage);
        map.put("productPrice", productPrice);
        map.put("quantity", quantity);
        return map;
    }


    public static CartItem fromCursor(Cursor cursor) {
        String productId = cursor.getString(cursor.getColumnIndex("productId"));
        String productName = cursor.getString(cursor.getColumnIndex("productName"));
        String productImage = cursor.getString(cursor.getColumnIndex("productImage"));
        double productPrice = cursor.getDouble(cursor.getColumnIndex("productPrice"));
        int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));

        return new CartItem(productId, productName, productImage, productPrice, quantity);
    }


    protected CartItem(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        productImage = in.readString();
        productPrice = in.readDouble();
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeString(productImage);
        dest.writeDouble(productPrice);
        dest.writeInt(quantity);
    }
}
