package com.sy.redpack;

import com.sy.mainland.util.db.annotation.Table;

import java.util.Date;

/**
 * 玩家现金红包信息
 */
@Table(alias = "redbag_info")
public class RedBagInfo {

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 红包类型
     * 0 钻石
     * 1 现金红包
     */
    private int redBagType;

    /**
     * 领取的红包金额
     */
    private float redbag;

    /**
     * 领取时间
     */
    private Date receiveDate;

    /**
     * 提现时间
     */
    private Date drawDate;

    /**
     * 红包来源
     */
    private int sourceType;

    /**
     * 红包来源名
     */
    private String sourceTypeName;

    public RedBagInfo() {
    }

    public RedBagInfo(long userId, int redBagType, float redbag, Date receiveDate, Date drawDate, int sourceType, String sourceTypeName) {
        this.userId = userId;
        this.redBagType = redBagType;
        this.redbag = redbag;
        this.receiveDate = receiveDate;
        this.drawDate = drawDate;
        this.sourceType = sourceType;
        this.sourceTypeName = sourceTypeName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRedBagType() {
        return redBagType;
    }

    public void setRedBagType(int redBagType) {
        this.redBagType = redBagType;
    }

    public float getRedbag() {
        return redbag;
    }

    public void setRedbag(float redbag) {
        this.redbag = redbag;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public Date getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public void setSourceTypeName(String sourceTypeName) {
        this.sourceTypeName = sourceTypeName;
    }
}
