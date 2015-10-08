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
import java.io.StringReader;
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
public class CampaignsEJB {
    
    @PersistenceContext(unitName = "Fridget_EJBsPU")
    private EntityManager em;
    
    @EJB
    private UsersEJB usersEjb;

    @EJB
    private AdDeviceEJB deviceEjb;
    
    @EJB
    private SystemEJB systemEjb;
    
    public List<Campaign> getCampaigns() {
        return getCampaigns(usersEjb.getCurrentUser().getId());
    }

    public List<Campaign> getCampaigns(int userId) {
        return em.createNamedQuery("Campaign.findByUserId", Campaign.class).setParameter("userId", userId).getResultList();
    }

    public List<CampaignAction> getCampaignActionsForCampaign(int campaignId) {
        return em.createNamedQuery("CampaignAction.findByCampaignId", CampaignAction.class).setParameter("campaignId", campaignId).getResultList();
    }

    public void saveCampaign(Campaign campaign, List<CampaignAction> actions) {
        if (campaign.getId() == null) {
            campaign.setUserId(usersEjb.getCurrentUser().getId());
            em.persist(campaign);
            em.flush(); //pre-fetch ID
        } else {
            em.merge(campaign);
            getCampaignActionsForCampaign(campaign.getId()).forEach(a -> {
                if (!actions.contains(a)) em.remove(a);
            });
        }
        for (CampaignAction a : actions) {
            a.setCampaignId(campaign.getId());
            em.merge(a);
        }
    }

    public void deleteCampaign(int id) {
        getCampaignActionsForCampaign(id).forEach(a->em.remove(a));
        em.remove(em.find(Campaign.class, id));
    }
    
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
            cal.set(Calendar.HOUR_OF_DAY, action.getMinuteOfDayFrom()/60);
            cal.set(Calendar.MINUTE, action.getMinuteOfDayFrom()%60);
            cal.set(Calendar.SECOND, 0);
            if (cal.getTimeInMillis() < now) cal.add(Calendar.DAY_OF_YEAR, 1);
            schedule.add(new ScheduledCampaignAction(action, cal.getTimeInMillis()));
        }
        // now sort the schedule:
        Collections.sort(schedule);
        
        // add the first one for next day again:
        ScheduledCampaignAction first = schedule.get(0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(first.getScheduledTime());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        schedule.add(new ScheduledCampaignAction(first.getAction(), cal.getTimeInMillis()));

        return buildProgramForSchedule(adDeviceId, schedule, now);
    }
    
    protected String buildProgramForSchedule(int adDeviceId, List<ScheduledCampaignAction> schedule, long now) {
        if (schedule.isEmpty()) return null;
        int cycleLen = systemEjb.getAttinyCycleLength();
        Calendar cal = Calendar.getInstance();
        //cal.setTimeInMillis(schedule.get(0).getScheduledTime());
        int currentDay = cal.get(Calendar.DAY_OF_YEAR);
        StringBuilder program = new StringBuilder();
        Properties imageMap = new Properties();
        StringBuilder flashImages = new StringBuilder();
        final String IMG_START_INDEX = "D"; // start campaign images at index D (A,B,C reserved for welcome, setup and connection error screens)
        byte currentImageIndex = IMG_START_INDEX.getBytes()[0];
        // always start with showing pictue "D" which is the LAST picture of the previous program!
        Properties previousImageMap = new Properties();
        try {previousImageMap.load(new StringReader(deviceEjb.getParameter(adDeviceId, "p").getValue()));} catch (Exception e) {}
        if (previousImageMap.containsKey("NEXT")) {
            imageMap.setProperty(IMG_START_INDEX, previousImageMap.getProperty("NEXT"));
            program.append(IMG_START_INDEX);
            flashImages.append(IMG_START_INDEX);
        } else {
            program.append("-");
        }
        for (ScheduledCampaignAction scheduledAction : schedule) {
            CampaignAction action = scheduledAction.getAction();
            long offset = scheduledAction.getScheduledTime() - now;
            long cycles = Math.max(Math.round(((double)offset)/cycleLen), 2);
            String delayString = Long.toHexString(0x10000+cycles).substring(1);
            program.append(delayString);
            cal.setTimeInMillis(scheduledAction.getScheduledTime());
            if (cal.get(Calendar.DAY_OF_YEAR) != currentDay) {
                // okay, this is the first event of next day, we have reached
                // the end of this program.  we remember it's picture ID
                // in entry "NEXT":
                imageMap.setProperty("NEXT", ""+action.getAdMediumId());
                break;
            } else {
                currentImageIndex++;
                String currentImage = new String(new byte[] {currentImageIndex});
                imageMap.setProperty(currentImage, ""+action.getAdMediumId());
                program.append(currentImage);
                flashImages.append(currentImage);
                now = scheduledAction.getScheduledTime() + 15000; // XXX 15 sec. img update
            }
            // Note: XXX loop must not end here but only on break above
        }
        // remember used images map in parameter "p"
        StringWriter imageMapString = new StringWriter();
        try {imageMap.store(imageMapString, null);}
        catch (IOException ioe) {}
        try {
            deviceEjb.setParameter(new AdDeviceParameter(adDeviceId, "p", imageMapString.toString()));
            // and set the flashimages parameter:
            deviceEjb.setParameter(new AdDeviceParameter(adDeviceId, "flashimages", flashImages.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not store p and flashimages parameters: " + imageMapString.toString() + ", " + flashImages.toString());
            return "A0070"; //XXX 
        }
        return program.toString();
    }
}
