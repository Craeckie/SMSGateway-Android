package de.sanemind.smsgateway;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;

public class MessageListAdapter extends android.support.v7.widget.RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_USER_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_GROUP_MESSAGE_RECEIVED = 3;
    private static final int VIEW_TYPE_GROUP_MESSAGE_RECEIVED_SHORT = 4;

    private Context mContext;
    BaseChat chat;
    List<BaseMessage> curMessages;

    public MessageListAdapter(Context context, BaseChat chat) {
        mContext = context;
        this.chat = chat;
        this.curMessages = chat.getMessages();
//        if (MessageList.messageList.containsKey(user))
//            curMessages =  MessageList.messageList.get(user);
//        else
//            curMessages = new LinkedList<>();
    }

    @Override
    public int getItemCount() {
        return curMessages.size();
    }


    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        BaseMessage message = curMessages.get(position);

        if (message.isSent()) { // If the current user is the sender of the message

            return VIEW_TYPE_MESSAGE_SENT;
        } else { // If some other user sent the message
            if (chat instanceof UserChat) {
                return VIEW_TYPE_USER_MESSAGE_RECEIVED;
            }
            else if (chat instanceof GroupChat) {
                if (((GroupChat)chat).isChannel())
                    return VIEW_TYPE_GROUP_MESSAGE_RECEIVED;
                UserChat currentMessageUser = ((GroupMessage)message).getUser();
                List<BaseMessage> chatMessages = message.getChat().getMessages();
                int index = message.getIndex();
                if (index < chatMessages.size() - 1) {
                    BaseMessage previousMessage = chatMessages.get(index + 1);
                    if (previousMessage instanceof GroupMessage) {
                        GroupMessage previousGroupMessage = (GroupMessage) previousMessage;
                        BaseChat user = previousGroupMessage.getUser();
                        if (user != null && previousGroupMessage.getUser().equals(currentMessageUser))
                            return VIEW_TYPE_GROUP_MESSAGE_RECEIVED_SHORT;
                    }
                }
                return VIEW_TYPE_GROUP_MESSAGE_RECEIVED;
            }
            return VIEW_TYPE_GROUP_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_USER_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_GROUP_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_GROUP_MESSAGE_RECEIVED_SHORT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_message_received_short, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = curMessages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_USER_MESSAGE_RECEIVED:
            case VIEW_TYPE_GROUP_MESSAGE_RECEIVED:
            case VIEW_TYPE_GROUP_MESSAGE_RECEIVED_SHORT:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }
}
