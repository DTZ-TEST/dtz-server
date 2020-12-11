package com.sy.controller;

import com.sy.entity.pojo.UserGroupPlaylog;
import com.sy.entity.pojo.UserInfo;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.redpack.*;
import com.sy.redpack.textmsg.TextMessage;
import com.sy.redpack.textmsg.util.MessageUtil;
import com.sy.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/**"})
public class GlobalController extends BaseController {

    private static final Logger monitor = LoggerFactory.getLogger("MONITOR");

    private static final Logger logger = LoggerFactory.getLogger(GlobalController.class);

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/{varAgencyId:\\w+}"})
    public String auto(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAgencyId") String agencyId) throws Exception {
        SessionUtil.clearSession(request);
        if (agencyId.length() == 6 && CommonUtil.isPureNumber(agencyId)) {
            monitor.info("success register by agencyId:{}", agencyId);
            request.setAttribute("agencyId", agencyId);
            return "register";
        } else if (agencyId.length() > 6) {
            CacheEntity<Integer> cacheEntity = CacheEntityUtil.getCache("agencyId:" + agencyId);
            if (cacheEntity != null && cacheEntity.getValue() != null) {
                monitor.info("success register by tempAgencyId:{},agencyId:{}", agencyId, cacheEntity.getValue());
                request.setAttribute("agencyId", cacheEntity.getValue());
                request.setAttribute("agencyId0", agencyId);
            } else {
                monitor.error("fail register by tempAgencyId:{}", agencyId);
            }
            return "register";
        } else {
            monitor.error("fail register by agencyId:{}", agencyId);
            return "login";
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/**"})
    public String all(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionUtil.clearSession(request);
        logger.error("page is not exists:{},{}", request.getRequestURI(),
                new StringBuilder(200).append("IP:").append(IpUtil.getIpAddrByRequest(request))
                        .append(",User-Agent:").append(request.getHeader("User-Agent"))
                        .append(",Referer:").append(request.getHeader("Referer")));
        return "login";
    }

    @Override
    public String verificate() {
        return null;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/d3/{varAid:\\d{6}}"})
    public String download(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAid") String tempId) throws Exception {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (StringUtils.isNotEmpty(context)) {
            uri = uri.substring(context.length());
        }
        request.setAttribute("uri", "noauth/download/proxy" + uri.substring(3));
        request.setAttribute("appid", "wx2338735ace5a27c9");
        request.setAttribute("pid", uri.substring(4));
        logger.info("GlobalController|download|inviterId:{}", uri.substring(4));
        return "proxy";
    }

    private void sendText(HttpServletRequest request, HttpServletResponse response, String fromUserName, String toUserName, String content) {
        TextMessage text = new TextMessage();
        text.setContent(content);
        text.setToUserName(fromUserName);
        text.setFromUserName(toUserName);
        text.setCreateTime(new Date().getTime());
        text.setMsgType("text");
        String respMessage = MessageUtil.textMessageToXml(text);
        OutputUtil.output(1000, respMessage, request, response, false);
    }

    /**
     * 转盘抽奖活动提现
     *
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/withDraw"})
    public String withDraw(HttpServletRequest request, HttpServletResponse response) throws Exception {
        synchronized (GlobalController.class) {
            String signature = request.getParameter("signature");// 微信加密签名
            String timestamp = request.getParameter("timestamp");// 时间戳
            String nonce = request.getParameter("nonce");// 随机数
            String echostr = request.getParameter("echostr");// 登陆验证
            if (echostr != null && !echostr.isEmpty()) {//这里 echostr 的值必须返回，否则微信认为请求失败
                PrintWriter pw = response.getWriter();
                pw.write(echostr);  //这里 echostr 的值必须返回，否则微信认为请求失败
                pw.flush();
                pw.close();
                return "";
            }
            String openid = request.getParameter("openid");// 客户端主动回复口令会带过来openid
            if (openid == null || openid.isEmpty()) {
                logger.error("openId不存在！");
                return "";
            }
            String fromUserName = request.getParameter("FromUserName");// 公众帐号
            String toUserName = request.getParameter("ToUserName");// 玩家openId
            String msgType = request.getParameter("MsgType");// 消息类型
            String content = request.getParameter("Content");// 消息内容
            Date drawStartDate = new Date(TimeUtil.parseTimeInMillis(WeixinRedbagConfig.getStartTime() + " 00:00:00"));
            Date drawEndDate = new Date(TimeUtil.parseTimeInMillis(WeixinRedbagConfig.getEndTime() + " 00:00:00"));
            Date date = new Date();
            if (!(msgType.equals("text") && content.equals(WeixinRedbagConfig.getToken()))) {
                sendText(request, response, fromUserName, toUserName, "欢迎关注" + WeixinRedbagConfig.getGameName() + "，参与游戏内" + WeixinRedbagConfig.getActivityName() + "获得现金奖励的朋友，请输入口令：" + WeixinRedbagConfig.getToken());
                return "";
            }
            if (date.before(drawStartDate)) {
                sendText(request, response, fromUserName, toUserName, "活动还未开始，请稍后再来哦！");
                return "";
            }
            if (date.after(drawEndDate)) {
                sendText(request, response, fromUserName, toUserName, "红包活动已结束！");
                return "";
            }
            String accessToken = WechatUtils.getNewAccessToken();
            logger.info("accessToken:" + accessToken + "---toUserName:" + toUserName + "---openid:" + openid);
            WeixinUser user = WechatUtils.getUserInfo(accessToken, openid);// 通过玩家openId 获得unionId
            if (user == null) {
                logger.error("用户信息获取失败！---accessToken:" + accessToken + "openid:" + openid);
                sendText(request, response, fromUserName, toUserName, "用户信息获取失败！");
                return "";
            }
            String unionId = user.getUnionid();
            String searchUserIdSql = SqlHelperUtil.getString("identity_userId", Constant.USER_INFO_FILE);
            long userId = commonManager.count(searchUserIdSql, new Object[]{unionId});
            logger.info("unionId:" + unionId + "---userId:" + userId);
            if (userId <= 0) {
                sendText(request, response, fromUserName, toUserName, user.getNickname() + "，您好！请用您登陆" + WeixinRedbagConfig.getGameName() + "的微信账号关注" + WeixinRedbagConfig.getAppName() + "才可以领取红包奖励哦!");
                return "";
            }
            List<RedBagInfo> list = commonManager.findList(SqlHelperUtil.getString("select", Constant.RED_BAG_FILE),
                    new Object[]{userId}, RedBagInfo.class);
            float canReceiveRedBag = getRedbagReceiveAmount(list);
            DecimalFormat fnum = new DecimalFormat("#0.00");
            float sendAccRedBagNum = Float.parseFloat(fnum.format(canReceiveRedBag));
            logger.info("userId:" + userId + "---canReceiveRedBag:" + sendAccRedBagNum);
            if (sendAccRedBagNum == 0) {// 总金额低于5元时
                sendText(request, response, fromUserName, toUserName, "抱歉！您的红包金额为0，不能提现哦！");
                return "";
            }
            if (sendAccRedBagNum < 1) {// 总金额低于5元时
                sendText(request, response, fromUserName, toUserName, "抱歉！您的累计红包金额低于1元，不能提现哦！");
                return "";
            }

            String getTodayUserRedBagNumSQL = SqlHelperUtil.getString("getTodayUserRedBagNum", Constant.RED_BAG_FILE);
            float todayTotalNum = commonManager.count(getTodayUserRedBagNumSQL, new Object[]{});
            if (todayTotalNum > Integer.parseInt(WeixinRedbagConfig.getRedbagMaxNum())) {
                sendText(request, response, fromUserName, toUserName, "活动太火爆了，今日系统红包已发放完毕，请您明日再领噢！");
                return "";
            }
            if (!activitySatisfy(request, response, fromUserName, toUserName, userId, content)) {// 活动额外条件满足才能领取红包
                return "";
            }
            WechatRedPackResponse sendResponse = RedPackSender.sendRedBag(fromUserName, sendAccRedBagNum);
            if (sendResponse == null) {
                sendText(request, response, fromUserName, toUserName, "红包发送失败，系统异常！");
                return "";
            }
            if (sendResponse != null && sendResponse.getErr_code_des().equals("发放失败，此请求可能存在风险，已被微信拦截")) {
                sendText(request, response, fromUserName, toUserName, "发放失败，此请求可能存在风险，已被微信拦截（如您未实名认证，请先实名认证后，再提现！）");
                return "";
            }
            if (sendResponse != null && !sendResponse.getResult_code().equals("SUCCESS")) {
                sendText(request, response, fromUserName, toUserName, "红包发送失败，服务出现未知故障，请联系客服！");
                return "";
            }
            if (sendResponse != null && sendResponse.getResult_code().equals("SUCCESS")) {// 发送成功
                logger.info("红包发送成功:" + sendAccRedBagNum);
                for (RedBagInfo record : list) {// 更新红包状态
                    if (record.getRedBagType() == 2 && record.getDrawDate() == null) {
                        record.setDrawDate(new Date());
                        String updateSql = SqlHelperUtil.getString("save_update", Constant.RED_BAG_FILE);
                        commonManager.saveOrUpdate(updateSql, new Object[]{record.getDrawDate(), record.getUserId(), record.getReceiveDate(), record.getRedBagType()});
                    }
                }
                logger.info("更新红包状态成功");
                sendText(request, response, fromUserName, toUserName, "恭喜您提现成功！获得" + sendAccRedBagNum + "元红包！");
            }
            return "";
        }
    }

    private boolean activitySatisfy(HttpServletRequest request, HttpServletResponse response, String fromUserName, String toUserName, long userId, String token) {
        if (token.equals("小甘红包")) {// 小甘红包活动需要满50局才能领取红包
            try {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(userId);
                userInfo = commonManager.findOne(userInfo);
                if (userInfo != null && userInfo.getTotalCount() >= WeixinRedbagConfig.getXgmjBureau()) {
                    return true;
                } else {
                    if (userInfo == null) {
                        sendText(request, response, fromUserName, toUserName, "您已触发系统风控，需要进入小甘麻将完成" + WeixinRedbagConfig.getXgmjBureau() + "小局游戏才能领奖噢！");
                    } else {
                        int leftBureau = (int) (WeixinRedbagConfig.getXgmjBureau() - userInfo.getTotalCount());
                        sendText(request, response, fromUserName, toUserName, "您已触发系统风控，需要再完成" + leftBureau + "小局游戏才能领奖噢！");
                    }
                    return false;
                }
            } catch (Exception e) {
                logger.error("查询用户局数失败");
                return true;
            }
        } else
            return true;
    }

    private float getRedbagReceiveAmount(List<RedBagInfo> list) {
        float canReceiveRedBag = 0.0f;
        if (list != null && !list.isEmpty()) {
            for (RedBagInfo record : list) {
                if (record.getRedBagType() == 2 && record.getDrawDate() == null) {
                    canReceiveRedBag += record.getRedbag();
                }
            }
        }
        return canReceiveRedBag;
    }

    /**
     * 今日已累计领取红包金额
     *
     * @param list
     * @return
     */
    private float getReceivedRedbagNum(List<RedBagInfo> list) {
        float receivedRedBag = 0.0f;
        long curTime = System.currentTimeMillis();
        if (list != null && !list.isEmpty()) {
            for (RedBagInfo record : list) {
                if (record.getRedBagType() == 2 && record.getDrawDate() != null && TimeUtil.isSameDay(curTime, record.getDrawDate().getTime())) {
                    receivedRedBag += record.getRedbag();
                }
            }
        }
        return receivedRedBag;
    }

    private float getUserRedBagRecords(List<UserRedBagRecord> list) {
        float canReceiveRedBag = 0;
        if (list != null && !list.isEmpty()) {
            for (UserRedBagRecord record : list) {
                for (SelfRedBagReceiveRecord receiveRecord : record.getReceiveRecordList()) {
                    if (!receiveRecord.isWithDraw()) {
                        canReceiveRedBag += receiveRecord.getReceiveNum();
                    }
                }
            }
        }
        return canReceiveRedBag;
    }

    /**
     * 领取红包页面和领取红包接口
     *
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/redBag"})
    public String redBag(HttpServletRequest request, HttpServletResponse response) throws Exception {
        synchronized (GlobalController.class) {
            String code = request.getParameter("code");// 微信授权code
            String openId = request.getParameter("openId");// 微信授权code
            String page = "redBagPage";
            request.setAttribute("resultCode", -1);
            request.setAttribute("appid", WeixinRedbagConfig.getAppId());
            if (StringUtils.isBlank(code) && StringUtils.isBlank(openId)) {
                //---------------------------------------------------------
                //第一步：微信授权，获取用户信息
                request.setAttribute("resultCode", 1);
                return page;
            }
            Date drawStartDate = new Date(TimeUtil.parseTimeInMillis(WeixinRedbagConfig.getStartTime() + " 00:00:00"));
            Date drawEndDate = new Date(TimeUtil.parseTimeInMillis(WeixinRedbagConfig.getEndTime() + " 00:00:00"));
            Date date = new Date();
            if (date.before(drawStartDate)) {
                request.setAttribute("resultMsg", "活动还未开始，请稍后再来哦！");
                return page;
            }
            if (date.after(drawEndDate)) {
                request.setAttribute("resultMsg", "红包活动已结束！");
                return page;
            }
            if (StringUtils.isNotBlank(code)) {
                //---------------------------------------------------------
                // 第二步：微信授权后，获取微信用户信息，返回给前端显示用户可以领取的红包数
                WeixinUser wxUser = WechatUtils.getUserInfo(code);
                if (wxUser == null || StringUtils.isBlank(wxUser.getOpenid()) || StringUtils.isBlank(wxUser.getUnionid())) {
                    request.setAttribute("resultMsg", "用户信息获取失败！");
                    return page;
                }
                String unionId = wxUser.getUnionid();
                String searchUserIdSql = SqlHelperUtil.getString("identity_userId", Constant.USER_INFO_FILE);
                long userId = commonManager.count(searchUserIdSql, new Object[]{unionId});
                logger.info("unionId:" + unionId + "---userId:" + userId);
                if (userId <= 0) {
                    request.setAttribute("resultMsg", "您好！请用您登陆" + WeixinRedbagConfig.getGameName() + "的微信账号关注" + WeixinRedbagConfig.getAppName() + "才可以领取红包奖励哦!");
                    return page;
                }
                List<RedBagInfo> list = commonManager.findList(SqlHelperUtil.getString("select", Constant.RED_BAG_FILE), new Object[]{userId}, RedBagInfo.class);
                float canReceiveRedBag = getRedbagReceiveAmount(list);
                DecimalFormat fnum = new DecimalFormat("#0.00");
                float sendAccRedBagNum = Float.parseFloat(fnum.format(canReceiveRedBag));
                logger.info("userId:" + userId + "---canReceiveRedBag:" + sendAccRedBagNum);
                if (sendAccRedBagNum == 0) {
                    request.setAttribute("resultMsg", "抱歉！您的红包金额为0，不能提现哦！");
                    return page;
                }
                if (sendAccRedBagNum < 1) {
                    request.setAttribute("resultMsg", "抱歉！您的累计红包金额低于1元，不能提现哦！");
                    return page;
                }

                String getTodayUserRedBagNumSQL = SqlHelperUtil.getString("getTodayUserRedBagNum", Constant.RED_BAG_FILE);
                float todayTotalNum = commonManager.count(getTodayUserRedBagNumSQL, new Object[]{});
                if (todayTotalNum > Integer.parseInt(WeixinRedbagConfig.getRedbagMaxNum())) {
                    request.setAttribute("resultMsg", "活动太火爆了，今日系统红包已发放完毕，请您明日再领噢！");
                    return page;
                }

                request.setAttribute("resultCode", 2);
                request.setAttribute("openId", wxUser.getOpenid());
                request.setAttribute("unionId", unionId);
                request.setAttribute("sendAccRedBagNum", sendAccRedBagNum);
                return page;
            }
            String unionId = request.getParameter("unionId");
            try {
                //---------------------------------------------------------
                //第三步：点击领取红包
                String searchUserIdSql = SqlHelperUtil.getString("identity_userId", Constant.USER_INFO_FILE);
                long userId = commonManager.count(searchUserIdSql, new Object[]{unionId});
                if (userId <= 0) {
                    response.getWriter().write("您好！请用您登陆" + WeixinRedbagConfig.getGameName() + "的微信账号关注" + WeixinRedbagConfig.getAppName() + "才可以领取红包奖励哦!");
                    return "";
                }
                List<RedBagInfo> list = commonManager.findList(SqlHelperUtil.getString("select", Constant.RED_BAG_FILE), new Object[]{userId}, RedBagInfo.class);
                float canReceiveRedBag = getRedbagReceiveAmount(list);
                DecimalFormat fnum = new DecimalFormat("#0.00");
                float sendAccRedBagNum = Float.parseFloat(fnum.format(canReceiveRedBag));
                logger.info("userId:" + userId + "---canReceiveRedBag:" + sendAccRedBagNum);
                if (sendAccRedBagNum == 0) {
                    response.getWriter().write("抱歉！您的红包金额为0，不能提现哦！");
                    response.getWriter().flush();
                    return null;
                }
                if (sendAccRedBagNum < 1) {
                    response.getWriter().write("抱歉！您的累计红包金额低于1元，不能提现哦！");
                    response.getWriter().flush();
                    return null;
                }

                String getTodayUserRedBagNumSQL = SqlHelperUtil.getString("getTodayUserRedBagNum", Constant.RED_BAG_FILE);
                float todayTotalNum = commonManager.count(getTodayUserRedBagNumSQL, new Object[]{});
                if (todayTotalNum > Integer.parseInt(WeixinRedbagConfig.getRedbagMaxNum())) {
                    response.getWriter().write("活动太火爆了，今日系统红包已发放完毕，请您明日再领噢！");
                    response.getWriter().flush();
                    return null;
                }
                WechatRedPackResponse sendResponse = RedPackSender.sendRedBag(openId, sendAccRedBagNum);
                if (sendResponse == null) {
                    response.getWriter().write("红包发送失败，系统异常！");
                    response.getWriter().flush();
                    return null;
                }
                if (sendResponse != null && sendResponse.getErr_code_des().equals("发放失败，此请求可能存在风险，已被微信拦截")) {
                    response.getWriter().write("发放失败，此请求可能存在风险，已被微信拦截（如您未实名认证，请先实名认证后，再提现！）");
                    response.getWriter().flush();
                    return null;
                }
                if (sendResponse != null && !sendResponse.getResult_code().equals("SUCCESS")) {
                    response.getWriter().write("红包发送失败，服务出现未知故障，请联系客服！");
                    response.getWriter().flush();
                    return null;
                }
                if (sendResponse != null && sendResponse.getResult_code().equals("SUCCESS")) {// 发送成功
                    logger.info("红包发送成功:" + sendAccRedBagNum);
                    for (RedBagInfo record : list) {// 更新红包状态
                        if (record.getRedBagType() == 2 && record.getDrawDate() == null) {
                            record.setDrawDate(new Date());
                            String updateSql = SqlHelperUtil.getString("save_update", Constant.RED_BAG_FILE);
                            commonManager.saveOrUpdate(updateSql, new Object[]{record.getDrawDate(), record.getUserId(), record.getReceiveDate(), record.getRedBagType()});
                        }
                    }
                    logger.info("更新红包状态成功");
                }
                response.getWriter().write("恭喜您提现成功！获得" + sendAccRedBagNum + "元红包！");
                response.getWriter().flush();
                return null;
            } catch (Exception e) {
                response.getWriter().write("系统出错：请联系管理员");
                response.getWriter().flush();
                logger.error("redBag|error|" + openId + "|" + unionId, e);
            }
            return null;
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/d9/{varAid}"})
    public String knowGroupPlayLog(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAid") String tempId) throws Exception {

        Map<String, String> params = UrlParamUtil.getParameters(request);
//    	SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_2");
        logger.info("knowGroupPlayLog tempId:" + tempId);

        UserGroupPlaylog userGroupPlaylog = new UserGroupPlaylog();
        userGroupPlaylog.setId(Long.valueOf(tempId));
        userGroupPlaylog = commonManager.findOne(userGroupPlaylog);

        if (userGroupPlaylog == null) {
            logger.info("knowGroupPlayLog userGroupPlaylog:" + userGroupPlaylog);
        }

        String difen = String.valueOf(userGroupPlaylog.getDiFen());
        String totalCount = String.valueOf(userGroupPlaylog.getTotalCount());
        String gameName = String.valueOf(userGroupPlaylog.getGamename());
        String groupId = String.valueOf(userGroupPlaylog.getGroupid());
        String playerCount = String.valueOf(userGroupPlaylog.getPlayercount());
        String playNo = String.valueOf(userGroupPlaylog.getCount());
        String roomId = String.valueOf(userGroupPlaylog.getTableid());
        String overTime = userGroupPlaylog.getOvertime();
//        String player1Name = params.get("player1Name").trim();
//        String player2Name = params.get("player2Name").trim();

        String players = userGroupPlaylog.getPlayers();
        String playersSZ[] = players.split(",");

        String Score = userGroupPlaylog.getDiFenScore();
        String score[] = Score.split(",");

        String player1No = score[0];
        String player2No = score[1];
        String player1ID = playersSZ[0];
        String player2ID = playersSZ[1];

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Long.valueOf(player1ID));
        userInfo = commonManager.findOne(userInfo);
        logger.info("player1Name={},player1ID={}", userInfo.getName(), player1ID);
        String player1Name = userInfo.getName();

        if (userInfo.getHeadimgurl() == null || userInfo.getHeadimgurl().length() == 0) {
            if (userInfo.getSex() == 1) {
                setSessionValue(request, "player1img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
            } else {
                setSessionValue(request, "player1img", "http://down.apk.51nmw.com/anhua/res/default_w.png");
            }

        } else {
            setSessionValue(request, "player1img", userInfo.getHeadimgurl());
        }

        UserInfo userInfo2 = new UserInfo();
        userInfo2.setUserId(Long.valueOf(player2ID));
        userInfo2 = commonManager.findOne(userInfo2);
        logger.info("player2Name={},player2ID={}", userInfo2.getName(), player2ID);
        String player2Name = userInfo2.getName();

        if (userInfo2.getHeadimgurl() == null || userInfo2.getHeadimgurl().length() == 0) {
            if (userInfo2.getSex() == 1) {
                setSessionValue(request, "player2img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
            } else {
                setSessionValue(request, "player2img", "http://down.apk.51nmw.com/anhua/res/default_w.png");
            }

        } else {
            setSessionValue(request, "player2img", userInfo2.getHeadimgurl());
        }

        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (StringUtils.isNotEmpty(context)) {
            uri = uri.substring(context.length());
        }
        logger.info("download1 url：" + uri);
        logger.info("noauth/share/proxy" + uri.substring(3));
        request.setAttribute("uri", "noauth/share/proxy" + uri.substring(3));
        request.setAttribute("appid", PropUtil.getString("playlog_appid"));

        setSessionValue(request, "difen", difen);
        setSessionValue(request, "totalCount", totalCount);
        setSessionValue(request, "groupId", groupId);
        setSessionValue(request, "playerCount", playerCount);
        setSessionValue(request, "playNo", playNo);
        setSessionValue(request, "roomId", roomId);
        setSessionValue(request, "overTime", overTime);
        setSessionValue(request, "player1Name", player1Name);
        setSessionValue(request, "player2Name", player2Name);
        setSessionValue(request, "player1ID", player1ID);
        setSessionValue(request, "player2ID", player2ID);
        setSessionValue(request, "player1No", player1No);
        setSessionValue(request, "player2No", player2No);
        setSessionValue(request, "gameName", gameName);
        if (playerCount.equals("3")) {
            String player3No = score[2];
            String player3ID = playersSZ[2];

            UserInfo userInfo3 = new UserInfo();
            userInfo3.setUserId(Long.valueOf(player3ID));
            userInfo3 = commonManager.findOne(userInfo3);

            String player3Name = userInfo3.getName();
            if (userInfo3.getHeadimgurl() == null || userInfo3.getHeadimgurl().length() == 0) {
                if (userInfo3.getSex() == 1) {
                    setSessionValue(request, "player3img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
                } else {
                    setSessionValue(request, "player3img", "http://down.apk.51nmw.com/anhua/res/default_w.png");
                }

            } else {
                setSessionValue(request, "player3img", userInfo3.getHeadimgurl());
            }

            setSessionValue(request, "player3Name", player3Name);
            setSessionValue(request, "player3ID", player3ID);
            setSessionValue(request, "player3No", player3No);

            logger.info("player3img：" + getSessionValue(request, "player3img"));
        }
        if (playerCount.equals("4")) {
            String player3No = score[2];
            String player3ID = playersSZ[2];

            UserInfo userInfo3 = new UserInfo();
            userInfo3.setUserId(Long.valueOf(player3ID));
            userInfo3 = commonManager.findOne(userInfo3);

            if (userInfo3.getHeadimgurl() == null || userInfo3.getHeadimgurl().length() == 0) {
                if (userInfo3.getSex() == 1) {
                    setSessionValue(request, "player3img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
                } else {
                    setSessionValue(request, "player3img", "http://down.apk.51nmw.com/anhua/res/default_w.png");
                }

            } else {
                setSessionValue(request, "player3img", userInfo3.getHeadimgurl());
            }

            String player3Name = userInfo3.getName();

            setSessionValue(request, "player3Name", player3Name);
            setSessionValue(request, "player3ID", player3ID);
            setSessionValue(request, "player3No", player3No);

            String player4No = score[3];
            String player4ID = playersSZ[3];

            UserInfo userInfo4 = new UserInfo();
            userInfo4.setUserId(Long.valueOf(player4ID));
            userInfo4 = commonManager.findOne(userInfo4);

            if (userInfo4.getHeadimgurl() == null || userInfo4.getHeadimgurl().length() == 0) {
                if (userInfo4.getSex() == 1) {
                    setSessionValue(request, "player4img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
                } else {
                    setSessionValue(request, "player4img", "http://down.apk.51nmw.com/anhua/res/default_w.png");
                }

            } else {
                setSessionValue(request, "player4img", userInfo4.getHeadimgurl());
            }

            String player4Name = userInfo4.getName();

            setSessionValue(request, "player4Name", player4Name);
            setSessionValue(request, "player4ID", player4ID);
            setSessionValue(request, "player4No", player4No);

            logger.info("player3img：" + getSessionValue(request, "player3img"));
            logger.info("player4img：" + getSessionValue(request, "player4img"));
        }


        return "proxy9";
    }
}
