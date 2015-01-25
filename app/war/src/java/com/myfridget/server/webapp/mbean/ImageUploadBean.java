/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import com.myfridget.server.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.List;
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
    
    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile originalImage = event.getFile();
            mediumEjb.uploadImage(selectedDisplayType, Utils.readAll(originalImage.getInputstream()));
            WebUtils.addFacesMessage("File " + event.getFile().getFileName() + " uploaded successfully.");
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
