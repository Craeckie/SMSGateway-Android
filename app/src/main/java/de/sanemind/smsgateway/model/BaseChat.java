package de.sanemind.smsgateway.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseChat implements Comparable<BaseChat> {
    protected String name;
    protected String nameIdentifier; //necessary for Telegram

    private List<BaseMessage> messages;

    private Map<String, Integer> knownServices;

    private boolean updatedFromContacts = false;

    public BaseChat(String name, String nameIdentifier) {
        this.name = name;
        messages = new ArrayList<>();
        knownServices = new HashMap<>();
        this.nameIdentifier = nameIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getIdentifier();

    public boolean isUpdatedFromContacts() {
        return updatedFromContacts;
    }

    public void setUpdatedFromContacts() {
        this.updatedFromContacts = true;
    }

    public BaseMessage getLastMessage() {
        if (messages.size() > 0)
            return messages.get(0);
        else
            return null;
    }

    public void addMessage(BaseMessage message, int position) {
        if (position == -1)
            messages.add(0, message);
        else
            messages.add(position, message);

        int num = 1;
        if (knownServices.containsKey(message.getServiceID()))
            num = knownServices.get(message.getServiceID()) + 1;
        knownServices.put(message.getServiceID(), num);
    }

    public List<BaseMessage> getMessages() {
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
        if (lastM1 == null)
            return -1;
        BaseMessage lastM2 = o.getLastMessage();
        if (lastM2 == null)
            return 1;
        return lastM2.getCreatedAt().compareTo(lastM1.getCreatedAt());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseChat) {
            BaseChat chat = (BaseChat) obj;
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
}