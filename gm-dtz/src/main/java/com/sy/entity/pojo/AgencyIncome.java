package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pc on 2017/4/18.
 */
@Table(alias = "agency_income")
public class AgencyIncome implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Integer startDate;
    private Integer endDate;
    private Integer agencyId;
    private Integer agencyLevel;
    private String userName;
    private String bankName;
    private String bankCard;
    private String agencyPhone;
    private String agencyWX;
    private BigDecimal minePay;
    private BigDecimal agencyPay;
    private BigDecimal mineRatio;
    private BigDecimal agencyRatio;
    private BigDecimal mineIncome;
    private BigDecimal agencyIncome;
    private BigDecimal totalPay;
    private BigDecimal totalIncome;
    private String currentState;
    private Date createdTime;
    private Integer incomeStatiscType;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public Integer getEndDate() {
        return endDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
    }

    public Integer getAgencyLevel() {
        return agencyLevel;
    }

    public void setAgencyLevel(Integer agencyLevel) {
        this.agencyLevel = agencyLevel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getAgencyPhone() {
        return agencyPhone;
    }

    public void setAgencyPhone(String agencyPhone) {
        this.agencyPhone = agencyPhone;
    }

    public String getAgencyWX() {
        return agencyWX;
    }

    public void setAgencyWX(String agencyWX) {
        this.agencyWX = agencyWX;
    }

    public BigDecimal getMinePay() {
        return minePay;
    }

    public void setMinePay(BigDecimal minePay) {
        this.minePay = minePay;
    }

    public BigDecimal getAgencyPay() {
        return agencyPay;
    }

    public void setAgencyPay(BigDecimal agencyPay) {
        this.agencyPay = agencyPay;
    }

    public BigDecimal getMineRatio() {
        return mineRatio;
    }

    public void setMineRatio(BigDecimal mineRatio) {
        this.mineRatio = mineRatio;
    }

    public BigDecimal getAgencyRatio() {
        return agencyRatio;
    }

    public void setAgencyRatio(BigDecimal agencyRatio) {
        this.agencyRatio = agencyRatio;
    }

    public BigDecimal getMineIncome() {
        return mineIncome;
    }

    public void setMineIncome(BigDecimal mineIncome) {
        this.mineIncome = mineIncome;
    }

    public BigDecimal getAgencyIncome() {
        return agencyIncome;
    }

    public void setAgencyIncome(BigDecimal agencyIncome) {
        this.agencyIncome = agencyIncome;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

	public Integer getIncomeStatiscType() {
		return incomeStatiscType;
	}

	public void setIncomeStatiscType(Integer incomeStatiscType) {
		this.incomeStatiscType = incomeStatiscType;
	}
    
}
