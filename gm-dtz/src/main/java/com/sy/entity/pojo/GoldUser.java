package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/18.
 */
@Table(alias = "t_gold_user")
public class GoldUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId ;// '主键',
    private Long userId ;// '玩家id',
    private String userName;//'玩家名称',
    private String userNickname;// '玩家昵称',
    private Integer playCount;//'局数',
    private Integer playCountWin;// '胜局数',
    private Integer playCountLose;// '败局数',
    private Integer playCountEven;//'平局数',
    private Integer freeGold;//免费的金币',
    private Integer Gold;
    private Integer usedGold;//'消费的金币',
    private Date regTime;// '注册时间',
    private Date lastLoginTime;// '最后登录时间',
	public Long getKeyId() {
		return keyId;
	}
	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserNickname() {
		return userNickname;
	}
	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}
	public Integer getPlayCount() {
		return playCount;
	}
	public void setPlayCount(Integer playCount) {
		this.playCount = playCount;
	}
	public Integer getPlayCountWin() {
		return playCountWin;
	}
	public void setPlayCountWin(Integer playCountWin) {
		this.playCountWin = playCountWin;
	}
	public Integer getPlayCountLose() {
		return playCountLose;
	}
	public void setPlayCountLose(Integer playCountLose) {
		this.playCountLose = playCountLose;
	}
	public Integer getPlayCountEven() {
		return playCountEven;
	}
	public void setPlayCountEven(Integer playCountEven) {
		this.playCountEven = playCountEven;
	}
	public Integer getFreeGold() {
		return freeGold;
	}
	public void setFreeGold(Integer freeGold) {
		this.freeGold = freeGold;
	}
	public Integer getGold() {
		return Gold;
	}
	public void setGold(Integer gold) {
		Gold = gold;
	}
	public Integer getUsedGold() {
		return usedGold;
	}
	public void setUsedGold(Integer usedGold) {
		this.usedGold = usedGold;
	}
	public Date getRegTime() {
		return regTime;
	}
	public void setRegTime(Date regTime) {
		this.regTime = regTime;
	}
	public Date getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
    
    
   
}
