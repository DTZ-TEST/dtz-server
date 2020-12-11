package com.sy.sanguo.game.pdkuai.manager;

import com.sy.sanguo.common.executor.HourTask;
import com.sy.sanguo.common.executor.MinuteTask;
import com.sy.sanguo.common.util.TaskExecutor;
import com.sy.sanguo.game.dao.RoomDaoImpl;
import com.sy.sanguo.game.dao.SqlDao;
import com.sy.sanguo.game.pdkuai.db.dao.TableLogDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class TaskManager {
    private static TaskManager _inst = new TaskManager();

    private static final int delLimit = 10000;

    public static TaskManager getInstance() {
        return _inst;
    }

    public void init() {
        TaskExecutor.getInstance().submitSchTask(new MinuteTask(), 0, TimeUtil.MIN_IN_MINILLS);
        TaskExecutor.getInstance().submitSchTask(new HourTask(), 60 * 60 * 1000, 6 * 60 * 60 * 1000);

        // --------------------------------------------------
        // ------------每天凌晨05:00点执行任务-----------------
        // --------------------------------------------------
        Calendar c5 = Calendar.getInstance();
        c5.set(Calendar.MINUTE, 0);
        c5.set(Calendar.SECOND, 0);
        if (c5.get(Calendar.HOUR_OF_DAY) >= 5) {
            c5.add(Calendar.DAY_OF_YEAR, 1);
        }
        c5.set(Calendar.HOUR_OF_DAY, 5);

        Timer timer5 = new Timer();
        timer5.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                recoverRoom();

                clearGroupTable();

                clearGoldRoom();

                clearDataStatistics();

                clearUserPlayLog();

                clearGroupCreditLog();

                clearUserCardRecord();
            }
        }, c5.getTime(), 24 * 60 * 60 * 1000);


        // --------------------------------------------------
        // -----------------每天凌晨00:00执行任务--------------
        // --------------------------------------------------
        Calendar c0 = Calendar.getInstance();
        c0.set(Calendar.MINUTE, 0);
        c0.set(Calendar.SECOND, 1);
        if (c0.get(Calendar.HOUR_OF_DAY) >= 0) {
            c0.add(Calendar.DAY_OF_YEAR, 1);
        }
        c0.set(Calendar.HOUR_OF_DAY, 0);
        Timer timer0 = new Timer();
        timer0.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

            }
        }, c0.getTime(), 24 * 60 * 60 * 1000);


        // --------------------------------------------------
        // -----------------每天凌晨04:00执行任务--------------
        // --------------------------------------------------
        Calendar c4 = Calendar.getInstance();
        c4.set(Calendar.MINUTE, 0);
        c4.set(Calendar.SECOND, 1);
        if (c4.get(Calendar.HOUR_OF_DAY) >= 0) {
            c4.add(Calendar.DAY_OF_YEAR, 1);
        }
        c4.set(Calendar.HOUR_OF_DAY, 4);
        Timer timer4 = new Timer();
        timer4.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processBjdNewerActivity();
            }
        }, c4.getTime(), 24 * 60 * 60 * 1000);

    }

    private void recoverRoom() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);

        String currentDate = sdf.format(cal.getTime());
        try {
            long startTime = System.currentTimeMillis();
            int delCount = RoomDaoImpl.getInstance().recoverRoom(currentDate);
            LogUtil.i("clearData|TaskManager|recoverRoom|" + (System.currentTimeMillis() - startTime) + "|" + delCount + "|" + currentDate);
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    private void clearGroupTable() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -14);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String currentDate = sdf.format(cal.getTime());

        String delName = "";
        String maxKeyIdSql = "";
        long maxKeyId = 0;
        String delSql = "";

        try {
            // ----------------------------- t_table_record ----------------------------------
            delName = "clearTableRecord";
            maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_table_record where createdTime < '" + currentDate + "'";
            maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
            if (maxKeyId > 0) {
                delSql = "delete from t_table_record where keyId <= " + maxKeyId;
                deleteDataForLogin(delName, delSql, delLimit);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        try {
            delName = "clearTableUser";
            maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_table_user where createdTime < '" + currentDate + "'";
            maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
            if (maxKeyId > 0) {
                delSql = "delete from t_table_user where keyId <= " + maxKeyId;
                deleteDataForLogin(delName, delSql, delLimit);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        try {
            // ----------------------------- t_group_table ----------------------------------
            delName = "clearGroupTable1";
            maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_group_table where overTime < '" + currentDate + "'";
            maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
            if (maxKeyId > 0) {
                delSql = "delete from t_group_table where keyId <= " + maxKeyId;
                deleteDataForLogin(delName, delSql, delLimit);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        try {
            //清理3天前的未开局解散房间
            cal = Calendar.getInstance();
//                cal.add(Calendar.DAY_OF_YEAR, -3);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            currentDate = sdf.format(cal.getTime());
            delName = "clearGroupTable2";
            delSql = "delete from t_group_table where overTime <= '" + currentDate + "' and currentState = '3' ";
            deleteDataForLogin(delName, delSql, delLimit);
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    private void clearGoldRoom() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String currentDate = sdf.format(cal.getTime());
        try {
            if (SqlDao.getInstance().checkExistsGoldRoomTable() > 0) {

                String delName = "";
                String maxKeyIdSql = "";
                long maxKeyId = 0;
                String delSql = "";

                delName = "clearGoldRoom1";
                maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_gold_room where createdTime < '" + currentDate + "'";
                maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
                if (maxKeyId > 0) {
                    delSql = " delete from t_gold_room where keyId <= " + maxKeyId;
                    deleteDataForLogin(delName, delSql, delLimit);
                }

                delName = "clearGoldRoomUser";
                maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_gold_room_user where createdTime < '" + currentDate + "'";
                maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
                if (maxKeyId > 0) {
                    delSql = " delete from t_gold_room_user where keyId <= " + maxKeyId;
                    deleteDataForLogin(delName, delSql, delLimit);
                }
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    private void clearDataStatistics() {
        SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
        SimpleDateFormat ymd = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat ymdh = new SimpleDateFormat("yyyyMMddHH");
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_YEAR, -40);
        String startDate1 = ym.format(cal.getTime());
        String startDate2 = ymd.format(cal.getTime());
        String startDate3 = ymdh.format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, -180);
        String endDate1 = ym.format(cal.getTime());
        String endDate2 = ymd.format(cal.getTime());
        String endDate3 = ymdh.format(cal.getTime());
        try {
            if (SqlDao.getInstance().checkExistsDataStatisticsTable() > 0) {

                String delSql;
                String delName = "clearDataStatistics1";
                delSql = "delete from t_data_statistics where dataDate <= " + startDate1 + " and dataDate >= " + endDate1;
                deleteDataForLogin(delName, delSql, delLimit);

                delName = "clearDataStatistics2";
                delSql = "delete from t_data_statistics where dataDate <= " + startDate2 + " and dataDate >= " + endDate2;
                deleteDataForLogin(delName, delSql, delLimit);

                delName = "clearDataStatistics3";
                delSql = "delete from t_data_statistics where dataDate <= " + startDate3 + " and dataDate >= " + endDate3;
                deleteDataForLogin(delName, delSql, delLimit);

            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 白金岛有效局数活动，新人
     */
    private void processBjdNewerActivity() {
        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_bjdNewerActivity", "0"))) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -30);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String startDate1 = sdf.format(cal.getTime());
                if (SqlDao.getInstance().checkExistsDataStatisticsTable() > 0) {
                    long startTime = System.currentTimeMillis();
                    String sql1 = " DELETE FROM bjd_group_newer_bind " +
                            " WHERE " +
                            " userId IN ( " +
                            "   SELECT " +
                            "   d.userId " +
                            "   FROM " +
                            "       ( " +
                            "        SELECT " +
                            "        gu.userId " +
                            "        FROM " +
                            "        t_group_user gu " +
                            "        LEFT JOIN ( SELECT * FROM t_table_user WHERE createdTime > '" + startDate1 + "' ) tu ON tu.userId = gu.userId " +
                            "        WHERE " +
                            "           tu.keyId IS NULL " +
                            "       ) AS d " +
                            " ); ";
                    try {
                        int delCount = SqlDao.getInstance().delete(sql1);
                        LogUtil.i("clearData|TaskManager|2|processBjdNewerActivity|" + delCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|sql=" + sql1);
                    } catch (Exception e) {
                        LogUtil.e("Exception:" + e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                LogUtil.e("Exception:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 清理信用分记录，15天
     */
    private void clearGroupCreditLog() {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -14);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            String clearDate = sdf.format(cal.getTime());

            String maxKeyIdSql = " select COALESCE( MAX(keyId),0 ) from t_group_credit_log where createdTime < '" + clearDate + "'";
            long maxKeyId = loadMaxKeyIdForLogin(maxKeyIdSql);
            if (maxKeyId > 0) {
                String delName = "clearGroupCreditLog";
                String delSql = "delete from t_group_credit_log where keyId <= " + maxKeyId;
                deleteDataForLogin(delName, delSql, delLimit);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 清理login库的数据
     *
     * @param delName  用于日志区分清理功能
     * @param sql      执行的sql
     * @param delLimit 单次最大清理数据量
     */
    private void deleteDataForLogin(String delName, String sql, int delLimit) {
        long totalDelCount = 0;
        long startTime = System.currentTimeMillis();
        try {
            sql = sql + " limit " + delLimit;
            startTime = System.currentTimeMillis();
            int delCount = SqlDao.getInstance().delete(sql);
            LogUtil.i("clearData|TaskManager|1|" + delName + "|" + delCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime);

            totalDelCount = delCount;
            while (delCount == delLimit) {
                Thread.sleep(1000);
                startTime = System.currentTimeMillis();
                delCount = SqlDao.getInstance().delete(sql);
                LogUtil.i("clearData|TaskManager|1|" + delName + "|" + delCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime);
                totalDelCount += delCount;
            }
            LogUtil.i("clearData|TaskManager|2|" + delName + "|" + totalDelCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|" + sql);
        } catch (Exception e) {
            LogUtil.i("clearData|TaskManager|2|" + delName + "|" + totalDelCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|" + sql);
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 清理回放日志，5天
     * user_play_log表在1kz库
     */
    private void clearUserPlayLog() {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -4);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            String clearDate = sdf.format(cal.getTime());

            String delName = "clearUserPlayLog";
            String delSql = "delete from user_playlog where time < " + "'" + clearDate + "'";
            deleteDataFor1kz(delName, delSql, delLimit);
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 玩家房卡消耗/获得日志30天过期清理
     * user_card_record表在1kz库
     */
    private void clearUserCardRecord() {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -30);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            String clearDate = sdf.format(cal.getTime());

            String delName = "clearUserCardRecord";
            String delSql = "delete from user_card_record where createTime < " + "'" + clearDate + "'";
            deleteDataFor1kz(delName, delSql, delLimit);
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 清理1kz库的数据
     *
     * @param delName  用于日志区分清理功能
     * @param sql      执行的sql
     * @param delLimit 单次最大清理数据量
     */
    private void deleteDataFor1kz(String delName, String sql, int delLimit) {
        long totalDelCount = 0;
        long startTime = System.currentTimeMillis();
        try {
            sql = sql + " limit " + delLimit;
            startTime = System.currentTimeMillis();
            int delCount = TableLogDao.getInstance().deleteSql(sql);
            LogUtil.i("clearData|TaskManager|1|" + delName + "|" + delCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime);

            totalDelCount = delCount;
            while (delCount == delLimit) {
                Thread.sleep(1000);
                startTime = System.currentTimeMillis();
                delCount = TableLogDao.getInstance().deleteSql(sql);
                LogUtil.i("clearData|TaskManager|1|" + delName + "|" + delCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime);
                totalDelCount += delCount;
            }
            LogUtil.i("clearData|TaskManager|2|" + delName + "|" + totalDelCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|" + sql);
        } catch (Exception e) {
            LogUtil.i("clearData|TaskManager|2|" + delName + "|" + totalDelCount + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|" + sql);
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 刷新每日下分经验限制
     */
    private void refreshGroupLevelData() {
        try {
            try {
                long startTime = System.currentTimeMillis();
                String sql = " update t_group set creditExpToday = 0 , refreshTimeDaily = now() ;";
                int count = SqlDao.getInstance().update(sql);
                LogUtil.i("clearData|TaskManager|1|refreshGroupLevelData|" + count + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|sql=" + sql);

                startTime = System.currentTimeMillis();
                sql = " update t_group_user set creditExpToday = 0 , refreshTimeDaily = now() ;";
                count = SqlDao.getInstance().update(sql);
                LogUtil.i("clearData|TaskManager|12|refreshGroupLevelData|" + count + "|" + (System.currentTimeMillis() - startTime) + "|" + startTime + "|sql=" + sql);
            } catch (Exception e) {
                LogUtil.e("Exception:" + e.getMessage(), e);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * login库中表数据满足sql条件的最大keyId
     *
     * @param maxKeyIdSql
     * @return
     */
    private long loadMaxKeyIdForLogin(String maxKeyIdSql) {
        return SqlDao.getInstance().loadMaxKeyId(maxKeyIdSql);
    }


}
