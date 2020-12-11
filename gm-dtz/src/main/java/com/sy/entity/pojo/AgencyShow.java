package com.sy.entity.pojo;

import java.io.Serializable;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;
@Table(alias = "agency_show")
public class AgencyShow implements Serializable{

	private Long keyId;
	
	@Column(alias = "weixin_name")
	private String weixinName;

	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public String getWeixinName() {
		return weixinName;
	}

	public void setWeixinName(String weixinName) {
		this.weixinName = weixinName;
	}
	
	
}
