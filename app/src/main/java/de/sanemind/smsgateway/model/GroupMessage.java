package de.sanemind.smsgateway.model;

import java.util.Date;

public class GroupMessage extends BaseMessage {
    public GroupMessage(Date createdAt, String message, String serviceID, GroupChat group, String username, boolean isSent) {
        super(createdAt, message, serviceID, isSent, true);
        this.group = group;
        this.username = username;
    }
    public GroupMessage(Date createdAt, String message, String serviceID, GroupChat group, String username, boolean isSent, boolean isReceived) {
        super(createdAt, message, serviceID, isSent, isReceived);
        this.group = group;
        this.username = username;
    }

    GroupChat group;
    String username;

    @Override
    public BaseChat getChat() {
        return group;
    }

    public String getUsername() {
        return username;
    }
}
