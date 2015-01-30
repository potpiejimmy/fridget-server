/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
        return em.createNamedQuery("AdMedium.findByUserId", AdMedium.class).setParameter("userId", usersEjb.getCurrentUser().getId()).getResultList();
    }
    
    @Override
    public AdMedium saveMedium(AdMedium medium) {
        if (medium.getId() == null) {
            medium.setUserId(usersEjb.getCurrentUser().getId());
            em.persist(medium);
        } else {
            em.merge(medium);
        }
        return medium;
    }
    
    @Override
    public void deleteMedium(int mediumId) {
        em.remove(em.find(AdMedium.class, mediumId));
    }

    @Override
    public byte[] convertImage(byte[] imgData, int displayType) throws IOException {
        BufferedImage img = EPDUtils.getResizedImageForDisplay(imgData, displayType);
        EPDUtils.makeSpectra3Color(img); // Note: converts "img" to 3 colors
        return Utils.encodeImage(img, "png"); // encode PNG
    }
    
    @Override
    public void setMediumPreview(AdMedium medium, byte[] imgData, int displayType) throws IOException {
        AdMediumItem item = new AdMediumItem();
        item.setAdMediumId(medium.getId());
        item.setType((short)displayType);
        em.persist(item);
        
        Utils.writeFile(cacheFileForImage(item, "png"), imgData);
    }
    
    @Override
    public byte[] getMediumPreview(int adMediumId, int displayType) throws IOException {
        AdMediumItem img = null;
        try {
            img = em.createNamedQuery("AdMediumItem.findByMediumAndType", AdMediumItem.class).setParameter("adMediumId", adMediumId).setParameter("type", displayType).getSingleResult();
        } catch (Exception ex) {
            return null; // not found
        }
        File file = cacheFileForImage(img, "png");
        if (!file.exists()) return null;
        return Utils.readAll(new FileInputStream(file));
    }    
    
    protected static File cacheFileForImage(AdMediumItem item, String type) {
        return new File("fridget_item_"+item.getId()+"."+type);
    }
}
