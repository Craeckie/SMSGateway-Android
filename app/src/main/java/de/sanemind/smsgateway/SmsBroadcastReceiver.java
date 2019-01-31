package de.sanemind.smsgateway;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;

import de.sanemind.smsgateway.Chat.ChatListFragment;
import de.sanemind.smsgateway.Message.MessageList;
import de.sanemind.smsgateway.Message.MessageListActivity;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    @SuppressWarnings("SpellCheckingInspection")
    protected static final String SMS_BUNDLE = "pdus";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final int SMS_SENT = 42;
    public static final int SMS_DELIVERED = 43;

    public static final int NOTIFICATION_ID = 42;
    public static BaseChat NOTIFICATION_CHAT;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras == null)
            return;
        int id = intentExtras.getInt("id", -1);
        if (id == SMS_SENT) {
            int num = intent.getIntExtra("object", -1);
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    String msg = "SMS Sent";
                    if (num != -1)
                        msg += ": " + num;
                    Toast.makeText(context,
                            msg,
                            Toast.LENGTH_SHORT).show();

                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "SMS unknown error :(", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else if (id == SMS_DELIVERED) {
            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(context,
                        "SMS Delivered: " + intent.getIntExtra("object", 0),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        "SMS couldn't be delivered! " + intent.getIntExtra("object", 0),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (intent.getAction().equals(SMS_RECEIVED)) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            if (sms.length == 0) {
                return;
            }
            Calendar cal =  Calendar.getInstance();
            SmsMessage[] messages = new SmsMessage[sms.length];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sms.length; i++) {
                String format = intentExtras.getString("format");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) sms[i], format);
                } else
                    messages[i] = SmsMessage.createFromPdu((byte[]) sms[i]);
                sb.append(messages[i].getMessageBody());
            }
            String address = messages[0].getOriginatingAddress();
            cal.setTimeInMillis(messages[0].getTimestampMillis());
            final BaseMessage receivedMessage = MessageList.addMessage(context, cal.getTime(), sb.toString(), address, false, true);
            if (receivedMessage != null) {
                NOTIFICATION_CHAT = receivedMessage.getChat();
                MessageListActivity messageListActivity = MessageListActivity.instance();
                if (messageListActivity != null && receivedMessage != null) {
                    messageListActivity.messageReceived(receivedMessage);
                }
                Map<ChatList, ChatListFragment> chatListFragments = ChatListFragment.getInstance();
                if (chatListFragments != null) {
                    ChatListFragment currentFragment = null;
                    for (ChatListFragment fragment : chatListFragments.values()) {
                        fragment.getChatListRecycler().updateAdapter();
                        ChatList fragmentChatList = fragment.getChatList();
                        if (fragmentChatList.equals(receivedMessage.getChatList(context)))
                            currentFragment = fragment;
                    }
                    if (currentFragment != null && receivedMessage != null && !receivedMessage.isSent()) {
                        if (messageListActivity != null && messageListActivity.getCurrentChat().equals(receivedMessage.getChat())
                                && messageListActivity.getWindow().isActive())
                            return;
                        final ChatListFragment finalCurrentFragment = currentFragment;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                openMessageNotification(context, receivedMessage, finalCurrentFragment);
                            }
                        }).start();
                    }
                }
            }
        }
    }

    public void openMessageNotification(Context context, BaseMessage receivedMessage, ChatListFragment chatListFragment) {
        Intent notificationIntent = chatListFragment.getOpenChatIntent(context, receivedMessage.getChat());
//                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(notificationIntent);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        }

        String notificationText = receivedMessage.getMessage();
        BaseChat chat = receivedMessage.getChat();
        if (chat instanceof GroupChat) {
            UserChat userChat = ((GroupMessage) receivedMessage).getUser();
            if (userChat != null) {
                String senderName = userChat.getDisplayName();
                notificationText = senderName + ": " + receivedMessage.getMessage();
            }
        }
        String chatListID = receivedMessage.getChat().getChatList().getIdentifier();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NotificationChannel.DEFAULT_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(receivedMessage.getChat().getName())
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setChannelId(chatListID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setAutoCancel(true)
//                .setVibrate(new long[] {0, 500, 500, 500, 500})
//                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
