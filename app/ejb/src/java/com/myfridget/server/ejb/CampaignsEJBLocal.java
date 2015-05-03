/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface CampaignsEJBLocal {
    
    /**
     * Returns the campaigns for the currently logged in user
     * @return list of campaigns
     */
    public List<Campaign> getCampaigns();
    
    /**
     * Returns all campaigns of the specified user
     * @param userId a user
     * @return list of campaigns
     */
    public List<Campaign> getCampaigns(int userId);
    
    public List<CampaignAction> getCampaignActionsForCampaign(int campaignId);
    
    public void saveCampaign(Campaign campaign, List<CampaignAction> actions);
    
    public void deleteCampaign(int id);
    
    public String getProgramForDevice(int adDeviceId);
}
