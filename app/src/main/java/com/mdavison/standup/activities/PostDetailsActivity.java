package com.mdavison.standup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Comment;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.support.Extras;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        final Post post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Extras.EXTRA_POST));

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
        final TextView tvRelativeCreation = findViewById(R.id.tvRelativeCreation);
        final long now = new Date().getTime();
        final String relativeDate = DateUtils
                .getRelativeTimeSpanString(post.getCreatedAt().getTime(),
                        now, DateUtils.SECOND_IN_MILLIS).toString();
        tvRelativeCreation.setText(relativeDate);
        final LinearLayout llComments = findViewById(R.id.llComments);
        final List<Comment> comments = new ArrayList<>();
        ParseRelation<Comment> commentRelation =
                post.getRelation(Post.KEY_COMMENTS);
        ParseQuery<Comment> query = commentRelation.getQuery();
        query.addDescendingOrder(Comment.KEY_LIKES);
        query.setLimit(20);
        query.findInBackground((results, e) -> {
            if (e != null) {
                Log.e(TAG, "Error fetching comments");
            } else {
                comments.clear();
                comments.addAll(results);
                for (int i = 0; i < results.size(); i++) {
                    View comment = LayoutInflater.from(this)
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
                    llComments.addView(comment,
                            comments.size() - results.size() + i);
                }
            }
        });
    }
}