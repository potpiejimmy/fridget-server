/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.rest.service;

import com.myfridget.server.ejb.AdMediumEJBLocal;
import com.myfridget.server.util.EPDUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.IOException;
import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/media/{id}")
@RequestScoped
public class MediumResource {
 
    @EJB
    protected AdMediumEJBLocal mediumEjb;
    
    @POST
    @Consumes({MediaType.TEXT_PLAIN})
    public Response uploadFile(@PathParam("id") Integer adMediumId, String file) throws IOException
    {
        byte[] image = mediumEjb.convertImage(Base64.decode(file), EPDUtils.SPECTRA_DISPLAY_TYPE_441);
        
        mediumEjb.setMediumPreview(adMediumId, EPDUtils.SPECTRA_DISPLAY_TYPE_441, image);
        
        String output = "File uploaded via Jersey based RESTFul Webservice to ";
 
        return Response.status(200).entity(output).build();
 
    }

}
