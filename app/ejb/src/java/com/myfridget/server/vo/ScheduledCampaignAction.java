/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.vo;

import com.myfridget.server.db.entity.CampaignAction;

/**
 * Represents a campaign action that has been scheduled for a specific date and
 * time.
 * @author thorsten.liese
 */
public class ScheduledCampaignAction implements Comparable<ScheduledCampaignAction> {

    private CampaignAction action = null;
    private long scheduledTime = 0;
    private boolean off = false;
    
    public ScheduledCampaignAction(CampaignAction action, long scheduledTime, boolean off) {
        this.action = action;
        this.scheduledTime = scheduledTime;
        this.off = off;
    }

    public CampaignAction getAction() {
        return action;
    }

    public void setAction(CampaignAction action) {
        this.action = action;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isOff() {
        return off;
    }

    public void setOff(boolean off) {
        this.off = off;
    }
    
    @Override
    public int compareTo(ScheduledCampaignAction o) {
        return (int)(scheduledTime - o.getScheduledTime());
    }
}
