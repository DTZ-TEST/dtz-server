package com.sy.sanguo.game.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Integer parentGroup;
    private Integer groupId;
    private String groupName;
    private Integer maxCount;
    private Integer currentCount;
    private Integer groupLevel;
    private Integer groupMode;
    private String extMsg;
    private Date createdTime;
    private Long createdUser;
    private String descMsg;
    private String groupState;
    private Date modifiedTime;
    private Integer isCredit;
    private Integer creditAllotMode;
    /**公告内容*/
    private String content;
    private int switchInvite;

    public String getGroupState() {
        return groupState;
    }

    public void setGroupState(String groupState) {
        this.groupState = groupState;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Integer parentGroup) {
        this.parentGroup = parentGroup;
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

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(Integer groupLevel) {
        this.groupLevel = groupLevel;
    }

    public Integer getGroupMode() {
        return groupMode;
    }

    public void setGroupMode(Integer groupMode) {
        this.groupMode = groupMode;
    }

    public String getExtMsg() {
        return extMsg;
    }

    public void setExtMsg(String extMsg) {
        this.extMsg = extMsg;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(Long createdUser) {
        this.createdUser = createdUser;
    }

    public String getDescMsg() {
        return descMsg;
    }

    public void setDescMsg(String descMsg) {
        this.descMsg = descMsg;
    }
    
    public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getIsCredit() {
        return isCredit;
    }

    public void setIsCredit(Integer isCredit) {
        this.isCredit = isCredit;
    }

    public Integer getCreditAllotMode() {
        return creditAllotMode;
    }

    public void setCreditAllotMode(Integer creditAllotMode) {
        this.creditAllotMode = creditAllotMode;
    }

    public int getSwitchInvite() {
        return switchInvite;
    }

    public void setSwitchInvite(int switchInvite) {
        this.switchInvite = switchInvite;
    }
}
