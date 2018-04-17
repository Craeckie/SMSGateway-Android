package de.sanemind.smsgateway;

import android.net.Uri;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.model.BaseChat;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.UserChat;

public class ChatHolder extends ViewHolder {
    TextView usernameText, lastMessageText, timeText;
    ImageView profileImage;
    public BaseChat chat;

    ChatHolder(View itemView) {
        super(itemView);

        usernameText = (TextView) itemView.findViewById(R.id.text_user_name);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        lastMessageText = (TextView) itemView.findViewById(R.id.text_last_message);
        profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        profileImage.setImageURI(null);

    }

    void bind(BaseChat _chat) {

        this.chat = _chat;

        profileImage.setImageURI(null);

        if (chat instanceof UserChat) {
            UserChat userChat = (UserChat) chat;
            if (userChat.getPictureUri() != null) {
                Uri uri = userChat.getPictureUri();
//                Log.v("SMSGateway", chat.getName() + ": " + uri.toString());
                profileImage.setImageURI(uri);
            }
        }

        usernameText.setText(chat.getName());
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
            lastMessageText.setText(lastMessage.getMessage());

            // Format the stored timestamp into a readable String using method.
            CharSequence timeStr = DateUtils.getRelativeTimeSpanString(chat.getLastMessage().getCreatedAt().getTime());
            timeText.setText(timeStr);
        }


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatListFragment.getInstance().openChat(chat);
            }
        });
    }
}
