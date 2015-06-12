package com.effectivelife.polygonsample;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by com on 2015-06-12.
 */
public class PolygonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
