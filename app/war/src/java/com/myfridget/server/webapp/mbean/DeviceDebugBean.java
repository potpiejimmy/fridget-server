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
    
    @EJB
    private AdDeviceEJBLocal deviceEjb;
    
    private Integer selectedDevice = null;

    public static int DEFAULT_WAKE_TIME = 5;
    public static int DEFAULT_SLEEP_TIME = 30;
    
    @Min(1) @Max(60)
    private int wakeTime = 0;
    @Min(1) @Max(60)
    private int sleepTime = 0;

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
            sleepTime= 0;
        } else {
            AdDeviceParameter param = deviceEjb.getParameter(selectedDevice, "waketime");
            wakeTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_WAKE_TIME;
            param = deviceEjb.getParameter(selectedDevice, "sleeptime");
            sleepTime = (param != null) ? Integer.parseInt(param.getValue()) : DEFAULT_SLEEP_TIME;
        }
    }
    
    protected void writeDeviceSettings() {
        if (selectedDevice == null) return;
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "waketime", ""+wakeTime));
        deviceEjb.setParameter(new AdDeviceParameter(null, selectedDevice, "sleeptime", ""+sleepTime));
    }
    
    public void save() {
        writeDeviceSettings();
    }
    
    public void clearLog() {
        deviceEjb.clearDebugMessages(selectedDevice!=null ? selectedDevice : 0);
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
    
}
