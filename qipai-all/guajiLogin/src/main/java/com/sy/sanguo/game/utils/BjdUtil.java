package com.sy.sanguo.game.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.RegInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BjdUtil {
    public static final String url_base = "https://wx.52bjd.com";
    public static final String url_checkBind = url_base + "/agent/player/checkBind";
    public static final String url_bind = url_base + "/agent/player/bind";
    public static final String url_transfer_group = url_base + "/agent/player/changeGroupMaster/wx_plat/mjqz";
    public static final String plat = "mjqz";
    public static final String sign_key = "0NUs3u0qpsfrB4k9";
    public static final String sign_key_new = "szmUQrkBRv54cSOj";
    public static long sign_key_time_out = 0L;

    public static void init() throws Exception {
        sign_key_time_out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-01-21 11:00:00").getTime();
    }

    public static boolean useNewSignKey() {
        return System.currentTimeMillis() > sign_key_time_out;
    }

    /**
     * 签名验证
     *
     * @param params
     * @return
     */
    public static boolean checkSign(Map<String, String> params) {
        String sign = params.remove("sign");
        String time = params.get("time");
        if (StringUtils.isBlank(sign) || !NumberUtils.isDigits(time)) {
            return false;
        }
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        String s1 = sb.toString() + "&key=" + sign_key;
        String s2 = sb.toString() + "&key=" + sign_key_new;
        if (useNewSignKey()) {
            return sign.equalsIgnoreCase(MD5Util.getStringMD5(s2, "utf-8"));
        } else {
            return sign.equalsIgnoreCase(MD5Util.getStringMD5(s1, "utf-8"))
                    || sign.equalsIgnoreCase(MD5Util.getStringMD5(s2, "utf-8"));
        }
    }

    /**
     * 获取用户绑定的代理邀请码
     *
     * @return
     */
    public static int getBindAgency(String unionId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account = unionId;
        String sign = MD5Util.getStringMD5(account + time, "utf-8");
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("sign", sign);
            HttpUtil httpUtil = new HttpUtil(url_checkBind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("getBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return 0;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return 0;
            }
            if (obj.getIntValue("code") != 1) {
                // 未绑定
                return 0;
            }
            JSONObject data = obj.getJSONObject("data");
            if (data == null) {
                return 0;
            }
            String parentId = data.getString("parent_id");
            if (StringUtils.isBlank(parentId) || "null".equals(parentId)) {
                return 0;
            }
            return Integer.valueOf(parentId);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("getBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }

    /**
     * 为用户绑定代理邀请码
     *
     * @param user
     * @param agencyId
     * @return
     */
    public static String bindAgencyId(RegInfo user, int agencyId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account = user.getIdentity();
        String sign = MD5Util.getStringMD5(account + "" + agencyId + "" + time, "utf-8");
        String defaltRes = "绑定" + agencyId + "的代理商失败，请联系管理员!";
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("invite_code", String.valueOf(agencyId));
            map.put("wx_plat", plat);
            map.put("time", time);
            map.put("sign", sign);
            HttpUtil httpUtil = new HttpUtil(url_bind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("bindAgencyId|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                // 绑定失败
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("bindAgencyId|error|" + account + "|" + time + "|" + sign, e);
        }
        return defaltRes;
    }

    /**
     * 获取用户预绑定的代理邀请码
     *
     * @return
     */
    public static int getPreBindAgency(String unionId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account = unionId;
        String sign = MD5Util.getStringMD5(account + time, "utf-8");
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("sign", sign);
            HttpUtil httpUtil = new HttpUtil(url_checkBind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("getPreBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return 0;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return 0;
            }
            if (obj.getIntValue("code") != 2) {
                // 未绑定
                return 0;
            }
            JSONObject data = obj.getJSONObject("data");
            if (data == null) {
                return 0;
            }
            String parentId = data.getString("parent_id");
            if (StringUtils.isBlank(parentId) || "null".equals(parentId)) {
                return 0;
            }
            return Integer.valueOf(parentId);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("getPreBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }


    /**
     * 转移俱乐部
     *
     * @return
     */
    public static String transferGroup(long fromUserId, long toUserId, long groupId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String sign = MD5Util.getStringMD5(fromUserId + "" + groupId + "" + toUserId + "" + time, "utf-8");
        String defaltRes = "转移俱乐部" + groupId + "给用户" + toUserId + "失败，请联系管理员!";
        try {
            map.put("from_char_id", String.valueOf(fromUserId));
            map.put("to_char_id", String.valueOf(toUserId));
            map.put("group_id", String.valueOf(groupId));
            map.put("time", time);
            map.put("sign", sign);
            HttpUtil httpUtil = new HttpUtil(url_transfer_group);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("transferGroup|" + fromUserId + "|" + toUserId + "|" + groupId + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                // 转移失败
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("transferGroup|error|" + fromUserId + "|" + toUserId + "|" + groupId + "|" + time + "|" + sign, e);
        }
        return defaltRes;
    }
}
