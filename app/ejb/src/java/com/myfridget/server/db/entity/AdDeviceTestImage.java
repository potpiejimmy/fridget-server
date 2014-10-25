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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author thorsten
 */
@Entity
@Table(name = "ad_device_test_image")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdDeviceTestImage.findAll", query = "SELECT a FROM AdDeviceTestImage a"),
    @NamedQuery(name = "AdDeviceTestImage.findByAdDeviceId", query = "SELECT a FROM AdDeviceTestImage a WHERE a.adDeviceId = :adDeviceId")})
public class AdDeviceTestImage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Column(name = "ad_device_id")
    private Integer adDeviceId;

    public AdDeviceTestImage() {
    }

    public AdDeviceTestImage(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        if (!(object instanceof AdDeviceTestImage)) {
            return false;
        }
        AdDeviceTestImage other = (AdDeviceTestImage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.AdDeviceTestImage[ id=" + id + " ]";
    }
    
}
