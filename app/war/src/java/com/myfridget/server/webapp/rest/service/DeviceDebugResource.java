/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import com.myfridget.server.ejb.SystemEJB;
import com.myfridget.server.ejb.SystemEJBLocal;
import com.myfridget.server.webapp.util.WebUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.RequestScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author thorsten
 */
@Path("/debug/{serial}")
@RequestScoped
public class DeviceDebugResource {
    
    @PathParam("serial")
    private String serial;
    
    protected AdDeviceEJBLocal deviceEjb = lookupEjb("java:app/Fridget_EJBs/AdDeviceEJB!com.myfridget.server.ejb.AdDeviceEJBLocal");
    protected SystemEJBLocal systemEjb = lookupEjb("java:global/Fridget_EA/Fridget_EJBs/SystemEJB!com.myfridget.server.ejb.SystemEJBLocal");
    
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public String hello(String msg) throws IOException {
        msg = WebUtils.removeQuotes(msg);
        deviceEjb.addDebugMessage(serial, ">>>" + msg);

        // verify client params string, flash firmware if necessary:
        systemEjb.verifyDeviceParams(deviceEjb.getBySerial(serial).getId(), msg);
        
        // send server params:
        StringBuilder result = new StringBuilder();
        for (AdDeviceParameter param : deviceEjb.getParameters(deviceEjb.getBySerial(serial).getId())) {
            if (!"accesstoken".equals(param.getParam()))
                result.append(param.getParam()).append('=').append(param.getValue()).append(';');
        }
        SystemParameter firmware = systemEjb.getSystemParameter(SystemEJB.PARAMETER_FIRMWARE_VERSION);
        result.append("firmware").append('=').append(firmware.getValue()).append(';');
        
        deviceEjb.addDebugMessage(serial, "<<< " + result);
        return result.toString();
    }
    
    /**
     * This is for testing only.
     * @return result message
     */
    @GET
    @Produces("text/plain")
    public String flashFirmware() throws Exception {
        return systemEjb.flashFirmware(deviceEjb.getBySerial(serial).getId());
    }
    
    private static <T> T lookupEjb(String name) {
        try {
            Context c = new InitialContext();
            return (T) c.lookup(name);
        } catch (NamingException ne) {
            Logger.getLogger(DeviceDebugResource.class.getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}
