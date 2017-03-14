package com.xaugmentedreality.arproject.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xaugmentedreality.arproject.EventBus.DeleteEvent;
import com.xaugmentedreality.arproject.EventBus.ResetClickEvent;
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
    private static boolean isLongPressed = false;
    private Vibrator v;
    private AppCompatImageView noVideoImage;
    private TextView noVideoText;

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

        noVideoImage = (AppCompatImageView)findViewById(R.id.noImage);
        noVideoText = (TextView)findViewById(R.id.noImageText);
        Typeface typeface1 = Typeface.createFromAsset(getAssets(), "fonts/robotothin.ttf");
        noVideoText.setTypeface(typeface1);

        noVideoImage.setVisibility(View.INVISIBLE);
        noVideoText.setVisibility(View.INVISIBLE);


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

        if(videoList.size()<=0)
        {
            noVideoImage.setVisibility(View.VISIBLE);
            noVideoText.setVisibility(View.VISIBLE);
        }

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


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
                    isLongPressed = false;
                    if(!strVideoId.toString().equals("X"))
                    {
                        deleteFromRealm(strVideoId.toString());
                    }
                    if(videoList.size()<=0)
                    {
                        noVideoImage.setVisibility(View.VISIBLE);
                        noVideoText.setVisibility(View.VISIBLE);
                    }
                }

                titleBar.setVisibility(View.VISIBLE);
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

        RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.activity_my_video);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isLongPressed)
                {
                    resetLongClick();
                }
            }
        });

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
                handleBackClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteEvent event)
    {
        isLongPressed = true;
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

    private void resetLongClick()
    {
        v.vibrate(100);
        titleBar.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);
        isLongPressed = false;
        EventBus.getDefault().post(new ResetClickEvent(true));


    }

    private void handleBackClick()
    {
        if(isLongPressed)
        {
            resetLongClick();
        }
        else
        {
            finish();
        }

    }
}
