package com.xaugmentedreality.arproject.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import com.liulishuo.filedownloader.FileDownloader;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.realm.ARDatabase;
import com.xaugmentedreality.arproject.utility.DeveloperKey;
import com.xaugmentedreality.arproject.utility.DownLoadList;
import com.xaugmentedreality.arproject.utility.ImageQueueObject;

import io.realm.Realm;
import io.realm.RealmResults;
//

public class CameraActivity extends AppCompatActivity implements ARmatcherImageCallBack, ARmatcherQRCallBack {
    private static final String API_KEY_MATCHER = "M3qjM+1zj4q4rDcYC9MOAPtGea/I8isWeQYhcvBCI/Y=";
    /** Called when the activity is first created. */

    /**The matcher instance */
    private ARmatcher aRmatcher;

    /**HashMap that holds added images IDs and its titles in the matching pool */
    private HashMap<Integer, ImageQueueObject> imageQueue;



    private List<ImageQueueObject> mdataList;
    private Realm mRealm;
    TextView textView = null;
    private int screenheight;
    private int screenwidth;
    private FrameLayout frame;
    private RelativeLayout grid;
    private RelativeLayout crop;
    private LayoutInflater controlInflater = null;
    private TextView title;
    private CardView card;
    private RelativeLayout rl;
    private TextView descText;
    private Button clickButton;
    private ProgressDialog progressDialog;


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
        imageQueue=new HashMap<>();
        frame=(FrameLayout)findViewById(R.id.frame);
        grid=(RelativeLayout)findViewById(R.id.grid);
        crop=(RelativeLayout)findViewById(R.id.crop);


        mRealm = Realm.getInstance(this);
        mdataList = new ArrayList<>();
        generateImageQueue();

        /**Create an instance of the ARmatcher object. */
        aRmatcher = new ARmatcher(this,API_KEY_MATCHER,ARmatcher.SCREEN_ORIENTATION_PORTRAIT,screenwidth,screenheight,true);

        aRmatcher.getTrueCameraViewWidth();
        aRmatcher.getTrueCameraViewHeight();

        /**Set image and QR matching callbacks */
        aRmatcher.setImageRecognitionCallback(this);
        //aRmatcher.setQRRecognitionCallback(this);

        /**Add camera view instance to content view */
        frame.addView(aRmatcher.getCameraViewInstance());

        /**Set the matching type.*/
        aRmatcher.setMatchingType(ARmatcher.IMAGE_MATCHER);

        /**Enable median filter ,witch help to reduce noise and mismatches in IMAGE matching .(Optional) */
        aRmatcher.enableMedianFilter(true);

        /**Set minimum image quality threshold, for image to be accepted in the image pool (0 - 10) */
        aRmatcher.setImageQuality(2);

        addView();

        title = (TextView)findViewById(R.id.titleText);
        card = (CardView)findViewById(R.id.card);
        // rl = (RelativeLayout)findViewById(R.id.relative);
        descText = (TextView)findViewById(R.id.descText);
        clickButton = (Button)findViewById(R.id.launchButton);

        /**Add TextView to the view in order to show matching results. */
        addResultTextView();

        beginAddImages();
    }

    private void addView()
    {
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.overlay, null);
        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
    }

    private void beginAddImages() {
        AddImagesTask ait=new AddImagesTask();
        ait.execute((Void)null);

    }

    protected void onPause() {
        super.onPause();
        /**Stop matching*/
        aRmatcher.stop();

    }

    protected void onResume() {
        super.onResume();
        progressDialog=ProgressDialog.show(CameraActivity.this, "Loading", "Setting up the engine");
        aRmatcher.start();
        beginAddImages();
        //test push
    }

    protected void onStop() {
        super.onStop();
        try {
            /**Empty image matching pool*/
            aRmatcher.releaseResources();

        } catch (Exception e) {	e.printStackTrace(); }
        System.gc();
    }

    protected void onDestroy()
    {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
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
                mdataList.add(q1);
            }
        }
    }


    private int addImageFromResources(ImageQueueObject db) {
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

            Log.e("TAG","image added to the pool with id: " + imagePool_Id);
        }else{
            Log.e("TAG","image not added to the pool");
        }

        bmp.recycle();

        return imagePool_Id;
    }




    /**Add TextView to the screen to show the matching results. */
    private void addResultTextView()
    {
        textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(23);
        FrameLayout.LayoutParams frame = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

        addContentView(textView,frame);
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
            clickButton.setText(db.getIsVideo()?"video":"Web");
            card.setCardBackgroundColor(db.getIsVideo()?getResources().getColor(R.color.colorAccent):getResources().getColor(R.color.colorPrimary));
            card.setVisibility(View.VISIBLE);
            clickButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(db.getIsVideo())
                    {
                        Intent intent = YouTubeStandalonePlayer.createVideoIntent(CameraActivity.this, DeveloperKey.DEVELOPER_KEY, db.getUrlApp());
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
            textView.setText(R.string.nothing);
        }
    }

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
            textView.setText(result);
            Toast.makeText(getApplication(), result, Toast.LENGTH_SHORT).show();
        }
        else
        {
            textView.setText(R.string.nothing);
        }
    }

    /**Callback that will accept all QR codes matching results */
    @Override
    public void onMultipleQRrecognitionResult(ArrayList<ROI> roiList) {
        String output = "";
        int i = 0;
        for(i=0; i<roiList.size(); i++){
            if(roiList.get(i).foundResult != null)
                output += roiList.get(i).foundResult + "\n";
        }
        textView.setText(output);
        if(output.equals(""))
        {
            textView.setText(R.string.nothing);
        }
    }




    /**
     * Through the AsyncTask the images are added to the library.
     * @author ARLab
     *
     */
    private class AddImagesTask extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Adding Images
            /** Add image from local resources **/
            for (ImageQueueObject x:mdataList)
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

}