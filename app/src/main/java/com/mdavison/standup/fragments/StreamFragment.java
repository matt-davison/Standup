package com.mdavison.standup.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.models.User;
import com.mdavison.standup.support.OnSwipeTouchListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.widget.Toast.makeText;


public class StreamFragment extends Fragment {

    private static final String TAG = "StreamFragment";
    //TODO: Switch to queue
    private List<Post> posts;
    private CardView post;
    private CardView postBehind;
    private TextView tvTitle;
    private TextView tvAuthor;
    private ImageView ivMedia;
    private ParseUser currentUser;

    public StreamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stream, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        posts = new LinkedList<Post>();
        queryPosts();
        tvTitle = view.findViewById(R.id.tvTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        ivMedia = view.findViewById(R.id.ivMedia);

        //TODO: Remove this button and use swipe gesture instead
        final Button btnLike = view.findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });
        postBehind = view.findViewById(R.id.postBehind);
        postBehind.setVisibility(View.GONE);
        post = view.findViewById(R.id.post);
        CardView postCard = post.findViewById(R.id.cvPost);
        /*
        post.setOnTouchListener(new OnSwipeTouchListener(getContext()) {

            @Override
            public void onSwipeRight() {
                Toast.makeText(getContext(), "\u2764", Toast.LENGTH_SHORT).show();
                likePost();
            }

            @Override
            public void onSwipeLeft() {
                viewPost();
            }

        });
        */

        postCard.setOnTouchListener(new View.OnTouchListener() {
            float dx, dy;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i(TAG, "Pressing");
                        dx = view.getX() - motionEvent.getRawX();
                        dy = view.getY() - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i(TAG, "Moving");
                        view.animate()
                                .x(motionEvent.getRawX() + dx - (view.getWidth() / 2))
                                .y(motionEvent.getRawY() + dy - (view.getHeight() / 2))
                                .setDuration(0)
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (motionEvent.getRawX() >= view.getWidth() * 4 / 5) {
                            likePost();
                        }
                        else if (motionEvent.getRawX() <= view.getWidth() / 5) {
                            viewPost();
                        }
                        view.clearAnimation();
                        view.animate().translationX(0).translationY(0);
                    default:
                        return false;
                }
                return true;
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "click");
            }
        });
    }

    private void likePost() {
        posts.get(0).addLike(currentUser);
        viewPost();
    }

    private void viewPost() {
        posts.get(0).addViewer(currentUser);
        advancePost();
    }

    private void advancePost() {
        posts.remove(0);
        showPost();
    }

    private void showPost() {
        if (posts.size() == 0) {
            Log.e(TAG, "No posts to show");
            makeText(getContext(), "No posts to show", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        tvTitle.setText(posts.get(0).getTitle());
        if (posts.get(0).getMedia() != null) {
            Glide.with(getContext()).load(posts.get(0).getMedia().getUrl())
                    .into(ivMedia);
        }
        //Change fetchIfNeeded to query.include(ptr to author) to reduce queries
        try {
            tvAuthor.setText(
                    (posts.get(0).getAuthor().fetchIfNeeded()).getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void queryPosts() {
        currentUser = ParseUser.getCurrentUser();
        //TODO: Generate list of communities on fragment opening and reuse
        ParseRelation<Community> communitiesRelation =
                currentUser.getRelation(User.KEY_COMMUNITIES);
        communitiesRelation.getQuery()
                .findInBackground(new FindCallback<Community>() {
                    @Override
                    public void done(List<Community> communities,
                                     ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issues with getting communities", e);
                            makeText(getContext(),
                                    "Issue getting communities",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (communities.size() == 0) {
                            Log.e(TAG, "Not following any communities");
                            makeText(getContext(),
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
                                    makeText(getContext(),
                                            "Issue getting posts",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                posts.addAll(newPosts);
                                Log.i(TAG,
                                        "Received " + posts.size() + " posts");
                                showPost();
                            }
                        });
                    }
                });

    }
}