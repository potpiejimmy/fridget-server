/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.ejb.CampaignsEJBLocal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class CampaignsBean {
    
    @EJB
    private CampaignsEJBLocal campaignsEjb;
    
    @EJB
    protected AdMediumEJBLocal mediumEjb;
    
    private Campaign currentCampaign = null;
    private CampaignAction currentAction = null;
    private String currentActionTime = null;
    private List<CampaignAction> currentActions = null;
            
    public Campaign getCurrentCampaign() {
        return currentCampaign;
    }

    public void setCurrentCampaign(Campaign currentCampaign) {
        this.currentCampaign = currentCampaign;
    }

    public CampaignAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(CampaignAction currentAction) {
        this.currentAction = currentAction;
    }

    public String getCurrentActionTime() {
        return currentActionTime;
    }

    public void setCurrentActionTime(String currentActionTime) {
        this.currentActionTime = currentActionTime;
    }
    
    public List<SelectItem> getMediaItems() {
        List<SelectItem> result = new ArrayList<>();
        for (AdMedium m : mediumEjb.getMediaForCurrentUser()) {
            result.add(new SelectItem(m.getId(), m.getName()));
        }
        return result;
    }

    public void newCampaign() {
        currentCampaign = new Campaign();
        currentActions = new ArrayList<>();
    }
    
    public void editCampaign(Campaign campaign) {
        currentCampaign = campaign;
        currentActions = campaignsEjb.getCampaignActionsForCampaign(currentCampaign.getId());
    }
    
    public void cancelEditing() {
        setCurrentCampaign(null);
    }
    
    public void save() {
        campaignsEjb.saveCampaign(currentCampaign, currentActions);
        setCurrentCampaign(null);
    }
    
    public void delete() {
        campaignsEjb.deleteCampaign(currentCampaign.getId());
        setCurrentCampaign(null);
    }
    
    public List<Campaign> getCampaigns() {
        return campaignsEjb.getCampaigns();
    }
    
    public List<CampaignAction> getCampaignActions() {
        return currentActions;
    }
    
    // ------------
    
    public void newAction() {
        currentAction = new CampaignAction();
    }

    public void deleteAction(CampaignAction action) {
        currentActions.remove(action);
    }

    public void saveAction() {
        currentAction.setTimeOfDay(Short.parseShort(currentActionTime.replace(":", "")));
        currentActions.add(currentAction);
        cancelAction();
    }

    public void cancelAction() {
        currentAction = null;
        currentActionTime = null;
    }
}
