package com.sy.entity.pojo;

import java.io.Serializable;
import java.util.Date;

import com.sy.mainland.util.db.annotation.Table;
@Table(alias = "activity_reward")
public class ActivityReward implements Serializable{

    private long keyId;
    private int activityId;//活动ID
    private long userId;//玩家ID
    private int type; //类型 1钻石2现金红包
    private int state;//状态 1已领取
    private int rewardIndex;//奖励 0开始
    private Date rewardDate;//领取时间
    private String reward;//奖励内容
    private int rewardNum;//奖励数
    private String userName;
    
    
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getRewardNum() {
		return rewardNum;
	}
	public void setRewardNum(int rewardNum) {
		this.rewardNum = rewardNum;
	}
	public long getKeyId() {
		return keyId;
	}
	public void setKeyId(long keyId) {
		this.keyId = keyId;
	}
	public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getRewardIndex() {
		return rewardIndex;
	}
	public void setRewardIndex(int rewardIndex) {
		this.rewardIndex = rewardIndex;
	}
	public Date getRewardDate() {
		return rewardDate;
	}
	public void setRewardDate(Date rewardDate) {
		this.rewardDate = rewardDate;
	}
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
		this.reward = reward;
	}

}
