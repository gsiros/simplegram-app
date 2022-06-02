package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.simplegram.src.Message;
import com.simplegram.src.Value;

import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.adapters.MessageListAdapter;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class TopicActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    private Button pushButton;
    private EditText inputText;

    private ArrayList<Value> values;
    private Context context = this;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        UserNode userNode = ((User) getApplication()).getUserNode();
        String topicname = getIntent().getExtras().getString("TOPIC_NAME");

        pushButton = (Button) findViewById(R.id.button_gchat_send);
        inputText = (EditText) findViewById(R.id.edit_gchat_message);

        pushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PushTask().execute(topicname);
            }
        });

        values = userNode.getTopics().get(topicname).getMessageQueue();
        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, values);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        if(!values.isEmpty())
            mMessageRecycler.smoothScrollToPosition(values.size()-1);
        mMessageRecycler.post(runnable);
    }

    private class PushTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            SharedPreferences settings = context.getApplicationContext()
                    .getSharedPreferences(context.getString(R.string.userdata_config), 0);
            String myusername = settings.getString("username","");

            UserNode userNode = ((User) getApplication()).getUserNode();
            Message msg2send = new Message(
                    myusername,
                    inputText.getText().toString()
            );
            userNode.pushValue(
                    strings[0],
                    msg2send
            );


            synchronized (values){
                values.add(msg2send);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inputText.getText().clear();
                    mMessageAdapter.notifyDataSetChanged();
                    mMessageRecycler.smoothScrollToPosition(values.size()-1);
                }
            });

            return null;
        }
    }

}