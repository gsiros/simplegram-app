package gr.aueb.simplegram.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simplegram.src.MultimediaFile;
import com.simplegram.src.Story;

import java.io.File;
import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.adapters.TopicViewAdapter;
import gr.aueb.simplegram.common.Topic;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class SubbedTopicsActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_PICTURE_PERMISSION_CODE = 101;

    FloatingActionButton subToTopicFAB;
    ListView subbedTopcisListView;
    TopicViewAdapter subbedTopcisAdapter;
    AlertDialog.Builder subscribeDialog;
    EditText subscribeEditText;

    UserNode userNode;

    ArrayList<Topic> topics;

    @Override
    protected void onResume() {
        super.onResume();
        userNode = ((User) getApplication()).getUserNode();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subbed_topics);

        userNode = ((User) getApplication()).getUserNode();
        topics = new ArrayList<>(userNode.getTopics().values());

        // Find elements from layout
        subbedTopcisListView = (ListView) findViewById(R.id.subbedtopics_listview);
        //subbedTopcisAdapter = new ArrayAdapter<String>(this,R.layout.improved_topic_item,topics);
        subbedTopcisAdapter = new TopicViewAdapter(this, topics);
        subbedTopcisListView.setAdapter(subbedTopcisAdapter);
        subToTopicFAB = (FloatingActionButton) findViewById(R.id.subToTopicFAB);
        subscribeDialog = new AlertDialog.Builder(this);

        // Setup subscribe dialog
        subscribeDialog.setMessage("Enter the name of the topic you want to subscribe to.");
        subscribeDialog.setTitle("Subscribe");
        subscribeEditText = new EditText(this);
        if (subscribeEditText.getParent() != null){
            ((ViewGroup) subscribeEditText.getParent()).removeView(subscribeEditText);
        }
        subscribeDialog.setView(subscribeEditText);
        subscribeDialog.setCancelable(true);
        subscribeDialog.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputTopic = subscribeEditText.getText().toString();
                new SubToTopicTask().execute(inputTopic);
            }
        });
        // Setup subscribe to topic floating action button.
        subToTopicFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Recreate the input view...
                subscribeEditText = new EditText(view.getContext());
                if (subscribeEditText.getParent() != null){
                    ((ViewGroup) subscribeEditText.getParent()).removeView(subscribeEditText);
                }
                subscribeDialog.setView(subscribeEditText);
                // Show dialog...
                subscribeDialog.show();
            }
        });
    }

    class SubToTopicTask extends AsyncTask<String, Void, String>{

        UserNode userNode = ((User) getApplication()).getUserNode();

        @Override
        protected String doInBackground(String... strings) {
            userNode.sub(strings[0]);
            return strings[0];
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            topics.add(userNode.getTopics().get(string));
            subbedTopcisAdapter.notifyDataSetChanged();
        }
    }

    class PushStoryTask extends AsyncTask<Object, Void, Void>{
        @Override
        protected Void doInBackground(Object... objs) {

            SharedPreferences settings = getApplicationContext()
                    .getSharedPreferences(getString(R.string.userdata_config), 0);
            String myusername = settings.getString("username","");

            UserNode userNode = ((User) getApplication()).getUserNode();

            Uri imageUri = (Uri) objs[0];
            String topicname = (String) objs[1];
            Log.d("topicname", "doInBackground: "+topicname);
            String[] pathComponents = imageUri.getPath().split("/");
            String filename = pathComponents[pathComponents.length-1];


            ArrayList<byte[]> chunks = userNode.chunkify(imageUri);
            Story story2send = new Story(
                    myusername,
                    filename,
                    chunks.size(),
                    chunks
            );


            userNode.pushValue(
                    topicname,
                    story2send
            );



            File dir = new File(getFilesDir(), "SimplegramVals/"+topicname+"/");
            if(!dir.exists()){
                dir.mkdir();
            }
            File destination = new File(dir, filename);

            Topic topic = null;
            for(Topic t : topics){
                if(t.getName().equals(topicname)){
                    topic = t;
                    break;
                }
            }
            if(topic!=null){
                synchronized (topic){
                    topic.getStoryQueue().add(story2send);
                }
            }

            return null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri imageUri = data.getData();
            String topicname = subbedTopcisAdapter.getTargetTopic();
            Log.d("topicname", "doInBackground: 5"+topicname);

            Toast.makeText(this, "Post "+imageUri.getPath() + " to '"+topicname+"'", Toast.LENGTH_LONG).show();
            new PushStoryTask().execute(imageUri, topicname);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SELECT_PICTURE_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("topicname", "doInBackground: 6"+subbedTopcisAdapter.getTargetTopic());
                    String topicname = subbedTopcisAdapter.getTargetTopic();
                    pickImageFromGallery(topicname);
                }else{
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    private void pickImageFromGallery(String topicname){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        intent.putExtra("topicname", topicname);
        startActivityForResult(intent, SELECT_PICTURE);
    }
}