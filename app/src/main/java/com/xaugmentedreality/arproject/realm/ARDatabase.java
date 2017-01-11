package com.xaugmentedreality.arproject.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by oblivion on 1/10/2017.
 * Realm database model class
 */
public class ARDatabase extends RealmObject
{
    @PrimaryKey
    private String uid;

    private String namex;
    private String desc;
    private boolean isVideo;
    private String urlImg;
    private String urlApp;
    private boolean isDownloaded;
    private String location;

    public ARDatabase(){}

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getNamex()
    {
        return namex;
    }

    public void setNamex(String namex)
    {
        this.namex = namex;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public boolean getIsVideo()
    {
        return isVideo;
    }

    public void setIsVideo(boolean isVideo)
    {
        this.isVideo = isVideo;
    }

    public String getUrlImg()
    {
        return urlImg;
    }

    public void setUrlImg(String urlImg)
    {
        this.urlImg = urlImg;
    }

    public String getUrlApp()
    {
        return urlApp;
    }

    public void setUrlApp(String urlApp)
    {
        this.urlApp = urlApp;
    }

    public boolean getIsDownloaded()
    {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded)
    {
        this.isDownloaded = isDownloaded;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
}
