package com.xaugmentedreality.arproject.utility;



public class VideoModel
{

    private final String videoid;

    private final String titles;

    private final String desc;

    public VideoModel(String videoid,String titles, String desc)
    {
        this.videoid = videoid;
        this.titles = titles;
        this.desc = desc;

    }

    public String getVideoid()
    {
        return videoid;
    }


    public String getTitles()
    {
        return titles;
    }


    public String getDesc()
    {
        return desc;
    }

}
