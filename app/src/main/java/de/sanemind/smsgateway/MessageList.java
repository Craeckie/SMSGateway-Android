package de.sanemind.smsgateway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;
import de.sanemind.smsgateway.model.UserMessage;

public class MessageList {
    //private static final Map<BaseChat, ArrayList<BaseMessage>> messageList = new java.util.HashMap<>();

    public static void addMessage(BaseMessage message) {
        addMessage(message, -1);
    }
    public static void addMessage(BaseMessage message, int position) {
//        ArrayList<BaseMessage> messages;
//        if (!messageList.containsKey(message.getChat())) {
//            messages = new ArrayList<>();
//            messageList.put(message.getChat(), messages);
//        } else
//            messages = messageList.get(message.getChat());

        //message.getChat().setMessages(messages);
        BaseChat chat = message.getChat();
        chat.addMessage(message, position);
        if (position == -1)
            position = 0;

        List<BaseMessage> messages = chat.getMessages();
        for (int i = position; i < messages.size(); i++) {
            messages.get(i).setIndex(i);
        }
    }

    public static BaseMessage addMessage(Context context, Date date, String body, String phoneNumber, boolean isSent) {
        BaseMessage msg = null;
        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
        if (PhoneNumberUtils.compare(phoneNumber, gatewayNumber)) {
            msg = GatewayUtils.tryParseGatewayMessage(context, body, date, isSent);
        }
        if (msg != null) {
            List<BaseMessage> chatMessages = msg.getChat().getMessages();
            for (int i = 0; i < 3 && i < chatMessages.size(); i++) {
                BaseMessage currentMessage = chatMessages.get(i);
                if (currentMessage.equals(msg)) {
                    switch (currentMessage.getStatus()) {
                        case BaseMessage.STATUS_SENT:
                            currentMessage.setStatus(BaseMessage.STATUS_RECEIVED);
                            //Toast.makeText(context, "Message forwarded to Telegram!", Toast.LENGTH_SHORT).show();
                            return null;
                        case BaseMessage.STATUS_RECEIVED:
                            currentMessage.setStatus(BaseMessage.STATUS_FORWARDED);
                            return null;
                        case BaseMessage.STATUS_FORWARDED:
                            msg = currentMessage;
                            break;
                    }
                    break;
                }
            }
        }
        else if (msg == null) {
            msg = new UserMessage(date, body, "SMS", ChatList.get_or_create_user(context, null, null, phoneNumber), isSent);
        }
        MessageList.addMessage(msg);
        if (msg instanceof GroupMessage) {
            GroupMessage groupMsg = (GroupMessage)msg;
            UserChat user = groupMsg.getUser();
            if (user.getMessages().size() == 0)
                ChatList.ChatList.remove(user);
        }
        Collections.sort(ChatList.ChatList);
//        MessageList.sortChatMessages(msg.getChat());
        return msg;
    }

    public static void addSentMessage(Context context, BaseMessage message) {
        addMessage(message, 0);
        Collections.sort(ChatList.ChatList);

        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);

        //Collections.sort(messages);
        ContentValues values = new ContentValues();
        values.put("address", gatewayNumber);
//        values.put("person", ChatList.);
        values.put("date", message.getCreatedAt().getTime());
        values.put("read", 1);
        values.put("type", 2); // Sent message
        values.put("seen", 1);
        values.put("status", 0);
        values.put("body", message.getMessage());
//        values.put("status", );
//        values.put("type", );
//        values.put("body", );
//        values.put("seen", );
        context.getContentResolver().insert(Uri.parse("content://sms"), values);
    }

    public static void sortChatMessages(BaseChat chat) {
//        List<BaseMessage> messages = MessageList.messageList.get(chat);
        Collections.sort(chat.getMessages());

        int index = 0;
        for (Iterator<BaseMessage> iterator = chat.getMessages().iterator(); iterator.hasNext();) {
            BaseMessage message = iterator.next();
            message.setIndex(index);
            index++;
        }
    }

    public static void refreshFromSMSInbox(Context context) {
//        messageList.clear();
//        List<BaseMessage> receivedMessages = getMessages(contentResolver, "content://sms/inbox", false, 50);
//        List<BaseMessage> sentMessages = getMessages(contentResolver, "content://sms/sent", true, -1);
        getMessages(context, "content://sms/inbox", false);
        getMessages(context, "content://sms/sent", true);

//        messageList.addAll(sentMessages);
//        messageList.addAll(receivedMessages);
        for (Iterator<BaseChat> iterator = ChatList.ChatList.iterator(); iterator.hasNext();) {
            BaseChat chat = iterator.next();
            if (chat.getMessages().size() == 0)
                iterator.remove();
            else
                sortChatMessages(chat);
        }
        Collections.sort(ChatList.ChatList);
    }

    private static void getMessages(Context context, String uri, boolean isSent) {
//        List<BaseMessage> messages = new LinkedList<>();
        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
        Cursor smsInboxCursor = context.getContentResolver().query(Uri.parse(uri), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDate = smsInboxCursor.getColumnIndex("date");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        Calendar cal = Calendar.getInstance();
        do {
            String address = smsInboxCursor.getString(indexAddress);
            String body = smsInboxCursor.getString(indexBody);
            String dateStr = smsInboxCursor.getString(indexDate);
            Long dateTimestamp = Long.parseLong(dateStr);
            cal.setTimeInMillis(dateTimestamp);
            Date date = cal.getTime();
            BaseMessage msg = null;
            if (PhoneNumberUtils.compare(address, gatewayNumber)) {
                msg = GatewayUtils.tryParseGatewayMessage(context, body, date, isSent);
            }
            if (msg == null) {
                msg = new UserMessage(date, body, "SMS", ChatList.get_or_create_user(context, null, null, address), isSent);
            }
            msg.getChat().addMessage(msg, 0);
//            messages.add(msg);
//            if (PhoneNumberUtils.compare(from, GatewayNumber)) {
//                mMessageAdapter.getmMessageList().add(new UserMessage(body, GatewayUser, new Date()));
//            } else { //else if (PhoneNumberUtils.compare(from, UserChat.currentUser.phoneNumber)) {
//                mMessageAdapter.getmMessageList().add(new UserMessage(body, ChatList.get_or_create(from), new Date()));
//            }
        } while (smsInboxCursor.moveToNext());
        smsInboxCursor.close();
        return;
    }
}
