/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.Campaign;
import com.myfridget.server.db.entity.CampaignAction;
import com.myfridget.server.db.entity.User;
import com.myfridget.server.vo.ScheduledCampaignAction;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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
        }
        for (CampaignAction a : actions) {
            a.setCampaignId(campaign.getId());
            em.merge(a);
        }
    }

    @Override
    public void deleteCampaign(int id) {
        getCampaignActionsForCampaign(id).forEach(a->em.remove(a));
        em.remove(em.find(Campaign.class, id));
    }
    
    @Override
    public String getProgramForDevice(int adDeviceId) {
        // first, collect all campaign actions associated with this device:
        List<User> deviceUsers = deviceEjb.getAssignedUsers(adDeviceId);
        List<Campaign> deviceCampaigns = new ArrayList<>();
        deviceUsers.forEach(u -> deviceCampaigns.addAll(getCampaigns(u.getId())));
        List<CampaignAction> deviceActions = new ArrayList<>();
        deviceCampaigns.forEach(c -> deviceActions.addAll(getCampaignActionsForCampaign(c.getId())));
        if (deviceActions.isEmpty()) return "A0070"; // XXX
        
        long now = System.currentTimeMillis();
        
        // now, associate each campaign action with a concrete calendar date and time:
        List<ScheduledCampaignAction> schedule = new ArrayList<>();
        for (CampaignAction action : deviceActions) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, action.getTimeOfDay()/100);
            cal.set(Calendar.MINUTE, action.getTimeOfDay()%100);
            cal.set(Calendar.SECOND, 0);
            if (cal.getTimeInMillis() < now) cal.add(Calendar.DAY_OF_YEAR, 1);
            schedule.add(new ScheduledCampaignAction(action, cal.getTimeInMillis()));
        }
        // now sort the schedule:
        Collections.sort(schedule);

        return buildProgramForSchedule(adDeviceId, schedule, now);
    }
    
    protected String buildProgramForSchedule(int adDeviceId, List<ScheduledCampaignAction> schedule, long now) {
        if (schedule.isEmpty()) return null;
        int cycleLen = systemEjb.getAttinyCycleLength();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(schedule.get(0).getScheduledTime());
        int currentDay = cal.get(Calendar.DAY_OF_YEAR);
        StringBuilder program = new StringBuilder();
        Properties imageMap = new Properties();
        final String IMG_START_INDEX = "D";
        byte currentImageIndex = IMG_START_INDEX.getBytes()[0];
        // always start with showing pictue "D" which is the LAST picture of the previous program!
        program.append(IMG_START_INDEX); 
        for (ScheduledCampaignAction scheduledAction : schedule) {
            CampaignAction action = scheduledAction.getAction();
            long offset = scheduledAction.getScheduledTime() - now;
            long cycles = Math.round(((double)offset)/cycleLen);
            String delayString = Long.toHexString(0x10000+cycles).substring(1);
            program.append(delayString);
            cal.setTimeInMillis(scheduledAction.getScheduledTime());
            if (cal.get(Calendar.DAY_OF_YEAR) != currentDay) {
                // okay, this is the first event of next day, we have reached
                // the end of this program.  we remember it's picture ID
                // in index position "D":
                imageMap.setProperty(IMG_START_INDEX, ""+action.getAdMediumId());
                break;
            } else {
                currentImageIndex++;
                String currentImage = new String(new byte[] {currentImageIndex});
                imageMap.setProperty(currentImage, ""+action.getAdMediumId());
                program.append(currentImage);
                now = scheduledAction.getScheduledTime() + 15000; // XXX 15 sec. img update
            }
        }
        // remember used images map in parameter "p"
        StringWriter imageMapString = new StringWriter();
        try {imageMap.store(imageMapString, null);}
        catch (IOException ioe) {}
        deviceEjb.setParameter(new AdDeviceParameter(adDeviceId, "p", imageMapString.toString()));
        return program.toString();
    }
}
