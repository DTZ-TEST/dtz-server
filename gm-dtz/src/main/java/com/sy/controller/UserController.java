package com.sy.controller;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sy.util.CashIncomeUtil;
import com.sy.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sy.entity.pojo.ActivityReward;
import com.sy.entity.pojo.AgencyPayInfo;
import com.sy.entity.pojo.AgencyPayInfoList;
import com.sy.entity.pojo.AgencyShow;
import com.sy.entity.pojo.BackCardInfo;
import com.sy.entity.pojo.BuyCardInfo;
import com.sy.entity.pojo.CashLog;
import com.sy.entity.pojo.Group;
import com.sy.entity.pojo.GroupUser;
import com.sy.entity.pojo.Matchjl;
import com.sy.entity.pojo.OrderInfo;
import com.sy.entity.pojo.PayInfo;
import com.sy.entity.pojo.QueryRoomInfo;
import com.sy.entity.pojo.Room;
import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.RoomCardOrder;
import com.sy.entity.pojo.RoomCardRecord;
import com.sy.entity.pojo.ServerConfig;
import com.sy.entity.pojo.SystemUser;
import com.sy.entity.pojo.UserInfo;
import com.sy.general.GeneralHelper;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.manager.CommonManager;
import com.sy.util.AccountUtil;
import com.sy.util.Constant;
import com.sy.util.LanguageUtil;
import com.sy.util.PropUtil;
import com.sy.util.SessionUtil;
import com.sy.util.SqlHelperUtil;
import com.sy.util.StringUtil;
import com.sy.util.statistics.CommonDataStatistics;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/user/*"})
public class UserController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/state"})
    public void userState(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        try {
            RoomCard roomCard = loadRoomCard(request);
            SystemUser user=loadSystemUser(request);
            if (roomCard == null || roomCard.getAgencyLevel() == null || (user.getRoleId()==null||user.getRoleId().intValue()<=0)) {
                OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
                return;
            }

            String userId = params.get("playerId");
            String type = params.get("type");
            String userState = params.get("userState");

            //String checkResult = StringUtil.checkBlank(false, params, "playerId", "type", "userState", "3");
            // (checkResult != null) {
               // OutputUtil.output(1001, checkResult, request, response, false);
               // return;
           // } else {
                int stateVal = -1;
                if ("red".equals(type)) {
                    if ("1".equals(userState)) {
                        stateVal = 2;
                    } else if ("2".equals(userState)) {
                        stateVal = 1;
                    }
                } else if ("forbid".equals(type)) {
                    if ("1".equals(userState)) {
                        stateVal = 0;
                    } else if ("2".equals(userState)) {
                        stateVal = 1;
                    }
                } else {
                    OutputUtil.output(1002, "Not Support!", request, response, false);
                    return;
                }

                if (stateVal == -1) {
                    OutputUtil.output(1003, "Not Support!", request, response, false);
                    return;
                }
            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            int result = commonManager.saveOrUpdate(dataBase,SqlHelperUtil.getString("user_state", Constant.USER_INFO_FILE), new Object[]{stateVal, userId});
                if (result > 0) {
                    logger.info("success change user state:value={},userId={},agencyId={}", stateVal, userId, roomCard.getAgencyId());
                    OutputUtil.output(1000, LanguageUtil.getString("operate_success"), request, response, false);

                    UserInfo userInfo=new UserInfo();
                    userInfo.setUserId(Long.parseLong(userId));
                    userInfo=commonManager.findOne(dataBase,userInfo);

                    if (userInfo!=null&&userInfo.getEnterServer()!=null&&userInfo.getEnterServer().intValue()>0){
                        ServerConfig serverConfig=new ServerConfig();
                        serverConfig.setId(userInfo.getEnterServer());
                        serverConfig=commonManager.findOne(dataBase,serverConfig);

                        if (serverConfig!=null){
                            String url=serverConfig.getIntranet();
                            if (StringUtils.isBlank(url)){
                                url=serverConfig.getHost();
                            }

                            if (StringUtils.isNotBlank(url)){
                                int idx=url.indexOf(".");
                                if (idx>0){
                                    idx=url.indexOf("/",idx);
                                    if (idx>0){
                                        url=url.substring(0,idx);
                                    }
                                    url+="/online/notice.do?type=playerUserState&userId="+userId+"&message="+stateVal;
                                    String noticeRet = HttpUtil.getUrlReturnValue(url);
                                    logger.info("notice result:url={},ret={}",url,noticeRet);
                                }
                            }
                        }

                   // }
                } else {
                    logger.info("fail change user state:value={},userId={},agencyId={}", stateVal, userId, roomCard.getAgencyId());
                    OutputUtil.output(1005, LanguageUtil.getString("operate_fail"), request, response, false);
                }
            }
        }catch (Exception e){
            logger.error("Exception:"+e.getMessage(),e);
            OutputUtil.output(1006, LanguageUtil.getString("operate_fail"), request, response, false);
        }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/forgot"})
    public void forgot(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String checkBlank = StringUtil.checkBlank(false, params, "pwdNew", "telCode", "tel");
        if (checkBlank != null) {
            logger.error(checkBlank);
            OutputUtil.output(1001, checkBlank, request, response, false);
            return;
        }

        String pwd=params.get("pwdNew").trim();
        String telCode=params.get("telCode").trim();
        String tel=params.get("tel").trim();

        Long telCodeExpire = getSessionValue(request, "telCode_expire");

        if (telCodeExpire == null || System.currentTimeMillis() - telCodeExpire > 0) {
            OutputUtil.output(1006, LanguageUtil.getString("code_timeout"), request, response, false);
            return;
        }

        if (!(tel + "," + telCode).equalsIgnoreCase(String.valueOf(getSessionValue(request, "telCode")))) {
            OutputUtil.output(1006, LanguageUtil.getString("tel_code_error"), request, response, false);
            return;
        }

        SystemUser systemUser = new SystemUser();
        systemUser.setUserTel(tel);

        if ((systemUser=commonManager.findOne(systemUser)) == null) {
            OutputUtil.output(1002, LanguageUtil.getString("user_not_exists"), request, response, false);
            return;
        }else{
            systemUser.setUserPwd(pwd);
            systemUser.setModifiedTime(new Date());
            commonManager.update(systemUser,new String[]{"user_id"},new Object[]{systemUser.getUserId()});
            logger.info("forgot pwd success:tel={}",tel);
            OutputUtil.output(1000, LanguageUtil.getString("forgot_success"), request, response, false);
            return;
        }

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/ip/players"})
    public void loadIpPlayers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String ip = params.get("ip");

        if (!GeneralHelper.isStrIPAddress(ip)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("param_error"))
                    , request, response, null, false);
            return;
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        UserInfo userInfo = new UserInfo();
        userInfo.setIp(ip);
        List<UserInfo> list= commonManager.findList(dataBase,userInfo);

       OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                                .builder("datas", list==null?"[]":list)
                        , request, response, null, false);

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/info"})
    public void loadUserInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        Long userId = NumberUtils.toLong(params.get("userId"), 0);

        if (userId <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                    , request, response, null, false);
            return;
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo = commonManager.findOne(dataBase,userInfo);

        if (userInfo == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                    , request, response, null, false);
        } else {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                                .builder("info", userInfo)
                        , request, response, null, false);
           
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/info"})
    public void loadAgencyInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
        if (agencyId <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , request, response, null, false);
            return;
        }

        SystemUser systemUser = loadSystemUser(request);
        if (systemUser == null || systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0) {
            OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        RoomCard roomCard0 = new RoomCard();
        roomCard0.setAgencyId(agencyId);
        roomCard0 = commonManager.findOne(roomCard0);

        if (roomCard0 == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , request, response, null, false);
        } else{
        	 MessageBuilder messageBuilder=MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                     .builder("info", roomCard0);

             if ("1".equals(request.getParameter("token"))){
                 String token=UUID.randomUUID().toString();
                 messageBuilder.builder("token",token);
                 CacheEntityUtil.setCache(token,new CacheEntity<>(roomCard0,5*60));
             }

             OutputUtil.output(messageBuilder, request, response, null, false);
        }

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cards/statistics"})
    public void cardsStatistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", list = new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        list = commonManager.find(dataBase,SqlHelperUtil.getString("count", Constant.CARDS_STATISTICS_FILE)
                , new Object[]{CommonUtil.dateTimeToString(date1, "yyyy-MM-dd"), CommonUtil.dateTimeToString(date2, "yyyy-MM-dd")});
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                .builder("datas", list != null ? list : (list = new ArrayList<>())), request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/logout"})
    public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("logout params:{}", params);
        SessionUtil.clearSession(request);
        return "login";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/register"})
    public void saveregister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("register params:{}", params);

        String checkResult = StringUtil.checkBlank(false, params, "telCode", "tel"
                , "inviterId", "pwd");

        if (checkResult != null) {
            logger.error(checkResult);
            OutputUtil.output(1001, checkResult, request, response, false);
            return;
        }

        String telCode = params.get("telCode").trim();
        String tel = params.get("tel").trim();
        String inviterId = params.get("inviterId").trim();
        String pwd = params.get("pwd").trim();

        Long telCodeExpire = getSessionValue(request, "telCode_expire");

        if (telCodeExpire == null || System.currentTimeMillis() - telCodeExpire > 0) {
            OutputUtil.output(1006, LanguageUtil.getString("code_timeout"), request, response, false);
            return;
        }

        if (!(tel + "," + telCode).equalsIgnoreCase(String.valueOf(getSessionValue(request, "telCode")))) {
            OutputUtil.output(1006, LanguageUtil.getString("tel_code_error"), request, response, false);
            return;
        }

        if (!CommonUtil.isPureNumber(inviterId)) {
            OutputUtil.output(1002, LanguageUtil.getString("param_error"), request, response, false);
            return;
        }

        RoomCard roomCard = new RoomCard();
        roomCard.setAgencyId(Integer.parseInt(inviterId));
        roomCard = commonManager.findOne(roomCard);
         
        if (roomCard == null) {
            OutputUtil.output(1003, LanguageUtil.getString("auth_error"), request, response, false);
            return;
        }

        int tempAgencyLevel = 0;
        int agencyCost = NumberUtils.toInt(PropUtil.getString("agency_cost"), 500);
        if (roomCard.getAgencyLevel() == null || roomCard.getAgencyLevel() == 0) {
           /* int total = agencyCost > 0 ? commonManager.count(SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                    new Object[]{inviterId, "2017-04-01 00:00:00", CommonUtil.dateTimeToString()}) : 0;*/
           /* if (total >= agencyCost) {*/
                roomCard.setAgencyLevel(1);
                commonManager.update(roomCard, new String[]{"userId"}, new Object[]{roomCard.getUserId()});
                logger.info("agencyId={},agencyLevel={}", inviterId, 1);
           /* } else {
                OutputUtil.output(1005, LanguageUtil.getString("agency_cost_less"), request, response, false);
                return;
            }*/
        } else if (roomCard.getAgencyLevel() < 0) {
            OutputUtil.output(1004, LanguageUtil.getString("agency_forbid"), request, response, false);
            return;
        } else if (roomCard.getAgencyLevel() == 99) {
            String agencyId0 = params.get("agencyId0");
            String cacheKey = null;
            if (StringUtils.isNotBlank(agencyId0)) {
                cacheKey = "agencyId:" + agencyId0.trim();
            }
            CacheEntity<Integer> cacheEntity;
            if (cacheKey == null || (cacheEntity = CacheEntityUtil.getCache(cacheKey)) == null || cacheEntity.getValue().intValue() != roomCard.getAgencyId().intValue()) {
                OutputUtil.output(1004, LanguageUtil.getString("agency_error"), request, response, false);
                return;
            } else {
                CacheEntityUtil.deleteCache(cacheKey);
                CacheEntityUtil.deleteCache("agencyId=" + roomCard.getAgencyId().intValue());
                tempAgencyLevel = 2;
            }
        } else if (roomCard.getAgencyLevel() > 1) {
            tempAgencyLevel = roomCard.getAgencyLevel() - 1;
        }

        SystemUser systemUser = new SystemUser();
        systemUser.setUserTel(tel);
        if (commonManager.findOne(systemUser) != null) {
            OutputUtil.output(1002, LanguageUtil.getString("tel_exists"), request, response, false);
            return;
        }

        Integer inviterKey = roomCard.getUserId();

        systemUser.setIsForbidden(0);
        systemUser.setUserName(tel);
        systemUser.setUserPwd(pwd);
        systemUser.setInviterId(inviterId);
        systemUser.setCreatedTime(new Date());
        systemUser.setModifiedTime(new Date());

        long userId = commonManager.saveAndGetKey(systemUser);

        if (userId > 0) {
            roomCard = new RoomCard();
            roomCard.setAgencyId(StringUtil.loadAgencyId());
            while (commonManager.findOne(roomCard) != null) {
                roomCard.setAgencyId(StringUtil.loadAgencyId());
            }

            roomCard.setUserId((int) userId);
            roomCard.setParentId(inviterKey);
            roomCard.setCreateTime(new Date());
            roomCard.setUpdateTime(new Date());
            roomCard.setPartAdmin(0);
            roomCard.setAgencyPhone(tel);
            roomCard.setCommonCard(0);
            roomCard.setFreeCard(0);
            roomCard.setAgencyLevel(tempAgencyLevel);

            if (commonManager.save(roomCard) > 0) {
                OutputUtil.output(1000, LanguageUtil.getString("register_success") + roomCard.getAgencyId(), request, response, false);

                logger.info("register success:name={},inviter={},agencyId={}", tel, inviterId, roomCard.getAgencyId());
                return;
            }

        }

        OutputUtil.output(1007, LanguageUtil.getString("register_fail"), request, response, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/update"})
    public void saveMsg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("saveMsg params:{}", params);

        String nickname = params.get("nickname");
        String name = params.get("name");
        String tel = params.get("tel");
//        String pwd = params.get("pwd");
        String qq = params.get("qq");
        String wx = params.get("wx");
        String email = params.get("email");
        String bankName = params.get("bankName");
        String bankNum = params.get("bankNum");
        String comment = params.get("comment");
        String pwdNew = params.get("pwdNew");
        String telCode = params.get("telCode");

        SystemUser systemUser = loadSystemUser(request);
        RoomCard roomCard = loadRoomCard(request);
        RoomCard updateRoomCard = new RoomCard();
        boolean isUpdateBase = false;
        boolean isUpdatePwd = false;

        if (StringUtils.isNotBlank(pwdNew)) {
            if (systemUser.getUserPwd().equalsIgnoreCase(pwdNew)) {
                OutputUtil.output(1003, LanguageUtil.getString("user_pwd_match"), request, response, false);
                return;
            }

            if (StringUtils.isBlank(telCode)) {
                OutputUtil.output(1006, LanguageUtil.getString("tel_code_null"), request, response, false);
                return;
            } else {
                telCode = telCode.trim();
                Long telCodeExpire = getSessionValue(request, "telCode_expire");

                if (telCodeExpire == null || System.currentTimeMillis() - telCodeExpire > 0) {
                    OutputUtil.output(1006, LanguageUtil.getString("code_timeout"), request, response, false);
                    return;
                }

                if (!(systemUser.getUserTel() + "," + telCode).equalsIgnoreCase(String.valueOf(getSessionValue(request, "telCode")))) {
                    OutputUtil.output(1006, LanguageUtil.getString("tel_code_error"), request, response, false);
                    return;
                }
            }

            pwdNew = pwdNew.trim();
            systemUser.setUserPwd(pwdNew);
            isUpdatePwd = true;
        } else {
            if (StringUtils.isBlank(roomCard.getUserName()) && StringUtils.isNotBlank(nickname)) {
                roomCard.setUserName(nickname.trim());
                updateRoomCard.setUserName(roomCard.getUserName());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getRemark()) && StringUtils.isNotBlank(name)) {
                roomCard.setRemark(name.trim());
                updateRoomCard.setRemark(roomCard.getRemark());
                isUpdateBase = true;
            }

            if (StringUtils.isBlank(roomCard.getAgencyComment()) && StringUtils.isNotBlank(comment)) {
                roomCard.setAgencyComment(comment.trim());
                updateRoomCard.setAgencyComment(roomCard.getAgencyComment());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getAgencyEmail()) && StringUtils.isNotBlank(email)) {
                roomCard.setAgencyEmail(email.trim());
                updateRoomCard.setAgencyEmail(roomCard.getAgencyEmail());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getAgencyQQ()) && StringUtils.isNotBlank(qq)) {
                roomCard.setAgencyQQ(qq.trim());
                updateRoomCard.setAgencyQQ(roomCard.getAgencyQQ());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getAgencyWechat()) && StringUtils.isNotBlank(wx)) {
                roomCard.setAgencyWechat(wx.trim());
                updateRoomCard.setAgencyWechat(roomCard.getAgencyWechat());
                isUpdateBase = true;
            }
            if (StringUtils.isNotBlank(bankName) && !bankName.equals(roomCard.getBankName())) {
                roomCard.setBankName(bankName.trim());
                updateRoomCard.setBankName(roomCard.getBankName());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getBankCard()) && StringUtils.isNotBlank(bankNum)) {
                roomCard.setBankCard(bankNum.trim().replace(" ", ""));
                updateRoomCard.setBankCard(roomCard.getBankCard());
                isUpdateBase = true;
            }
            if (StringUtils.isBlank(roomCard.getAgencyPhone()) && StringUtils.isNotBlank(tel)) {
                roomCard.setAgencyPhone(tel.trim());
                updateRoomCard.setAgencyPhone(roomCard.getAgencyPhone());
                isUpdateBase = true;
                systemUser.setUserTel(tel.trim());

                if (commonManager.update(systemUser, new String[]{"user_id"}, new Object[]{systemUser.getUserId()}) > 0) {
                    setSessionValue(request, "user", systemUser);
                }
            }
        }

        if (isUpdatePwd) {
            if (commonManager.update(systemUser, new String[]{"user_id"}, new Object[]{systemUser.getUserId()}) > 0) {
                setSessionValue(request, "user", systemUser);
            }
            removeSessionAttribute(request, "telCode");
            OutputUtil.output(1000, LanguageUtil.getString("pwd_update_success"), request, response, false);
        } else if (isUpdateBase) {
            roomCard.setUpdateTime(new Date());
            updateRoomCard.setUpdateTime(roomCard.getUpdateTime());
            if (commonManager.update(updateRoomCard, new String[]{"userId"}, new Object[]{roomCard.getUserId()}) > 0) {
                setSessionValue(request, "roomCard", roomCard);
            }
            OutputUtil.output(1000, LanguageUtil.getString("msg_update_success"), request, response, false);
        } else {
            OutputUtil.output(1001, LanguageUtil.getString("msg_update_fail"), request, response, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/login"})
    public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("login params:{}", params);

        String checkBlank = StringUtil.checkBlank(false, params, "name", "pwd", "code");
        if (checkBlank != null) {
            logger.error(checkBlank);
            OutputUtil.output(1001, checkBlank, request, response, false);
            return;
        }

        String name = params.get("name").trim();
        String pwd = params.get("pwd").trim();
        String code = params.get("code").trim();

        if (!code.equalsIgnoreCase(String.valueOf(getSessionValue(request, Constant.VERCODE_NAME)))) {
            OutputUtil.output(1005, LanguageUtil.getString("code_error"), request, response, false);
        } else {
            SystemUser systemUser = new SystemUser();
            if (StringUtil.isPhoneNumber(name)) {
                systemUser.setUserTel(name);
            } else {
                systemUser.setUserName(name);
            }
            systemUser = commonManager.findOne(systemUser);
            if (systemUser == null) {
                OutputUtil.output(1002, LanguageUtil.getString("user_not_exists"), request, response, false);
            } else {
                if (!pwd.equalsIgnoreCase(systemUser.getUserPwd())) {
                    OutputUtil.output(1003, LanguageUtil.getString("user_pwd_error"), request, response, false);
                } else if (systemUser.getIsForbidden() != 0) {
                    OutputUtil.output(1004, LanguageUtil.getString("user_not_allow_login"), request, response, false);
                } else {
                    RoomCard roomCard = new RoomCard();
                    roomCard.setUserId(systemUser.getUserId());
                    roomCard = commonManager.findOne(roomCard);
                    if (roomCard != null) {
                        setSessionValue(request, "roomCard", roomCard);

                        setSessionValue(request, "user", systemUser);
                        setSessionValue(request, "userSession", UUID.randomUUID().toString());
                        setSessionValue(request, "marqueeText", PropUtil.getString("marqueeText", ""));

                        OutputUtil.output(1000, "success", request, response, false);

                        logger.info("login success:name={}", name);
                    } else {
                        OutputUtil.output(1002, LanguageUtil.getString("user_not_exists"), request, response, false);
                    }
                }
            }
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/msg"})
    public void loadMyMsg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

//        SystemUser systemUser = loadSystemUser(request);
        RoomCard roomCard = loadRoomCard(request);

        OutputUtil.output(MessageBuilder.newInstance()
                        .builder("code", 1000)
                        .builder("myname", StringUtils.isBlank(roomCard.getUserName()) ? "" : roomCard.getUserName())
                        .builder("mycode", roomCard.getAgencyId())
                        .builder("mypic", "")
                        .builder("popMsg", CashIncomeUtil.popMsg(roomCard.getUserId()))
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/players"})
    public void loadMyPlayers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        int count = 0;

        if (roomCard != null) {
            String sql;
            if ("1".equals(params.get("today"))) {
                StringBuilder strBuilder = new StringBuilder(SqlHelperUtil.getString("myplayers_count", Constant.USER_INFO_FILE));
                strBuilder.append(" and payBindTime>=?");
                sql = strBuilder.toString();
            } else {
                sql = SqlHelperUtil.getString("myplayers_count", Constant.USER_INFO_FILE);
            }
            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            count = commonManager.count(dataBase,sql
                    , "1".equals(params.get("today")) ?
                            new Object[]{roomCard.getAgencyId(), CommonUtil.dateTimeToString("yyyy-MM-dd") + " 00:00:00"}
                            : new Object[]{roomCard.getAgencyId()}
            );
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("count", count), request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/players/detail"})
    public void loadMyPlayersDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 50);
        int count = 0;
        RoomCard roomCard = loadRoomCard(request);
        List<UserInfo> result = null;
        if (roomCard != null) {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add(roomCard.getAgencyId());

            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            String sql;
            StringBuilder stringBuilder = new StringBuilder(SqlHelperUtil.getString("myplayers", Constant.USER_INFO_FILE));
            if ("1".equals(params.get("today"))) {
                stringBuilder.append(" and payBindTime>=?");
                paramsList.add(CommonUtil.dateTimeToString("yyyy-MM-dd") + " 00:00:00");
                StringBuilder strBuilder = new StringBuilder(SqlHelperUtil.getString("myplayers_count", Constant.USER_INFO_FILE));
                strBuilder.append(" and payBindTime>=?");
                sql = strBuilder.toString();
            }else{
            	sql = SqlHelperUtil.getString("myplayers_count", Constant.USER_INFO_FILE);
            }

            count = commonManager.count(dataBase,sql
                    , "1".equals(params.get("today")) ?
                            new Object[]{roomCard.getAgencyId(), CommonUtil.dateTimeToString("yyyy-MM-dd") + " 00:00:00"}
                            : new Object[]{roomCard.getAgencyId()});
            stringBuilder.append(" order by payBindTime desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);

            result = commonManager.findList(dataBase,stringBuilder.toString(), paramsList.toArray(), UserInfo.class);
        }

        int page = 0;
        if(count%pageSize== 0){
        	page = count/pageSize;
        }else{
        	page = count/pageSize+1;
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("page", page)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agencies"})
    public void loadMyAgencies(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        int count = 0;
        if (roomCard != null) {
            count = commonManager.count(SqlHelperUtil.getString("myagencies_count", Constant.ROOMCARD_FILE)
                    , new Object[]{roomCard.getUserId()});
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("count", count), request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agencies/detail"})
    public void loadMyAgenciesDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        Integer userId = NumberUtils.toInt(params.get("userId"), 0);
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 50);
        if (userId <= 0) {
            userId = loadRoomCard(request).getUserId();
        }
        List<RoomCard> result = null;
        int count = 0;
        if (userId > 0) {
            result = commonManager.findList(SqlHelperUtil.getString("myagencies_detail", Constant.ROOMCARD_FILE)+" limit "+(pageNo - 1) * pageSize+","+pageSize
                    , new Object[]{userId}, RoomCard.class);
            count = commonManager.count(SqlHelperUtil.getString("myagencies_count", Constant.ROOMCARD_FILE)
                    , new Object[]{userId});
        }
        int page = 0;
        if(count%pageSize== 0){
        	page = count/pageSize;
        }else{
        	page = count/pageSize+1;
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("page", page)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/players/pay"})
    public void loadMyPlayersMoney(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        int[] rets;
        if (roomCard == null) {
            rets = new int[]{0, 0, 0};
        } else {
            rets = new int[3];
            Date date = new Date();
            String cur = CommonUtil.dateTimeToString(date, "yyyy-MM-dd");
            String[] strs = StringUtil.loadWeekRange(date);
            String[] strs1 = StringUtil.loadMonthRange(date);

            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            List<Map<String, Object>> list = commonManager.find(dataBase,SqlHelperUtil.getString("range_sum_today_week_month", Constant.ORDER_INFO_FILE),
                    new Object[]{roomCard.getAgencyId().toString(), cur + " 00:00:00", cur + " 23:59:59"
                            , roomCard.getAgencyId().toString(), strs[0] + " 00:00:00", strs[1] + " 23:59:59"
                            , roomCard.getAgencyId().toString(), strs1[0] + " 00:00:00", strs1[1] + " 23:59:59"});
            if (list != null && list.size() == 3) {
                rets[0] = CommonUtil.object2Int(list.get(0).entrySet().iterator().next().getValue());
                rets[1] = CommonUtil.object2Int(list.get(1).entrySet().iterator().next().getValue());
                rets[2] = CommonUtil.object2Int(list.get(2).entrySet().iterator().next().getValue());
            }
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("today", rets[0])
                        .builder("week", rets[1])
                        .builder("month", rets[2])
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/income/count"})
    public void loadMyIncome(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        //SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),-1));
        String monthStr = params.get("month");
        Calendar cal = Calendar.getInstance();
//        double tempAgencyRatio = 0.003;
        RoomCard roomCard = loadRoomCard(request);
        boolean bl;
        if (roomCard != null) {
            bl = true;
        } else {
            bl = false;
        }
        if (StringUtils.isBlank(monthStr)) {
            monthStr = CommonUtil.dateTimeToString(cal.getTime(), "yyyy-MM");
        } else {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            cal.setTime(CommonUtil.stringToDateTime(monthStr, "yyyy-MM"));
            if (cal.get(Calendar.YEAR) > year) {
                bl = false;
            } else if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) > month) {
                bl = false;
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        List<String> dateList = StringUtil.loadWeekRange1(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH,0);//设置为1号,当前日期既为本月第一天
        //2017-08-07~2017-08-13
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-01~yyyy-MM-dd");
        String lastDay = format.format(cal.getTime());
        dateList.add(lastDay);
        if (bl) {
            List<Object> sqlParams=new ArrayList<>();
            sqlParams.add(roomCard.getAgencyId());

            String tmp="OR ";
            StringBuilder strBuilder=new StringBuilder();
            strBuilder.append(SqlHelperUtil.getString("select",Constant.AGENCY_INCOME_FILE));
            strBuilder.append(" (");
            for (String str : dateList) {
                String[] strs = str.split("\\~");
                sqlParams.add(strs[0].replace("-",""));
                sqlParams.add(strs[1].replace("-",""));
                strBuilder.append("(startDate=? AND endDate=?) ").append(tmp);
            }
            strBuilder.replace(strBuilder.length()-tmp.length(),strBuilder.length(),")");

            List<Map<String,Object>> list=commonManager.find(strBuilder.toString(),sqlParams.toArray());
            Calendar calendar=Calendar.getInstance();
            if (list==null||list.size()==0){
            }else if (list.size()==dateList.size()){
                dateList.clear();
                for (Map<String,Object> tempMap:list){
                    String t1=String.valueOf(tempMap.get("startDate"));
                    String t2=String.valueOf(tempMap.get("endDate"));
                    SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
                    Date d1 = sim.parse(t1);
                    Date d2 = sim.parse(t2);
                    boolean f = true;
                    if((d2.getTime()-d1.getTime()) <=(6*24*60*60*1000)){
                    	f = false;
                    }
                    if (t1.length()==8&&t2.length()==8){
                        String key=new StringBuilder(32).append(t1.substring(0,4)).append("-")
                                .append(t1.substring(4,6)).append("-").append(t1.substring(6))
                                .append("~").append(t2.substring(0,4)).append("-").append(t2.substring(4,6))
                                .append("-").append(t2.substring(6)).toString();
                        map.put(key,(int) (100*CommonUtil.object2Double(tempMap.get("totalIncome"))));
                        String currentState=String.valueOf(tempMap.get("currentState"));
                        if ("0".equals(currentState)){
                            map.put("a"+key,notCanCash(calendar,CommonUtil.object2Int(tempMap.get("endDate")))?"-1":currentState);
                            if(!f){
                            	map.put("a"+key,"-2");
                            }
                        }else{
                            map.put("a"+key,currentState);
                        }
                    }
                }
            }else{
                for (Map<String,Object> tempMap:list){
                    String t1=String.valueOf(tempMap.get("startDate"));
                    String t2=String.valueOf(tempMap.get("endDate"));
                    if (t1.length()==8&&t2.length()==8){
                        String key=new StringBuilder(32).append(t1.substring(0,4)).append("-")
                                .append(t1.substring(4,6)).append("-").append(t1.substring(6))
                                .append("~").append(t2.substring(0,4)).append("-").append(t2.substring(4,6))
                                .append("-").append(t2.substring(6)).toString();
                        map.put(key,(int) (100*CommonUtil.object2Double(tempMap.get("totalIncome"))));
                        String currentState=String.valueOf(tempMap.get("currentState"));
                        if ("0".equals(currentState)){
                            map.put("a"+key,notCanCash(calendar,CommonUtil.object2Int(tempMap.get("endDate")))?"-1":currentState);
                        }else{
                            map.put("a"+key,currentState);
                        }
                        dateList.remove(key);
                    }
                }
            }
            
            int maxLevel = 2;
            if (roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel().intValue() == 99) {
                maxLevel = Integer.MAX_VALUE;
            }
            
            if (dateList.size()>0){
           	    String startTime;
                String endTime;

                for (String str : dateList) {
                    String[] strs = str.split("\\~");
                    startTime = strs[0] + " 00:00:00";
                    endTime = strs[1] + " 23:59:59";
                    int temp = 0;
                    if(!str.equals(lastDay)){
                    	temp = AccountUtil.countAgencyPay(commonManager, roomCard, startTime, endTime);
                    }
                    map.put(str, temp);
                    map.put("a" + str, "-2");
                }
            }
            map.put("month",0);
        } else {
            for (String str : dateList) {
                map.put(str, 0);
                map.put("a"+str,"-2");
            }
            map.put("month",0);
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("month", monthStr)
                        .builder("monthend", lastDay)
                        .builder("datas", map)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/income/count2"})
    public void loadMyIncome2(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String monthStr = params.get("month");
        Calendar cal = Calendar.getInstance();
        if(cal.getTime().getDate() == 1){
        	cal.add(Calendar.DAY_OF_MONTH, -1);
        	monthStr = CommonUtil.dateTimeToString(cal.getTime(), "yyyy-MM");
        }
        RoomCard roomCard = loadRoomCard(request);
        boolean bl;
        if (roomCard != null) {
            bl = true;
        } else {
            bl = false;
        }
        if (StringUtils.isBlank(monthStr)) {
            monthStr = CommonUtil.dateTimeToString(cal.getTime(), "yyyy-MM");
        } else {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            cal.setTime(CommonUtil.stringToDateTime(monthStr, "yyyy-MM"));
            if (cal.get(Calendar.YEAR) > year) {
                bl = false;
            } else if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) > month) {
                bl = false;
            }
        }
        String monthLike = monthStr.replace("-", "");
        Map<String, Object> map ;
        List<Map<String, Object>> result = new ArrayList<>();
        if (bl) {
            List<Object> sqlParams=new ArrayList<>();
            sqlParams.add(roomCard.getAgencyId());
            StringBuilder strBuilder=new StringBuilder();
            strBuilder.append(SqlHelperUtil.getString("select",Constant.AGENCY_INCOME_FILE));
            if("1".equals(params.get("type"))){
            	strBuilder.append(" currentState!=1 ");
            	strBuilder.append(" and totalIncome>0 ");
            }else{
            	 strBuilder.append(" startDate like ? ");
                 sqlParams.add("%"+monthLike+"%");
            }
            strBuilder.append(" and endDate <= 20200605 ");
            strBuilder.append(" order by createdTime desc ");
            List<Map<String,Object>> list=commonManager.find(strBuilder.toString(),sqlParams.toArray());
            if (list !=null&&list.size() >0){
            	 for (Map<String,Object> tempMap:list){
            		 map = new LinkedHashMap<>();
                     String t1=String.valueOf(tempMap.get("startDate"));
                     String t2=String.valueOf(tempMap.get("endDate"));
                     Integer type = Integer.parseInt(String.valueOf(tempMap.get("incomeStatiscType")));
                     String key = "";
                     if (type == 0 && t1.length()==8 && t2.length()==8){
                         key=new StringBuilder(32).append(t1.substring(0,4)).append("-")
                                 .append(t1.substring(4,6)).append("-").append(t1.substring(6))
                                 .append("~").append(t2.substring(0,4)).append("-").append(t2.substring(4,6))
                                 .append("-").append(t2.substring(6)).toString();
                     }else if(type == 1){
                    	 key= t1.substring(0,6)+"(月末补足)";
                     }else{
                    	 key = t1;
                     }
                     map.put(key,(int) (100*CommonUtil.object2Double(tempMap.get("totalIncome"))));
                     String currentState=String.valueOf(tempMap.get("currentState"));
                     map.put("t"+key,type);
                     map.put("m"+key, CommonUtil.object2Int(tempMap.get("minePay"))*10);
                     map.put("d" + key,CommonUtil.object2Int(tempMap.get("agencyPay"))*10);
                     if ("0".equals(currentState)){
                    	 SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
                    	 int num = Integer.parseInt(StringUtils.isBlank(PropUtil.getString("day_num"))?"0":PropUtil.getString("day_num"));
                    	 if(type == 2 &&(new Date().getTime()-sim.parse(key).getTime())<num*24*60*60*1000){
                     		map.put("a"+key,"-2");
                     	 }else if(type == 1 &&(new Date().getTime()-sim.parse(t2).getTime())<num*24*60*60*1000){
                        		map.put("a"+key,"-2");
                         }else if((int) (100*CommonUtil.object2Double(tempMap.get("totalIncome"))) == 0){
                     		map.put("a"+key,"-2");
                     	 }else{
                     		map.put("a"+key,currentState);
                     	}
                     }else{
                         map.put("a"+key,currentState);
                     }
                     result.add(map);
                 }
            }
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("month", monthStr)
                        .builder("datas", result)
                , request, response, null, false);
    }
    /**
     * 每周一十二点之前不能提取上周的返佣
     * @param cal
     * @param dateInt
     * @return
     */
    private static final boolean notCanCash(Calendar cal,int dateInt){
        if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY&&cal.get(Calendar.HOUR_OF_DAY)<12){
            if (Integer.parseInt(CommonUtil.dateTimeToString(cal.getTime(),"yyyyMMdd"))-dateInt<=1){
                return true;
            }
        }
        return false;
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/pay/detail"})
    public void loadPlayerPayDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String userId = params.get("userId");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 50);
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 10) {
            pageSize = 10;
        } else if (pageSize > 50) {
            pageSize = 50;
        }

        int totalSize = 0;
        int totalCount = 0;

        RoomCard roomCard = loadRoomCard(request);
        List<OrderInfo> result = null;
        if (roomCard != null) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Object> paramsList = new ArrayList<>();
            if (roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel().intValue() == 99) {
                Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
                String all = params.get("all");

                List<Integer> list = loadRelationByUserId(commonManager, roomCard.getUserId(), null);
                if (list.size() > 0 && agencyId.intValue() > 0) {
                    if (list.contains(agencyId)) {
                        if ("1".equals(all)) {
                            List<Integer> list0 = loadRelationByAgencyId(commonManager, agencyId, null);

                            if (list0.size() > 0) {
                                stringBuilder.append(" and server_id in(?");
                                paramsList.add(list0.get(0).toString());

                                for (int i = 1; i < list0.size(); i++) {
                                    stringBuilder.append(",?");
                                    paramsList.add(list0.get(i).toString());
                                }

                                stringBuilder.append(")");
                            } else {
                                stringBuilder.append(" and server_id=?");
                                paramsList.add(agencyId.toString());
                            }

                        } else {
                            stringBuilder.append(" and server_id=?");
                            paramsList.add(agencyId);
                        }
                    } else {
                        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                        .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                        .builder("datas", new ArrayList<>())
                                , request, response, null, false);
                        return;
                    }
                } else {
                    if ("1".equals(all) && list.size() > 0) {
                        stringBuilder.append(" and server_id in(?");
                        paramsList.add(list.get(0).toString());

                        for (int i = 1; i < list.size(); i++) {
                            stringBuilder.append(",?");
                            paramsList.add(list.get(i).toString());
                        }

                        stringBuilder.append(")");

                    } else {
                        stringBuilder.append(" and server_id=?");
                        paramsList.add(roomCard.getAgencyId());
                    }
                }
            } else {
                stringBuilder.append(" and server_id=?");
                paramsList.add(roomCard.getAgencyId().toString());
            }

            if (StringUtils.isNotBlank(userId)) {
                stringBuilder.append(" and userId=?");
                paramsList.add(userId);
            }
            if (StringUtils.isNotBlank(startDate)) {
                stringBuilder.append(" and create_time>=?");
                paramsList.add(startDate + " 00:00:00");
            }
            if (StringUtils.isNotBlank(endDate)) {
                stringBuilder.append(" and create_time<=?");
                paramsList.add(endDate + " 23:59:59");
            }

            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


            List<Map<String, Object>> countList = commonManager.find(dataBase,new StringBuilder(SqlHelperUtil.getString("player_order_count", Constant.ORDER_INFO_FILE)).append(stringBuilder.toString()).toString(), paramsList.toArray());

            if (countList != null && countList.size() == 1) {
                totalCount = CommonUtil.object2Int(countList.get(0).get("mycount1"));
                totalSize = CommonUtil.object2Int(countList.get(0).get("mycount2"));

                if (totalSize > 0) {
                    stringBuilder.append(" order by create_time desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);


                    result = commonManager.findList(dataBase,new StringBuilder(SqlHelperUtil.getString("player_order", Constant.ORDER_INFO_FILE)).append(stringBuilder.toString()).toString(), paramsList.toArray(), OrderInfo.class);

                }
            }

        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("totalSize", totalSize).builder("totalCount", totalCount)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agencies/pay"})
    public void loadMyAgenciesMoney(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 Map<String, String> params = UrlParamUtil.getParameters(request);
         logger.info("params:{}", params);

         RoomCard roomCard = loadRoomCard(request);

         int[] results = new int[]{0, 0, 0,0,0,0};

         if (roomCard != null) {

             String cacheKey = "loadMyAgenciesMoney:" + roomCard.getAgencyId();
             CacheEntity<int[]> cacheEntity = CacheEntityUtil.getCache(cacheKey);
             if (cacheEntity != null && cacheEntity.getValue() != null) {
                 results = cacheEntity.getValue();
             } else {
                 Date date = new Date();
                 String cur = CommonUtil.dateTimeToString(date, "yyyy-MM-dd");
                 String[] strs = StringUtil.loadWeekRange(date);
                 String[] strs1 = StringUtil.loadMonthRange(date);

                 int level = 2;
                 if (roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel().intValue() == 99) {
                     level = 200;
                 }
                 results[0] = AccountUtil.countSubAgencyPay(commonManager, roomCard, cur, cur, 0, level,true);
                 results[1] = AccountUtil.countSubAgencyPay(commonManager, roomCard, strs[0], strs[1], 0, level,true);
                 results[2] = AccountUtil.countSubAgencyPay(commonManager, roomCard, strs1[0], strs1[1], 0, level,true);
                 
                 results[3] = AccountUtil.countSubAgencyPay(commonManager, roomCard, cur, cur, 0, level,false);
                 results[4] = AccountUtil.countSubAgencyPay(commonManager, roomCard, strs[0], strs[1], 0, level,false);
                 results[5] = AccountUtil.countSubAgencyPay(commonManager, roomCard, strs1[0], strs1[1], 0, level,false);

                 CacheEntityUtil.setCache(cacheKey, new CacheEntity<>(results, NumberUtils.toInt(PropUtil.getString("agency_multi_cache", "300"), 5 * 60)));
             }
         }
         OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                         .builder("today", results[0])
                         .builder("week", results[1])
                         .builder("month", results[2])
                         .builder("today0", results[3])
                         .builder("week0", results[4])
                         .builder("month0", results[5])
                 , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/pay/detail"})
    public void loadAgencyPayDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String userId = params.get("userId");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        String[] strs = StringUtil.loadWeekRange(new Date());
        if (StringUtils.isBlank(startDate)) {
            startDate = strs[0];
        }
        if (StringUtils.isBlank(endDate)) {
            endDate = strs[1];
        }
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);

        RoomCard roomCard = loadRoomCard(request);

        final int maxLevel = (roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel().intValue() == 99) ? Integer.MAX_VALUE : 2;
        int current = 0;

        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> list0 = commonManager.find(SqlHelperUtil.getString("myagencies_by_userId", Constant.ROOMCARD_FILE),
                new Object[]{roomCard.getUserId()});
        if (list0 != null && list0.size() > 0) {
            if (StringUtils.isNotBlank(userId)) {
                for (Map<String, Object> temp : list0) {
                    if (userId.equals(String.valueOf(temp.get("agencyId")))) {
                        list.add(temp);
                        break;
                    }
                }
            } else {
                list = list0;
            }

            if (list.size() > 0) {
                loadPay(commonManager, startDate, endDate, list, current, maxLevel);
            }
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", list).builder("startDate", startDate).builder("endDate", endDate)
                , request, response, null, false);
    }

    private static final void loadPay(CommonManager commonManager, String startDate, String endDate, List<Map<String, Object>> list, int current, final int max) throws Exception {
        if (current < max && list != null && list.size() > 0) {
            current++;
            Iterator<Map<String, Object>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> temp = iterator.next();
                int minePay = AccountUtil.countAgencyPay(commonManager, CommonUtil.object2Int(temp.get("agencyId")), startDate, endDate);
                int agencyPay = AccountUtil.countSubAgencyPay(commonManager, CommonUtil.object2Int(temp.get("userId")), startDate, endDate, current, CommonUtil.object2Int(temp.get("agencyLevel")).intValue() == 99 ? 200 : 2,true);

                if (minePay <= 0 && agencyPay <= 0) {
                    iterator.remove();
                } else {
                    temp.put("minePay", minePay);
                    temp.put("agencyPay", agencyPay);
                    temp.put("totalPay", minePay + agencyPay);

                    if (current < max && agencyPay > 0) {
                        List<Map<String, Object>> list0 = commonManager.find(SqlHelperUtil.getString("myagencies_by_userId", Constant.ROOMCARD_FILE),
                                new Object[]{CommonUtil.object2Int(temp.get("userId"))});
                        if (list0 != null && list0.size() > 0) {
                            temp.put("subList", list0);
                            loadPay(commonManager, startDate, endDate, list0, current, max);
                        }
                    }
                }
            }

            Collections.sort(list, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    int total2 = CommonUtil.object2Int(o2.get("totalPay"));
                    int total1 = CommonUtil.object2Int(o1.get("totalPay"));
                    int ret = total2 - total1;
                    if (ret == 0) {
                        ret = CommonUtil.object2Int(o2.get("minePay")) - CommonUtil.object2Int(o1.get("minePay"));
                    }
                    return ret;
                }
            });
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/income"})
    public void loadAgencyIncome(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Integer roleId = loadSystemUser(request).getRoleId();
        if (roleId == null || roleId.intValue() < 9) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        if (StringUtils.isAnyBlank(startDate, endDate)) {
            OutputUtil.output(1001, "date is null", request, response, false);
            return;
        } else {
            boolean isMonth=false;

            if (startDate.equals(endDate)&&(startDate.length()==6||startDate.length()==7)){
                isMonth=true;
            }

            Date date1 = GeneralHelper.str2Date(startDate);
            Date date2 = GeneralHelper.str2Date(endDate);

            if (date1 == null || date2 == null) {
                OutputUtil.output(1002, "date format error", request, response, false);
                return;
            } else {
                if (isMonth){
                    String[] strs=StringUtil.loadMonthRange(date1);
                    startDate=strs[0];
                    endDate=strs[1];
                }else{
                    startDate = CommonUtil.dateTimeToString(date1, "yyyy-MM-dd");
                    endDate = CommonUtil.dateTimeToString(date2, "yyyy-MM-dd");
                }

                CommonDataStatistics.incomeStatistics(startDate, endDate, commonManager,isMonth,isMonth?1:0);
            }
        }

        OutputUtil.output(1000, "OK", request, response, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/pay/detail2"})
    public void loadAgencyPayDetail2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String userId = params.get("userId");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 50);
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 10) {
            pageSize = 10;
        } else if (pageSize > 100) {
            pageSize = 100;
        }

        RoomCard roomCard = loadRoomCard(request);
        List<Map<String, Object>> result = null;

        if (roomCard != null) {
            StringBuilder stringBuilder = new StringBuilder(SqlHelperUtil.getString("agency_order", Constant.ORDER_INFO_FILE));
            List<Object> paramsList = new ArrayList<>();
            if (StringUtils.isNotBlank(userId)) {
                stringBuilder.append(" and server_id=?");
                paramsList.add(userId);
            }
            if (StringUtils.isNotBlank(startDate)) {
                stringBuilder.append(" and create_time>=?");
                paramsList.add(startDate + " 00:00:00");
            }
            if (StringUtils.isNotBlank(endDate)) {
                stringBuilder.append(" and create_time<=?");
                paramsList.add(endDate + " 23:59:59");
            }
            stringBuilder.append(" and server_id in (select agencyId from roomcard where parentId=?)");
            paramsList.add(roomCard.getUserId());
            stringBuilder.append(" GROUP BY server_id,mydate order by mydate desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);
            result = commonManager.find(stringBuilder.toString(), paramsList.toArray());

            if (result != null && result.size() > 0) {
                String subSql = SqlHelperUtil.getString("agency_order1", Constant.ORDER_INFO_FILE);

                for (Map<String, Object> cur : result) {
                    String curDate = String.valueOf(cur.get("mydate"));
                    String curAgency = String.valueOf(cur.get("server_id"));

                    List<Map<String, Object>> subResult = commonManager.find(subSql, new Object[]{
                            curDate + " 00:00:00"
                            , curDate + " 23:59:59"
                            , curAgency
                    });
                    cur.put("subList", subResult == null ? new ArrayList<>() : subResult);
                }
            }
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/relation/{varAgencyId:\\d{6}}"})
    public void loadAgencyRelation(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAgencyId") Integer varAgencyId) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String without = params.get("without");

        RoomCard roomCard = new RoomCard();
        roomCard.setAgencyId(varAgencyId);
        roomCard = commonManager.findOne(roomCard);

        if (roomCard == null) {
            OutputUtil.output(1001, "Not exists", request, response, false);
            return;
        }

        OutputUtil.output(1000, loadRelationByUserId(commonManager, roomCard.getUserId(), without), request, response, false);

    }

    private static final List<Integer> loadRelationByAgencyId(CommonManager commonManager, Integer agencyId, String withoutAgencyId) throws Exception {
        CacheEntity<List<Map<String, Object>>> cache = CacheEntityUtil.getCache("agency_all");
        List<Map<String, Object>> list;
        if (cache != null && (list = cache.getValue()) != null) {
        } else {
            list = commonManager.find(SqlHelperUtil.getString("agency_all", Constant.ROOMCARD_FILE)
                    , new Object[]{});
            CacheEntityUtil.setCache("agency_all", new CacheEntity<>(list, 5 * 60));
        }
        Integer userId = null;
        for (Map<String, Object> map : list) {
            if (agencyId.intValue() == CommonUtil.object2Int(map.get("agencyId")).intValue()) {
                userId = CommonUtil.object2Int(map.get("userId"));
                break;
            }
        }
        if (userId == null) {
            return new ArrayList<>();
        } else {
            return loadRelationByUserId(commonManager, userId, withoutAgencyId);
        }
    }

    private static final List<Integer> loadRelationByUserId(CommonManager commonManager, Integer userId, String withoutAgencyId) throws Exception {
        CacheEntity<List<Map<String, Object>>> cache = CacheEntityUtil.getCache("agency_all");
        List<Map<String, Object>> list;
        if (cache != null && (list = cache.getValue()) != null) {
        } else {
            list = commonManager.find(SqlHelperUtil.getString("agency_all", Constant.ROOMCARD_FILE)
                    , new Object[]{});
            CacheEntityUtil.setCache("agency_all", new CacheEntity<>(list, 5 * 60));
        }

        List<String> result = new ArrayList<>();

        loadRelation(list, userId, result, withoutAgencyId);

        List<Integer> ret = new ArrayList<>(result.size());

        for (Map<String, Object> map : list) {
            for (String str : result) {
                if (str.equals(String.valueOf(map.get("userId")))) {
                    ret.add(CommonUtil.object2Int(map.get("agencyId")));
                }
            }
        }

        return ret;
    }

    private static final void loadRelation(List<Map<String, Object>> list, Integer userId, List<String> result, String without) {
        for (Map<String, Object> map : list) {
            if (userId.intValue() == CommonUtil.object2Int(map.get("parentId")).intValue()) {
                if (StringUtils.isEmpty(without) || (!String.valueOf(map.get("agencyId")).equals(without))) {
                    result.add(String.valueOf(map.get("userId")));
                    loadRelation(list, CommonUtil.object2Int(map.get("userId")), result, without);
                }
            }
        }
    }

    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/purchase"})
    public void queryUserPurchase(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String agencyId = params.get("agencyId");
        if(StringUtils.isNoneBlank(agencyId)){
        	  List<Map<String, Object>> mapList = commonManager.find(SqlHelperUtil.getString("select_user_purchase", Constant.USER_INFO_FILE),new Object[]{agencyId});
        	  if(mapList != null && mapList.size() > 0){
        		  Map<String, Object> map = mapList.get(0);
        		  OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
	                      .builder("name", map.get("rName")!=null?map.get("rName"):map.get("uName")).builder("phone", map.get("rPhone"))
	                      .builder("isHavePurchase", (map.get("isHave")==null || CommonUtil.object2Int(map.get("isHave")).intValue()==0)?"否":"是")
	              , request, response, null, false);
        	  }else{
	              OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
	                      .builder("name", "").builder("phone", "")
	                      .builder("isHavePurchase", "")
	              , request, response, null, false);
        	  }
        }else{
        	 OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                     .builder("name", "").builder("phone", "")
                     .builder("isHavePurchase", "")
             , request, response, null, false);
        }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/savepurchase"})
    public void saveUserPurchase(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String agencyId = params.get("agencyId");
        if(StringUtils.isNoneBlank(agencyId)){
        	 List<Map<String, Object>> mapList = commonManager.find(SqlHelperUtil.getString("select__agency", Constant.USER_INFO_FILE),new Object[]{agencyId});
       	    if(mapList != null && mapList.size() > 0){
       	    	String userId = mapList.get(0).get("userId").toString();
       	    	commonManager.saveOrUpdate(SqlHelperUtil.getString("update_user_purchase", Constant.USER_INFO_FILE),new Object[]{userId});
       	     OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
       	             , request, response, null, false);
       	    }
        }else{
        	 OutputUtil.output(MessageBuilder.newInstance().builder("code", 0)
             , request, response, null, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/info/list"})
    public void loadAgencyList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        List<Map<String, Object>> list = commonManager.find(SqlHelperUtil.getString("agency_list", Constant.ROOMCARD_FILE)
                , new Object[]{roomCard.getUserId()});

        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                        .builder("datas", list == null ? new ArrayList<>() : list)
                , request, response, null, false);

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/players/connect"})
    public void queryMyPlayersconnect(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        int pageNo = 1;
        int pageSize = 30;
        RoomCard roomCard = loadRoomCard(request);
        List<UserInfo> result = null;
        if (roomCard != null) {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add(roomCard.getAgencyId());
            paramsList.add(roomCard.getAgencyId());
            StringBuilder stringBuilder = new StringBuilder(SqlHelperUtil.getString("select_connect_user", Constant.USER_INFO_FILE));
            stringBuilder.append(" order by payBindTime desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);

            result = commonManager.findList(stringBuilder.toString(), paramsList.toArray(), UserInfo.class);
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agencies/connect"})
    public void queryMyAgenciesconnect(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        RoomCard roomCard = loadRoomCard(request);
        Integer userId = NumberUtils.toInt(params.get("userId"), 0);

        if (userId <= 0) {
            userId = loadRoomCard(request).getUserId();
        }

        List<RoomCard> result = null;

        if (userId > 0) {
            result = commonManager.findList(SqlHelperUtil.getString("myagencies_connect", Constant.ROOMCARD_FILE)
                    , new Object[]{userId,roomCard.getAgencyId()}, RoomCard.class);
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/update/month"})
    public void updateMonthIncome(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	String monthStr = params.get("month");
        for(int i=1 ;i<=12;i++){
        	if(i<10){
        		monthStr = "2017-0"+i;
        	}else{
        		monthStr = "2017-"+i;
        	}
        	 Calendar cal = Calendar.getInstance();
        	 cal.setTime(CommonUtil.stringToDateTime(monthStr, "yyyy-MM"));
        	 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-01~yyyy-MM-dd");
        	 cal.add(Calendar.MONTH, 1);
             cal.set(Calendar.DAY_OF_MONTH,0);//设置为1号,当前日期既为本月第一天
             String lastDay = format.format(cal.getTime());
             String[] strs = lastDay.split("\\~");
             commonManager.saveOrUpdate(SqlHelperUtil.getString("update",Constant.AGENCY_INCOME_FILE).toString(), new String[]{strs[0].replace("-",""),strs[1].replace("-","")});
//             commonManager.saveOrUpdate(dataBase2,SqlHelperUtil.getString("update",Constant.AGENCY_INCOME_FILE).toString(), new String[]{strs[0].replace("-",""),strs[1].replace("-","")});
        }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/test"})
    public void test(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	String isMonth = params.get("isMonth");
    	Integer type = Integer.parseInt(params.get("type"));
    	String start = params.get("start");
    	String end = params.get("end");
    	CommonDataStatistics.incomeStatistics(start, end, commonManager, (StringUtils.isNoneBlank(isMonth)&&isMonth.equals("1"))?true:false, type);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"get/info/agency"})
    public void loadAgencyInfoById(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String agencyId = params.get("agencyId").trim();
        RoomCard roomCard = new RoomCard();
        roomCard.setAgencyId(Integer.parseInt(agencyId));
        roomCard = commonManager.findOne(roomCard);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("data", roomCard)
                , request, response, null, false);

    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/info/reset"})
    public void userInfoReset(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
        String str = sim.format(new Date());
        RoomCard roomCard = loadRoomCard(request);
        String telCode = params.get("telCode").trim();
        String tel = params.get("tel").trim();
        str+=roomCard.getAgencyId();
        if(StringUtils.isNotBlank(Constant.restTime.get(roomCard.getAgencyId())) && Constant.restTime.get(roomCard.getAgencyId()).equals(str)){
        	OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "每天只能重置1次，请明天再试！")
                    , request, response, null, false);
        	return;
        }
        Long telCodeExpire = getSessionValue(request, "telCode_expire");
        if (telCodeExpire == null || System.currentTimeMillis() - telCodeExpire > 0) {
            OutputUtil.output(1006, LanguageUtil.getString("code_timeout"), request, response, false);
            return;
        }
        if (!(tel + "," + telCode).equalsIgnoreCase(String.valueOf(getSessionValue(request, "telCode")))) {
            OutputUtil.output(1006, LanguageUtil.getString("tel_code_error"), request, response, false);
            return;
        }
        commonManager.saveOrUpdate(SqlHelperUtil.getString("reset_user_info", Constant.ROOMCARD_FILE).toString(),new Object[]{"","",roomCard.getAgencyId()} );
        roomCard.setUserName("");
        roomCard.setOpenid("");
        setSessionValue(request, "roomCard",roomCard);
        Constant.restTime.put(roomCard.getAgencyId(),str);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "重置成功")
                , request, response, null, false);

    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/info/agency/reset"})
    public void userAgencyInfoReset(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String agencyId = params.get("agencyId").trim();
        RoomCard roomCard = new RoomCard();
        roomCard.setAgencyId(Integer.parseInt(agencyId));
        roomCard = commonManager.findOne(roomCard);
        if(roomCard == null){
        	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "该代理不存在")
                     , request, response, null, false);
        	 return;
        }
        commonManager.saveOrUpdate( SqlHelperUtil.getString("reset_user_info", Constant.ROOMCARD_FILE).toString(),new Object[]{"","",roomCard.getAgencyId()} );
        
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "重置成功")
                , request, response, null, false);

    }
    public void roomCardListByParent(RoomCard roomcard,List<RoomCard> list) throws Exception{
    	list.add(roomcard);
   	   RoomCard r = new RoomCard();
   	   r.setParentId(roomcard.getUserId());
   	   List<RoomCard> childList = commonManager.findList(r);
   	   if(childList != null && childList.size() > 0){
   		   for(RoomCard rm : childList){
   			   list.add(rm);
   			   RoomCard r0 = new RoomCard();
   		 	   r0.setParentId(rm.getUserId());
   		 	   List<RoomCard> childList0 = commonManager.findList(r0);
   		 	   for(RoomCard rm0 : childList0){
   	 			   list.add(rm0);
   	 		   }
   		   }
   	   }
   	   
 	} 
    public void roomCardListByParent2(RoomCard roomcard,List<RoomCard> list) throws Exception{
  	   list.add(roomcard);
  	   RoomCard r = new RoomCard();
  	   r.setParentId(roomcard.getUserId());
  	   List<RoomCard> childList = commonManager.findList(r);
  	   if(childList != null && childList.size() > 0){
  		   for(RoomCard rm : childList){
  			   roomCardListByParent2(rm, list);
  		   }
  	   }
  	   
  	} 
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/info"})
    public void loadPlayInfoDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String agencyId1 = params.get("agencyId1");
        String agencyId2 = params.get("agencyId2");
        String payType = params.get("payType");
        String type = params.get("type");
        //agencyId1:agencyId1,agencyId2
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 20);
        if (pageNo < 1) {
            pageNo = 1;
        }
        int totalSize = 0;
        int totalCount = 0;
        int count = 0;
        RoomCard roomCard = null;
        if(!(type.equals("2") || type.equals("3"))&& StringUtils.isNoneBlank(agencyId2)){
        	roomCard = new RoomCard();
        	roomCard.setAgencyId(Integer.parseInt(agencyId2));
        	roomCard=commonManager.findOne(roomCard);
        }else if(!(type.equals("2") || type.equals("3")) && StringUtils.isNoneBlank(agencyId1)){
        	roomCard = new RoomCard();
        	roomCard.setAgencyId(Integer.parseInt(agencyId1));
        	roomCard=commonManager.findOne(roomCard);
        }else{
        	roomCard = loadRoomCard(request);
        }
        List<OrderInfo> result = null;
        if (roomCard != null) {
        	List<RoomCard> list = new ArrayList<>();
        	if (roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel().intValue() == 99) {
        		roomCardListByParent2(roomCard, list);
            }else{
            	roomCardListByParent(roomCard, list);
            }
        	StringBuilder stringBuilder = new StringBuilder(SqlHelperUtil.getString("player_order", Constant.ORDER_INFO_FILE));
            List<Object> paramsList = new ArrayList<>();
            if(type.equals("1")){
            	stringBuilder.append(" and server_id =?");
                paramsList.add(agencyId1);
            }else if(type.equals("2")){
                if (list.size()<=1){
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                    .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                                    .builder("datas", result == null ? new ArrayList<>() : result)
                            , request, response, null, false);
                    return;
                }
            	stringBuilder.append(" and server_id in(?");
            	list.remove(0);
            	paramsList.add(list.get(0).getAgencyId().toString()+"Z");
        		for(int i = 1; i < list.size(); i++){
        			stringBuilder.append(",?");
                    paramsList.add(list.get(i).getAgencyId().toString()+"Z");
        		}
        		stringBuilder.append(")");
            }else if(type.equals("3")){
                if (list.size()<=1){
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                    .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                                    .builder("datas", result == null ? new ArrayList<>() : result)
                            , request, response, null, false);
                    return;
                }
            	stringBuilder.append(" and server_id in(?");
            	list.remove(0);
            	paramsList.add(list.get(0).getAgencyId().toString());
        		for(int i = 1; i < list.size(); i++){
        			stringBuilder.append(",?");
                    paramsList.add(list.get(i).getAgencyId().toString());
        		}
        		stringBuilder.append(")");
            }else if(StringUtils.isNoneBlank(payType) && payType.equals("1")){
                if (list.size()<=0){
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                    .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                                    .builder("datas", result == null ? new ArrayList<>() : result)
                            , request, response, null, false);
                    return;
                }
            	stringBuilder.append(" and server_id in(?");
            	paramsList.add(list.get(0).getAgencyId().toString());
        		for(int i = 1; i < list.size(); i++){
        			stringBuilder.append(",?");
                    paramsList.add(list.get(i).getAgencyId().toString());
        		}
        		stringBuilder.append(")");
        	}else if(StringUtils.isNoneBlank(payType) && payType.equals("2")){
                if (list.size()<=0){
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                    .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                                    .builder("datas", result == null ? new ArrayList<>() : result)
                            , request, response, null, false);
                    return;
                }
        		stringBuilder.append(" and server_id in(?");
            	paramsList.add(list.get(0).getAgencyId().toString()+"Z");
        		for(int i = 1; i < list.size(); i++){
        			stringBuilder.append(",?");
                    paramsList.add(list.get(i).getAgencyId().toString()+"Z");
        		}
        		stringBuilder.append(")");
        	}else{
                if (list.size()<=0){
                    OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                                    .builder("totalSize", totalSize).builder("totalCount", totalCount)
                                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                                    .builder("datas", result == null ? new ArrayList<>() : result)
                            , request, response, null, false);
                    return;
                }
        		stringBuilder.append(" and server_id in(?,?");
            	paramsList.add(list.get(0).getAgencyId().toString());
            	paramsList.add(list.get(0).getAgencyId().toString()+"Z");
        		for(int i = 1; i < list.size(); i++){
        			stringBuilder.append(",?,?");
                    paramsList.add(list.get(i).getAgencyId().toString());
                    paramsList.add(list.get(i).getAgencyId().toString()+"Z");
        		}
        		stringBuilder.append(")");
        	}
            if (StringUtils.isNotBlank(startDate)) {
                stringBuilder.append(" and create_time>=?");
                paramsList.add(startDate + " 00:00:00");
            }
            if (StringUtils.isNotBlank(endDate)) {
                stringBuilder.append(" and create_time<=?");
                paramsList.add(endDate + " 23:59:59");
            }

            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            List<OrderInfo> allList = commonManager.findList(dataBase,stringBuilder.toString(), paramsList.toArray(), OrderInfo.class);
            if(allList != null & allList.size() > 0){
            	totalSize = allList.size();
            	for(OrderInfo o : allList){
            		if(o.getServerId().endsWith("Z")){
            		   totalCount +=  CommonUtil.object2Int(o.getOrderAmount())/10;
            		}else{
            		   totalCount +=  CommonUtil.object2Int(o.getOrderAmount());
            		}
            	}
            	count = allList.size();
            	stringBuilder.append(" order by create_time desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);
                result = commonManager.findList(dataBase,stringBuilder.toString(), paramsList.toArray(), OrderInfo.class);
                if(result != null && result.size() > 0){
                	for(OrderInfo r : result){
                		String  str = r.getServerId().substring(0, 6);
                		RoomCard rm = new RoomCard();
                		rm.setAgencyId(Integer.parseInt(str));
                		rm = commonManager.findOne(rm);
                		r.setName(rm.getUserName());
                	}
                }
            }
        }

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("totalSize", totalSize).builder("totalCount", totalCount)
                        .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/buy/card/info"})
    public void buycardDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String agencyId = params.get("agencyId");
        String playerId = params.get("playerId");
        String type = params.get("type");
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 20);
        if (pageNo < 1) {
            pageNo = 1;
        }
        int count = 0;
        List<BuyCardInfo> result = new ArrayList<>();
        RoomCard  roomCard = loadRoomCard(request);
        SystemUser users = loadSystemUser(request);
        if(roomCard == null){
        	OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                    .builder("datas", result == null ? new ArrayList<>() : result)
            , request, response, null, false);
        	return;
        }
        List<Object> paramsList = new ArrayList<>();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(type.equals("0")){
        	//查询给玩家的售钻
        	String  sql = "select * from roomcard_order where rechargeAgencyId>0 and  rechargeAgencyId="+roomCard.getAgencyId();
        	/*if((roomCard.getAgencyLevel()!=null&&roomCard.getAgencyLevel()==99) || (users.getRoleId() !=null && users.getRoleId() > 0)){
        		sql = "select * from roomcard_order where  rechargeAgencyId>0 and  1=1 ";
        	}*/
            SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

            if(StringUtils.isNotBlank(playerId)){
                sql += " and roleId="+playerId;
            }
        	if(StringUtils.isNotBlank(startDate)){
        		sql += " and createTime >=?";
        		paramsList.add(startDate+" 00:00:00");
        	}
        	if(StringUtils.isNotBlank(endDate)){
        		sql += " and createTime <=?";
        		paramsList.add(endDate+" 23:59:59");
        	}
        	List<RoomCardOrder> list = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
        	count = list.size();
        	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
        	List<RoomCardOrder> orderList = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
        	if(orderList != null && orderList.size() > 0){
        		for(RoomCardOrder r : orderList){
        			BuyCardInfo info = new BuyCardInfo();
        			info.setAgencyId(r.getRechargeAgencyId().toString());
        			info.setId(r.getRoleId().toString());
        			info.setPtype("玩家");
        			info.setTime(sim.format(r.getCreateTime()));
        			info.setCardNums(r.getCommonCards());
        			UserInfo user = new UserInfo();
        			user.setUserId(r.getRoleId());
        			user = commonManager.findOne(dataBase,user);
        			if(user != null){
        				info.setName(StringUtils.isBlank(user.getName())?"":user.getName());
        			}
        			result.add(info);
        		}
        	}
        }else{
        	//查代理
        	String  sql = "select * from roomcardrecord where activeUserid="+roomCard.getAgencyId();
        	/*if((roomCard.getAgencyLevel()!=null&&roomCard.getAgencyLevel()==99) || (users.getRoleId() !=null && users.getRoleId() > 0)){
        		sql = "select * from roomcardrecord where 1=1 ";
        	}*/
        	if(StringUtils.isNotBlank(agencyId)){
        		sql += " and reactiveUserId="+agencyId;
        	}
        	if(StringUtils.isNotBlank(startDate)){
        		sql += " and createTime >=?";
        		paramsList.add(startDate+" 00:00:00");
        	}
        	if(StringUtils.isNotBlank(endDate)){
        		sql += " and createTime <=?";
        		paramsList.add(endDate+" 23:59:59");
        	}
        	List<RoomCardRecord> list = commonManager.findList(sql,paramsList.toArray() , RoomCardRecord.class);
        	count = list.size();
        	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
        	List<RoomCardRecord> orderList = commonManager.findList(sql,paramsList.toArray(), RoomCardRecord.class);
        	if(orderList != null && orderList.size() > 0){
        		for(RoomCardRecord r : orderList){
        			BuyCardInfo info = new BuyCardInfo();
        			info.setId(r.getReactiveUserId().toString());
        			info.setAgencyId(r.getActiveUserid().toString());
        			info.setPtype("代理");
        			info.setTime(sim.format(r.getCreateTime()));
        			info.setCardNums(r.getRoomCardNumber());
        			RoomCard user = new RoomCard();
        			user.setAgencyId(r.getReactiveUserId());
        			user = commonManager.findOne(user);
        			if(user != null){
        				info.setName(StringUtils.isBlank(user.getUserName())?"":user.getUserName());
        			}
        			result.add(info);
        		}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                .builder("datas",result)
        , request, response, null, false);
       }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/get/player/card/record"})
    public void playerCardRecord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String agencyId = params.get("agencyId").trim();

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        String hql = " select * from roomcard_order where rechargeAgencyId=?  order by createTime desc limit 0,10";
        List<RoomCardOrder> orderList = commonManager.findList(dataBase,hql, new Object[]{agencyId}, RoomCardOrder.class);
        if(orderList != null && orderList.size() > 0){
        	for(RoomCardOrder r : orderList){
        		UserInfo info = new UserInfo();
        		info.setUserId(r.getRoleId());
            		info = commonManager.findOne(dataBase,info);
            		if(info != null){
            			r.setPlayerName(info.getName());
            		}else{
            			r.setPlayerName("");
            		}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("data", orderList)
                , request, response, null, false);

    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/get/agency/card/record"})
    public void agencyCardRecord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String agencyId = params.get("agencyId").trim();
        String hql = " select * from roomcardrecord where activeUserid=?  order by createTime desc limit 0,10";
        List<RoomCardRecord> orderList = commonManager.findList(hql, new Object[]{agencyId}, RoomCardRecord.class);
        if(orderList != null && orderList.size() > 0){
        	for(RoomCardRecord r : orderList){
        		RoomCard info = new RoomCard();
        		info.setAgencyId(r.getReactiveUserId());
            	info = commonManager.findOne(info);
            	if(info != null){
            			r.setAgencyName(info.getUserName());
            	}else{
            		r.setAgencyName("");
            	}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("data", orderList)
                , request, response, null, false);

    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/forbid"})
    public void forbidAgency(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoomCard rc = loadRoomCard(request);
        SystemUser user=loadSystemUser(request);
        Map<String, String> params = UrlParamUtil.getParameters(request);
        if (rc==null||user==null) {
            OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }else if (!((rc.getAgencyLevel()!=null&&rc.getAgencyLevel().intValue()==99)||(user.getRoleId()!=null&&user.getRoleId().intValue()>=1))){
        	OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "没有权限进行该操作")
                    , request, response, null, false);
        	return;
        }
        logger.info("params:{}", params);

        Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
        if (agencyId <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , request, response, null, false);
            return;
        }

        RoomCard roomCard0 = new RoomCard();
        roomCard0.setAgencyId(agencyId);
        roomCard0 = commonManager.findOne(roomCard0);

        if (roomCard0 == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                    , request, response, null, false);
            return;
        } 
        String hql = " update system_user set isForbidden=? where user_id=?  ";
        commonManager.saveOrUpdate(hql, new Object[]{params.get("type"),roomCard0.getUserId()});
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "执行成功")
                    , request, response, null, false);

    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/query"})
    public void loadAgencyQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 RoomCard rc = loadRoomCard(request);
         SystemUser user=loadSystemUser(request);
         if (rc==null||user==null) {
             OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
             return;
         }else if ((rc.getAgencyLevel()!=null&&rc.getAgencyLevel().intValue()==99)||(user.getRoleId()!=null&&user.getRoleId().intValue()>=1)){
         }else{
             OutputUtil.output(1004, LanguageUtil.getString("no_auth"), request, response, false);
             return;
         }
         Map<String, String> params = UrlParamUtil.getParameters(request);
         logger.info("params:{}", params);

         Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
         SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_1");
         if (agencyId <= 0) {
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                     , request, response, null, false);
             return;
         }

         RoomCard roomCard0 = new RoomCard();
         roomCard0.setAgencyId(agencyId);
         roomCard0 = commonManager.findOne(dataBase,roomCard0);

         if (roomCard0 == null) {
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("agency_not_exists"))
                     , request, response, null, false);
         } else{
         	SystemUser user1 = new SystemUser();
         	user1.setUserId(roomCard0.getUserId());
         	user1=commonManager.findOne(user1);
         	int type = 0;
         	if(user1.getIsForbidden() != null){
         		type = user1.getIsForbidden();
         	}
         	String  parentAgencyId = "无";
         	if(roomCard0.getParentId() != null){
         		RoomCard roomCard1 = new RoomCard();
      	        roomCard1.setUserId(roomCard0.getParentId());
      	        roomCard1 = commonManager.findOne(roomCard1);
      	        if(roomCard1 != null){
      	        	parentAgencyId = roomCard1.getAgencyId().toString();
      	        }
         	}
             OutputUtil.output(MessageBuilder.newInstance().builder("type", type).builderCodeMessage(1000, roomCard0).builder("parentAgencyId",parentAgencyId)
                     , request, response, null, false);
         }

    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/buy/card/info"})
    public void mbuycardDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String agencyId = params.get("agencyId");
        String playerId = params.get("playerId");
        String type = params.get("type");
        String type2 = params.get("type2");
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        int pageSize = NumberUtils.toInt(params.get("pageSize"), 20);
        if (pageNo < 1) {
            pageNo = 1;
        }
        int count = 0;
        List<BuyCardInfo> result = new ArrayList<>();
        RoomCard  roomCard = loadRoomCard(request);
        SystemUser users = loadSystemUser(request);
        if(roomCard == null){
        	OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                    .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                    .builder("datas", result == null ? new ArrayList<>() : result)
            , request, response, null, false);
        	return;
        }
        List<Object> paramsList = new ArrayList<>();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        if(type.equals("0")){
        	//查询给玩家的售钻
        	String  sql =  "select * from roomcard_order where  rechargeAgencyId>0 ";

            if(StringUtils.isNotBlank(playerId)){
                sql += " and roleId="+playerId;
            }
            if(StringUtils.isNotBlank(startDate)){
                sql += " and createTime >=?";
                paramsList.add(startDate+" 00:00:00");
            }
            if(StringUtils.isNotBlank(endDate)){
                sql += " and createTime <=?";
                paramsList.add(endDate+" 23:59:59");
            }
        	List<RoomCardOrder> list = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
        	count = list.size();
        	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
        	List<RoomCardOrder> orderList = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
        	if(orderList != null && orderList.size() > 0){
        		for(RoomCardOrder r : orderList){
        			BuyCardInfo info = new BuyCardInfo();
        			info.setAgencyId(r.getRechargeAgencyId().toString());
        			info.setId(r.getRoleId().toString());
        			info.setPtype("玩家");
        			info.setTime(sim.format(r.getCreateTime()));
        			info.setCardNums(r.getCommonCards());
        			UserInfo user = new UserInfo();
        			user.setUserId(r.getRoleId());
        			user = commonManager.findOne(dataBase,user);
        			if(user != null){
        				info.setName(StringUtils.isBlank(user.getName())?"":user.getName());
        			}
        			result.add(info);
        		}
        	}
        }else{
        	//查代理
        	if(type2.equals("0")){
        		String  sql =  "select * from roomcard_order where  rechargeAgencyId>0 and  1=1 ";
            	if(StringUtils.isNotBlank(agencyId)){
            		sql += " and rechargeAgencyId="+agencyId;
            	}
            	if(StringUtils.isNotBlank(startDate)){
            		sql += " and createTime >=?";
            		paramsList.add(startDate+" 00:00:00");
            	}
            	if(StringUtils.isNotBlank(endDate)){
            		sql += " and createTime <=?";
            		paramsList.add(endDate+" 23:59:59");
            	}
            	List<RoomCardOrder> list = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
            	count = list.size();
            	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
            	List<RoomCardOrder> orderList = commonManager.findList(dataBase,sql,paramsList.toArray(), RoomCardOrder.class);
            	if(orderList != null && orderList.size() > 0){
            		for(RoomCardOrder r : orderList){
            			BuyCardInfo info = new BuyCardInfo();
            			info.setAgencyId(r.getRechargeAgencyId().toString());
            			info.setId(r.getRoleId().toString());
            			info.setPtype("玩家");
            			info.setTime(sim.format(r.getCreateTime()));
            			info.setCardNums(r.getCommonCards());
            			UserInfo user = new UserInfo();
            			user.setUserId(r.getRoleId());
            			user = commonManager.findOne(dataBase,user);
            			if(user != null){
            				info.setName(StringUtils.isBlank(user.getName())?"":user.getName());
            			}
            			result.add(info);
            		}
            	}
        	}else if(type2.equals("1")){
        		String sql = "select * from roomcardrecord where 1=1 ";
            	if(StringUtils.isNoneBlank(agencyId)){
            		sql += " and reactiveUserId="+agencyId;
            	}
            	if(StringUtils.isNoneBlank(startDate)){
            		sql += " and createTime >=?";
            		paramsList.add(startDate+" 00:00:00");
            	}
            	if(StringUtils.isNoneBlank(endDate)){
            		sql += " and createTime <=?";
            		paramsList.add(endDate+" 23:59:59");
            	}
            	List<RoomCardRecord> list = commonManager.findList(sql,paramsList.toArray() , RoomCardRecord.class);
            	count = list.size();
            	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
            	List<RoomCardRecord> orderList = commonManager.findList(sql,paramsList.toArray(), RoomCardRecord.class);
            	if(orderList != null && orderList.size() > 0){
            		for(RoomCardRecord r : orderList){
            			BuyCardInfo info = new BuyCardInfo();
            			info.setId(r.getReactiveUserId().toString());
            			info.setAgencyId(r.getActiveUserid().toString());
            			info.setPtype("代理");
            			info.setTime(sim.format(r.getCreateTime()));
            			info.setCardNums(r.getRoomCardNumber());
            			RoomCard user = new RoomCard();
            			user.setAgencyId(r.getReactiveUserId());
            			user = commonManager.findOne(user);
            			if(user != null){
            				info.setName(StringUtils.isBlank(user.getUserName())?"":user.getUserName());
            			}
            			result.add(info);
            		}
            	}
        	}else if(type2.equals("2")){
        		String sql = "select * from roomcardrecord where 1=1 ";
            	if(StringUtils.isNoneBlank(agencyId)){
            		sql += " and activeUserid="+agencyId;
            	}
            	if(StringUtils.isNoneBlank(startDate)){
            		sql += " and createTime >=?";
            		paramsList.add(startDate+" 00:00:00");
            	}
            	if(StringUtils.isNoneBlank(endDate)){
            		sql += " and createTime <=?";
            		paramsList.add(endDate+" 23:59:59");
            	}
            	List<RoomCardRecord> list = commonManager.findList(sql,paramsList.toArray() , RoomCardRecord.class);
            	count = list.size();
            	sql += " order by createTime desc limit "+(pageNo - 1) * pageSize+","+(pageSize);
            	List<RoomCardRecord> orderList = commonManager.findList(sql,paramsList.toArray(), RoomCardRecord.class);
            	if(orderList != null && orderList.size() > 0){
            		for(RoomCardRecord r : orderList){
            			BuyCardInfo info = new BuyCardInfo();
            			info.setId(r.getReactiveUserId().toString());
            			info.setAgencyId(r.getActiveUserid().toString());
            			info.setPtype("代理");
            			info.setTime(sim.format(r.getCreateTime()));
            			info.setCardNums(r.getRoomCardNumber());
            			RoomCard user = new RoomCard();
            			user.setAgencyId(r.getReactiveUserId());
            			user = commonManager.findOne(user);
            			if(user != null){
            				info.setName(StringUtils.isBlank(user.getUserName())?"":user.getUserName());
            			}
            			result.add(info);
            		}
            	}
        	}
        	
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                .builder("page", count%pageSize==0?count/pageSize:count/pageSize+1)
                .builder("datas",result)
        , request, response, null, false);
       }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/d/agency/pay/detail"})
    public void loaddAgencyPayDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String userId = params.get("userId");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("userId", userId);
        String hql ="SELECT * FROM order_info where is_sent>0 and extend=? and create_time>=? and create_time<=? order by create_time desc ";
        if (!startDate.contains(" ")) {
            startDate += " 00:00:00";
        }
        if (!endDate.contains(" ")) {
            endDate += " 23:59:59";
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


        List<OrderInfo> lists = commonManager.findList(dataBase,hql, new Object[]{userId,startDate,endDate}, OrderInfo.class);
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", lists)
                , request, response, null, false);
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/vip/wx"})
    public void vipwx(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        for(long i=1;i<=17;i++){
        	AgencyShow show = new AgencyShow();
        		show.setKeyId(i);
        		show = commonManager.findOne(show);
        		if(show == null){
        			show = new AgencyShow();
        			show.setKeyId(i);
        			show.setWeixinName(params.get("wx"+i));
        			commonManager.save(show);
        		}else{
        			show.setWeixinName(params.get("wx"+i));
        			commonManager.update(show, new String[]{"keyId"}, new Object[]{i});
        		}
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "修改成功")
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/query/vip/wx"})
    public void queryvipwx(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        AgencyShow show = new AgencyShow();
        List<AgencyShow> list = commonManager.findList(show);
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("data", list)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/agency/cardbackinfo"})
    public void cardbackinfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);

        int num =  NumberUtils.toInt(params.get("type"), 0);
        logger.info("params:{}", params);
        String  hql = " select * from back_card_info where 1=1  and backCardType!=? order by createTime desc limit 0,10";
        List<BackCardInfo> result = null;
        List<BackCardInfo> result1 = new ArrayList<>();
        List<Object> paramsList = new ArrayList<>();
       	paramsList.add(num);
        result = commonManager.findList(hql, paramsList.toArray(), BackCardInfo.class);
        
        for(BackCardInfo b:result){
        	UserInfo u=new UserInfo();
        	u.setUserId(Long.valueOf(b.getSendUserId()));
        	u=commonManager.findOne(u);
        	logger.info("u:{},u.name:{}", params,u.getName());
        	b.setSendName(u.getName()==null?"":u.getName());
        	
        	UserInfo u1=new UserInfo();
        	u1.setUserId(Long.valueOf(b.getReciaveUserId()));
        	u1=commonManager.findOne(u1);
        	logger.info("u1.name:{}", u1.getName());
        	b.setReciaveName(u1.getName()==null?"":u1.getName());
        	
        	result1.add(b);
        	}
//        int count = 0;
//        int total = 0;
//        if(result != null && result.size() > 0){
//        	count = result.size();
//        	for(RoomCard r : result){
//        		total += r.getCommonCard();
//        	}
//        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result1 == null ? new ArrayList<>() : result1)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/agency/cardbackinfoOne"})
    public void cardbackinfoOne(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);

        String playerId =  params.get("playerId");
        String playerId2 =  params.get("playerId2");
        logger.info("params:{}", params);
        String  hql = " select * from back_card_info where 1=1  and sendUserId=? and reciaveUserId=? order by createTime desc limit 0,10";
        List<BackCardInfo> result = null;
        List<BackCardInfo> result1 = new ArrayList<>();
        List<Object> paramsList = new ArrayList<>();
       	paramsList.add(playerId);
       	paramsList.add(playerId2);
        result = commonManager.findList(hql, paramsList.toArray(), BackCardInfo.class);
        
        for(BackCardInfo b:result){
        	UserInfo u=new UserInfo();
        	u.setUserId(Long.valueOf(b.getSendUserId()));
        	u=commonManager.findOne(u);
        	b.setSendName(u.getName()==null?"":u.getName());
        	
        	UserInfo u1=new UserInfo();
        	u1.setUserId(Long.valueOf(b.getReciaveUserId()));
        	u1=commonManager.findOne(u1);
        	b.setReciaveName(u1.getName()==null?"":u1.getName());
        	
        	result1.add(b);
        	}

        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result1 == null ? new ArrayList<>() : result1)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/agency/cardback"})
    public void playerAndagencyCardBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 Map<String, String> params = UrlParamUtil.getParameters(request);
         logger.info("params:{}", params);
         String agencyId = params.get("agencyId").trim();
         String playerId = params.get("playerId").trim();
         String playerId2 = params.get("playerId2").trim();
         int cardNum = Integer.parseInt(params.get("cardNum").trim());
         int type = Integer.parseInt(params.get("type").trim());
         String agencyId1 = params.get("agencyId1").trim();
         String agencyId2 = params.get("agencyId2").trim();
         String playerId3 = params.get("playerId3").trim();
         BackCardInfo binfo = new BackCardInfo();

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


        if(type == 0){
        	 RoomCard roomCard = new RoomCard();
        	 roomCard.setAgencyId(Integer.parseInt(agencyId));
             roomCard = commonManager.findOne(roomCard);
             if(roomCard == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该代理不存在")
                          , request, response, null, false);
             	 return;
             }

             UserInfo info = new UserInfo();
             info.setUserId(Long.parseLong(playerId3));
             info = commonManager.findOne(dataBase,info);
             if(info == null){
            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该玩家不存在")
                         , request, response, null, false);
            	 return;
             }
             if(info.getCards()+info.getFreeCards()<cardNum){
            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退的房卡数大于玩家自己的房卡数")
                         , request, response, null, false);
            	 return;
             }
             if(info.getCards() > cardNum){
            	 commonManager.saveOrUpdate("update user_inf set cards=? where userId=? " ,new Object[]{info.getCards()-cardNum,info.getUserId()} );
             }else{
            	 commonManager.saveOrUpdate("update user_inf set cards=?,freeCards=? where userId=? " ,new Object[]{0,info.getFreeCards()-cardNum+info.getCards(),info.getUserId()} );
             }
             binfo.setBackCardType(0);
             binfo.setSendUserId(playerId3);
             binfo.setReciaveUserId(agencyId);
             binfo.setCardNum(cardNum);
             binfo.setCreateTime(new Date());
             commonManager.save(dataBase,binfo);
             commonManager.saveOrUpdate("update roomcard set commonCard=? where agencyId=? " ,new Object[]{roomCard.getCommonCard()+cardNum,roomCard.getAgencyId()} );
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "退卡成功")
                     , request, response, null, false);
         }else if(type == 2){
       	  UserInfo info2 = new UserInfo();
             info2.setUserId(Long.parseLong(playerId2));
             info2 = commonManager.findOne(dataBase,info2);
             if(info2 == null){
            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "接收玩家不存在")
                         , request, response, null, false);
            	 return;
             }
              UserInfo info = new UserInfo();
              info.setUserId(Long.parseLong(playerId));
              info = commonManager.findOne(dataBase,info);
              if(info == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退卡玩家不存在")
                          , request, response, null, false);
             	 return;
              }
              if(info.getCards()+info.getFreeCards()<cardNum){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退的房卡数大于玩家自己的房卡数")
                          , request, response, null, false);
             	 return;
              }
              if(info.getCards() > cardNum){
             	 commonManager.saveOrUpdate(dataBase,"update user_inf set cards=? where userId=? " ,new Object[]{info.getCards()-cardNum,info.getUserId()} );
              }else{
             	 commonManager.saveOrUpdate(dataBase,"update user_inf set cards=?,freeCards=? where userId=? " ,new Object[]{0,info.getFreeCards()-cardNum+info.getCards(),info.getUserId()} );
              }
              binfo.setBackCardType(2);
              binfo.setSendUserId(playerId);
              binfo.setReciaveUserId(playerId2);
              binfo.setCardNum(cardNum);
              binfo.setCreateTime(new Date());
              commonManager.save(dataBase,binfo);
              commonManager.saveOrUpdate(dataBase,"update user_inf set freeCards=? where userId=? " ,new Object[]{cardNum+info2.getFreeCards(),info2.getUserId()} );
              OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "退卡成功")
                      , request, response, null, false);
          }else{
        	 RoomCard roomCard = new RoomCard();
        	 roomCard.setAgencyId(Integer.parseInt(agencyId1));
        	 if(agencyId1.equals("admin")){
        		   OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "退卡不能从系统账号上退")
                           , request, response, null, false);
        		   return;
        	 }
        	 if(agencyId1.equals(agencyId2)){
        		 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "自己不能退卡给自己")
                         , request, response, null, false);
      		   return;
        	 }
             roomCard = commonManager.findOne(roomCard);
             if(roomCard == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退卡代理不存在")
                          , request, response, null, false);
             	 return;
             }
             RoomCard roomCard2 = new RoomCard();
             roomCard2.setAgencyId(Integer.parseInt(agencyId2));
             roomCard2 = commonManager.findOne(roomCard2);
             if(roomCard2 == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "接收代理不存在")
                          , request, response, null, false);
             	 return;
             }
             if(roomCard.getCommonCard() < cardNum){
            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退的房卡数大于退卡代理自己的房卡数")
                         , request, response, null, false);
            	 return; 
             }
             binfo.setBackCardType(1);
             binfo.setSendUserId(agencyId1);
             binfo.setReciaveUserId(agencyId2);
             binfo.setCardNum(cardNum);
             binfo.setCreateTime(new Date());
             commonManager.save(dataBase,binfo);
             
             commonManager.saveOrUpdate("update roomcard set commonCard=? where agencyId=? " ,new Object[]{roomCard.getCommonCard()-cardNum,roomCard.getAgencyId()} );
             commonManager.saveOrUpdate("update roomcard set commonCard=? where agencyId=? " ,new Object[]{roomCard2.getCommonCard()+cardNum,roomCard2.getAgencyId()} );
               
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "退卡成功")
                     , request, response, null, false); 
         }
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/agency"})
    public void queryPlayerAndagency(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 Map<String, String> params = UrlParamUtil.getParameters(request);
         logger.info("params:{}", params);
         String agencyId = params.get("agencyId").trim();
         String playerId = params.get("playerId").trim();
         String playerId2 = params.get("playerId2").trim();
         String playerId3 = params.get("playerId3").trim();
         String agencyId1 = params.get("agencyId1").trim();
         String agencyId2 = params.get("agencyId2").trim();
         int type = Integer.parseInt(params.get("type").trim());

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        if(type == 0){
        	 RoomCard roomCard = new RoomCard();
        	 roomCard.setAgencyId(Integer.parseInt(agencyId));
             roomCard = commonManager.findOne(roomCard);
             if(roomCard == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该代理不存在")
                          , request, response, null, false);
             	 return;
             }
             UserInfo info = new UserInfo();
             info.setUserId(Long.parseLong(playerId3));
             info = commonManager.findOne(dataBase,info);
             if(info == null){
            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该玩家不存在")
                         , request, response, null, false);
            	 return;
             }
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("player", info).builder("agency", roomCard)
                     , request, response, null, false); 
         }else if(type == 2){
       	     UserInfo info2 = new UserInfo();
             info2.setUserId(Long.parseLong(playerId2));
             info2 = commonManager.findOne(dataBase,info2);
             if(info2 == null){
              	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "接收玩家不存在")
                           , request, response, null, false);
              	 return;
               }
              UserInfo info = new UserInfo();
              info.setUserId(Long.parseLong(playerId));
              info = commonManager.findOne(dataBase,info);
              if(info == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退卡玩家不存在")
                          , request, response, null, false);
             	 return;
              }
              if(Long.parseLong(playerId)==Long.parseLong(playerId2)){
            	  OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "自己不能退卡给自己")
                          , request, response, null, false);
             	 return;
              }
              OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("player", info).builder("player2", info2)
                      , request, response, null, false); 
          }else{
        	 RoomCard roomCard = new RoomCard();
        	 roomCard.setAgencyId(Integer.parseInt(agencyId1));
             roomCard = commonManager.findOne(roomCard);
             if(roomCard == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "退卡代理不存在")
                          , request, response, null, false);
             	 return;
             }
             RoomCard roomCard2 = new RoomCard();
             roomCard2.setAgencyId(Integer.parseInt(agencyId2));
             roomCard2 = commonManager.findOne(roomCard2);
             if(roomCard2 == null){
             	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "接收代理不存在")
                          , request, response, null, false);
             	 return;
             }
             OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("roomCard1", roomCard).builder("roomCard2", roomCard2)
                     , request, response, null, false); 
         }

    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/players"})
    public void queryMyPlayersByAency(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        int pageNo = NumberUtils.toInt(params.get("page"), 1);
        int pageSize = 20;
        int count = 0;
        Integer agencyId = NumberUtils.toInt(params.get("agencyId"), 0);
        RoomCard roomCard = new RoomCard();
        roomCard.setAgencyId(agencyId);
        roomCard = commonManager.findOne(roomCard);

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        List<UserInfo> result = null;
        if (roomCard != null) {
            List<Object> paramsList = new ArrayList<>();
            paramsList.add(roomCard.getAgencyId());
            List<UserInfo> results = commonManager.findList(dataBase,"select userId,headimgurl,name,sex,payBindId,payBindTime from user_inf where payBindId=?", paramsList.toArray(), UserInfo.class);
            if(results != null && results.size() > 0){
            	count = results.size();
            }
            StringBuilder stringBuilder = new StringBuilder("select userId,headimgurl,name,sex,payBindId,payBindTime from user_inf where payBindId=?");
            stringBuilder.append(" order by payBindTime desc limit ").append((pageNo - 1) * pageSize).append(",").append(pageSize);

            result = commonManager.findList(dataBase,stringBuilder.toString(), paramsList.toArray(), UserInfo.class);
        }
        int page = 0;
        if(count%pageSize== 0){
        	page = count/pageSize;
        }else{
        	page = count/pageSize+1;
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                        .builder("datas", result == null ? new ArrayList<>() : result).builder("page", page)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/queryroom"})
    public void queryroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	     Map<String, String> params = UrlParamUtil.getParameters(request);
   	     RoomCard roomCard = loadRoomCard(request);
   	     String roomId = params.get("roomId");
   	     Room r = new Room();
   	     r.setUsed(1);
   	     r.setRoomId(Long.parseLong(roomId));
   	     r = commonManager.findOne(r);
   	     if(r == null){
   	    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "房间不存在")
                     , request, response, null, false);
   	    	 return;
   	     }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        List<UserInfo> userList = new ArrayList<>();
   	     String playerIds = r.getPlayers();
   	     int flag = 1;
   	     if(StringUtils.isNoneBlank(playerIds)){
   	    	 String hql = " select * from user_inf where userId in("+playerIds+") ";
   	   	     userList = commonManager.findList(dataBase,hql, new Object[]{}, UserInfo.class);
   	   	     
   	   	     if(userList != null && userList.size() > 0 && !(roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel() ==99)){
   	   	    	 for(UserInfo info : userList){
   	   	    		 if(info.getPayBindId()==null){
   	   	    			 continue;
   	   	    		 }
   	   	    		 if(!info.getPayBindId().toString().equals(roomCard.getAgencyId().toString())){
   	   	    			 flag=0;
   	   	    		 }
   	   	    	 }
   	   	     }
   	     }
   	     QueryRoomInfo rinfo = new QueryRoomInfo();
   	     BeanUtils.copyProperties(r, rinfo);
   	     rinfo.setUserList(userList);
   	     OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("info", rinfo).builder("flag", flag)
              , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/unroom"})
    public void unroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	     Map<String, String> params = UrlParamUtil.getParameters(request);
   	     RoomCard roomCard = loadRoomCard(request);
   	     String roomId = params.get("roomId");
   	     Room r = new Room();
   	     r.setRoomId(Long.parseLong(roomId));

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


        r = commonManager.findOne(dataBase,r);
   	     if(r == null){
   	    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "房间不存在")
                     , request, response, null, false);
   	    	 return;
   	     }

   	     ServerConfig serverConfig=new ServerConfig();
	     serverConfig.setId(Integer.parseInt(r.getServerId().toString()));
	     serverConfig=commonManager.findOne(dataBase,serverConfig);
	     String url=serverConfig.getIntranet();
	     logger.info("+++++++++++++++++++++++++++++++++++++++++url:"+url);
	     if (StringUtils.isBlank(url)){
	         url=serverConfig.getHost();
	         logger.info("+++++++++++++++++++++++++++++++++++++++++url:"+url);
	     }
	     SystemUser user = loadSystemUser(request);
	     if (StringUtils.isNotBlank(url)){
	         int idx=url.indexOf(".");
	         if (idx>0){
	             idx=url.indexOf("/",idx);
	             if (idx>0){
	                 url=url.substring(0,idx);
	             }
	             int role = 1;
	             if(roomCard.getAgencyLevel() != null && roomCard.getAgencyLevel() == 99){
	            	 role = 0;
	             }
	             if(user.getRoleId() != null && user.getRoleId() >= 1){
	            	 role = 0;
	             }
	             url+="/online/notice.do?type=agencyDissRoom&roomId="+r.getRoomId()+"&agencyId="+roomCard.getAgencyId()+"&serverId="+r.getServerId()+"&role="+role;
	             String noticeRet = HttpUtil.getUrlReturnValue(url);
	             if("0".equals(noticeRet)){
	            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "解散失败")
		                     , request, response, null, false);
	             }else if("1".equals(noticeRet)){
	            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "解散成功")
		                     , request, response, null, false);
	             }else if("-3".equals(noticeRet)){
	            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "当前房间有绑定其他代理邀请码的玩家参与，请联系客服处理！")
		                     , request, response, null, false);
	             }else{
	            	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "解散失败")
		                     , request, response, null, false);
	             }
	         }
	     }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cash/log"})
    public void cashLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        String agencyId = params.get("agencyId");
        int pageSize = 20;
        List<CashLog> result = null;
        int count = 0;
        String hql = " select * from cashlog where 1=1 and state=2 ";
        List<Object> paramsList = new ArrayList<>();
        if(StringUtils.isNoneBlank(agencyId)){
       	 hql += "and agencyId=? ";
       	 paramsList.add(agencyId);
        }
        List<CashLog> list = commonManager.findList(hql, paramsList.toArray(), CashLog.class);
        if(list != null && list.size() > 0){
       	 count = list.size();
        }
        if(count > 0){
       	 hql += " order by createTime desc limit "+(pageNo - 1) * pageSize+ ","+pageSize;
            result = commonManager.findList(hql, paramsList.toArray(), CashLog.class);
        }
        int page;
        if(count%pageSize== 0){
            page = count/pageSize;
        }else{
            page = count/pageSize+1;
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("page", page)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/activityreward"})
    public void activityreward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
        String playerId = params.get("playerId");
        int pageSize = 20;
        List<ActivityReward> result = null;
        int count = 0;
        String hql = " select * from activity_reward where 1=1  ";
        List<Object> paramsList = new ArrayList<>();
        if(StringUtils.isNoneBlank(playerId)){
       	 hql += "and userId=? ";
       	 paramsList.add(playerId);
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


        List<ActivityReward> list = commonManager.findList(dataBase,hql, paramsList.toArray(), ActivityReward.class);
        if(list != null && list.size() > 0){
       	 count = list.size();
        }
        if(count > 0){
       	 hql += " order by rewardDate desc limit "+(pageNo - 1) * pageSize+ ","+pageSize;
            result = commonManager.findList(dataBase,hql, paramsList.toArray(), ActivityReward.class);
        }
        int page;
        if(count%pageSize== 0){
            page = count/pageSize;
        }else{
            page = count/pageSize+1;
        }
        if(result != null && result.size()>0){
        	for(ActivityReward a : result){
        		UserInfo info = new UserInfo();
        		info.setUserId(a.getUserId());
        		info = commonManager.findOne(dataBase,info);
        		if(info != null){
        			a.setUserName(info.getName());
        		}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("page", page)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/updateactivityreward"})
    public void updateactivityreward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String id = params.get("id");
        System.out.println("+++++++++++++++++++++++++"+id);
        String hql = "update activity_reward set state=2 where keyId="+id;

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        commonManager.saveOrUpdate(dataBase,hql, new Object[]{});
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/querygroupBypalerid"})
    public void querygroupBypalerid(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String playerId = params.get("playerId");
        if(StringUtils.isBlank(playerId)){
        	OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                    , request, response, null, false);
        	return;
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        String hql = "select * from t_group where groupId in( select groupId from t_group_user where userId=?)";
        List<Group> list = commonManager.findList(dataBase,hql, new Object[]{playerId}, Group.class);
        if(list != null && list.size() > 0){
        	for(Group g :  list){
        		GroupUser user = new GroupUser();
        		user.setGroupId(g.getGroupId());
        		g = commonManager.findOne(g);
        		if(g != null){
        			g.setCreatedTime(user.getCreatedTime());
        		}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("datas", list)
                , request, response, null, false);
    }
    
   
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/queryAgencyPaySource"})
    public void queryAgencyPaySource(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, String> params = UrlParamUtil.getParameters(request);

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        logger.info("params:{}", params);
        String agencyId = params.get("agencyId");
        String  hql = " select * from roomcardrecord where reactiveUserId =? order by createTime desc ";
       
        List<AgencyPayInfoList> result = new ArrayList<>();
        
        List<RoomCardRecord> rlist = commonManager.findList(dataBase, hql, new Object[]{agencyId}, RoomCardRecord.class);
        if(rlist != null && rlist.size() > 0){
        		for(RoomCardRecord r : rlist){
        			AgencyPayInfoList apl = new AgencyPayInfoList();
        			apl.setAgencyId(r.getActiveUserid());
        			apl.setCreateTime(r.getCreateTime());
        			apl.setNums(r.getRoomCardNumber());
        			hql = " select * from roomcardrecord where reactiveUserId =? order by createTime desc ";
        			List<RoomCardRecord> plist = commonManager.findList(dataBase, hql, new Object[]{r.getActiveUserid()}, RoomCardRecord.class);
        			if(plist != null && plist.size() > 0){
        				 List<AgencyPayInfo> result2 = new ArrayList<>();
        				for(RoomCardRecord p : plist){
        					AgencyPayInfo info = new AgencyPayInfo();
        					info.setAgencyId(p.getActiveUserid());
        					info.setCreateTime(p.getCreateTime());
        					info.setNums(p.getRoomCardNumber());
        					result2.add(info);
        				}
        				apl.setParentAgencyPayInfo(result2);
        			}
        			result.add(apl);
        		}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("datas", result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cards/jf/statistics"})
    public void jfcardsStatistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        RoomCard roomCard = loadRoomCard(request);
        SystemUser systemUser = loadSystemUser(request);

        if ((systemUser.getRoleId() == null || systemUser.getRoleId().intValue() <= 0)) {
            OutputUtil.output(1001, LanguageUtil.getString("no_auth"), request, response, false);
            return;
        }

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", list = new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        CacheEntity<String> cacheEntity = CacheEntityUtil.getCache("todayDataStatisticscard");
        if (cacheEntity == null) {
            CacheEntityUtil.setCache("todayDataStatisticscard", new CacheEntity<String>("1", 15));
            CommonDataStatistics.cardStatistics(commonManager);
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        list = commonManager.find(dataBase,"select * from gold_card_statistics where dateTime>=? and dateTime<=? order by dateTime desc "
                , new Object[]{dateFormat.format(date1), dateFormat.format(date2)});
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                .builder("datas", list != null ? list : new ArrayList<>()), request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/testgold"})
    public void testgold(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	String time = params.get("time");
    	SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
    	Date d = sim.parse(time);
    	SimpleDateFormat simf = new SimpleDateFormat("yyyyMMdd");
    	
        int dateTime = Integer.parseInt(simf.format(d));
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(d);
        String start = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    	CommonDataStatistics.goldStatistics(commonManager,dateTime,start,start,calendar.getTime());
    	OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/queryAgencyCard"})
    public void queryAgencyCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);

        int num =  NumberUtils.toInt(params.get("num"), 1000000);
        logger.info("params:{}", params);
        String  hql = " select * from roomcard where 1=1  and agencyId!=100000 ";
        hql += " and commonCard >=? ";
        List<RoomCard> result = null;
        List<Object> paramsList = new ArrayList<>();
       	paramsList.add(num);
        hql += " order by commonCard desc";
        result = commonManager.findList(hql, paramsList.toArray(), RoomCard.class);
        int count = 0;
        int total = 0;
        if(result != null && result.size() > 0){
        	count = result.size();
        	for(RoomCard r : result){
        		total += r.getCommonCard();
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("page", 1).builder("count", count).builder("total", total)
                        .builder("datas", result == null ? new ArrayList<>() : result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/info4"})
    public void loadUserInfo4(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        Long userId = NumberUtils.toLong(params.get("userId"), 0);

        if (userId <= 0) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                    , request, response, null, false);
            return;
        }

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        RoomCard roomCard = loadRoomCard(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo = commonManager.findOne(dataBase,userInfo);
        Group group = new Group();
        group.setCreatedUser(Long.parseLong(roomCard.getUserId().toString()));
        group = commonManager.findOne(dataBase,group);
        if(group == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "未创建俱乐部,还不能更换群主")
                    , request, response, null, false);
       	 return;
        }
        GroupUser up = new GroupUser();
        //up.setUserId(Long.parseLong(userId.toString()));
        up.setUserRole(0);
        up = commonManager.findOne(dataBase,up);
        int type = 0;
        if(up != null){
       	 type=1;
        }
        if (userInfo == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists"))
                    , request, response, null, false);
        } else {
           OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                                .builder("info", userInfo).builder("type", type)
                        , request, response, null, false);
        }
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/create/user/jt/group/manage"})
    public void createrGroupManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	 Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        RoomCard roomCard = loadRoomCard(request);
        if(roomCard == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该代理不存在")
                    , request, response, null, false);
       	 return;
        }
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        int type = Integer.parseInt(params.get("type"));
        Group group = new Group();
        group.setGroupState("1");
        group.setCreatedUser(Long.parseLong(roomCard.getUserId().toString()));
        group = commonManager.findOne(dataBase,group);
        if(group == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "俱乐部不存在")
                    , request, response, null, false);
       	 return;
        }
        UserInfo info = new UserInfo();
        info.setUserId(Long.parseLong(params.get("playerId")));
        info = commonManager.findOne(dataBase,info);
        if(info == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "成员不存在")
                    , request, response, null, false);
       	 return;
        }
        
        GroupUser u2 = new GroupUser();
        u2.setUserRole(0);
        u2.setGroupId(group.getGroupId());
        u2 = commonManager.findOne(dataBase,u2);
        if(type != 3){
       	 if(u2 != null){
           	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "该俱乐部已经存在群主，不能重复设置群主")
                        , request, response, null, false);
           	 return;
            }
        }else if(u2 != null){
       	  u2.setUserRole(2);
       	  commonManager.update(dataBase,u2, new String[]{"keyId"}, new Object[]{u2.getKeyId()});
        }
        
        GroupUser u = new GroupUser();
        u.setUserId(Long.parseLong(params.get("playerId")));
        u.setGroupId(group.getGroupId());
        u = commonManager.findOne(dataBase,u);
        if(u != null){
       	 /*if(!u.getGroupId().equals(group.getGroupId())){
       		 u = new GroupUser();
                u.setGroupName(group.getGroupName());
           	 u.setCreatedTime(new Date());
           	 u.setUserLevel(1);
           	 u.setPlayCount1(0);
           	 u.setPlayCount2(0);
           	 u.setUserName(info.getName());
           	 u.setUserNickname(info.getName());
           	 u.setUserId(Long.parseLong(params.get("playerId")));
                u.setGroupId(group.getGroupId());
                u.setInviterId(Long.parseLong(roomCard.getUserId().toString()));
                u.setUserRole(0);
           	 commonManager.save(u);
       	 }*/
       	 u.setUserRole(0);
       	 commonManager.update(dataBase,u, new String[]{"keyId"}, new Object[]{u.getKeyId()});
       	 //commonManager.saveOrUpdate(" update t_group_user set userRole=? where userId=? ", new Object[]{0,u.getUserId()});
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "执行成功")
                    , request, response, null, false);
       	 return;
        }else{
       	 u = new GroupUser();
            u.setGroupName(group.getGroupName());
       	 u.setCreatedTime(new Date());
       	 u.setUserLevel(1);
       	 u.setPlayCount1(0);
       	 u.setPlayCount2(0);
       	 u.setUserName(info.getName());
       	 u.setUserNickname(info.getName());
       	 u.setUserId(Long.parseLong(params.get("playerId")));
            u.setGroupId(group.getGroupId());
            u.setInviterId(Long.parseLong(roomCard.getUserId().toString()));
            u.setUserRole(0);
       	 commonManager.save(dataBase,u);
       	 commonManager.saveOrUpdate(dataBase," update t_group set currentCount=? where groupId=? ", new Object[]{group.getCurrentCount()+1,group.getGroupId()});
       	 commonManager.saveOrUpdate(dataBase," update user_inf set isGroup=? where userId=? ", new Object[]{1,params.get("playerId")});
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "执行成功")
                , request, response, null, false);
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/queryAgencyPay"})
    public void queryAgencyPay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);
        String type = params.get("type");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String hql = "";
        if("1".equals(type)){
        	hql = " select * from roomcard_order where commonCards >=7000 and  commonCards<=15000 ";
        }else if("2".equals(type)){
        	hql = " select * from roomcard_order where commonCards >15000 ";
        }else if("3".equals(type)){
        	hql = " select * from roomcardrecord where roomCardNumber >=20000 and roomCardNumber <=100000 ";
        }else{
        	hql = " select * from roomcardrecord where roomCardNumber >100000  ";
        }
        List<Object> paramsList = new ArrayList<>();
        if (StringUtils.isNotBlank(startDate)) {
            hql += " and createTime>=? ";
            paramsList.add(startDate+ " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
        	hql += " and createTime<=? ";
        	 paramsList.add(endDate+" 23:59:59");
        }
        hql += " order by createTime desc";
        List<PayInfo> result = new ArrayList<>();
        if("1".equals(type) || "2".equals(type)){
        	List<RoomCardOrder> rlist = commonManager.findList(hql, paramsList.toArray(), RoomCardOrder.class);
        	if(rlist != null && rlist.size() > 0){
        		for(RoomCardOrder r : rlist){
        			PayInfo p = new  PayInfo();
        			p.setNums(r.getCommonCards());
        			p.setCreateTime(r.getCreateTime());
        			p.setAgencyId(r.getRechargeAgencyId());
        			p.setReciveId(r.getRoleId());
        			result.add(p);
        		}
        	}
        }else{
        	List<RoomCardRecord> rlist = commonManager.findList( hql, paramsList.toArray(), RoomCardRecord.class);
        	if(rlist != null && rlist.size() > 0){
        		for(RoomCardRecord r : rlist){
        			PayInfo p = new  PayInfo();
        			p.setNums(r.getRoomCardNumber());
        			p.setCreateTime(r.getCreateTime());
        			p.setAgencyId(r.getActiveUserid());
        			p.setReciveId(Long.parseLong(r.getReactiveUserId().toString()));
        			result.add(p);
        		}
        	}
        }
        OutputUtil.output(MessageBuilder.newInstance().builder("code", 1000).builder("datas", result)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/querygroupbyId"})
    public void queryGroupManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	    Map<String, String> params = UrlParamUtil.getParameters(request);
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        logger.info("params:{}", params);
        Group group = new Group();
        group.setGroupState("1");
        group.setGroupId(Integer.parseInt(params.get("groupId")));
        group = commonManager.findOne(dataBase,group);
        if(group == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "俱乐部不存在")
                    , request, response, null, false);
       	 return;
        }
        UserInfo userInfo=new UserInfo();
        userInfo.setUserId(group.getCreatedUser());
        userInfo = commonManager.findOne(dataBase,userInfo);
        group.setLastGroupName(userInfo.getName());
        
        GroupUser group1 = new GroupUser();
        group1.setUserRole(0);
        group1.setGroupId(Integer.parseInt(params.get("groupId")));
        group1 = commonManager.findOne(dataBase,group1);
        group.setDescMsg(group1.getUserName());
        group.setGroupState(String.valueOf(group1.getUserId()));
        
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("data", group)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/changeGroupManage"})
    public void changeGroupManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	    Map<String, String> params = UrlParamUtil.getParameters(request);
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        logger.info("params:{}", params);
        RoomCard rc = loadRoomCard(request);
        Group group = new Group();
        group.setGroupState("1");
        group.setGroupId(Integer.parseInt(params.get("groupId")));
        group = commonManager.findOne(dataBase,group);
        if(group == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "俱乐部不存在")
                    , request, response, null, false);
       	 return;
        }
        UserInfo info = new UserInfo();
        info.setUserId(Long.parseLong(params.get("playerId")));
        info = commonManager.findOne(dataBase,info);
        if(info == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "成员不存在")
                    , request, response, null, false);
       	 return;
        }
        GroupUser u = new GroupUser();
        u.setGroupId(group.getGroupId());
        u.setUserRole(0);
        u = commonManager.findOne(dataBase,u);
        
        GroupUser u2 = new GroupUser();
        u2.setGroupId(group.getGroupId());
        u2.setUserId(Long.parseLong(params.get("playerId")));
        u2 = commonManager.findOne(dataBase,u2);
        if(u != null){
         u.setUserRole(2);
       	 commonManager.update(dataBase,u, new String[]{"keyId"}, new Object[]{u.getKeyId()});
        }
        if(u2 == null){
       	 u = new GroupUser();
         u.setGroupName(group.getGroupName());
       	 u.setCreatedTime(new Date());
       	 u.setUserLevel(1);
       	 u.setPlayCount1(0);
       	 u.setPlayCount2(0);
       	 u.setUserName(info.getName());
       	 u.setUserNickname(info.getName());
       	 u.setUserId(Long.parseLong(params.get("playerId")));
         u.setGroupId(group.getGroupId());
         u.setInviterId(Long.parseLong(rc.getUserId().toString()));
         u.setUserRole(0);
       	 commonManager.save(dataBase,u);
        }else{
        	 u2.setUserRole(0);
          	 commonManager.update(dataBase,u2, new String[]{"keyId"}, new Object[]{u2.getKeyId()});
          	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "执行成功")
                       , request, response, null, false);
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "执行成功")
                , request, response, null, false);
    }
   
    /*
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/paomadenglist"})
    public void paomadenglist(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	   SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));
    	   List<Object> paramsList = new ArrayList<>();
    	   
    	   
    	   StringBuilder stringBuilder = new StringBuilder();
    	   stringBuilder.append("select * from system_marquee limit 0,20");
           paramsList.add(1);
           List<SystemMarquee> result =commonManager.findList("select * from system_marquee where 1=? limit 0,20", new Object[]{1}, SystemMarquee.class);
    	OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("datas", result)
                , request, response, null, false);
    }*/
    
    
   /* @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/paomadengupdate"})
    public void paomadengupdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
//    	   SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));
    	   List<Object> paramsList = new ArrayList<>();
    	   SystemMarquee systemMarquee =new SystemMarquee();
    	   
//    	   StringBuilder stringBuilder = new StringBuilder();
//    	   stringBuilder.append("select * from system_marquee limit 0,20");
//           paramsList.add(1);
           if(params.get("id")!=null){
        	 int type=  Integer.valueOf(params.get("type"));
        	int delay= Integer.valueOf(params.get("delay"));
        	int round= Integer.valueOf(params.get("round"));
        	String content=params.get("content");
        	
        	   int result =commonManager.saveOrUpdate("UPDATE system_marquee SET type = ?,delay=?,round=?,content=?,updateTime=?,startTime=?,endTime=?,isuse=? WHERE id = ?; ", 
        			   new Object[]{type, delay,round,content,this.getNowDate(),this.strToDateLong(params.get("startTime")),this.strToDateLong(params.get("endTime"))});
        	   OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "修改成功").builder("data", result)
                       , request, response, null, false);
           }else{
        	   systemMarquee.setType(Integer.valueOf(params.get("type")));
        	   systemMarquee.setDelay(Integer.valueOf(params.get("delay")));
        	   systemMarquee.setRound(Integer.valueOf(params.get("round")));
        	   systemMarquee.setContent(params.get("content"));
        	   systemMarquee.setUpdateTime(this.getNowDate());
        	   systemMarquee.setStartTime(this.strToDateLong(params.get("startTime")));
        	   systemMarquee.setStartTime(this.strToDateLong(params.get("endTime")));
        	   systemMarquee.setIsuse(Integer.valueOf(params.get("isuse")));
        	   long result =commonManager.saveAndGetKey(systemMarquee);
        	 	OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "保存成功").builder("data", result)
                        , request, response, null, false);
           }
   
    }*/
    
    
   /* @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/stoppao"})
    public void stoppao(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	   
           if(params.get("id")!=null){
        	 int isuse=  Integer.valueOf(params.get("isuse"));
        	
        	   int result =commonManager.saveOrUpdate("UPDATE system_marquee SET updateTime=?,isuse=? WHERE id = ? ", 
        			   new Object[]{new Date(),isuse,params.get("id")});
        	   OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "修改成功").builder("data", result)
                       , request, response, null, false);
           }
   
    }*/
    
    /*@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/deletepao"})
    public void deletepao(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String, String> params = UrlParamUtil.getParameters(request);
    	   
           if(params.get("id")!=null){
        	 int id=  Integer.valueOf(params.get("id"));
        	   int result =commonManager.saveOrUpdate("DELETE FROM system_marquee WHERE id = ? ", 
        			   new Object[]{params.get("id")});
        	   OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "修改成功").builder("data", result)
                       , request, response, null, false);
           }
   
    }*/
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/jl"})
    public void jl(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	    Map<String, String> params = UrlParamUtil.getParameters(request);
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String userId = params.get("userId");

        Date date1 = null, date2 = null;
        if (StringUtils.isNotBlank(startDate)) {
            date1 = GeneralHelper.str2Date(startDate, "yyyy-MM-dd");
        }
        if (StringUtils.isNotBlank(endDate)) {
            date2 = GeneralHelper.str2Date(endDate, "yyyy-MM-dd");
        }

        Date currentDate = new Date();

        List<Map<String, Object>> list;
        if (date2 != null) {
            if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                date2 = currentDate;
            }
            if (date1 != null) {
                long days = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);
                if (days < 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "success")
                            .builder("datas", list = new ArrayList<>()), request, response, null, false);
                    return;
                } else {
                    if (days > 31) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date2);
                        cal.add(Calendar.DAY_OF_YEAR, -31);
                        date1 = cal.getTime();
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.add(Calendar.DAY_OF_YEAR, -31);
                date1 = cal.getTime();
            }
        } else {
            if (date1 != null) {
                if (((date1.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date1 = currentDate;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.DAY_OF_YEAR, 31);
                date2 = cal.getTime();

                if (((date2.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24)) > 0) {
                    date2 = currentDate;
                }
            } else {
                date2 = currentDate;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date2);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                date1 = cal.getTime();
            }
        }
        
        logger.info("params:{}", params);
//        Matchjl matchjl = new Matchjl();
//        matchjl.setAwardState("1");
        if(userId.length()>0){
        	 list = commonManager.find(dataBase,"SELECT t1.userId,t2.matchName,t1.userAward,t1.createdTime,t1.matchId,t1.awardState FROM t_match_user t1 LEFT JOIN  t_match t2 ON t1.matchType=t2.matchType WHERE t1.awardState!='0'  AND (t1.userAward  LIKE '%话费%' or t1.userAward  LIKE '%红包%')   AND t1.userId=? AND t1.createdTime >= ?  AND t1.createdTime <= ? GROUP BY t1.userId,t1.matchId"
                     , new Object[]{userId,CommonUtil.dateTimeToString(date1, "yyyy-MM-dd")+ " 00:00:00", CommonUtil.dateTimeToString(date2, "yyyy-MM-dd")+ " 23:59:59"});
        }else{
        	 list = commonManager.find(dataBase,"SELECT t1.userId,t2.matchName,t1.userAward,t1.createdTime,t1.matchId,t1.awardState FROM t_match_user t1 LEFT JOIN  t_match t2 ON t1.matchType=t2.matchType WHERE t1.awardState!='0'  AND (t1.userAward  LIKE '%话费%' or t1.userAward  LIKE '%红包%')  AND t1.createdTime >= ?  AND t1.createdTime <= ? GROUP BY t1.userId,t1.matchId"
                     , new Object[]{CommonUtil.dateTimeToString(date1, "yyyy-MM-dd")+ " 00:00:00", CommonUtil.dateTimeToString(date2, "yyyy-MM-dd")+ " 23:59:59"});
        }
       
        
        
//        List<Matchjl>matchjl1 = commonManager.findList(dataBase,matchjl);
        if(list == null){
       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "查询暂无数据")
                    , request, response, null, false);
       	 return;
        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("datas", list)
                , request, response, null, false);
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/ff"})
    public void ff(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	    Map<String, String> params = UrlParamUtil.getParameters(request);
        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

       
        
        logger.info("params:{}", params);
       String userId= params.get("userId");
       String matchId= params.get("matchId");
        Matchjl matchjl = new Matchjl();
        matchjl.setAwardState("2");
        matchjl.setModifiedTime(new Date());
        
        commonManager.update(matchjl,new String[]{"userId","matchId"},new Object[]{userId,matchId});
        
        /*list = commonManager.find(dataBase,"SELECT t1.userId,t2.matchName,t1.userAward,t1.createdTime FROM t_match_user t1 LEFT JOIN  t_match t2 ON t1.matchType=t2.matchType WHERE awardState='1' AND t1.createdTime >= ?  AND t1.createdTime <= ? ;"
                , new Object[]{CommonUtil.dateTimeToString(date1, "yyyy-MM-dd"), CommonUtil.dateTimeToString(date2, "yyyy-MM-dd")});
        */
        
//        List<Matchjl>matchjl1 = commonManager.findList(dataBase,matchjl);
//        if(list == null){
//       	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "俱乐部不存在")
//                    , request, response, null, false);
//       	 return;
//        }
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "修改成功").builder("datas", "成功")
                , request, response, null, false);
    }
    
    /**
     * 获取现在时间
     * 
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
  public static Date getNowDate() {
     Date currentTime = new Date();
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     String dateString = formatter.format(currentTime);
     ParsePosition pos = new ParsePosition(8);
     Date currentTime_2 = formatter.parse(dateString, pos);
     return currentTime_2;
  }
  
  /**
   * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
   * 
   * @param strDate
   * @return
   */
