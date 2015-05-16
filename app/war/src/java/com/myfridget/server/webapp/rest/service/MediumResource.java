/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.db.entity.AdMediumItem;
import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import com.myfridget.server.vo.AdMediumPreviewImageData;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.RequestScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/media/{id}")
@RequestScoped
public class MediumResource {
    
    protected AdMediumEJBLocal mediumEjb = lookupAdMediumEJBLocal();
 
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public Response uploadFile(@PathParam("id") Integer adMediumId, @QueryParam("type") Integer type, String file) throws IOException
    {
        byte[] imgIn = Base64.getDecoder().decode(file);
        
        if (type == null) for (int t : EPDUtils.SPECTRA_DISPLAY_DEFAULT_TYPES) setMediumPreview(adMediumId, t, imgIn);
        else setMediumPreview(adMediumId, type, imgIn);
 
        return Response.ok("File uploaded successfully.\n").build();
 
    }
    
    protected void setMediumPreview(int adMediumId, int type, byte[] imgIn) throws IOException {
        byte[] image = mediumEjb.convertImage(imgIn, type);
        
        mediumEjb.setMediumPreview(adMediumId, type, new AdMediumPreviewImageData(image, AdMediumItem.GENERATION_TYPE_MANUAL));
    }

    private AdMediumEJBLocal lookupAdMediumEJBLocal() {
        try {
            Context c = new InitialContext();
            return (AdMediumEJBLocal) c.lookup("java:app/Fridget_EJBs/AdMediumEJB!com.myfridget.server.ejb.AdMediumEJBLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

}
