/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.ejb;

import com.myfridget.server.db.entity.SystemParameter;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author thorsten
 */
@Stateless
public class SystemEJB implements SystemEJBLocal {

    @PersistenceContext(unitName = "Fridget_EJBsPU")
    protected EntityManager em;
    
    @Override
    public List<SystemParameter> getSystemParameters() {
        return em.createNamedQuery("SystemParameter.findAll", SystemParameter.class).getResultList();
    }

    @Override
    public void setSystemParameter(SystemParameter param) {
        em.merge(param);
    }
    
}
