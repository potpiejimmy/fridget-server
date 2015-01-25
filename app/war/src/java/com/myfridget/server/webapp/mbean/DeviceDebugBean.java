/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.AdDeviceTestImage;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import com.myfridget.server.ejb.UsersEJBLocal;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class DeviceDebugBean extends ImageUploadBean {
    
    protected final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @EJB
    private AdDeviceEJBLocal deviceEjb;
    @EJB
    private UsersEJBLocal usersEjb;
    
    private Integer selectedDevice = null;
    
    private String exec = null;
    private boolean connectToCloud = false;
    private String flashImages = null;
    
    private int currentImageIndex = 0;
    
    private boolean autoUpdate = false;

    public String getSayHello() {
        return "Say Hello";
    }
    
    public List<AdDeviceDebugMsg> getDebugMessages() {
        List<AdDeviceDebugMsg> msgs = deviceEjb.getDebugMessages(selectedDevice!=null ? selectedDevice : 0);
        return msgs;
    }
    
    public List<SelectItem> getDevicesSelectItems() {
        List<AdDevice> devices = deviceEjb.getDevicesForUser(usersEjb.getCurrentUser().getId());
        List<SelectItem> items = new ArrayList<>();
        items.add(new SelectItem(null, "<Select Device>"));
        devices.forEach(i -> items.add(new SelectItem(i.getId(), i.getName())));
        return items;
    }
    
    protected void readDeviceSettings() {
        if (selectedDevice == null) {
            exec = null;
            connectToCloud = false;
            flashImages = null;
        } else {
            AdDeviceParameter param = deviceEjb.getParameter(selectedDevice, "exec");
            exec = (param != null) ? param.getValue() : null;
            param = deviceEjb.getParameter(selectedDevice, "connectmode");
            connectToCloud = (param != null) ? "1".equals(param.getValue()) : true;
            param = deviceEjb.getParameter(selectedDevice, "flashimages");
            flashImages = (param != null) ? param.getValue() : null;
        }
    }
    
    protected void writeDeviceSettings() {
        if (selectedDevice == null) return;
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "exec", exec));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "connectmode", connectToCloud ? "1" : "2"));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "flashimages", flashImages == null ? "" : flashImages));
    }
    
    public void save() {
        try {
            writeDeviceSettings();
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
            ex.printStackTrace();
        }
    }
    
    public void clearLog() {
        deviceEjb.clearDebugMessages(selectedDevice!=null ? selectedDevice : 0);
    }
    
    @Override
    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile originalImage = event.getFile();
            deviceEjb.uploadTestImage(selectedDevice, selectedDisplayType, Utils.readAll(originalImage.getInputstream()));
            WebUtils.addFacesMessage("File " + event.getFile().getFileName() + " uploaded successfully.");
            currentImageIndex = deviceEjb.getDeviceTestImages(selectedDevice).size()-1;
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public List<SelectItem> getImageSelectItems() {
        List<SelectItem> result = new ArrayList<>();
        List<AdDeviceTestImage> imgs = getImages();
        if (imgs != null) {
            for (int i=0; i<imgs.size(); i++) result.add(new SelectItem(i, "Image "+ new String(new byte[] {(byte)(65+i)})));
        }
        return result;
    }
    
    public List<AdDeviceTestImage> getImages() {
        if (selectedDevice == null) return null;
        return deviceEjb.getDeviceTestImages(selectedDevice);
    }
    
    public StreamedContent getImageData() throws IOException {
        List<AdDeviceTestImage> imgs = getImages();
        if (imgs == null || imgs.size() == 0) return null;
        byte[] image = deviceEjb.getTestImagePreview(imgs.get(currentImageIndex).getId());
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
    }
    
    public void deleteImage() throws IOException {
        List<AdDeviceTestImage> imgs = getImages();
        if (imgs == null || imgs.size() == 0) return;
        deviceEjb.removeTestImage(imgs.get(currentImageIndex).getId());
        if (currentImageIndex == imgs.size()-1 && currentImageIndex > 0) currentImageIndex--;
    }
    
    public String getFormattedDate(Long date) {
        return DATE_FORMAT.format(new java.util.Date(date));
    }

    public Integer getSelectedDevice() {
        return selectedDevice;
    }
    
    public String getSelectedDeviceSerial() {
        if (selectedDevice == null) return null;
        return deviceEjb.getById(selectedDevice).getSerial();
    }

    public void setSelectedDevice(Integer selectedDevice) {
        this.selectedDevice = selectedDevice;
        readDeviceSettings();
        this.currentImageIndex = 0;
    }

    public String getExec() {
        return exec;
    }

    public void setExec(String exec) {
        this.exec = exec;
    }

    public boolean isConnectToCloud() {
        return connectToCloud;
    }

    public void setConnectToCloud(boolean connectToCloud) {
        this.connectToCloud = connectToCloud;
    }

    public String getFlashImages() {
        return flashImages;
    }

    public void setFlashImages(String flashImages) {
        this.flashImages = flashImages;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public int getCurrentImageIndex() {
        return currentImageIndex;
    }

    public void setCurrentImageIndex(int currentImageIndex) {
        this.currentImageIndex = currentImageIndex;
    }
}
