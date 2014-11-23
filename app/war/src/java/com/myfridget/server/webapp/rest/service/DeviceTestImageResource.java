/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.AdDeviceTestImage;
import com.myfridget.server.ejb.AdDeviceEJBLocal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
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
    public byte[] getData(@QueryParam("index") Integer index) throws IOException {
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return null; // unknown device
        List<AdDeviceTestImage> images = deviceEjb.getDeviceTestImages(device.getId());
        if (images == null) return null; // no images;
        if (index !=null) {
            if (index < 0 || index >= images.size()) return null; // no image at that index
            return deviceEjb.getTestImage(images.get(index).getId());
        } else {
            // return the images listed in parameter "flashimages":
            AdDeviceParameter imgList = deviceEjb.getParameter(device.getId(), "flashimages");
            if (imgList == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte b : imgList.getValue().getBytes()) {
                int imgIndex = b - 65; //'A'
                if (imgIndex < 0 || imgIndex >= images.size()) continue; // no such image
                byte[] imgData = deviceEjb.getTestImage(images.get(imgIndex).getId());
                int size = imgData.length;
                // write one byte image index
                baos.write(imgIndex);
                // write two bytes of length
                baos.write((size&0xff00)>>8);
                baos.write((size&0xff));
                // write image data
                baos.write(imgData);
            }
            baos.close();
            return baos.toByteArray();
        }
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
