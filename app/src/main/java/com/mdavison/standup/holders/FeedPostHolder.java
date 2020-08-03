package com.mdavison.standup.holders;

import com.mdavison.standup.models.Post;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FeedPostHolder {

    private final static double RATE_ADJUSTMENT = 1.0d;
    private final static double RATE_FACTOR = 2.0d;
    private final static long TIME_ADJUSTMENT = 3600000;
    private final static double TIME_FACTOR = 1.0d;

    private List<Post> posts;

    public FeedPostHolder(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getNewPosts() {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post to) {
                return post.getCreatedAt().getTime() >
                        to.getCreatedAt().getTime() ? 1 : -1;
            }
        });
        return posts;
    }

    public List<Post> getTrendingPosts() {
        final long now = new Date().getTime();
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post to) {
                final double fromScore =
                        Math.pow(post.getRating() + RATE_ADJUSTMENT,
                                RATE_FACTOR) / Math.pow(
                                now - post.getCreatedAt().getTime() +
                                        TIME_ADJUSTMENT, TIME_FACTOR);
                final double toScore =
                        Math.pow(post.getRating() + RATE_ADJUSTMENT,
                                RATE_FACTOR) / Math.pow(
                                now - post.getCreatedAt().getTime() +
                                        TIME_ADJUSTMENT, TIME_FACTOR);
                return fromScore > toScore ? 1 : -1;
            }
        });
        return posts;
    }
}
