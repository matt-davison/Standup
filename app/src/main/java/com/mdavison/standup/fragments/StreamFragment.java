package com.mdavison.standup.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Comment;
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
    private List<Comment> comments;
    private CardView postFront;
    private CardView postBehind;
    private CardView postDetail;
    private LinearLayout llComments;
    private ParseUser currentUser;
    private TextView tvDetailAuthor;
    private TextView tvDetailDescription;
    private ImageView ivDetailMedia;
    private TextView tvDetailDate;
    private TextView ivDetailTitle;
    private int postsViewed = 0;

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
    public void onViewCreated(@NonNull final View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        posts = new LinkedList<Post>();
        comments = new ArrayList<Comment>();
        queryPosts();
        postBehind = view.findViewById(R.id.postBehind);
        //TODO: Make postFront an item_post_detail and change height instead
        // of having another view
        postFront = view.findViewById(R.id.post);
        postDetail = view.findViewById(R.id.postDetail);
        llComments = postDetail.findViewById(R.id.llComments);
        ScrollView svDetailsHolder = view.findViewById(R.id.svDetails);
        tvDetailAuthor = postDetail.findViewById(R.id.tvAuthor);
        tvDetailDescription = postDetail.findViewById(R.id.tvDescription);
        tvDetailDate = postDetail.findViewById(R.id.tvRelativeCreation);
        ivDetailMedia = postDetail.findViewById(R.id.ivMedia);
        ivDetailTitle = postDetail.findViewById(R.id.tvTitle);

        OnSwipeTouchListener swipeListener =
                new OnSwipeTouchListener(getContext()) {

                    //TODO: fade in the alpha of postBehind on swipe so that
                    // it doesn't show large posts behind small posts
                    @Override
                    public void onSwipeRight() {
                        this.onSwipeDown();
                        Toast.makeText(getContext(), "\uD83D\uDC4D",
                                Toast.LENGTH_SHORT).show();
                        ValueAnimator swipeAnim = ObjectAnimator
                                .ofFloat(postFront, View.TRANSLATION_X,
                                        view.getWidth());
                        swipeAnim.setDuration(250);
                        swipeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                likePost();
                                ValueAnimator returnAnim = ObjectAnimator
                                        .ofFloat(postFront, View.X, 0f);
                                returnAnim.setDuration(0);
                                returnAnim.start();
                            }
                        });
                        swipeAnim.start();
                        Log.i(TAG, "swipe right");

                    }

                    @Override
                    public void onSwipeLeft() {
                        this.onSwipeDown();
                        Toast.makeText(getContext(), "\uD83D\uDC4E",
                                Toast.LENGTH_SHORT).show();
                        ValueAnimator swipeAnim = ObjectAnimator
                                .ofFloat(postFront, View.TRANSLATION_X,
                                        -view.getWidth());
                        swipeAnim.setDuration(250);
                        swipeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                viewPost();
                                ValueAnimator returnAnim = ObjectAnimator
                                        .ofFloat(postFront, View.X, 0f);
                                returnAnim.setDuration(0);
                                returnAnim.start();
                            }
                        });
                        swipeAnim.start();
                        Log.i(TAG, "swipe left");

                    }

                    @Override
                    public void onSwipeUp() {
                        postFront.setVisibility(View.GONE);
                        postBehind.setVisibility(View.GONE);
                        postDetail.setVisibility(View.VISIBLE);
                        Log.i(TAG, "swipe up");
                    }

                    @Override
                    public void onSwipeDown() {
                        postDetail.setVisibility(View.GONE);
                        postFront.setVisibility(View.VISIBLE);
                        postBehind.setVisibility(View.VISIBLE);
                        Log.i(TAG, "swipe down");
                    }

                };
        view.setOnTouchListener(swipeListener);
        svDetailsHolder.setOnTouchListener(swipeListener);
    }

    private void likePost() {
        if (posts.size() == 0) {
            return;
        }
        posts.get(0).addLike(currentUser);
        viewPost();
    }

    private void viewPost() {
        if (posts.size() == 0) {
            return;
        }
        posts.get(0).addViewer(currentUser);
        postsViewed++;
        advancePost();
    }

    private void advancePost() {
        if (posts.size() == 0) {
            Log.e(TAG, "No posts to advance");
            makeText(getContext(), "No posts!", Toast.LENGTH_LONG).show();
            //queryMorePosts();
            return;
        }
        if (posts.size() < 5) {
            //queryMorePosts();
        }
        posts.remove(0);
        setPosts();
    }

    private void setPosts() {
        if (posts.size() == 0) {
            Log.e(TAG, "No posts to show");
            makeText(getContext(), "No posts to show", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (posts.size() == 1) {
            Log.e(TAG, "Not enough posts");
            makeText(getContext(), "Last Post", Toast.LENGTH_LONG).show();
            setPost(posts.get(0), postFront);
            return;
        }


        setPost(posts.get(0), postFront);
        setPost(posts.get(1), postBehind);

        try {
            tvDetailAuthor.setText(
                    (posts.get(0).getAuthor().fetchIfNeeded()).getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (posts.get(0).getDescription() != null) {
            tvDetailDescription.setText((posts.get(0).getDescription()));
        }
        ivDetailTitle.setText((posts.get(0).getTitle()));
        if (posts.get(0).getMedia() != null) {
            Glide.with(getContext()).load(posts.get(0).getMedia().getUrl())
                    .fitCenter().into(ivDetailMedia);
        } else {
            Glide.with(getContext()).clear(ivDetailMedia);
        }

        ParseRelation<Comment> commentRelation =
                posts.get(0).getRelation(Post.KEY_COMMENTS);
        ParseQuery<Comment> query = commentRelation.getQuery();
        query.addDescendingOrder(Comment.KEY_LIKES);
        query.setLimit(20);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> results, ParseException e) {
                if (e != null) {
                    // There was an error
                } else {
                    Log.i(TAG, "Received " + results.size() + " comments");
                    comments.clear();
                    comments.addAll(results);
                    for (int i = 0; i < results.size(); i++) {
                        View comment = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_comment, null);
                        try {
                            ((TextView) comment.findViewById(R.id.tvAuthor))
                                    .setText(comments.get(i).getAuthor()
                                            .fetchIfNeeded().getUsername());
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                        ((TextView) comment.findViewById(R.id.tvComment))
                                .setText(comments.get(i).getComment());
                        //TODO: Set date!
                        llComments.addView(comment,
                                comments.size() - results.size() + i);
                    }
                }
            }
        });

    }

    private void setPost(Post post, CardView postView) {
        if (getContext() == null) {return;}
        TextView tvTitle = postView.findViewById(R.id.tvTitle);
        TextView tvAuthor = postView.findViewById(R.id.tvAuthor);
        ImageView ivMedia = postView.findViewById(R.id.ivMedia);

        //TODO: Set Date!
        tvTitle.setText(post.getTitle());
        Log.i(TAG, "attaching: " + post.getTitle());
        if (post.getMedia() != null) {
            Glide.with(getContext()).load(post.getMedia().getUrl()).fitCenter()
                    .into(ivMedia);
        } else {
            Glide.with(getContext()).clear(ivMedia);
        }
        //Change fetchIfNeeded to query.include(ptr to author) to reduce queries
        try {
            tvAuthor.setText((post.getAuthor().fetchIfNeeded()).getUsername());
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
                            makeText(getContext(), "Issue getting communities",
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
                        //query.setSkip(postsViewed);
                        query.addDescendingOrder(Post.KEY_CREATED);
                        query.findInBackground(new FindCallback<Post>() {
                            @Override
                            public void done(List<Post> newPosts,
                                             ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Issue with getting posts", e);
                                    makeText(getContext(),
                                            "Issue getting posts",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                posts.addAll(newPosts);
                                Log.i(TAG, "Received " + newPosts.size() +
                                        " posts");
                                if (postsViewed == 0) {
                                    setPosts();
                                }
                            }
                        });
                    }
                });
    }


    private void queryMoreComments(Comment comment) {
        ParseRelation<Comment> commentRelation =
                posts.get(0).getRelation(Post.KEY_COMMENTS);
        ParseQuery<Comment> query = commentRelation.getQuery();
        query.addDescendingOrder(Comment.KEY_LIKES);
        query.setLimit(20);
        query.setSkip(comments.size());
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> results, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    makeText(getContext(), "Issue getting comments",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.i(TAG, "Received " + results.size() + " comments");
                    comments.addAll(results);
                }
            }
        });
    }

    //TODO: Might be of user later... will leave for now
    /*
    private void querySubComments(Comment comment) {
        ParseRelation<Comment> subComments =
                comment.getRelation(Comment.KEY_SUB_COMMENTS);
        ParseQuery<Comment> query = subComments.getQuery();
        query.setLimit(20);
        query.addDescendingOrder(Comment.KEY_LIKES);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    makeText(getContext(), "Issue getting comments",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                comments.clear();
                comments.addAll(comments);
                Log.i(TAG, "Received " + comments.size() + " comments");
                if (postsViewed == 0) {
                    setPosts();
                }
            }
        });
    }
     */

}