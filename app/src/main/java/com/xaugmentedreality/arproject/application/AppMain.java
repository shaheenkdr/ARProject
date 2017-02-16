package com.xaugmentedreality.arproject.application;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.firebase.client.Firebase;
import com.liulishuo.filedownloader.FileDownloader;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;

public class AppMain extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        FileDownloader.init(getApplicationContext());

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

    }
}