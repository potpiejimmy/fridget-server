/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class DeviceDebugBean {
    
    @EJB
    private AdDeviceEJBLocal deviceEjb;

    public String getSayHello() {
        return "Say Hello";
    }
    
    public List<AdDeviceDebugMsg> getDebugMessages() {
        List<AdDeviceDebugMsg> msgs = deviceEjb.getDebugMessages();
        return msgs;
    }
    
}
