/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdDevice;
import com.myfridget.server.db.entity.AdDeviceParameter;
import com.myfridget.server.db.entity.AdDeviceTestImage;
import com.myfridget.server.ejb.AdDeviceEJB;
import com.myfridget.server.ejb.AdMediumEJB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
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
    
    @EJB
    protected AdDeviceEJB deviceEjb;
    
    @EJB
    protected AdMediumEJB mediumEjb;
    
    @GET
    @Produces({"application/binary"})
    public byte[] getData(@QueryParam("index") Integer index) throws IOException {
        AdDevice device = deviceEjb.getBySerial(serial);
        if (device == null) return null; // unknown device
        
        AdDeviceParameter exec = deviceEjb.getParameter(device.getId(), "exec");
        boolean testData = exec != null && exec.getValue() != null && exec.getValue().length() > 0;
        
        List<AdDeviceTestImage> images = deviceEjb.getDeviceTestImages(device.getId());
        if (index !=null) {
            // note: index support only for test images
            if (images == null || index < 0 || index >= images.size()) return null; // no image at that index
            return deviceEjb.getTestImage(images.get(index).getId());
        } else {
            // return the images listed in parameter "flashimages":
            AdDeviceParameter imgList = deviceEjb.getParameter(device.getId(), "flashimages");
            if (imgList == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (char c : imgList.getValue().toCharArray()) {
                String image = Character.valueOf(c).toString();
                int imgIndex = image.getBytes()[0] - 65; //'A'
                byte[] imgData;
                if (testData) {
                    if (images == null || imgIndex < 0 || imgIndex >= images.size()) continue; // no such image
                    imgData = deviceEjb.getTestImage(images.get(imgIndex).getId());
                } else {
                    AdDeviceParameter pImg = deviceEjb.getParameter(device.getId(), "p");
                    Properties imageMap = new Properties();
                    try {imageMap.load(new StringReader(pImg.getValue()));} catch (Exception e) {}
                    if (!imageMap.containsKey(image)) continue; // unknown image
                    imgData = mediumEjb.getMediumEPD(Integer.parseInt(imageMap.getProperty(image)), device.getType() & 0x0f);
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
}
