package com.mdavison.standup.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.mdavison.standup.R;
import com.mdavison.standup.activities.ProfileActivity;
import com.mdavison.standup.holders.FeedPostHolder;
import com.mdavison.standup.models.Comment;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.models.User;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.OnSwipeTouchListener;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This Fragment displays a stream of posts to the user.
 */
public class StreamFragment extends Fragment {

    private static final String TAG = "StreamFragment";
    private static final int MAX_TRENDING_HRS = 480;
    private List<Post> posts;
    private List<Comment> comments;
    private CardView postFront;
    private CardView postBehind;
    private CardView postDetail;
    private LinearLayout llComments;
    private Button btnMoreComments;
    private ParseUser currentUser;
    private int postsRetrieved = 0;
    private SortBy sortBy = SortBy.LATEST;
    private boolean onlyUnseenPosts = false;
    private FeedPostHolder postHolder;

    public StreamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stream, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull final View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUser = ParseUser.getCurrentUser();
        posts = new ArrayList<>();
        postHolder = new FeedPostHolder(posts);
        comments = new ArrayList<>();
        queryPosts();
        postBehind = view.findViewById(R.id.postBehind);
        postFront = view.findViewById(R.id.post);
        postDetail = view.findViewById(R.id.postDetail);
        llComments = postDetail.findViewById(R.id.llComments);
        final ScrollView svDetailsHolder = view.findViewById(R.id.svDetails);
        final OnSwipeTouchListener swipeListener =
                new OnSwipeTouchListener(getContext()) {

                    //TODO: fade in the alpha of postBehind on swipe so that
                    // it doesn't show large posts behind small posts
                    @Override
                    public void onSwipeRight() {
                        this.onSwipeDown();
                        ValueAnimator swipeAnim = ObjectAnimator
                                .ofFloat(postFront, View.TRANSLATION_X,
                                        view.getWidth());
                        swipeAnim.setDuration(250);
                        swipeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                likePost();
                                ObjectAnimator
                                        .ofFloat(postFront, View.TRANSLATION_X,
                                                0).setDuration(0).start();
                            }
                        });
                        swipeAnim.start();
                        ValueAnimator fadeAnim = ObjectAnimator
                                .ofFloat(postBehind, View.ALPHA, 1);
                        fadeAnim.setDuration(250);
                        fadeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ObjectAnimator
                                        .ofFloat(postBehind, View.ALPHA, 0)
                                        .setDuration(0).start();
                            }
                        });
                        fadeAnim.start();
                    }

                    @Override
                    public void onSwipeLeft() {
                        this.onSwipeDown();
                        ValueAnimator swipeAnim = ObjectAnimator
                                .ofFloat(postFront, View.TRANSLATION_X,
                                        -view.getWidth());
                        swipeAnim.setDuration(250);
                        swipeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                viewPost();
                                ObjectAnimator
                                        .ofFloat(postFront, View.TRANSLATION_X,
                                                0).setDuration(0).start();
                            }
                        });
                        swipeAnim.start();
                        ValueAnimator fadeAnim = ObjectAnimator
                                .ofFloat(postBehind, View.ALPHA, 1);
                        fadeAnim.setDuration(250);
                        fadeAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ObjectAnimator
                                        .ofFloat(postBehind, View.ALPHA, 0)
                                        .setDuration(0).start();
                            }
                        });
                        fadeAnim.start();
                    }

                    @Override
                    public void onSwipeUp() {
                        postFront.setVisibility(View.GONE);
                        postBehind.setVisibility(View.GONE);
                        postDetail.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSwipeDown() {
                        postDetail.setVisibility(View.GONE);
                        postFront.setVisibility(View.VISIBLE);
                        postBehind.setVisibility(View.VISIBLE);
                    }

                };
        view.setOnTouchListener(swipeListener);
        svDetailsHolder.setOnTouchListener(swipeListener);
        final TextView tvAuthor = postFront.findViewById(R.id.tvAuthor);
        tvAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent i =
                        new Intent(getContext(), ProfileActivity.class);
                i.putExtra(Extras.EXTRA_USER,
                        Parcels.wrap(posts.get(0).getAuthor()));
                startActivity(i);
            }
        });
        postBehind.setAlpha(0);
        postFront.setVisibility(View.INVISIBLE);
        final EditText etNewComment =
                postDetail.findViewById(R.id.etNewComment);
        final Button btnCreateComment =
                postDetail.findViewById(R.id.btnCreateComment);
        btnCreateComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etNewComment.getText().toString().isEmpty()) {
                    Comment comment = new Comment();
                    comment.setAuthor(currentUser);
                    comment.setComment(etNewComment.getText().toString());
                    final ParseRelation<Comment> commentRelation =
                            posts.get(0).getRelation(Post.KEY_COMMENTS);
                    commentRelation.add(comment);
                    comment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            posts.get(0).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    etNewComment.setText("");
                                    comments.clear();
                                    llComments.removeAllViews();
                                    loadComments();
                                }
                            });
                        }
                    });

                }
            }
        });
        btnMoreComments = postDetail.findViewById(R.id.btnMoreComments);
        btnMoreComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadComments();
            }
        });
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
        advancePost();
    }

    private void advancePost() {
        if (posts.size() <= 1) {
            Log.e(TAG, "Not enough posts to advance");
            Toast.makeText(getContext(),
                    "No more posts to show, try following more communities",
                    Toast.LENGTH_LONG).show();
            return;
        }
        posts.remove(0);
        if (posts.size() <= 5) {
            queryPosts();
        }
        setPosts();
    }

    private void setPosts() {
        if (posts.size() == 0) {
            Log.w(TAG, "No posts to show");
            Toast.makeText(getContext(),
                    "No posts to show, try following more communities",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (posts.size() == 1) {
            Log.w(TAG, "Not enough posts");
            setPost(posts.get(0), postFront);
            setPost(posts.get(0), postBehind);
            setPost(posts.get(0), postDetail);
            return;
        }

        setPost(posts.get(0), postFront);
        setPost(posts.get(1), postBehind);

        setPost(posts.get(0), postDetail);
        comments.clear();
        llComments.removeAllViews();
        loadComments();
    }

    private void setPost(Post post, CardView postView) {
        if (getContext() == null) {
            return;
        }
        TextView tvTitle = postView.findViewById(R.id.tvTitle);
        TextView tvAuthor = postView.findViewById(R.id.tvAuthor);
        ImageView ivMedia = postView.findViewById(R.id.ivMedia);
        TextView tvDescription = postView.findViewById(R.id.tvDescription);

        tvTitle.setText(post.getTitle());
        if (post.getDescription() != null) {
            tvDescription.setText(post.getDescription());
            if (tvDescription.getVisibility() == View.GONE) {
                tvDescription.setVisibility(View.VISIBLE);
            }
        } else {
            tvDescription.setVisibility(View.GONE);
        }
        if (post.getMedia() != null) {
            Glide.with(getContext()).load(post.getMedia().getUrl()).fitCenter()
                    .transform(new RoundedCorners(20)).into(ivMedia);
        } else {
            Glide.with(getContext()).clear(ivMedia);
            ivMedia.setImageResource(0);
        }
        try {
            tvAuthor.setText((post.getAuthor().fetchIfNeeded()).getUsername());
        } catch (ParseException e) {
            Log.e(TAG, "Unable to fetch author");
        }

        final TextView tvRelativeCreation =
                postView.findViewById(R.id.tvRelativeCreation);
        final long now = new Date().getTime();
        String relativeDate = DateUtils
                .getRelativeTimeSpanString(post.getCreatedAt().getTime(), now,
                        DateUtils.SECOND_IN_MILLIS).toString();
        tvRelativeCreation.setText(relativeDate);
    }

    private void loadComments() {
        final ParseRelation<Comment> commentRelation =
                posts.get(0).getRelation(Post.KEY_COMMENTS);
        final ParseQuery<Comment> query = commentRelation.getQuery();
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        query.setLimit(10);
        query.setSkip(comments.size());
        query.findInBackground((results, e) -> {
            if (e != null) {
                Log.e(TAG, "Error fetching comments");
            } else {
                if (results.size() == 0) {
                    btnMoreComments.setVisibility(View.GONE);
                } else {
                    comments.addAll(results);
                    btnMoreComments.setVisibility(View.VISIBLE);
                    query.setSkip(comments.size());
                    query.setLimit(1);
                    query.findInBackground((more, err) -> {
                        if (more.size() > 0) {
                            btnMoreComments.setVisibility(View.VISIBLE);
                        }
                        else {
                            btnMoreComments.setVisibility(View.GONE);
                        }
                    });
                }
                for (int i = 0; i < results.size(); i++) {
                    final View comment = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_comment, null);
                    try {
                        final TextView tvAuthor =
                                comment.findViewById(R.id.tvAuthor);
                        tvAuthor.setText(
                                results.get(i).getAuthor().fetchIfNeeded()
                                        .getUsername());
                        tvAuthor.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(),
                                        ProfileActivity.class);
                                i.putExtra(Extras.EXTRA_USER,
                                        Parcels.wrap(posts.get(0).getAuthor()));
                                startActivity(i);
                            }
                        });
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    ((TextView) comment.findViewById(R.id.tvComment))
                            .setText(results.get(i).getComment());
                    final long now = new Date().getTime();
                    String relativeDate = DateUtils.getRelativeTimeSpanString(
                            results.get(i).getCreatedAt().getTime(), now,
                            DateUtils.SECOND_IN_MILLIS).toString();
                    ((TextView) comment.findViewById(R.id.tvDate))
                            .setText(relativeDate);
                    llComments.addView(comment,
                            comments.size() - results.size() + i);
                }
            }
        });
    }

    //TODO: Move this and all possible post logic into FeedPostHolder
    private void queryPosts() {
        ParseRelation<Community> communitiesRelation =
                currentUser.getRelation(User.KEY_COMMUNITIES);
        communitiesRelation.getQuery().findInBackground((communities, e) -> {
            if (e != null) {
                Log.e(TAG, "Issues with getting communities", e);
                Toast.makeText(getContext(), "Issue getting communities",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (communities.size() == 0) {
                Log.e(TAG, "Not following any communities");
                Toast.makeText(getContext(), "Not following any communities",
                        Toast.LENGTH_LONG).show();
                return;
            }
            List<ParseQuery<Post>> queries = new ArrayList<>();
            for (int i = 0; i < communities.size(); i++) {
                ParseQuery<Post> fromCommunity =
                        ParseQuery.getQuery(Post.class);
                fromCommunity
                        .whereEqualTo(Post.KEY_POSTED_TO, communities.get(i));
                queries.add(fromCommunity);
            }
            ParseQuery<Post> query = ParseQuery.or(queries);
            if (onlyUnseenPosts) {
                query.whereDoesNotMatchKeyInQuery(Post.KEY_OBJECT_ID,
                        Post.KEY_OBJECT_ID,
                        currentUser.getRelation(User.KEY_VIEW_HISTORY)
                                .getQuery());
                query.setSkip(posts.size());
            } else {
                query.setSkip(postsRetrieved);
            }
            final Date now = new Date();
            switch (sortBy) {
                case TOP_ALL:
                    query.setLimit(20);
                    query.addDescendingOrder(Post.KEY_RATING);
                    break;
                case TOP_WEEK:
                    query.setLimit(20);
                    query.addDescendingOrder(Post.KEY_RATING);
                    final Date topWeek = new Date(now.getTime() - 604800000);
                    query.whereGreaterThan(Post.KEY_CREATED_AT, topWeek);
                case TRENDING:
                    query.setLimit(20);
                    query.addDescendingOrder(Post.KEY_RATING);
                    final Date trendSince = new Date(
                            now.getTime() - MAX_TRENDING_HRS * 3600000);
                    query.whereGreaterThan(Post.KEY_CREATED_AT, trendSince);
                    break;
                case LATEST:
                default:
                    query.setLimit(20);
                    query.addDescendingOrder(Post.KEY_CREATED_AT);
                    break;
            }
            query.findInBackground((newPosts, error) -> {
                if (error != null) {
                    Log.e(TAG, "Issue with getting posts", error);
                    Toast.makeText(getContext(), "Issue getting posts",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                posts.addAll(newPosts);
                switch (sortBy) {
                    case TRENDING:
                        posts = postHolder.getTrendingPosts();
                        break;
                    case TOP_ALL:
                    case TOP_WEEK:
                        posts = postHolder.getTopPosts();
                        break;
                }
                if (postsRetrieved == 0) {
                    postFront.setVisibility(View.VISIBLE);
                    setPosts();
                }
                postsRetrieved += newPosts.size();
            });
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_stream, menu);
        MenuItem selected = menu.findItem(R.id.action_latest);
        selected.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_latest:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    posts.clear();
                    sortBy = SortBy.LATEST;
                    postsRetrieved = 0;
                    queryPosts();
                }
                return true;
            case R.id.action_trending:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    posts.clear();
                    sortBy = SortBy.TRENDING;
                    postsRetrieved = 0;
                    queryPosts();
                }
                return true;
            case R.id.action_top_week:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    posts.clear();
                    sortBy = SortBy.TOP_WEEK;
                    postsRetrieved = 0;
                    queryPosts();
                }
                return true;
            case R.id.action_top_all:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    posts.clear();
                    sortBy = SortBy.TOP_ALL;
                    postsRetrieved = 0;
                    queryPosts();
                }
                return true;
            case R.id.action_show_unseen:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    onlyUnseenPosts = true;
                } else {
                    item.setChecked(false);
                    onlyUnseenPosts = false;
                }
                posts.clear();
                postsRetrieved = 0;
                queryPosts();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private enum SortBy {
        LATEST, TRENDING, TOP_ALL, TOP_WEEK
    }
}