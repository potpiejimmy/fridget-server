/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.db.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author thorsten
 */
@Entity
@Table(name = "ad_device")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdDevice.findAll", query = "SELECT a FROM AdDevice a"),
    @NamedQuery(name = "AdDevice.findById", query = "SELECT a FROM AdDevice a WHERE a.id = :id"),
    @NamedQuery(name = "AdDevice.findBySerial", query = "SELECT a FROM AdDevice a WHERE a.serial = :serial"),
    @NamedQuery(name = "AdDevice.findByUserId", query = "SELECT a FROM AdDevice a, UserAdDevice b WHERE a.id = b.adDeviceId AND b.userId = :userId"),
    @NamedQuery(name = "AdDevice.findByLon", query = "SELECT a FROM AdDevice a WHERE a.lon = :lon"),
    @NamedQuery(name = "AdDevice.findByLat", query = "SELECT a FROM AdDevice a WHERE a.lat = :lat")})
public class AdDevice implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 24)
    @Column(name = "serial")
    private String serial;
    
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "lon")
    private BigDecimal lon;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "lat")
    private BigDecimal lat;
    
    @Basic(optional = true)
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;
    
    public AdDevice() {
    }

    public AdDevice(Integer id) {
        this.id = id;
    }

    public AdDevice(Integer id, String serial, BigDecimal lon, BigDecimal lat) {
        this.id = id;
        this.serial = serial;
        this.lon = lon;
        this.lat = lat;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(object instanceof AdDevice)) {
            return false;
        }
        AdDevice other = (AdDevice) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.AdDevice[ id=" + id + " ]";
    }
    
}
