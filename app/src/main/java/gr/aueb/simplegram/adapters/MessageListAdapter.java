package gr.aueb.simplegram.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplegram.src.Message;
import com.simplegram.src.MultimediaFile;
import com.simplegram.src.Value;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.activities.ImageActivity;
import gr.aueb.simplegram.activities.VideoActivity;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class MessageListAdapter extends RecyclerView.Adapter {

    private final static int ME = 1;
    private final static int OTHER = 2;
    private final static int ME_PHOTO = 3;
    private final static int OTHER_PHOTO = 4;
    private final static int ME_VIDEO = 5;
    private final static int OTHER_VIDEO = 6;

    private String topicname;

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_other);
        }

        void bind(Message msg) {
            messageText.setText(msg.getMsg());
            timeText.setText(msg.getDateSent().getHour()+":"+msg.getDateSent().getMinute());
            dateText.setText(msg.getDateSent().getMonth().toString()+", "+msg.getDateSent().getDayOfMonth());
            nameText.setText(msg.getSentFrom());
        }


    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_me);
        }

        void bind(Message msg) {
            messageText.setText(msg.getMsg());
            timeText.setText(msg.getDateSent().getHour()+":"+msg.getDateSent().getMinute());
            dateText.setText(msg.getDateSent().getMonth().toString()+", "+msg.getDateSent().getDayOfMonth());
        }
    }

    private class ReceivedPhotoHolder extends RecyclerView.ViewHolder {
        TextView timeText, nameText, dateText;
        ImageView imageView;

        ReceivedPhotoHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_gchat_multimedia_other_photo);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other_photo);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other_photo);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_other_photo);
        }

        void bind(MultimediaFile multimediaFile) {
            imageView.setImageURI(Uri.fromFile(new File(context.getFilesDir()+"/SimplegramVals/"+topicname+"/"+multimediaFile.getFilename())));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showImageIntent = new Intent(context, ImageActivity.class);
                    showImageIntent.putExtra("filename", multimediaFile.getFilename());
                    showImageIntent.putExtra("topicname", topicname);
                    context.startActivity(showImageIntent);
                }
            });
            timeText.setText(multimediaFile.getDateSent().getHour()+":"+multimediaFile.getDateSent().getMinute());
            dateText.setText(multimediaFile.getDateSent().getMonth().toString()+", "+multimediaFile.getDateSent().getDayOfMonth());
            nameText.setText(multimediaFile.getSentFrom());
        }
    }

    private class SentPhotoHolder extends RecyclerView.ViewHolder {
        TextView timeText, dateText;
        ImageView imageView;

        SentPhotoHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_gchat_multimedia_me_photo);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me_photo);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_me_photo);
        }

        void bind(MultimediaFile multimediaFile) {
            imageView.setImageURI(Uri.fromFile(new File(context.getFilesDir()+"/SimplegramVals/"+topicname+"/"+multimediaFile.getFilename())));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showImageIntent = new Intent(context, ImageActivity.class);
                    showImageIntent.putExtra("filename", multimediaFile.getFilename());
                    showImageIntent.putExtra("topicname", topicname);
                    context.startActivity(showImageIntent);
                }
            });
            timeText.setText(multimediaFile.getDateSent().getHour() + ":" + multimediaFile.getDateSent().getMinute());
            dateText.setText(multimediaFile.getDateSent().getMonth().toString() + ", " + multimediaFile.getDateSent().getDayOfMonth());

        }
    }

    private class ReceivedVideoHolder extends RecyclerView.ViewHolder {
        TextView timeText, nameText, dateText, filenameTextView;
        ImageView imageView;

        ReceivedVideoHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.preview_gchat_multimedia_other_video);
            filenameTextView = (TextView) itemView.findViewById(R.id.preview_name_gchat_multimedia_other_video);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other_video);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other_video);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_other_video);
        }

        void bind(MultimediaFile multimediaFile) {

            filenameTextView.setText(multimediaFile.getFilename());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent playVideoIntent = new Intent(context, VideoActivity.class);
                    playVideoIntent.putExtra("filename", multimediaFile.getFilename());
                    playVideoIntent.putExtra("topicname", topicname);
                    context.startActivity(playVideoIntent);
                }
            });
            timeText.setText(multimediaFile.getDateSent().getHour()+":"+multimediaFile.getDateSent().getMinute());
            dateText.setText(multimediaFile.getDateSent().getMonth().toString()+", "+multimediaFile.getDateSent().getDayOfMonth());
            nameText.setText(multimediaFile.getSentFrom());


        }
    }

    private class SentVideoHolder extends RecyclerView.ViewHolder {
        TextView timeText, dateText, filenameTextView;
        ImageView imageView;

        SentVideoHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.preview_gchat_multimedia_me_video);
            filenameTextView = (TextView) itemView.findViewById(R.id.preview_name_gchat_multimedia_me_video);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me_video);
            dateText = (TextView) itemView.findViewById(R.id.text_gchat_date_me_video);
        }

        void bind(MultimediaFile multimediaFile) {
            filenameTextView.setText(multimediaFile.getFilename());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent playVideoIntent = new Intent(context, VideoActivity.class);
                    playVideoIntent.putExtra("filename", multimediaFile.getFilename());
                    playVideoIntent.putExtra("topicname", topicname);
                    context.startActivity(playVideoIntent);
                }
            });
            timeText.setText(multimediaFile.getDateSent().getHour() + ":" + multimediaFile.getDateSent().getMinute());
            dateText.setText(multimediaFile.getDateSent().getMonth().toString() + ", " + multimediaFile.getDateSent().getDayOfMonth());
        }
    }


    private Context context;
    private List<Value> valueList;

    public MessageListAdapter(Context context, String topicname, List<Value> valueList) {
        this.context = context;
        this.valueList = valueList;
        this.topicname = topicname;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {

        Value val = valueList.get(position);

        SharedPreferences settings = context.getApplicationContext()
                .getSharedPreferences(context.getString(R.string.userdata_config), 0);
        String myusername = settings.getString("username","");

        if(val.getClass() == Message.class){
            if (val.getSentFrom().equals(myusername)) {
                // If the current user is the sender of the message
                return ME;
            } else {
                // If some other user sent the message
                return OTHER;
            }
        } else {
            MultimediaFile mf = (MultimediaFile) val;
            if(mf.getType().equals("PHOTO")){
                if (val.getSentFrom().equals(myusername)) {
                    // If the current user is the sender of the message
                    return ME_PHOTO;
                } else {
                    // If some other user sent the message
                    return OTHER_PHOTO;
                }
            } else {
                if (val.getSentFrom().equals(myusername)) {
                    // If the current user is the sender of the message
                    return ME_VIDEO;
                } else {
                    // If some other user sent the message
                    return OTHER_VIDEO;
                }
            }

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
        } else if (viewType == ME_PHOTO){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.me_photo, parent, false);
            return new SentPhotoHolder(view);
        } else if (viewType == OTHER_PHOTO){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_photo, parent, false);
            return new ReceivedPhotoHolder(view);
        } else if (viewType == ME_VIDEO){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.me_video, parent, false);
            return new SentVideoHolder(view);
        } else if (viewType == OTHER_VIDEO){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_video, parent, false);
            return new ReceivedVideoHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Value val = valueList.get(position);
        switch (holder.getItemViewType()) {
            case ME:
                ((SentMessageHolder) holder).bind((Message) val);
                break;
            case OTHER:
                ((ReceivedMessageHolder) holder).bind((Message) val);
                break;
            case ME_PHOTO:
                ((SentPhotoHolder) holder).bind((MultimediaFile) val);
                break;
            case OTHER_PHOTO:
                ((ReceivedPhotoHolder) holder).bind((MultimediaFile) val);
                break;
            case ME_VIDEO:
                ((SentVideoHolder) holder).bind((MultimediaFile) val);
                break;
            case OTHER_VIDEO:
                ((ReceivedVideoHolder) holder).bind((MultimediaFile) val);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return this.valueList.size();
    }
}
