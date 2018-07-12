package de.sanemind.smsgateway.model;

import android.support.annotation.NonNull;

import java.util.Date;

public abstract class BaseMessage implements Comparable<BaseMessage> {

    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;
    public static final int STATUS_FORWARDED = 3;

    private Date createdAt;
    private String message;
    private String serviceID;
    private boolean isSent;
    private int status;
    private int index;
    private long ID;
    private boolean isEdit;


    public BaseMessage(long ID, Date createdAt, String message, String serviceID, boolean isSent, int status, boolean isEdit) {
        this.ID = ID;
        this.createdAt = createdAt;
        this.message = message;
        this.serviceID = serviceID;
        this.isSent = isSent;
        this.status = status;
        this.isEdit = isEdit;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getServiceID() { return serviceID; }

    public boolean isSent() {
        return isSent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public long getID() {
        return ID;
    }
    public abstract BaseChat getChat();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

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

