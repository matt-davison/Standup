package com.mdavison.standup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mdavison.standup.R;
import com.mdavison.standup.fragments.ComposeFragment;
import com.mdavison.standup.fragments.ExploreFragment;
import com.mdavison.standup.fragments.ProfileFragment;
import com.mdavison.standup.fragments.StreamFragment;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(
                            @NonNull MenuItem menuItem) {
                        Fragment fragment;
                        switch (menuItem.getItemId()) {
                            case R.id.action_stream:
                                fragment = new StreamFragment();
                                break;
                            case R.id.action_compose:
                                fragment = new ComposeFragment();
                                break;
                            case R.id.action_explore:
                                fragment = new ExploreFragment();
                                break;
                            case R.id.action_profile:
                            default:
                                fragment = new ProfileFragment();
                                break;
                        }
                        fragmentManager.beginTransaction()
                                .replace(R.id.flContainer, fragment).commit();
                        return true;
                    }
                });
        bottomNavigationView.setSelectedItemId(R.id.action_stream);
        bottomNavigationView.setItemIconTintList(null);

    }
}