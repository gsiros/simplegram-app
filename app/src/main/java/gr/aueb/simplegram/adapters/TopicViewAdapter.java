package gr.aueb.simplegram.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.activities.StoryPreview;
import gr.aueb.simplegram.activities.TopicActivity;
import gr.aueb.simplegram.common.Topic;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class TopicViewAdapter extends ArrayAdapter<Topic> {

    private static class TopicView{
        FloatingActionButton fab;
        TextView textView;
        ImageView imageView;
    }

    private ArrayList<Topic> dataSet;
    private UserNode userNode;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }

    public TopicViewAdapter(Context context, ArrayList<Topic> data) {
        super(context, R.layout.improved_topic_item, data);
        this.dataSet = data;
        this.mContext=context;
        this.userNode = ((User) context.getApplicationContext()).getUserNode();
    }


    private Topic target_topic = null;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        target_topic = (Topic) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        TopicView viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new TopicView();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.improved_topic_item, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.label);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.story_ring);

            viewHolder.fab = (FloatingActionButton) convertView.findViewById(R.id.topic_fab);
            viewHolder.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Activity) mContext).runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if(!userNode.getTopics().get(target_topic.getName()).getStoryQueue().isEmpty()){
                                        Intent showStoryIntent = new Intent(mContext, StoryPreview.class);
                                        showStoryIntent.putExtra("topicname", target_topic.getName());
                                        mContext.startActivity(showStoryIntent);

                                    } else {
                                        Toast.makeText(mContext, "No stories available!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                    );
                }
            });
            result=convertView;

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Topic name is: "+target_topic.getName(), Toast.LENGTH_SHORT).show();
                    Intent newActivityIntent = new Intent(mContext, TopicActivity.class);
                    newActivityIntent.putExtra("TOPIC_NAME", target_topic.getName());
                    mContext.startActivity(newActivityIntent);
                }
            });

            Runnable storyCheckerRunnable = new Runnable() {
                @Override
                public void run() {
                    // Check story...
                    Topic topic = userNode.getTopics().get(target_topic.getName());
                    if(!topic.getStoryQueue().isEmpty()){
                        ((Activity) mContext).runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.imageView.setVisibility(View.VISIBLE);
                                    }
                                }
                        );
                    } else {
                        ((Activity) mContext).runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.imageView.setVisibility(View.INVISIBLE);
                                    }
                                }
                        );
                    }
                    viewHolder.imageView.postDelayed(this, 1000);
                }
            };
            viewHolder.imageView.post(storyCheckerRunnable);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TopicView) convertView.getTag();
            result=convertView;
        }

        viewHolder.textView.setText(target_topic.getName());
        // Return the completed view to render on screen
        return convertView;
    }


}

