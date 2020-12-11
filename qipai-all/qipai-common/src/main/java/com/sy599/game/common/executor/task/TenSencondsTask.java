package com.sy599.game.common.executor.task;

import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.MonitorManger;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.MonitorMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;

import java.util.concurrent.atomic.AtomicLong;

public class TenSencondsTask implements Runnable {

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0);

    @Override
    public void run() {
        long current = ATOMIC_LONG.getAndAdd(1);

        PlayerManager.getInstance().saveConsumeDatas();

        if (current % 6 == 0) {
            try {
                // 跑马灯检查
                MarqueeManager.getInstance().check();
            } catch (Throwable t) {
                LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
            }
        }
        if (current % 30 == 0) {
            try {
                // 记录当前人数及内存使用情况
                MonitorMsg monitorMsg = MonitorManger.getInst().buildMonitorMsg();
                LogUtil.monitor_i(String.format("-----playerMapCount:%d,tableCount:%d,playerOnlineCount:%d,free Memory %d M,total Memory %d M, Max Memory %d M -----", monitorMsg.getCount(), monitorMsg.getTableCount(),
                        monitorMsg.getOnlineCount(), monitorMsg.getFreeMem(), monitorMsg.getTotalMem(), monitorMsg.getMaxMem()));
            } catch (Throwable t) {
                LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
            }
        }

        SystemCommonInfo resetTimeInfo = SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.mangguoResetJiFenTime);
        SystemCommonInfo hasResetTimeInfo = SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.isMangGuoJiFenReset);
        if (!resetTimeInfo.getContent().isEmpty() && !hasResetTimeInfo.getContent().isEmpty()) {
            try {
                long resetTime = TimeUtil.parseTimeInMillis(resetTimeInfo.getContent());// 重置时间
                if(System.currentTimeMillis() >= resetTime && hasResetTimeInfo.getContent().equals("0")) {// 芒果跑得快积分重置
                    UserDao.getInstance().resetJiFen();
                    hasResetTimeInfo.setContent("1");//重置开关  已重置
                    SystemCommonInfoDao.getInstance().update(hasResetTimeInfo);// 立即入库
                    SystemCommonInfoManager.getInstance().updateSystemCommonInfo(hasResetTimeInfo);
                    LogUtil.monitor_i("芒果跑得快重置积分成功！");
                }
            } catch (Throwable t) {
                LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
            }
        }

        long start = System.currentTimeMillis();
        try {
            TableManager.getInstance().checkAutoQuit();
        } catch (Exception e) {
            LogUtil.errorLog.error("checkAutoQuit|error|" + current + "|" + start + "|" + e.getMessage(), e);
        }
    }
}
