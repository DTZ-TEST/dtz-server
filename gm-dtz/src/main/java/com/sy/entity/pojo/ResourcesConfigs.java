package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;

@Table(alias = "t_resources_configs")
public class ResourcesConfigs implements Serializable{
	
	  private static final long serialVersionUID = 1L;
	
	private Long keyId;
    private String msgType;//资源类型
    private String msgKey;//资源key
    private String msgValue;//资源value
    private String msgDesc;
    private Date configTime;
    private Integer msgStatus;
    private String startTime;
    private String endTime;
    private String cfTime;


	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public String getCfTime() {
		return cfTime;
	}

	public void setCfTime(String cfTime) {
		this.cfTime = cfTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Integer getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(Integer msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getMsgDesc() {
		return msgDesc;
	}

	public void setMsgDesc(String msgDesc) {
		this.msgDesc = msgDesc;
	}
	public Date getConfigTime() {
		return configTime;
	}

	public void setConfigTime(Date configTime) {
		this.configTime = configTime;
	}

	public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getMsgValue() {
        return msgValue;
    }

    public void setMsgValue(String msgValue) {
        this.msgValue = msgValue;
    }
}
