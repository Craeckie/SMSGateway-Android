package de.sanemind.smsgateway.Message;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

public class MessageListRecyclerView extends RecyclerView {
    public MessageListRecyclerView(Context context) {
        super(context);
    }

    public MessageListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateAdapter() {
        getAdapter().notifyDataSetChanged();
        scrollToPosition(0);
    }
}
