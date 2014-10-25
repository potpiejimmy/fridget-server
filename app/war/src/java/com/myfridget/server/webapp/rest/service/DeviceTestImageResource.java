/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.RequestScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author thorsten
 */
@Path("/img/{serial}")
@RequestScoped
public class DeviceTestImageResource {
    
    @PathParam("serial")
    private String serial;
    
    protected AdDeviceEJBLocal deviceEjb = lookupAdDeviceEJBLocal();
    
    @GET
    @Produces({"application/binary"})
    public byte[] getData(@QueryParam("pos") Integer pos, @QueryParam("len") Integer len) throws IOException {
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return null; // unknown device
        byte[] data = deviceEjb.getTestImage(device.getId());
        if (data == null) return null;
        if (pos == null || len == null) return data;
        byte[] result = new byte[len];
        System.arraycopy(data, pos, result, 0, len);
        return result;
    }
    
    
    private AdDeviceEJBLocal lookupAdDeviceEJBLocal() {
        try {
            Context c = new InitialContext();
            return (AdDeviceEJBLocal) c.lookup("java:global/Fridget_EA/Fridget_EJBs/AdDeviceEJB!com.myfridget.server.ejb.AdDeviceEJBLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}
