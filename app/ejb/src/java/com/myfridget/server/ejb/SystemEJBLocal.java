/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.SystemParameter;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author thorsten
 */
@Local
public interface SystemEJBLocal {
    
    public List<SystemParameter> getSystemParameters();
    
    public void setSystemParameter(SystemParameter param);
}
