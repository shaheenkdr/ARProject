package com.xaugmentedreality.arproject.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.firebase.DataPojo;
import com.xaugmentedreality.arproject.firebase.Item;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.utility.DownLoadList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

public class SplashActivity extends AppCompatActivity {

    private Realm mRealm;
    private DataPojo mData;
    private List<DownLoadList> mdataCollection;
    private Intent mIntent;
    private Intent mIntentIntro;
    private boolean isFirstTime;
    private SmoothProgressBar sm;
    private boolean isDownloadEnabled = false;
    private int totalTaskCount = 0;
    private int finishedTaskCount = 0;


    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppThemeSecondary);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mRealm = Realm.getInstance(this);

        final String TEST ="";
        SharedPreferences pref = getSharedPreferences("OnBoardCheck", Context.MODE_PRIVATE);
        String check = pref.getString("HASH","");
        isFirstTime = check.equals(TEST);

        mIntent = new Intent(SplashActivity.this,CameraActivity.class);
        mIntentIntro = new Intent(SplashActivity.this,AppIntro.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntentIntro.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        sm = (SmoothProgressBar)findViewById(R.id.progress);
        sm.setBackgroundColor(getResources().getColor(R.color.black));
        mdataCollection = new ArrayList<>();

        listDatabase();
        checkConnectivity(getApplicationContext());
    }

    /**
     * Method to check whether the device is connected to the Internet
     * If connected initiate download process, other wise launch
     * the image recognition engine
     */
    private void checkConnectivity(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null)
        {
            retrieveData();
        }
        else
        {
            launchActivity();
        }

    }


    /**
     * method to retrieve the data from Firebase database
     * and pass the data to `insertToDatabase()` to be added to realm
     */
    private void retrieveData()
    {
        DatabaseReference fRef = FirebaseDatabase.getInstance().getReference();
        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sm.setVisibility(View.VISIBLE);
                mData = dataSnapshot.getValue(DataPojo.class);
                insertToDatabase();
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                launchActivity();
            }
        });
    }

    /**
     * method to insert records to realm database
     * if the record does not exist already
     */
    @SuppressWarnings("unused")
    private void insertToDatabase()
    {
        final List<Item> items = mData.getItems();
        Snackbar.make(this.findViewById(android.R.id.content), "Please wait while the app synchronizes", Snackbar.LENGTH_SHORT).show();
        for(Item x:items)
        {
            final Item itemx = x;
            Log.e("TAG-D",""+itemx.getTitle());
            try
            {
                if(mRealm.where(ARDatabase.class).equalTo("uid",itemx.getUid()).findFirst()==null && !itemx.getIsdeleted())
                {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            ARDatabase db = realm.createObject(ARDatabase.class);
                            db.setUid(itemx.getUid());
                            db.setNamex(itemx.getTitle());
                            db.setDesc(itemx.getDesc());
                            db.setIsVideo(itemx.getIsvideo());
                            db.setIsDeleted(itemx.getIsdeleted());
                            db.setUrlImg(itemx.getUrlImage());
                            db.setUrlApp(itemx.getUrlApp());
                            db.setUpdates(itemx.getUpdated());
                            db.setIsDownloaded(false);
                            db.setLocation("X");
                        }
                    });
                }
                else
                {
                    if(!itemx.getIsdeleted())
                    {
                        ARDatabase query = mRealm.where(ARDatabase.class).equalTo("uid",itemx.getUid()).findFirst();

                        if(itemx.getUpdated()>query.getUpdates())
                        {
                            networkUpdateDatabase(itemx);
                        }
                    }
                    else
                    {
                        File file = new File(String.valueOf(getExternalCacheDir())+"/"+itemx.getUid()+".jpg");
                        if(file.exists())
                        {
                            boolean res = deleteImage(String.valueOf(file));
                        }

                        deleteFromRealm(itemx.getUid());
                    }


                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        downloadImage();

    }

    /**
     * method to check if an existing record has been deleted and to delete
     * redundant files accordingly
     */
    private void deleteFromRealm(final String uid)
    {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ARDatabase> rows = realm.where(ARDatabase.class).equalTo("uid",uid).findAll();
                rows.clear();
            }
        });
    }

    /**
     * method to delete a specific image in cache
     * @param path : absolute file path to the image file
     * @return returns true if successful else false
     */
    private boolean deleteImage(String path)
    {
        File file = new File(path);
        return file.delete();
    }

    /**
     * method to create a list of URLS of images to be downloaded
     * and invoke the downloader once the list is complete
     */
    private void downloadImage()
    {
        RealmResults<ARDatabase> results = mRealm.where(ARDatabase.class).findAll();

        for(ARDatabase x:results)
        {

            if(!x.getIsDownloaded())
            {
                mdataCollection.add(new DownLoadList(x.getUrlImg(),x.getUid(),x.getNamex()));
            }
        }
        invokeDownloadManager();

    }

    /**
     * method to log the entire realm database
     */
    @SuppressWarnings("unused")
    private void listDatabase()
    {
        RealmResults<ARDatabase> result2 = mRealm.where(ARDatabase.class)
                .findAll();

        for(ARDatabase x:result2)
        {
            Log.e("TAG","NAME:"+x.getNamex()+"DOWNLOADED"+x.getIsDownloaded());
        }
    }

    /**
     * method to update the realm database when the
     * download of image is completed
     * @param uid the primary key
     * @param location_address location where image is stored in cache
     * @throws RealmException
     */
    private void downloadUpdateDatabase(final String uid,final String location_address) throws RealmException
    {

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ARDatabase db = realm.where(ARDatabase.class).equalTo("uid", uid).findFirst();
                db.setIsDownloaded(true);
                db.setLocation(location_address);
            }
        });

        if(isDownloadEnabled)
        {
            if(finishedTaskCount>=totalTaskCount)
            {
                launchActivity();
            }

        }

    }

    /**
     * method that updates the realm database when an update has been
     * made in the network
     * @param item : Item object
     */
    private void networkUpdateDatabase(final Item item)
    {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm)
            {
                ARDatabase db = realm.where(ARDatabase.class).equalTo("uid", item.getUid()).findFirst();
                db.setNamex(item.getTitle());
                db.setDesc(item.getDesc());
                db.setIsVideo(item.getIsvideo());
                db.setIsDeleted(item.getIsdeleted());
                db.setUrlImg(item.getUrlImage());
                db.setUrlApp(item.getUrlApp());
                db.setUpdates(item.getUpdated());
                db.setIsDownloaded(false);
                db.setLocation("X");
            }
        });

    }

    /**
     * method to download the images as a queue using download manager
     */
    private void invokeDownloadManager()
    {
        if(mdataCollection.size()!=0)
        {
            final FileDownloadListener queueTarget = new FileDownloadListener() {
                @Override
                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void blockComplete(BaseDownloadTask task) {
                }

                @Override
                protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                }

                @Override
                protected void completed(BaseDownloadTask task)
                {

                    ++finishedTaskCount;
                    try {
                        downloadUpdateDatabase(task.getFilename().replaceAll(".jpg", ""), String.valueOf(getExternalCacheDir()) + "/");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                }

                @Override
                protected void warn(BaseDownloadTask task) {
                }
            };

            File sd = getExternalCacheDir();
            final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(queueTarget);
            totalTaskCount = mdataCollection.size();

            final List<BaseDownloadTask> tasks = new ArrayList<>();
            for (DownLoadList x:mdataCollection)
            {
                tasks.add(FileDownloader.getImpl().create(x.getImageUrl()).setPath(String.valueOf(sd)+"/"+x.getUid()+".jpg").setTag(x.getUid()));
            }
            queueSet.disableCallbackProgressTimes();
            isDownloadEnabled = true;
            queueSet.downloadSequentially(tasks);
            queueSet.addTaskFinishListener(new BaseDownloadTask.FinishListener() {

                @Override
                public void over(BaseDownloadTask task)
                {

                }
            });
            queueSet.start();
        }
        else
        {
            launchActivity();
        }

    }

    /**
     * method to launch the next activity
     */
    private void launchActivity()
    {
        if(isFirstTime)
        {
            startActivity(mIntentIntro);
            finish();
        }
        else
        {
            startActivity(mIntent);
            finish();
        }

    }




}
