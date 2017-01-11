package com.xaugmentedreality.arproject.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.firebase.DataPojo;
import com.xaugmentedreality.arproject.firebase.Item;
import com.xaugmentedreality.arproject.realm.ARDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

public class SplashActivity extends AppCompatActivity {

    private Realm mRealm;
    private DataPojo mData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_splash);
        mRealm = Realm.getInstance(this);
        Button b1 = (Button)findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(SplashActivity.this,CameraActivity.class);
                startActivity(x);
                finish();
            }
        });
        updateDatabase("AR11","PEEP PEEP");
        //retrieveData();
        listDatabase();


    }


    /**
     * method to retrieve the data from firebase database
     */
    private void retrieveData()
    {
        Firebase fRef = new Firebase("https://project-ar-312a4.firebaseio.com/");
        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mData = dataSnapshot.getValue(DataPojo.class);
                insertToDatabase();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("data error", firebaseError.getMessage());

            }
        });
    }

    /**
     * method to insert values to realm database
     */
    private void insertToDatabase()
    {

        final List<Item> items = mData.getItems();
        for(Item x:items)
        {
            final Item itemx = x;
            try {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ARDatabase db = realm.createObject(ARDatabase.class);
                        db.setUid(itemx.getUid());
                        db.setNamex(itemx.getTitle());
                        db.setDesc(itemx.getDesc());
                        db.setIsVideo(itemx.getIsvideo());
                        db.setUrlImg(itemx.getUrlImage());
                        db.setUrlApp(itemx.getUrlApp());
                        db.setIsDownloaded(false);
                        db.setLocation("X");
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("TAG","EXCEPTION"+e);
            }

        }

    }

    /**
     * method to invoke the download of multiple images as a queue
     */
    private void downloadImage()
    {
        RealmResults<ARDatabase> results = mRealm.where(ARDatabase.class).findAll();

        for(ARDatabase x:results)
        {
            Picasso.with(SplashActivity.this).load(x.getUrlImg()).into(target);
        }
    }

    /**
     * method to log the entire realm database
     */
    private void listDatabase()
    {
        RealmResults<ARDatabase> result2 = mRealm.where(ARDatabase.class)
                .findAll();

        for(ARDatabase x:result2)
        {
            Log.e("TAG",""+x.getLocation());
        }
    }

    /**
     * method to update an existing object in a realm database
     * @param uid the primary key
     * @param location_address location where image is stored in cache
     * @throws RealmException
     */
    private void updateDatabase(String uid,String location_address) throws RealmException
    {
        mRealm.beginTransaction();
        ARDatabase db = new ARDatabase();
        db.setUid(uid);
        db.setIsDownloaded(true);
        db.setLocation(location_address);
        mRealm.copyToRealmOrUpdate(db);
        mRealm.commitTransaction();

    }


    /**====================================================================================
     * Beginning of Picasso block to download the images
     */


    @SuppressWarnings("all")


    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    File sd = getExternalCacheDir();
                    File folder = new File(sd, "/mobio/");
                    if (!folder.exists()) {
                        if (!folder.mkdir()) {
                            Log.e("ERROR", "Cannot create a directory!");
                        } else {
                            folder.mkdirs();
                        }
                    }

                    //File[] fileName = {new File(folder, "one.jpg"), new File(folder, "two.jpg")};

                    RealmResults<ARDatabase> result2 = mRealm.where(ARDatabase.class).findAll();

                    for (ARDatabase i:result2)
                    {
                        if(!i.getIsDownloaded())
                        {
                            File fileName = new File(folder,i.getUid().toLowerCase()+".jpg");


                            if (!fileName.exists()) {
                                try {
                                    fileName.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {

                                try {
                                    FileOutputStream outputStream = new FileOutputStream(String.valueOf(fileName));
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                    outputStream.close();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    }


                }
            }).start();

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

}
