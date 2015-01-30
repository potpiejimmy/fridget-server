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
    
    public List<AdMedium> getMedia() {
        return mediumEjb.getMediaForCurrentUser();
    }
    
    public void newMedium() {
        currentMedium = new AdMedium();
    }
    
    public void save() {
        mediumEjb.saveMedium(currentMedium);
        super.save();
        setCurrentMedium(null);
    }
    
    public void edit(AdMedium medium) {
        setCurrentMedium(medium);
    }
    
    public void cancel() {
        setCurrentMedium(null);
    }
    
    public StreamedContent getCurrentMediumDisplay(int displayType) throws IOException {
        byte[] image = imageData.get(displayType);
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
    }
    
}
