package com.xaugmentedreality.arproject.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.adapter.VideoAdapter;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.realm.VideoDatabase;
import com.xaugmentedreality.arproject.utility.VideoModel;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MyVideoActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_video);
        Realm mVideoRealm = Realm.getInstance(
                new RealmConfiguration.Builder(this)
                        .name("videoDb.realm")
                        .build()
        );

        List<VideoModel> videoList = new ArrayList<>();

        RealmResults<VideoDatabase> results = mVideoRealm.where(VideoDatabase.class).findAll();

        for(VideoDatabase x:results)
        {
            videoList.add(new VideoModel(x.getVideoid(),x.getTitles(),x.getDesc()));
        }

        RecyclerView rView = (RecyclerView)findViewById(R.id.videoRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rView.setLayoutManager(llm);

        VideoAdapter mAdapter = new VideoAdapter(videoList);

        rView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }
}
