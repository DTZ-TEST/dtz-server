package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;
@Table(alias = "t_match_user")
public class Matchjl implements Serializable{

	private Long keyId;
	
	private Long matchId;
	
	private String matchType;
	
	private String  userId;
	
	private String currentState;
	
	private Date createTime;
	private Date modifiedTime;
	
	private Integer currentNo;
	
	private Integer currentScore;
	
	private Integer userRank;
	private String userAward;
	private String awardState;
	private Integer reliveCount;
	private String matchName;
	
	
	public String getMatchName() {
		return matchName;
	}
	public void setMatchName(String matchName) {
		this.matchName = matchName;
	}
	public Long getKeyId() {
		return keyId;
	}
	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}
	public Long getMatchId() {
		return matchId;
	}
	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}
	public String getMatchType() {
		return matchType;
	}
	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getCurrentState() {
		return currentState;
	}
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifiedTime() {
		return modifiedTime;
	}
	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	public Integer getCurrentNo() {
		return currentNo;
	}
	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
	}
	public Integer getCurrentScore() {
		return currentScore;
	}
	public void setCurrentScore(Integer currentScore) {
		this.currentScore = currentScore;
	}
	public Integer getUserRank() {
		return userRank;
	}
	public void setUserRank(Integer userRank) {
		this.userRank = userRank;
	}
	public String getUserAward() {
		return userAward;
	}
	public void setUserAward(String userAward) {
		this.userAward = userAward;
	}
	public String getAwardState() {
		return awardState;
	}
	public void setAwardState(String awardState) {
		this.awardState = awardState;
	}
	public Integer getReliveCount() {
		return reliveCount;
	}
	public void setReliveCount(Integer reliveCount) {
		this.reliveCount = reliveCount;
	}
	
	

}
