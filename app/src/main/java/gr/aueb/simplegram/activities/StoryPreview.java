package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.simplegram.src.Story;

import java.io.File;

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

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_preview);
        storyImageView = (ImageView) findViewById(R.id.storyImageView);
        userNode = ((User) getApplicationContext()).getUserNode();
        topicname = getIntent().getExtras().getString("topicname");
        topic = userNode.getTopics().get(topicname);

        story = topic.getLatestStoryFor();
        storyImageView.setImageURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+story.getFilename())));
        storyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent;
                if(!topic.getStoryQueue().isEmpty()){
                    story = topic.getLatestStoryFor();
                    storyImageView.setImageURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+story.getFilename())));
                }
                else{
                    Toast.makeText(context, "No more stories!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}