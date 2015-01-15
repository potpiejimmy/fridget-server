/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.AdDeviceTestImage;
import com.myfridget.server.db.entity.User;
import java.io.IOException;
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
    
    public List<AdDevice> getDevicesForUser(int userId);
    
    public List<User> getAssignedUsers(int deviceId);
    
    public AdDevice getBySerial(String deviceSerial);
    
    public AdDevice getById(int deviceId);
    
    public List<AdDeviceParameter> getParameters(int deviceId);

    public AdDeviceParameter getParameter(int deviceId, String param);

    public void setParameter(AdDeviceParameter param);
    
    public void uploadTestImage(int deviceId, int displayType, byte[] imgData) throws IOException;
    
    public byte[] getTestImage(int deviceTestImageId) throws IOException;

    public byte[] getTestImagePreview(int deviceTestImageId) throws IOException;
    
    public List<AdDeviceTestImage> getDeviceTestImages(int deviceId);
    
    public void removeTestImage(int deviceTestImageId) throws IOException;
    
    public void saveDevice(AdDevice device, List<User> assignedUsers);
}
