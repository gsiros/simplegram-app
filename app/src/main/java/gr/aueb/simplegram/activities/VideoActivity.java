package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import gr.aueb.simplegram.R;

public class VideoActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoView = (VideoView) findViewById(R.id.videoView);
        String topicname = getIntent().getExtras().getString("topicname");
        String filename = getIntent().getExtras().getString("filename");
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        // Set video link (mp4 format )
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+filename)));
        videoView.start();

    }
}