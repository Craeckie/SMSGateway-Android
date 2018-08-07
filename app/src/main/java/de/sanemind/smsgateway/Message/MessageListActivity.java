package de.sanemind.smsgateway.Message;

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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

import de.sanemind.smsgateway.ButtonAdapter;
import de.sanemind.smsgateway.Chat.ChatListFragment;
import de.sanemind.smsgateway.Messengers;
import de.sanemind.smsgateway.PermissionRequestActivity;
import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.SmsBroadcastReceiver;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.Buttons;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.MessageStatus;
import de.sanemind.smsgateway.model.PhoneNumber;
import de.sanemind.smsgateway.model.UserChat;
import de.sanemind.smsgateway.model.UserMessage;

public class MessageListActivity extends PermissionRequestActivity {

    private TextView mTextMessage;
    private MessageListRecyclerView messageRecycler;

    private MessageListAdapter messageAdapter;
    private Button mSendButton;
    private TextView mChatBox;
//    private LinearLayout layoutButtons;
//    private ScrollView scrollButtons;
    private RecyclerView buttonRecycler;
    private ButtonAdapter buttonAdapter;
    private Button buttonButtons;

    private ChatList chatList;
    private BaseChat currentChat = null;

    private String standardService = null;

    private SmsManager smsManager = SmsManager.getDefault();

