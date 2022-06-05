package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.adapters.TopicViewAdapter;
import gr.aueb.simplegram.common.Topic;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class SubbedTopicsActivity extends AppCompatActivity {

    FloatingActionButton subToTopicFAB;
    ListView subbedTopcisListView;
    ArrayAdapter subbedTopcisAdapter;
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
}