package com.mdavison.standup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Comment;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.support.Extras;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailsActivity";

    private Post post;
    private LinearLayout llComments;
    private List<Comment> comments;
    private Button btnMoreComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        post = Parcels.unwrap(
                getIntent().getParcelableExtra(Extras.EXTRA_POST));

        final TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(post.getTitle());
        final TextView tvDescription = findViewById(R.id.tvDescription);
        if (post.getDescription() != null) {
            tvDescription.setText(post.getDescription());
        }
        final ImageView ivMedia = findViewById(R.id.ivMedia);
        if (post.getMedia() != null) {
            Glide.with(this).load(post.getMedia().getUrl()).fitCenter()
                    .into(ivMedia);
        } else {
            Glide.with(this).clear(ivMedia);
        }
        final TextView tvAuthor = findViewById(R.id.tvAuthor);
        try {
            tvAuthor.setText((post.getAuthor().fetchIfNeeded()).getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final TextView tvRelativeCreation =
                findViewById(R.id.tvRelativeCreation);
        final long now = new Date().getTime();
        final String relativeDate = DateUtils
                .getRelativeTimeSpanString(post.getCreatedAt().getTime(), now,
                        DateUtils.SECOND_IN_MILLIS).toString();
        tvRelativeCreation.setText(relativeDate);
        llComments = findViewById(R.id.llComments);
        comments = new ArrayList<>();
        final EditText etNewComment = findViewById(R.id.etNewComment);
        final Button btnCreateComment = findViewById(R.id.btnCreateComment);
        btnCreateComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etNewComment.getText().toString().isEmpty()) {
                    Comment comment = new Comment();
                    comment.setAuthor(ParseUser.getCurrentUser());
                    comment.setComment(etNewComment.getText().toString());
                    final ParseRelation<Comment> commentRelation =
                            post.getRelation(Post.KEY_COMMENTS);
                    commentRelation.add(comment);
                    comment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            post.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    etNewComment.setText("");
                                    loadComments();
                                }
                            });
                        }
                    });

                }
            }
        });
        btnMoreComments = findViewById(R.id.btnMoreComments);
        btnMoreComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadComments();
            }
        });
    }

    private void loadComments() {
        final ParseRelation<Comment> commentRelation =
                post.getRelation(Post.KEY_COMMENTS);
        final ParseQuery<Comment> query = commentRelation.getQuery();
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        query.setLimit(10);
        query.setSkip(comments.size());
        query.findInBackground((results, e) -> {
            if (e != null) {
                Log.e(TAG, "Error fetching comments");
            } else {
                comments.addAll(results);
                for (int i = 0; i < results.size(); i++) {
                    final View comment = LayoutInflater.from(this)
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
                                Intent i = new Intent(PostDetailsActivity.this,
                                        ProfileActivity.class);
                                i.putExtra(Extras.EXTRA_USER,
                                        Parcels.wrap(post.getAuthor()));
                                startActivity(i);
                            }
                        });
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    ((TextView) comment.findViewById(R.id.tvComment))
                            .setText(comments.get(i).getComment());
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
}