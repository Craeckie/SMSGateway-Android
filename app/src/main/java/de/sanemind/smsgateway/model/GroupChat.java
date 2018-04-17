package de.sanemind.smsgateway.model;

public class GroupChat extends BaseChat {
    public GroupChat(String name, String groupIdentifier) {
        super(name, groupIdentifier);
    }

    @Override
    public String getIdentifier() {
        return nameIdentifier;
    }

}
