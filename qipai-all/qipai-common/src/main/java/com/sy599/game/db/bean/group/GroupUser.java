package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Integer groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String userNickname;
    private Integer userLevel;
    private Integer playCount1;
    private Integer playCount2;
    private Date createdTime;
    private Long inviterId;
    private Integer userRole;
    private Integer userGroup;
    private int credit;
    private int promoterLevel;
    private long promoterId1;
    private long promoterId2;
    private long promoterId3;
    private long promoterId4;
    private int creditCommissionRate;
    private Integer refuseInvite;
    private int isBjdNewer;
    
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public Integer getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(Integer userGroup) {
        this.userGroup = userGroup;
    }

    public int getPromoterLevel() {
        return promoterLevel;
    }

    public void setPromoterLevel(int promoterLevel) {
        this.promoterLevel = promoterLevel;
    }

    public long getPromoterId1() {
        return promoterId1;
    }

    public void setPromoterId1(long promoterId1) {
        this.promoterId1 = promoterId1;
    }

    public long getPromoterId2() {
        return promoterId2;
    }

    public void setPromoterId2(long promoterId2) {
        this.promoterId2 = promoterId2;
    }

    public long getPromoterId3() {
        return promoterId3;
    }

    public void setPromoterId3(long promoterId3) {
        this.promoterId3 = promoterId3;
    }

    public long getPromoterId4() {
        return promoterId4;
    }

    public void setPromoterId4(long promoterId4) {
        this.promoterId4 = promoterId4;
    }

    public int getCreditCommissionRate() {
        return creditCommissionRate;
    }

    public void setCreditCommissionRate(int creditCommissionRate) {
        this.creditCommissionRate = creditCommissionRate;
    }

	public Integer getRefuseInvite() {
		return refuseInvite;
	}

	public void setRefuseInvite(Integer refuseInvite) {
		this.refuseInvite = refuseInvite;
	}

    public int getIsBjdNewer() {
        return isBjdNewer;
    }

    public void setIsBjdNewer(int isBjdNewer) {
        this.isBjdNewer = isBjdNewer;
    }
}
