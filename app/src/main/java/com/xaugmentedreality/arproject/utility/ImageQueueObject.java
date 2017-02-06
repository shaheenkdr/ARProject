package com.xaugmentedreality.arproject.utility;



public class ImageQueueObject
{
    private String uid;
    private String namex;
    private String desc;
    private boolean isVideo;
    private boolean isDeleted;
    private String urlImg;
    private String urlApp;
    private int updates;
    private boolean isDownloaded;
    private String location;

    public ImageQueueObject(){}

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

    public boolean getIsDeleted()
    {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted)
    {
        this.isDeleted = isDeleted;
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

    public int getUpdates()
    {
        return updates;
    }

    public void setUpdates(int updates)
    {
        this.updates = updates;
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
