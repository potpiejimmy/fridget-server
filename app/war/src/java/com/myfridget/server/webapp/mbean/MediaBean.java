/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.GoogleCalendarRenderer;
import com.myfridget.server.util.Utils;
import com.myfridget.server.webapp.util.GoogleAuthorizationServlet;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
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
        byte[] image = imageData.get(displayType);
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
    }
    
    public StreamedContent getMediumPreview() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // Rendering the view. Return a stub StreamedContent so that it will generate URL.
            return new DefaultStreamedContent();
        } else {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("adMediumId");
            byte[] image = mediumEjb.getMediumPreview(Integer.parseInt(id), EPDUtils.SPECTRA_DISPLAY_TYPE_441);
            if (image == null) image = mediumEjb.getMediumPreview(Integer.parseInt(id), EPDUtils.SPECTRA_DISPLAY_TYPE_74);
            return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
        }
    }
    
    // ---------------
    
    public void generateCalendar() {
        try {
            if (GoogleAuthorizationServlet.startAuthorizationRequest(WebUtils.getHttpServletRequest(), WebUtils.getHttpServletResponse())) {
                // okay, do it:
                GoogleCalendarRenderer renderer = new GoogleCalendarRenderer();
                setImageData((Utils.encodeImage(renderer.renderCalendar(), "png")));
            }
        } catch (Exception e) {
            WebUtils.addFacesMessage(e);
        }
    }
}
