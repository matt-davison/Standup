package com.mdavison.standup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mdavison.standup.R;
import com.mdavison.standup.support.Extras;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;

/**
 * This is the launch activity, usually not seen by user unless they are not
 * logged in
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final List<String> FB_READ_PERMISSIONS =
            Arrays.asList("email");


    @Override
    protected void onResume() {
        super.onResume();
        if (ParseUser.getCurrentUser() != null) {
            try {
                ParseUser.getCurrentUser().fetch();
                goMainActivity();
            } catch (ParseException e) {
                Log.e(TAG, "Unable to fetch current user");
                Toast.makeText(this,
                        "Unable to fetch current user, please try logging in " +
                                "again", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ParseFacebookUtils.initialize(this);

        final Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText etUsername = findViewById(R.id.etUsername);
                final EditText etPassword = findViewById(R.id.etPassword);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        final Button btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(
                        LoginActivity.this, FB_READ_PERMISSIONS,
                        new LogInCallback() {
                            @Override
                            public void done(ParseUser user,
                                             ParseException err) {
                                if (user == null) {
                                    Log.e(TAG, "Facebook Login Failed");
                                    Toast.makeText(LoginActivity.this,
                                            "Facebook Login Failed",
                                            Toast.LENGTH_SHORT).show();
                                } else if (user.isNew()) {
                                    Intent i = new Intent(LoginActivity.this,
                                            FacebookSignupActivity.class);
                                    i.putExtra(Extras.EXTRA_USER,
                                            Parcels.wrap(user));
                                    startActivity(i);
                                } else {
                                    goMainActivity();
                                }
                            }
                        });
            }
        });

        final Button btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this,
                            "Issue logging in, please try again.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

}