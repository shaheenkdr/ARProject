package com.xaugmentedreality.arproject.application;

import android.app.Application;

import com.firebase.client.Firebase;
import com.liulishuo.filedownloader.FileDownloader;

import io.realm.Realm;

public class AppMain extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        FileDownloader.init(getApplicationContext());

    }
}