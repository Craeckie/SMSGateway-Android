package de.sanemind.smsgateway.Message;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.Utils;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.MessageStatus;
import de.sanemind.smsgateway.model.UserChat;

class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText, nameText;
    ImageView profileImage, receivedImage;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        nameText = (TextView) itemView.findViewById(R.id.text_message_name);
        profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        receivedImage = itemView.findViewById(R.id.image_message_received);
        if (profileImage != null)
            profileImage.setImageURI(null);
        if (receivedImage != null)
            receivedImage.setImageURI(null);
    }

    void bind(BaseMessage message) {
        messageText.setText(message.getMessage());
//        messageText.setText(message.getID() + ": " + message.getMessage());

        if (profileImage != null) {

            UserChat userChat = null;
            if (message.getChat() instanceof UserChat) {
                userChat = (UserChat) message.getChat();
            } else if (message instanceof GroupMessage) {
                userChat = ((GroupMessage)message).getUser();
            }
            if (userChat != null && userChat.getPictureUri() != null) {
                Uri uri = userChat.getPictureUri();
//                Log.v("SMSGateway", chat.getName() + ": " + uri.toString());
                profileImage.setImageURI(uri);
            } else {
                profileImage.setImageURI(null);
            }
        }

        // Format the stored timestamp into a readable String using method.
        long time = message.getCreatedAt().getTime();
        CharSequence timeStr = Utils.formatRelativeTime(itemView.getContext(), time);
        if (message.getStatus() == MessageStatus.EDITED)
            timeStr = "edited  " + timeStr;
        else if (message.getStatus() == MessageStatus.DELETED)
            timeStr = "deleted  " + timeStr;
        timeText.setText(timeStr);
//        String name = message.getChat().getName();
        if (nameText != null && message instanceof GroupMessage) {
            GroupMessage groupMessage = (GroupMessage) message;
            if (!((GroupChat)message.getChat()).isChannel()) {
                String name = groupMessage.getUser().getName();
                nameText.setText(name);

                // Insert the profile image from the URL into the ImageView.
                //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
            }
        }
        if (receivedImage != null) {
            switch (message.getStatus()) {
                case SENT:
                    receivedImage.setImageResource(R.drawable.ic_access_time_black_24dp);
                    break;
                case RECEIVED:
                    receivedImage.setImageResource(R.drawable.ic_check_black_24dp);
                    break;
                case FORWARDED:
                    receivedImage.setImageResource(R.drawable.ic_double_check_black_24dp);
                    break;
                case DELETED:
                    receivedImage.setImageResource(android.R.drawable.ic_delete);
                    break;
            }
        }

    }
}
