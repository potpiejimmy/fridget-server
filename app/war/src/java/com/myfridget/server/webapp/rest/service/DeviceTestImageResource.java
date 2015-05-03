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
import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author thorsten
 */
@Path("/img/{serial}")
@RequestScoped
public class DeviceTestImageResource {
    
    @PathParam("serial")
    private String serial;
    
    protected AdDeviceEJBLocal deviceEjb = lookupEjb("java:app/Fridget_EJBs/AdDeviceEJB!com.myfridget.server.ejb.AdDeviceEJBLocal");
    protected AdMediumEJBLocal mediumEjb = lookupEjb("java:global/Fridget_EA/Fridget_EJBs/AdMediumEJB!com.myfridget.server.ejb.AdMediumEJBLocal");
    
    @GET
    @Produces({"application/binary"})
    public byte[] getData(@QueryParam("index") Integer index) throws IOException {
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return null; // unknown device
        List<AdDeviceTestImage> images = deviceEjb.getDeviceTestImages(device.getId());
        if (index !=null) {
            if (images == null || index < 0 || index >= images.size()) return null; // no image at that index
            return deviceEjb.getTestImage(images.get(index).getId());
        } else {
            // return the images listed in parameter "flashimages":
            AdDeviceParameter imgList = deviceEjb.getParameter(device.getId(), "flashimages");
            if (imgList == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte b : imgList.getValue().getBytes()) {
                int imgIndex = b - 65; //'A'
                byte[] imgData;
                AdDeviceParameter pImg = deviceEjb.getParameter(device.getId(), "p");
                if (imgIndex == 15 /*'P'*/ && pImg != null) {
                    imgData = mediumEjb.getMediumEPD(Integer.parseInt(pImg.getValue()), device.getType());
                } else {
                    if (images == null || imgIndex < 0 || imgIndex >= images.size()) continue; // no such image
                    imgData = deviceEjb.getTestImage(images.get(imgIndex).getId());
                }
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
    
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public Response uploadFile(String file) throws IOException
    {
        // quick and dirty hack for wolfram to upload 7.4" test images
        byte[] imgIn = Base64.getDecoder().decode(file);
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return null; // unknown device
        List<AdDeviceTestImage> images = deviceEjb.getDeviceTestImages(device.getId());
        if (images.size()>0) deviceEjb.removeTestImage(images.get(images.size()-1).getId());
        deviceEjb.uploadTestImage(device.getId(), EPDUtils.SPECTRA_DISPLAY_TYPE_74, imgIn);
        return Response.ok("File uploaded successfully.\n").build();
    }
    
    private static <T> T lookupEjb(String name) {
        try {
            Context c = new InitialContext();
            return (T) c.lookup(name);
        } catch (NamingException ne) {
            Logger.getLogger(DeviceTestImageResource.class.getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}
