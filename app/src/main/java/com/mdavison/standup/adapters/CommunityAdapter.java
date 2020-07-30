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
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

/**
 * This adapter adapts Posts to a RecyclerView
 */
public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";
    private final Context context;
    private final List<Community> communities;

    public CommunityAdapter(Context context, List<Community> communities) {
        this.context = context;
        this.communities = communities;
    }

    @NonNull
    @Override
    public CommunityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_community_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(communities.get(position));
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public void clear() {
        communities.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Community> list) {
        communities.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private ImageView ivIcon;
        private TextView tvDescription;
        private TextView tvUserCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvUserCount = itemView.findViewById(R.id.tvUserCount);
        }

        public void bind(Community community) {
            tvName.setText(community.getName());
            tvDescription.setText(community.getDescription());
            ParseFile image = community.getIcon();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivIcon);
            } else {
                Glide.with(context).clear(ivIcon);
            }
            tvUserCount.setText(community.getUserCount() + " followers");
        }
    }
}
