package de.sanemind.smsgateway;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;
import de.sanemind.smsgateway.model.UserMessage;

public class MessageListActivity extends PermissionRequestActivity {

    private TextView mTextMessage;
    private MessageListRecyclerView messageRecycler;

    private MessageListAdapter messageAdapter;
    private Button mSendButton;
    private TextView mChatBox;

    BaseChat currentChat = null;

    String standardService = null;

    private SmsManager smsManager = SmsManager.getDefault();

    Timer messageUpdateTimer;
    Runnable messageUpdateRunnable;

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };

    private static MessageListActivity inst;

    public static MessageListActivity instance() {
        return inst;
    }

    @Override
    protected void onStart() {
        inst = this;
        super.onStart();
    }

    public void messageReceived(BaseMessage message) {
        if (message.getChat().equals(currentChat)) {
            messageAdapter.notifyItemInserted(0);
            messageRecycler.scrollToPosition(0);
        }
    }

    @Override
    protected void onStop() {
        messageUpdateTimer.cancel();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);


        if (!MessageList.isRefreshedFromSMSInbox()) {
            requestPermissions();
        }

        Intent intent = getIntent();
        String chatName = intent.getStringExtra(ChatListFragment.EXTRA_CHAT);
        String chatType = intent.getStringExtra(ChatListFragment.EXTRA_CHAT_TYPE);
        if (chatType.equals("USER")) {
            currentChat = ChatList.get_or_create_user(getApplicationContext(), chatName, chatName, chatName);
            titleText.setText(currentChat.getName() + "\n" + currentChat.getIdentifier());
            ImageView profileImage = (ImageView) findViewById(R.id.image_message_profile);
            UserChat userChat = (UserChat) currentChat;
            if (userChat.getPictureUri() != null) {
                Uri uri = userChat.getPictureUri();
                profileImage.setImageURI(uri);
            } else {
                profileImage.setImageURI(null);
            }
        }
        else if (chatType.equals("GROUP")) {
            currentChat = ChatList.get_or_create_group(getApplicationContext(), chatName, chatName, false);
            titleText.setText( currentChat.getDisplayName());
        }
        else if (chatType.equals("CHANNEL")) {
            currentChat = ChatList.get_or_create_group(getApplicationContext(), chatName, chatName, true);
            titleText.setText(currentChat.getDisplayName());
        } else
            throw new IllegalArgumentException("Unknown chat type!");

        BaseChat notificationChat = SmsBroadcastReceiver.NOTIFICATION_CHAT;
        if (notificationChat != null && SmsBroadcastReceiver.NOTIFICATION_CHAT.equals(currentChat)) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
        }



        messageAdapter = new MessageListAdapter(this, currentChat);

        messageRecycler = (MessageListRecyclerView) findViewById(R.id.reyclerview_message_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        messageRecycler.setLayoutManager(layoutManager);
        messageRecycler.setAdapter(messageAdapter);

        mChatBox = (TextView) findViewById(R.id.edittext_chatbox);
        mSendButton = (Button) findViewById(R.id.button_chatbox_send);
        mSendButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                String text = mChatBox.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentChat != null) {
                    String serviceID = "TG";
                    if (standardService != null)
                        serviceID = standardService;
                    ArrayList<String> lines = new ArrayList<String>();
                    lines.add(serviceID);
                    lines.add("To: " + currentChat.getNameIdentifier());
                    if (currentChat instanceof UserChat)
                        lines.add("Type: User");
                    else if (currentChat instanceof GroupChat) {
                        if (((GroupChat)currentChat).isChannel())
                            lines.add("Type: Channel");
                        else
                            lines.add("Type: Group");
                    }
                    lines.add("");
                    lines.add(text);
                    String message = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        message = String.join("\n", lines);
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        StringJoiner joiner = new StringJoiner("\n");
                        for (String line : lines) {
                            joiner.add(line);
                        }
                        message = joiner.toString();
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (String line : lines) {
                            builder.append(line + "\n");
                        }
                        builder.deleteCharAt(builder.length() - 1); //Remove last newline
                        message = builder.toString();
                    }
                    String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