    private Timer messageUpdateTimer;
    private Runnable messageUpdateRunnable;

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
            SortedSet<BaseMessage> messages = message.getChat().getMessages();
            messageAdapter.setMessages(messages);
            if (message.isSent() && message.getStatus() != MessageStatus.EDITED)
                messageAdapter.notifyItemChanged(0);
            else {
                messageAdapter.notifyItemInserted(0);
                messageRecycler.scrollToPosition(0);
            }
            BaseMessage msg = messages.first();
            Buttons btns = msg.getButtons();
            if (btns != null) {
                buttonButtons.setVisibility(View.VISIBLE);
                setButtons(btns);
            } else {
                buttonButtons.setVisibility(View.GONE);
            }
        }
    }

    void setButtons(Buttons btns) {

        Context context = getApplicationContext();

        if (btns != null) {
            buttonAdapter = new ButtonAdapter(btns, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            buttonRecycler.setLayoutManager(layoutManager);
            buttonRecycler.setAdapter(buttonAdapter);
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
        final Button toggleButtons = findViewById(R.id.button_buttons);
        buttonRecycler = findViewById(R.id.recyclerview_button_list);
//        layoutButtons = findViewById(R.id.layout_buttons);
//        scrollButtons = findViewById(R.id.scroll_buttons);


        buttonButtons = findViewById(R.id.button_buttons);

        if (!MessageList.isRefreshedFromSMSInbox()) {
            requestPermissions();
        }

        Intent intent = getIntent();
        String messengerIdentifier = intent.getStringExtra(ChatListFragment.EXTRA_MESSENGER);
        String chatName = intent.getStringExtra(ChatListFragment.EXTRA_CHAT);
        String chatType = intent.getStringExtra(ChatListFragment.EXTRA_CHAT_TYPE);
        chatList = Messengers.listForIdentifier(getApplicationContext(), messengerIdentifier);
        if (chatType.equals("USER")) {
            currentChat = chatList.get_or_create_user(getApplicationContext(), chatName, chatName, chatName);
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
            currentChat = chatList.get_or_create_group(getApplicationContext(), chatName, chatName, false);
            titleText.setText( currentChat.getDisplayName());
        }
        else if (chatType.equals("CHANNEL")) {
            currentChat = chatList.get_or_create_group(getApplicationContext(), chatName, chatName, true);
            titleText.setText(currentChat.getDisplayName());
        } else
            throw new IllegalArgumentException("Unknown chat type!");

        BaseChat notificationChat = SmsBroadcastReceiver.NOTIFICATION_CHAT;
        if (notificationChat != null && SmsBroadcastReceiver.NOTIFICATION_CHAT.equals(currentChat)) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
        }

        BaseMessage msg = currentChat.getMessages().first();
        Buttons btns = msg.getButtons();
//        btns = new Buttons();
//        ArrayList<String> row = new ArrayList<>();
//        row.add("sdf"); row.add("sdfSDF");
//        btns.addRow(row);
//        row = new ArrayList<>();
//        row.add("DFGFDG");
//        btns.addRow(row);
        if (btns != null) {
            buttonButtons.setVisibility(View.VISIBLE);
            setButtons(btns);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            buttonButtons.setVisibility(View.GONE);
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
                String text = mChatBox.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(text);
                    mChatBox.setText("");
                }
            }
        });
        toggleButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = buttonRecycler.getVisibility();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (visibility != View.VISIBLE) {
                    buttonRecycler.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(mChatBox.getWindowToken(), 0);
                    toggleButtons.setText("KEYB");
                } else {
                    buttonRecycler.setVisibility(View.GONE);
                    imm.toggleSoftInputFromWindow(
                            mChatBox.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, 0);
                    toggleButtons.setText("BTNS");
                }
            }
        });
        mChatBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRecycler.setVisibility(View.GONE);
            }
        });
        mChatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
            }
        });


        standardService = currentChat.getMostUsedService();
        if (standardService != null)
            mSendButton.setText("SEND (" + standardService + ")");

        mTextMessage = findViewById(R.id.message);

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
    private String generateSendMessage(String serviceID, String text) {
        if (standardService != null)
            serviceID = standardService;

        String message;
        if (!serviceID.equals("SMS")) {
            ArrayList<String> lines = new ArrayList<String>();
            lines.add(serviceID);
            lines.add("To: " + currentChat.getNameIdentifier());

            if (currentChat instanceof UserChat) {
                lines.add("Type: User");
                UserChat userChat = (UserChat)currentChat;
                PhoneNumber phoneNumber = userChat.getMostImportantPhoneNumber();
                if (phoneNumber != null)
                    lines.add("Phone: " + phoneNumber.getNumber());
            }
            else if (currentChat instanceof GroupChat) {
                if (((GroupChat) currentChat).isChannel())
                    lines.add("Type: Channel");
                else
                    lines.add("Type: Group");
            }
            lines.add("");
            lines.add(text);
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

//            try {
//                KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
//
//                Cipher cipher = Cipher.getInstance("AES_256/CBC/PKCS5Padding");
//            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//                Toast.makeText(inst,"Couldn't encrypt!\n" + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
        } else if (serviceID.equals("SMS") && currentChat instanceof UserChat) {
            message = text;
        } else {
            throw new IllegalArgumentException("service ID is SMS, but it's a group chat!");
        }
        return message;
    }


    public void sendMessage(String text) {
        if (currentChat != null) {
            Context context = getApplicationContext();
            String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_phone_gateway", null);
            String serviceID = "TG";

            String message = generateSendMessage(serviceID, text);

            if (serviceID.equals("SMS")) {
                UserChat userchat = (UserChat) currentChat;
                gatewayNumber = userchat.getMostImportantPhoneNumber().getNumber();
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
//                    Toast.makeText(inst, "Message sent to gateway!", Toast.LENGTH_SHORT).show();

//                    String meUserName = PreferenceManager.getDefaultSharedPreferences(context).getString("edit_text_preference_name_telegram", null);
//                    UserChat meUser = ChatList.get_or_create_user(context, meUserName, meUserName, null);

            BaseMessage chatMessage;
//                    long seconds = System.currentTimeMillis() / 1000L;
            if (currentChat instanceof UserChat)
                chatMessage = new UserMessage(Long.MAX_VALUE, new Date(), text, serviceID, (UserChat)currentChat, true, MessageStatus.SENT);
            else if (currentChat instanceof GroupChat)
                chatMessage = new GroupMessage(Long.MAX_VALUE, new Date(), text, serviceID, (GroupChat)currentChat, chatList.get_meUser(context), true, MessageStatus.SENT);
            else
                throw new IllegalArgumentException("Unknown chat type!");
//                    MessageList.addSentMessage(context, chatMessage);
//                    messageAdapter.setMessages(currentChat.getMessages());
            messageAdapter.curMessages.add(0, chatMessage);
            messageAdapter.notifyItemInserted(0);
            messageRecycler.scrollToPosition(0);
            //messageAdapter.notifyDataSetChanged();
        }
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

    public BaseChat getCurrentChat() {
        return currentChat;
    }
}
