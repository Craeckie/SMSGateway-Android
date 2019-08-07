package de.sanemind.smsgateway.Message;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.R;
import de.sanemind.smsgateway.Utils;
import de.sanemind.smsgateway.model.BaseMessage;
import de.sanemind.smsgateway.model.MessageStatus;

public class SentMessageHolder extends ViewHolder {
    TextView messageText, timeText;
    ImageView receivedImage;

    SentMessageHolder(View itemView) {
        super(itemView);

        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        receivedImage = (ImageView) itemView.findViewById(R.id.image_message_received);
        if (receivedImage != null)
            receivedImage.setImageURI(null);
    }

    void bind(BaseMessage message) {
        messageText.setText(message.getMessage());
//        messageText.setText(message.getID() + ": " + message.getMessage());

        // Format the stored timestamp into a readable String
        long time = message.getCreatedAt().getTime();
        CharSequence timeStr = Utils.formatRelativeTime(itemView.getContext(), time);
        if (message.getStatus() == MessageStatus.EDITED)
            timeStr = "edited  " + timeStr;
        timeText.setText(timeStr);

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
            case READ:
                receivedImage.setImageResource(R.drawable.ic_double_check_blue_24dp);
                break;
            case DELETED:
                receivedImage.setImageResource(android.R.drawable.ic_delete);
                break;
        }
    }

}
