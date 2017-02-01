package com.xaugmentedreality.arproject.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.xaugmentedreality.arproject.R;

public class AppIntro extends AppIntro2
{



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSkipButton(false);
        showStatusBar(false);
        addSlide(AppIntroFragment.newInstance("Hi", "Welcome to CamTube. Follow the instructions to understand the usage", R.drawable.onboardi, Color.parseColor("#FFB300")));
        addSlide(AppIntroFragment.newInstance("Focus", "Place the CamTube camera under an AR enabled Ad and wait for the app to detect", R.drawable.scanx, Color.parseColor("#4CAF50")));
        addSlide(AppIntroFragment.newInstance("Detect", "An AR enabled overlay is displayed when an image is detected. Clicking on it provides more details.", R.drawable.star, Color.parseColor("#F44336")));
        addSlide(AppIntroFragment.newInstance("Done!", "You are all done and good to go!", R.drawable.medalx, Color.parseColor("#1976D2")));

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment)
    {
        super.onDonePressed(currentFragment);
        SharedPreferences pref = getSharedPreferences("OnBoardCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putString("HASH", "123456789");
        ed.apply();
        Intent mIntent = new Intent(AppIntro.this,CameraActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
    }



}
