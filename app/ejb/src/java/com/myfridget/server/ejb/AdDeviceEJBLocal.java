/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface AdDeviceEJBLocal {
    
    public AdDeviceDebugMsg addDebugMessage(String deviceSerial, String message);

    public List<AdDeviceDebugMsg> getDebugMessages();
}
