/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.db.entity.AdDeviceParameter;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface AdDeviceEJBLocal {
    
    public AdDeviceDebugMsg addDebugMessage(String deviceSerial, String message);

    public List<AdDeviceDebugMsg> getDebugMessages(int deviceId);
    
    public void clearDebugMessages(int deviceId);
    
    public List<AdDevice> getAllDevices();
    
    public AdDevice getBySerial(String deviceSerial);
    
    public AdDeviceParameter getParameter(int deviceId, String param);

    public void setParameter(AdDeviceParameter param);
}
