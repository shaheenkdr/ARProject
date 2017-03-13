package com.xaugmentedreality.arproject.activities;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xaugmentedreality.arproject.EventBus.DeleteEvent;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.adapter.VideoAdapter;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.realm.VideoDatabase;
import com.xaugmentedreality.arproject.utility.VideoModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MyVideoActivity extends AppCompatActivity {

    private static int remove_index;
    private static StringBuilder strVideoId;
    private AppCompatImageButton deleteButton;
    private Realm mVideoRealm;
    private TextView titleBar;
    private ImageButton clearButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        setContentView(R.layout.activity_my_video);
        mVideoRealm = Realm.getInstance(
                new RealmConfiguration.Builder(this)
                        .name("videoDb.realm")
                        .build()
        );

        remove_index = -1;
        strVideoId = new StringBuilder("X");

        final List<VideoModel> videoList = new ArrayList<>();

        RealmResults<VideoDatabase> results = mVideoRealm.where(VideoDatabase.class).findAll();

        for(VideoDatabase x:results)
        {
            videoList.add(new VideoModel(x.getVideoid(),x.getTitles(),x.getDesc()));
        }

        RecyclerView rView = (RecyclerView)findViewById(R.id.videoRecyclerView);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rView.setLayoutManager(llm);

        final VideoAdapter mAdapter = new VideoAdapter(videoList);

        rView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        titleBar = (TextView)findViewById(R.id.titleBar);

        deleteButton = (AppCompatImageButton) getSupportActionBar().getCustomView().findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(remove_index!=-1)
                {
                    videoList.remove(remove_index);
                    mAdapter.notifyDataSetChanged();
                    if(!strVideoId.toString().equals("X"))
                    {
                        deleteFromRealm(strVideoId.toString());
                    }
                }

                deleteButton.setVisibility(View.INVISIBLE);
            }
        });

  /*      clearButton = (ImageButton)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.INVISIBLE);
                clearButton.setVisibility(View.INVISIBLE);
                titleBar.setVisibility(View.VISIBLE);

            }
        });*/

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteEvent event)
    {
        Log.e("Test","EventBus invoked");
        titleBar.setVisibility(View.INVISIBLE);
        //clearButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        remove_index = event.getId();
        strVideoId.setLength(0);
        strVideoId.append(event.getVideoId());
    }

    private void deleteFromRealm(final String videoid)
    {
        mVideoRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<VideoDatabase> rows = realm.where(VideoDatabase.class).equalTo("videoid",videoid).findAll();
                rows.clear();
            }
        });
    }
}
