package com.xaugmentedreality.arproject.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xaugmentedreality.arproject.R;
import com.xaugmentedreality.arproject.utility.VideoModel;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder>
{

    DataHolder d1 = new DataHolder();
    private static String RAW_URL = "https://img.youtube.com/vi/XXXXX/0.jpg";


    public  class VideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        ImageView thumbnail;
        TextView title;
        TextView description;
        Context mContext;
        VideoHolder(View itemView) {
            super(itemView);

            mContext = itemView.getContext();
            thumbnail = (ImageView)itemView.findViewById(R.id.videoPoster);
            title = (TextView)itemView.findViewById(R.id.titleText);
            description = (TextView)itemView.findViewById(R.id.descText);
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view)
        {

            //itemView.getContext().startActivity(intent);
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
