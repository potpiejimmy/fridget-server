/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.util;

import java.security.Principal;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Contains static utility methods.
 */
public class WebUtils
{
    public static int getCurrentUserId(HttpServletRequest hsr)
    {
        Principal p = hsr.getUserPrincipal();
        return (p==null ? -1 : Integer.parseInt(p.getName()));
    }

    public static int getCurrentUserId()
    {
        return getCurrentUserId((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest());
    }
    
    public static String removeQuotes(String in)
    {
        if (in.startsWith("\"") || in.startsWith("'"))
            in = in.substring(1, in.length()-1);
        return in;
    }
}
