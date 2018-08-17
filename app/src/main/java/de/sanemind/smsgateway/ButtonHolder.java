package de.sanemind.smsgateway;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import de.sanemind.smsgateway.Message.MessageListActivity;
import de.sanemind.smsgateway.model.Buttons;

public class ButtonHolder extends RecyclerView.ViewHolder {
    private Buttons.Row row;
    private Button button;
    private LinearLayout buttonList;
    private ViewGroup viewGroup;
    private MessageListActivity messageListActivity;


    public ButtonHolder(View itemView, ViewGroup viewGroup, MessageListActivity messageListActivity) {
        super(itemView);
        this.viewGroup = viewGroup;
        this.messageListActivity = messageListActivity;
    }

    public void bind(Buttons.Row row) {
        this.row = row;
        if (row.size() > 1) {
            buttonList = itemView.findViewById(R.id.linear_layout);
            buttonList.removeAllViews();
            for (final String name : row) {
//                View view = LayoutInflater.from(itemView.getContext())
//                        .inflate(R.layout.item_button, viewGroup, false);
//                Button button = view.findViewById(R.id.button);
                Button button = new Button(itemView.getContext());
                button.setTextSize(12);
                button.setText(name);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                button.setLayoutParams(params);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messageListActivity.sendMessage(name);
                    }
                });
                buttonList.addView(button);
            }
        } else if (row.size() == 1) {
            button = itemView.findViewById(R.id.button);
            final String name = row.get(0);
            button.setText(name);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageListActivity.sendMessage(name);
                }
            });
        }
    }
}
