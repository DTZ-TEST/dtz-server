package com.sy599.game.common.executor.task;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.common.datasource.DruidDataSourceFactory;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.ServerDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.netty.NettyUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class ServerConfigTask implements Runnable {
    @Override
    public void run() {

        try {
            int count = WebSocketManager.webSocketMap.size();
            int ret = ServerDao.getInstance().updateServerOnlineCount(GameServerConfig.SERVER_ID,count);

            DataStatisticsDao.getInstance().saveOrUpdateOnlineData(CommonUtil.dateTimeToString("yyyyMMddHHmm"),"all"+GameServerConfig.SERVER_ID,count);

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)TaskExecutor.EXECUTOR_SERVICE;
            LogUtil.msgLog.info("ServerConfigTask:serverId={}, count={}, table count={}, user count={}, channel count={}, update result={}, thread pool msg:active={},completed={},poolSize={},taskCount={},queueSize={}"
            ,GameServerConfig.SERVER_ID,count,TableManager.getInstance().getTableCount()
            ,PlayerManager.getInstance().getPlayerCount(),NettyUtil.channelUserMap.size(),ret
            ,threadPoolExecutor.getActiveCount(),threadPoolExecutor.getCompletedTaskCount(),threadPoolExecutor.getPoolSize(),threadPoolExecutor.getTaskCount(),threadPoolExecutor.getQueue().size());

            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("db_monitor"))){
                DruidDataSourceFactory.msg();
            }

            ServerManager.init();
        }catch (Throwable t){
            LogUtil.errorLog.error("Throwable:"+t.getMessage(),t);
        }
    }
}
