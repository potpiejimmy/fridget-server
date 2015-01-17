/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.Campaign;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface CampaignsEJBLocal {
    
    public List<Campaign> getCampaigns();
    
    public void saveCampaign(Campaign campaign);
    
    public void deleteCampaign(int id);
}
