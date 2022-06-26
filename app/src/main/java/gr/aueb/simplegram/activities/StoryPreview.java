package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.simplegram.src.Story;

import java.io.File;
import java.time.LocalDateTime;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.common.Topic;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class StoryPreview extends AppCompatActivity {

    private UserNode userNode;
    private String topicname;
    private Topic topic;
    private Story story;

    private ImageView storyImageView;
    private VideoView storyVideoView;
    private TextView usernameTextView, timeSentTextView;

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_preview);
        getSupportActionBar().hide();
        storyImageView = (ImageView) findViewById(R.id.storyImageView);
        storyVideoView = (VideoView) findViewById(R.id.storyVideoView);
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        timeSentTextView = (TextView) findViewById(R.id.timeSentTextView);
        userNode = ((User) getApplicationContext()).getUserNode();
        topicname = getIntent().getExtras().getString("topicname");
        topic = userNode.getTopics().get(topicname);

        storyImageView.setVisibility(View.GONE);
        storyVideoView.setVisibility(View.GONE);

        story = topic.getLatestStoryFor();
        if(story.getType().equals("PHOTO")){
            setUpImageView();
        } else {
            setUpVideoView();
        }

        storyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!topic.getStoryQueue().isEmpty()){
                    story = topic.getLatestStoryFor();
                    if(story.getType().equals("PHOTO")){
                        setUpImageView();
                    } else {
                        setUpVideoView();
                    }
                }
                else{
                    Toast.makeText(context, "No more stories!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        storyVideoView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!topic.getStoryQueue().isEmpty()) {
                    story = topic.getLatestStoryFor();
                    if (story.getType().equals("PHOTO")) {
                        setUpImageView();
                    } else {
                        setUpVideoView();
                    }
                }else{
                    Toast.makeText(context, "No more stories!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


    }

    private void setUserData(){
        usernameTextView.setText(story.getSentFrom());
        LocalDateTime storyPosted = story.getDateSent();
        String datePosted = storyPosted.getMonth().toString() + " " + storyPosted.getDayOfMonth() + ", " + storyPosted.getHour() + ":" + storyPosted.getMinute();
        timeSentTextView.setText(datePosted);
    }

    private void setUpImageView(){
        storyImageView.setVisibility(View.VISIBLE);
        storyVideoView.setVisibility(View.GONE);
        setUserData();
        storyImageView.setImageURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+story.getFilename())));
    }

    private void setUpVideoView(){
        storyImageView.setVisibility(View.GONE);
        storyVideoView.setVisibility(View.VISIBLE);
        setUserData();
        storyVideoView.setVideoURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+story.getFilename())));
        storyVideoView.start();
    }



}