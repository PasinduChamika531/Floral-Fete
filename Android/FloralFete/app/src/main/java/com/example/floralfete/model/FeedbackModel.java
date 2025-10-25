package com.example.floralfete.model;

public class FeedbackModel {
    private String feedback;
    private String productId;
    private String userId;
    private String username;

    public FeedbackModel() {
    }

    public FeedbackModel(String feedback, String productId, String userId, String username) {
        this.feedback = feedback;
        this.productId = productId;
        this.userId = userId;
        this.username = username;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
