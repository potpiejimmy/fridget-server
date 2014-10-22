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

    public static int DEFAULT_WAKE_TIME = 5;
    public static int DEFAULT_SLEEP_TIME = 30;
    
    @Min(1) @Max(120)
    private int wakeTime = 0;
    @Min(1) @Max(3600)
    private int sleepTime = 0;
    private boolean connectToCloud = false;
    
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
        if (selectedDevice == null) {
            wakeTime = 0;
            sleepTime = 0;
            connectToCloud = false;
        } else {
            AdDeviceParameter param = deviceEjb.getParameter(selectedDevice, "waketime");
            wakeTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_WAKE_TIME;
            param = deviceEjb.getParameter(selectedDevice, "sleeptime");
            sleepTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_SLEEP_TIME;
            param = deviceEjb.getParameter(selectedDevice, "connectmode");
            connectToCloud = (param != null) ? "1".equals(param.getValue()) : true;
        }
    }
    
    protected void writeDeviceSettings() {
        if (selectedDevice == null) return;
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "waketime", ""+wakeTime));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "sleeptime", ""+sleepTime));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "connectmode", connectToCloud ? "1" : "2"));
    }
    
    public void save() {
        writeDeviceSettings();
    }
    
    public void clearLog() {
        deviceEjb.clearDebugMessages(selectedDevice!=null ? selectedDevice : 0);
    }
    
    public String getFormattedDate(Long date) {
        return DATE_FORMAT.format(new java.util.Date(date));
    }

    public Integer getSelectedDevice() {
        return selectedDevice;
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

    public boolean isConnectToCloud() {
        return connectToCloud;
    }

    public void setConnectToCloud(boolean connectToCloud) {
        this.connectToCloud = connectToCloud;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
    
}
