/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.google.GoogleCalendarRenderer;
import com.myfridget.server.util.Utils;
import com.myfridget.server.util.google.GoogleAuthorizationHelper;
import com.myfridget.server.util.google.GoogleTasks;
import com.myfridget.server.util.google.GoogleTasksRenderer;
import com.myfridget.server.util.wettercom.WetterDotComRenderer;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import com.myfridget.server.webapp.util.GoogleAuthorizationServlet;
import com.myfridget.server.webapp.util.WebUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class MediaBean extends ImageUploadBean {
    
    public List<AdMedium> getMedia() {
        return mediumEjb.getMediaForCurrentUser();
    }
    
    public void newMedium() {
        currentMedium = new AdMedium();
    }
    
    @Override
    public void save() {
        currentMedium = mediumEjb.saveMedium(currentMedium);
        super.save();
        setCurrentMedium(null);
    }
    
    public void edit(AdMedium medium) {
        setCurrentMedium(medium);
    }
    
    public void cancel() {
        setCurrentMedium(null);
    }
    
    public void delete() {
        mediumEjb.deleteMedium(currentMedium.getId());
        cancel();
    }
    
    public StreamedContent getCurrentMediumDisplay(int displayType) throws IOException {
        AdMediumPreviewImageData image = imageData.get(displayType);
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image.data), "image/png");
    }
    
    public StreamedContent getMediumPreview() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // Rendering the view. Return a stub StreamedContent so that it will generate URL.
            return new DefaultStreamedContent();
        } else {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("adMediumId");
            AdMediumPreviewImageData image = mediumEjb.getMediumPreview(Integer.parseInt(id), EPDUtils.SPECTRA_DISPLAY_TYPE_441);
            if (image == null) image = mediumEjb.getMediumPreview(Integer.parseInt(id), EPDUtils.SPECTRA_DISPLAY_TYPE_74);
            return new DefaultStreamedContent(new ByteArrayInputStream(image.data), "image/png");
        }
    }
    
    // ---------------
    
    public String getGoogleRefreshToken() {
        return new GoogleAuthorizationHelper(WebUtils.getCurrentPerson()).getRefreshToken();
    }
    
    protected void doAuthorizedImageGen(Consumer<Integer> generator) {
        if (GoogleAuthorizationServlet.authorize(WebUtils.getHttpServletRequest(), WebUtils.getHttpServletResponse())) {
            // okay, authorized, generate images:
            if (selectedDisplayType < 0) {
                for (int type : EPDUtils.SPECTRA_DISPLAY_DEFAULT_TYPES)
                    generator.accept(type);
            } else {
                generator.accept(selectedDisplayType);
            }
        }
    }
    
    public void generateCalendar() {
        doAuthorizedImageGen(type -> doGenerateCalendar(type));
    }
    
    public void generateTasks() {
        doAuthorizedImageGen(type -> doGenerateTasks(type));
    }
    
    protected void doGenerateCalendar(int displayType) {
        try {
            GoogleCalendarRenderer renderer = new GoogleCalendarRenderer(EPDUtils.dimensionForDisplayType(displayType));
            BufferedImage img = renderer.renderCalendar(WebUtils.getCurrentPerson());
            JsonObjectBuilder genInfo = Json.createObjectBuilder();
            addGeneratedImage(displayType, img, AdMediumItem.GENERATION_TYPE_AUTO_GCAL, genInfo);
        } catch (Exception e) {
            WebUtils.addFacesMessage(e);
        }
    }
    
    protected void doGenerateTasks(int displayType) {
        try {
            GoogleTasksRenderer renderer = new GoogleTasksRenderer(EPDUtils.dimensionForDisplayType(displayType));
            BufferedImage img = renderer.renderTasks(WebUtils.getCurrentPerson(), this.selectedTaskList);
            JsonObjectBuilder genInfo = Json.createObjectBuilder();
            genInfo.add("taskList", this.selectedTaskList);
            addGeneratedImage(displayType, img, AdMediumItem.GENERATION_TYPE_AUTO_GTASKS, genInfo);
        } catch (Exception e) {
            WebUtils.addFacesMessage(e);
        }
    }
    
    protected void addGeneratedImage(int displayType, BufferedImage img, short genType, JsonObjectBuilder genInfo) throws IOException {
        addWeatherPanel(img, genInfo);
        imageData.put(displayType, new AdMediumPreviewImageData(mediumEjb.convertImage(Utils.encodeImage(img, "png"), displayType), genType, genInfo.build().toString()));
    }
    
    protected void addWeatherPanel(BufferedImage img, JsonObjectBuilder genInfo) throws IOException {
        if (addWeather) {
            WetterDotComRenderer renderer = new WetterDotComRenderer();
            renderer.renderWeather(img, weatherLocation);
            genInfo.add("addWeatherForLocation", weatherLocation);
        }
    }
    
    public List<SelectItem> getTaskLists() {
        GoogleTasks tasks = new GoogleTasks(WebUtils.getCurrentPerson());
        List<GoogleTasks.GoogleTaskList> taskLists = tasks.getTaskLists();
        if (taskLists == null) return Arrays.asList(new SelectItem("<Could not load task lists>"));
        List<SelectItem> result = new ArrayList<>(taskLists.size());
        for (GoogleTasks.GoogleTaskList list : taskLists)
            result.add(new SelectItem(list.id, list.title));
        return result;
    }
    
    private String selectedTaskList = null;

    public String getSelectedTaskList() {
        return selectedTaskList;
    }

    public void setSelectedTaskList(String selectedTaskList) {
        this.selectedTaskList = selectedTaskList;
    }
    
    private boolean addWeather = false;
    private String weatherLocation = null;

    public boolean isAddWeather() {
        return addWeather;
    }

    public void setAddWeather(boolean addWeather) {
        this.addWeather = addWeather;
    }

    public String getWeatherLocation() {
        return weatherLocation;
    }

    public void setWeatherLocation(String weatherLocation) {
        this.weatherLocation = weatherLocation;
    }
    
}
