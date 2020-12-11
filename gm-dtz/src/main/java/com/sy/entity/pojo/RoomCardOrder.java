package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/28.
 */
@Table(alias = "roomcard_order")
public class RoomCardOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;
    private String orderId;
    private Integer registerBindAgencyId;
    private Integer rechargeBindAgencyId;
    private Integer isFirstPayBindId;
    private Integer commonCards;
    private Integer freeCards;
    private Integer isDirectRecharge;
    private String rechargeWay;
    private Integer rechargeAgencyId;
    private Integer orderStatus;
    private Date createTime;
    private Integer isFirstPayAmount;
    private String remark;
    private String playerName;

	public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getRegisterBindAgencyId() {
        return registerBindAgencyId;
    }

    public void setRegisterBindAgencyId(Integer registerBindAgencyId) {
        this.registerBindAgencyId = registerBindAgencyId;
    }

    public Integer getRechargeBindAgencyId() {
        return rechargeBindAgencyId;
    }

    public void setRechargeBindAgencyId(Integer rechargeBindAgencyId) {
        this.rechargeBindAgencyId = rechargeBindAgencyId;
    }

    public Integer getIsFirstPayBindId() {
        return isFirstPayBindId;
    }

    public void setIsFirstPayBindId(Integer isFirstPayBindId) {
        this.isFirstPayBindId = isFirstPayBindId;
    }

    public Integer getCommonCards() {
        return commonCards;
    }

    public void setCommonCards(Integer commonCards) {
        this.commonCards = commonCards;
    }

    public Integer getFreeCards() {
        return freeCards;
    }

    public void setFreeCards(Integer freeCards) {
        this.freeCards = freeCards;
    }

    public Integer getIsDirectRecharge() {
        return isDirectRecharge;
    }

    public void setIsDirectRecharge(Integer isDirectRecharge) {
        this.isDirectRecharge = isDirectRecharge;
    }

    public String getRechargeWay() {
        return rechargeWay;
    }

    public void setRechargeWay(String rechargeWay) {
        this.rechargeWay = rechargeWay;
    }

    public Integer getRechargeAgencyId() {
        return rechargeAgencyId;
    }

    public void setRechargeAgencyId(Integer rechargeAgencyId) {
        this.rechargeAgencyId = rechargeAgencyId;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsFirstPayAmount() {
        return isFirstPayAmount;
    }

    public void setIsFirstPayAmount(Integer isFirstPayAmount) {
        this.isFirstPayAmount = isFirstPayAmount;
    }

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
    
    
}
