package de.sanemind.smsgateway.model;

import java.util.Date;

public class UserMessage extends BaseMessage {
    public UserMessage(long ID, Date createdAt, String message, String serviceID, UserChat user, boolean isSent, boolean isEdit) {
        super(ID, createdAt, message, serviceID, isSent, STATUS_FORWARDED, isEdit);
        this.user = user;
    }
    public UserMessage(long ID, Date createdAt, String message, String serviceID, UserChat user, boolean isSent, int status, boolean isEdit) {
        super(ID, createdAt, message, serviceID, isSent, status, isEdit);
        this.user = user;
    }

    UserChat user;

    public UserChat getUser() {
        return user;
    }


    @Override
    public BaseChat getChat() {
        return user;
    }
}
