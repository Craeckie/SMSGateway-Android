package de.sanemind.smsgateway.model;

import java.util.Date;

public class GroupMessage extends BaseMessage {
    public GroupMessage(long ID, Date createdAt, String message, String serviceID, GroupChat group, UserChat user, boolean isSent, boolean isEdit) {
        super(ID, createdAt, message, serviceID, isSent, STATUS_FORWARDED, isEdit);
        this.group = group;
        this.user = user;
    }
    public GroupMessage(long ID, Date createdAt, String message, String serviceID, GroupChat group, UserChat user, boolean isSent, int status, boolean isEdit) {
        super(ID, createdAt, message, serviceID, isSent, status, isEdit);
        this.group = group;
        this.user = user;
    }

    private GroupChat group;
    private UserChat user;

    @Override
    public BaseChat getChat() {
        return group;
    }

    public UserChat getUser() {
        return user;
    }
}
