package com.mdavison.standup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.adapters.PostAdapter;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.support.EndlessRecyclerViewScrollListener;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.ItemClickSupport;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class CommunityDetailsActivity extends AppCompatActivity {
    private static final String TAG = "CommunityDetailsActivity";
    private List<Post> communityPosts;
    private PostAdapter postAdapter;
    private Community community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_details);

        community = (Community) Parcels
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

        final ToggleButton tbtnJoin = findViewById(R.id.tbtnJoin);
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
                    tbtnJoin.setOnCheckedChangeListener(
                            (compoundButton, isChecked) -> {
                                if (isChecked) {
                                    ParseUser.getCurrentUser()
                                            .getRelation("communities")
                                            .add(community);
                                    community
                                            .increment(Community.KEY_USER_COUNT,
                                                    1);
                                    Log.i(TAG, "ischecked");
                                } else {
                                    ParseUser.getCurrentUser()
                                            .getRelation("communities")
                                            .remove(community);
                                    community
                                            .increment(Community.KEY_USER_COUNT,
                                                    -1);
                                }
                                ParseUser.getCurrentUser().saveInBackground();
                                community.saveInBackground();
                            });
                });
        final RecyclerView rvPosts = findViewById(R.id.rvPosts);
        communityPosts = new ArrayList<>();
        postAdapter = new PostAdapter(this, communityPosts);
        rvPosts.setAdapter(postAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPosts.setLayoutManager(layoutManager);
        queryPosts();
        ItemClickSupport.addTo(rvPosts)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    Intent i = new Intent(CommunityDetailsActivity.this,
                            PostDetailsActivity.class);
                    i.putExtra(Extras.EXTRA_POST,
                            Parcels.wrap(communityPosts.get(position)));
                    startActivity(i);
                });
        final EndlessRecyclerViewScrollListener scrollListener =
                new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount,
                                           RecyclerView view) {
                        queryPosts();
                    }
                };
        rvPosts.addOnScrollListener(scrollListener);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.whereEqualTo(Post.KEY_POSTED_TO, community);
        query.setLimit(20);
        query.setSkip(communityPosts.size());
        query.addDescendingOrder(Post.KEY_CREATED);
        query.findInBackground((newPosts, error) -> {
            if (error != null) {
                Log.e(TAG, "Issue with getting posts", error);
                Toast.makeText(this, "Issue getting posts", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            postAdapter.addAll(newPosts);
            Log.i(TAG, "Received " + newPosts.size() + " posts");
        });
    }
}