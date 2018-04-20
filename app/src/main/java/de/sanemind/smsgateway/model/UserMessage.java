package de.sanemind.smsgateway.model;

import java.util.Date;

public class UserMessage extends BaseMessage {
    public UserMessage(Date createdAt, String message, String serviceID, UserChat user, boolean isSent) {
        super(createdAt, message, serviceID, isSent, STATUS_FORWARDED);
        this.user = user;
    }
    public UserMessage(Date createdAt, String message, String serviceID, UserChat user, boolean isSent, int status) {
        super(createdAt, message, serviceID, isSent, status);
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
