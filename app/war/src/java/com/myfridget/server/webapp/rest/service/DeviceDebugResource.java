/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDeviceDebugMsg;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.RequestScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
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
    
    @GET
    @Produces({"application/xml","application/json"})
    public AdDeviceDebugMsg debug(@QueryParam("serial") String serial, @QueryParam("msg") String msg) {
        return deviceEjb.addDebugMessage(serial, msg);
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
