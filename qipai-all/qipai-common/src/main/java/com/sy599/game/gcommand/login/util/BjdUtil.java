package com.sy599.game.gcommand.login.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BjdUtil {
    public static final String url_checkBind = "https://wx.52bjd.com/agent/player/checkBind";
    public static final String url_bind = "https://wx.52bjd.com/agent/player/bind";
    public static final String plat = "mjqz";
    public static final String sign_key = "0NUs3u0qpsfrB4k9";
    public static final int timeout_second = 3;

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
        sb.append("&key=").append(sign_key);
        return sign.equalsIgnoreCase(MD5Util.getMD5String(sb.toString()));
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
        String sign = MD5Util.getMD5String(account + time);
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("sign", sign);
            String postRes = HttpUtil.getUrlReturnValue(url_checkBind, "UTF-8", "POST", map, timeout_second);
            LogUtil.msgLog.info("getBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
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
            LogUtil.errorLog.error("getBindAgency|error|" + account + "|" + time + "|" + sign, e);
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
        String sign = MD5Util.getMD5String(account + "" + agencyId + "" + time);
        String defaltRes = "绑定" + agencyId + "的代理商失败，请联系管理员!";
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("invite_code", String.valueOf(agencyId));
            map.put("wx_plat", plat);
            map.put("time", time);
            map.put("sign", sign);
            String postRes = HttpUtil.getUrlReturnValue(url_bind, "UTF-8", "POST", map, timeout_second);
            LogUtil.msgLog.info("bindAgencyId|" + account + "|" + time + "|" + sign + "|" + postRes);
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
            LogUtil.errorLog.error("bindAgencyId|error|" + account + "|" + time + "|" + sign, e);
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
        String sign = MD5Util.getMD5String(account + time);
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("sign", sign);
            String postRes = HttpUtil.getUrlReturnValue(url_checkBind, "UTF-8", "POST", map, timeout_second);
            LogUtil.msgLog.info("getPreBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
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
            LogUtil.errorLog.error("getPreBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }
}
