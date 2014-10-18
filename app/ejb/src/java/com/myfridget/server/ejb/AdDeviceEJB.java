/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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
        msg.setDate(new java.util.Date());
        msg.setMessage(message);
        em.persist(msg);
        em.flush();
        return msg;
    }

    @Override
    public List<AdDeviceDebugMsg> getDebugMessages() {
        //return em.createNamedQuery("AdDeviceDebugMsg.findByAdDeviceId", AdDeviceDebugMsg.class).setParameter("adDeviceId", 0).getResultList();
        return em.createNamedQuery("AdDeviceDebugMsg.findAll", AdDeviceDebugMsg.class).getResultList();
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
}
