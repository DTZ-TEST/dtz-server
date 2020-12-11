package com.sy.controller;

import com.alibaba.fastjson.JSONObject;
import com.sy.entity.pojo.GroupUser;
import com.sy.entity.pojo.HbExchangeRecord;
import com.sy.entity.pojo.UserInfo;
import com.sy.entity.pojo.WXauthorization;
import com.sy.mainland.util.*;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MD5Util;
import com.sy.util.*;
import com.sy.util.weixin.PayUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/noauth/*"})
public class NoAuthController extends BaseController {

    File repository;

    private static final Logger logger = LoggerFactory.getLogger(NoAuthController.class);

    private static final int DOWNLOAD_VAL = 200;//一个下载奖励2元
    private static final int PLAYGAME_VAL = 200;//一次有效牌局奖励2元

    private static final String WX_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WX_ACCESS_USERINFO = "https://api.weixin.qq.com/sns/userinfo";

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cash"})
    public String goCash(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String appid = PropUtil.getString("appid");
        String code = UUID.randomUUID().toString();
        request.setAttribute("wx_appid", appid);
        setSessionValue(request, "cash_code_state", code);
        return "cash";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/proxy"})
    public String payProxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "pay_proxy";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/query"})
    public void query(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("params:{}", params);

        String orderId = params.get("orderId");

        if (StringUtils.isBlank(orderId)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1001, LanguageUtil.getString("param_error"))
                    , request, response, null, false);
            return;
        }


//<xml>
//     <sign><![CDATA[E1EE61A91C8E90F299DE6AE075D60A2D]]></sign>
//     <partner_trade_no><![CDATA[0010010404201411170000046545]]></partner_trade_no>
//     <mch_id ><![CDATA[10000097]]></mch_id >
//     <appid><![CDATA[wxe062425f740c30d8]]></appid>
//     <nonce_str><![CDATA[50780e0cca98c8c8e814883e5caa672e]]></nonce_str>
//</xml>

        Map<String, String> map = new LinkedHashMap<>();
        map.put("appid", PropUtil.getString("appid"));
        map.put("mch_id", PropUtil.getString("mchid"));
        map.put("nonce_str", MD5Util.getMD5String(UUID.randomUUID().toString()));
        map.put("partner_trade_no", orderId);

        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        StringBuilder strBuilder = new StringBuilder();
        for (String key : keys) {
            String value = map.get(key);
            if (StringUtils.isNotBlank(value)) {
                strBuilder.append(key).append("=").append(value).append("&");
            }
        }

        strBuilder.append("key=").append(PropUtil.getString("paykey"));

        map.put("sign", MD5Util.getMD5String(strBuilder));

        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append("<xml>");
        for (Map.Entry<String, String> kv : map.entrySet()) {
            paramBuilder.append("<").append(kv.getKey()).append(">").append("<![CDATA[");
            paramBuilder.append(kv.getValue());
            paramBuilder.append("]]></").append(kv.getKey()).append(">");
        }
        paramBuilder.append("</xml>");

        String postContent = paramBuilder.toString();
        String result = PayUtil.post(PropUtil.getString("mchid"), PayUtil.QUERY_URL, postContent);
        OutputUtil.output(1000, result, request, response, false);
    }


    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/callback/user}"})
    public String callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);

        logger.info("params:{}", params);

        String code = params.get("code");
        try {
            if (StringUtils.isNotBlank(code)) {

                Map<String, String> map = new LinkedHashMap<>();

                map.put("appid", PropUtil.getString("appid"));
                map.put("secret", PropUtil.getString("secret"));
                map.put("code", code);
                map.put("grant_type", "authorization_code");

                String result = HttpUtil.getUrlReturnValue(WX_ACCESS_TOKEN, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                logger.info("load access_token url:{},params:{},result:{}", WX_ACCESS_TOKEN, map, result);

                String openid = null;
                String access_token = null;
                String unionId = null;
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONObject.parseObject(result);
                    openid = json.getString("openid");
                    access_token = json.getString("access_token");
                    unionId = json.getString("unionid");
                }

                if (StringUtils.isNotBlank(openid) && StringUtils.isBlank(unionId)) {
                    map = new HashMap<>();

                    map.put("access_token", access_token);
                    map.put("openid", openid);

                    result = HttpUtil.getUrlReturnValue(WX_ACCESS_USERINFO, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                    logger.info("load userinfo url:{},params:{},result:{}", WX_ACCESS_USERINFO, map, result);

                    if (StringUtils.isNotBlank(result)) {
                        JSONObject json = JSONObject.parseObject(result);
                        unionId = json.getString("unionid");
                    }
                }

                if (StringUtils.isNotBlank(unionId)) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setIdentity(unionId);
                    userInfo = commonManager.findOne(userInfo);
                    if (userInfo != null) {

                        HbExchangeRecord hbExchangeRecord = new HbExchangeRecord();
                        hbExchangeRecord.setUserId(userInfo.getUserId());
                        hbExchangeRecord.setPhone(openid);
                        hbExchangeRecord.setState(0);

                        HbExchangeRecord hbExchangeRecord1 = commonManager.findOne(hbExchangeRecord);
                        if (hbExchangeRecord1 == null) {
                            hbExchangeRecord.setWxname(userInfo.getName());
                            hbExchangeRecord.setCreateTime(new Date());
                            hbExchangeRecord.setMoney(new BigDecimal(0));

                            commonManager.save(hbExchangeRecord);
                        }

                        setSessionValue(request, "player_openid", openid);
                        setSessionValue(request, "player", userInfo);
                        setSessionValue(request, "wx_appid", PropUtil.getString("appid"));
                        return "cash";
                    }
                }
            }
            response.sendRedirect("http://www.688gs.com");
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
            response.sendRedirect("http://www.688gs.com");
        }
        return null;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/download/proxy/{varAid:\\d{6}}"})
    public void downloadProxy(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAid") String tempId) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("NoAuthController|downloadProxy|inviterId:{}|params:{}", tempId, params);
        String code = params.get("code");
        try {
            if (StringUtils.isNotBlank(code)) {

                Map<String, String> map = new LinkedHashMap<>();

                map.put("appid", "wx2338735ace5a27c9");
                map.put("secret", "5ce09372872af60752a3624c6de01e68");
                map.put("code", code);
                map.put("grant_type", "authorization_code");

                String result = HttpUtil.getUrlReturnValue(WX_ACCESS_TOKEN, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                logger.info("load access_token inviterId:{},url:{},params:{},result:{}", tempId, WX_ACCESS_TOKEN, map, result);

                String openid = null;
                String access_token = null;
                String unionId = null;
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONObject.parseObject(result);
                    openid = json.getString("openid");
                    access_token = json.getString("access_token");
                    unionId = json.getString("unionid");
                }

                if (StringUtils.isNotBlank(openid) && StringUtils.isBlank(unionId)) {
                    map = new HashMap<>();

                    map.put("access_token", access_token);
                    map.put("openid", openid);

                    result = HttpUtil.getUrlReturnValue(WX_ACCESS_USERINFO, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                    logger.info("load userinfo inviterId:{}, url:{},params:{},result:{}", tempId, WX_ACCESS_USERINFO, map, result);

                    if (StringUtils.isNotBlank(result)) {
                        JSONObject json = JSONObject.parseObject(result);
                        unionId = json.getString("unionid");
                    }
                }

                if (StringUtils.isNotBlank(unionId)) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setIdentity(unionId);
                    if (commonManager.findOne(userInfo) == null) {
                        WXauthorization wx = new WXauthorization();
                        wx.setUnionId(unionId);
                        if (commonManager.findOne(wx) == null) {
                            wx.setAgencyId(0);
                            wx.setInviterId(Long.parseLong(tempId));
                            wx.setCreateTime(new Date());
                            wx.setInviterTime(wx.getCreateTime());
                            int ret = commonManager.save(wx);
                            logger.info("save WXauthorization success:{},inviterId:{},unionId:{}", ret, tempId, unionId);
                        }
                    } else {
                        logger.info("user playing unionId:{},inviterId:{}", unionId, tempId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        } finally {
            response.sendRedirect("http://bjdkw.secondbjd.club/agentZp/user/downTest/wx_plat/zpq/");
        }
    }

    private static final void forwardParams(HttpServletRequest request) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        for (Map.Entry<String, String> kv : params.entrySet()) {
            request.setAttribute(kv.getKey(), kv.getValue());
        }
    }

    @Override
    public String verificate() {
        return null;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/share/proxy/{varAid}"})
    public void downloadProxy1(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "varAid") String tempId) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("inviterId:{},params:{}", tempId, params);


//        String state=params.get("state");
        String code = params.get("code");
        try {
            if (StringUtils.isNotBlank(code)) {
                Map<String, String> map1 = new HashMap<>();
                Map<String, String> map = new LinkedHashMap<>();

                map.put("appid", PropUtil.getString("playlog_appid"));
                map.put("secret", PropUtil.getString("playlog_secret"));
                map.put("code", code);
                map.put("grant_type", "authorization_code");

                String result = HttpUtil.getUrlReturnValue(WX_ACCESS_TOKEN, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                logger.info("load access_token url:{},params:{},result:{}", WX_ACCESS_TOKEN, map, result);
                String sharepath = PropUtil.getString("sharepath", "", Constant.H5PAY_FILE);
                logger.info("sharepath:{}", sharepath);
                String openid = null;
                String access_token = null;
                String unionId = null;
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONObject.parseObject(result);
                    openid = json.getString("openid");
                    access_token = json.getString("access_token");
                    unionId = json.getString("unionid");
                }

                if (StringUtils.isNotBlank(openid) && StringUtils.isBlank(unionId)) {
                    map = new HashMap<>();

                    map.put("access_token", access_token);
                    map.put("openid", openid);

                    result = HttpUtil.getUrlReturnValue(WX_ACCESS_USERINFO, HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, map);

                    logger.info("load userinfo url:{},params:{},result:{}", WX_ACCESS_USERINFO, map, result);

                    if (StringUtils.isNotBlank(result)) {
                        JSONObject json = JSONObject.parseObject(result);
                        unionId = json.getString("unionid");
                    }
                }

                if (StringUtils.isNotBlank(unionId)) {

//                    SelectDataBase.DataBase dataBase = SelectDataBase.DataBase.valueOf("DB_3");
                    UserInfo userInfo = new UserInfo();
                    userInfo.setIdentity(unionId);

                    if (commonManager.findOne(userInfo) == null) {
                   /*     WXauthorization wx = new WXauthorization();
                        wx.setUnionId(unionId);*/
                        //查不到该玩家
                        response.sendRedirect(sharepath + "/notGroup");
                        logger.info("查不到该玩家");
                        /*if (commonManager.findOne( wx) == null) {
                            wx.setAgencyId(0);
                            wx.setInviterId(Long.parseLong(tempId));
                            wx.setCreateTime(new Date());
                            wx.setInviterTime(wx.getCreateTime());
                            int ret = commonManager.save( wx);
                            logger.info("save WXauthorization success={}:inviterId={},unionId={}", ret, tempId, unionId);
                        }*/
                    } else {
                        userInfo = commonManager.findOne(userInfo);
                        setSessionValue(request, "myuserid", userInfo.getUserId());
                        GroupUser groupUser = new GroupUser();
                        groupUser.setUserId(userInfo.getUserId());
                        String groupId = getSessionValue(request, "groupId");
                        logger.info("亲友圈ID" + groupId);
                        groupUser.setGroupId(Integer.valueOf(groupId));
                        if (commonManager.findOne(groupUser) == null) {
                            //跳转非亲友圈玩家
                            logger.info("跳转非亲友圈玩家");
                            response.sendRedirect(sharepath + "/notGroup");
                        } else {
                            String myid = String.valueOf(userInfo.getUserId());
                            logger.info("亲友圈玩家");
                            //几人局
                            String a = getSessionValue(request, "playerCount");
                            logger.info("getSessionValue playerCount={}", a);
                            //加入群主ID
                            GroupUser s = commonManager.findOne(groupUser);
                            GroupUser asd = new GroupUser();
                            asd.setUserRole(0);
                            asd.setGroupId(Integer.valueOf(groupId));
                            asd = commonManager.findOne(asd);
                            map1.put(String.valueOf(asd.getUserId()), String.valueOf(asd.getUserId()));

                            //没参与游戏
                            if (a.equals("2")) {
                                String player1ID = getSessionValue(request, "player1ID");
                                String player2ID = getSessionValue(request, "player2ID");
                                map1.put(player1ID, player1ID);
                                map1.put(player2ID, player2ID);

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player1ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "3");
                                    } else {
                                        setSessionValue(request, "player1Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player1ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "1");
                                        setSessionValue(request, "player1Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player1Status", "2");
                                    }
                                }

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player2ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "3");
                                    } else {
                                        setSessionValue(request, "player2Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player2ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "1");
                                        setSessionValue(request, "player2Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player2Status", "2");
                                    }
                                }
                            }
                            if (a.equals("3")) {
                                String player1ID = getSessionValue(request, "player1ID");
                                String player2ID = getSessionValue(request, "player2ID");
                                String player3ID = getSessionValue(request, "player3ID");
                                map1.put(player1ID, player1ID);
                                map1.put(player2ID, player2ID);
                                map1.put(player3ID, player3ID);

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player1ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "3");
                                    } else {
                                        setSessionValue(request, "player1Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player1ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "1");
                                        setSessionValue(request, "player1Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player1Status", "2");
                                    }
                                }

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player2ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "3");
                                    } else {
                                        setSessionValue(request, "player2Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player2ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "1");
                                        setSessionValue(request, "player2Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player2Status", "2");
                                    }
                                }
                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player3ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player3Status", "3");
                                    } else {
                                        setSessionValue(request, "player3Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player3ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player3Status", "1");
                                        setSessionValue(request, "player3Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player3Status", "2");
                                    }
                                }
                            }
                            if (a.equals("4")) {
                                String player1ID = getSessionValue(request, "player1ID");
                                String player2ID = getSessionValue(request, "player2ID");
                                String player3ID = getSessionValue(request, "player3ID");
                                String player4ID = getSessionValue(request, "player4ID");
                                map1.put(player1ID, player1ID);
                                map1.put(player2ID, player2ID);
                                map1.put(player3ID, player3ID);
                                map1.put(player4ID, player4ID);


                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player1ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "3");
                                    } else {
                                        setSessionValue(request, "player1Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player1ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player1Status", "1");
                                        setSessionValue(request, "player1Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player1Status", "2");
                                    }
                                }

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player2ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "3");
                                    } else {
                                        setSessionValue(request, "player2Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player2ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player2Status", "1");
                                        setSessionValue(request, "player2Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player2Status", "2");
                                    }
                                }

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player3ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player3Status", "3");
                                    } else {
                                        setSessionValue(request, "player3Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player3ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player3Status", "1");
                                        setSessionValue(request, "player3Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player3Status", "2");
                                    }
                                }

                                //等于3修改 4上传  不等于2 暂无 1打赏
                                if (player4ID.equals(myid)) {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(myid));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player4Status", "3");
                                    } else {
                                        setSessionValue(request, "player4Status", "4");
                                    }
                                } else {
                                    UserInfo userInfoS = new UserInfo();
                                    userInfoS.setUserId(Long.valueOf(player4ID));
                                    userInfoS = commonManager.findOne(userInfoS);
                                    if (userInfoS != null && userInfoS.getPhoto() != null && userInfoS.getPhoto().length() > 0) {
                                        setSessionValue(request, "player4Status", "1");
                                        setSessionValue(request, "player4Photo", userInfoS.getPhoto());
                                    } else {
                                        setSessionValue(request, "player4Status", "2");
                                    }
                                }
                            }
                            String oStatus = getSessionValue(request, "player1Status");
                            String tStatus = getSessionValue(request, "player2Status");
                            String tkliu = getSessionValue(request, "player3Status");
                            String fkliu = getSessionValue(request, "player4Status");
                            logger.info("player4Status={},player3Status(),player2Status={},player1Status()", fkliu, tkliu, tStatus, oStatus);
                            logger.info("map1={},UserId():{}", map1, userInfo.getUserId());
                            boolean isOK = map1.containsKey(String.valueOf(userInfo.getUserId()));
                            logger.info("isOK={}", isOK);
                            if (isOK == false) {
                                logger.info("跳转没有参与游戏");
                                response.sendRedirect(sharepath + "/noplay");
                            } else {


                                setSessionValue(request, "gmName", asd.getUserName());
                                setSessionValue(request, "groupName", asd.getGroupName());
                                setSessionValue(request, "groupNo", asd.getGroupId());
                                setSessionValue(request, "groupUserID", asd.getUserId());

                                UserInfo userInfo22 = new UserInfo();
                                userInfo22.setUserId(asd.getUserId());
                                userInfo22 = commonManager.findOne(userInfo22);
                                logger.info("userInfo22={}", userInfo22);
                                if (userInfo22 == null) {
                                    logger.info("无法查找到亲友圈群主 error");
                                } else {
                                    logger.info("gm1img={}", userInfo22.getHeadimgurl());
                                    if (userInfo22.getHeadimgurl() == null) {
                                        setSessionValue(request, "gm1img", "http://down.apk.51nmw.com/anhua/res/default_m.png");
                                        logger.info("设置管理员默认头像");
                                    } else {
                                        setSessionValue(request, "gm1img", userInfo22.getHeadimgurl());
                                    }
                                }
                            }


                            //都满足跳转
                            String kliu = getSessionValue(request, "playerCount");
                            logger.info("kliu={}", kliu);
                            if (kliu.equals("2")) {
                                logger.info("share={}", sharepath + "/share/share");
                                response.sendRedirect(sharepath + "/share/share");
                            } else if (kliu.equals("3")) {
                                logger.info("share={}", sharepath + "/share/share3");
                                response.sendRedirect(sharepath + "/share/share3");
                            } else if (kliu.equals("4")) {
                                logger.info("share={}", sharepath + "/share/share4");
                                response.sendRedirect(sharepath + "/share/share4");
                            }
                        }
                        logger.info("user playing:unionId={}", unionId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception:" + e.getMessage(), e);
        } 
    /*    finally {
            response.sendRedirect("http://www.688gs.com");
        }*/
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/notGroup"})
    public String notGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "notGroup";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/noplay"})
    public String noplay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "noplay";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/share/share"})
    public String shareshare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "share";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/share/share3"})
    public String shareshare3(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "share3";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/share/share4"})
    public String shareshare4(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "share4";
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/uploadImagetest"})
    public void uploadImagetest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        //返回客户端消息
        String resultMessage = "";
        logger.info("params:{}", params);
        String userID = getSessionValue(request, "userID");
        String myid = getSessionValue(request, "myid");
        logger.info("userID:{},myid:{}", userID, myid);
        if (!userID.equals(myid)) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(300, "无法操作他人相册")
                    , request, response, null, false);
            return;
        }
        if (getSessionValue(request, "userID") == null) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(300, "没有用户ID信息")
                    , request, response, null, false);
            return;
        }
        try {
            String userId = getSessionValue(request, "userID");
            System.out.println("我进入了post方法！");
            logger.info("我进入了post方法！");

            DiskFileItemFactory fileFactory = new DiskFileItemFactory();
            //设置缓冲区大小 这里是4KB （单位是B 字节）
            fileFactory.setSizeThreshold(4096);
            //设置缓存
            fileFactory.setRepository(repository);

            ServletFileUpload servletFileUpload = new ServletFileUpload(fileFactory);
            //设置文件上传的大小这里是4M
            servletFileUpload.setFileSizeMax(4194304);


            logger.info("我进入了try方法！");
            //得到上传文件列表
            List<FileItem> fileItem = servletFileUpload.parseRequest(request);

            Iterator<FileItem> iterator = fileItem.iterator();
            //得到保存路径
            File saveDir = UploadUtil.getSavePath(request);

            logger.info("我得到保存路径！" + saveDir);
            while (iterator.hasNext()) {

                FileItem file = iterator.next();
                if (file != null) {
                    String fileName = file.getName();
                    if (fileName != null) {


                        //System.out.println("上传文件的没有转码的文件名为："+fileName);
                        logger.info("1");
                        String saveFileName = UploadUtil.getSaveFileName(String.valueOf(System.currentTimeMillis()) + ".png");
                        //构建本地文件\
                        logger.info("2");
                        File saveFile = new File(saveDir, saveFileName);

                        logger.info("3");
                        file.write(saveFile);
                        String photopath = PropUtil.getString("photopath", "", Constant.H5PAY_FILE);
                        String absolutePath = PropUtil.getString("absolutePath", "", Constant.H5PAY_FILE);
                        // scaleImage(absolutePath+"/"+saveFileName, absolutePath+"/"+saveFileName, 2, saveFileName);
                        logger.info("4");
                        UserInfo userInfo = new UserInfo();
                        userInfo.setPhoto(photopath + saveFileName);
                        userInfo.setUserId(Long.valueOf(userId));
                        commonManager.update(userInfo, new String[]{"userId"}, new Object[]{userInfo.getUserId()});
                        logger.info("上传成功！！");


//                        resultMessage = UploadUtil.getResponseResult(200, "上传成功！");
                        setSessionValue(request, "photo", userInfo.getPhoto());

                    } else {
                        System.out.println("上传失败！");
//                        resultMessage = UploadUtil.getResponseResult(300, "请选择上传文件，上传失败！");
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(300, "文件上传错误，上传失败！")
                                , request, response, null, false);
                        return;
                    }
                }
            }
            logger.info("文件上传结束 ！");
        } catch (FileUploadException e) {
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(400, "文件上传错误，上传失败！")
                    , request, response, null, false);
