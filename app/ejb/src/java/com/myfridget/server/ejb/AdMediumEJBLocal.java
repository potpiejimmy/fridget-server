/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdMedium;
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
    
    public void uploadImage(int displayType, byte[] imgData) throws IOException;
    
    public byte[] getMediumPreview(int adMediumId, int mediumType) throws IOException;
    
}