package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.List;
public class AgencyPayInfoList extends AgencyPayInfo implements Serializable{

	private List<AgencyPayInfo> parentAgencyPayInfo;

	public List<AgencyPayInfo> getParentAgencyPayInfo() {
		return parentAgencyPayInfo;
	}

	public void setParentAgencyPayInfo(List<AgencyPayInfo> parentAgencyPayInfo) {
		this.parentAgencyPayInfo = parentAgencyPayInfo;
	}
	
}
