package com.sy599.game.gcommand.login.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.GoldPlayer;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.login.base.BaseSdk;
import com.sy599.game.gcommand.login.base.SdkFactory;
import com.sy599.game.gcommand.login.base.msg.User;
import com.sy599.game.gcommand.login.base.pfs.configs.PfSdkConfig;
import com.sy599.game.gcommand.login.base.pfs.configs.PfUtil;
import com.sy599.game.gcommand.login.base.pfs.qq.QQ;
import com.sy599.game.gcommand.login.base.pfs.qq.QqUtil;
import com.sy599.game.gcommand.login.base.pfs.weixin.Weixin;
import com.sy599.game.gcommand.login.base.pfs.weixin.WeixinUtil;
import com.sy599.game.gcommand.login.base.pfs.xianliao.Xianliao;
import com.sy599.game.gcommand.login.base.pfs.xianliao.XianliaoUtil;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.SslUtil;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public final class LoginUtil {

    /**
     * 签名密钥
     */
    public static final String DEFAULT_KEY = "A7E046F99965FB3EF151FE3357DBE828";

    /**
     * 用户登录
     */
    public static Map<String, Object> login(ChannelHandlerContext ctx, JSONObject params) {

        String ip = NettyUtil.userIpMap.get(ctx.channel());
        if (StringUtils.isBlank(ip)){
            ip = NettyUtil.getRemoteAddr(ctx);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            RegInfo regInfo = null;
            String username = params.getString("u");
            String password = params.getString("ps");
            if (password == null) {
                password = "123456";
            }
            String platform = params.getString("p");
            // 整包更新用_版本号
            String vc = params.getString("vc");

            // 游戏客户端_版本号
            String syvc = params.getString("syvc");

            String os = params.getString("os");
            String mac = params.getString("mac");
            String deviceCode = params.getString("deviceCode");

            String channelId = "";

            boolean isNewReg = false;
            BaseSdk sdk;
            Map<String, Object> modify = null;
            boolean fromThird = false;
            String info = null;
            String uid = null;
            String unionId = null;
            if (StringUtils.isNotBlank(platform)&&!"phoneLogin".equals(platform)) {// 使用平台SDK登录
                if (!PfUtil.isHasPf(platform) || (sdk = SdkFactory.getInst(platform, params)) == null) {
                    LogUtil.msgLog.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    return result;
                }

                boolean isWx = (sdk instanceof Weixin);
                boolean isXl = isWx ? false : (sdk instanceof Xianliao);
                boolean isQQ = isWx ? false : (sdk instanceof QQ);

                int trMark;
                ThirdRelation thirdRelation;

                if (isWx || isXl || isQQ) {
                    uid = params.getString("openid");
                    String code = params.getString("code");
                    int loginMark = 0;
                    if (StringUtils.isBlank(uid) && StringUtils.isNotBlank(code)) {
                        PfSdkConfig pfSdkConfig = PfUtil.getConfig(platform);
                        if (pfSdkConfig == null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("code", 2019);
                            return map;
                        }

                        String miniProgram = isWx ? params.getString("miniProgram") : null;

                        if (StringUtils.isBlank(miniProgram)) {
                            JsonWrapper jsonWrapper = isXl ? XianliaoUtil.getAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code)
                                    : WeixinUtil.getAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code);
                            if (jsonWrapper != null) {
                                String access_token = jsonWrapper.getString("access_token");
                                sdk.setOpt("auth:" + access_token);
                                if (isWx) {
                                    sdk.setExt("openid:" + jsonWrapper.getString("openid"));
                                }
                                info = sdk.loginExecute();
                                uid = sdk.getSdkId();
                                if (StringUtils.isNotBlank(info) && jsonWrapper.isHas("refresh_token")) {
                                    String refresh_token = jsonWrapper.getString("refresh_token");
                                    jsonWrapper = new JsonWrapper(info);
                                    jsonWrapper.putString("refresh_token", refresh_token);
                                    info = jsonWrapper.toString();
                                }
                            }
                        }else{
                            JsonWrapper jsonWrapper = WeixinUtil.jscode2session(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), code);
                            if (jsonWrapper!=null){
                                Weixin weixin = (Weixin) sdk;
                                weixin.setCode(code);
                                weixin.setSessionJson(jsonWrapper);

                                info = weixin.loginExecute();
                                uid = weixin.getSdkId();
                            }
                        }
                        loginMark = 1;
                    }

                    thirdRelation = UserRelationDao.getInstance().selectThirdRelation(uid, platform);
                    if (thirdRelation == null) {
                        if (loginMark != 1) {
                            sdk.setOpt("create");
                            info = sdk.loginExecute();
                            uid = sdk.getSdkId();
                        }
                        trMark = 1;
                    } else if (System.currentTimeMillis() - thirdRelation.getCheckedTime().getTime() >= 4 * 60 * 60 * 1000) {
                        if (loginMark != 1) {
                            sdk.setOpt("refresh");
                            info = sdk.loginExecute();
                            if (StringUtils.isNotBlank(uid) && !uid.equalsIgnoreCase(sdk.getSdkId())) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("code", 2018);
                                return map;
                            } else {
                                uid = sdk.getSdkId();
                            }
                        }
                        trMark = 2;
                    } else {
                        if (loginMark != 1) {
                            info = MessageBuilder.newInstance().builder("access_token", params.getString("access_token"))
                                        .builder("refresh_token", params.getString("refresh_token")).toString();
                        }
                        trMark = 0;
                    }

                    if (StringUtils.isBlank(info)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", 2018);
                        return map;
                    }
                } else {
                    info = sdk.loginExecute();
                    uid = sdk.getSdkId();
                    thirdRelation = UserRelationDao.getInstance().selectThirdRelation(uid, platform);
                    if (thirdRelation == null) {
                        trMark = 1;
                    } else {
                        trMark = 2;
                    }
                }

                // 验证成功
                if (!StringUtils.isBlank(uid)) {
                    fromThird = true;
                    // 给每个从第三方平台进来的用户初始化一个唯一的密码
                    password = "xsg_" + platform + "_pw_default_" + uid;

                    if (trMark == 0) {
                        regInfo = UserDao.getInstance().getUser(thirdRelation.getUserId());
                    } else {
                        if (isWx) {
                            if ("true".equals(ResourcesConfigsUtil.loadServerPropertyValue("weixin_openid"))) {
                                regInfo = UserDao.getInstance().getUser(uid, platform);
                            } else {
                                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(info);
                                unionId = jsonObject.getString("unionid");
                                if (StringUtils.isNotBlank(unionId)) {
                                    regInfo = UserDao.getInstance().getUser(unionId, "weixin", uid, platform);
                                } else {
                                    regInfo = UserDao.getInstance().getUser(uid, platform);
                                }
                            }
                        } else if (isXl) {
                            regInfo = UserDao.getInstance().getUser(uid, platform);
                        } else {
                            regInfo = UserDao.getInstance().getUser(uid, platform);
                        }
                    }

                    if (regInfo == null && StringUtils.isNotBlank(platform)) {
                        String bind_pf = params.getString("bind_pf");

                        if (StringUtils.isNotBlank(bind_pf)) {
                            String bind_access_token = params.getString("bind_access_token");
                            String bind_openid = params.getString("bind_openid");
                            String bind_fresh_token = params.getString("bind_fresh_token");

                            if (bind_pf.startsWith("weixin")) {
                                JsonWrapper bindMsg = WeixinUtil.getUserinfo(bind_access_token, bind_openid);
                                if (bindMsg != null && bindMsg.isHas("openid")) {
                                    ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bindMsg.getString("openid"), bind_pf);
                                    if (tr != null) {
                                        regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                    }
                                }
                            } else if (bind_pf.startsWith("xianliao")) {
                                JsonWrapper bindMsg = XianliaoUtil.getUserinfo(bind_access_token);
                                if (bindMsg == null) {
                                    PfSdkConfig pfSdkConfig = PfUtil.getConfig(bind_pf);
                                    if (pfSdkConfig != null) {
                                        bindMsg = XianliaoUtil.refreshAccessToken(pfSdkConfig.getAppId(), pfSdkConfig.getAppKey(), bind_fresh_token);
                                        if (bindMsg != null) {
                                            bindMsg = XianliaoUtil.getUserinfo(bindMsg.getString("access_token"));
                                        }
                                    }
                                }
                                if (bindMsg != null) {
                                    ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bindMsg.getString("openId"), bind_pf);
                                    if (tr != null) {
                                        regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                    }
                                }
                            } else if (bind_pf.startsWith("qq")) {
                                PfSdkConfig pfSdkConfig = PfUtil.getConfig(bind_pf);
                                if (pfSdkConfig != null) {
                                    JsonWrapper bindMsg = QqUtil.getUserinfo(pfSdkConfig.getAppId(), bind_access_token, bind_openid);
                                    if (bindMsg != null) {
                                        ThirdRelation tr = UserRelationDao.getInstance().selectThirdRelation(bind_openid, bind_pf);
                                        if (tr != null) {
                                            regInfo = UserDao.getInstance().getUser(tr.getUserId());
                                        }
                                    }
                                }
                            }
                        }

                    }

                    // 自动注册
                    if (regInfo == null) {
                        long maxId;
                        if (Redis.isConnected()) {
                            String cacheKey = CacheUtil.loadStringKey(String.class, "user_max_id20180403");
                            if (RedisUtil.tryLock(cacheKey, 2, 2000)) {
                                try {
                                    maxId = generatePlayerId();
                                } catch (Exception e) {
                                    maxId = 0;
                                    LogUtil.e("Exception:" + e.getMessage(), e);
                                } finally {
                                    RedisUtil.unlock(cacheKey);
                                }
                            } else {
                                maxId = 0;
                            }
                        } else {
                            maxId = generatePlayerId();
                        }

                        if (maxId <= 0) {
                            result.put("code", 999);
                            result.put("msg", "登录异常,请稍后再试");

                            return result;
                        }

                        regInfo = new RegInfo();
                        String mangguoPf = "common";
                        if(unionId != null) {// 芒果跑得快渠道记录
                            LogUtil.msgLog.info("unionId:" + unionId);
                            MGauthorization mGauthorization = MGauthorizationDao.getInstance().getMGauthorization(unionId);
                            if(mGauthorization != null) {
                                mangguoPf = mGauthorization.getPf();
                                LogUtil.msgLog.info("mangguoPf:" + mangguoPf);
                            }
                        }
                        regInfo.setChannel(mangguoPf);
                        regInfo.setOs(os);
                        buildBaseUser(regInfo, platform, maxId);
                        sdk.createRole(regInfo, info);

                        if (regInfo.getPayBindId() > 0) {
                            Integer bindGiveRoomCards = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "bindGiveRoomCards");
                            if (bindGiveRoomCards != null && bindGiveRoomCards.intValue() > 0) {
                                regInfo.setFreeCards(regInfo.getFreeCards() + bindGiveRoomCards.intValue());
                                MessageUtil.sendMessage(false,true,UserMessageEnum.TYPE0,regInfo,"绑定邀请码" + regInfo.getPayBindId() + "成功，获得房卡x" + bindGiveRoomCards,null);
                            }
                        }

                        isNewReg = UserDao.getInstance().addUser(regInfo) > 0L;
                    } else {
                        if (trMark == 0) {
                            modify = new HashMap<>();
                        } else {
                            modify = sdk.refreshRole(regInfo, info);
                        }
                    }

                    if (regInfo.getUserId() > 0 && StringUtils.isNotBlank(platform) && StringUtils.isNotBlank(uid)) {
                        if (trMark == 0) {
                        } else if (trMark == 1) {
                            UserRelationDao.getInstance().insert(new ThirdRelation(regInfo.getUserId(), platform, uid));
                        } else if (trMark == 2) {
                            UserRelationDao.getInstance().updateCheckedTime(thirdRelation.getKeyId().toString());
                        }
                    }

                } else {
                    LogUtil.msgLog.info("platform::" + platform + " uid::" + uid + " login auth error");
                    result.put("code", 997);
                    result.put("msg", "没有找到该平台" + platform);
                    return result;
                }
            } else if("phoneLogin".equals(platform)) {
            }else {
                platform = "self";
            }

            // 验证用户名和密码合法性
            if (regInfo == null && (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(platform))) {
                result.put("code", 995);
                result.put("msg", "账号或密码不合法");
            } else {
                if (regInfo == null) {
                    regInfo = UserDao.getInstance().getUser(username, platform);

                }
                if (regInfo == null) {
                    // 验证成功
                    if (!StringUtils.isBlank(username)) {
                        // 给每个从第三方平台进来的用户初始化一个唯一的密码
                        password = "xsg_" + platform + "_pw_default_" + username;
                        regInfo = UserDao.getInstance().getUser(username, platform);
                    }
                }
                if (regInfo != null) {

                    if (regInfo.getUserState() != null && regInfo.getUserState().intValue() == 0) {
                        result.put("code", 604);
                        result.put("msg", "您已被禁止登录，请联系所在群主或客服！");
                        return result;
                    }
                    String md5Pw=genPw(password);
                    boolean isPhoneLogin = "phoneLogin".equals(platform) ? md5Pw.equals(regInfo.getPhonePw()) : md5Pw.equals(regInfo.getPw());
                    if (fromThird || isPhoneLogin) {

                        //如果是手机登录username存的是手机号码，需要替换成username
                        if ("phoneLogin".equals(platform)){
                            username=regInfo.getName();
                        }

                        String gamevc = params.getString("gamevc");
//                        String extend = regInfo.getExtend();
                        boolean checkVc = false;
                        String versionCheck = ResourcesConfigsUtil.loadServerPropertyValue("login_version_" + platform);
                        if (versionCheck == null) {
                            versionCheck = ResourcesConfigsUtil.loadServerPropertyValue("login_version_all");
                        }
                        if (StringUtils.isNotBlank(versionCheck) && StringUtils.isNotBlank(gamevc)) {
                            checkVc = checkVersion(versionCheck, 1, gamevc, 1);
                        }
                        if (checkVc) {
                            // 版本错误
                            result.put("code", 2017);
                            result.put("msg", "有新的更新内容，请退出重登！");

                            return result;
                        }

                        if (modify == null) {
                            modify = new HashMap<>();
                        }
                        Date now = TimeUtil.now();
                        if (regInfo.getLogTime() != null && !TimeUtil.isSameDay(regInfo.getLogTime().getTime(), now.getTime())) {
                            modify.put("loginDays", 1);
                            regInfo.setPreLoginTime(regInfo.getLogTime());
                            modify.put("preLoginTime", regInfo.getPreLoginTime());
                        }
                        regInfo.setSessCode(genSessCode(username));
                        regInfo.setLogTime(now);

                        modify.put("sessCode", regInfo.getSessCode());
                        modify.put("logTime", regInfo.getLogTime());

                        if (!StringUtils.isBlank(os) && !os.equals(regInfo.getOs())) {
                            modify.put("os", os);
                        }

                        if (!StringUtils.isBlank(ip) && !ip.equals(regInfo.getIp())) {
                            modify.put("ip", ip);
                        }
                        // modify.put("ip", "");
                        if (!StringUtils.isBlank(mac) && !mac.equals(regInfo.getMac())) {
                            modify.put("mac", mac);
                        }
                        if (!StringUtils.isBlank(deviceCode) && !deviceCode.equals(regInfo.getSessCode())) {
                            modify.put("deviceCode", deviceCode);
                        }
                        if (!StringUtils.isBlank(syvc) && !syvc.equals(regInfo.getSyvc())) {
                            modify.put("syvc", syvc);
                        }
                        int totalCount = checkTotalCount(regInfo);
                        if (regInfo.getTotalCount() != totalCount) {
                            modify.put("totalCount", totalCount);
                        }
                        // 检测房间号
                        int servserId = 0;
                        if (regInfo.getPlayingTableId() != 0) {
                            if (GoldRoomUtil.isGoldRoom(regInfo.getPlayingTableId())) {
                                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(regInfo.getPlayingTableId());
                                if (goldRoom != null && NumberUtils.toInt(goldRoom.getCurrentState()) < 2) {
                                    servserId = goldRoom.getServerId().intValue();
                                }
                            } else {
                                RoomBean room = TableDao.getInstance().queryUsingRoom(regInfo.getPlayingTableId());
                                if (room != null) {
                                    servserId = room.getServerId();
                                }
                            }

                            if (servserId != regInfo.getEnterServer()) {
                                regInfo.setEnterServer(servserId);
                                modify.put("enterServer", servserId);
                            }
                            if (servserId == 0) {
                                regInfo.setPlayingTableId(0);
                                modify.put("playingTableId", 0);
                            }
                        }

                        result.put("isIosAudit", 0);

                        UserDao.getInstance().updateUser(regInfo, modify);

                        if (StringUtils.isNotBlank(info) && info.contains("access_token") && StringUtils.isNotBlank(uid)) {
                            JsonWrapper jsonWrapper = new JsonWrapper(info);
                            result.put("access_token", jsonWrapper.getString("access_token"));
                            result.put("refresh_token", jsonWrapper.getString("refresh_token"));
                            result.put("openid", uid);
                            result.put("pf", platform);
                        }
                        // 登录成功
                        result.put("code", 0);

                        result.put("notices", Collections.EMPTY_LIST);

                        String systemMessage = NoticeDao.getInstance().loadSystemNotice();
                        if (!StringUtils.isBlank(systemMessage)) {
                            result.put("message", systemMessage);
                        }

                        regInfo.setPf(platform);
                        String timeRemoveBindStr = ResourcesConfigsUtil.loadServerPropertyValue("periodRemoveBind");
                        if (!StringUtils.isBlank(timeRemoveBindStr) && !"0".equals(timeRemoveBindStr)) {
                            // 七天没玩并且绑码超过七天，解绑
                            if (regInfo.getPayBindId() > 0 && TimeUtil.apartDays(regInfo.getPayBindTime(), new Date()) > 7 && regInfo.getLastPlayTime() != null && TimeUtil.apartDays(regInfo.getLastPlayTime(), new Date()) > 7) {
                                int res = autoRemoveBind(regInfo);
                                if (res == 1) {
                                    regInfo.setPayBindId(0);
                                }
                            }
                        }
                        User user = new User();
                        user.setIsNewReg(isNewReg ? 1 : 0);
                        regInfo.setPf(platform);
                        if (platform != null) {
                            if (platform.startsWith("weixin")) {
                                String name = new JsonWrapper(regInfo.getLoginExtend()).getString("wx");
                                if (name != null)
                                    regInfo.setName(name);
                            } else if (platform.startsWith("xianliao")) {
                                String name = new JsonWrapper(regInfo.getLoginExtend()).getString("xl");
                                if (name != null)
                                    regInfo.setName(name);
                            } else if (platform.startsWith("qq")) {
                                String name = new JsonWrapper(regInfo.getLoginExtend()).getString("qq");
                                if (name != null)
                                    regInfo.setName(name);
                            }
                        }

                        user = buildUser(regInfo, user, ctx);

                        result.put("user", user);
                        result.put("currTime", TimeUtil.currentTimeMillis());

                        int payBindId = regInfo.getPayBindId();

                        HashMap<String, Object> agencyInfo = null;
                        if (payBindId > 0) {
                            agencyInfo = RoomCardDao.getInstance().queryAgencyByAgencyId(payBindId);
                            if (agencyInfo != null) {
                                Map<String, Object> retMap = new HashMap<>();
                                retMap.put("agencyId", agencyInfo.get("agencyId"));
                                retMap.put("agencyWechat", agencyInfo.get("agencyWechat"));
                                retMap.put("agencyPhone", agencyInfo.get("agencyPhone"));
                                retMap.put("agencyPf", agencyInfo.get("pf"));

                                result.put("agencyInfo", retMap);
                            }
                        }

                        // 自有平台渠道号
                        if (!StringUtils.isBlank(channelId)) {
                            result.put("channelid", channelId);
                        }

                        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("open_user_relation"))) {
                            String gameCode = params.getString("gameCode");
                            if (org.apache.commons.lang3.StringUtils.isBlank(gameCode)) {
                                if (platform.startsWith("weixin") && platform.length() > 6) {
                                    gameCode = platform.substring(6);
                                } else {
                                    gameCode = platform;
                                }
                            }
                            String userId = String.valueOf(regInfo.getUserId());
                            UserRelation relation = UserRelationDao.getInstance().select(gameCode, userId);
                            if (relation == null) {
                                relation = new UserRelation();
                                relation.setGameCode(gameCode);
                                relation.setUserId(userId);
                                relation.setRegPf(platform);
                                relation.setRegTime(new Date());
                                relation.setLoginPf(platform);
                                relation.setLoginTime(relation.getRegTime());
                                UserRelationDao.getInstance().insert(relation);
                            } else {
                                UserRelationDao.getInstance().update(relation.getKeyId().toString(), platform, new Date());
                            }

                            if (agencyInfo != null && agencyInfo.size() > 0) {
                                String agencyPf = String.valueOf(agencyInfo.get("pf"));
                                if (org.apache.commons.lang3.StringUtils.isNotBlank(agencyPf) && (!"null".equalsIgnoreCase(agencyPf))) {
                                    gameCode = gameCode + "_" + agencyPf;
                                    relation = UserRelationDao.getInstance().select(gameCode, userId);
                                    if (relation == null) {
                                        relation = new UserRelation();
                                        relation.setGameCode(gameCode);
                                        relation.setUserId(userId);
                                        relation.setRegPf(platform);
                                        relation.setRegTime(new Date());
                                        relation.setLoginPf(platform);
                                        relation.setLoginTime(relation.getRegTime());
                                        UserRelationDao.getInstance().insert(relation);
                                    } else {
                                        UserRelationDao.getInstance().update(relation.getKeyId().toString(), platform, new Date());
                                    }
                                }
                            }

                        }
                    } else {
                        // 密码错误
                        result.put("code", 994);
                        result.put("msg", "密码错误");
                    }
                } else {
                    // 用户不存在
                    result.put("code", 996);
                    result.put("msg", "账号不存在");
                    LogUtil.msgLog.error("code 996-->user not exist:" + username);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("login.exception:" + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 加密密码
     *
     * @param source
     * @return
     */
    public static String genPw(String source) {
        return MD5Util.getMD5String(source + "sanguo_shangyou_2013");
    }

    /**
     * 生成session code
     *
     * @param username
     * @return
     */
    public static String genSessCode(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(UUID.randomUUID().toString());
        return MD5Util.getMD5String(sb.toString());
    }

    private static long generatePlayerId() throws Exception {
        synchronized (LoginUtil.class) {
            long maxId = UserDao.getInstance().getMaxId();
            long min_player_id = Long.parseLong(ResourcesConfigsUtil.loadServerPropertyValue("min_player_id", "100000"));
            if (maxId < min_player_id) {
                maxId = min_player_id;
            }
            maxId++;
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("withFilterUserId", "0"))) {
                while (filterUserId(maxId)) {
                    maxId++;
                }
            }
            return maxId;
        }
    }

    /**
     * 过滤(2017.04.11添加)<br/>
     * 4A/5A/6A，如：102222、85555<br/>
     * ABCD/ABCDE/ABCDEF，如：201234、56789<br/>
     * 3A/3B，如：111222<br/>
     *
     * @param userId
     * @return
     */
    private static boolean filterUserId(long userId) {
        String userIdStr = String.valueOf(userId);
        if (userIdStr.length() >= 4) {
            int count = 1;
            int temp = userIdStr.charAt(userIdStr.length() - 1) - userIdStr.charAt(userIdStr.length() - 2);

            switch (temp) {
                case 0:
                    boolean isAAABBB = false;
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == 0) {
                            count++;
                            if (count >= 3 || (isAAABBB && count >= 2)) {
                                return true;
                            }
                        } else {
                            if (count >= 2) {
                                count = 0;
                                isAAABBB = true;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                case 1:
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == 1) {
                            count++;
                            if (count >= 3) {
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
                case -1:
                    for (int i = userIdStr.length() - 2; i >= 1; i--) {
                        if (userIdStr.charAt(i) - userIdStr.charAt(i - 1) == -1) {
                            count++;
                            if (count >= 3) {
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
                default:
                    return false;
            }

        }
        return false;
    }

    private static void buildBaseUser(RegInfo regInfo, String platform, long maxId) {
        regInfo.setPf(platform);
        regInfo.setUserId(maxId);
        regInfo.setLoginDays(1);
        Integer giveRoomCards = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "giveRoomCards");
        regInfo.setFreeCards(giveRoomCards == null ? 0 : giveRoomCards.longValue());
        regInfo.setPlayedSid("[]");
        regInfo.setConfig("1,1");
    }

    /**
     * 检查玩家局数
     *
     * @param regInfo
     * @return
     */
    private static int checkTotalCount(RegInfo regInfo) {
        if (!StringUtils.isBlank(regInfo.getExtend())) {
            JsonWrapper wrapper = new JsonWrapper(regInfo.getExtend());
            String val5 = wrapper.getString(5);
            String val6 = wrapper.getString(6);
            int total5 = 0;
            if (!StringUtils.isBlank(val5)) {
                total5 = split(val5);
            }
            int total6 = 0;
            if (!StringUtils.isBlank(val6)) {
                total6 = split(val6);
            }
            return total5 + total6;
        }
        return 0;

    }

    /**
     * 分解出局数
     *
     * @param val
     * @return
     */
    private static int split(String val) {
        int total = 0;
        String[] values = val.split(";");
        for (String value : values) {
            String[] _values = value.split(",");
            if (_values.length < 2) {
                continue;
            }
            int valInt = Integer.parseInt(_values[1]);
            total += valInt;
        }
        return total;
    }

    private static User buildUser(RegInfo userInfo, User user, ChannelHandlerContext ctx) throws Exception {
        Server server = null;

        //局数+充值》》》用于用户分级
        //((-usedCards+cards)/150+totalCount)
        long totalCount = (-userInfo.getUsedCards() + userInfo.getCards()) / 150 + userInfo.getTotalCount();

        boolean loadFromCheckNet = true;
        String[] gameUrls = null;
        RoomBean room;

        String bst = ResourcesConfigsUtil.loadServerPropertyValue("base_server_type"+GameServerConfig.SERVER_ID);
        if (StringUtils.isBlank(bst)){
            bst = ResourcesConfigsUtil.loadServerPropertyValue("base_server_type");
        }
        int baseServerType = StringUtils.isNotBlank(bst) ? NumberUtils.toInt(bst,1) : 1;

        if (GoldRoomUtil.isGoldRoom(userInfo.getPlayingTableId())) {
            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(userInfo.getPlayingTableId());
            if (goldRoom != null && NumberUtils.toInt(goldRoom.getCurrentState()) < 2) {
                server = ServerManager.loadServer(goldRoom.getServerId());
                if (server == null) {
                    gameUrls = CheckNetUtil.loadGameUrl(goldRoom.getServerId(), totalCount);
                    if (gameUrls != null) {
                        server = new Server();
                        server.setId(goldRoom.getServerId());
                        if (gameUrls[0].startsWith("ws:")){
                            server.setChathost(gameUrls[0]);
                        }else if (gameUrls[0].startsWith("wss:")){
                            server.setWssUri(gameUrls[0]);
                        }
                        loadFromCheckNet = false;
                    }
                }
            }
        } else if (userInfo.getPlayingTableId() > 0 && (room = TableDao.getInstance().queryRoom(userInfo.getPlayingTableId())) != null && room.getUsed() > 0) {
            server = ServerManager.loadServer(room.getServerId());
            if (server == null) {
                gameUrls = CheckNetUtil.loadGameUrl(room.getServerId(), totalCount);
                if (gameUrls != null) {
                    server = new Server();
                    server.setId(room.getServerId());
                    if (gameUrls[0].startsWith("ws:")){
                        server.setChathost(gameUrls[0]);
                    }else if (gameUrls[0].startsWith("wss:")){
                        server.setWssUri(gameUrls[0]);
                    }
                    loadFromCheckNet = false;
                }
            }
        } else {
            String loginExtend = userInfo.getLoginExtend();
            if (StringUtils.isNotBlank(loginExtend)){
                String matchId = JSON.parseObject(loginExtend).getString("match");
                if (StringUtils.isNotBlank(matchId)) {
                    MatchBean matchBean = MatchDao.getInstance().selectOne(matchId);
                    if (matchBean != null && !JjsUtil.isOver(matchBean)) {
                        server = ServerManager.loadServer(matchBean.getServerId().intValue());
                    }
                }
            }

            if (server == null){
                server = ServerManager.loadServer(userInfo.getPf(), baseServerType);
            }
        }

        if (server == null) {
            LogUtil.msgLog.info("buildUser server is null-->uId:" + userInfo.getUserId() + " enterServer:" + userInfo.getEnterServer());
            server = ServerManager.loadServer(userInfo.getPf(), baseServerType);
            if (server == null) {
                server = ServerManager.loadServer(userInfo.getPf(), 0);
            }
        }
        if (user == null) {
            user = new User();
        }
        user.setServerId(server.getId());
        user.setUsername(userInfo.getFlatId());
        user.setUserId(userInfo.getUserId());
        user.setPf(userInfo.getPf());
        user.setName(userInfo.getName());
        user.setSex(userInfo.getSex());
        user.setHeadimgurl(userInfo.getHeadimgurl());//
        if ("1".equals(GoldConstans.isGoldSiteOpen())) {
            GoldPlayer goldInfo = GoldDao.getInstance().selectGoldUserByUserId(userInfo.getUserId());
            if (goldInfo != null) {
                user.setGoldUserInfo(goldInfo);
            }
        }

        boolean useSsl = SslUtil.hasSslHandler(ctx);

        if (loadFromCheckNet) {
            gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
        }

        if (gameUrls==null){
            user.setConnectHost(useSsl ? server.getWssUri() : server.getChathost());
            user.setConnectHost1("");
            user.setConnectHost2("");
        }else{
            String url0;
            if (useSsl){
                url0 = (StringUtils.isNotBlank(gameUrls[0])&&gameUrls[0].startsWith("wss:"))?gameUrls[0]:server.getWssUri();
            }else{
                url0 = (StringUtils.isNotBlank(gameUrls[0])&&gameUrls[0].startsWith("ws:"))?gameUrls[0]:server.getChathost();
            }

            user.setConnectHost(url0);
            user.setConnectHost1(gameUrls[1]);
            user.setConnectHost2(gameUrls[2]);
        }

//        user.setTotalCount(userInfo.getTotalCount());
        user.setTotalCount(totalCount);
        user.setSessCode(userInfo.getSessCode());
        user.setCards(userInfo.getCards() + userInfo.getFreeCards());
        user.setPlayTableId(userInfo.getPlayingTableId());

        int payBindId = userInfo.getPayBindId();
        user.setPayBindId(payBindId);
        user.setRegBindId(userInfo.getRegBindId());
        user.setHasPay(user.getIsNewReg()==1?false:!UserDao.getInstance().isFirstPay(userInfo.getUserId(),1,9));

        user.setPayBindId(payBindId);
        if (payBindId <= 0 && StringUtils.isNotBlank(userInfo.getIdentity())) {
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_isBjdApp", SharedConstants.SWITCH_DEFAULT_OFF))) {
                user.setInviterPayBindId(BjdUtil.getPreBindAgency(userInfo.getIdentity()));
            }
        }
        String phoneNum = userInfo.getPhoneNum();
        //用于返回前端该账号是否绑定手机提示
        user.setPhoneNum(userInfo.getPhoneNum());
        return user;
    }

    public final static boolean checkVersion(String serverVersion, int serverIdx, String clientVersion, int clientIdx) {
        boolean result = false;
        int idxS1 = serverVersion.indexOf(".", serverIdx);
        int idxC1 = clientVersion.indexOf(".", clientIdx);
        if (idxS1 > 0 && idxC1 > 0) {
            int valS1 = NumberUtils.toInt(serverVersion.substring(serverIdx, idxS1), -1);
            int valC1 = NumberUtils.toInt(clientVersion.substring(clientIdx, idxC1), -1);
            if (valS1 >= 0 && valC1 >= 0) {
                if (valS1 > valC1) {
                    result = true;
                } else if (valS1 < valC1) {
                    result = false;
                } else {
                    return checkVersion(serverVersion, idxS1 + 1, clientVersion, idxC1 + 1);
                }
            }
        } else if (idxS1 > 0) {
            result = true;
        } else if (idxC1 > 0) {
            result = false;
        } else if (idxS1 == -1 && idxC1 == -1) {
            idxS1 = serverVersion.lastIndexOf(".");
            idxC1 = clientVersion.lastIndexOf(".");
            if (idxS1 > 0 && idxC1 > 0) {
                result = NumberUtils.toInt(serverVersion.substring(idxS1 + 1), -1) > NumberUtils.toInt(clientVersion.substring(idxC1 + 1), -1);
            } else if (NumberUtils.isDigits(serverVersion) && NumberUtils.isDigits(clientVersion)) {
                result = NumberUtils.toInt(serverVersion, -1) > NumberUtils.toInt(clientVersion, -1);
            }
        }
        return result;
    }

    private static int autoRemoveBind(RegInfo regInfo) {
        int res = UserDao.getInstance().removeBindInfo(regInfo);
        if (res == 1) {
            // 添加解绑记录
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", regInfo.getUserId());
            paramMap.put("agencyId", regInfo.getPayBindId());
            paramMap.put("createUserId", regInfo.getUserId());
            paramMap.put("createTime", TimeUtil.formatTime(new Date()));
            paramMap.put("bindType", 1);
            UserDao.getInstance().insertRBInfo(paramMap);
        }
        return res;
    }
}
