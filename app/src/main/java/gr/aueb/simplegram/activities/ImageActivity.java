package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

import gr.aueb.simplegram.R;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageView = (ImageView) findViewById(R.id.imageView);
        String topicname = getIntent().getExtras().getString("topicname");
        String filename = getIntent().getExtras().getString("filename");

        imageView.setImageURI(Uri.fromFile(new File(getFilesDir()+"/SimplegramVals/"+topicname+"/"+filename)));
    }
}