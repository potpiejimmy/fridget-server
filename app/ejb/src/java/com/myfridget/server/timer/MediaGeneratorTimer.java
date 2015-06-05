/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.timer;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.ejb.AdMediumEJB;
import com.myfridget.server.ejb.UsersEJB;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.google.GoogleCalendarRenderer;
import com.myfridget.server.util.Utils;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;

/**
 *
 * @author thorsten
 */
@Singleton
@Startup
public class MediaGeneratorTimer {
    
    @Resource
    protected TimerService timerService;
    
    @EJB
    protected AdMediumEJB mediumEjb;
    
    @EJB
    protected UsersEJB userEjb;
    
    @PostConstruct
    public void initialize(){
        ScheduleExpression expression = new ScheduleExpression();
        expression.hour("1,7,13,19");
        timerService.createCalendarTimer(expression);
    }
    
    @Timeout
    public void execute(){
        System.out.println("----MediaGeneratorTimer: " + new java.util.Date());
        
        for (AdMediumItem item : mediumEjb.getAllMediumItems()) {
            
            if (item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GCAL) {
            
                AdMedium medium = mediumEjb.findAdMedium(item.getAdMediumId());
                String userId = userEjb.getUser(medium.getUserId()).getEmail();
                System.out.println("--------MediaGeneratorTimer: Generating item " + item.getId() + " for " + userId);

                try {
                    GoogleCalendarRenderer renderer = new GoogleCalendarRenderer(EPDUtils.dimensionForDisplayType(item.getType()));
                    byte[] img = mediumEjb.convertImage(Utils.encodeImage(renderer.renderCalendar(userId), "png"), item.getType());
                    mediumEjb.setMediumPreview(medium.getId(), item.getType(), new AdMediumPreviewImageData(img, item.getGentype()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
