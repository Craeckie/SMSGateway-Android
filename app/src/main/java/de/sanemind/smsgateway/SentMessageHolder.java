package de.sanemind.smsgateway;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.sanemind.smsgateway.model.BaseMessage;

public class SentMessageHolder extends ViewHolder {
    TextView messageText, timeText;
    ImageView receivedImage;

    SentMessageHolder(View itemView) {
        super(itemView);

        messageText = (TextView) itemView.findViewById(R.id.text_message_body);
        timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        receivedImage = (ImageView) itemView.findViewById(R.id.image_message_received);
    }

    void bind(BaseMessage message) {
        messageText.setText(message.getMessage());
//        messageText.setText(message.getID() + ": " + message.getMessage());

        // Format the stored timestamp into a readable String
        long time = message.getCreatedAt().getTime();
        CharSequence timeStr = Utils.formatRelativeTime(itemView.getContext(), time);
        if (message.isEdit())
            timeStr = "edited  " + timeStr;
        timeText.setText(timeStr);

        switch (message.getStatus()) {
            case BaseMessage.STATUS_SENT:
                receivedImage.setImageResource(R.drawable.ic_access_time_black_24dp);
                break;
            case BaseMessage.STATUS_RECEIVED:
                receivedImage.setImageResource(R.drawable.ic_check_black_24dp);
                break;
            case BaseMessage.STATUS_FORWARDED:
                receivedImage.setImageResource(R.drawable.ic_double_check_black_24dp);
                break;
        }
    }

}
