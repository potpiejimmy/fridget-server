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
import com.myfridget.server.vo.AdMediumPreviewImageData;
import com.myfridget.server.webapp.util.GoogleAuthorizationServlet;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
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
            byte[] data = Utils.encodeImage(renderer.renderCalendar(WebUtils.getCurrentPerson()), "png");
            imageData.put(displayType, new AdMediumPreviewImageData(mediumEjb.convertImage(data, displayType), AdMediumItem.GENERATION_TYPE_AUTO_GCAL));
        } catch (Exception e) {
            WebUtils.addFacesMessage(e);
        }
    }
    
    protected void doGenerateTasks(int displayType) {
        try {
            
        } catch (Exception e) {
            WebUtils.addFacesMessage(e);
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
}
