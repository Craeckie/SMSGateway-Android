package de.sanemind.smsgateway;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;
import de.sanemind.smsgateway.model.UserMessage;

public class MessageListActivity extends AppCompatActivity {

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

        Intent intent = getIntent();
        String chatName = intent.getStringExtra(ChatListFragment.EXTRA_CHAT);
        String chatType = intent.getStringExtra(ChatListFragment.EXTRA_CHAT_TYPE);
        if (chatType.equals("USER"))
            currentChat = ChatList.get_or_create_user(getApplicationContext(), chatName);
        else if (chatType.equals("GROUP"))
            currentChat = ChatList.get_or_create_group(getApplicationContext(), chatName, chatName);
        else
            throw new IllegalArgumentException("Unknown chat type!");

        setTitle(currentChat.getName() + "(" + currentChat.getIdentifier() + ")");

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
                if (currentChat != null) {
                    String serviceID = "TG";
                    if (standardService != null)
                        serviceID = standardService;
                    String message = serviceID + "\nTo: " + currentChat.getNameIdentifier() + "\n" + text;
                    String gatewayNumber = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edit_text_preference_phone_gateway", null);
//                    String phoneNumber = ChatList.GatewayNumber;
                    if (serviceID.equals("SMS") && currentChat instanceof UserChat) {
                        message = text;
                        UserChat userchat = (UserChat) currentChat;
                        gatewayNumber = userchat.getMostImportantPhoneNumber().getNumber();
                    }

                    smsManager.sendTextMessage(gatewayNumber, null, message, null, null);
                    mChatBox.setText("");
                    Toast.makeText(inst, "Message sent to gateway!", Toast.LENGTH_SHORT).show();

                    BaseMessage chatMessage;
                    if (currentChat instanceof UserChat)
                        chatMessage = new UserMessage(new Date(), text, serviceID, (UserChat)currentChat, true, false);
                    else if (currentChat instanceof GroupChat)
                        chatMessage = new GroupMessage(new Date(), text, serviceID, (GroupChat)currentChat, "Me", true, false);
                    else
                        throw new IllegalArgumentException("Unknown chat type!");
                    MessageList.addSentMessage(getApplicationContext(), chatMessage);
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
