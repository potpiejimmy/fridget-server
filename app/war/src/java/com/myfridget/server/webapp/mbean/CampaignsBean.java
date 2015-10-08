/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
import com.myfridget.server.ejb.AdMediumEJB;
import com.myfridget.server.ejb.CampaignsEJB;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class CampaignsBean {
    
    @EJB
    private CampaignsEJB campaignsEjb;
    
    @EJB
    protected AdMediumEJB mediumEjb;
    
    private Campaign currentCampaign = null;
    private CampaignAction currentAction = null;
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
    
    public StreamedContent getCampaignPreviewImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // Rendering the view. Return a stub StreamedContent so that it will generate URL.
            return new DefaultStreamedContent();
        } else {
            String campaignId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("campaignId");
            List<CampaignAction> actions = campaignsEjb.getCampaignActionsForCampaign(Integer.parseInt(campaignId));
            if (actions.isEmpty()) return null;
            AdMediumPreviewImageData image = mediumEjb.getMediumPreview(actions.get(0).getAdMediumId(), EPDUtils.SPECTRA_DISPLAY_TYPE_441);
            if (image == null) image = mediumEjb.getMediumPreview(actions.get(0).getAdMediumId(), EPDUtils.SPECTRA_DISPLAY_TYPE_74);
            return new DefaultStreamedContent(new ByteArrayInputStream(image.data), "image/png");
        }
    }
    // ------------
    // actions
    // ------------
    
    private boolean editingAction = false;
    
    public void newAction() {
        currentAction = new CampaignAction();
        currentAction.setMinuteOfDayTo((short)1440);
    }

    public void deleteAction(CampaignAction action) {
        currentActions.remove(action);
    }

    public void editAction(CampaignAction action) {
        currentAction = action;
        editingAction = true;
    }

    public void saveAction() {
        if (!editingAction) currentActions.add(currentAction);
        cancelAction();
    }

    public void cancelAction() {
        currentAction = null;
        editingAction = false;
    }
    
    public String getFormattedActionTime(short minuteOfDay) {
        return twoDigitNumber(minuteOfDay / 60) + ":" + twoDigitNumber(minuteOfDay % 60);
    }
    
    protected static String twoDigitNumber(int number) {
        return ("" + (100+number)).substring(1);
    }
}
