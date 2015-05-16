/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdMedium;
import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import java.io.IOException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface AdMediumEJBLocal {
    
    public List<AdMedium> getMediaForCurrentUser();

    public byte[] convertImage(byte[] imgData, int displayType) throws IOException;
    
    public void setMediumPreview(int adMediumId, int displayType, AdMediumPreviewImageData data) throws IOException;
    
    public AdMediumPreviewImageData getMediumPreview(int adMediumId, int displayType) throws IOException;
    
    public byte[] getMediumEPD(int adMediumId, int displayType) throws IOException;
    
    public AdMedium saveMedium(AdMedium medium);
    
    public void deleteMedium(int mediumId);
    
    public List<AdMediumItem> getAllMediumItems();
    
    public AdMedium findAdMedium(int adMediumId);
}
