package com.xaugmentedreality.arproject.application;

import android.app.Application;

import com.firebase.client.Firebase;

public class AppMain extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

    }
}