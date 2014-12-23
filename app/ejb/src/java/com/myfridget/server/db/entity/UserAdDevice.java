package com.myfridget.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="user_ad_device")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserAdDevice.findByUserId", query = "SELECT f FROM UserAdDevice f WHERE f.userId=:userId ORDER BY f.adDeviceId"),
    @NamedQuery(name = "UserAdDevice.findByAdDeviceId", query = "SELECT f FROM UserAdDevice f WHERE f.adDeviceId=:adDeviceId"),
    @NamedQuery(name = "UserAdDevice.deleteByAdDeviceId", query = "DELETE FROM UserAdDevice f WHERE f.adDeviceId=:adDeviceId")})
public class UserAdDevice implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name="user_id")
	private Integer userId;

        @Id
	@NotNull
	@Column(name="ad_device_id")
	private Integer adDeviceId;

	public UserAdDevice() {
	}

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAdDeviceId() {
        return adDeviceId;
    }

    public void setAdDeviceId(Integer adDeviceId) {
        this.adDeviceId = adDeviceId;
    }

}