package com.example.demoapp.model;

public class Message {
    private String from, message, type, to, messageUID, time, date, filename;

    public Message() {
    }

    public Message(String from, String message, String type, String to, String messageUID, String time, String date, String filename) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageUID = messageUID;
        this.time = time;
        this.date = date;
        this.filename = filename;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageUID() {
        return messageUID;
    }

    public void setMessageUID(String messageUID) {
        this.messageUID = messageUID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
