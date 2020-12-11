package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;
public class AgencyPayInfo implements Serializable{

	private Integer agencyId;
	
	
	private Date createTime;
	
	private Integer nums;

	public Integer getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Integer agencyId) {
		this.agencyId = agencyId;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getNums() {
		return nums;
	}

	public void setNums(Integer nums) {
		this.nums = nums;
	}
	
	
	
}
