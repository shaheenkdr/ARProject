package com.xaugmentedreality.arproject.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.wang.avi.AVLoadingIndicatorView;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.firebase.DataPojo;
import com.xaugmentedreality.arproject.firebase.Item;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.utility.DownLoadList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

public class SplashActivity extends AppCompatActivity {

    private Realm mRealm;
    private DataPojo mData;
    private List<DownLoadList> mdataCollection;
    private Intent mIntent;
    private AVLoadingIndicatorView avi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_splash);
        mRealm = Realm.getInstance(this);
        mIntent = new Intent(SplashActivity.this,CameraActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mdataCollection = new ArrayList<>();
        avi= (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.show();
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
            Log.e("TAG","HAS INTERNET CONNECTION");
            retrieveData();
        }
        else
        {
            Log.e("TAG","NO INTERNET CONNECTION");
            avi.hide();
            startActivity(mIntent);
            finish();
        }

    }


    /**
     * method to retrieve the data from Firebase database
     * and pass the data to `insertToDatabase()` to be added to realm
     */
    private void retrieveData()
    {
        Firebase fRef = new Firebase("https://project-ar-312a4.firebaseio.com/");
        fRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.e("TAG","FIREBASE DATA RETRIEVED");
                mData = dataSnapshot.getValue(DataPojo.class);
                insertToDatabase();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("TAG","FIREBASE RETRIEVAL FAILED");
                Log.e("data error", firebaseError.getMessage());
                avi.hide();
                startActivity(mIntent);
                finish();

            }
        });
    }

    /**
     * method to insert records to realm database
     * if the record already does not exist
     */
    private void insertToDatabase()
    {
        final List<Item> items = mData.getItems();
        for(Item x:items)
        {
            final Item itemx = x;
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
                            Log.e("THIN",""+db.getUpdates());
                            db.setIsDownloaded(false);
                            db.setLocation("X");
                        }
                    });
                }
                else
                {
                    if(!itemx.getIsdeleted())
                    {
                        Log.e("TAGX","RECORD EXISTS");

                        ARDatabase query = mRealm.where(ARDatabase.class).equalTo("uid",itemx.getUid()).findFirst();

                        if(itemx.getUpdated()>query.getUpdates())
                        {
                            Log.e("TAGX","RECORD UPDATE DETECTED");
                            networkUpdateDatabase(itemx);
                        }
                    }
                    else
                    {
                        Log.e("TAG","DELETED ITEM DETECTED");
                        File file = new File(String.valueOf(getExternalCacheDir())+"/"+itemx.getUid()+".jpg");
                        if(file.exists())
                        {
                            boolean res = deleteImage(String.valueOf(file));
                            Log.e("TAG","DELETION STATUS:"+res);
                        }
                        else
                        {
                            Log.e("TAG","FILE DOESN'T EXIST");
                        }
                        deleteFromRealm(itemx.getUid());
                    }


                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("TAG","EXCEPTION"+e);
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
        Log.e("TAG","DOWNLOAD UPDATE COMPLETED");

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
            public void execute(Realm realm) {
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
                    try {
                        downloadUpdateDatabase(task.getFilename().replaceAll(".jpg", ""), String.valueOf(getExternalCacheDir()) + "/");
                    }
                    catch (Exception e){ e.printStackTrace(); Log.e("TAG","UPDATE CRASH");}
                    Log.e("TAG","NAME:"+task.getFilename().replaceAll(".jpg","")+" ID: "+task.getId());
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
            final int taskCount = mdataCollection.size()-1;

            final List<BaseDownloadTask> tasks = new ArrayList<>();
            for (DownLoadList x:mdataCollection)
            {
                tasks.add(FileDownloader.getImpl().create(x.getImageUrl()).setPath(String.valueOf(sd)+"/"+x.getUid()+".jpg").setTag(x.getUid()));
            }
            queueSet.disableCallbackProgressTimes();
            queueSet.downloadSequentially(tasks);
            queueSet.addTaskFinishListener(new BaseDownloadTask.FinishListener() {
                int finishedTaskCount = 0;
                @Override
                public void over(BaseDownloadTask task)
                {
                    Log.e("TAGX","taskCount:"+taskCount+"finishedtaskcount:"+finishedTaskCount);
                    if (finishedTaskCount < taskCount)
                    {
                        ++finishedTaskCount;
                    }
                    else
                    {
                        avi.hide();
                        startActivity(mIntent);
                        finish();
                    }
                }
            });
            queueSet.start();
        }
        else
        {
            avi.hide();
            startActivity(mIntent);
        }

    }




}
