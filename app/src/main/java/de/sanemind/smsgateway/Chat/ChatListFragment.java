package de.sanemind.smsgateway.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.sanemind.smsgateway.Message.MessageListActivity;
import de.sanemind.smsgateway.Messengers;
import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.UserChat;

public class ChatListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "1";

    public static final String EXTRA_MESSENGER = "de.sanemind.smsgateway.MESSENGER";
    public static final String EXTRA_CHAT = "de.sanemind.smsgateway.CHAT";
    public static final String EXTRA_CHAT_TYPE = "de.sanemind.smsgateway.CHAT_TYPE";

    private ChatListRecyclerView chatListRecycler;
    private ChatListAdapter chatListAdapter;
    private ChatList chatList;

    private static Map<ChatList, ChatListFragment> sectionInstances;

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
        Context context = getContext();

        if (sectionInstances == null)
            sectionInstances = new HashMap<>();

        int section = getArguments().getInt(ARG_SECTION_NUMBER);
        chatList = Messengers.listAtIndex(context, section);
        if (chatList == null)
            throw new IllegalArgumentException("ChatListFragment was opened with invalid section number: " + section);

        sectionInstances.put(chatList, this);

        chatListRecycler = (ChatListRecyclerView) rootView.findViewById(R.id.recyclerview_user_list);
        chatListAdapter = new ChatListAdapter(inflater.getContext(), chatList);
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
        String chatName = chat.getNameIdentifier();
        if (chatName == null)
            chatName = chat.getName();
        if (chatName == null)
            chatName = chat.getIdentifier();
        String identifier = Messengers.identifierForList(context, chat.getChatList());
        intent.putExtra(EXTRA_MESSENGER, identifier);
        intent.putExtra(EXTRA_CHAT, chatName);
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

    public static Map<ChatList, ChatListFragment> getInstance() {
        if (sectionInstances != null)
            return sectionInstances;
        else
            return null;
    }

    public ChatListRecyclerView getChatListRecycler() {
        return chatListRecycler;
    }

    public ChatList getChatList() {
        return chatList;
    }
}
