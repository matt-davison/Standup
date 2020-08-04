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
    private long now;

    public FeedPostHolder(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getLatestPosts() {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post to) {
                return post.getCreatedAt().getTime() <
                        to.getCreatedAt().getTime() ? 1 : -1;
            }
        });
        return posts;
    }

    public List<Post> getTopPosts() {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post to) {
                if (post.getRating() < to.getRating()) {
                    return 1;
                } else if (post.getRating() > to.getRating()) {
                    return -1;
                } else {
                    return post.getCreatedAt().getTime() <
                            to.getCreatedAt().getTime() ? 1 : -1;
                }
            }
        });
        return posts;
    }

    public List<Post> getTrendingPosts() {
        now = new Date().getTime();
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post to) {
                return trendingScore(post) < trendingScore(to) ? 1 : -1;
            }
        });
        return posts;
    }

    private double trendingScore(Post post) {
        return Math.pow(post.getRating() + RATE_ADJUSTMENT, RATE_FACTOR) /
                Math.pow(now - post.getCreatedAt().getTime() + TIME_ADJUSTMENT,
                        TIME_FACTOR);
    }
}
