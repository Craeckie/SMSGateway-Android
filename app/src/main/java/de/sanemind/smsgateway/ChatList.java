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

    public static Boolean isEmpty() {
        return ChatList.isEmpty();
    }

    private static void fillIfEmpty(Context context) {
        if (ChatList.isEmpty()) {
            //ChatList.add(currentUser);
            UserChat user = new UserChat("Gateway", "Gateway");
            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
            user.addPhoneNumber(gatewayNumber, 1);
            ChatList.add(user);
        }
    }

    public static GroupChat get_or_create_group(Context context, String groupName, String groupIdentifier) {
        GroupChat g = find_group(context, groupIdentifier);
        if (g != null)
            return g;
        else {
            g = new GroupChat(groupName, groupIdentifier);
            ChatList.add(g);
            return g;
        }
    }
    public static GroupChat find_group(Context context, String groupIdentifier) {
        fillIfEmpty(context);
        for (BaseChat chat:ChatList) {
            if (chat instanceof GroupChat) {
                GroupChat group = (GroupChat) chat;
                if (group.getIdentifier().equals(groupIdentifier)) {
                    return group;
                }
            }
        }
        return null;
    }
    public static UserChat get_or_create_user(Context context, String name, String nameIdentifier) {
        fillIfEmpty(context);
        if (name.contains("995")) {
//            Log.v("SMSGateway", "Thomas");
        }

        for (BaseChat chat:ChatList) {
            if (chat instanceof UserChat) {
                UserChat user = (UserChat) chat;
                if (user.hasPhoneNumber(name)) {
//                    Log.v("SMSGateway", "user");
                    return user;
                }
                if (user.getName().equals(name)) {
//                    Log.v("SMSGateway", "user");
                    return user;
                }
            }
        }
        UserChat user = new UserChat(name, nameIdentifier);
        ContactsLoader.updateUserChat(context, user);
        ChatList.add(user);
        return user;
    }
    public static UserChat get_or_create_user(Context context, String name) {
        return get_or_create_user(context, name, name);
    }
}
