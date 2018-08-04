package de.sanemind.smsgateway;

import android.content.Context;
import android.preference.PreferenceManager;

import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.UserChat;

public class Messengers {

    private static ContactsLoader contactsLoader = new ContactsLoader();

    private static ChatList TG = new ChatList("TG", contactsLoader);
    private static ChatList FB = new ChatList("FB", contactsLoader);
    private static ChatList SMS = new ChatList("SMS", contactsLoader);

    public static final UserChat GatewayUser = new UserChat(SMS,"Gateway", "Gateway");

    public static ChatList getTG(Context context) {
        fillIfEmpty(context);
        return TG;
    }

    public static ChatList getFB(Context context) {
        fillIfEmpty(context);
        return FB;
    }

    public static ChatList getSMS(Context context) {
        fillIfEmpty(context);
        return SMS;
    }

    private static void fillIfEmpty(Context context) {
        if (SMS.isEmpty()) {
            contactsLoader.loadContacts(context);

            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
            GatewayUser.addPhoneNumber(context, gatewayNumber, 1);
            SMS.ChatList.add(GatewayUser);
        }
    }

    public static ChatList listForIdentifier(Context context, String identifier) {
        fillIfEmpty(context);
        switch (identifier) {
            case "TG":
                return TG;
            case "FB":
                return FB;
            case "SMS":
                return SMS;
            default:
                return null;
        }
    }

    public static String identifierForList(Context context, ChatList list) {
        fillIfEmpty(context);
        if (list == TG)
            return "TG";
        else if (list == FB)
            return "FB";
        else if (list == SMS)
            return "SMS";
        else
            throw new IllegalArgumentException("Unknown messenger list: " + list);
    }

    public static void cleanChats() {
        TG.cleanChatList();
        FB.cleanChatList();
        SMS.cleanChatList();
    }
}
