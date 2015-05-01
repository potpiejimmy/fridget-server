/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.db.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author thorsten
 */
@Entity
@Table(name = "system_parameter")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SystemParameter.findAll", query = "SELECT s FROM SystemParameter s"),
    @NamedQuery(name = "SystemParameter.findByParam", query = "SELECT s FROM SystemParameter s WHERE s.param = :param"),
    @NamedQuery(name = "SystemParameter.findByValue", query = "SELECT s FROM SystemParameter s WHERE s.value = :value")})
public class SystemParameter implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "param")
    private String param;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "value")
    private String value;

    public SystemParameter() {
    }

    public SystemParameter(String param) {
        this.param = param;
    }

    public SystemParameter(String param, String value) {
        this.param = param;
        this.value = value;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (param != null ? param.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SystemParameter)) {
            return false;
        }
        SystemParameter other = (SystemParameter) object;
        if ((this.param == null && other.param != null) || (this.param != null && !this.param.equals(other.param))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.SystemParameter[ param=" + param + " ]";
    }
    
}
