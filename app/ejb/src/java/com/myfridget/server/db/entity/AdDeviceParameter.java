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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "ad_device_parameter")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdDeviceParameter.findAll", query = "SELECT a FROM AdDeviceParameter a"),
    @NamedQuery(name = "AdDeviceParameter.findByAdDeviceId", query = "SELECT a FROM AdDeviceParameter a WHERE a.adDeviceId = :adDeviceId"),
    @NamedQuery(name = "AdDeviceParameter.findByAdDeviceIdAndParam", query = "SELECT a FROM AdDeviceParameter a WHERE a.adDeviceId = :adDeviceId AND a.param = :param")})
public class AdDeviceParameter implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
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

    @Basic(optional = false)
    @NotNull
    @Column(name = "ad_device_id")
    private Integer adDeviceId;

    public AdDeviceParameter() {
    }

    public AdDeviceParameter(Integer id) {
        this.id = id;
    }

    public AdDeviceParameter(Integer id, Integer adDeviceId, String param, String value) {
        this.id = id;
        this.adDeviceId = adDeviceId;
        this.param = param;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getAdDeviceId() {
        return adDeviceId;
    }

    public void setAdDeviceId(Integer adDeviceId) {
        this.adDeviceId = adDeviceId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AdDeviceParameter)) {
            return false;
        }
        AdDeviceParameter other = (AdDeviceParameter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.AdDeviceParameter[ id=" + id + " ]";
    }
    
}
