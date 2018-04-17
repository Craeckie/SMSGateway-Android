package de.sanemind.smsgateway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
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
        message.getChat().addMessage(message, position);
    }

    public static BaseMessage addMessage(Context context, Date date, String body, String phoneNumber, boolean isSent) {
        BaseMessage msg = null;
        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
        if (PhoneNumberUtils.compare(phoneNumber, gatewayNumber)) {
            msg = GatewayUtils.tryParseGatewayMessage(context, body, date, isSent);
        }
        if (msg != null) {
            List<BaseMessage> chatMessages = msg.getChat().getMessages();
            for (int i = 0; i < 3; i++) {
                BaseMessage currentMessage = chatMessages.get(i);
                if (currentMessage.equals(msg)) {
                    if (currentMessage.isReceived()) {
                        msg = currentMessage;
                        break;
                    }
                    else {
                        currentMessage.setReceived(true);
                        Toast.makeText(context, "Message forwarded to Telegram!", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        }
        else if (msg == null) {
            msg = new UserMessage(date, body, "SMS", ChatList.get_or_create_user(context, phoneNumber), isSent);
        }
        MessageList.addMessage(msg);
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
    }

    public static void refreshFromSMSInbox(Context context) {
//        messageList.clear();
//        List<BaseMessage> receivedMessages = getMessages(contentResolver, "content://sms/inbox", false, 50);
//        List<BaseMessage> sentMessages = getMessages(contentResolver, "content://sms/sent", true, -1);
        getMessages(context, "content://sms/inbox", false);
        getMessages(context, "content://sms/sent", true);

//        messageList.addAll(sentMessages);
//        messageList.addAll(receivedMessages);
        for(BaseChat chat : ChatList.ChatList) {
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
                msg = new UserMessage(date, body, "SMS", ChatList.get_or_create_user(context, address), isSent);
            }
            addMessage(msg);
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
