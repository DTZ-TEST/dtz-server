package com.sy.redpack;

import com.sy.util.Constant;
import com.sy.util.PropUtil;

/**
 * 微信红包相关参数配置
 */
public class WeixinRedbagConfig {

    /*
     * 微信支付分配的商户号
     */
    public static String getMchId() {
        return PropUtil.getString("MCH_ID", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /*
     * 商户appid
     */
    public static String getAppId() {
        return PropUtil.getString("APP_ID", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /*
     * API密钥 用于签名
     */
    public static String getKey() {
        return PropUtil.getString("KEY", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /**
     * 商户密钥  用于调用微信接口
     */
    public static String getAppsecret() {
        return PropUtil.getString("appsecret", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /*
     * 微信红包的API地址
     */
    public static String getApiUrl() {
        return PropUtil.getString("API_URL", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /*
     * 微信p12文件存放路径
     */
    public static String getP12KeyPath() {
        return PropUtil.getString("p12KeyPath", "", Constant.WEIXIN_RED_BAG_FILE);
    }

    /**
     * 参与游戏名
     */
    public static String getGameName() {return PropUtil.getString("gameName", "", Constant.WEIXIN_RED_BAG_FILE);}

    /**
     * 红包祝福语
     */
    public static String getWishContent() {return PropUtil.getString("wishContent", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 活动名
     */
    public static String getActivityName() {return PropUtil.getString("activityName", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 公众号名
     */
    public static String getAppName() {return PropUtil.getString("appName", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 现金红包领取开始时间
     */
    public static String getStartTime() {return PropUtil.getString("startTime", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 现金红包领取结束时间
     */
    public static String getEndTime() {return PropUtil.getString("endTime", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 现金红包领取口令
     */
    public static String getToken() {return PropUtil.getString("token", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 现金红包每日系统最高领取额
     */
    public static String getRedbagMaxNum() {return PropUtil.getString("redbagMaxNum", "", Constant.WEIXIN_RED_BAG_FILE); }

    /**
     * 小甘麻将需要达成的领红包局数
     */
    public static int getXgmjBureau() { return Integer.parseInt(PropUtil.getString("xgmjBureau", "100", Constant.WEIXIN_RED_BAG_FILE)); }
}