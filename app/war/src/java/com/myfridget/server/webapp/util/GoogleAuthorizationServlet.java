/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.util;

import com.myfridget.server.util.GoogleAuthorizationHelper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author thorsten
 */
@WebServlet("/gauth")
public class GoogleAuthorizationServlet extends HttpServlet {
    
    protected static String getRedirectUrl(GoogleAuthorizationHelper helper, HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        url = url.substring(0, url.indexOf(req.getContextPath()));
        return url + req.getContextPath() + "/gauth";
    }
    
    public static boolean authorize(HttpServletRequest req, HttpServletResponse resp) {
        // load credential from persistence store
        String userId = req.getUserPrincipal().getName();

        GoogleAuthorizationHelper helper = new GoogleAuthorizationHelper(userId);
        if (helper.hasCredentials()) return true;

        // redirect to the authorization flow
        try {
            resp.sendRedirect(helper.getAuthorizationUrl(getRedirectUrl(helper, req)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuffer buf = req.getRequestURL();
        if (req.getQueryString() != null) {
            buf.append('?').append(req.getQueryString());
        }
      
        String userId = req.getUserPrincipal().getName();
        GoogleAuthorizationHelper helper = new GoogleAuthorizationHelper(userId);
      
        try {
            helper.handleAuthorizationCodeResult(buf.toString(), getRedirectUrl(helper, req));
            
            resp.sendRedirect(req.getContextPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
