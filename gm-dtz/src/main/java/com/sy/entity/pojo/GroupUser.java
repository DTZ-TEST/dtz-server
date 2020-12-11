package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;
@Table(alias = "t_group_user")
public class GroupUser implements Serializable{

	private Long keyId;
	
	private Integer groupId;
	
	private String groupName;
	
	private Long userId;
	
	private String descMsg;
	
	private Date createdTime;
	
	private Long inviterId;
	
	private Integer userRole;
	
	private String userName;
	
	private Integer userLevel;
	
	private Integer playCount1;
	
	private Integer playCount2;
	
	private String userNickname;

	private Integer creditScore;
	
	
	public Integer getCreditScore() {
		return creditScore;
	}

	public void setCreditScore(Integer creditScore) {
		this.creditScore = creditScore;
	}

	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDescMsg() {
		return descMsg;
	}

	public void setDescMsg(String descMsg) {
		this.descMsg = descMsg;
	}
	public Long getInviterId() {
		return inviterId;
	}

	public void setInviterId(Long inviterId) {
		this.inviterId = inviterId;
	}

	public Integer getUserRole() {
		return userRole;
	}

	public void setUserRole(Integer userRole) {
		this.userRole = userRole;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Integer getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(Integer userLevel) {
		this.userLevel = userLevel;
	}

	public Integer getPlayCount1() {
		return playCount1;
	}

	public void setPlayCount1(Integer playCount1) {
		this.playCount1 = playCount1;
	}

	public Integer getPlayCount2() {
		return playCount2;
	}

	public void setPlayCount2(Integer playCount2) {
		this.playCount2 = playCount2;
	}

	public String getUserNickname() {
		return userNickname;
	}

	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}
	
	
}
