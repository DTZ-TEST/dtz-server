package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/28.
 */
@Table(alias = "roomcardrecord")
public class RoomCardRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer activeUserid;
    private Integer reactiveUserId;
    private String activeUserName;
    private String reactiveUserName;
    private Integer rechargeOrReturn;
    private Integer roomCardType;
    private Integer roomCardNumber;
    private Integer recordStatus;
    private String remark;//备注
    private Date createTime;
    private String agencyName;
    private String	gameName;
    
    
    public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

    public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActiveUserid() {
        return activeUserid;
    }

    public void setActiveUserid(Integer activeUserid) {
        this.activeUserid = activeUserid;
    }

    public Integer getReactiveUserId() {
        return reactiveUserId;
    }

    public void setReactiveUserId(Integer reactiveUserId) {
        this.reactiveUserId = reactiveUserId;
    }

    public String getActiveUserName() {
        return activeUserName;
    }

    public void setActiveUserName(String activeUserName) {
        this.activeUserName = activeUserName;
    }

    public String getReactiveUserName() {
        return reactiveUserName;
    }

    public void setReactiveUserName(String reactiveUserName) {
        this.reactiveUserName = reactiveUserName;
    }

    public Integer getRechargeOrReturn() {
        return rechargeOrReturn;
    }

    public void setRechargeOrReturn(Integer rechargeOrReturn) {
        this.rechargeOrReturn = rechargeOrReturn;
    }

    public Integer getRoomCardType() {
        return roomCardType;
    }

    public void setRoomCardType(Integer roomCardType) {
        this.roomCardType = roomCardType;
    }

    public Integer getRoomCardNumber() {
        return roomCardNumber;
    }

    public void setRoomCardNumber(Integer roomCardNumber) {
        this.roomCardNumber = roomCardNumber;
    }

    public Integer getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Integer recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
    
    
}
