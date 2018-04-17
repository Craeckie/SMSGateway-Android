package de.sanemind.smsgateway;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.sanemind.smsgateway.model.BaseChat;

public class ChatListAdapter extends RecyclerView.Adapter {

    private Context mContext;

    public ChatListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return ChatList.ChatList.size();
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
        BaseChat user = ChatList.ChatList.get(position);

        ((ChatHolder) holder).bind(user);
    }
}
