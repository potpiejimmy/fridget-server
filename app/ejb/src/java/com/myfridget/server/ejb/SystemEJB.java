/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.util.Utils;
import com.myfridget.server.util.WebRequester;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 *
 * @author thorsten
 */
@Stateless
public class SystemEJB implements SystemEJBLocal {

    public final static String PARAMETER_FIRMWARE_VERSION = "firmware.version";
    public final static String PARAMETER_FIRMWARE_PATH = "firmware.path";
    public final static String PARAMETER_FIRMWARE_FLASHURL = "firmware.flashurl";
    
    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    @EJB
    protected AdDeviceEJBLocal deviceEjb;
    
    @Override
    public List<SystemParameter> getSystemParameters() {
        return em.createNamedQuery("SystemParameter.findAll", SystemParameter.class).getResultList();
    }
    
    @Override
    public SystemParameter getSystemParameter(String param) {
        return em.createNamedQuery("SystemParameter.findByParam", SystemParameter.class).setParameter("param", param).getSingleResult();
    }

    @Override
    public void setSystemParameter(SystemParameter param) {
        em.merge(param);
    }
    
    @Override
    public String flashFirmware(int adDeviceId) throws Exception {
        
        AdDevice device = deviceEjb.getById(adDeviceId);
        
        // URL
        String url = getSystemParameter(PARAMETER_FIRMWARE_FLASHURL).getValue();
        url += device.getSerial();
        
        // Firmware file:
        String firmwareVersion = getSystemParameter(PARAMETER_FIRMWARE_VERSION).getValue();
        String fileName = "core-firmware-"+device.getType()+"-"+firmwareVersion+".bin";
        File fwFile = new File(getSystemParameter(PARAMETER_FIRMWARE_PATH).getValue(), fileName);

        // Upload data:
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("file_type", "binary", ContentType.TEXT_PLAIN);
        builder.addBinaryBody("file", fwFile);
        
        WebRequester curl = new WebRequester();
        curl.setParam("access_token", deviceEjb.getParameter(adDeviceId, "accesstoken").getValue());
        
        return "Flashing " + fileName + ", result: " + curl.put(url, builder.build());
    }
}
