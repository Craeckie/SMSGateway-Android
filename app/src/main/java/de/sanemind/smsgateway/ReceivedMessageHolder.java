package de.sanemind.smsgateway;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.GroupMessage;

class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText, nameText;
    ImageView profileImage;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        nameText = (TextView) itemView.findViewById(R.id.text_message_name);
        profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
    }

    void bind(BaseMessage message) {
        messageText.setText(message.getMessage());

        // Format the stored timestamp into a readable String using method.
        long time = message.getCreatedAt().getTime();
        timeText.setText(Utils.formatRelativeTime(itemView.getContext(), time));
//        String name = message.getChat().getName();
        if (message instanceof GroupMessage) {
            String name = ((GroupMessage) message).getUsername();
            nameText.setText(name);

            // Insert the profile image from the URL into the ImageView.
            //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }

    }
}
