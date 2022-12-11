package com.mju.exercise.Domain;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class SendNotiDTO {

    @SerializedName("to")
    String to;
    @SerializedName("priority")
    String priority;
    @SerializedName("notification")
    HashMap<String, String> notification;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public HashMap<String, String> getNotification() {
        return notification;
    }

    public void setNotification(HashMap notification) {
        this.notification = notification;
    }
}
