package com.myfridget.server.webapp.mbean;

import com.myfridget.server.ejb.UsersEJBLocal;
import com.myfridget.server.webapp.util.WebUtils;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class ChangePasswordBean {


    @EJB
    private UsersEJBLocal ejb;

    private String oldPassword;
    private String newPassword;

    public void checkChangePasswordNeeded() {
        try {
            if (isChangePasswordNeeded()) WebUtils.redirect("/app/changepassword.xhtml");
        } catch (Exception ex) {
        	// redirect failed? ignore, just redirect the next time.
        }
    }
    
    public boolean isChangePasswordNeeded() {
        return false;//(ejb.getCurrentUser().getPwStatus() == 0);
    }
    
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
        
    public String save() {
        try {
            ejb.changePassword(oldPassword, newPassword);
            WebUtils.addFacesMessage("Password was changed successfully.");
            HttpServletRequest req = WebUtils.getHttpServletRequest();
            String userName = WebUtils.getCurrentPerson(req);
            req.logout();
            req.login(userName, newPassword);
            
            return "index";
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
            return null;
        }
    }

}