//            resultMessage = UploadUtil.getResponseResult(300, "文件上传错误，上传失败！");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
//            resultMessage = UploadUtil.getResponseResult(300, "文件上传错误，上传失败！");
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(500, "文件上传错误，上传失败！")
                    , request, response, null, false);
            return;
        }
//        resultMessage = UploadUtil.getResponseResult(200, "上传成功！");
//        response.getWriter().print(resultMessage);//返回客户端 resultMessage
        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(200, "上传成功！")
                , request, response, null, false);
        return;
    }

    /*** 
     * 按指定的比例缩放图片 
     *
     * @param sourceImagePath
     *      源地址 
     * @param destinationPath
     *      改变大小后图片的地址 
     * @param scale
     *      缩放比例，如1.2 
     */
    public static void scaleImage(String sourceImagePath,
                                  String destinationPath, double scale, String format) {
        logger.info("进入压缩图片 scaleImage " + sourceImagePath);
        File file = new File(sourceImagePath);
        BufferedImage bufferedImage;
        try {
            logger.info("进入压缩图片 file " + file);
            bufferedImage = ImageIO.read(file);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            width = parseDoubleToInt(width * scale);
            height = parseDoubleToInt(height * scale);
            logger.info("进入压缩图片 width " + width);
            logger.info("进入压缩图片 width " + height);
            Image image = bufferedImage.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics graphics = outputImage.getGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            logger.info("进入压缩图片 destinationPath " + sourceImagePath);
            logger.info("进入压缩图片 format " + format);
            logger.info("进入压缩图片 outputImage " + format);
            ImageIO.write(outputImage, format, new File(destinationPath));
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("scaleImage方法压缩图片时出错了");

        }

    }

    /**
     * 将double类型的数据转换为int，四舍五入原则
     *
     * @param sourceDouble
     * @return
     */
    private static int parseDoubleToInt(double sourceDouble) {
        int result = 0;
        result = (int) sourceDouble;
        return result;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/checkimgi"})
    public void checkimgi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = UrlParamUtil.getParameters(request);
        logger.info("name:{},no:{},img:{},userID:{},myid:{}", params);
        String name = params.get("name");
        String no = params.get("no");
        String img = params.get("img");
        String userID = params.get("userID");
        String myid = params.get("myid");

        UserInfo userInfo22 = new UserInfo();
        userInfo22.setUserId(Long.valueOf(userID));
        userInfo22 = commonManager.findOne(userInfo22);

        setSessionValue(request, "name", name);
        setSessionValue(request, "no", no);
        setSessionValue(request, "img", img);
        setSessionValue(request, "userID", userID);
        setSessionValue(request, "myid", myid);
        if (userInfo22.getPhoto() != null && userInfo22.getPhoto().length() > 0) {
            setSessionValue(request, "photo", userInfo22.getPhoto());
        } else {
            setSessionValue(request, "photo", "");
        }
        OutputUtil.output(1000, "success", request, response, false);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/checkimgOK"})
    public String checkimgOK(HttpServletRequest request, HttpServletResponse response) throws Exception {
        forwardParams(request);
        return "checkimgi";
    }
}
