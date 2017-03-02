package com.xaugmentedreality.arproject.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.arlab.callbacks.ARmatcherImageCallBack;
import com.arlab.callbacks.ARmatcherQRCallBack;
import com.arlab.imagerecognition.ARmatcher;
import com.arlab.imagerecognition.ROI;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.realm.VideoDatabase;
import com.xaugmentedreality.arproject.utility.DeveloperKey;
import com.xaugmentedreality.arproject.utility.ImageQueueObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class CameraActivity extends AppCompatActivity implements ARmatcherImageCallBack, ARmatcherQRCallBack {
    private static final String API_KEY_MATCHER = "M3qjM+1zj4q4rDcYC9MOAPtGea/I8isWeQYhcvBCI/Y=";
    /** Called when the activity is first created. */

    /**The matcher instance */
    private ARmatcher aRmatcher;

    /**HashMap that holds added images IDs and its titles in the matching pool */
    private ArrayMap<Integer, ImageQueueObject> imageQueue;



    TextView textView = null;
    private int screenheight;
    private int screenwidth;
    private FrameLayout frame;
    private RelativeLayout grid;
    private RelativeLayout crop;
    private List<ImageQueueObject> mDataList;
    private Realm mRealm;
    private Realm mVideoRealm;
    private TextView title;
    private CardView card;
    private TextView descText;
    private Button clickButton;
    private ProgressDialog progressDialog;
    private RelativeLayout rlCard;
    private boolean isAutoPlayEnabled;

    private static final String PREFERENCES_NAME = "videopreferences";
    private static final String PREFERENCE_ID = "isAutoPlayEnabled";



    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Get full screen size */
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenheight = getWindowManager().getDefaultDisplay().getHeight();
        screenwidth = getWindowManager().getDefaultDisplay().getWidth();


        /** Set activity view*/
        setContentView(R.layout.activity_camera);
        imageQueue=new ArrayMap<>();
        frame = (FrameLayout)findViewById(R.id.frame);
        grid=(RelativeLayout)findViewById(R.id.grid);
        crop=(RelativeLayout)findViewById(R.id.crop);

        /** Initiate Realm db*/
        mRealm = Realm.getInstance(this);
        mDataList = new ArrayList<>();

        mVideoRealm = Realm.getInstance(
                new RealmConfiguration.Builder(this)
                        .name("videoDb.realm")
                        .build()
        );

        /**Create an instance of the ARmatcher object. */
        aRmatcher = new ARmatcher(this,API_KEY_MATCHER,ARmatcher.SCREEN_ORIENTATION_PORTRAIT,screenwidth,screenheight,true);

        aRmatcher.getTrueCameraViewWidth();
        aRmatcher.getTrueCameraViewHeight();

        /**Set image and QR matching callbacks */
        aRmatcher.setImageRecognitionCallback(this);
        aRmatcher.setQRRecognitionCallback(this);

        /**Add camera view instance to content view */
        frame.addView(aRmatcher.getCameraViewInstance());

        /**Set the matching type.*/
        aRmatcher.setMatchingType(ARmatcher.BOTH_IMAGE_AND_QR_MATCHER);

        /**Enable median filter ,witch help to reduce noise and mismatches in IMAGE matching .(Optional) */
        aRmatcher.enableMedianFilter(true);

        /**Set minimum image quality threshold, for image to be accepted in the image pool (0 - 10) */
        aRmatcher.setImageQuality(2);

        addView();

        generateImageQueue();

        /** bind overlay xml items to code*/
        title = (TextView)findViewById(R.id.titleText);
        card = (CardView)findViewById(R.id.card);
        descText = (TextView)findViewById(R.id.descText);
        clickButton = (Button)findViewById(R.id.launchButton);
        rlCard = (RelativeLayout)findViewById(R.id.relativeCard);

        /**Setting custom fonts for overlay*/
        Typeface typeface1 = Typeface.createFromAsset(getAssets(), "fonts/robotothin.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/robotobold.ttf");
        descText.setTypeface(typeface1);
        title.setTypeface(typeface2);



        beginAddImages();
    }

    /**
     * method to add overlay xml on top of the activity
     */
    @SuppressWarnings("all")
    private void addView()
    {
        LayoutInflater controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.overlay, null);
        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
    }

    /**
     * add images to the queue for recognition from a separate thread
     * using AsyncTask
     */
    private void beginAddImages() {
        AddImagesTask ait=new AddImagesTask();
        ait.execute((Void)null);

    }

    /**Activity lifecycle method */
    protected void onPause() {
        super.onPause();
        /**Stop matching*/
        aRmatcher.stop();

    }

    /**Activity lifecycle method */
    protected void onResume() {
        super.onResume();
        progressDialog=ProgressDialog.show(CameraActivity.this, "Loading", "Setting up the engine");
        aRmatcher.start();
        beginAddImages();
        SharedPreferences pref = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        isAutoPlayEnabled = pref.getBoolean(PREFERENCE_ID,false);

    }

    /**Activity lifecycle method */
    protected void onStop() {
        super.onStop();
        try {
            /**Empty image matching pool*/
            aRmatcher.releaseResources();

        } catch (Exception e) {	e.printStackTrace(); }
        System.gc();
    }

    /**Activity lifecycle method */
    protected void onDestroy()
    {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    protected void setCroppingArea() {
        /**Select cropping area to be analyzed */
        /**
         * Take in consideration that the cropping area is defined on landscape and portrait modes differently,
         * In Portrait Mode the (0,0) point will be the upper left corner holding the device portrait ,
         *  In Landscape Mode the (0,0) point will be the upper left corner holding the device landscape left (upper right in portrait)
         *
         */

        /**Set cropping area in TABLET or DEVICES where Camera Preview is NOT set to full screen */
        /**
         * Here we use camerawidth and cameraweight because of the camera preview that is not
         * in a full screen of the device, so we need to know
         * the exact size to not exceed the camera view borders.
         *
         * the screenwidth and screenheight used for drawing the red rectangel marker
         *
         */
    	/*aRmatcher.setImageCropRect(camerawidth/2, 0, camerawidth, cameraheight/2);

    	RelativeLayout.LayoutParams p=new RelativeLayout.LayoutParams(screenwidth/2, screenheight/2);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.addRule(RelativeLayout.ALIGN_PARENT_TOP);*/



        /**Set cropping area in PHONES or Devices with camera preview set to full screen */
        /**
         * Here we use screenwidth and screenwidth for both cropping and drawing the marker rectangle
         * becasue of the full video preview
         *
         *
         */
        aRmatcher.setImageCropRect(screenwidth/2, 0, screenwidth, screenheight/2);

        RelativeLayout.LayoutParams p=new RelativeLayout.LayoutParams(screenwidth/2, screenheight/2);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.addRule(RelativeLayout.ALIGN_PARENT_TOP);



        crop.setLayoutParams(p);
        crop.setVisibility(View.VISIBLE);
    }

    protected void setROIs() {
        /**Add 4 ROIs */
        /**
         * We define 4 regions of interest on the mobile screen to match simultaneous QR codes.
         */
        /**
         * Take in consideration that the cropping area is defined on landscape and portrait modes differently,
         * In Portrait Mode the (0,0) point will be the upper left corner holding the device portrait ,
         *  In Landscape Mode the (0,0) point will be the upper left corner holding the device landscape left (upper right in portrait)
         *
         */
        //ROI1
        ROI r = new ROI(0, 0, screenwidth/2, screenheight/2);
        aRmatcher.addRoi(r);
        //ROI2
        r = new ROI(0,screenheight/2, screenwidth/2, screenheight/2);
        aRmatcher.addRoi(r);
        //ROI3
        r = new ROI(screenwidth/2,0,screenwidth/2, screenheight/2);
        aRmatcher.addRoi(r);
        //ROI4
        r = new ROI(screenwidth/2, screenheight/2,screenwidth/2, screenheight/2);
        aRmatcher.addRoi(r);
        grid.setVisibility(View.VISIBLE);
    }

    private int addLargeImageWithId(int resource, String title, int id, int maxSize) {

        /**Add LARGE (High resolution) image with unique ID*/

        /**
         * The API provides a helper function to reduce the image size, to avoid memory allocation errors.
         */
        Bitmap b = aRmatcher.decodeScaledImageFromResource(resource, maxSize);

        if(aRmatcher.addImage(b,id)){

            b.recycle();

            Log.i("TAG","image added to the pool with id: " + id);
        }else{
            Log.i("TAG","image not added to the pool");
        }

        return id;
    }

    private int addImageFromURL(String url, String title) {
        /**Add image from URL */
        /**It is possible to load remote images to the image pool by setting a valid URL */

        int imagePool_Id = aRmatcher.addImage(url);
        if(imagePool_Id != -1){
            //imageTitles.put(imagePool_Id,title);
            Log.i("TAG","image added to the pool with id: " + imagePool_Id);
        }else{
            Log.i("TAG","image not added to the pool");
        }

        return imagePool_Id;
    }



    /**
     * method to fetch the objects from realm and add to the data
     * collection to be added to the Image loader of the
     * Augmented reality engine
     */
    private void generateImageQueue()
    {
        RealmResults<ARDatabase> results = mRealm.where(ARDatabase.class).findAll();

        for(ARDatabase x:results)
        {
            Log.e("TAGZ","DOWNLOAD STATUS"+x.getIsDownloaded()+" UID:"+x.getUid());

            if(x.getIsDownloaded())
            {
                ImageQueueObject q1 = new ImageQueueObject();
                q1.setUid(x.getUid());
                q1.setNamex(x.getNamex());
                q1.setDesc(x.getDesc());
                q1.setIsVideo(x.getIsVideo());
                q1.setIsDeleted(x.getIsDeleted());
                q1.setUrlImg(x.getUrlImg());
                q1.setUrlApp(x.getUrlApp());
                q1.setUpdates(x.getUpdates());
                q1.setIsDownloaded(x.getIsDownloaded());
                q1.setLocation(x.getLocation());
                mDataList.add(q1);

            }
        }
    }


    private synchronized int addImageFromResources(ImageQueueObject db) {
        /**Add images from local resources to image matching pool
         * Adding image returns image id assigned to the image in the matching pool.
         * We will save it to know which image was matched
         *
         * (The best practice is to make image adding in another thread to avoid the system to get stuck)
         *
         * IMORTANT !!! Decoding large images to Bitmap may result in insufficient memory allocations and application crashing,
         * therefore large images must be reduced/scaled.
         */

        File sd = getExternalCacheDir();
        File f1 = new File(String.valueOf(sd)+"/"+db.getUid()+".jpg");
        Bitmap bmp = BitmapFactory.decodeFile(f1.getAbsolutePath());
        int imagePool_Id;

        imagePool_Id = aRmatcher.addImage(bmp);

        if(imagePool_Id != -1){
            imageQueue.put(imagePool_Id,db);

            Log.e("TAGZ","image added to the pool with id: " + db.getUid());
        }else
        {
            Log.e("TAGZ","image not added to the pool"+ db.getUid());
        }

        bmp.recycle();

        return imagePool_Id;
    }

    private int addImageDataFromPath(String path,String title)
    {
        int imagePool_Id ;

        imagePool_Id = aRmatcher.addImageFromData(path);

        if(imagePool_Id != -1){
            //imageTitles.put(imagePool_Id,title);

            Log.i("TAG","image added to the pool with id: " + imagePool_Id);
        }else{
            Log.i("TAG","image not added to the pool");
        }

        return imagePool_Id;
    }

    @SuppressWarnings("unused")
    private int addImageDataFromUrl(String url,String title)
    {
        int imagePool_Id ;

        imagePool_Id = aRmatcher.addImageFromDataThroughUrl(url);

        if(imagePool_Id != -1){
            //imageTitles.put(imagePool_Id,title);
            Log.i("TAG","image added to the pool with id: " + imagePool_Id);
        }else{
            Log.i("TAG","image not added to the pool");
        }
        return imagePool_Id;
    }


    /**Callback that will accept all IMAGE matching results */
    @Override
    public void onImageRecognitionResult(int result)
    {
        Log.e("TAG","IMAGE DETECTED::RESULT: "+result);


        if (result != -1)
        {
            Log.e("TAG","NOT NULL:: UID: "+imageQueue.get(result));
            final ImageQueueObject db = imageQueue.get(result);
            title.setText(db.getNamex());
            descText.setText(db.getDesc());
            rlCard.setBackgroundResource(db.getIsVideo()?R.drawable.forth_bg:R.drawable.thrid_bg);
            clickButton.setBackgroundResource(db.getIsVideo()?R.drawable.round_button_video:R.drawable.round_button_image);
            card.setVisibility(View.VISIBLE);
            clickButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(db.getIsVideo())
                    {
                        insertVideoRealm(db.getUrlApp(),db.getNamex(),db.getDesc());
                        Intent intent = YouTubeStandalonePlayer.createVideoIntent(CameraActivity.this, DeveloperKey.DEVELOPER_KEY, db.getUrlApp(),0,isAutoPlayEnabled,false);
                        Bundle extras = new Bundle();
                        extras.putString("YOUTUBE_VIDEO", db.getUrlApp());
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                    else
                    {
                        launchUrl(db.getUrlApp());
                    }
                }
            });


        }
        else
        {
            card.setVisibility(View.INVISIBLE);
        }
    }

    /**method to launch web pages */
    private void launchUrl(String url)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    /**Callback that will accept all QR codes matching results */
    @Override
    public void onSingleQRrecognitionResult(String result) {
        if(!result.equals(""))
        {
            Toast.makeText(getApplication(), result, Toast.LENGTH_SHORT).show();
        }
    }

    /**Callback that will accept all QR codes matching results */
    @Override
    @SuppressWarnings("UnusedDeclaration")
    public void onMultipleQRrecognitionResult(ArrayList<ROI> roiList) {
        String output = "";
        for(int i=0; i<roiList.size(); i++){
            if(roiList.get(i).foundResult != null)
                output += roiList.get(i).foundResult + "\n";
        }

    }




    /**
     * Through the AsyncTask the images are added to the library.
     * @author OBX
     */
    private class AddImagesTask extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... params)
        {
            /** Add image from local resources **/
            for (ImageQueueObject x:mDataList)
            {
                addImageFromResources(x);

            }
            return true;
        }

        /**
         * Dismiss the ProgressView.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            /**Start matching*/
            aRmatcher.start();
        }
    }

    public void openSettings(View view)
    {
        startActivity(new Intent(CameraActivity.this,SettingsActivity.class));
    }

    private void insertVideoRealm(final String id, final String title,  final String desc)
    {
        if(mVideoRealm.where(VideoDatabase.class).equalTo("videoid",id).findFirst()==null)
        {
            mVideoRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    VideoDatabase db = realm.createObject(VideoDatabase.class);
                    db.setVideoid(id);
                    db.setTitles(title);
                    db.setDesc(desc);
                }
            });
        }
    }

}