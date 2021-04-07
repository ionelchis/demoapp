package com.example.demoapp.model;

public class User {
    private String username, full_name, info, profile_image;

    public User() {
    }

    public User(String username, String full_name, String info, String profile_image) {
        this.username = username;
        this.full_name = full_name;
        this.info = info;
        this.profile_image = profile_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }
}
