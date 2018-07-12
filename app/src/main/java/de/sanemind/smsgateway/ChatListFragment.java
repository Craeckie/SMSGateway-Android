package de.sanemind.smsgateway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.UserChat;

public class ChatListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "1";

    public static final String EXTRA_CHAT = "de.sanemind.smsgateway.CHAT";
    public static final String EXTRA_CHAT_TYPE = "de.sanemind.smsgateway.CHAT_TYPE";

    private ChatListRecyclerView chatListRecycler;
    private ChatListAdapter chatListAdapter;

    private static ChatListFragment instance;

    private Timer chatListUpdateTimer;
    private Runnable chatListUpdateRunnable;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ChatListFragment newInstance(int sectionNumber) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStop() {
        chatListUpdateTimer.cancel();
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        instance = this;

        chatListRecycler = (ChatListRecyclerView) rootView.findViewById(R.id.recyclerview_user_list);
        chatListAdapter = new ChatListAdapter(inflater.getContext());
        chatListRecycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        chatListRecycler.setAdapter(chatListAdapter);

//        UserChat chat = new UserChat("John", "+49123123123");
//        chat.getMessages().add(new UserMessage(new Date(), "Hey, whatsuppp?", "TG", chat, false));
//        ChatList.ChatList.add(chat);
//        chat = new UserChat("Miles", "+49123123123");
//        chat.getMessages().add(new UserMessage(new Date(), "yeah man, whats on?", "TG", chat, true));
//        ChatList.ChatList.add(chat);


        chatListUpdateTimer = new Timer();
        chatListUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                chatListAdapter.notifyDataSetChanged();
            }
        };
        chatListUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null)
                    return;
                activity.runOnUiThread(chatListUpdateRunnable);
            }
        }, 1000, 5000);

        return rootView;
    }


//    Intent intent = new Intent(this, DisplayMessageActivity.class);
//    EditText editText = (EditText) findViewById(R.id.editText);
//    String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//    startActivity(intent);

    public Intent getOpenChatIntent(Context context, BaseChat chat) {
        Intent intent = new Intent(context, MessageListActivity.class);
        String identifier = chat.getNameIdentifier();
        if (identifier == null)
            identifier = chat.getName();
        if (identifier == null)
            identifier = chat.getIdentifier();
        intent.putExtra(EXTRA_CHAT, identifier);
        if (chat instanceof UserChat)
            intent.putExtra(EXTRA_CHAT_TYPE, "USER");
        else if (chat instanceof GroupChat) {
            GroupChat groupChat = (GroupChat) chat;
            if (groupChat.isChannel())
                intent.putExtra(EXTRA_CHAT_TYPE, "CHANNEL");
            else
                intent.putExtra(EXTRA_CHAT_TYPE, "GROUP");
        } else
            throw new IllegalArgumentException("Chat is of unknown type!");
        return intent;
    }

    public void openChat(BaseChat chat) {
        startActivity(getOpenChatIntent(getContext(), chat));
    }

    public static ChatListFragment getInstance() {
        return instance;
    }

    public ChatListRecyclerView getChatListRecycler() {
        return chatListRecycler;
    }

}
