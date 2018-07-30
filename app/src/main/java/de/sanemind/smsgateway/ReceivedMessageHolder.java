package de.sanemind.smsgateway;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupChat;
import de.sanemind.smsgateway.model.GroupMessage;
import de.sanemind.smsgateway.model.UserChat;

class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText, nameText;
    ImageView profileImage;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        nameText = (TextView) itemView.findViewById(R.id.text_message_name);
        profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        if (profileImage != null)
            profileImage.setImageURI(null);
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
        if (message.isEdit())
            timeStr = "edited  " + timeStr;
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

    }
}
