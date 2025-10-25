package com.example.floralfete.model;

import java.util.List;

public class Order {
    private String orderId;
    private double totalAmount;
    private String deliveryDate;
    private String orderStatus;

    private double orderedDatetime;
    private List<Product> products;


    public Order() {
    }

    public Order(String orderId, double totalAmount, String deliveryDate, String orderStatus, List<Product> products) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.deliveryDate = deliveryDate;
        this.orderStatus = orderStatus;
        this.products = products;
    }

    public Order(String orderId, double totalAmount, String deliveryDate, String orderStatus, double orderedDatetime, List<Product> products) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.deliveryDate = deliveryDate;
        this.orderStatus = orderStatus;
        this.orderedDatetime = orderedDatetime;
        this.products = products;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public double getOrderedDatetime() {
        return orderedDatetime;
    }

    public void setOrderedDatetime(double orderedDatetime) {
        this.orderedDatetime = orderedDatetime;
    }
}
