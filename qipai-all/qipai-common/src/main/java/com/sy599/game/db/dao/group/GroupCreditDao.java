package com.sy599.game.db.dao.group;

import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class GroupCreditDao extends BaseDao {

    public static final GroupCreditDao INST = new GroupCreditDao();

    public static int getConfigCreditValue(String groupId, long preUserId, long userId, long configId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", groupId);
            map.put("preUserId", preUserId);
            map.put("userId", userId);
            map.put("configId", configId);
            Integer res = (Integer) INST.getSqlLoginClient().queryForObject("groupCredit.getConfigCreditValue", map);
            return res == null ? 0 : res;
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public static Integer loadConfigCreditValue(String groupId, long preUserId, long userId, long configId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", groupId);
            map.put("preUserId", preUserId);
            map.put("userId", userId);
            map.put("configId", configId);
            return (Integer) INST.getSqlLoginClient().queryForObject("groupCredit.getConfigCreditValue", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }


}
