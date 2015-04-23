/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class ImageUploadBean {
    
    @EJB
    protected AdMediumEJBLocal mediumEjb;
    
    protected int selectedDisplayType = -1;
    
    protected AdMedium currentMedium = null;
    
    protected Map<Integer,byte[]> imageData = new HashMap<>();

    public AdMedium getCurrentMedium() {
        return currentMedium;
    }

    public void setCurrentMedium(AdMedium currentMedium) {
        this.currentMedium = currentMedium;
        
        if (currentMedium == null) {
            imageData.clear();
            return;
        }
        
        try {
            for (int type : EPDUtils.SPECTRA_DISPLAY_DEFAULT_TYPES) {
                byte[] data = mediumEjb.getMediumPreview(currentMedium.getId(), type);
                if (data != null) imageData.put(type, data);
            }
        } catch (IOException ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile originalImage = event.getFile();
            byte[] data = Utils.readAll(originalImage.getInputstream());
            if (selectedDisplayType < 0) {
                for (int type : EPDUtils.SPECTRA_DISPLAY_DEFAULT_TYPES)
                    imageData.put(type, mediumEjb.convertImage(data, type));
            } else {
                imageData.put(selectedDisplayType, mediumEjb.convertImage(data, selectedDisplayType));
            }
            WebUtils.addFacesMessage("File " + event.getFile().getFileName() + " uploaded successfully.");
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public void save() {
        try {
            for (int type : imageData.keySet()) {
                mediumEjb.setMediumPreview(currentMedium.getId(), type, imageData.get(type));
            }
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public List<SelectItem> getDisplayTypesSelectItems() {
        List<SelectItem> result = new ArrayList<>();
        result.add(new SelectItem(-1, "<All sizes>"));
        result.add(new SelectItem(EPDUtils.SPECTRA_DISPLAY_TYPE_441, "Spectra 4.41\" (400x300)"));
        result.add(new SelectItem(EPDUtils.SPECTRA_DISPLAY_TYPE_74, "Spectra 7.4\" (480x800)"));
        return result;
    }
    
    public int getSelectedDisplayType() {
        return selectedDisplayType;
    }

    public void setSelectedDisplayType(int selectedDisplayType) {
        this.selectedDisplayType = selectedDisplayType;
    }
}
