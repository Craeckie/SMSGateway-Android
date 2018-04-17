package de.sanemind.smsgateway.model;

import android.support.annotation.NonNull;

import java.util.Date;

public abstract class BaseMessage implements Comparable<BaseMessage> {

    private Date createdAt;
    private String message;
    private String serviceID;
    private boolean isSent;
    private boolean isReceived;


    public BaseMessage(Date createdAt, String message, String serviceID, boolean isSent, boolean isReceived) {
        this.createdAt = createdAt;
        this.message = message;
        this.serviceID = serviceID;
        this.isSent = isSent;
        this.isReceived = isReceived;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }

    public String getServiceID() { return serviceID; }

    public boolean isSent() {
        return isSent;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
        createdAt = new Date();
    }

    public abstract BaseChat getChat();

    @Override
    public int compareTo(@NonNull BaseMessage o) {
        return o.getCreatedAt().compareTo(this.getCreatedAt());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseMessage) {
            BaseMessage m = ((BaseMessage)obj);
            return m.getChat().equals(getChat()) && m.getMessage().equals(getMessage());
        }
        return super.equals(obj);
    }
}

