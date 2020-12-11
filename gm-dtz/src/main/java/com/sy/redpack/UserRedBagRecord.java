package com.sy.redpack;

import com.alibaba.fastjson.TypeReference;
import com.sy.mainland.util.db.annotation.Table;
import com.sy.util.JacksonUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.sy599.game.util.JacksonUtil;
//import com.sy599.game.util.StringUtil;
//import com.sy599.game.util.TimeUtil;

/**
 * 现金红包活动记录  KEY==>玩家ID_领取红包时间
 */
@Table(alias = "activity_redbag")
public class UserRedBagRecord {

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 领取红包时间
     */
    private String receiveDate;

    /**
     * 今日玩牌局数
     */
    private int gameNum;

    /**
     * 当天已领取红包次数
     */
    private int receiveNum;

    private String receiveRecords;

    /**
     * 登陆红包领取金额
     */
    private float loginRedBag;

    /**
     * 打牌红包领取金额
     */
    private float gameRedBag;

    /**
     * 最后领取时间
     */
    private Date lastReceiveTime;

    public UserRedBagRecord() {
    }

    public UserRedBagRecord(long userId, String receiveDate, int gameNum, int receiveNum) {
        this.userId = userId;
        this.receiveDate = receiveDate;
        this.gameNum = gameNum;
        this.receiveNum = receiveNum;
        this.receiveRecords = "";
        this.loginRedBag = 0;
        this.gameRedBag = 0;
        this.lastReceiveTime = null;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public int getGameNum() {
        return gameNum;
    }

    public void setGameNum(int gameNum) {
        this.gameNum = gameNum;
    }

    public void alterGameNum(int num) {
        this.gameNum += num;
    }

    public int getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(int receiveNum) {
        this.receiveNum = receiveNum;
    }


    public String getReceiveRecords() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            return "";
        }
        return receiveRecords;
    }

    public List<SelfRedBagReceiveRecord> getReceiveRecordList() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            return new ArrayList<>();
        }
        return JacksonUtil.readValue(receiveRecords,
                new TypeReference<List<SelfRedBagReceiveRecord>>() {
                });
    }

    public void setReceiveRecords(String receiveRecords) {
        this.receiveRecords = receiveRecords;
    }

    public float getLoginRedBag() {
        return loginRedBag;
    }

    public void setLoginRedBag(float loginRedBag) {
        this.loginRedBag = loginRedBag;
    }

    public float getGameRedBag() {
        return gameRedBag;
    }

    public void setGameRedBag(float gameRedBag) {
        this.gameRedBag = gameRedBag;
    }

    public Date getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(Date lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }
}
