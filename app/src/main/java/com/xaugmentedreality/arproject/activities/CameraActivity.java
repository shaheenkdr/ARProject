package com.xaugmentedreality.arproject.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.app.ProgressDialog;
import android.content.Intent;
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
import com.xaugmentedreality.arproject.utility.DeveloperKey;
import com.xaugmentedreality.arproject.utility.ImageQueueObject;

import io.realm.Realm;
import io.realm.RealmResults;

public class CameraActivity extends AppCompatActivity implements ARmatcherImageCallBack, ARmatcherQRCallBack {
    private static final String API_KEY_MATCHER = "M3qjM+1zj4q4rDcYC9MOAPtGea/I8isWeQYhcvBCI/Y=";
    /** Called when the activity is first created. */

    /**The matcher instance */
    private ARmatcher aRmatcher;

    /**HashMap that holds added images IDs and its titles in the matching pool */
    private ArrayMap<Integer, ImageQueueObject> imageQueue;



    private List<ImageQueueObject> mDataList;
    private Realm mRealm;
    private TextView title;
    private CardView card;
    private TextView descText;
    private Button clickButton;
    private ProgressDialog progressDialog;
    private RelativeLayout rlCard;



    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Get full screen size */
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenheight = getWindowManager().getDefaultDisplay().getHeight();
        int screenwidth = getWindowManager().getDefaultDisplay().getWidth();


        /** Set activity view*/
        setContentView(R.layout.activity_camera);
        imageQueue=new ArrayMap<>();
        FrameLayout frame = (FrameLayout)findViewById(R.id.frame);

        /** Initiate Realm db*/
        mRealm = Realm.getInstance(this);
        mDataList = new ArrayList<>();
        generateImageQueue();

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
        aRmatcher.setMatchingType(ARmatcher.IMAGE_MATCHER);

        /**Enable median filter ,witch help to reduce noise and mismatches in IMAGE matching .(Optional) */
        aRmatcher.enableMedianFilter(true);

        /**Set minimum image quality threshold, for image to be accepted in the image pool (0 - 10) */
        aRmatcher.setImageQuality(2);

        addView();

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

        TextView aboutText = (TextView)findViewById(R.id.aboutText);
        TextView contactText = (TextView)findViewById(R.id.contactText);
        aboutText.setTypeface(typeface2);
        contactText.setTypeface(typeface2);
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
        startActivity(new Intent(CameraActivity.this,AboutActivity.class));
    }

}