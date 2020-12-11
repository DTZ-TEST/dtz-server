package com.sy599.game;

import com.sy599.game.common.asyn.AsynExecutor;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.datasource.DruidDataSourceFactory;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.gcommand.login.util.LoginDataUtil;
import com.sy599.game.manager.DBManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MessageTransmits;
import com.sy599.game.util.helper.ResourceWatchHandler;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.netty.WebSocketServer;
import com.sy599.game.websocket.netty.WebSocketServerHandler;

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 异常停服处理，jvm不能保证一定调用 kill -2 保证执行
 *
 * @author taohuiliang
 * @version v1.0
 * @date 2013-3-27
 */
public class ShutdownManager extends Thread implements Thread.UncaughtExceptionHandler {

    private static volatile ShutdownManager _inst;
    private static volatile WebSocketServer server;
    private static volatile Closeable[] closeables;

    private ShutdownManager(WebSocketServer server, Closeable... closeables) {
        this.server = server;
        this.closeables = closeables;
    }

    public static ShutdownManager getInstance(WebSocketServer server, Closeable... closeables) {
        synchronized (ShutdownManager.class) {
            if (_inst == null) {
                _inst = new ShutdownManager(server, closeables);
            }
        }
        return _inst;
    }

    public void run() {
        shutdownNow();
    }

    public void shutdownNow() {
        WebSocketServerHandler.isOpen = false;

        String tempStr = ("===exit===" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ",current online=" + WebSocketManager.webSocketMap.size());
        LogUtil.msgLog.info(tempStr);

        long start = System.currentTimeMillis();
        LogUtil.msgLog.info("shutdownNow|start|" + tempStr);

        if (server != null) {
            try {
                server.distory();
            } catch (Throwable t) {
                LogUtil.errorLog.error("shutdownNow|error|stopNetty|",t);
            }
        }
        LogUtil.msgLog.info("shutdownNow|stopNetty|end|" + (System.currentTimeMillis() - start));


        start = System.currentTimeMillis();
        SystemCommonInfoDao.getInstance().selectOne();
        SystemCommonInfoDao.getInstance().selectLoginOne();

        SystemCommonInfoManager.getInstance().updateStartGame(null, SharedConstants.game_flag_shut);
        SystemCommonInfoManager.getInstance().updateGameVersion(0);
        SystemCommonInfoManager.getInstance().updateShutDownGameVersion(SharedConstants.version);

        LogUtil.msgLog.info("shutdownNow|TaskExecutor|start|" + (System.currentTimeMillis() - start));
        try {
            TaskExecutor.getInstance().shutDown();
        } catch (Throwable t) {
            LogUtil.errorLog.error("shutdownNow|error|1|",t);
        }
        LogUtil.msgLog.info("shutdownNow|TaskExecutor|end|" + (System.currentTimeMillis() - start));

        try {
            ResourceWatchHandler.shutDown();
        } catch (Throwable t) {
            LogUtil.errorLog.error("shutdownNow|error|2|",t);
        }
        LogUtil.msgLog.info("shutdownNow|ResourceWatchHandler|end|" + (System.currentTimeMillis() - start));

        try {
            MessageTransmits.shutDown();
        } catch (Throwable t) {
            LogUtil.errorLog.error("shutdownNow|error|3|",t);
        }
        LogUtil.msgLog.info("shutdownNow|MessageTransmits|end|" + (System.currentTimeMillis() - start));

        try{
            AsynExecutor.destroy();
        }catch(Exception e){
            LogUtil.errorLog.error("shutdownNow|AsynExecutor|error|" + e.getMessage(), e);
        }
        LogUtil.msgLog.info("shutdownNow|AsynExecutor|end|" + (System.currentTimeMillis() - start));

        try{
            DBManager.saveDB(false);
        }catch(Exception e){
            LogUtil.errorLog.error("shutdownNow|DBManager.saveDB|error|" + e.getMessage(), e);
        }
        LogUtil.msgLog.info("shutdownNow|DBManager.saveDB|end|" + (System.currentTimeMillis() - start));

        try{
            PlayerManager.getInstance().saveConsumeDatas();
        }catch(Exception e){
            LogUtil.errorLog.error("shutdownNow|saveConsumeDatas|error|" + e.getMessage(), e);
        }
        LogUtil.msgLog.info("shutdownNow|saveConsumeDatas|end|" + (System.currentTimeMillis() - start));

        int total = 0;
        Iterator<Long> it = WebSocketManager.webSocketMap.keySet().iterator();
        while (it.hasNext()) {
            Long userId = it.next();
            if (userId != null) {
                MyWebSocket myWebSocket = WebSocketManager.webSocketMap.remove(userId);
                if (myWebSocket != null && myWebSocket.isLoginSuccess()) {
                    total++;
                    LoginDataUtil.logOutData(userId.toString(), myWebSocket.getDateTime(), new Date());
                }
            }
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        //关闭全部数据库连接池
        DruidDataSourceFactory.closeAll();

        if (closeables != null && closeables.length > 0) {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (Throwable t) {
                }
            }
        }

        LogUtil.msgLog.info("shutdownNow|end|" + total + "|" + (System.currentTimeMillis() - start));
    }


    public void uncaughtException(Thread t, Throwable e) {

    }

}
