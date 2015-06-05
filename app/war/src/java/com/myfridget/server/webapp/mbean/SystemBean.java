/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.mbean;

import com.myfridget.server.db.entity.SystemParameter;
import com.myfridget.server.ejb.SystemEJB;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author thorsten
 */
@ManagedBean
@SessionScoped
public class SystemBean {
    
    @EJB
    private SystemEJB systemEjb;
    
    private SystemParameter currentParameter = new SystemParameter();
    
    public void clearParameter() {
        currentParameter = new SystemParameter();
    }
    
    public List<SystemParameter> getParameters() {
        return systemEjb.getSystemParameters();
    }
    
    public SystemParameter getCurrentParameter() {
        return currentParameter;
    }
    
    public void saveParameter() {
        systemEjb.setSystemParameter(currentParameter);
        clearParameter();
    }
    
    public void editParameter(SystemParameter p) {
        this.currentParameter = p;
    }
}
