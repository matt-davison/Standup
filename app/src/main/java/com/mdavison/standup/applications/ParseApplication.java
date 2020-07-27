package com.mdavison.standup.applications;

import android.app.Application;

import com.mdavison.standup.BuildConfig;
import com.mdavison.standup.models.Comment;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.models.Tag;
import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Registers custom ParseObjects
 */
public class ParseApplication extends Application {

    public static final String CLIENT_KEY = BuildConfig.CLIENT_KEY;

    @Override
    public void onCreate() {
        super.onCreate();


        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Community.class);
        ParseObject.registerSubclass(Tag.class);
        ParseObject.registerSubclass(Comment.class);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see
        // the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor =
                new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(this).applicationId(
                "mdavison-standup") // should correspond to APP_ID env
                // variable
                .clientKey(null)  // set explicitly unless clientKey is
                // explicitly configured on Parse server
                .server("https://mdavison-standup.herokuapp.com/parse/")
                .build());
    }
}
