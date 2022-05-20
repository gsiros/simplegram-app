package gr.aueb.simplegram.common;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import gr.aueb.simplegram.MainActivity;
import gr.aueb.simplegram.R;

public class LoginActivity extends AppCompatActivity {

    EditText usernameEditText;
    Button loginButton;

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

        SharedPreferences settings = getApplicationContext().getSharedPreferences(getString(R.string.userdata_config), 0);

        loginButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get username from editText widget
                String usernameInput = usernameEditText.getText().toString();
                // Save it as username
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", usernameInput);
                editor.apply();
                // Launch main activity.

            }
        }));

    }
}