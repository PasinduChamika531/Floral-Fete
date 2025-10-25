package com.example.floralfete.model;

public class UserModel {
    private String userId, profileImageUrl, fname, lname, mobile, email, status;

    public UserModel() {

    }

    public UserModel(String userId, String profileImageUrl, String fname, String lname, String mobile, String email, String status) {
        this.userId = userId;
        this.profileImageUrl = profileImageUrl;
        this.fname = fname;
        this.lname = lname;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

