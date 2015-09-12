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
import com.myfridget.server.util.google.GoogleTasksRenderer;
import com.myfridget.server.util.wettercom.WetterDotComRenderer;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.json.Json;
import javax.json.JsonObject;

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
            
            if (item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GCAL ||
                item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GTASKS) {
            
                AdMedium medium = mediumEjb.findAdMedium(item.getAdMediumId());
                String userId = userEjb.getUser(medium.getUserId()).getEmail();
                System.out.println("--------MediaGeneratorTimer: Generating item " + item.getId() + " for " + userId);

                try {
                    if (item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GCAL ||
                        item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GTASKS) {
                        
                        BufferedImage image = null;
                        JsonObject genInfo = null;
                        if (item.getGeninfo()!=null) 
                            genInfo = Json.createReader(new StringReader(item.getGeninfo())).readObject();
                        
                        if (item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GCAL) {
                            GoogleCalendarRenderer renderer = new GoogleCalendarRenderer(EPDUtils.dimensionForDisplayType(item.getType()));
                            image = renderer.renderCalendar(userId);
                        } else if (item.getGentype() == AdMediumItem.GENERATION_TYPE_AUTO_GTASKS) {
                            GoogleTasksRenderer renderer = new GoogleTasksRenderer(EPDUtils.dimensionForDisplayType(item.getType()));
                            image = renderer.renderTasks(userId, genInfo.getString("taskList"));
                        }
                        renderWeatherPanel(image, genInfo);
                        byte[] img = mediumEjb.convertImage(Utils.encodeImage(image, "png"), item.getType());
                        mediumEjb.setMediumPreview(medium.getId(), item.getType(), new AdMediumPreviewImageData(img, item.getGentype(), item.getGeninfo()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected void renderWeatherPanel(BufferedImage img, JsonObject genInfo) throws IOException {
        String location = null;
        if (genInfo != null) location = genInfo.getString("addWeatherForLocation", null);
        if (location != null && location.length()>0) {
            WetterDotComRenderer renderer = WetterDotComRenderer.createInstanceForImage(img);
            renderer.renderWeather(location);
        }
    }
}
