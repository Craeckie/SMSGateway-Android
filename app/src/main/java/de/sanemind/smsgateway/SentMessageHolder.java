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

        // Format the stored timestamp into a readable String
        long time = message.getCreatedAt().getTime();

        timeText.setText(Utils.formatRelativeTime(itemView.getContext(), time));

        if (message.isReceived())
            receivedImage.setImageResource(R.drawable.ic_check_black_24dp);
        else
            receivedImage.setImageResource(R.drawable.ic_access_time_black_24dp);
    }

}
