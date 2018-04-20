package de.sanemind.smsgateway;

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
import android.telephony.SmsMessage;

import java.util.Calendar;

import de.sanemind.smsgateway.model.BaseMessage;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    protected static final String SMS_BUNDLE = "pdus";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null && intent.getAction().equals(SMS_RECEIVED)) {
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
            BaseMessage receivedMessage = MessageList.addMessage(context, cal.getTime(), sb.toString(), address, false);
            MessageListActivity messageList = MessageListActivity.instance();
            if (messageList != null && receivedMessage != null) {
                messageList.messageReceived(receivedMessage);
            }
            ChatListFragment chatListFragment = ChatListFragment.getInstance();
            if (chatListFragment != null) {
                chatListFragment.getChatListRecycler().updateAdapter();
                if (receivedMessage != null && !receivedMessage.isSent()) {
                    Intent notificationIntent = chatListFragment.getOpenChatIntent(receivedMessage.getChat());
                    //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addNextIntentWithParentStack(notificationIntent);
                        pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
                    } else {
                        pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }


                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NotificationChannel.DEFAULT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(receivedMessage.getChat().getName())
                            .setContentText(receivedMessage.getMessage())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(receivedMessage.getMessage()))
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                }
            }
        }
    }
}
