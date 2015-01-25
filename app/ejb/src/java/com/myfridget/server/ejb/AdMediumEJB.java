/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.AdMedium;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author thorsten
 */
@Stateless
public class AdMediumEJB implements AdMediumEJBLocal {

    @EJB
    private UsersEJBLocal usersEjb;
    
    @Override
    public List<AdMedium> getMediaForCurrentUser() {
        return null;
    }
    
    @Override
    public void uploadImage(int displayType, byte[] imgData) throws IOException {
        
    }
    
    @Override
    public byte[] getMediumPreview(int adMediumId, int mediumType) throws IOException {
        return null;
    }    
}
