/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.ejb.CampaignsEJBLocal;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class CampaignsBean {
    
    @EJB
    private CampaignsEJBLocal campaignsEjb;
    
    private Campaign currentCampaign = null;

    public Campaign getCurrentCampaign() {
        return currentCampaign;
    }

    public void setCurrentCampaign(Campaign currentCampaign) {
        this.currentCampaign = currentCampaign;
    }
    
    public void newCampaign() {
        currentCampaign = new Campaign();
    }
    
    public void editCampaign(Campaign campaign) {
        currentCampaign = campaign;
    }
    
    public void cancelEditing() {
        setCurrentCampaign(null);
    }
    
    public void save() {
        campaignsEjb.saveCampaign(currentCampaign);
        setCurrentCampaign(null);
    }
    
    public void delete() {
        campaignsEjb.deleteCampaign(currentCampaign.getId());
        setCurrentCampaign(null);
    }
    
    public List<Campaign> getCampaigns() {
        return campaignsEjb.getCampaigns();
    }
}
