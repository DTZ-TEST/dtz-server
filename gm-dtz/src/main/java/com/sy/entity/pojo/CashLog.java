package com.sy.entity.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;

@Table(alias = "cashlog")
public class CashLog implements Serializable{

	private Integer id;
	
	private Long cashId;
	
	private String cashReport;
	
	private Date createTime;
	
	private Integer agencyId;
	
	private String cashDesc;
	
	private Integer state;
	
	private BigDecimal money;
	
	

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getCashId() {
		return cashId;
	}

	public void setCashId(Long cashId) {
		this.cashId = cashId;
	}

	public String getCashReport() {
		return cashReport;
	}

	public void setCashReport(String cashReport) {
		this.cashReport = cashReport;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Integer agencyId) {
		this.agencyId = agencyId;
	}

	public String getCashDesc() {
		return cashDesc;
	}

	public void setCashDesc(String cashDesc) {
		this.cashDesc = cashDesc;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
	
	
}
