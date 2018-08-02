package de.sanemind.smsgateway.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Date;

import de.sanemind.smsgateway.Messengers;

public abstract class BaseMessage implements Comparable<BaseMessage> {

    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;
    public static final int STATUS_FORWARDED = 3;

    private Date createdAt;
    private String message;
    private String serviceID;
    private boolean isSent;
    private int status;
//    private int index;
    private long ID;
    private boolean isEdit;
    private Buttons buttons;


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

    public ChatList getChatList(Context context) {
        return Messengers.listForIdentifier(context, serviceID);
    }

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

    public Buttons getButtons() {
        return buttons;
    }

    public void setButtons(Buttons buttons) {
        this.buttons = buttons;
    }

    //    public int getIndex() {
//        return index;
//    }
//
//    public void setIndex(int index) {
//        this.index = index;
//    }

    @Override
    public int compareTo(@NonNull BaseMessage o) {
        long oID = o.getID();
        if (oID != -1 || ID != -1) {
            return oID > ID ? +1 : oID < ID ? -1 : 0;
        } else  {
            return o.getCreatedAt().compareTo(this.getCreatedAt());
        }
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

