package com.xaugmentedreality.arproject.utility;

/**
 * Model class to insert download URLs and corresponding Uid's
 * to download using Picasso
 * Created by oblivion on 1/11/2017.
 */
public class DownLoadList
{
    private final String IMAGE_URL;
    private final String UID;
    private final String TITLE;

    public DownLoadList(String IMAGE_URL,String UID,String TITLE)
    {
        this.IMAGE_URL = IMAGE_URL;
        this.UID = UID;
        this.TITLE = TITLE;
    }

    public String getImageUrl()
    {
        return IMAGE_URL;
    }

    public String getUid()
    {
        return UID;
    }

    public String getTitle()
    {
        return TITLE;
    }
}