//                    String phoneNumber = ChatList.GatewayNumber;
                    if (serviceID.equals("SMS") && currentChat instanceof UserChat) {
                        message = text;
                        UserChat userchat = (UserChat) currentChat;
                        gatewayNumber = userchat.getMostImportantPhoneNumber().getNumber();
                    } else {
                        try {
                            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");

                            Cipher cipher = Cipher.getInstance("AES_256/CBC/PKCS5Padding");
//                            cipher.
                        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                            Toast.makeText(inst,"Couldn't encrypt!\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    Intent broadcastReceiverSentIntent = new Intent(context, SmsBroadcastReceiver.class);
                    broadcastReceiverSentIntent.putExtra("id", SmsBroadcastReceiver.SMS_SENT);
                    PendingIntent messageSent = PendingIntent.getBroadcast(context, SmsBroadcastReceiver.SMS_SENT, broadcastReceiverSentIntent, 0);
                    Intent broadcastReceiverDeliveredIntent = new Intent(context, SmsBroadcastReceiver.class);
                    broadcastReceiverDeliveredIntent.putExtra("id", SmsBroadcastReceiver.SMS_DELIVERED);
                    PendingIntent messageDelivered = PendingIntent.getBroadcast(context, SmsBroadcastReceiver.SMS_DELIVERED, broadcastReceiverDeliveredIntent, 0);

                    ArrayList<String> messageParts = smsManager.divideMessage(message);
                    ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>(Collections.nCopies(messageParts.size(), messageSent));
                    ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>(Collections.nCopies(messageParts.size(), messageDelivered));

//                    for (int i = 0; i < ; i++) {
//                        sentPendingIntents.add(messageSent);
//                    }
//                    smsManager.sendTextMessage(gatewayNumber, null, message, null, null);
                    smsManager.sendMultipartTextMessage(gatewayNumber, null, messageParts, sentPendingIntents, deliveredPendingIntents);
                    mChatBox.setText("");
//                    Toast.makeText(inst, "Message sent to gateway!", Toast.LENGTH_SHORT).show();

//                    String meUserName = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_name_telegram", null);
//                    UserChat meUser = ChatList.get_or_create_user(context, meUserName, meUserName, null);

                    BaseMessage chatMessage;
                    long seconds = System.currentTimeMillis() / 1000L;
                    if (currentChat instanceof UserChat)
                        chatMessage = new UserMessage(seconds, new Date(), text, serviceID, (UserChat)currentChat, true, BaseMessage.STATUS_SENT, false);
                    else if (currentChat instanceof GroupChat)
                        chatMessage = new GroupMessage(seconds, new Date(), text, serviceID, (GroupChat)currentChat, ChatList.get_meUser(context), true, BaseMessage.STATUS_SENT, false);
                    else
                        throw new IllegalArgumentException("Unknown chat type!");
                    MessageList.addSentMessage(context, chatMessage);
                    messageAdapter.notifyItemInserted(0);
                    messageRecycler.scrollToPosition(0);
                    //messageAdapter.notifyDataSetChanged();
                }
            }
        });
        standardService = currentChat.getMostUsedService();
        if (standardService != null)
            mSendButton.setText("SEND (" + standardService + ")");

        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        messageUpdateTimer = new Timer();
        messageUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        };
        messageUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(messageUpdateRunnable);

            }
        }, 1000, 10000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        TaskStackBuilder.create(this)
                                // Add all of this activity's parents to the back stack
                                .addNextIntentWithParentStack(upIntent)
                                // Navigate up to the closest parent
                                .startActivities();
                    } else {

                    }
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public MessageListRecyclerView getMessageRecycler() {
        return messageRecycler;
    }

    public MessageListAdapter getMessageAdapter() {
        return messageAdapter;
    }
}
