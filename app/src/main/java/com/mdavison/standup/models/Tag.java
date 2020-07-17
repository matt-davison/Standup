package com.mdavison.standup.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Tag")
public class Tag extends ParseObject {

    public static final String KEY_TAG = "tag";
    public static final String KEY_POSTS_COUNT = "postsCount";

    public Tag() {
        //required empty constructor
    }
}
