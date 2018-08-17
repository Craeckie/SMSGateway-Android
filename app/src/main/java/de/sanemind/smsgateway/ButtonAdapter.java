package de.sanemind.smsgateway;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.sanemind.smsgateway.Message.MessageListActivity;
import de.sanemind.smsgateway.model.Buttons;

public class ButtonAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_SINGLE = 1;
    private static final int VIEW_TYPE_LIST = 2;

    private Buttons buttons;
    private MessageListActivity messageListActivity;

    public ButtonAdapter(Buttons buttons, MessageListActivity messageListActivity) {
        this.buttons = buttons;
        this.messageListActivity = messageListActivity;
    }

    @Override
    public int getItemViewType(int position) {
        Buttons.Row row = buttons.get(position);
        if (row.size() > 1)
            return VIEW_TYPE_LIST;
        else if (row.size() == 1)
            return VIEW_TYPE_SINGLE;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case VIEW_TYPE_SINGLE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_button, parent, false);
                break;
            case VIEW_TYPE_LIST:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_button_list, parent, false);
                break;
        }
        if (view != null)
            return new ButtonHolder(view, parent, messageListActivity);
        else
            return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Buttons.Row row = buttons.get(position);
        ((ButtonHolder) holder).bind(row);
    }

    public void setButtons(Buttons buttons) {
        this.buttons = buttons;
    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }
}
