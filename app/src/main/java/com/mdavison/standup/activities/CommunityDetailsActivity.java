package com.mdavison.standup.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.support.Extras;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class CommunityDetailsActivity extends AppCompatActivity {
    private static final String TAG = "CommunityDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_details);

        final Community community = (Community) Parcels
                .unwrap(getIntent().getParcelableExtra(Extras.EXTRA_COMMUNITY));
        final TextView tvName = findViewById(R.id.tvName);
        tvName.setText(community.getName());
        final TextView tvUserCount = findViewById(R.id.tvUserCount);
        tvUserCount.setText(community.getUserCount() + " followers");
        final ImageView ivBanner = findViewById(R.id.ivBanner);
        if (community.getBanner() != null) {
            Glide.with(this).load(community.getBanner().getUrl()).fitCenter()
                    .into(ivBanner);
        } else {
            Glide.with(this).clear(ivBanner);
        }
        final TextView tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(community.getDescription());

        ToggleButton tbtnJoin = findViewById(R.id.tbtnJoin);
        ParseUser.getCurrentUser().getRelation("communities").getQuery()
                .whereEqualTo(Community.KEY_NAME, community.getName())
                .countInBackground((foundCommunities, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to get community information", e);
                        Toast.makeText(this, "Unable to check following status",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (foundCommunities == 0) {
                        Log.i(TAG, "not in community");
                        tbtnJoin.setChecked(false);
                    } else {
                        Log.i(TAG, "in community");
                        tbtnJoin.setChecked(true);
                    }
                });
        tbtnJoin.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                ParseUser.getCurrentUser().getRelation("communities").add(community);
                community.increment(Community.KEY_USER_COUNT, 1);
                Log.i(TAG, "ischecked");
            } else {
                ParseUser.getCurrentUser().getRelation("communities").remove(community);
                community.increment(Community.KEY_USER_COUNT, -1);
            }
            ParseUser.getCurrentUser().saveInBackground();
            community.saveInBackground();
        });
    }
}