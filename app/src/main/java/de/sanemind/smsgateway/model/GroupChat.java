package de.sanemind.smsgateway.model;

public class GroupChat extends BaseChat {
    public GroupChat(String name, String groupIdentifier, boolean isChannel) {
        super(name, groupIdentifier);
        this.isChannel = isChannel;
    }

    private boolean isChannel;

    public boolean isChannel() {
        return isChannel;
    }
    @Override
    public String getDisplayName() {
        if (name != null) {
            if (isChannel)
                return "\uD83D\uDCEF" + name;
            else
                return "\uD83D\uDDE8" + name;
        }
        if (nameIdentifier != null)
            return nameIdentifier;
        return null;
    }

    @Override
    public String getIdentifier() {
        return nameIdentifier;
    }


}
