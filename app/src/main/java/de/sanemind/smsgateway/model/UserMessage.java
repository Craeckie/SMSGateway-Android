package de.sanemind.smsgateway.model;

import java.util.Date;
import java.util.Map;

public class UserMessage extends BaseMessage {
    public UserMessage(long ID,
                       Date createdAt,
                       String message,
                       String serviceID,
                       UserChat user,
                       boolean isSent,
                       MessageStatus status) {
        super(ID, createdAt, message, serviceID, isSent, status);
        this.user = user;
    }
    public UserMessage(long ID,
                       Date createdAt,
                       String message,
                       String serviceID,
                       UserChat user,
                       boolean isSent,
                       MessageStatus status,
                       Map<String, String> otherHeaders) {
        super(ID, createdAt, message, serviceID, isSent, status, otherHeaders);
        this.user = user;
    }
//    public UserMessage(long ID,
//                       Date createdAt,
//                       String message,
//                       String serviceID,
//                       UserChat user,
//                       boolean isSent,
//                       MessageStatus status,
//                       boolean isEdit,
//                       Map<String, String> otherHeaders) {
//        super(ID, createdAt, message, serviceID, isSent, status, otherHeaders);
//        this.user = user;
//    }

    UserChat user;

    public UserChat getUser() {
        return user;
    }


    @Override
    public BaseChat getChat() {
        return user;
    }
}
