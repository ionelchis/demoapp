package com.example.demoapp.model;

public class Chat {
    private String name, profile_image, date_created, time;
    private ChatType type;

    public Chat() {}

    public Chat(String name, String profile_image, String date_created, String time, ChatType type) {
        this.name = name;
        this.profile_image = profile_image;
        this.date_created = date_created;
        this.time = time;
        this.type = type;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "name='" + name + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", date_created='" + date_created + '\'' +
                ", time='" + time + '\'' +
                ", type=" + type +
                '}';
    }
}
