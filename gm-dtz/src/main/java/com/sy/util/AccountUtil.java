package com.sy.util;

import com.sy.entity.pojo.RoomCard;
import com.sy.mainland.util.db.annotation.SelectDataBase;
import com.sy.manager.CommonManager;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by pc on 2017/4/12.
 */
public class AccountUtil {

    public static final int countAgencyPay(CommonManager commonManager, RoomCard roomCard, String startDate, String endDate) throws Exception {
        return countAgencyPay(commonManager, roomCard.getAgencyId(),startDate,endDate);
    }

    public static final int countAgencyPay(CommonManager commonManager, Integer agencyId, String startDate, String endDate) throws Exception {
        if (!startDate.contains(" ")) {
            startDate += " 00:00:00";
        }
        if (!endDate.contains(" ")) {
            endDate += " 23:59:59";
        }
        return commonManager.count(SelectDataBase.DataBase.DB_1,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                new Object[]{agencyId.toString(), startDate, endDate})+commonManager.count(SelectDataBase.DataBase.DB_3,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                new Object[]{agencyId.toString(), startDate, endDate});
    }

    public static final int countSubAgencyPay(CommonManager commonManager, final RoomCard roomCard, String startDate, String endDate, int current, final int maxLevel,boolean flag) throws Exception {
        return countSubAgencyPay(commonManager, roomCard.getUserId(), startDate, endDate, current,maxLevel,flag);
    }

    public static final int countSubAgencyPay(CommonManager commonManager, Integer userId, String startDate, String endDate, int current, final int maxLevel,boolean flag) throws Exception {
    	if (!startDate.contains(" ")) {
            startDate += " 00:00:00";
        }
        if (!endDate.contains(" ")) {
            endDate += " 23:59:59";
        }

        int total = 0;
        if (current < maxLevel) {
            current++;

            RoomCard roomCard0 = new RoomCard();
            roomCard0.setParentId(userId);
            List<RoomCard> list = commonManager.findList(roomCard0);

            if (list != null && list.size() > 0) {
                for (RoomCard rc : list) {
                	 /*int tmp = commonManager.count(SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                             new Object[]{rc.getAgencyId().toString(), startDate, endDate});*/
                     int tmp;
                     if (flag){
                         tmp=(commonManager.count(SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                                 new Object[]{(rc.getAgencyId().toString()+"Z"), startDate, endDate}))/10;
                     }else{
                         tmp=(commonManager.count(SelectDataBase.DataBase.DB_1,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                                 new Object[]{(rc.getAgencyId().toString()), startDate, endDate}))+(commonManager.count(SelectDataBase.DataBase.DB_3,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                                 new Object[]{(rc.getAgencyId().toString()), startDate, endDate}));
                     }

                    total += tmp;
                    total += countSubAgencyPay(commonManager, rc, startDate, endDate, current, maxLevel,flag);
                }
            }
        }
        return total;
    }
    public static final int countSubAgencyPay(CommonManager commonManager, Integer userId, String startDate, String endDate, int current, final int maxLevel) throws Exception {
        if (!startDate.contains(" ")) {
            startDate += " 00:00:00";
        }
        if (!endDate.contains(" ")) {
            endDate += " 23:59:59";
        }

        int total = 0;
        if (current < maxLevel) {
            current++;

            RoomCard roomCard0 = new RoomCard();
            roomCard0.setParentId(userId);
            List<RoomCard> list = commonManager.findList(roomCard0);

            if (list != null && list.size() > 0) {
                for (RoomCard rc : list) {
                    int tmp = commonManager.count(SelectDataBase.DataBase.DB_1,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                            new Object[]{rc.getAgencyId().toString(), startDate, endDate});
                    tmp += commonManager.count(SelectDataBase.DataBase.DB_3,SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                            new Object[]{rc.getAgencyId().toString(), startDate, endDate});
                    tmp += (commonManager.count(SqlHelperUtil.getString("range_sum", Constant.ORDER_INFO_FILE),
                            new Object[]{rc.getAgencyId()+"Z", startDate, endDate})/10);

                    total += tmp;
                    total += countSubAgencyPay(commonManager, rc, startDate, endDate, current, maxLevel);
                }
            }
        }
        return total;
    }
    
    public static final int countSubAgencyPay(CommonManager commonManager, final RoomCard roomCard, String startDate, String endDate, int current, final int maxLevel) throws Exception {
        return countSubAgencyPay(commonManager, roomCard.getUserId(), startDate, endDate, current,maxLevel);
    }
    
    public static final String loadTempAgencyId(int length) {
        String str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder stringBuilder = new StringBuilder(length);
        SecureRandom random = new SecureRandom();
        int first = random.nextInt(36);
        stringBuilder.append(str.charAt(first <= 10 ? (first + random.nextInt(26)) : first));
        for (int i = 0, len = length - 1; i < len; i++) {
            stringBuilder.append(str.charAt(random.nextInt(36)));
        }
        return stringBuilder.toString();
    }

    /**
     * @param request
     * @param verCode
     */
    public static final void putVerCode(HttpServletRequest request, String verCode) {
        SessionUtil.setSessionValue(request, Constant.VERCODE_NAME, verCode);
    }
}
