package com.xaugmentedreality.arproject.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by obx on 2/3/17.
 */

public class VideoDatabase extends RealmObject
{
    @PrimaryKey
    private String videoid;

    private String titles;

    private String desc;

    public VideoDatabase(){}

    public String getVideoid()
    {
        return videoid;
    }

    public void setVideoid(String videoid)
    {
        this.videoid = videoid;
    }

    public String getTitles()
    {
        return titles;
    }

    public void setTitles(String titles)
    {
        this.titles = titles;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

}
