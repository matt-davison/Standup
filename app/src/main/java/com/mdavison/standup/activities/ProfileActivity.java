package com.mdavison.standup.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mdavison.standup.R;
import com.mdavison.standup.fragments.ProfileFragment;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContainer, new ProfileFragment()).commit();
    }
}