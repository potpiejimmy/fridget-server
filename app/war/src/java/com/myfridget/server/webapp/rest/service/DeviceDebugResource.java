/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.ejb.AdDeviceEJB;
import com.myfridget.server.ejb.CampaignsEJB;
import com.myfridget.server.ejb.SystemEJB;
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
    
    protected AdDeviceEJB deviceEjb = lookupEjb("java:app/Fridget_EJBs/AdDeviceEJB!com.myfridget.server.ejb.AdDeviceEJB");
    protected SystemEJB systemEjb = lookupEjb("java:app/Fridget_EJBs/SystemEJB!com.myfridget.server.ejb.SystemEJB");
    protected CampaignsEJB campaignsEjb = lookupEjb("java:app/Fridget_EJBs/CampaignsEJB!com.myfridget.server.ejb.CampaignsEJB");
    
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public String hello(String msg) throws IOException {
        msg = WebUtils.removeQuotes(msg);
        deviceEjb.addDebugMessage(serial, ">>>" + msg);
        
        // XXX voltage reporting for debugging, ignore
        if (msg.contains("voltage=")) {
            deviceEjb.addDebugMessage(serial, "<<< ok");
            return "ok";
        }
        
        AdDevice device = deviceEjb.getBySerial(serial);

        // verify client params string, flash firmware if necessary:
        systemEjb.verifyDeviceParams(device.getId(), msg);
        
        // send server params:
        StringBuilder result = new StringBuilder();
        AdDeviceParameter param = deviceEjb.getParameter(device.getId(), "exec");
        appendParam(result, param.getParam(), getDemoProgram(device.getId(), param.getValue()));
        param = deviceEjb.getParameter(device.getId(), "flashimages");
        appendParam(result, param.getParam(), param.getValue());
        param = deviceEjb.getParameter(device.getId(), "connectmode");
        appendParam(result, param.getParam(), param.getValue());
        SystemParameter firmware = systemEjb.getSystemParameter(SystemEJB.PARAMETER_FIRMWARE_VERSION);
        result.append("firmware").append('=').append(firmware.getValue()).append(';');
        
        deviceEjb.addDebugMessage(serial, "<<< " + result);
        return result.toString();
    }
    
    protected static void appendParam(StringBuilder result, String param, String value) {
        result.append(param).append('=').append(value).append(';');
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
    
    protected String getDemoProgram(int adDeviceId, String program) {
        // if a program is set in "exec" device configuration parameter, use it
        if (program!=null && program.length()>0) return program;
        // otherwise calculate a real campaign program
        return campaignsEjb.getProgramForDevice(adDeviceId);
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
