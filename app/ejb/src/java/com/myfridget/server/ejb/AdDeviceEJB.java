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
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author thorsten
 */
@Stateless
public class AdDeviceEJB implements AdDeviceEJBLocal {

    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    @Override
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
    
    @Override
    public AdDevice getBySerial(String deviceSerial) {
        try {
            return em.createNamedQuery("AdDevice.findBySerial", AdDevice.class).setParameter("serial", deviceSerial).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    @Override
    public AdDevice getById(int deviceId) {
        return em.find(AdDevice.class, deviceId);
    }

    @Override
    public List<AdDeviceDebugMsg> getDebugMessages(int deviceId) {
        return em.createNamedQuery("AdDeviceDebugMsg.findByAdDeviceId", AdDeviceDebugMsg.class).setParameter("adDeviceId", deviceId).getResultList();
    }
    
    @Override
    public List<AdDevice> getAllDevices() {
        return em.createNamedQuery("AdDevice.findAll", AdDevice.class).getResultList();
    }
    
    @Override
    public void clearDebugMessages(int deviceId) {
        getDebugMessages(deviceId).forEach(i->em.remove(i));
    }

    @Override
    public AdDeviceParameter getParameter(int deviceId, String param) {
        try {
            return em.createNamedQuery("AdDeviceParameter.findByAdDeviceIdAndParam", AdDeviceParameter.class).setParameter("adDeviceId", deviceId).setParameter("param", param).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
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
    
    @Override
    public void uploadTestImage(int deviceId, byte[] imgData) throws IOException {
        BufferedImage img = getResizedImage(imgData);
        byte[] epdData = encodeEPD(img);
        imgData = Utils.encodeImage(img, "png");
        
        AdDeviceTestImage testImage = new AdDeviceTestImage();
        testImage.setAdDeviceId(deviceId);
        em.persist(testImage);
        em.flush(); // fetch new ID
        
        writeCacheFile(testImage, "png", imgData);
        writeCacheFile(testImage, "epd", epdData);
    }
    
    protected static BufferedImage getResizedImage(byte[] imgData) {
        // XXX 400x300
        return Utils.getScaledBufferedImage(imgData, 400, 300);
    }
    
    protected static byte[] encodeEPD(BufferedImage img) {
        return EPDUtils.compressRLE(EPDUtils.makeSpectra3Color(img));
    }
    
    protected void writeCacheFile(AdDeviceTestImage img, String type, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(cacheFileForImage(img, type));
        fos.write(data);
        fos.close();
    }
    
    protected void removeDeviceTestImage(AdDeviceTestImage img) {
        if (img == null) return;
        cacheFileForImage(img, "png").delete();
        cacheFileForImage(img, "epd").delete();
        em.remove(img);
    }
    
    @Override
    public byte[] getTestImage(int deviceTestImageId) throws IOException {
        byte[] imgData = getTestImageData(deviceTestImageId, "epd");
        // XXX REMOVE THE FOLLOWING. RECREATE ON THE FLY (allow change of encoding by removing EPD files from cache)
        if (imgData == null) {
            // recreate from preview
            imgData = getTestImagePreview(deviceTestImageId);
            imgData = encodeEPD(getResizedImage(imgData));
            writeCacheFile(em.find(AdDeviceTestImage.class, deviceTestImageId), "epd", imgData);
        }
        return imgData;
    }

    @Override
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

    @Override
    public List<AdDeviceTestImage> getDeviceTestImages(int deviceId) {
        try {
            return em.createNamedQuery("AdDeviceTestImage.findByAdDeviceId", AdDeviceTestImage.class).setParameter("adDeviceId", deviceId).getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void removeTestImage(int deviceTestImageId) throws IOException {
        em.remove(em.find(AdDeviceTestImage.class, deviceTestImageId));
    }
    
    protected static File cacheFileForImage(AdDeviceTestImage img, String type) {
        return new File("fridgetcache_"+img.getId()+"."+type);
    }
}
