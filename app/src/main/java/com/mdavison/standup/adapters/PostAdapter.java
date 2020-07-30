package com.mdavison.standup.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

/**
 * This adapter adapts Posts to a RecyclerView
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";
    private final Context context;
    private final List<Post> posts;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAuthor;
        private ImageView ivMedia;
        private TextView tvDescription;
        private TextView tvRelativeCreation;
        private TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvRelativeCreation = itemView.findViewById(R.id.tvRelativeCreation);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }

        public void bind(Post post) {
            try {
                tvAuthor.setText(post.getAuthor().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                Log.e(TAG, "Unable to fetch username");
                tvAuthor.setText("");
            }
            tvTitle.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            ParseFile image = post.getMedia();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivMedia);
            } else {
                Glide.with(context).clear(ivMedia);
            }
            long now = new Date().getTime();
            String relativeDate = DateUtils
                    .getRelativeTimeSpanString(post.getCreatedAt().getTime(),
                            now, DateUtils.SECOND_IN_MILLIS).toString();
            tvRelativeCreation.setText(relativeDate);
        }
    }
}
