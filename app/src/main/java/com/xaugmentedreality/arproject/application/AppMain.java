package com.xaugmentedreality.arproject.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import com.liulishuo.filedownloader.FileDownloader;


public class AppMain extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileDownloader.init(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}