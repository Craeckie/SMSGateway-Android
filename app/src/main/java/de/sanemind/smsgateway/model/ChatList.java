package de.sanemind.smsgateway.model;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.sanemind.smsgateway.ContactsLoader;

public class ChatList {
    public final List<BaseChat> ChatList = new ArrayList<>();

    private ContactsLoader contactsLoader;

    public ChatList(ContactsLoader contactsLoader) {
        this.contactsLoader = contactsLoader;
    }

    public Boolean isEmpty() {
        return ChatList.isEmpty();
    }

    public GroupChat get_or_create_group(Context context, String groupName, String groupIdentifier, boolean isChannel) {
        GroupChat g = find_group(context, groupIdentifier, isChannel);
        if (g != null)
            return g;
        else {
            g = new GroupChat(this, groupName, groupIdentifier, isChannel);
            ChatList.add(g);
            return g;
        }
    }
    public GroupChat find_group(Context context, String groupIdentifier, boolean isChannel) {

        for (BaseChat chat: ChatList) {
            if (chat instanceof GroupChat) {
                GroupChat group = (GroupChat) chat;
                if (group.getIdentifier().equals(groupIdentifier) && group.isChannel() == isChannel) {
                    return group;
                }
            }
        }
        return null;
    }
    public UserChat get_or_create_user(Context context, String name, String nameIdentifier, String phoneNumber) {

        for (BaseChat chat: ChatList) {
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
        UserChat user = contactsLoader.getUserInfo(context, name, phoneNumber);

        if (user == null) {
            user = new UserChat(this, name, nameIdentifier);
            if (phoneNumber != null)
                user.addPhoneNumber(context, phoneNumber, 1);
        } else {
            user.setChatList(this);
        }

        ChatList.add(user);
        return user;
    }

    public UserChat get_meUser(Context context) {
        String meUserName = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_name_telegram", null);
        return get_or_create_user(context, meUserName, meUserName, null);
    }

    public void cleanChatList() {
        for (Iterator<BaseChat> iterator = ChatList.iterator(); iterator.hasNext();) {
            BaseChat chat = iterator.next();
            if (chat.getMessages().size() == 0)
                iterator.remove();
        }
        Collections.sort(ChatList);
    }
}
