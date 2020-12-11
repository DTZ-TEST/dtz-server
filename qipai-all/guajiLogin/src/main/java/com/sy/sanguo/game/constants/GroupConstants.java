package com.sy.sanguo.game.constants;

import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupUser;

import java.util.Date;

public class GroupConstants {
    /**
     * 群主
     **/
    public static final int GROUP_ROLE_MASTER = 0;
    /**
     * 管理员
     **/
    public static final int GROUP_ROLE_ADMIN = 1;
    /**
     * 成员
     **/
    public static final int GROUP_ROLE_MEMBER = 2;
    /**
     * 小组长
     **/
    public static final int GROUP_ROLE_TEAM_LEADER = 10;
    /**
     * 拉手
     **/
    public static final int GROUP_ROLE_PROMOTOR = 20;

    /**
     * 群主
     */
    public static boolean isMaster(int userRole) {
        return userRole == GROUP_ROLE_MASTER;
    }

    /**
     * 群主或管理员
     */
    public static boolean isMasterOrAdmin(int userRole) {
        return userRole == GROUP_ROLE_MASTER || userRole == GROUP_ROLE_ADMIN;
    }

    /**
     * 管理员
     */
    public static boolean isAdmin(int userRole) {
        return userRole == GROUP_ROLE_ADMIN;
    }

    /**
     * 小组长
     */
    public static boolean isTeamLeader(int userRole) {
        return userRole == GROUP_ROLE_TEAM_LEADER;
    }

    /**
     * 普通成员
     */
    public static boolean isMember(int userRole) {
        return userRole == GROUP_ROLE_MEMBER;
    }

    /**
     * 拉手
     */
    public static boolean isPromotor(int userRole) {
        return userRole == GROUP_ROLE_PROMOTOR;
    }


    /**
     * 小组长往下
     * lower 是 groupUser的直接下级关系
     *
     * @param groupUser
     * @param lower
     * @return
     */
    public static boolean isNextLevel(GroupUser groupUser, GroupUser lower) {
        if (!groupUser.getUserGroup().equals(lower.getUserGroup())) {
            return false;
        }
        if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            if (lower.getPromoterLevel() == 1) {
                return true;
            }
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            if (groupUser.getPromoterLevel() == 1 && lower.getPromoterId1() == groupUser.getUserId() && lower.getPromoterLevel() == 2) {
                return true;
            } else if (groupUser.getPromoterLevel() == 2 && lower.getPromoterId2() == groupUser.getUserId() && lower.getPromoterLevel() == 3) {
                return true;
            } else if (groupUser.getPromoterLevel() == 3 && lower.getPromoterId3() == groupUser.getUserId() && lower.getPromoterLevel() == 4) {
                return true;
            } else if (groupUser.getPromoterLevel() == 4 && lower.getPromoterId4() == groupUser.getUserId() && lower.getPromoterLevel() == 5) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }


    /**
     * lower 是否是 groupUser 下级
     *
     * @param groupUser
     * @param lower
     * @return
     */
    public static boolean isLower(GroupUser groupUser, GroupUser lower) {
        if (groupUser == null || lower == null) {
            return false;
        }
        if (groupUser.getUserId() == lower.getUserId()) {
            return true;
        }
        if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
            return true;
        } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            return lower.getPromoterLevel() > 0 && groupUser.getUserGroup().equals(lower.getUserGroup());
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            if (groupUser.getPromoterLevel() == 1 && lower.getPromoterId1() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 2 && lower.getPromoterId2() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 3 && lower.getPromoterId3() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 4 && lower.getPromoterId4() == groupUser.getUserId()) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public static GroupUser genGroupUser(GroupInfo group, GroupUser inviter, RegInfo targetUser) {
        GroupUser target = new GroupUser();
        target.setCreatedTime(new Date());
        target.setGroupId(inviter.getGroupId());
        target.setGroupName(group.getGroupName());
        target.setInviterId(inviter.getUserId());
        target.setPlayCount1(0);
        target.setPlayCount2(0);
        target.setUserId(targetUser.getUserId());
        target.setUserLevel(1);
        target.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
        target.setUserName(targetUser.getName());
        target.setUserNickname(targetUser.getName());
        target.setUserGroup(inviter.getUserGroup());
        target.setCredit(0);
        target.setInviterId(inviter.getUserId());

        target.setUserGroup(inviter.getUserGroup());
        target.setPromoterLevel(inviter.getPromoterLevel() + 1);
        target.setPromoterId1(inviter.getPromoterId1());
        target.setPromoterId2(inviter.getPromoterId2());
        target.setPromoterId3(inviter.getPromoterId3());
        target.setPromoterId4(inviter.getPromoterId4());
        if (inviter.getPromoterLevel() == 1) {
            target.setPromoterId1(inviter.getUserId());
        } else if (inviter.getPromoterLevel() == 2) {
            target.setPromoterId2(inviter.getUserId());
        } else if (inviter.getPromoterLevel() == 3) {
            target.setPromoterId3(inviter.getUserId());
        } else if (inviter.getPromoterLevel() == 4) {
            target.setPromoterId4(inviter.getUserId());
        }
        return target;
    }

}
