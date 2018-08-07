package de.sanemind.smsgateway.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.ChatList;

public class ChatListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ChatList chatList;

    public ChatListAdapter(Context context, ChatList chatList) {
        mContext = context;
        this.chatList = chatList;
    }

    @Override
    public int getItemCount() {
        return chatList.ChatList.size();
    }


    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        //UserMessage message = (UserMessage) mMessageList.get(position);
        return 0;
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_chat, parent, false);
        return new ChatHolder(view);
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseChat user = chatList.ChatList.get(position);

        ((ChatHolder) holder).bind(user, chatList);
    }

    public ChatList getChatList() {
        return chatList;
    }
}
