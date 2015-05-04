/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.db.entity.User;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author thorsten
 */
@Stateless
public class CampaignsEJB implements CampaignsEJBLocal {
    
    @PersistenceContext(unitName = "Fridget_EJBsPU")
    private EntityManager em;
    
    @EJB
    private UsersEJBLocal usersEjb;

    @EJB
    private AdDeviceEJBLocal deviceEjb;
    
    @EJB
    private SystemEJBLocal systemEjb;
    
    @Override
    public List<Campaign> getCampaigns() {
        return getCampaigns(usersEjb.getCurrentUser().getId());
    }

    @Override
    public List<Campaign> getCampaigns(int userId) {
        return em.createNamedQuery("Campaign.findByUserId", Campaign.class).setParameter("userId", userId).getResultList();
    }

    @Override
    public List<CampaignAction> getCampaignActionsForCampaign(int campaignId) {
        return em.createNamedQuery("CampaignAction.findByCampaignId", CampaignAction.class).setParameter("campaignId", campaignId).getResultList();
    }

    @Override
    public void saveCampaign(Campaign campaign, List<CampaignAction> actions) {
        if (campaign.getId() == null) {
            campaign.setUserId(usersEjb.getCurrentUser().getId());
            em.persist(campaign);
            em.flush(); //pre-fetch ID
        } else {
            em.merge(campaign);
            for (CampaignAction a : getCampaignActionsForCampaign(campaign.getId())) em.remove(a);
        }
        for (CampaignAction a : actions) {
            a.setCampaignId(campaign.getId());
            em.persist(a);
        }
    }

    @Override
    public void deleteCampaign(int id) {
        em.remove(em.find(Campaign.class, id));
    }
    
    @Override
    public String getProgramForDevice(int adDeviceId) {
        SystemParameter cycleLenParam = systemEjb.getSystemParameter("attiny.cycle.len");
        int cycleLen = 0;
        try {
            cycleLen = Integer.parseInt(cycleLenParam.getValue());
        } catch (Exception e) {
            cycleLen = 8870;  // use a default
        }
        List<User> deviceUsers = deviceEjb.getAssignedUsers(adDeviceId);
        List<Campaign> deviceCampaigns = new ArrayList<>();
        deviceUsers.forEach(u -> deviceCampaigns.addAll(getCampaigns(u.getId())));
        List<CampaignAction> deviceActions = new ArrayList<>();
        deviceCampaigns.forEach(c -> deviceActions.addAll(getCampaignActionsForCampaign(c.getId())));
        if (deviceActions.isEmpty()) return "A0070"; // XXX
        // XXX just a demo, use first action of first campaign:
        CampaignAction demoAction = deviceActions.get(0);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, demoAction.getTimeOfDay()/100);
        cal.set(Calendar.MINUTE, demoAction.getTimeOfDay()%100);
        cal.set(Calendar.SECOND, 0);
        long now = System.currentTimeMillis();
        if (cal.getTimeInMillis() < now) cal.add(Calendar.DAY_OF_YEAR, 1);
        long offset = cal.getTimeInMillis() - now;
        long cycles = Math.round(((double)offset)/cycleLen);
        String delayString = Long.toHexString(0x10000+cycles).substring(1);
        // XXX: remember used medium ID in parameter "p"
        deviceEjb.setParameter(new AdDeviceParameter(null, adDeviceId, "p", ""+demoAction.getAdMediumId()));
        return "-" + delayString + "P0001";
    }
}
