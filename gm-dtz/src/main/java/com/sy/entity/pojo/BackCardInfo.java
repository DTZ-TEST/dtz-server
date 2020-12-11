package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;
@Table(alias = "back_card_info")
public class BackCardInfo implements Serializable{

	private Integer id;
	
	private String sendUserId;
	
	private String reciaveUserId;
	
	private Integer backCardType;
	
	private Date createTime;
	
	private Integer cardNum;
	
	private String sendName;
	
	private String reciaveName;
	
	public String getSendName() {
		return sendName;
	}

	public void setSendName(String sendName) {
		this.sendName = sendName;
	}

	public String getReciaveName() {
		return reciaveName;
	}

	public void setReciaveName(String reciaveName) {
		this.reciaveName = reciaveName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSendUserId() {
		return sendUserId;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	public String getReciaveUserId() {
		return reciaveUserId;
	}

	public void setReciaveUserId(String reciaveUserId) {
		this.reciaveUserId = reciaveUserId;
	}

	public Integer getBackCardType() {
		return backCardType;
	}

	public void setBackCardType(Integer backCardType) {
		this.backCardType = backCardType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getCardNum() {
		return cardNum;
	}

	public void setCardNum(Integer cardNum) {
		this.cardNum = cardNum;
	}
	
	
}
