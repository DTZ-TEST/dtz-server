package com.sy.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

public final class Constant {
    /**
     * 工程核心配置文件
     */
    public static final String CORE_FILE = "core.properties";

    /**
     * 语言文件
     */
    public static final String LANGUAGE_DIRECTORY = "internationalization" + File.separator;


    /**
     * H5微信支付配置文件
     */
    public static final String H5PAY_FILE = "h5Pay.properties";

    /**
     * 微信红包领取配置
     */
    public static final String WEIXIN_RED_BAG_FILE = "redbag.properties";

    /**
     * sql文件
     */
    public static final String SQL_DIRECTORY = "sql" + File.separator;

    /**
     * 验证码字段名
     */
    public static final String VERCODE_NAME = "vercode";

    public static final String SYSTEM_USER_FILE = "system_user.properties";
    public static final String ROOMCARD_FILE= "roomcard.properties";
    public static final String ORDER_INFO_FILE= "order_info.properties";
    public static final String USER_INFO_FILE= "user_info.properties";
    public static final String ROOMCARD_ORDER_FILE= "roomcard_order.properties";
    public static final String ROOMCARD_RECORD_FILE= "roomcard_record.properties";
    public static final String CARDS_STATISTICS_FILE= "cards_statistics.properties";
    public static final String STATISTICS_PF_FILE= "statistics_pf.properties";
    public static final String AGENCY_INCOME_FILE= "agency_income.properties";
    public static final String RED_BAG_FILE= "redbag_info.properties";
    public static Map<String, HttpServletRequest> requestMap = new ConcurrentHashMap<>();
    public static Map<Integer, String> restTime = new HashMap<>();
}
