package com.xaugmentedreality.arproject.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xaugmentedreality.arproject.R;


public class SettingsActivity extends AppCompatActivity {

    private RelativeLayout relativeLayout;
    private CheckBox autoPlay;
    private SharedPreferences pref;

    private static final String PREFERENCES_NAME = "videopreferences";
    private static final String PREFERENCE_ID = "isAutoPlayEnabled";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        setContentView(R.layout.activity_settings);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/robotomedium.ttf");

        TextView aboutText = (TextView)findViewById(R.id.aboutText);
        TextView rateText = (TextView)findViewById(R.id.rateText);
        TextView feedbackText = (TextView)findViewById(R.id.feedbackText);
        TextView shareText = (TextView)findViewById(R.id.shareText);
        TextView videosText = (TextView)findViewById(R.id.videosText);
        TextView autoplayText = (TextView)findViewById(R.id.autoPlayText);

        aboutText.setTypeface(typeface);
        rateText.setTypeface(typeface);
        feedbackText.setTypeface(typeface);
        shareText.setTypeface(typeface);
        videosText.setTypeface(typeface);
        autoplayText.setTypeface(typeface);

        relativeLayout = (RelativeLayout)findViewById(R.id.activity_settings);
        autoPlay = (CheckBox)findViewById(R.id.autoPlayBox);
        pref = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(pref.getBoolean(PREFERENCE_ID,false))
        {
            autoPlay.setChecked(true);
        }
        autoPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    autoPlay.setChecked(true);

                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean(PREFERENCE_ID,true);
                    ed.apply();

                    Snackbar.make(relativeLayout, "Autoplay enabled for videos", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    autoPlay.setChecked(false);

                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean(PREFERENCE_ID,false);
                    ed.apply();

                    Snackbar.make(relativeLayout, "Autoplay disabled for videos", Snackbar.LENGTH_LONG).show();
                }
            }
        });

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

    public void rateApp(View view)
    {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public void shareApp(View view)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey check out this cool Augmented reality app at: https://play.google.com/store/apps/details?id="+getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void autoplay(View view)
    {
        if(autoPlay.isChecked())
        {
            Snackbar.make(relativeLayout, "Autoplay disabled for videos", Snackbar.LENGTH_LONG).show();
            autoPlay.setChecked(false);

            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean(PREFERENCE_ID,false);
            ed.apply();
        }

        else
        {
            Snackbar.make(relativeLayout, "Autoplay enabled for videos", Snackbar.LENGTH_LONG).show();
            autoPlay.setChecked(true);

            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean(PREFERENCE_ID,true);
            ed.apply();
        }
    }

    /**
     * method to open the email app using intent
     * to contact the app developer teams
     * @param view view object
     */
    public void emailLauncher(View view)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"picgingermail@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "pic-ginger enquiry");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * method to launch the about app activity
     * @param view view object
     */
    public void aboutLauncher(View view)
    {
        startActivity(new Intent(SettingsActivity.this,AboutActivity.class));
    }

    public void openVideos(View view)
    {
        startActivity(new Intent(SettingsActivity.this,MyVideoActivity.class));
    }
}
