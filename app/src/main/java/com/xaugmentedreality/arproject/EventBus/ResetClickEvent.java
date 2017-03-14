package com.xaugmentedreality.arproject.EventBus;



public class ResetClickEvent
{
    private final boolean RESET_CLICK;

    public ResetClickEvent(boolean RESET_CLICK)
    {
        this.RESET_CLICK = RESET_CLICK;
    }

    public boolean getResetClick()
    {
        return RESET_CLICK;
    }
}
