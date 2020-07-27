package com.mdavison.standup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * This model represents a community and is compatible with the Parse database
 */
@ParseClassName("Community")
public class Community extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_NAME = "name";
    public static final String KEY_BANNER = "banner";
    public static final String KEY_ICON = "icon";
    public static final String KEY_MODS = "mods";
    public static final String KEY_USER_COUNT = "userCount";
    public static final String KEY_POSTS = "posts";
    public static final String KEY_TAGS = "tags";

    // empty constructor needed by Parceler library
    public Community() {
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getName() {
        return getString(KEY_NAME);
    }
}
