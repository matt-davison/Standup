package com.mdavison.standup.applications;

import android.app.Application;

import com.mdavison.standup.BuildConfig;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Registers custom ParseObjects
 */
public class ParseApplication extends Application {

    public static final String CLIENT_KEY = BuildConfig.CLIENT_KEY;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this).applicationId(
                "mdavison-parsetagram") // should correspond to APP_ID env
                // variable
                .clientKey(null)  // set explicitly unless clientKey is
                // explicitly configured on Parse server
                .server("https://mdavison-parsetagram.herokuapp.com/parse/")
                .build());
    }
}
