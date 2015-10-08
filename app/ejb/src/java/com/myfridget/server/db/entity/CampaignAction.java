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
@Table(name = "campaign_action")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CampaignAction.findAll", query = "SELECT c FROM CampaignAction c"),
    @NamedQuery(name = "CampaignAction.findByCampaignId", query = "SELECT c FROM CampaignAction c WHERE c.campaignId = :campaignId ORDER BY c.minuteOfDayFrom")})
public class CampaignAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "campaign_id")
    @NotNull
    private Integer campaignId;
    
    @Column(name = "ad_medium_id")
    @NotNull
    private Integer adMediumId;

    @Basic(optional = false)
    @NotNull
    @Column(name = "minute_of_day_from")
    private short minuteOfDayFrom;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "minute_of_day_to")
    private short minuteOfDayTo;
    
    public CampaignAction() {
    }

    public CampaignAction(Integer id) {
        this.id = id;
    }

    public CampaignAction(Integer id, short minuteOfDayFrom, short minuteOfDayTo) {
        this.id = id;
        this.minuteOfDayFrom = minuteOfDayFrom;
        this.minuteOfDayTo = minuteOfDayTo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    public Integer getAdMediumId() {
        return adMediumId;
    }

    public void setAdMediumId(Integer adMediumId) {
        this.adMediumId = adMediumId;
    }

    public short getMinuteOfDayFrom() {
        return minuteOfDayFrom;
    }

    public void setMinuteOfDayFrom(short minuteOfDayFrom) {
        this.minuteOfDayFrom = minuteOfDayFrom;
    }

    public short getMinuteOfDayTo() {
        return minuteOfDayTo;
    }

    public void setMinuteOfDayTo(short minuteOfDayTo) {
        this.minuteOfDayTo = minuteOfDayTo;
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
        if (!(object instanceof CampaignAction)) {
            return false;
        }
        CampaignAction other = (CampaignAction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.myfridget.server.db.entity.CampaignAction[ id=" + id + " ]";
    }
    
}
