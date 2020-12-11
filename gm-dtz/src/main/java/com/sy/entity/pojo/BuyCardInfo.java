package com.sy.entity.pojo;

import java.io.Serializable;

public class BuyCardInfo implements Serializable{

	
	private String id;
	
	private String name;
	
	private String ptype;
	
	private Integer cardNums;
	
	private String time;
	private String agencyId;
	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPtype() {
		return ptype;
	}

	public void setPtype(String ptype) {
		this.ptype = ptype;
	}

	public Integer getCardNums() {
		return cardNums;
	}

	public void setCardNums(Integer cardNums) {
		this.cardNums = cardNums;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	
}
