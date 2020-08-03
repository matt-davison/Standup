package com.mdavison.standup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.FaceDetector;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mdavison.standup.R;
import com.mdavison.standup.support.Extras;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.parceler.Parcels;

public class FacebookSignupActivity extends AppCompatActivity {

    private static final String TAG = "FacebookSignupActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_signup);

        final Button btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser user = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra(
                        Extras.EXTRA_USER));
                final EditText etUsername = findViewById(R.id.etUsername);
                final String username = etUsername.getText().toString();
                user.setUsername(username);
                user.saveInBackground();
                finish();
            }
        });
    }
}