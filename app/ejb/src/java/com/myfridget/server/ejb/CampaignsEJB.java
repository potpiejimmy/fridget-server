/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
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

    @Override
    public List<Campaign> getCampaigns() {
        return em.createNamedQuery("Campaign.findByUserId", Campaign.class).setParameter("userId", usersEjb.getCurrentUser().getId()).getResultList();
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
    
}
