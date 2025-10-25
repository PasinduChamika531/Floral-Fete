package com.example.floralfete.model;

import java.util.List;

public class OrderModel {
    private String orderId, userId, userMobile, orderedDatetime, deliveryAddress, deliveryDate, orderStatus;
    private double totalAmount;
    private List<Product> products;

    public OrderModel() {

    }

    public OrderModel(String orderId, String userId, String userMobile, String orderedDatetime,
                      String deliveryAddress, String deliveryDate, double totalAmount,
                      String orderStatus, List<Product> products) {
        this.orderId = orderId;
        this.userId = userId;
        this.userMobile = userMobile;
        this.orderedDatetime = orderedDatetime;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDate = deliveryDate;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.products = products;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getUserMobile() { return userMobile; }
    public String getOrderedDatetime() { return orderedDatetime; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDeliveryDate() { return deliveryDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public List<Product> getProducts() { return products; }

    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }



}

