package de.sanemind.smsgateway.Chat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.Collections;

public class ChatListRecyclerView extends RecyclerView {
    public ChatListRecyclerView(Context context) {
        super(context);
    }

    public ChatListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    ChatListAdapter chatListAdapter;
    //@Override
    public void setAdapter(ChatListAdapter adapter) {
        super.setAdapter(adapter);
        chatListAdapter = adapter;
    }

    public void updateAdapter() {
        Collections.sort(chatListAdapter.getChatList().ChatList);
        getAdapter().notifyDataSetChanged();
    }
}
