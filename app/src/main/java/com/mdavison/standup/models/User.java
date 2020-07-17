package com.mdavison.standup.models;

import com.parse.ParseUser;

public class User extends ParseUser {
    public static final String KEY_LIKE_HISTORY = "likeHistory";
    public static final String KEY_TAG_HISTORY = "tagHistory";
    public static final String KEY_FOLLOWERS = "followers";
    public static final String KEY_FOLLOWINGS = "followings";
    public static final String KEY_FOLLOWING = "following";
    public static final String KEY_COMMUNITIES = "communities";
}
