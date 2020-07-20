package com.mdavison.standup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * This model represents a post and is compatible with the Parse database
 */
@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_TITLE = "title";
    public static final String KEY_POSTED_TO = "postedTo";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_MEDIA = "media";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_CREATED = "createdAt";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_LIKE_COUNT = "likes";
    public static final String KEY_RATING = "rating";
    public static final String KEY_VIEW_COUNT = "views";
    public static final String KEY_VIEWERS = "viewers";
    public static final String KEY_LIKERS = "likers";
    public static final String KEY_TAGS = "tags";

    // empty constructor needed by Parceler library
    public Post() {
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getMedia() {
        return getParseFile(KEY_MEDIA);
    }

    public void setMedia(ParseFile parseFile) {
        put(KEY_MEDIA, parseFile);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public void setAuthor(ParseUser user) {
        put(KEY_AUTHOR, user);
    }

    public boolean addLike(ParseUser user) {
        //TODO: Add check if user has already liked post and if so, return false
        put(KEY_LIKERS, user);
        put(KEY_LIKE_COUNT, getNumber(KEY_LIKE_COUNT).longValue() + 1);
        saveInBackground();
        return true;
    }

    public boolean addViewer(ParseUser user) {
        put(KEY_VIEWERS, user);
        put(KEY_VIEW_COUNT, getNumber(KEY_VIEW_COUNT).longValue() + 1);
        saveInBackground();
        return true;
    }
}
