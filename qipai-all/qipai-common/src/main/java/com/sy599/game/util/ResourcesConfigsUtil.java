package com.sy599.game.util;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy599.game.db.bean.ResourcesConfigs;
import com.sy599.game.db.dao.ResourcesConfigsDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源配置工具类
 */
public final class ResourcesConfigsUtil {

    private static final Map<String, Map<String, Integer>> INT_VALUE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> STRING_VALUE_MAP = new ConcurrentHashMap<>();

    public static final String TYPE_SERVER_CONFIG = "ServerConfig";


    /** 亲友圈房间限制：未开局、正在进行**/
    private static final String KEY_GROUP_TABLE_COUNT_LIMIT = "group_table_count_limit";

    /**
     * 初始化资源配置
     */
    public final static void initResourcesConfigs() {
        try {
            List<ResourcesConfigs> list = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"t_resources_configs") ? ResourcesConfigsDao.getInstance().loadAllConfigs() : null;
            if (list != null) {
                boolean reloadGameConfig = false;
                for (ResourcesConfigs rc : list) {
                    if (NumberUtils.isDigits(rc.getMsgValue())) {
                        Map<String, Integer> map = INT_VALUE_MAP.get(rc.getMsgType());
                        try {
                            if (map == null) {
                                map = new ConcurrentHashMap<>();
                                map.put(rc.getMsgKey(), Integer.valueOf(rc.getMsgValue()));
                                INT_VALUE_MAP.put(rc.getMsgType(), map);
                            } else {
                                map.put(rc.getMsgKey(), Integer.valueOf(rc.getMsgValue()));
                            }
                        }catch (Exception e){
                            LogUtil.msgLog.warn("msgType={},msgValue={},convert integer fail : Exception:{}",rc.getMsgType(),rc.getMsgValue(),e.getMessage());
                        }
                    }

                    String pre;
                    Map<String, String> map = STRING_VALUE_MAP.get(rc.getMsgType());
                    if (map == null) {
                        map = new ConcurrentHashMap<>();
                        pre = map.put(rc.getMsgKey(), rc.getMsgValue());
                        STRING_VALUE_MAP.put(rc.getMsgType(), map);
                    } else {
                        pre = map.put(rc.getMsgKey(), rc.getMsgValue());
                    }

                    if (pre == null) {
                        LogUtil.msgLog.info("initResourcesConfigs:type={},key={},value={}", rc.getMsgType(), rc.getMsgKey(), rc.getMsgValue());
                    } else if (!pre.equals(rc.getMsgValue())) {
                        LogUtil.msgLog.info("reloadResourcesConfigs:type={},key={},value={}", rc.getMsgType(), rc.getMsgKey(), rc.getMsgValue());
                    }

                    if ("GameOrGoldConfig".equals(rc.getMsgType())){
                        reloadGameConfig = true;
                    }
                }

                if (reloadGameConfig){
                    GameConfigUtil.loadGameConfigsFromDb();
                }
            }

            if (list == null || list.size() == 0) {
                LogUtil.msgLog.info("initResourcesConfigs:size=0");
            } else {
                LogUtil.msgLog.info("initResourcesConfigs:size=" + list.size());
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 支付配置
     *
     * @param key
     * @return
     */
    public final static Integer loadPayPropertyValue(String key) {
        return loadIntegerValue("PayConfig", key);
    }

    /**
     * server配置
     *
     * @param key
     * @return
     */
    public final static String loadServerPropertyValue(String key) {
        return loadServerPropertyValue(key, null);
    }

    /**
     * server配置
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final static String loadServerPropertyValue(String key, String defaultValue) {
        String val = loadStringValue("ServerConfig", key);
        if (val == null) {
            val = PropertiesCacheUtil.getValue(key, "config" + File.separator + "server.properties");
            return val.length() > 0 ? val : defaultValue;
        } else {
            return val;
        }
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @return
     */
    public final static Integer loadIntegerValue(String type, String key) {
        Map<String, Integer> map = INT_VALUE_MAP.get(type);
        return map == null ? null : map.get(key);
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @param defaultValue
     * @return
     */
    public final static Integer loadIntegerValue(String type, String key,int defaultValue) {
        Map<String, Integer> map = INT_VALUE_MAP.get(type);
        Integer value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @return
     */
    public final static Map<String, Integer> loadIntegerValues(String type) {
        return INT_VALUE_MAP.get(type);
    }

    /**
     * 获取字符串资源配置（包括整型资源）
     *
     * @param type
     * @param key
     * @return
     */
    public final static String loadStringValue(String type, String key) {
        Map<String, String> map = STRING_VALUE_MAP.get(type);
        return map == null ? null : map.get(key);
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @param defaultValue
     * @return
     */
    public final static String loadStringValue(String type, String key,String defaultValue) {
        Map<String, String> map = STRING_VALUE_MAP.get(type);
        String value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * 获取字符串资源配置（包括整型资源）
     *
     * @param type
     * @return
     */
    public final static Map<String, String> loadStringValues(String type) {
        return STRING_VALUE_MAP.get(type);
    }

    public final static Integer loadServerConfigIntegerValue(String key, int defaultValue) {
        Map<String, Integer> map = INT_VALUE_MAP.get(TYPE_SERVER_CONFIG);
        Integer value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    public static int getGroupTableCountLimit() {
        return ResourcesConfigsUtil.loadServerConfigIntegerValue(KEY_GROUP_TABLE_COUNT_LIMIT, 300);
    }

}
