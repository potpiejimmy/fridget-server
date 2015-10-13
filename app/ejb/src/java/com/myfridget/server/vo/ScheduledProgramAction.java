/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.vo;

import com.myfridget.server.db.entity.CampaignAction;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one scheduled program step holding all campaign actions present at
 * that particular time.
 * @author thorsten
 */
public class ScheduledProgramAction {
    private List<CampaignAction> actions = null;
    private long scheduledTime = 0;
    
    public ScheduledProgramAction(long scheduledTime) {
        this.actions = new ArrayList<>();
        this.scheduledTime = scheduledTime;
    }

    public List<CampaignAction> getActions() {
        return actions;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }
}
