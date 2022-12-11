package com.mju.exercise.Domain;

import com.google.gson.annotations.SerializedName;

public class SendNotiDTO {

    @SerializedName("to")
    String to;
    @SerializedName("priority")
    String priority;
    @SerializedName("notification")
    Object notification;

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

    public Object getNotification() {
        return notification;
    }

    public void setNotification(Object notification) {
        this.notification = notification;
    }
}
