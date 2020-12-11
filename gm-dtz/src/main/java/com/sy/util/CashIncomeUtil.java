package com.sy.util;

import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CashIncomeUtil {

    public static Map<Integer, Date> popTimeMap = new HashMap<>();
    public static Map<Integer, Integer> popCountMap = new HashMap<>();
    public static final String msg_OnLogin = "因微信政策调整，《每日返佣提现》无法下发相应推广奖励。故该功能将于6月5日暂停，现有未提现的部分请联系客服进行折钻奖励。";
    public static final String msg_cashIncome = "因微信政策调整，《每日返佣提现》无法下发相应推广奖励。故该功能将于6月5日暂停，现有未提现的部分请联系客服进行折钻奖励。";
    public static Date startDate;
    public static Date endDate;

    static {
        try {
            startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-03-06 00:00:00");
            endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-03-08 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static boolean isOpen(Date date) {
        if (date.before(startDate) || date.after(endDate)) {
            return false;
        }
        return true;
    }

    public static String popMsg(Integer userId) {
        String res = "";
        try {
            Date now = new Date();
            if (!isOpen(now)) {
                return res;
            }
            Date date = popTimeMap.get(userId);
            if (date == null) {
                res = msg_OnLogin;
                popTimeMap.put(userId, now);
                popCountMap.put(userId, 1);
            } else {
                if (!DateUtils.isSameDay(new Date(), date)) {
                    res = msg_OnLogin;
                    popTimeMap.put(userId, now);
                    popCountMap.put(userId, 1);
                } else {
                    Integer count = popCountMap.get(userId);
                    if (count != null && count < 3) {
                        res = msg_OnLogin;
                        popCountMap.put(userId, (count + 1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean canCashIncome(Date date) {
        if (!isOpen(date)) {
            return true;
        }
        return false;
    }
}
