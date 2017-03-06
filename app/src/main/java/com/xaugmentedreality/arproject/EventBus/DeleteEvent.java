package com.xaugmentedreality.arproject.EventBus;



public class DeleteEvent
{
    private final int id;
    private final String videoId;

    public DeleteEvent(int id,String videoId)
    {
        this.id = id;
        this.videoId = videoId;
    }

    public int getId()
    {
        return id;
    }

    public String getVideoId()
    {
        return videoId;
    }
}
