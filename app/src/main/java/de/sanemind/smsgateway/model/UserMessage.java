package de.sanemind.smsgateway.model;

import java.util.Date;

public class UserMessage extends BaseMessage {
    public UserMessage(Date createdAt, String message, String serviceID, UserChat user, boolean isSent) {
        super(createdAt, message, serviceID, isSent, true);
        this.user = user;
    }
    public UserMessage(Date createdAt, String message, String serviceID, UserChat user, boolean isSent, boolean isReceived) {
        super(createdAt, message, serviceID, isSent, isReceived);
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
