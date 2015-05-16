/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.util.HuffmanCompression;
import com.myfridget.server.util.Utils;
import com.myfridget.server.vo.AdMediumPreviewImageData;
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
    
    protected void removeMediumItemsForMedium(AdMedium medium) {
        for (AdMediumItem item : em.createNamedQuery("AdMediumItem.findByMedium", AdMediumItem.class).setParameter("adMediumId", medium.getId()).getResultList()) {
            cacheFileForImage(item, "png").delete();
            em.remove(item);
        }
    }
    
    @Override
    public void deleteMedium(int mediumId) {
        AdMedium medium = em.find(AdMedium.class, mediumId);
        removeMediumItemsForMedium(medium);
        em.remove(medium);
    }

    @Override
    public byte[] convertImage(byte[] imgData, int displayType) throws IOException {
        BufferedImage img = EPDUtils.getResizedImageForDisplay(imgData, displayType);
        EPDUtils.makeSpectra3Color(img); // Note: converts "img" to 3 colors
        //img = Utils.setImageOrientation(img, false); // force landscape mode
        return Utils.encodeImage(img, "png"); // encode PNG
    }
    
    @Override
    public void setMediumPreview(int adMediumId, int displayType, AdMediumPreviewImageData data) throws IOException {
        AdMediumItem item = findMediumItem(adMediumId, displayType);
        if (item == null) {
            // not found, create a new one
            item = new AdMediumItem();
            item.setAdMediumId(adMediumId);
            item.setType((short)displayType);
            em.persist(item);
            em.flush(); // pre-fetch ID
        }
        item.setGentype(data.gentype);
        Utils.writeFile(cacheFileForImage(item, "png"), data.data);
    }
    
    @Override
    public AdMediumPreviewImageData getMediumPreview(int adMediumId, int displayType) throws IOException {
        AdMediumItem img = findMediumItem(adMediumId, displayType);
        if (img == null) return null; // not found
        File file = cacheFileForImage(img, "png");
        if (!file.exists()) return null;
        return new AdMediumPreviewImageData(Utils.readAll(new FileInputStream(file)), img.getGentype());
    }    
    
    @Override
    public byte[] getMediumEPD(int adMediumId, int displayType) throws IOException {
        byte[] preview = getMediumPreview(adMediumId, displayType).data; // encode from preview
        if (preview == null) return null; // not found
        return HuffmanCompression.compress(EPDUtils.compressRLE(EPDUtils.makeSpectra3Color(EPDUtils.getResizedImageForDisplay(preview, displayType))));
    }
    
    protected AdMediumItem findMediumItem(int adMediumId, int displayType) {
        try {
            return em.createNamedQuery("AdMediumItem.findByMediumAndType", AdMediumItem.class).setParameter("adMediumId", adMediumId).setParameter("type", displayType).getSingleResult();
        } catch (Exception ex) {
            return null; // not found
        }
    }
    
    @Override
    public List<AdMediumItem> getAllMediumItems() {
        return em.createNamedQuery("AdMediumItem.findAll", AdMediumItem.class).getResultList();
    }
    
    @Override
    public AdMedium findAdMedium(int adMediumId) {
        return em.find(AdMedium.class, adMediumId);
    }

    protected static File cacheFileForImage(AdMediumItem item, String type) {
        return new File("fridget_item_"+item.getId()+"."+type);
    }
}
