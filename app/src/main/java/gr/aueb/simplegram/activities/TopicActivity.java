package gr.aueb.simplegram.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.simplegram.src.Message;
import com.simplegram.src.MultimediaFile;
import com.simplegram.src.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.adapters.MessageListAdapter;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class TopicActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_PICTURE_PERMISSION_CODE = 101;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    private Button pushButton;
    private Button pickFileButton;
    private EditText inputText;

    private ArrayList<Value> values;
    private Context context = this;

    private String topicname;


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
        topicname = getIntent().getExtras().getString("TOPIC_NAME");
        getSupportActionBar().setTitle(topicname);

        pushButton = (Button) findViewById(R.id.button_gchat_send);
        inputText = (EditText) findViewById(R.id.edit_gchat_message);
        pickFileButton = (Button) findViewById(R.id.pick_file_gchat_send);

        pickFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, SELECT_PICTURE_PERMISSION_CODE);
                    } else{
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }
            }
        });

        pushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PushTask().execute();
            }
        });

        values = userNode.getTopics().get(topicname).getMessageQueue();
        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, topicname, values);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        if(!values.isEmpty())
            mMessageRecycler.smoothScrollToPosition(values.size()-1);
        mMessageRecycler.post(runnable);
    }

    private class PushTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            SharedPreferences settings = context.getApplicationContext()
                    .getSharedPreferences(context.getString(R.string.userdata_config), 0);
            String myusername = settings.getString("username","");

            UserNode userNode = ((User) getApplication()).getUserNode();
            Message msg2send = new Message(
                    myusername,
                    inputText.getText().toString()
            );
            userNode.pushValue(
                    topicname,
                    msg2send
            );


            synchronized (values){
                values.add(msg2send);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inputText.getText().clear();
                    mMessageAdapter.notifyDataSetChanged();
                    mMessageRecycler.smoothScrollToPosition(values.size()-1);
                }
            });
        }
    }

    private class PushImageTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {

            SharedPreferences settings = context.getApplicationContext()
                    .getSharedPreferences(context.getString(R.string.userdata_config), 0);
            String myusername = settings.getString("username","");

            UserNode userNode = ((User) getApplication()).getUserNode();

            Uri imageUri = uris[0];
            String[] pathComponents = imageUri.getPath().split("/");
            String filename = pathComponents[pathComponents.length-1];


            ArrayList<byte[]> chunks = userNode.chunkify(imageUri);
            MultimediaFile mf2send = new MultimediaFile(
                    myusername,
                    filename,
                    chunks.size(),
                    chunks
            );

            userNode.pushValue(
                    topicname,
                    mf2send
            );



            File dir = new File(context.getFilesDir(), "SimplegramVals/"+topicname+"/");
            if(!dir.exists()){
                dir.mkdir();
            }
            File destination = new File(dir, filename);

            synchronized (values){
                values.add(mf2send);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessageAdapter.notifyDataSetChanged();
                    mMessageRecycler.smoothScrollToPosition(values.size()-1);
                }
            });
        }
    }

    private void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            Uri imageUri = data.getData();
            Toast.makeText(this, ""+imageUri.getPath(), Toast.LENGTH_LONG).show();
            new PushImageTask().execute(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SELECT_PICTURE_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else{
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT);
                }
                break;
        }
    }
}