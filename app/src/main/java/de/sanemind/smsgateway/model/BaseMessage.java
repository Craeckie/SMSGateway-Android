package de.sanemind.smsgateway.model;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.sanemind.smsgateway.Messengers;

public abstract class BaseMessage implements Comparable<BaseMessage> {

    public static final int STATUS_SENT = 1;
    public static final int STATUS_RECEIVED = 2;
    public static final int STATUS_FORWARDED = 3;

    private Date createdAt;
    private String message;
    private String serviceID;
    private boolean isSent;
//    private int status;
//    private int index;
    private long ID;
    //private boolean isEdit;
    private Buttons buttons;
    @NonNull
    private MessageStatus status;
    private Map<String, String> otherHeaders;


    public BaseMessage(long ID,
                       Date createdAt,
                       String message,
                       String serviceID,
                       boolean isSent,
                       MessageStatus status) {
        this.ID = ID;
        this.createdAt = createdAt;
        this.message = message;
        this.serviceID = serviceID;
        this.isSent = isSent;
        this.status = status;
        this.otherHeaders = new HashMap<>();
    }
    public BaseMessage(long ID,
                       Date createdAt,
                       String message,
                       String serviceID,
                       boolean isSent,
                       MessageStatus status,
                       Map<String, String> otherHeaders) {
        this.ID = ID;
        this.createdAt = createdAt;
        this.message = message;
        this.serviceID = serviceID;
        this.isSent = isSent;
        this.status = status;
        this.otherHeaders = otherHeaders;
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

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Map<String, String> getOtherHeaders() {
        return otherHeaders;
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

