package de.sanemind.smsgateway.Chat;

import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.ChatList;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;

public class ChatHolder extends ViewHolder {
    TextView usernameText, lastMessageText, timeText;
    ImageView profileImage;
    public BaseChat chat;
    private ChatList chatList;

    ChatHolder(View itemView) {
        super(itemView);

        usernameText = (TextView) itemView.findViewById(R.id.text_user_name);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        lastMessageText = (TextView) itemView.findViewById(R.id.text_last_message);
        profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        profileImage.setImageURI(null);

    }

    void bind(final BaseChat chat, final ChatList chatList) {

        this.chat = chat;
        this.chatList = chatList;

        profileImage.setImageURI(null);

        if (chat instanceof UserChat) {
            UserChat userChat = (UserChat) chat;
            if (userChat.getPictureUri() != null) {
                Uri uri = userChat.getPictureUri();
//                Log.v("SMSGateway", chat.getName() + ": " + uri.toString());
                profileImage.setImageURI(uri);
            }
        }

        usernameText.setText(chat.getDisplayName());
//        String[] lines = lastMessage.message.split("\n");
//        String message = "";
//        if (lines.length > 0) {
//            message = lines[0];
//            for (int i = 1; i < lines.length; i++) {
//                message += " " + lines[i];
//            }
//        }
        BaseMessage lastMessage = chat.getLastMessage();
        if (lastMessage != null) {
            String text = lastMessage.getMessage();
            if (chat instanceof GroupChat) {
                UserChat userChat = ((GroupMessage) lastMessage).getUser();
                if (userChat != null) {
                    String senderName = userChat.getDisplayName();
                    text = "<font color='#4D83B3'>" + senderName + "</font>: " + text;
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lastMessageText.setText(Html.fromHtml("<html><body>" + text + "</body></html>", Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                lastMessageText.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            }

            // Format the stored timestamp into a readable String using method.
            CharSequence timeStr = DateUtils.getRelativeTimeSpanString(lastMessage.getCreatedAt().getTime());
            timeText.setText(timeStr);
        }


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<ChatList, ChatListFragment> fragments = ChatListFragment.getInstance();
                ChatListFragment fragment = fragments.get(chatList);
                fragment.openChat(chat);
            }
        });
    }
}