public static Date strToDateLong(String strDate) {
   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   ParsePosition pos = new ParsePosition(0);
   Date strtodate = formatter.parse(strDate, pos);
   return strtodate;
}

@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/whitemenu"})
public void whitemenu(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, String> params = UrlParamUtil.getParameters(request);
    logger.info("params:{}", params);
    String agencyId = params.get("agencyId").trim();
//    ResourcesConfigs resourcesConfigs = new ResourcesConfigs();
//    resourcesConfigs.setMsgType("ServerConfig");
//    resourcesConfigs.setMsgKey("credit_white_group_ids");
//    resourcesConfigs = commonManager.findOne(resourcesConfigs);
//    
//    if(resourcesConfigs == null){
//    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "该代理不存在")
//                 , request, response, null, false);
//    	 return;
//    }
//    String msgvalue=resourcesConfigs.getMsgValue();
//    String a=msgvalue+agencyId+",";
    
    commonManager.saveOrUpdate("update t_group set isCredit=1 where groupId=?  ", new Object[]{agencyId} );
    
    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "保存成功")
            , request, response, null, false);

}
	
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/backCardplayers"})
public void backCardplayers(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map<String, String> params = UrlParamUtil.getParameters(request);
    logger.info("params:{}", params);
    String agencyId = params.get("agencyId").trim();
//    ResourcesConfigs resourcesConfigs = new ResourcesConfigs();
//    resourcesConfigs.setMsgType("ServerConfig");
//    resourcesConfigs.setMsgKey("credit_white_group_ids");
//    resourcesConfigs = commonManager.findOne(resourcesConfigs);
//    
//    if(resourcesConfigs == null){
//    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "该代理不存在")
//                 , request, response, null, false);
//    	 return;
//    }
//    String msgvalue=resourcesConfigs.getMsgValue();
//    String a=msgvalue+agencyId+",";
    
    commonManager.saveOrUpdate("update t_group set isCredit=1 where groupId=?  ", new Object[]{agencyId} );
    
    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "保存成功")
            , request, response, null, false);

}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/querygoldroom"})
	public void querygoldroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
	     Map<String, String> params = UrlParamUtil.getParameters(request);
	     RoomCard roomCard = loadRoomCard(request);
	     Long userid = Long.valueOf(params.get("userid"));
	     UserInfo r = new UserInfo();
	     r.setUserId(userid);
	     r = commonManager.findOne(r);
	     if(r == null){
	    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "玩家不存在")
                 , request, response, null, false);
	    	 return;
	     }

	     OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "查询成功").builder("info", r.getPlayingTableId())
          , request, response, null, false);
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/delgoldroom"})
    public void delgoldroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	     Map<String, String> params = UrlParamUtil.getParameters(request);
   	     RoomCard roomCard = loadRoomCard(request);
   	     Long playingTableId = Long.valueOf(params.get("playingTableId"));
   	     List<UserInfo> r=new ArrayList<>();
   	     UserInfo ri = new UserInfo();
   	     ri.setPlayingTableId(playingTableId);

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));


        r = commonManager.findList(dataBase,ri);
   	     if(r == null&&r.size()==0){
   	    	 OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "查询不到对应数据")
                     , request, response, null, false);
   	    	 return;
   	     }
   	    	int result = commonManager.saveOrUpdate(dataBase,"update user_inf set playingTableId=0 where playingTableId=?", new Object[]{ r.get(0).getPlayingTableId()});

   	  OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "删除成功").builder("info", result)
   	          , request, response, null, false);

    }

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/transferLeader"})
    public void transferLeader(HttpServletRequest request, HttpServletResponse response) throws Exception {
   	     Map<String, String> params = UrlParamUtil.getParameters(request);

   	  SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_4");

   	     String a = String.valueOf(params.get("a"));
   	     String b = String.valueOf(params.get("b"));
   	     String c = String.valueOf(params.get("c"));

   	    int result = commonManager.saveOrUpdate("SELECT transfer(?,?,?)", new Object[]{ a,b,c});//(dataBase,"SELECT transfer(?,?,?);", new Object[]{ a,b,c});

   	  OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "成功").builder("info", result)
   	          , request, response, null, false);

    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/modifyPhoneNum"})
    public void modifyPhoneNum(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);

        Long userId = NumberUtils.toLong(params.get("userId"), 0);

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo = commonManager.findOne(dataBase,userInfo);
        if (userInfo == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists")), request, response, null, false);
            return;
        }
        String phoneNum = params.get("phoneNum");

        if(!isPhoneNum(phoneNum)){
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "手机号不正确"), request, response, null, false);
            return;
        }

        UserInfo exit = new UserInfo();
        exit.setPhoneNum(phoneNum);
        exit = commonManager.findOne(dataBase,exit);
        if (exit != null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, "手机号已存在"), request, response, null, false);
            return;
        }
        String phonePw = params.get("phonePw");
        String pwMsg = verifyPw(phonePw);
        if(pwMsg != null){
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, pwMsg), request, response, null, false);
            return;
        }
        phonePw = genPw(phonePw);
        int ret = commonManager.saveOrUpdate(dataBase, "update user_inf set phoneNum=?,phonePw=? where userId=?", new Object[]{phoneNum, phonePw, userInfo.getUserId()});
        if(ret == 1){
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "成功"), request, response, null, false);
        }else{
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(10001, "失败"), request, response, null, false);
        }
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/delPhoneNum"})
    public void delPhoneNum(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        Long userId = NumberUtils.toLong(params.get("userId"), 0);

        SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_"+NumberUtils.toInt(params.get("gameId"),1));

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo = commonManager.findOne(dataBase,userInfo);
        if (userInfo == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("player_not_exists")), request, response, null, false);
            return;
        }
        int ret = commonManager.saveOrUpdate(dataBase, "update user_inf set phoneNum=null,phonePw=null where userId=?", new Object[]{userInfo.getUserId()});
        if(ret == 1){
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1000, "成功"), request, response, null, false);
        }else{
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(10001, "失败"), request, response, null, false);
        }
    }

    /**
     * 验证短信合法性
     * 手机登录密码设定规则
     * 仅用 A-Z  a-z  0-9
     * 区分大小写
     * 不能纯数字 不能纯字母
     * 位数在6-8 位
     *
     * @return
     */
    private String verifyPw(String passwd) {
        if (passwd.length() < 6 || passwd.length() > 20) {
            return "密码长度不符6-8位规则";
        }
        if (!passwd.matches("^[A-Za-z0-9]+$")) {
            return "密码需数字和字母组合";
        }
        boolean flag1 = false, flag2 = false;
        for (int i = 0; i < passwd.length(); i++) {
            if (passwd.charAt(i) >= 65) {
                flag1 = true;
            } else if (passwd.charAt(i) <= 57) {
                flag2 = true;
            }
        }
        if (flag1 && flag2) {
            return null;
        } else {
            return "不能纯数字或不能纯字母";
        }

    }

    private boolean isPhoneNum(String phoneNum) {
        if (phoneNum == null || phoneNum.equals(""))
            return false;
        return phoneNum.matches("[0-9]{11}");
    }

    /**
     * 加密密码
     *
     * @param source
     * @return
     */
    private String genPw(String source) {
        return MD5Util.getStringMD5(source + "sanguo_shangyou_2013");
    }
}
