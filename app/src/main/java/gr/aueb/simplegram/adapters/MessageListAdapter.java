package gr.aueb.simplegram.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplegram.src.Message;
import com.simplegram.src.Value;

import java.util.List;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class MessageListAdapter extends RecyclerView.Adapter {

    private final static int ME = 1;
    private final static int OTHER = 2;


    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
        }

        void bind(Message msg) {

            messageText.setText(msg.getMsg());
            timeText.setText(msg.getDateSent().getHour()+":"+msg.getDateSent().getMinute());
            nameText.setText(msg.getSentFrom());

        }
    }
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
        }

        void bind(Message msg) {
            messageText.setText(msg.getMsg());
            timeText.setText(msg.getDateSent().getHour()+":"+msg.getDateSent().getMinute());
        }
    }

    private Context context;
    private List<Value> valueList;

    public MessageListAdapter(Context context, List<Value> valueList) {
        this.context = context;
        this.valueList = valueList;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {

        Value val = valueList.get(position);

        SharedPreferences settings = context.getApplicationContext()
                .getSharedPreferences(context.getString(R.string.userdata_config), 0);
        String myusername = settings.getString("username","");

        Log.d("getItemViewType", "getItemViewType: "+val.getSentFrom().equals(myusername));
        if (val.getSentFrom().equals(myusername)) {
            // If the current user is the sender of the message
            return ME;
        } else {
            // If some other user sent the message
            return OTHER;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == ME) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.me_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == OTHER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_message, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) valueList.get(position);

        switch (holder.getItemViewType()) {
            case ME:
                ((SentMessageHolder) holder).bind(message);
                break;
            case OTHER:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return this.valueList.size();
    }
}
