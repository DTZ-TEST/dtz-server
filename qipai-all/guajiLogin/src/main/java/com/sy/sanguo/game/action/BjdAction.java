package com.sy.sanguo.game.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.KeyWordsFilter;
import com.sy.sanguo.common.util.MessageBuilder;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.action.group.GroupAction;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupReview;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.UserRelationDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.utils.BjdUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BjdAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static final int min_group_id = 1000;

    private UserDaoImpl userDao;

    private GroupDao groupDao;

    private UserRelationDaoImpl userRelationDao;

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setUserRelationDao(UserRelationDaoImpl userRelationDao) {
        this.userRelationDao = userRelationDao;
    }

    /**
     * 白金岛后台修改钻石后，通知用户更新钻石
     */
    public void notifyChangCards() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("BjdAction|notifyChangCards|" + params);
            if (!BjdUtil.checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int cards = NumberUtils.toInt(params.get("cards"), 0);
            int freeCards = NumberUtils.toInt(params.get("freeCards"), 0);

            RegInfo userInfo = userDao.getUser(userId);
            if (userInfo != null && userInfo.getEnterServer() > 0) {
                ServerConfig serverConfig = new ServerConfig();
                serverConfig.setId(userInfo.getEnterServer());
                serverConfig = ServerDaoImpl.getInstance().queryServer(userInfo.getEnterServer());
                if (serverConfig != null) {
                    String url = serverConfig.getIntranet();
                    if (StringUtils.isBlank(url)) {
                        url = serverConfig.getHost();
                    }
                    if (StringUtils.isNotBlank(url)) {
                        int idx = url.indexOf(".");
                        if (idx > 0) {
                            idx = url.indexOf("/", idx);
                            if (idx > 0) {
                                url = url.substring(0, idx);
                            }
                            url += "/online/notice.do?type=notifyChangCards&userId=" + userInfo.getUserId() + "&cards=" + cards + "&freeCards=" + freeCards;
                            String noticeRet = HttpUtil.getUrlReturnValue(url);
                            LOGGER.info("BjdAction|notifyChangCards|result|{}|{}", url, noticeRet);
                            OutputUtil.output(0, "操作成功", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("BjdAction|notifyChangCards|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(2, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
        }
        OutputUtil.output(2, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
    }


    public void clientLogMsg() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("BjdAction|clientLogMsg|" + params);
            if (!BjdUtil.checkSign(params)) {
//                OutputUtil.output(1, "签名验证失败", getRequest(), getResponse(), false);
//                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            String logMsg = params.get("logMsg");
            if (StringUtils.isNotBlank(logMsg)) {
                LOGGER.info("clientLogMsg|" + userId + "|" + logMsg);
                OutputUtil.output(0, "操作成功", getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("BjdAction|clientLogMsg|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(2, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
        }
        OutputUtil.output(2, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
    }


    /**
     * 创建亲友圈
     */
    public void createGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("createGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            String groupName = params.get("groupName");
            long userId = NumberUtils.toLong(params.get("userId"), 0);

            if (StringUtils.isBlank(groupName)) {
                OutputUtil.output(1, "请输入亲友圈名字！", getRequest(), getResponse(), false);
                return;
            } else if (userId <= 0) {
                OutputUtil.output(1, "参数错误", getRequest(), getResponse(), false);
                return;
            }
            String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
            if (!groupName.matches(regex)) {
                OutputUtil.output(1, "亲友圈名称仅限字母数字和汉字", getRequest(), getResponse(), false);
                return;
            }
            groupName = groupName.trim();
            String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
            if (!groupName.equals(groupName0)) {
                OutputUtil.output(1, "亲友圈名不能包含敏感字符", getRequest(), getResponse(), false);
                return;
            }

            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser;
            GroupInfo groupInfo;
            synchronized (GroupAction.class) {
                SecureRandom random = new SecureRandom();
                int base = 9000;
                int groupId = min_group_id + random.nextInt(base);
                boolean canCreate = false;
                int c = 0;
                while (c < 3) {
                    c++;
                    if (groupDao.existsGroupInfo(groupId, 0)) {
                        groupId = min_group_id * c * 10 + random.nextInt(base * c * 10);
                    } else {
                        canCreate = true;
                        break;
                    }
                }
                if (!canCreate) {
                    OutputUtil.output(5, "请稍后再试", getRequest(), getResponse(), false);
                    return;
                }
                groupInfo = new GroupInfo();
                groupInfo.setCreatedTime(new Date());
                groupInfo.setCreatedUser(userId);
                groupInfo.setCurrentCount(1);
                groupInfo.setDescMsg("");
                groupInfo.setGroupId(groupId);
                JSONObject jsonObject = new JSONObject();
                String defaultStr = PropertiesCacheUtil.getValueOrDefault("group_kf_default", "", Constants.GAME_FILE);
                if (!StringUtils.isBlank(defaultStr)) {
                    String[] strs = defaultStr.split(",");
                    for (String value : strs) {
                        if ((value.startsWith("+q") || value.startsWith("-q"))) {
                            jsonObject.put("oq", value);
                        } else if ((value.startsWith("+p3") || value.startsWith("-p3"))) {
                            jsonObject.put("pc", value);
                        } else if ((value.startsWith("+r") || value.startsWith("-r"))) {
                            jsonObject.put("cr", value);
                        }
                    }
                }
                groupInfo.setExtMsg(jsonObject.toString());
                groupInfo.setGroupLevel(1);
                groupInfo.setGroupMode(0);
                groupInfo.setGroupName(groupName);
                groupInfo.setMaxCount(500);
                groupInfo.setParentGroup(0);
                groupInfo.setGroupState("1");
                groupInfo.setModifiedTime(groupInfo.getCreatedTime());
                if (groupDao.createGroup(groupInfo) <= 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(3, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }
                groupUser = new GroupUser();
                groupUser.setCreatedTime(new Date());
                groupUser.setGroupId(groupInfo.getGroupId());
                groupUser.setGroupName(groupInfo.getGroupName());
                groupUser.setInviterId(userId);
                groupUser.setPlayCount1(0);
                groupUser.setPlayCount2(0);
                groupUser.setUserId(userId);
                groupUser.setUserLevel(1);
                groupUser.setUserRole(0);
                groupUser.setUserName(user.getName());
                groupUser.setUserNickname(user.getName());
                groupUser.setUserGroup("0");
                groupUser.setCredit(0);
                if (groupDao.createGroupUser(groupUser) <= 0) {
                    groupDao.deleteGroupInfoByGroupId(groupInfo.getGroupId(), 0);
                    return;
                }

                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(0, "创建成功").builder("groupId", groupInfo.getGroupId()), getRequest(), getResponse(), null, false);
                LOGGER.info("createGroup|succ|" + JSON.toJSONString(groupInfo));
            }
        } catch (Exception e) {
            OutputUtil.output(2, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
            LOGGER.error("createGroup|error|" + e.getMessage(), e);
        }
    }

    /**
     * 通过分享链接
     * 申请进群
     */
    public void inviteJoinGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("applyJoinGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            String unionId = params.get("unionId");
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            long inviterId = NumberUtils.toLong(params.get("inviterId"), 0);

            if (StringUtils.isBlank(unionId)) {
                OutputUtil.output(1, "参数错误：openId或unionId为空", getRequest(), getResponse(), false);
                return;
            }
            if (groupId <= 0) {
                OutputUtil.output(2, "参数错误：groupId不能为空", getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDao.loadGroupInfo(groupId, 0);
            if (group == null) {
                OutputUtil.output(3, "参数错误：groupId为[" + groupId + "]的用户不存在", getRequest(), getResponse(), false);
                return;
            } else if (group.getGroupMode().intValue() != 0) {
                OutputUtil.output(4, "亲友圈不接受申请", getRequest(), getResponse(), false);
                return;
            } else if (group.getCurrentCount().intValue() >= group.getMaxCount().intValue()) {
                OutputUtil.output(5, "亲友圈人员已满", getRequest(), getResponse(), false);
                return;
            }
            RegInfo regInfo = userDao.getUserByUnionid(unionId);
            long userId = 0;
            if (regInfo == null) {
                OutputUtil.output(5, "角色不存在，请先创建角色", getRequest(), getResponse(), false);
                return;
//                // 创建用户
//                String openId = params.get("openId");
//                String nickname = params.get("nickname");
//                int sex = NumberUtils.toInt(params.get("sex"), 0);
//                String headImgUrl = params.get("headImgUrl");
//                synchronized (BjdAction.class) {
//                    String os = params.get("os");
//                    String pf = "weixinbjd";
//                    if ("2".equals(os)) {
//                        pf = "weixinbjdIOS";
//                    }
//                    regInfo = new RegInfo();
//                    JsonWrapper userJson = new JsonWrapper("");
//                    userJson.putString("openid", openId);
//                    userJson.putString("nickname", StringUtil.filterEmoji(nickname));
//                    userJson.putString("headimgurl", headImgUrl);
//                    userJson.putString("unionid", unionId);
//                    userJson.putInt("sex", sex);
//                    long maxId = Manager.getInstance().generatePlayerId(userDao);
//                    Manager.getInstance().buildBaseUser(regInfo, pf, maxId);
//                    WeixinUtil.createRole(userJson, regInfo);
//                    userId = userDao.addUser(regInfo);
//                    userRelationDao.insert(new ThirdRelation(regInfo.getUserId(), pf, openId));
//                }
            } else {
                userId = regInfo.getUserId();
                GroupUser gu = groupDao.loadGroupUser(userId, groupId);
                if (gu != null) {
                    OutputUtil.output(7, "参数错误：userId为[" + userId + "]的用户已是亲友圈[" + groupId + "]的成员", getRequest(), getResponse(), false);
                    return;
                }
                GroupUser inviter = groupDao.loadGroupUser(inviterId, groupId);
                if (inviter == null || GroupConstants.isMember(inviter.getUserRole())) {
                    OutputUtil.output(5, "邀请人已不在该亲友圈，或邀请人权限不够", getRequest(), getResponse(), false);
                    return;
                }
            }
            MessageBuilder resMsg = MessageBuilder.newInstance();
            resMsg.builderCodeMessage(0, "申请加入成功");
            resMsg.builder("userId", userId);
            resMsg.builder("groupId", groupId);
            List<GroupReview> reviewList = groupDao.loadGroupReviewByUser(userId, groupId);
            if (reviewList != null && reviewList.size() > 0) {
                for (GroupReview review : reviewList) {
                    if (review.getReviewMode() == 0 || review.getReviewMode() == 1 || review.getReviewMode() == 3) {
                        OutputUtil.output(resMsg, getRequest(), getResponse(), null, false);
                        return;
                    }
                }
            }

            // 创建加入亲友圈邀请
            GroupReview groupReview = new GroupReview();
            groupReview.setCreatedTime(new Date());
            groupReview.setCurrentState(0);
            groupReview.setGroupId(groupId);
            groupReview.setGroupName(group.getGroupName());
            groupReview.setReviewMode(3);
            groupReview.setUserId(userId);
            groupReview.setUserName(regInfo.getName());
            groupReview.setCurrentOperator(inviterId);
            if (groupDao.createGroupReview(groupReview) <= 0) {
                OutputUtil.output(9, "操作失败，请稍后再试", getRequest(), getResponse(), false);
                return;
            }

            OutputUtil.output(resMsg, getRequest(), getResponse(), null, false);
            LOGGER.info("applyJoinGroup|succ|" + JSON.toJSONString(params));
        } catch (Exception e) {
            OutputUtil.output(99, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
            LOGGER.error("applyJoinGroup|error|" + e.getMessage(), e);
        }
    }


    /**
     * 解散亲友圈
     */
    public void dissGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("dissGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "参数错误:groupId=" + groupId, getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(2, "亲友圈不存在", getRequest(), getResponse(), false);
                return;
            }
            int count = groupDao.countGroupUser(groupId);
            if (count <= 1) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", groupInfo.getKeyId().toString());
                map.put("groupState", "0");
                int ret1 = groupDao.deleteGroupUserByGroupId(groupId);
                int ret2 = groupDao.updateGroupInfoByKeyId(map);
                int ret3 = groupDao.deleteGroupInfoByParentGroup(groupId);
                groupDao.deleteTeamByGroupKey(String.valueOf(groupId));
                OutputUtil.output(0, "解散亲友圈成功", getRequest(), getResponse(), false);
                LOGGER.info("dissGroup|succ|{}|{}|{}|{}", groupId, ret1, ret2, ret3);
            } else {
                OutputUtil.output(5, "请先踢除成员再解散,当前成员人数 "+count, getRequest(), getResponse(), false);
                LOGGER.info("dissGrou|fail|{}|{}", groupId, count);
            }
        } catch (Exception e) {
            OutputUtil.output(99, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
            LOGGER.error("dissGroup|error|" + e.getMessage(), e);
        }
    }


    /**
     * 复制亲友圈
     */
    public void copyGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("copyGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int destGroupId = NumberUtils.toInt(params.get("destGroupId"), -1);

            if (groupId <= 0 || destGroupId <= 0) {
                OutputUtil.output(1, "参数错误:groupId=" + groupId + ",desgGroupId=" + destGroupId, getRequest(), getResponse(), false);
                return;
            }
            GroupInfo srcGroup = groupDao.loadGroupInfoState0(groupId);
            if (srcGroup == null) {
                OutputUtil.output(2, "亲友圈不存在或未暂停[" + groupId + "]", getRequest(), getResponse(), false);
                return;
            }
            GroupInfo destGroup = groupDao.loadGroupInfoAllState(destGroupId);
            if (destGroup != null) {
                OutputUtil.output(2, "亲友圈已存在[" + destGroupId + "]", getRequest(), getResponse(), false);
                return;
            }

            groupDao.copyGroup(groupId, destGroupId);
            groupDao.copySubGroup(groupId, destGroupId);
            groupDao.copyGroupUser(groupId, destGroupId);
            groupDao.setGroupRelationOldKey(groupId);
            groupDao.copyGroupRelation(groupId, destGroupId);
            groupDao.resetUserGroupForCopy(destGroupId);
            groupDao.copyGroupTableConfig(groupId, destGroupId);
            groupDao.copySubGroupTableConfig(groupId, destGroupId);

            OutputUtil.output(0, "复制成功", getRequest(), getResponse(), false);
            LOGGER.info("copyGroup|succ|" + JSON.toJSONString(params));
        } catch (Exception e) {
            OutputUtil.output(99, "操作失败：请联系系统管理员", getRequest(), getResponse(), false);
            LOGGER.error("copyGroup|error|" + e.getMessage(), e);
        }
    }

}
