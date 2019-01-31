package de.sanemind.smsgateway;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.UserChat;

public class Messengers {

    private static ContactsLoader contactsLoader = new ContactsLoader();

    private static Map<String, ChatList> chatMap;
    private static ArrayList<ChatList> chatList;
    private static ChatList SMS = new ChatList("SMS", "SMS", contactsLoader);

    public static final UserChat GatewayUser = new UserChat(SMS,"Gateway", "Gateway");

    private static void fillIfEmpty(Context context) {
        if (chatList == null) {
            chatList = new ArrayList<>();

            chatList.add(new ChatList("TG", "Telegram", contactsLoader));
            chatList.add(new ChatList("FB", "Facebook", contactsLoader));
            chatList.add(new ChatList("SG", "Signal", contactsLoader));
            chatList.add(new ChatList("SL", "Slack", contactsLoader));
            chatList.add(new ChatList("EM", "E-Mail", contactsLoader));
            chatList.add(SMS);

            chatMap = new HashMap<>();
            for (ChatList list : chatList)
                chatMap.put(list.getIdentifier(), list);
        }
        if (SMS.isEmpty()) {
            contactsLoader.loadContacts(context);

            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
            GatewayUser.addPhoneNumber(context, gatewayNumber, 1);
            SMS.ChatList.add(GatewayUser);
        }
    }

    public static ArrayList<ChatList> getChatList(Context context) {
        fillIfEmpty(context);
        return chatList;
    }

    public static ChatList getSMS(Context context) {
        fillIfEmpty(context);
        return SMS;
    }

    public static ChatList listForIdentifier(Context context, String identifier) {
        fillIfEmpty(context);
        return chatMap.get(identifier);
    }

    public static String identifierForList(Context context, ChatList list) {
        fillIfEmpty(context);
        return list.getIdentifier();
    }

    public static int count() {
        return chatMap.size();
    }

    public static ChatList listAtIndex(Context context, int index) {
        fillIfEmpty(context);
        return chatList.get(index);
    }

    public static void cleanChats() {
        for (ChatList list : chatList)
            list.cleanChatList();
        SMS.cleanChatList();
    }
}
