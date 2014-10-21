/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import com.myfridget.server.webapp.mbean.DeviceDebugBean;
import com.myfridget.server.webapp.util.WebUtils;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author thorsten
 */
@Path("/debug")
@RequestScoped
public class DeviceDebugResource {
    
    protected AdDeviceEJBLocal deviceEjb = lookupAdDeviceEJBLocal();
    
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public AdDeviceDebugMsg debug(@QueryParam("serial") String serial, String msg) {
        return deviceEjb.addDebugMessage(serial, WebUtils.removeQuotes(msg));
    }
    
    @GET
    @Produces({"application/json"})
    public String getParameter(@QueryParam("serial") String serial, @QueryParam("param") String param) {
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return ""; // unknown device
        AdDeviceParameter p = deviceEjb.getParameter(device.getId(), param);
        if (p == null) return ""; // parameter not set
        return p.getValue();
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
