/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdDeviceTestImage;
import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author thorsten
 */
@Stateless
public class AdMediumEJB implements AdMediumEJBLocal {

    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    @EJB
    private UsersEJBLocal usersEjb;
    
    @Override
    public List<AdMedium> getMediaForCurrentUser() {
        return null;
    }
    
    @Override
    public void uploadImage(int displayType, byte[] imgData) throws IOException {
        if (displayType < 0)
            handleImageUploads(imgData, EPDUtils.SPECTRA_DISPLAY_DEFAULT_TYPES);
        else
            handleImageUploads(imgData, displayType);
    }
    
    protected void handleImageUploads(byte[] imgData, int... displayTypes) throws IOException {
        for (int displayType : displayTypes)
            handleImageUpload(imgData, displayType);
    }
    protected void handleImageUpload(byte[] imgData, int displayType) throws IOException {
        BufferedImage img = EPDUtils.getResizedImageForDisplay(imgData, displayType);
        EPDUtils.makeSpectra3Color(img); // Note: converts "img" to 3 colors
        imgData = Utils.encodeImage(img, "png"); // encode PNG
        
        AdMediumItem item = new AdMediumItem();
        //item.setAdMediumId(0);
        item.setType((short)displayType);
        em.persist(item);
        em.flush(); // fetch new ID
        
        Utils.writeFile(cacheFileForImage(item, "png"), imgData);
    }
    
    @Override
    public byte[] getMediumPreview(int adMediumId, int mediumType) throws IOException {
        return null;
    }    
    
    protected static File cacheFileForImage(AdMediumItem item, String type) {
        return new File("fridget_item_"+item.getId()+"."+type);
    }
}
