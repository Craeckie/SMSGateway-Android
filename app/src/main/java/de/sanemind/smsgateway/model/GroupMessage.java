package de.sanemind.smsgateway.model;

import java.util.Date;

public class GroupMessage extends BaseMessage {
    public GroupMessage(Date createdAt, String message, String serviceID, GroupChat group, UserChat user, boolean isSent) {
        super(createdAt, message, serviceID, isSent, STATUS_FORWARDED);
        this.group = group;
        this.user = user;
    }
    public GroupMessage(Date createdAt, String message, String serviceID, GroupChat group, UserChat user, boolean isSent, int status) {
        super(createdAt, message, serviceID, isSent, status);
        this.group = group;
        this.user = user;
    }

    GroupChat group;
    UserChat user;

    @Override
    public BaseChat getChat() {
        return group;
    }

    public UserChat getUser() {
        return user;
    }
}
