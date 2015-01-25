/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class MediaBean extends ImageUploadBean {
    
    @EJB
    protected AdMediumEJBLocal mediumEjb;
    
    private AdMedium currentMedium = null;

    public AdMedium getCurrentMedium() {
        return currentMedium;
    }

    public void setCurrentMedium(AdMedium currentMedium) {
        this.currentMedium = currentMedium;
    }
    
    public List<AdMedium> getMedia() {
        return mediumEjb.getMediaForCurrentUser();
    }
    
    public void save() {
        
    }
    
    public StreamedContent getCurrentMedium4() throws IOException {return getCurrentMedium(EPDUtils.SPECTRA_DISPLAY_TYPE_441);}
    public StreamedContent getCurrentMedium7() throws IOException {return getCurrentMedium(EPDUtils.SPECTRA_DISPLAY_TYPE_74);}
    public StreamedContent getCurrentMedium(int displayType) throws IOException {
        if (currentMedium == null || currentMedium.getId() == null) return null;
        byte[] image = mediumEjb.getMediumPreview(currentMedium.getId(), displayType);
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
    }
    
}
