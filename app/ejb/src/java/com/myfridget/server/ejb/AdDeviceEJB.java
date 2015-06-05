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
import com.myfridget.server.db.entity.UserAdDevice;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.HuffmanCompression;
import com.myfridget.server.util.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author thorsten
 */
@Stateless
public class AdDeviceEJB {

    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    public AdDeviceDebugMsg addDebugMessage(String deviceSerial, String message) {
        AdDevice adDevice = findAdDeviceBySerial(deviceSerial);
        if (adDevice == null) {
            adDevice = createNewAdDevice(deviceSerial);
        }
        
        AdDeviceDebugMsg msg = new AdDeviceDebugMsg();
        msg.setAdDeviceId(adDevice.getId());
        msg.setDate(System.currentTimeMillis());
        msg.setMessage(message);
        em.persist(msg);
        em.flush();
        return msg;
    }
    
    public AdDevice getBySerial(String deviceSerial) {
        try {
            return em.createNamedQuery("AdDevice.findBySerial", AdDevice.class).setParameter("serial", deviceSerial).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    public AdDevice getById(int deviceId) {
        return em.find(AdDevice.class, deviceId);
    }

    public List<AdDeviceDebugMsg> getDebugMessages(int deviceId) {
        return em.createNamedQuery("AdDeviceDebugMsg.findByAdDeviceId", AdDeviceDebugMsg.class).setParameter("adDeviceId", deviceId).getResultList();
    }
    
    public List<AdDevice> getAllDevices() {
        return em.createNamedQuery("AdDevice.findAll", AdDevice.class).getResultList();
    }

    public List<AdDevice> getDevicesForUser(int userId) {
        return em.createNamedQuery("AdDevice.findByUserId", AdDevice.class).setParameter("userId", userId).getResultList();
    }
    
    public List<User> getAssignedUsers(int deviceId) {
        return em.createNamedQuery("User.findByDeviceId", User.class).setParameter("adDeviceId", deviceId).getResultList();
    }

    public void clearDebugMessages(int deviceId) {
        getDebugMessages(deviceId).forEach(i->em.remove(i));
    }
    
    public List<AdDeviceParameter> getParameters(int deviceId) {
        return em.createNamedQuery("AdDeviceParameter.findByAdDeviceId", AdDeviceParameter.class).setParameter("adDeviceId", deviceId).getResultList();
    }

    public AdDeviceParameter getParameter(int deviceId, String param) {
        try {
            return em.createNamedQuery("AdDeviceParameter.findByAdDeviceIdAndParam", AdDeviceParameter.class).setParameter("adDeviceId", deviceId).setParameter("param", param).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void setParameter(AdDeviceParameter param) {
        AdDeviceParameter storedParam = getParameter(param.getAdDeviceId(), param.getParam());
        if (storedParam != null) {
            // if already stored, just update:
            storedParam.setValue(param.getValue());
        } else {
            // else, create a new one:
            param.setId(null);
            em.persist(param);
        }
    }
    
    public AdDevice findAdDeviceBySerial(String deviceSerial) {
        try {
            return em.createNamedQuery("AdDevice.findBySerial", AdDevice.class).setParameter("serial", deviceSerial).getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }
    
    protected AdDevice createNewAdDevice(String deviceSerial) {
        AdDevice adDevice = new AdDevice();
        adDevice.setSerial(deviceSerial);
        adDevice.setLat(BigDecimal.ONE);
        adDevice.setLon(BigDecimal.ONE);
        em.persist(adDevice);
        em.flush();
        return adDevice;
    }
    
    public void saveDevice(AdDevice device, List<User> assignedUsers) {
        // update entity
        em.merge(device);
        // delete previous user assignments
        em.createNamedQuery("UserAdDevice.deleteByAdDeviceId").setParameter("adDeviceId", device.getId()).executeUpdate();
        // save new user assignements
        for (User user : assignedUsers) {
            UserAdDevice assignment = new UserAdDevice();
            assignment.setAdDeviceId(device.getId());
            assignment.setUserId(user.getId());
            em.persist(assignment);
        }
    }
    
    public void uploadTestImage(int deviceId, int displayType, byte[] imgData) throws IOException {
        if (displayType < 0) throw new RuntimeException("Sorry, invalid display size for test upload: "+displayType);

        BufferedImage img = EPDUtils.getResizedImageForDisplay(imgData, displayType);
        byte[] epdData = encodeEPD(img); // Note: converts "img" to 3 colors
        imgData = Utils.encodeImage(img, "png");
        
        AdDeviceTestImage testImage = new AdDeviceTestImage();
        testImage.setAdDeviceId(deviceId);
        testImage.setOrderIndex(0x7FFF);
        em.persist(testImage);
        em.flush(); // fetch new ID
        
        Utils.writeFile(cacheFileForImage(testImage, "png"), imgData);
        Utils.writeFile(cacheFileForImage(testImage, "epd"), epdData);
    }
    
    protected static byte[] encodeEPD(BufferedImage img) throws IOException {
        return HuffmanCompression.compress(EPDUtils.compressRLE(EPDUtils.makeSpectra3Color(img)));
    }
    
    protected void removeDeviceTestImage(AdDeviceTestImage img) {
        if (img == null) return;
        cacheFileForImage(img, "png").delete();
        cacheFileForImage(img, "epd").delete();
        em.remove(img);
    }
    
    public byte[] getTestImage(int deviceTestImageId) throws IOException {
        byte[] imgData = getTestImageData(deviceTestImageId, "epd");
        // XXX REMOVE THE FOLLOWING. RECREATE ON THE FLY (allow change of encoding by removing EPD files from cache)
        if (imgData == null) {
            // recreate from preview
            imgData = getTestImagePreview(deviceTestImageId);
            imgData = encodeEPD(EPDUtils.getResizedImageForDisplay(imgData, EPDUtils.SPECTRA_DISPLAY_TYPE_441)); // XXX WARNING REENCODING ALWAYS FOR 4.41, must persist display type
            Utils.writeFile(cacheFileForImage(em.find(AdDeviceTestImage.class, deviceTestImageId), "epd"), imgData);
        }
        return imgData;
    }

    public byte[] getTestImagePreview(int deviceTestImageId) throws IOException {
        return getTestImageData(deviceTestImageId, "png");
    }

    protected byte[] getTestImageData(int deviceTestImageId, String format) throws IOException {
        AdDeviceTestImage img = em.find(AdDeviceTestImage.class, deviceTestImageId);
        if (img == null) return null;
        File file = cacheFileForImage(img, format);
        if (!file.exists()) return null;
        return Utils.readAll(new FileInputStream(file));
    }

    public List<AdDeviceTestImage> getDeviceTestImages(int deviceId) {
        try {
            return em.createNamedQuery("AdDeviceTestImage.findByAdDeviceId", AdDeviceTestImage.class).setParameter("adDeviceId", deviceId).getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
    public void removeTestImage(int deviceTestImageId) throws IOException {
        em.remove(em.find(AdDeviceTestImage.class, deviceTestImageId));
    }
    
    protected static File cacheFileForImage(AdDeviceTestImage img, String type) {
        return new File("fridgetcache_"+img.getId()+"."+type);
    }
}
