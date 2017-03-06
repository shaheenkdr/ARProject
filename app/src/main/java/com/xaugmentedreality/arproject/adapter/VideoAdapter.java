package com.xaugmentedreality.arproject.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xaugmentedreality.arproject.EventBus.DeleteEvent;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.utility.VideoModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder>
{

    DataHolder d1 = new DataHolder();
    private static String RAW_URL = "https://img.youtube.com/vi/XXXXX/0.jpg";


    public  class VideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener
    {

        ImageView thumbnail;
        TextView description;
        Context mContext;
        Vibrator v;
        RelativeLayout rl;
        VideoHolder(View itemView) {
            super(itemView);

            mContext = itemView.getContext();
            v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            thumbnail = (ImageView)itemView.findViewById(R.id.videoPoster);
            rl = (RelativeLayout)itemView.findViewById(R.id.relativeVideo);
            Typeface typeface1 = Typeface.createFromAsset(mContext.getAssets(), "fonts/robotomedium.ttf");
            description = (TextView)itemView.findViewById(R.id.descText);
            description.setTypeface(typeface1);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }


        @Override
        public void onClick(View view)
        {

            //itemView.getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view)
        {
            rl.setSelected(true);
            v.vibrate(200);
            EventBus.getDefault().post(new DeleteEvent(getLayoutPosition(),d1.vidList.get(getLayoutPosition()).getVideoid()));
            return true;
        }


    }

    private class DataHolder
    {
        List<VideoModel> vidList;

    }
    public VideoAdapter(List<VideoModel> vid)
    {
        this.d1.vidList = vid;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_card, viewGroup, false);
        VideoHolder pvh = new VideoHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(VideoHolder movieViewHolder, int i)
    {
        // movieViewHolder.MovieName_fav.setText(d1.favList.get(i).getMovieName());
        Log.w("TAG",""+d1.vidList.get(i).getTitles());
        String URL = RAW_URL.replaceAll("XXXXX",d1.vidList.get(i).getVideoid());
        Glide.with(movieViewHolder.mContext)
                .load(URL)
                .into(movieViewHolder.thumbnail);

        if(d1.vidList.get(i).getDesc().length()<40)
        {
            movieViewHolder.description.setText(d1.vidList.get(i).getDesc());
        }
        else
        {
            String temp = d1.vidList.get(i).getDesc().substring(0,37)+"...";
            movieViewHolder.description.setText(temp);
        }




    }

    @Override
    public int getItemCount()
    {

        if(d1.vidList!=null)
        {
            return d1.vidList.size();
        }
        else
        {
            return 0;
        }
    }



}
