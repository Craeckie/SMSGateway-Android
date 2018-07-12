package de.sanemind.smsgateway;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.UserChat;

public class ChatList {
    protected static final List<BaseChat> ChatList = new ArrayList<>();


    public static final UserChat GatewayUser = new UserChat("Gateway", "Gateway");

    public static Boolean isEmpty() {
        return ChatList.isEmpty();
    }

    private static void fillIfEmpty(Context context) {
        if (ChatList.isEmpty()) {
            //ChatList.add(currentUser);
            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
            GatewayUser.addPhoneNumber(context, gatewayNumber, 1);
            ChatList.add(GatewayUser);
        }
    }

    public static GroupChat get_or_create_group(Context context, String groupName, String groupIdentifier, boolean isChannel) {
        GroupChat g = find_group(context, groupIdentifier, isChannel);
        if (g != null)
            return g;
        else {
            g = new GroupChat(groupName, groupIdentifier, isChannel);
            ChatList.add(g);
            return g;
        }
    }
    public static GroupChat find_group(Context context, String groupIdentifier, boolean isChannel) {
        fillIfEmpty(context);
        for (BaseChat chat:ChatList) {
            if (chat instanceof GroupChat) {
                GroupChat group = (GroupChat) chat;
                if (group.getIdentifier().equals(groupIdentifier) && group.isChannel() == isChannel) {
                    return group;
                }
            }
        }
        return null;
    }
    public static UserChat get_or_create_user(Context context, String name, String nameIdentifier, String phoneNumber) {
        fillIfEmpty(context);

        for (BaseChat chat:ChatList) {
            if (chat instanceof UserChat) {
                UserChat user = (UserChat) chat;
                if (phoneNumber != null && user.hasPhoneNumber(context, phoneNumber)) {
//                    Log.v("SMSGateway", "user");
                    return user;
                }
                String currentNameIdentifier = user.getNameIdentifier();
                if (nameIdentifier != null && currentNameIdentifier != null && currentNameIdentifier.equals(nameIdentifier)) {
                    return user;
                }
                String currentName = user.getName();
                if (name != null && currentName != null && currentName.equals(name)) {
//                    Log.v("SMSGateway", "user");
                    return user;
                }
            }
        }
        UserChat user = new UserChat(name, nameIdentifier);
        if (phoneNumber != null)
            user.addPhoneNumber(context, phoneNumber, 1);
        ContactsLoader.updateUserChat(context, user);

        ChatList.add(user);
        return user;
    }
}
