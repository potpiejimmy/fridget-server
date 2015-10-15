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
import com.myfridget.server.vo.ScheduledProgramAction;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
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
    
    protected static ScheduledCampaignAction scheduleAction(CampaignAction action, boolean off, long now) {
        short scheduleTime = off ? action.getMinuteOfDayTo() : action.getMinuteOfDayFrom();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, (scheduleTime/60) % 24);
        cal.set(Calendar.MINUTE, scheduleTime%60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() < now) cal.add(Calendar.DAY_OF_YEAR, 1);
        return new ScheduledCampaignAction(action, cal.getTimeInMillis(), off);
    }
    
    public String getProgramForDevice(int adDeviceId) {
        // first, collect all campaign actions associated with this device:
        List<User> deviceUsers = deviceEjb.getAssignedUsers(adDeviceId);
        List<Campaign> deviceCampaigns = new ArrayList<>();
        deviceUsers.forEach(u -> deviceCampaigns.addAll(getCampaigns(u.getId())));
        List<CampaignAction> deviceActions = new ArrayList<>();
        deviceCampaigns.forEach(c -> deviceActions.addAll(getCampaignActionsForCampaign(c.getId())));
        if (deviceActions.isEmpty()) return "1A0070"; // XXX
        
        long now = System.currentTimeMillis();
        
        Collection<ScheduledProgramAction> programSchedule = getScheduleForActions(deviceActions, now);
        return buildProgramForSchedule(adDeviceId, programSchedule, now);
    }
        
    public static Collection<ScheduledProgramAction> getScheduleForActions(List<CampaignAction> deviceActions, long now) {
        
        ScheduledProgramAction initialAction = new ScheduledProgramAction(now);
        
        // now, associate each campaign action with a concrete calendar date and time:
        List<ScheduledCampaignAction> schedule = new ArrayList<>();
        for (CampaignAction action : deviceActions) {
            if (action.getMinuteOfDayFrom()%1440 == action.getMinuteOfDayTo()%1440) {
                initialAction.getActions().add(action); // always on!
            } else {
                schedule.add(scheduleAction(action, false, now)); // schedule "on" action
                schedule.add(scheduleAction(action, true, now)); // schedule "off" action
            }
        }
        // now sort the schedule:
        Collections.sort(schedule);
        
        // collect all actions that are already active on program start
        List<CampaignAction> gotOn = new ArrayList<>();
        for (ScheduledCampaignAction action : schedule) {
            CampaignAction a = action.getAction();
            if (action.isOff()) {if (!gotOn.contains(a)) initialAction.getActions().add(a);}
            else gotOn.add(a);
        }
        
        // build the program action schedule:
        Map<Long,ScheduledProgramAction> programSchedule = new TreeMap<>();
        programSchedule.put(0L, initialAction);
        ScheduledProgramAction lastAction = initialAction;
        for (ScheduledCampaignAction action : schedule) {
            ScheduledProgramAction currentAction = programSchedule.get(action.getScheduledTime());
            if (currentAction == null) {
                currentAction = new ScheduledProgramAction(action.getScheduledTime());
                currentAction.getActions().addAll(lastAction.getActions());
                programSchedule.put(action.getScheduledTime(), currentAction);
                lastAction = currentAction;
            }
            if (action.isOff()) {
                currentAction.getActions().remove(action.getAction());
            } else {
                currentAction.getActions().add(action.getAction());
            }
        }
        
        return programSchedule.values();
    }
    
    protected String buildProgramForSchedule(int adDeviceId, Collection<ScheduledProgramAction> schedule, long now) {
        if (schedule.isEmpty()) return null;
        dumpProgramSchedule(schedule); // XXX for debugging
        
        int cycleLen = systemEjb.getAttinyCycleLength();
        
        //cal.setTimeInMillis(schedule.get(0).getScheduledTime());
        StringBuilder program = new StringBuilder();
        Properties imageMap = new Properties();
        Map<Integer,String> usedMediumIds = new HashMap<>();
        StringBuilder flashImages = new StringBuilder();
        
        final String IMG_START_INDEX = "D"; // start campaign images at index D (A,B,C reserved for welcome, setup and connection error screens)
        byte currentImageIndex = IMG_START_INDEX.getBytes()[0];
        for (ScheduledProgramAction scheduledAction : schedule) {
            List<CampaignAction> actions = scheduledAction.getActions();
            
            final int numActions = Math.min(actions.size(), 9); // XXX do not support more than 9 images
            program.append(numActions);
            for (int i=0; i<numActions; i++) {
                CampaignAction action = actions.get(i);
                String image = usedMediumIds.get(action.getAdMediumId());
                if (image == null) {
                    image = new String(new byte[] {currentImageIndex});
                    usedMediumIds.put(action.getAdMediumId(), image);
                    imageMap.setProperty(image, ""+action.getAdMediumId());
                    flashImages.append(image);
                    currentImageIndex++;
                }
                program.append(image);
            }
            
            long offset = scheduledAction.getScheduledTime() - now;
            long cycles = Math.max(Math.round(((double)offset)/cycleLen), 2);
            String delayString = Long.toHexString(0x10000+cycles).substring(1);
            program.append(delayString);

            now = scheduledAction.getScheduledTime() + 15000; // XXX 15 sec. img update
        }
        // remember images map in parameter "p"
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
            return "1A0070"; //XXX 
        }
        
        System.out.println(imageMapString.toString()); // XXX for debugging
        System.out.println(program.toString()); // XXX for debugging
        
        return program.toString();
    }
    
    /**
     * For testing only.
     * @param schedule a schedule
     */
    protected static void dumpProgramSchedule(Collection<ScheduledProgramAction> schedule) {
        final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int i=0;
        for (ScheduledProgramAction action : schedule) {
            System.out.print((++i) + " " + DF.format(new Date(action.getScheduledTime())) + " ");
            for (CampaignAction a : action.getActions()) System.out.print(a.getId()+"["+a.getAdMediumId()+"]"+" ");
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        List<CampaignAction> actions = new ArrayList<>();
        actions.add(new CampaignAction(1, (short)0, (short)(24*60-1)));
        actions.add(new CampaignAction(2, (short)60, (short)120));
        actions.add(new CampaignAction(3, (short)120, (short)180));
        actions.add(new CampaignAction(4, (short)150, (short)210));
        
        dumpProgramSchedule(getScheduleForActions(actions, System.currentTimeMillis()-180000000));
    }
}
