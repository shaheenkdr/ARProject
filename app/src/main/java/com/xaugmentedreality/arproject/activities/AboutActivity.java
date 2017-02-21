package com.xaugmentedreality.arproject.activities;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xaugmentedreality.arproject.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Typeface typeface1 = Typeface.createFromAsset(getAssets(), "fonts/robotothin.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/robotobold.ttf");

        TextView appName = (TextView)findViewById(R.id.appNameAbout);
        appName.setTypeface(typeface2);

        TextView version = (TextView)findViewById(R.id.versionCode);
        version.setTypeface(typeface1);

        TextView copyright = (TextView)findViewById(R.id.copyRightText);
        copyright.setTypeface(typeface1);

        TextView reserved = (TextView)findViewById(R.id.reservedText);
        reserved.setTypeface(typeface1);

    }

    public void endActivity(View view)
    {
        finish();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }
}
