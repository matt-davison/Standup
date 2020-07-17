package com.mdavison.standup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mdavison.standup.R;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.models.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class StreamFragment extends Fragment {

    private static final String TAG = "StreamFragment";
    private List<Post> posts;
    private TextView tvTitle;
    private TextView tvAuthor;

    public StreamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stream, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        posts = new LinkedList<Post>();
        queryPosts();
        tvTitle = view.findViewById(R.id.tvTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);

        //TODO: Remove this button and use swipe gesture instead
        final Button btnLike = view.findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    posts.remove(0);
                    Log.i(TAG, "Advance posts");
                    advancePosts();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void likePost() {
        try {
            advancePosts();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        //remove post in position 0
    }

    private void advancePosts() throws ParseException {
        if(posts.size() == 0) {
            Log.e(TAG, "No posts to show");
            Toast.makeText(getContext(), "No posts to show", Toast.LENGTH_LONG).show();
            return;
        }
        tvTitle.setText(posts.get(0).getTitle());
        //Change fetchIfNeeded to query.include(ptr to author)
        tvAuthor.setText((posts.get(0).getAuthor().fetchIfNeeded()).getUsername());
    }
    private void queryPosts() {
        ParseUser user = ParseUser.getCurrentUser();
        //TODO: Generate list of communities on fragment opening and reuse
        ParseRelation<Community> communitiesRelation =
                user.getRelation(User.KEY_COMMUNITIES);
        communitiesRelation.getQuery()
                .findInBackground(new FindCallback<Community>() {
                    @Override
                    public void done(List<Community> communities,
                                     ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issues with getting communities", e);
                            Toast.makeText(getContext(),
                                    "Issue getting communities",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (communities.size() == 0) {
                            Log.e(TAG, "Not following any communities");
                            Toast.makeText(getContext(),
                                    "Not following any communities",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<ParseQuery<Post>> queries =
                                new ArrayList<ParseQuery<Post>>();
                        for (int i = 0; i < communities.size(); i++) {
                            ParseQuery<Post> fromCommunity =
                                    ParseQuery.getQuery(Post.class);
                            fromCommunity.whereEqualTo(Post.KEY_POSTED_TO,
                                    communities.get(i));
                            queries.add(fromCommunity);
                        }
                        //do similar for following posts
                        ParseQuery<Post> query = ParseQuery.or(queries);
                        query.setLimit(20);
                        query.addDescendingOrder(Post.KEY_CREATED);
                        query.findInBackground(new FindCallback<Post>() {
                            @Override
                            public void done(List<Post> newPosts,
                                             ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Issues with getting posts", e);
                                    Toast.makeText(getContext(),
                                            "Issue getting posts",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                posts.addAll(newPosts);
                                Log.i(TAG, "Received " + posts.size() + " posts");
                                try {
                                    advancePosts();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                });

    }
}