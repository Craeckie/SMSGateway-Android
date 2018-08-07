package de.sanemind.smsgateway.model;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class BaseChat implements Comparable<BaseChat> {
    protected String name;
    protected String nameIdentifier; //necessary for Telegram

    private SortedSet<BaseMessage> messages;

    private Map<String, Integer> knownServices;

    private ChatList chatList;

    private boolean updatedFromContacts = false;

    public BaseChat(ChatList chatList, String name, String nameIdentifier) {
        this.chatList = chatList;
        this.name = name;
        messages = new TreeSet<>();
        knownServices = new HashMap<>();
        this.nameIdentifier = nameIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getDisplayName();

    public abstract String getIdentifier();

    public void setIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    public ChatList getChatList() {
        return chatList;
    }

    public void setChatList(ChatList chatList) {
        this.chatList = chatList;
    }

    public boolean isUpdatedFromContacts() {
        return updatedFromContacts;
    }

    public void setUpdatedFromContacts() {
        this.updatedFromContacts = true;
    }

    public BaseMessage getLastMessage() {
        if (messages.size() > 0)
            return messages.first();
        else
            return null;
    }

    public BaseMessage getMessageFromID(long ID) {

        SortedSet<BaseMessage> tailSet = messages.tailSet(new UserMessage(ID, new Date(0), null, null, null, false, null));
        if (tailSet.size() > 0) {
            BaseMessage msg = tailSet.first();
            if (msg.getID() == ID)
                return msg;
        }
        return null;
    }

    public void addMessage(BaseMessage message) { //, int position) {
//        if (messages.contains(message)) {
//        }
//        SortedSet<BaseMessage> followingMessages = messages.tailSet(message);
//        if (followingMessages.size() > 0)
//            followingMessages.first()
        BaseMessage refMessage = getMessageFromID(message.getID());
        if (message.getStatus() != MessageStatus.EDITED || refMessage == null || refMessage.getStatus() != MessageStatus.EDITED) {
            if (refMessage != null)
                messages.remove(refMessage);
            messages.add(message);
        }
//        if (position == -1)
//            messages.add(0, message);
//        else
//            messages.add(position, message);

        int num = 1;
        if (knownServices.containsKey(message.getServiceID()))
            num = knownServices.get(message.getServiceID()) + 1;
        knownServices.put(message.getServiceID(), num);
    }

    public SortedSet<BaseMessage> getMessages() {
        return messages;
    }

//    public void setMessages(List<BaseMessage> messages) {
//        this.messages = messages;
//        knownServices = new HashMap<>();
//        for (BaseMessage msg : messages) {
//            int num = 1;
//            if (knownServices.containsKey(msg.serviceID))
//                num = knownServices.get(msg.serviceID) + 1;
//
//            knownServices.put(msg.serviceID, num);
//        }
//    }

    public Set<String> getKnownServices() {
        return knownServices.keySet();
    }

    public String getMostUsedService() {
        Set<String> keys = knownServices.keySet();
        String mostUsed = null; int maxNum = 0;
        for (String key : keys) {
            int num = knownServices.get(key);
            if (num > maxNum) {
                mostUsed = key;
                maxNum = num;
            }
        }
        return mostUsed;
    }

    @Override
    public int compareTo(@NonNull BaseChat o) {
        BaseMessage lastM1 = getLastMessage();
        BaseMessage lastM2 = o.getLastMessage();
        if (lastM1 == null && lastM2 == null)
            return 0;
        if (lastM1 == null) {
            return -1;
        }
        if (lastM2 == null) {
            return 1;
        }
        int comp = lastM2.getCreatedAt().compareTo(lastM1.getCreatedAt());
        return comp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseChat) {
            BaseChat chat = (BaseChat) obj;
//            if (obj instanceof UserChat && this instanceof UserChat)
//                return ((UserChat)obj).equals((UserChat)this);
//            else
            return chat.getName().equals(getName()) && chat.getIdentifier().equals(getIdentifier());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getNameIdentifier() {
        if (nameIdentifier != null)
            return nameIdentifier;
        else
            return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
