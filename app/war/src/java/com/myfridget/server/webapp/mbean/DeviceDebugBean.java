/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
public class DeviceDebugBean {
    
    protected final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @EJB
    private AdDeviceEJBLocal deviceEjb;
    
    private Integer selectedDevice = null;
    
    private UploadedFile originalImage = null;

    public static int DEFAULT_WAKE_TIME = 5;
    public static int DEFAULT_SLEEP_TIME = 30;
    
    @Min(1) @Max(120)
    private int wakeTime = 0;
    @Min(1) @Max(3600)
    private int sleepTime = 0;
    @Min(1) @Max(50)
    private int connectCycle = 0;
    private boolean connectToCloud = false;
    private boolean flashImage = false;
    
    private boolean autoUpdate = false;

    public String getSayHello() {
        return "Say Hello";
    }
    
    public List<AdDeviceDebugMsg> getDebugMessages() {
        List<AdDeviceDebugMsg> msgs = deviceEjb.getDebugMessages(selectedDevice!=null ? selectedDevice : 0);
        return msgs;
    }
    
    public List<SelectItem> getDevicesSelectItems() {
        List<AdDevice> devices = deviceEjb.getAllDevices();
        List<SelectItem> items = new ArrayList<>();
        items.add(new SelectItem(null, "<Select Device>"));
        devices.forEach(i -> items.add(new SelectItem(i.getId(), "#"+i.getSerial())));
        return items;
    }
    
    protected void readDeviceSettings() {
        this.originalImage = null; // holds uploaded image temporarily only
        if (selectedDevice == null) {
            wakeTime = 0;
            sleepTime = 0;
            connectCycle = 0;
            connectToCloud = false;
            flashImage = false;
        } else {
            AdDeviceParameter param = deviceEjb.getParameter(selectedDevice, "waketime");
            wakeTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_WAKE_TIME;
            param = deviceEjb.getParameter(selectedDevice, "sleeptime");
            sleepTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_SLEEP_TIME;
            param = deviceEjb.getParameter(selectedDevice, "connectcycle");
            connectCycle = (param != null) ? Integer.parseInt(param.getValue()) : 1;
            param = deviceEjb.getParameter(selectedDevice, "connectmode");
            connectToCloud = (param != null) ? "1".equals(param.getValue()) : true;
            param = deviceEjb.getParameter(selectedDevice, "flashimage");
            flashImage = (param != null) ? "1".equals(param.getValue()) : false;
        }
    }
    
    protected void writeDeviceSettings() {
        if (selectedDevice == null) return;
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "waketime", ""+wakeTime));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "sleeptime", ""+sleepTime));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "connectcycle", ""+connectCycle));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "connectmode", connectToCloud ? "1" : "2"));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "flashimage", flashImage ? "1" : "0"));
    }
    
    public void save() {
        writeDeviceSettings();
    }
    
    public void clearLog() {
        deviceEjb.clearDebugMessages(selectedDevice!=null ? selectedDevice : 0);
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        try {
            originalImage = event.getFile();
            deviceEjb.uploadTestImage(selectedDevice, Utils.readAll(originalImage.getInputstream()));
            WebUtils.addFacesMessage("File " + event.getFile().getFileName() + " uploaded successfully.");  
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public StreamedContent getImage() throws IOException {
        if (selectedDevice == null) return null;
        byte[] image = deviceEjb.getTestImagePreview(selectedDevice);
        if (image == null) return null;
        return new DefaultStreamedContent(new ByteArrayInputStream(image), "image/png");
    }
    
    public StreamedContent getOriginalImage() throws IOException {
        if (originalImage == null) return null;
        return new DefaultStreamedContent(originalImage.getInputstream(), originalImage.getContentType());
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
    }

    public int getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(int wakeTime) {
        this.wakeTime = wakeTime;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getConnectCycle() {
        return connectCycle;
    }

    public void setConnectCycle(int connectCycle) {
        this.connectCycle = connectCycle;
    }

    public boolean isConnectToCloud() {
        return connectToCloud;
    }

    public void setConnectToCloud(boolean connectToCloud) {
        this.connectToCloud = connectToCloud;
    }

    public boolean isFlashImage() {
        return flashImage;
    }

    public void setFlashImage(boolean flashImage) {
        this.flashImage = flashImage;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
    
}
