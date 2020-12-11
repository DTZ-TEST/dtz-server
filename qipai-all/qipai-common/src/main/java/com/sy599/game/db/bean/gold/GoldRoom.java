package com.sy599.game.db.bean.gold;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class GoldRoom {
    private Long keyId;
    private Integer currentCount;
    private Integer maxCount;
    private Integer serverId;
    private String currentState;
    private String tableMsg;
    private String modeId;
    private Integer gameCount;
    private Date createdTime;
    private Date modifiedTime;


    //不是表字段
    private JSONObject tableMsgJson;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;

        if (StringUtils.isNotBlank(tableMsg))
            this.tableMsgJson = JSONObject.parseObject(tableMsg);
        else this.tableMsgJson = new JSONObject();
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
    }

    public Integer getGameCount() {
        return gameCount;
    }

    public void setGameCount(Integer gameCount) {
        this.gameCount = gameCount;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public boolean isNotStart() {
        return "0".equals(currentState);
    }

    public boolean isPlaying() {
        return "1".equals(currentState);
    }

    public boolean isNormalOver() {
        return "2".equals(currentState);
    }

    public boolean isZeroOver() {
        return "3".equals(currentState);
    }

    public boolean isMidwayOver() {
        return "4".equals(currentState);
    }

    public boolean isOver() {
        return isNormalOver() || isZeroOver() || isMidwayOver();
    }

    public String loadMatchId(){
        return tableMsgJson==null?null:tableMsgJson.getString("matchId");
    }

    public int loadMatchRatio(){
        int ratio = tableMsgJson==null?1:tableMsgJson.getIntValue("matchRatio");
        return ratio<=0?1:ratio;
    }
}
