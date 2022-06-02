package gr.aueb.simplegram.activities;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.services.PullService;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText;
    Button loginButton;

    LoginActivity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = (EditText) findViewById(R.id.username_textbox);
        loginButton = (Button) findViewById(R.id.login_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getApplicationContext()
                .getSharedPreferences(getString(R.string.userdata_config), 0);

        loginButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get username from editText widget
                String usernameInput = usernameEditText.getText().toString();
                // Save it as username
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", usernameInput);
                editor.apply();

                //prepare the usernode
                ((User) getApplication()).setUserName(usernameInput);

                Intent serviceIntent = new Intent(thisActivity, PullService.class);
                startService(serviceIntent);

                // Launch main activity.
                Toast.makeText(getApplicationContext(), "User with username: '"+usernameInput+"' logged in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(thisActivity, SubbedTopicsActivity.class);
                startActivity(intent);
            }
        }));

    }
}