package com.sy599.game.credit;

import com.sy599.game.db.bean.group.GroupUser;

public class CreditCommission {

    private GroupUser groupUser;
    private long destUserId;
    private int credit;

    public CreditCommission(GroupUser groupUser, long destUserId, int credit) {
        this.groupUser = groupUser;
        this.destUserId = destUserId;
        this.credit = credit;
    }

    public GroupUser getGroupUser() {
        return groupUser;
    }

    public void setGroupUser(GroupUser groupUser) {
        this.groupUser = groupUser;
    }

    public long getDestUserId() {
        return destUserId;
    }

    public void setDestUserId(long destUserId) {
        this.destUserId = destUserId;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

}
