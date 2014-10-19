/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

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
    
    public void clearLog() {
        deviceEjb.clearDebugMessages(selectedDevice!=null ? selectedDevice : 0);
    }

    public Integer getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(Integer selectedDevice) {
        this.selectedDevice = selectedDevice;
    }
    
}
