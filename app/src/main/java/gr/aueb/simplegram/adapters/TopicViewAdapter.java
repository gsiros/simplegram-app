package gr.aueb.simplegram.adapters;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
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

import static androidx.core.app.ActivityCompat.requestPermissions;

public class TopicViewAdapter extends ArrayAdapter<Topic> {

    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_PICTURE_PERMISSION_CODE = 101;

    private String topicToPass;



    private static class TopicView{
        FloatingActionButton fab;
        TextView textView;
        ImageView imageView;
    }

    private ArrayList<Topic> dataSet;
    private UserNode userNode;
    Context mContext;

    public TopicViewAdapter(Context context, ArrayList<Topic> data) {
        super(context, R.layout.improved_topic_item, data);
        this.dataSet = data;
        this.mContext=context;
        this.userNode = ((User) context.getApplicationContext()).getUserNode();
    }

    public String getTargetTopic() {
        return topicToPass;
    }

    private void setTopicToPass(String topicToPass) {
        this.topicToPass = topicToPass;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Topic target_topic = getItem(position);
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
                    if(!userNode.getTopics().get(target_topic.getName()).getStoryQueue().isEmpty()){
                        Intent showStoryIntent = new Intent(mContext, StoryPreview.class);
                        showStoryIntent.putExtra("topicname", target_topic.getName());
                        mContext.startActivity(showStoryIntent);

                    } else {
                        Toast.makeText(mContext, "No stories available!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            viewHolder.fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(mContext, "Long press i guess...", Toast.LENGTH_SHORT).show();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_DENIED){
                            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            ((Activity) mContext).requestPermissions(permissions, SELECT_PICTURE_PERMISSION_CODE);
                            Log.d("topicname", "doInBackground: 1"+target_topic.getName());

                            setTopicToPass(target_topic.getName());
                        } else{
                            Log.d("topicname", "doInBackground: 2"+target_topic.getName());
                            setTopicToPass(target_topic.getName());
                            pickImageFromGallery(target_topic.getName());
                        }
                    } else {
                        Log.d("topicname", "doInBackground: 3"+target_topic.getName());
                        setTopicToPass(target_topic.getName());
                        pickImageFromGallery(target_topic.getName());
                    }
                    return true;
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
                    topic.cleanStories();
                    if(!topic.getStoryQueue().isEmpty()){
                        viewHolder.imageView.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.imageView.setVisibility(View.INVISIBLE);
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
        return result;
    }

    private void pickImageFromGallery(String topicname){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        intent.putExtra("topicname", topicname);
        Log.d("topicname", "doInBackground: 4"+topicname);

        ((Activity) mContext).startActivityForResult(intent, SELECT_PICTURE);
    }

}

