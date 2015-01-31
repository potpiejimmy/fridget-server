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
    
    public List<Campaign> getCampaigns();
    
    public List<CampaignAction> getCampaignActionsForCampaign(int campaignId);
    
    public void saveCampaign(Campaign campaign, List<CampaignAction> actions);
    
    public void deleteCampaign(int id);
}
