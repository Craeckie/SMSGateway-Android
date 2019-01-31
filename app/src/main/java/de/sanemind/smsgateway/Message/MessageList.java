package de.sanemind.smsgateway.Message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import de.sanemind.smsgateway.GatewayUtils;
import de.sanemind.smsgateway.Messengers;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.MessageStatus;
import de.sanemind.smsgateway.model.UserChat;
import de.sanemind.smsgateway.model.UserMessage;

public class MessageList {



    private static boolean refreshedFromSMSInbox = false;
    //private static final Map<BaseChat, ArrayList<BaseMessage>> messageList = new java.util.HashMap<>();

//    public static void addMessage(BaseMessage message) {
//        addMessage(message, -1);
//    }
//    public static void addMessage(BaseMessage message, int position) {
    public static void addMessage(BaseMessage message) {
//        ArrayList<BaseMessage> messages;
//        if (!messageList.containsKey(message.getChat())) {
//            messages = new ArrayList<>();
//            messageList.put(message.getChat(), messages);
//        } else
//            messages = messageList.get(message.getChat());

        //message.getChat().setMessages(messages);
        BaseChat chat = message.getChat();
//        chat.addMessage(message, position);
        chat.getMessages().remove(message);
//        }
        chat.addMessage(message);
//        if (position == -1)
//            position = 0;

//        List<BaseMessage> messages = chat.getMessages();
//        for (int i = position; i < messages.size(); i++) {
//            messages.get(i).setIndex(i);
//        }
    }

    public static BaseMessage addMessage(Context context, Date date, String body, String phoneNumber, boolean isSent,  boolean sortChatList) {
        return addMessage(context, date, body, phoneNumber, isSent, sortChatList, true);
    }
    public static BaseMessage addMessage(Context context, Date date, String body, String phoneNumber, boolean isSent,  boolean sortChatList, boolean parseMessages) {
        BaseMessage msg = null;
        String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
        if (parseMessages && PhoneNumberUtils.compare(phoneNumber, gatewayNumber)) {
            msg = GatewayUtils.tryParseGatewayMessage(context, body, date, isSent, phoneNumber);
        } else if (!parseMessages) {
            body = GatewayUtils.decryptBody(context, body);
        }
        if (msg != null) {
            BaseMessage currentMessage = null;
            BaseChat chat = msg.getChat();
            if (isSent) {
                BaseMessage refMessage = chat.getMessageFromID(Long.MAX_VALUE);
                if (refMessage != null)
                    chat.getMessages().remove(refMessage);
            }
//            if (messages.containsKey(msg.getID())) {
//                currentMessage = msg.getChat().getMessages().get(msg.getID()); // Get the element with the same ID as the received message
//            }
//            if (currentMessage != null) {
//                    switch (currentMessage.getStatus()) {
//                        case BaseMessage.STATUS_SENT:
//                            currentMessage.setStatus(BaseMessage.STATUS_RECEIVED);
//                            //Toast.makeText(context, "Message forwarded to Telegram!", Toast.LENGTH_SHORT).show();
//                        case BaseMessage.STATUS_FORWARDED:
//                            msg = currentMessage;
//                            break;
//                        case BaseMessage.STATUS_RECEIVED:
//                            currentMessage.setStatus(BaseMessage.STATUS_FORWARDED);
//                            return null;
//                    }
////                    break;
//                }
////            }
        }
//        else if (msg == null) {
//            msg = new UserMessage(-1, date, body, "SMS", Messengers.getSMS(context).get_or_create_user(context, null, null, phoneNumber), isSent, MessageStatus.SENT);
//        }
        if (msg != null) {
            MessageList.addMessage(msg);
            ChatList chatList = msg.getChatList(context);
            //TODO: do not add user to the ChatList which posted to a group, so we don't need to remove it
            if (msg instanceof GroupMessage) {
                GroupMessage groupMsg = (GroupMessage) msg;
                UserChat user = groupMsg.getUser();
                if (user != null && user.getMessages().size() == 0)
                    chatList.ChatList.remove(user);
            }
            if (sortChatList)
                Collections.sort(chatList.ChatList);
//        MessageList.sortChatMessages(msg.getChat());
        }
        return msg;
    }

    public static void addSentMessage(Context context, BaseMessage message) {
//        addMessage(message, 0);
        addMessage(message);
        Collections.sort(message.getChatList(context).ChatList);

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
//        Collections.sort(chat.getMessages());

//        int index = 0;
//        for (Iterator<BaseMessage> iterator = chat.getMessages().iterator(); iterator.hasNext();) {
//            BaseMessage message = iterator.next();
//            message.setIndex(index);
//            index++;
//        }
    }

    public static void refreshFromSMSInbox(Context context) {
        refreshFromSMSInbox(context, true);
    }
    public static void refreshFromSMSInbox(Context context, boolean parseMessages) {
        setRefreshedFromSMSInbox();
//        messageList.clear();
//        List<BaseMessage> receivedMessages = getMessages(contentResolver, "content://sms/inbox", false, 50);
//        List<BaseMessage> sentMessages = getMessages(contentResolver, "content://sms/sent", true, -1);
        getMessages(context, "content://sms/inbox", false, parseMessages);
        getMessages(context, "content://sms/sent", true, parseMessages);

//        messageList.addAll(sentMessages);
//        messageList.addAll(receivedMessages);

//        Collections.sort(ChatList.ChatList);
        Messengers.cleanChats();
    }
    private static void getMessages(Context context, String uri, boolean isSent) {
        getMessages(context, uri, isSent, true);
    }

    private static void getMessages(Context context, String uri, boolean isSent, boolean parseMessages) {
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
//            boolean foundEditedMessage = false;
            if (parseMessages && PhoneNumberUtils.compare(address, gatewayNumber)) {
                msg = GatewayUtils.tryParseGatewayMessage(context, body, date, isSent, address);
            } else if (!parseMessages) {
                body = GatewayUtils.decryptBody(context, body);
                msg = new UserMessage(
                        -1,
                        date,
                        body,
                        "SMS",
                        Messengers.getSMS(context).get_or_create_user(context, address, address, address),
                        isSent,
                        MessageStatus.SENT);
            }
//            } else if (msg.isEdit()) {
//                for (BaseMessage cur_msg : msg.getChat().getMessages()) {
//                    if (cur_msg.getID() == msg.getID()) {
//                        cur_msg.setMessage(msg.getMessage());
//                        foundEditedMessage = true;
//                        break;
//                    }
//                }
//            }
//            if (!foundEditedMessage)
            if (msg != null) {
                BaseChat chat = msg.getChat();
                if (chat != null) // Is message disposable?
                    msg.getChat().addMessage(msg);
//                msg.getChat().addMessage(msg, 0);
//            messages.add(msg);
//            if (PhoneNumberUtils.compare(from, GatewayNumber)) {
//                mMessageAdapter.getmMessageList().add(new UserMessage(body, GatewayUser, new Date()));
//            } else { //else if (PhoneNumberUtils.compare(from, UserChat.currentUser.phoneNumber)) {
//                mMessageAdapter.getmMessageList().add(new UserMessage(body, ChatList.get_or_create(from), new Date()));
//            }
            }
        } while (smsInboxCursor.moveToNext());
        smsInboxCursor.close();
        return;
    }

    public static boolean isRefreshedFromSMSInbox() {
        return refreshedFromSMSInbox;
    }

    public static void setRefreshedFromSMSInbox() {
        MessageList.refreshedFromSMSInbox = true;
    }

}
