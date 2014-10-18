/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.db.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author thorsten
 */
@Entity
@Table(name = "ad_device_debug_msg")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdDeviceDebugMsg.findAll", query = "SELECT a FROM AdDeviceDebugMsg a ORDER BY a.date DESC"),
    @NamedQuery(name = "AdDeviceDebugMsg.findByAdDeviceId", query = "SELECT a FROM AdDeviceDebugMsg a WHERE a.adDeviceId = :adDeviceId ORDER BY a.date DESC"),
    @NamedQuery(name = "AdDeviceDebugMsg.findByDate", query = "SELECT a FROM AdDeviceDebugMsg a WHERE a.date = :date"),
    @NamedQuery(name = "AdDeviceDebugMsg.findByMessage", query = "SELECT a FROM AdDeviceDebugMsg a WHERE a.message = :message")})
public class AdDeviceDebugMsg implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "message")
    private String message;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "ad_device_id")
    private Integer adDeviceId;

    public AdDeviceDebugMsg() {
    }

    public AdDeviceDebugMsg(Integer id) {
        this.id = id;
    }

    public AdDeviceDebugMsg(Integer id, Date date, String message) {
        this.id = id;
        this.date = date;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        if (!(object instanceof AdDeviceDebugMsg)) {
            return false;
        }
        AdDeviceDebugMsg other = (AdDeviceDebugMsg) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.AdDeviceDebugMsg[ id=" + id + " ]";
    }
    
}
