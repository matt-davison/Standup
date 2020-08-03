package com.mdavison.standup.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_LIKE_COUNT = "likes";
    public static final String KEY_RATING = "rating";
    public static final String KEY_VIEW_COUNT = "views";
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
        ParseQuery<ParseObject> query =
                user.getRelation(User.KEY_LIKE_HISTORY).getQuery();
        query.whereEqualTo(Post.KEY_OBJECT_ID, getObjectId());
        query.countInBackground((count, e) -> {
            if (e == null && count == 0) {
                user.getRelation(User.KEY_LIKE_HISTORY).add(Post.this);
                user.saveInBackground();
                increment(KEY_LIKE_COUNT);
                put(KEY_RATING,
                        getDouble(KEY_LIKE_COUNT) / getDouble(KEY_VIEW_COUNT));
                saveInBackground();
            }
        });
        return true;
    }

    public boolean addViewer(ParseUser user) {
        ParseQuery<ParseObject> query =
                user.getRelation(User.KEY_VIEW_HISTORY).getQuery();
        query.whereEqualTo(Post.KEY_OBJECT_ID, getObjectId());
        query.countInBackground((count, e) -> {
            if (e == null && count == 0) {
                user.getRelation(User.KEY_VIEW_HISTORY).add(Post.this);
                increment(KEY_VIEW_COUNT);
                user.saveInBackground();
                put(KEY_RATING,
                        getDouble(KEY_LIKE_COUNT) / getDouble(KEY_VIEW_COUNT));
                saveInBackground();
            }
        });
        return true;
    }

    public double getRating() {
        return getDouble(KEY_RATING);
    }
}
