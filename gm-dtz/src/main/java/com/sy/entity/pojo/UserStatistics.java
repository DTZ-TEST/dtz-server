package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Column;
import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pc on 2017/4/14.
 */
@Table(alias = "user_statistics")
public class UserStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(alias = "keyId")
    private Integer keyId;//主键
    @Column(alias = "userId")
    private String userId;//玩家id
    @Column(alias = "winCount")
    private Integer winCount;//赢的次数
    @Column(alias = "loseCount")
    private Integer loseCount;//输的次数
    @Column(alias = "drawCount")
    private Integer drawCount;//平局次数
    @Column(alias = "userScore")
    private Integer userScore;//得分
    
    @Column(alias = "roomType")
    private String roomType;//房间类型
    
    
    @Column(alias = "currentDate")//统计日期
    private Long currentDate;
    
    @Column(alias = "gameType")
    private Integer gameType;//游戏标识
    
    @Column(alias = "gameCount0")
    private String gameCount0;//小局总数
    
    @Column(alias = "gameCount1")
    private String gameCount1;//大局总数
    
    private Long userNum;
    private Long playNum;
    
    



	public Long getUserNum() {
		return userNum;
	}

	public void setUserNum(Long userNum) {
		this.userNum = userNum;
	}

	public Long getPlayNum() {
		return playNum;
	}

	public void setPlayNum(Long playNum) {
		this.playNum = playNum;
	}

	public Integer getKeyId() {
		return keyId;
	}

	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getWinCount() {
		return winCount;
	}

	public void setWinCount(Integer winCount) {
		this.winCount = winCount;
	}

	public Integer getLoseCount() {
		return loseCount;
	}

	public void setLoseCount(Integer loseCount) {
		this.loseCount = loseCount;
	}

	public Integer getDrawCount() {
		return drawCount;
	}

	public void setDrawCount(Integer drawCount) {
		this.drawCount = drawCount;
	}

	public Integer getUserScore() {
		return userScore;
	}

	public void setUserScore(Integer userScore) {
		this.userScore = userScore;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public Long getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Long currentDate) {
		this.currentDate = currentDate;
	}

	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public String getGameCount0() {
		return gameCount0;
	}

	public void setGameCount0(String gameCount0) {
		this.gameCount0 = gameCount0;
	}

	public String getGameCount1() {
		return gameCount1;
	}

	public void setGameCount1(String gameCount1) {
		this.gameCount1 = gameCount1;
	}


    
}