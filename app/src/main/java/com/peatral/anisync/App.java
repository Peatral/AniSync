package com.peatral.anisync;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static Context getContext() {
        return application.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}