package com.mdavison.standup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mdavison.standup.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


/**
 * This Activity allows the user to create a new account
 */
public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser user = new ParseUser();
                final String username = etUsername.getText().toString();
                user.setUsername(username);
                final String password = etPassword.getText().toString();
                user.setPassword(password);
                user.setEmail(etEmail.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            ParseUser.logInInBackground(username, password,
                                    new LogInCallback() {
                                        @Override
                                        public void done(ParseUser user,
                                                         ParseException e) {
                                            if (e != null) {
                                                Log.e(TAG, "Issue with login",
                                                        e);
                                                return;
                                            }
                                            Intent i = new Intent(
                                                    SignupActivity.this,
                                                    MainActivity.class);
                                            startActivity(i);
                                        }
                                    });
                        } else {
                            Toast.makeText(SignupActivity.this,
                                    "Error while " + "creating account!",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error while creating account", e);
                        }
                    }
                });
            }
        });


    }
}