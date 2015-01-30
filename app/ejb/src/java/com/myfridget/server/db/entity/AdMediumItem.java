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
@Table(name = "ad_medium_item")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AdMediumItem.findAll", query = "SELECT a FROM AdMediumItem a"),
    @NamedQuery(name = "AdMediumItem.findByType", query = "SELECT a FROM AdMediumItem a WHERE a.type = :type"),
    @NamedQuery(name = "AdMediumItem.findByMediumAndType", query = "SELECT a FROM AdMediumItem a WHERE a.adMediumId = :adMediumId AND a.type = :type")})
public class AdMediumItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "ad_medium_id")
    @NotNull
    private Integer adMediumId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "type")
    private short type;

    public AdMediumItem() {
    }

    public AdMediumItem(Integer id) {
        this.id = id;
    }

    public AdMediumItem(Integer id, short type) {
        this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdMediumId() {
        return adMediumId;
    }

    public void setAdMediumId(Integer adMediumId) {
        this.adMediumId = adMediumId;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
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
        if (!(object instanceof AdMediumItem)) {
            return false;
        }
        AdMediumItem other = (AdMediumItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.AdMediumItem[ id=" + id + " ]";
    }
    
}
