package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by pc on 2017/5/10.
 */
@Table(alias = "t_login_data")
public class LoginData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long	keyId ;
    private Integer dateTime;
    private Integer loginTotalCount;
    private Integer loginTotalTime;
	
    public Long getKeyId() {
		return keyId;
	}
	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}
	public Integer getDateTime() {
		return dateTime;
	}
	public void setDateTime(Integer dateTime) {
		this.dateTime = dateTime;
	}
	public Integer getLoginTotalCount() {
		return loginTotalCount;
	}
	public void setLoginTotalCount(Integer loginTotalCount) {
		this.loginTotalCount = loginTotalCount;
	}
	public Integer getLoginTotalTime() {
		return loginTotalTime;
	}
	public void setLoginTotalTime(Integer loginTotalTime) {
		this.loginTotalTime = loginTotalTime;
	}
    
    
    
}
