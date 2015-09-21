/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.util;

import java.io.IOException;
import java.security.Principal;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Contains static utility methods.
 */
public class WebUtils
{
    public static String getCurrentPerson()
    {
        return getCurrentPerson(getHttpServletRequest());
    }

    public static String getCurrentPerson(HttpServletRequest hsr)
    {
        Principal p = hsr.getUserPrincipal();
        return (p==null ? null : p.getName());
    }

    public static String removeQuotes(String in)
    {
        if (in.startsWith("\"") || in.startsWith("'"))
            in = in.substring(1, in.length()-1);
        return in;
    }
    
    public static void addFacesMessage(String msg)
    {
        addFacesMessage(msg, null);
    }

    public static void addFacesMessage(String msg, String clientId)
    {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(msg));
    }

    public static void addFacesMessage(Throwable ex)
    {
        while(ex.getCause() != null) ex = ex.getCause();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.toString(), "");
    	FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }
    
    public static HttpServletRequest getHttpServletRequest()
    {
        return (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }
    
    public static HttpServletResponse getHttpServletResponse()
    {
        return (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
    }
    
    /**
     * Redirect to the given URL (context root relative)
     * @param relativeUrl url relative to context path, must start with a slash
     */
    public static void redirect(String relativeUrl) throws IOException
    {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        ctx.redirect(ctx.getRequestContextPath() + relativeUrl);
    }
    
}
