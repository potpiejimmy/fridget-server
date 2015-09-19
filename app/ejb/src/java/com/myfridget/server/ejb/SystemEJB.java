/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.util.WebRequester;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 *
 * @author thorsten
 */
@Stateless
public class SystemEJB {

    public final static String PARAMETER_FIRMWARE_VERSION = "firmware.version.";
    public final static String PARAMETER_FIRMWARE_PATH = "firmware.path";
    public final static String PARAMETER_FIRMWARE_FLASHURL = "firmware.flashurl";
    
    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    @Resource
    protected TimerService timerService;
    
    @EJB
    protected AdDeviceEJB deviceEjb;
    
    public List<SystemParameter> getSystemParameters() {
        return em.createNamedQuery("SystemParameter.findAll", SystemParameter.class).getResultList();
    }
    
    public SystemParameter getSystemParameter(String param) {
        try {
            return em.createNamedQuery("SystemParameter.findByParam", SystemParameter.class).setParameter("param", param).getSingleResult();
        } catch (NoResultException nre) {
            return null; // parameter not found
        }
    }

    public void setSystemParameter(SystemParameter param) {
        em.merge(param);
    }
    
    public String flashFirmware(int adDeviceId) {
        
        AdDevice device = deviceEjb.getById(adDeviceId);
        
        try {
            // URL
            String url = getSystemParameter(PARAMETER_FIRMWARE_FLASHURL).getValue();
            url += device.getSerial();

            // Firmware file:
            String firmwareVersion = getSystemParameter(PARAMETER_FIRMWARE_VERSION+device.getType()).getValue();
            String fileName = "core-firmware-"+device.getType()+"-"+firmwareVersion+".bin";
            File fwFile = new File(getSystemParameter(PARAMETER_FIRMWARE_PATH).getValue(), fileName);

            // Upload data:
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("file_type", "binary", ContentType.TEXT_PLAIN);
            builder.addBinaryBody("file", fwFile);

            try (WebRequester curl = new WebRequester()) {
                curl.setParam("access_token", deviceEjb.getParameter(adDeviceId, "accesstoken").getValue());

                String result = curl.put(url, builder.build());

                return "Flashing " + fileName + ", result: " + result;
            }
        } catch (Exception e) {
            return "Flashing firmware failed: " + e;
        }
    }
    
    public boolean verifyDeviceParams(int adDeviceId, String deviceParamsString) throws IOException {
        Properties deviceParams = new Properties();
        deviceParams.load(new StringReader(deviceParamsString.replace(',', '\n')));
        
        AdDevice device = deviceEjb.getById(adDeviceId);
        String serverFirmware = getSystemParameter(PARAMETER_FIRMWARE_VERSION+device.getType()).getValue();
        String deviceFirmware = deviceParams.getProperty("firmware");
        if (deviceFirmware!=null && !deviceFirmware.equals(serverFirmware))
            timerService.createSingleActionTimer(10000, new TimerConfig(adDeviceId, true));
        
        return true;
    }
    
    @Timeout
    public void timeout(final Timer timer) throws Exception {
        AdDevice device = deviceEjb.getById((Integer)timer.getInfo());
        deviceEjb.addDebugMessage(device.getSerial(), flashFirmware(device.getId()));
    }
    
    @PermitAll
    public int getAttinyCycleLength() {
        SystemParameter cycleLenParam = getSystemParameter("attiny.cycle.len");
        try {
            return Integer.parseInt(cycleLenParam.getValue());
        } catch (Exception e) {
            return 8870;  // use a default
        }
    }
}
