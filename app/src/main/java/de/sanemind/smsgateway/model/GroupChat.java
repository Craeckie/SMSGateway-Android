package de.sanemind.smsgateway.model;

public class GroupChat extends BaseChat {
    public GroupChat(String name, String groupIdentifier) {
        super(name, groupIdentifier);
    }

    @Override
    public String getDisplayName() {
        if (name != null)
            return name;
        if (nameIdentifier != null)
            return nameIdentifier;
        return null;
    }

    @Override
    public String getIdentifier() {
        return nameIdentifier;
    }

}
