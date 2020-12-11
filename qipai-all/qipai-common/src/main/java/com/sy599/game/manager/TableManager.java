package com.sy599.game.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg.BaiRenTableRes;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.JoinTableRes;
import com.sy599.game.shutdown.ShutDownAction;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TableManager {
    private static TableManager _inst = new TableManager();
    private static final Map<Long, BaseTable> tableMap = new ConcurrentHashMap<>();
    //金币场房间
    private static final Map<Long, BaseTable> goldTableMap = new ConcurrentHashMap<>();

    public static final Map<Integer, Class<? extends BaseTable>> wanfaTableTypes = new HashMap<>();
    private static final List<Long> tableIdList = new ArrayList<>();
    //记录特殊消息需要转换的player信息
    public static Map<Integer, Class<? extends Player>> msgPlayerTypes = new HashMap<>();
    /**
     * 俱乐部牌桌
     */
    private static final Map<String, List<BaseTable>> serverTypeMap = new ConcurrentHashMap<>();

    /**
     * 自动完成的任务超时时间
     */
    public static final int AUTO_TASK_TIMEOUT = 3;//3s

    public static boolean wanfaTableTypesPut(Integer gameType, Class<? extends BaseTable> tableClass) {
        if (GameConfigUtil.hasGame(gameType)) {
            wanfaTableTypes.put(gameType, tableClass);
            return true;
        }
        return false;
    }

    public static TableManager getInstance() {
        return _inst;
    }

    private TableManager() {
    }

    /**
     * 检查玩法和table是否匹配
     *
     * @param playType
     * @param tableClass
     * @return
     */
    public static boolean checkTableType(Integer playType, Class<? extends BaseTable> tableClass) {
        return tableClass == wanfaTableTypes.get(playType);
    }

    public void initData() {
        JjsUtil.loadMatchData();
        loadFromDB();
    }


    private void loadFromDB() {
        long time1 = TimeUtil.currentTimeMillis();
        int totalCount = TableDao.getInstance().selectCount();
        LogUtil.monitor_i("loadTable count-->" + totalCount);
        int start = 0;
        long time2 = TimeUtil.currentTimeMillis();
        LogUtil.monitor_i("time2:" + (time2 - time1));

        List<Long> deleteds = new ArrayList<>();
        while (start < totalCount) {
            long time3 = TimeUtil.currentTimeMillis();
            List<TableInf> list = TableDao.getInstance().selectAll(start, 1000);
            if (list == null || list.size() == 0) {
                break;
            }
            long time4 = TimeUtil.currentTimeMillis();
            LogUtil.monitor_i("time3:" + (time4 - time3) + " table size:" + list.size());
            for (TableInf info : list) {
                BaseTable bean = getInstanceTable(info.getPlayType());
                if (bean == null) {
                    continue;
                }
                bean.loadFromDB(info);

                if (NumberUtils.isDigits(bean.getServerKey())) {
                    try {
                        GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(bean.getServerKey());
                        if (groupTable != null) {
                            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
                            if (groupTableConfig != null) {
                                bean.setCheckPay(groupTableConfig.getPayType().intValue() == 1);
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }

                boolean reload = true;
                if (bean.isGroupRoom() && bean.getServerKey().contains("_")) {
                    try {
                        String[] temps = bean.getServerKey().split("_");
                        if (temps.length >= 2) {
                            GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(temps[1]);
                            if (groupTable == null || groupTable.isOver()) {
                                reload = false;

                                if (Redis.isConnected()) {
                                    RedisUtil.zrem(GroupRoomUtil.loadGroupKey(temps[0].substring(5), temps.length >= 3 ? Integer.parseInt(temps[2]) : 0), temps[1]);
                                    RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(temps[0].substring(5), temps.length >= 3 ? Integer.parseInt(temps[2]) : 0), temps[1]);
                                }

                                LogUtil.errorLog.error("reload table error:tableId=" + bean.getId() + ",serverKey=" + bean.getServerKey());
                            }
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }
                } else if (bean.isGoldRoom()) {
                    try {
                        GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(bean.getId());
                        if (goldRoom == null || goldRoom.isOver()) {
                            reload = false;
                            LogUtil.errorLog.error("reload table error:tableId=" + bean.getId());
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }
                }

                if (reload) {
                    tableMap.put(info.getTableId(), bean);
                    tableIdList.add(bean.getId());

                    LogUtil.monitorLog.info("init table:tableId=" + info.getTableId());

                    if (bean.getServerType() == 0) {
                        if (StringUtils.isNotBlank(bean.getServerKey()) && !NumberUtils.isDigits(bean.getServerKey())) {
                            if (bean.getState() == table_state.ready && bean.getPlayBureau() <= 1) {
                                List<BaseTable> list1 = serverTypeMap.get(bean.getServerKey());
                                if (list1 == null) {
                                    list1 = new ArrayList<>();
                                    list1.add(bean);
                                    serverTypeMap.put(bean.getServerKey(), list1);
                                } else {
                                    if (bean.getPlayerCount() < bean.getMaxPlayerCount()) {
                                        list1.add(0, bean);
                                    } else {
                                        list1.add(bean);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    deleteds.add(info.getTableId());
                }
            }
            long time5 = TimeUtil.currentTimeMillis();
            LogUtil.monitorLog.info("time4:" + (time5 - time4));
            start += 1000;
        }

        if (deleteds.size() > 0) {
            for (Long tableId : deleteds) {
                int ret = TableDao.getInstance().delete(tableId.longValue());
                LogUtil.monitorLog.info("init table datas:delete from table_inf where tableId=" + tableId + ",ret=" + ret);
            }
            LogUtil.monitorLog.info("init table datas:delete table count=" + deleteds.size());
        }

        for (BaseTable table : tableMap.values()) {
            for (Player player : table.getPlayerMap().values()) {
                player.setSyncTime(new Date());
            }
        }
    }

    public BaseTable getInstanceTable(int playType) {
        Class<? extends BaseTable> cls = wanfaTableTypes.get(playType);
        if (cls == null) {
            LogUtil.errorLog.error("getInstanceTable error:table is not exists:playType=" + playType);
        } else {
            try {
                BaseTable table = ObjectUtil.newInstance(cls);
                table.setPlayType(playType);
                return table;
            } catch (Exception e) {
                LogUtil.errorLog.error("getInstanceTable err:" + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取房间id
     *
     * @param createId
     * @param type
     * @param tableType 房间类型 0普通开房，1军团开房，2代开房
     * @return
     */
    public long generateId(long createId, int type, int tableType) {
        return generateId(createId, type, tableType, GameServerConfig.SERVER_ID);
    }

    public synchronized long generateId(long createId, int type, int tableType, int serverId) {
//		if (GameServerConfig.isMemcache) {
//			// 从redis中获取房间id
//			Long roomId = getRoomIdToCached();
//			if (roomId != null) {
//				return roomId;
//			}
//		}
        long tableId = TableDao.getInstance().callProCreateRoom(createId, serverId, type, tableType);
        return tableId;
    }

    public static Map<String, List<BaseTable>> getServerTypeMap() {
        return serverTypeMap;
    }

    public int addTable(BaseTable table) {
        if (table.getId() > 0) {
            synchronized (this) {
                BaseTable table0 = tableMap.put(table.getId(), table);
                tableIdList.add(table.getId());

                if (table.isGoldRoom()) {
                    goldTableMap.put(table.getId(), table);
                }
                return table0 == null ? 1 : 2;
            }
        }
        return 0;
    }

    /**
     * 添加金币场房间
     *
     * @param table
     */
    public int addGoldTable(BaseTable table) {
        if (table.isGoldRoom()) {
            BaseTable table1 = goldTableMap.put(table.getId(), table);
            if (table1 != null && table1.getPlayerCount() > 0) {
                goldTableMap.put(table.getId(), table1);
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 如果deleted==true，则只从内存中删除，否则也要删除数据库中的数据
     * @param table
     * @param deleted 是否已被删除
     * @return
     */
    public int delTable(BaseTable table, boolean deleted) {
        int result = 0;
        BaseTable tempTable = tableMap.remove(table.getId());

        if (table.isGoldRoom()) {
            goldTableMap.remove(table.getId());
        }

        int reset = 0;
        int del;
        if (!deleted) {
            del = TableDao.getInstance().delete(table.getId());
            if (del > 0) {
                reset = TableDao.getInstance().clearRoom(table.getId());
                GotyeChatManager.getInstance().deleteGotyeRoomId(table.getGotyeRoomId());
                if (table.isDaikaiTable()) {
                    result = table.dissDaikaiTable();
                } else {
                    result = del;
                }
                // TableDao.getInstance().setGotyeRoomUseOver(table.getGotyeRoomId());
//				RedisManage.getInstance().setListCached(ROOM_IDS_KEY, table.getId());
            }
        } else {
            TableDao.getInstance().delete(table.getId());
            del = 0;
        }

        LogUtil.msgLog.info("delTable msg: table=" + (tempTable == null ? "null" : "deleted") + ",tableId=" + table.getId() + ",serverKey=" + table.getServerKey() + ",del=" + del + ",reset room=" + reset + ",deleted=" + deleted);

        if (tempTable != null) {
            tableIdList.remove(Long.valueOf(table.getId()));
            if (StringUtils.isNotBlank(tempTable.getServerKey())) {
                List<BaseTable> list = serverTypeMap.get(tempTable.getServerKey());
                if (list != null) {
                    boolean ret = list.remove(tempTable);
                    LogUtil.msgLog.info("delTable from serverTypeMap:tableId=" + table.getId() + ",serverType=" + table.getServerType() + ",serverKey=" + table.getServerKey() + ",result=" + ret);
                }
            }
        }

        if (!deleted) {
            String tableKeyId = table.getServerKey();
            if (NumberUtils.isDigits(tableKeyId)) {
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", tableKeyId);
                    map.put("currentState", table.getDissCurrentState());

                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                } catch (Exception e) {
                    LogUtil.errorLog.error("Group Exception:" + e.getMessage(), e);
                }
            }
        }

        return result;
    }

    public static boolean removeUnavailableTable(BaseTable table) {
        if (StringUtils.isNotBlank(table.getServerKey())) {
            List<BaseTable> list = serverTypeMap.get(table.getServerKey());
            if (list != null) {
                boolean ret = list.remove(table);
                LogUtil.msgLog.info("removeUnavailableTable from serverTypeMap:tableId=" + table.getId() + ",serverType=" + table.getServerType() + ",serverKey=" + table.getServerKey() + ",state=" + table.getState() + ",result=" + ret);
                return ret;
            }
        }
        return false;
    }

    public BaseTable getTable(long id) {
        if (id > 0)
            return tableMap.get(id);
        else
            return null;
    }

    public int getTableCount() {
        return tableMap.size();
    }

    public void saveDB(boolean asyn) {

        int count = tableMap.size();
        if (count > 0) {
            if (asyn) {
                TaskExecutor.SINGLE_EXECUTOR_SERVICE_TABLE.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> list = saveDB0(true);
                        if (list != null && list.size() > 0) {
                            TableDao.getInstance().batchUpdate(list);
                        }

                    }
                });
            } else {
                List<Map<String, Object>> list = saveDB0(asyn);
                if (list != null && list.size() > 0) {
                    TableDao.getInstance().batchUpdate(list);
                }
            }
        }
    }

    private final static List<Map<String, Object>> saveDB0(boolean asyn) {
        int count = tableMap.size();
        if (count > 0) {
            List<Map<String, Object>> list = new ArrayList<>(count);
            for (Map.Entry<Long, BaseTable> kv : tableMap.entrySet()) {
                Map<String, Object> map = kv.getValue().saveDB(asyn);
                if (map != null && map.size() > 0) {
                    list.add(map);
                }
            }
            return list;
        }
        return null;
    }

    private static void execute(final CountDownLatch countDownLatch, final BaseTable table) {
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                int referenceCount;
                if ((referenceCount = table.getReferenceCounter().get()) >= 1 || referenceCount < 0) {
                    countDownLatch.countDown();
                    LogUtil.errorLog.warn("table is dead ? tableId={},referenceCount={},players={}", table.getId(), referenceCount, table.getPlayerMap().keySet());
                } else {
                    table.getReferenceCounter().addAndGet(1);

                    if (table.getMaxPlayerCount() == table.getPlayerCount() || table.getPlayedBureau() > 0) {
                        try {
                            table.checkCompetitionPlay();
                        } catch (Throwable e) {
                            LogUtil.errorLog.error("Exception|" + table.getId() + "|" + e.getMessage(), e);
                        }
                    }

                    try {
                        boolean isdiss = table.checkRoomDiss();
                        if (isdiss) {
                            LogUtil.monitor_i("apply diss table timeout-->id:" + table.getId() + " time:" + table.getCreateTime().toString() + " createId:" + table.getMasterId() + ",result=1");
                        } else {
                            isdiss = table.checkDissByDate();
                            if (isdiss) {
                                int ret = table.diss();
                                LogUtil.monitor_i("auto deltable-->id:" + table.getId() + " time:" + table.getCreateTime().toString() + " createId:" + table.getMasterId() + ",result=" + ret);
                            }
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }

                    table.getReferenceCounter().addAndGet(-1);
                    countDownLatch.countDown();
                }
            }
        });
    }

    public void checkCompetitionTask() {
        try {
            BaseTable[] tables = tableMap.values().toArray(new BaseTable[0]);
            int count = tables.length;
            if (count > 0) {
                long startTime = System.currentTimeMillis();
                CountDownLatch countDownLatch = new CountDownLatch(count);
                for (BaseTable table : tables) {
                    execute(countDownLatch, table);
                }
                boolean isOk = false;
                try {
                    isOk = countDownLatch.await(AUTO_TASK_TIMEOUT, TimeUnit.SECONDS);
                } catch (Throwable e) {
                    LogUtil.errorLog.error("Exception:count=" + countDownLatch.getCount() + ",msg=" + e.getMessage(), e);
                } finally {
                    if (System.currentTimeMillis() - startTime > 50)
                        LogUtil.msgLog.info("auto play:table count=" + count + ",time(ms)=" + (System.currentTimeMillis() - startTime) + ",ok=" + isOk);
                }
            }

            long currentTime = System.currentTimeMillis();
            //金币场房间检查
            for (BaseTable table : goldTableMap.values()) {
                if (table.getReferenceCounter().get() >= 1) {
                    continue;
                }

                if (table.isGoldRoom() && table.allowRobotJoin() && table.getPlayerCount() < table.getMaxPlayerCount()) {
                    if (table.getLastCheckTime() == 0L) {
                        table.setLastCheckTime(currentTime);
                        continue;
                    } else if (currentTime - table.getLastCheckTime() < GoldConstans.loadRobotJoinTime()) {
                        continue;
                    }

                    synchronized (Constants.GOLD_LOCK) {
                        if (table.getPlayerCount() < table.getMaxPlayerCount()) {
                            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(table.getId());
                            if (goldRoom != null && goldRoom.isNotStart() && goldRoom.getCurrentCount().intValue() > 0 && goldRoom.getCurrentCount().intValue() < goldRoom.getMaxCount().intValue()) {

                                List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsers(goldRoom.getKeyId());
                                List<Player> playerList = new ArrayList<>(list.size());
                                for (HashMap<String, Object> map : list) {
                                    long tempUserId = Long.parseLong(map.get("userId").toString());
                                    Player tempPlayer = PlayerManager.getInstance().getPlayer(tempUserId);
                                    if (tempPlayer == null) {
                                        tempPlayer = PlayerManager.getInstance().loadPlayer(tempUserId, table.getPlayType());
                                        tempPlayer.setIsOnline(0);
                                        tempPlayer.setIsEntryTable(SharedConstants.table_offline);
                                        LogUtil.msgLog.info("gold user online?false:userId=" + tempUserId);
                                    }
                                    playerList.add(tempPlayer);
                                }
                                int c = table.getMaxPlayerCount() - playerList.size();
                                while (c > 0) {
                                    Player tempPlayer = ObjectUtil.newInstance(table.getPlayerClass());
                                    tempPlayer.setUserId(-c);
                                    tempPlayer.setFlatId(String.valueOf(tempPlayer.getUserId()));
                                    tempPlayer.setPf("robot");
                                    tempPlayer.setName(DataLoaderUtil.loadRandomRobotName());
                                    playerList.add(tempPlayer);
                                    c--;
                                }

                                //开始游戏
                                GoldRoomDao.getInstance().updateGoldRoom0(goldRoom.getKeyId(), goldRoom.getMaxCount(), "1");

                                //随机房主
                                Player master = playerList.remove(new SecureRandom().nextInt(playerList.size()));

                                JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
                                BaseTable table1 = TableManager.getInstance().createSimpleTable(master.isRobot() ? master : playerList.get(playerList.size() - 1), StringUtil.explodeToIntList(json.getString("ints"), ",")
                                        , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, JjsUtil.loadMatch(goldRoom));

                                if (table1 != null) {
                                    table1.setMasterId(master.getUserId());
                                    table1.saveSimpleTable();
                                    playerList.add(0, master);

                                    // 牌桌进入准备阶段
                                    table1.changeTableState(table_state.ready);
                                    TableManager.getInstance().addTable(table1);

                                    master.sendActionLog(LogConstants.reason_createtable, "tableId:" + table1.getId());

                                    List<Player> tablePlayers = new ArrayList<>(playerList.size());
                                    for (Player player1 : playerList) {
                                        Player player2 = PlayerManager.getInstance().changePlayer(player1, table1.getPlayerClass());
//							if(player2.getUserId() == table1.getMasterId()) {
                                        table1.ready(player2);
//							}

                                        // 加入牌桌
                                        if (!table1.joinPlayer(player2)) {
                                            continue;
                                        } else {
                                            tablePlayers.add(player2);
                                        }
                                        player2.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                                    }

                                    Map<Long, GeneratedMessage> msgMap = new HashMap<>();
                                    //先发给自己
                                    for (Player player2 : tablePlayers) {
                                        JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
                                        joinRes.setPlayer(player2.buildPlayInTableInfo());
                                        //玩法
                                        joinRes.setWanfa(table1.getPlayType());

                                        GeneratedMessage msg = joinRes.build();
                                        msgMap.put(player2.getUserId(), msg);
                                        player2.writeSocket(msg);
                                    }

                                    //后发给其他人
                                    for (Player player2 : tablePlayers) {
                                        for (Map.Entry<Long, GeneratedMessage> kv : msgMap.entrySet()) {
                                            if (player2.getUserId() != kv.getKey().longValue()) {
                                                player2.writeSocket(kv.getValue());
                                            }
                                        }
                                    }

                                    //房主以外的其他玩家选座发送消息
                                    for (Player player2 : tablePlayers) {
                                        if (player2.getUserId() != master.getUserId()) {
                                            table1.sendPlayerStatusMsg();
                                        }
                                    }

                                    table1.checkDeal();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LogUtil.errorLog.error("Throwable:" + e.getMessage(), e);
        }
    }

    // 代开牌桌
    public void daikaiTable(Player player, ComReq req) {
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();

        int playType = StringUtil.getIntValue(params, 1, 0);
        int allowGroupMember = 0;
        if (GameUtil.isPlayDn(playType)) {
            if (!SharedConstants.isKingOfBull()) {
                allowGroupMember = StringUtil.getIntValue(params, 18, 0);
            } else {
                allowGroupMember = StringUtil.getIntValue(params, 17, 0);
            }
        }
        if (allowGroupMember > 0 && player.getIsGroup() <= 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_52));
            return;
        }
        if (playType == GameUtil.play_type_3POK || playType == GameUtil.play_type_4POK) {//临时屏蔽四人打筒子代开
            return;
        }

        int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        if (!GameUtil.isPlayAhGame() && GameUtil.isPlayBopi(playType)) {
            bureauCount = 50;
        }
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        BaseTable table = TableManager.getInstance().getInstanceTable(playType);

        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, playType);
            return;
        }

        int payType = 2;

        if (GameUtil.isPlayAhDdz(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            } else if (playerCount != 2 && playerCount != 3) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 9, 2);
            if (payType == 1) {
                payType = 2;
            }
        } else if (GameUtil.isPlayAhPdk(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            } else if (playerCount != 2 && playerCount != 3) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhMj(playType)) {
            playerCount = StringUtil.getIntValue(params, 8, 4);// 人数

            if (playerCount < 2 || playerCount > 4) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhPhz(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhCsMj(playType) || GameUtil.isPlayAhZzOrHzMj(playType)) {
            if (playerCount == 0) {
                playerCount = 4;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayDn(playType) || GameUtil.isPlayYjMj(playType)) {
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayTenthirty(playType) || GameUtil.isPlayPdk(playType) || GameUtil.isPlayYjPdk(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayDtz(playType)) {
            bureauCount = bureauCount > 0 ? bureauCount : 30;
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlaySyPhz(playType)) {
            if (GameUtil.isPlayBopi(playType)) {
                bureauCount = 50;
            }
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlaySp(playType) || GameUtil.isPlayBbtz(playType) || GameUtil.isPlayYjGhz(playType)) {
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlayQianFen(playType)) {
            payType = StringUtil.getIntValue(params, 2, 2);
            bureauCount = 100;
        } else {
            payType = StringUtil.getIntValue(params, 10, 2);
        }
//        boolean isAAdaikai = false;
//        if(GameUtil.isPlayDn(playType) || GameUtil.isPlayPdk(playType) || GameUtil.isPlaySyPhz(playType) || GameUtil.isPlayDtz(playType)){
//        	isAAdaikai = true;
//        }
        int needCard;
        if (payType == 1) {
            needCard = 0;
        } else {
            if (GameUtil.isPlayDtz(playType)) {
                int score_max = StringUtil.getIntValue(params, 3, 600);
                bureauCount = bureauCount > 0 ? bureauCount : 30;
                needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), 1, score_max);
            } else {
                needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), 1);
            }
        }

        if (needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return;
        }

        long userId = player.getUserId();
        String createPara = StringUtil.implode(params, ",");
        String createStrPara = StringUtil.implode(strParams, "#");

        long tableId;
        boolean existFlag;
        Server server = ServerManager.loadServer(playType, 1);
        int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;
        do {
            tableId = generateId(userId, playType, 2, serverId);
            if (tableId <= 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_0));
                return;
            }
            existFlag = TableDao.getInstance().checkTableIdExist(tableId);
        } while (existFlag);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("tableId", tableId);
        paramMap.put("daikaiId", userId);
        paramMap.put("serverId", serverId);
        paramMap.put("playType", playType);
        paramMap.put("needCard", needCard);
        paramMap.put("state", 0);
        paramMap.put("createFlag", 0);
        paramMap.put("createPara", createPara);
        paramMap.put("createStrPara", createStrPara);
        paramMap.put("createTime", null);
        paramMap.put("daikaiTime", new Date());
        paramMap.put("returnFlag", 0);
        paramMap.put("playerInfo", "");
        paramMap.put("extend", null);

        try {
            TableDao.getInstance().daikaiTable(paramMap);
        } catch (SQLException e) {
            LogUtil.e("daikaiTable err:", e);
            return;
        }

        if (needCard != 0) {
            player.changeCards(0, -needCard, true, playType, false, CardSourceType.daikaiTable_FZ);
        }

        LogUtil.msgLog.info("daikai msg={},payType={}", paramMap, payType);
        player.writeComMessage(WebSocketMsgType.res_com_code_daikaitable, 1, playType);
    }

    public BaseTable createBaiRenTable(Player player, List<Integer> params, List<String> strParams) throws Exception {
        int play = StringUtil.getIntValue(params, 0, 1);// 玩法
        if (params.size() < 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return null;
        }
        BaseTable table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        if (player == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        long tableId = new Long(play);
        if (!tableMap.containsKey(tableId)) {// 房间ID为2表示龙虎斗房间
            table.createBaiRenTable(player, play, params, strParams);
            table.joinPlayer(player);// 加入房间
            addTable(table);
        } else {// 已经有房间时
            table = tableMap.get(tableId);
            if (!table.getPlayerMap().containsKey(player.getUserId())) {
                table.joinPlayer(player);// 加入房间
            }
        }
        table.getPlayerMap().put(player.getUserId(), player);
        if (player.getPlayingTableId() != play) {
            player.setPlayingTableId(play);
            player.saveBaseInfo();
        }
        return table;
    }

    public BaseTable createSimpleTable(Player player, List<Integer> params, List<String> strParams, GoldRoom goldRoom, MatchBean matchBean) throws Exception {
        int bureauCount;
        String modeId;
        if (matchBean != null) {
            bureauCount = JjsUtil.loadMatchCurrentGameCount(matchBean);
            modeId = "match" + matchBean.getKeyId().toString();
        } else {
            modeId = goldRoom.getModeId();
            bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        }
        int play = StringUtil.getIntValue(params, 1, 1);// 玩法
        if (params.size() <= 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return null;
        }

        BaseTable table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        table.setDaikaiTableId(goldRoom.getKeyId());
        if ("2".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1"))) {
            table.createTable(player, play, bureauCount, params, strParams);
            if (table.getId() <= 0) {
                return null;
            }
            if (table.isGoldRoom()) {
                try {
                    table.setModeId(goldRoom.getModeId());
                } catch (Exception e) {
                }
            }
            return table;
        } else {
            Long matchId = matchBean == null ? null : matchBean.getKeyId();
            table.setModeId(modeId);
            table.setMatchId(matchId);
            table.setMatchRatio(goldRoom.loadMatchRatio());
            table.changeExtend();
            if (table.createSimpleTable(player, play, bureauCount, params, strParams, false)) {
                table.setModeId(modeId);
                table.setMatchId(matchId);
                table.setMatchRatio(goldRoom.loadMatchRatio());
                table.changeExtend();
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                return table;
            } else {
                return null;
            }
        }
    }

    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, Map<String, Object> properties) throws Exception {
        return createTable(player, params, strParams, daikaiTableId, creatorId, true, properties);
    }

    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, boolean checkPay, Map<String, Object> properties) throws Exception {
        return createTable(player, params, strParams, daikaiTableId, creatorId, checkPay, properties, null, null);
    }

    // 创建牌桌
    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, boolean checkPay, Map<String, Object> properties, GroupTable groupTable, GroupTableConfig groupTableConfig) throws Exception {
        // 是比赛场
        if (player.isMatching()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_240));
            return null;
        }

        BaseTable table = player.getPlayingTable();
        boolean requiredReload = true;
        boolean autoCreate = properties != null && properties.containsKey("autoCreate"); //智能补房
        boolean recreate = properties != null && properties.containsKey("recreate"); //一次创建多个房间或智能补房，只是创建了GroupTable，实际未生成Table

        // 牌桌不为空
        if (table != null && !autoCreate) {
            // 重连牌桌
            reconnect(table, player);
            return null;
        }

        long playingTableId = player.getPlayingTableId();
        // 牌桌为空但是牌桌id不为0，房间已解散
        if (playingTableId != 0 && table == null && !autoCreate) {
            // player.writeErrMsg(LangHelp.getMsg(LangMsg.code_8,
            // player.getPlayingTableId()));
            if (GoldRoomUtil.isNotGoldRoom(player.getPlayingTableId())) {
                player.setPlayingTableId(0);
                player.saveBaseInfo();
            } else {
                if (daikaiTableId == playingTableId) {
                    //创建房间
                } else {
                    GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(playingTableId, player.getUserId());
                    if (goldRoomUser != null) {
                        GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                        String matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");
                        if ("2".equals(matchType)) {
                            if (goldRoom == null || goldRoom.isOver()) {
                                player.setPlayingTableId(0);
                                player.saveBaseInfo();
                            } else {
                                //游戏正在进行中
                                table = TableManager.getInstance().getTable(goldRoom.getKeyId());
                                if (table != null) {
                                    if (table.getPlayerMap().isEmpty() && table.getRoomPlayerMap().isEmpty()) {
                                        table.diss();
                                        player.setPlayingTableId(0);
                                        player.saveBaseInfo();
                                        player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_8, table.getId()), WebSocketMsgType.sc_code_err_table);
                                        return null;
                                    }
                                    if (!table.checkPlayer(player)) {
                                        LogUtil.e("check player err:" + player.getUserId());
                                        player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_15, player.getPlayingTableId()), WebSocketMsgType.sc_code_err_table);
                                        return null;
                                    }
                                    table.ready();

                                    table.checkDeal();
                                    table.broadIsOnlineMsg(player, SharedConstants.table_online);
                                    LogUtil.d_msg("table-->" + table.getId() + " " + table.getClass().getName());

                                    CreateTableRes res = table.buildCreateTableRes(player.getUserId(), true, false);
                                    player.writeSocket(res);
                                    table.checkDiss();
                                    table.checkReconnect(player);
                                    table.checkSendDissMsg(player);
                                    table.sendPlayerStatusMsg();
                                    table.broadOnlineStateMsg();
                                } else {
                                    player.setPlayingTableId(0);
                                    player.saveBaseInfo();
                                }
                            }
                        } else {
                            if (goldRoom == null || goldRoom.isOver()) {
                                player.setPlayingTableId(0);
                                player.saveBaseInfo();
                            } else if (goldRoom.isPlaying()) {
                                //游戏正在进行中
                                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                                return null;
                            } else {
                                //等待开始游戏
                                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);

                                JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
                                BaseTable table1 = TableManager.getInstance().createSimpleTable(player, StringUtil.explodeToIntList(json.getString("ints"), ",")
                                        , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, JjsUtil.loadMatch(goldRoom));

                                if (table1 != null)
                                    synchronized (Constants.GOLD_LOCK) {
                                        TableManager.getInstance().addGoldTable(table1);
                                    }

                                return null;
                            }
                        }
                    } else {
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    }
                }
            }
        }

        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_240));
            return null;
        }

        /**
         * 重置玩家俱乐部成员数据
         */
        player.setGroupUser(null);
        player.setIsGroup(0);

        String groupId = strParams.size() > 0 && NumberUtils.isDigits(strParams.get(0)) && Long.parseLong(strParams.get(0)) > 0L ? strParams.get(0) : null;
        int tableCount = NumberUtils.toInt(strParams.size() > 1 ? strParams.get(1) : "1", 1);
        String tableVisible = strParams.size() > 2 ? strParams.get(2) : "1";//0私密1可见
        String modeId = strParams.size() > 3 ? strParams.get(3) : "";//创房模式ID
        //俱乐部包厢ID
        int groupRoom = 0;

        Long modeVal = null;

        if (tableCount > 20) {
            tableCount = 20;
        }
        if (recreate) {
            tableCount = 1;
        }
        if (tableCount < 1) {
            tableCount = 1;
        }
        int maxTableCount = tableCount;

        int pay0 = 0;

        boolean isGroupMatch = properties != null && properties.containsKey("groupMatchUsers");

        if (isGroupMatch) {
            GroupUser creator = GroupDao.getInstance().loadGroupMaster(groupId);
            if (creator != null) {
                creatorId = creator.getUserId();
            }
        }

        if (modeId.length() > 0 && NumberUtils.isDigits(modeId) && Integer.parseInt(modeId) > 0 && !recreate) {
            GroupTableConfig groupTableConfig0 = groupTableConfig != null ? groupTableConfig : GroupDao.getInstance().loadGroupTableConfig(Long.parseLong(modeId));
            if (groupTableConfig0 == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                return null;
            }
            if (groupTableConfig0.getParentGroup().longValue() == 0) {
                groupRoom = 0;
                if (!groupTableConfig0.getGroupId().toString().equals(groupId)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                    return null;
                }
            } else {
                groupRoom = groupTableConfig0.getGroupId().intValue();
                if (!groupTableConfig0.getParentGroup().toString().equals(groupId)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                    return null;
                }
            }

            modeVal = groupTableConfig0.getKeyId();

            JsonWrapper json;
            List<Integer> intsList = null;
            List<String> strsList = null;
            try {
                json = new JsonWrapper(groupTableConfig0.getModeMsg());
                intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                strsList = GameConfigUtil.string2List(json.getString("strs"));
            } catch (Throwable th) {
            } finally {
                if ((intsList == null || intsList.size() == 0) && (strsList == null || strsList.size() == 0)) {
                    intsList = GameConfigUtil.string2IntList(groupTableConfig0.getModeMsg());
                    strsList = Collections.emptyList();
                }
            }

            params = intsList;
            if (!strsList.isEmpty()) {
                strParams = strsList;
            }
        }

        if (params.size() <= 0) {
            if (groupId != null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_241));
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            }
            return null;
        }

        // ComReq req = (ComReq) this.recognize(ComReq.class, message);
        int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        int play = StringUtil.getIntValue(params, 1, 1);// 玩法
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数

        if (maxTableCount == 1) {
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("force_game_type"))) {
                Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
                if (server == null || !server.getGameType().contains(play)) {
                    player.writeErrMsg(LangMsg.code_251, play);
                    return null;
                }
            }
        }

        int payType = 1;//1AA,2房主,3代付
        //根据player对象中的playType属性，在缓存中查找对应的玩法table,new table对象，并未初始化
        table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        } else {
            table.setCheckPay(checkPay);
        }

        boolean goGold = false;
        if (properties != null) {
            String serverKey = String.valueOf(properties.get("serverKey"));
            if (serverKey.contains("gold")) {
                goGold = true;
            }
        }

        int needCard = 0;
        int payMark = -1;
        Player payPlayer = null;

        int allowGroupMember = 0;
        if (GoldRoomUtil.isGoldRoom(daikaiTableId)) {
            table.setDaikaiTableId(daikaiTableId);
        } else if (daikaiTableId > 0) {
            if (SharedConstants.isAssisOpen()) {
                if (properties != null && !properties.isEmpty()) {
                    String assisCreateNo = (String) properties.get("assisCreateNo");
                    String assisGroupNo = (String) properties.get("assisGroupNo");
                    table.setAssisCreateNo(assisCreateNo);
                    table.setAssisGroupNo(assisGroupNo);
                }
            }

            if (GameUtil.isPlayDn(play)) {
                if (!SharedConstants.isKingOfBull()) {
                    allowGroupMember = StringUtil.getIntValue(params, 18, 0);
                } else {
                    allowGroupMember = StringUtil.getIntValue(params, 17, 0);
                }
            }

            GroupUser groupUser;
            if (allowGroupMember > 0) {
                // 代开军团房间是检测第一个加入房间的人和代开人是否同一个军团
                Player player1 = PlayerManager.getInstance().getPlayer(creatorId);
                if (player1 == null) {
                    groupUser = GroupDao.getInstance().loadGroupUser(creatorId, groupId);
                } else {
                    groupUser = player1.loadGroupUser(groupId);
                }
                if (groupUser == null) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_52));
                    return null;
                }
                if (player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != groupUser.getGroupId().intValue()) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                    return null;
                } else if (player.getGroupUser().getUserLevel() == null || player.getGroupUser().getUserLevel().intValue() <= 0) {
                    player.writeErrMsg("您已被禁止游戏，请联系群主");
                    return null;
                }
            }

            table.setCheckPay(false);
            table.setDaikaiTableId(daikaiTableId);

            if (GameUtil.isPlayAhDdz(play)) {
                if (playerCount == 0) {
                    playerCount = 3;
                } else if (playerCount != 2 && playerCount != 3) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                    return null;
                }
                payType = StringUtil.getIntValue(params, 9, 2);
                if (payType == 1) {
                    payType = 2;
                }
            } else if (GameUtil.isPlayAhPdk(play)) {
                if (playerCount == 0) {
                    playerCount = 3;
                } else if (playerCount != 2 && playerCount != 3) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                    return null;
                }
                payType = StringUtil.getIntValue(params, 10, 2);
            } else if (GameUtil.isPlayAhMj(play)) {
                playerCount = StringUtil.getIntValue(params, 8, 4);// 人数

                if (playerCount < 2 || playerCount > 4) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                    return null;
                }
                payType = StringUtil.getIntValue(params, 10, 2);
            } else if (GameUtil.isPlayAhPhz(play)) {
                payType = StringUtil.getIntValue(params, 10, 2);
                if (playerCount == 0) {
                    playerCount = 3;
                }
            } else if (GameUtil.isPlayAhCsMj(play) || GameUtil.isPlayAhZzOrHzMj(play)) {
                payType = StringUtil.getIntValue(params, 10, 2);
                if (playerCount == 0) {
                    playerCount = 4;
                }
            } else if (GameUtil.isPlayDn(play) || GameUtil.isPlayYjMj(play)) {
                payType = StringUtil.getIntValue(params, 10, 2);
            } else if (GameUtil.isPlayTenthirty(play) || GameUtil.isPlayPdk(play) || GameUtil.isPlayYjPdk(play)) {
                payType = StringUtil.getIntValue(params, 9, 2);
            } else if (GameUtil.isPlayDtz(play)) {
                bureauCount = bureauCount > 0 ? bureauCount : 30;
                payType = StringUtil.getIntValue(params, 2, 2);
            } else if (GameUtil.isPlaySyPhz(play)) {
                if (GameUtil.isPlayBopi(play)) {
                    bureauCount = 50;
                }
                payType = StringUtil.getIntValue(params, 9, 2);
            } else if (GameUtil.isPlaySp(play) || GameUtil.isPlayBbtz(play) || GameUtil.isPlayYjGhz(play)) {
                payType = StringUtil.getIntValue(params, 2, 2);
            } else if (GameUtil.isPlayQianFen(play)) {
                payType = StringUtil.getIntValue(params, 2, 2);
                bureauCount = 100;
            } else if (GameUtil.isPlayZzMj(play) || GameUtil.isPlayCsMj(play) || GameUtil.isPlayHzMj(play) || GameUtil.isPlaySyMj(play)) {
                payType = StringUtil.getIntValue(params, 2, 2);
            } else {
                payType = StringUtil.getIntValue(params, 10, 2);
            }

            if (payType == 1) {
                table.setCheckPay(true);

                if (GameUtil.isPlayYjGhz(play) || GameUtil.isPlayYjMj(play) || GameUtil.isPlayYjPdk(play)) {
                    needCard = table.getNeedCard(params);
                } else {
                    if (GameUtil.isPlayDtz(play)) {
                        int score_max = StringUtil.getIntValue(params, 3, 600);
                        needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 0, score_max);
                    } else {
                        needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 0);
                    }
                }

                // 如果玩家的钻石小于玩一局需要的钻石，则返回
                if (PayConfigUtil.loadPayResourceType(play) == UserResourceType.TILI) {
                    if (needCard < 0 || needCard > 0 && player.getUserTili() < needCard) {
                        player.writeErrMsg(UserResourceType.TILI.getName() + "不足");
                        return null;
                    }
                } else {
                    if (needCard < 0 || needCard > 0 && player.loadAllCards() < needCard) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return null;
                    }
                }
            }
        } else {
            if (!goGold) {
                if (GameUtil.isPlayAhDdz(play)) {
                    if (playerCount == 0) {
                        playerCount = 3;
                    } else if (playerCount != 2 && playerCount != 3) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                        return null;
                    }
                    payType = StringUtil.getIntValue(params, 9, 2);
                    if (payType == 1) {
                        payType = 2;
                    }
                    if (payType == 1) {
                        needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                    } else {
                        needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                    }
                } else if (GameUtil.isPlayAhPdk(play)) {
                    if (playerCount == 0) {
                        playerCount = 3;
                    } else if (playerCount != 2 && playerCount != 3) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                        return null;
                    }
                    payType = StringUtil.getIntValue(params, 10, 2);
                    if (payType == 1) {
                        needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                    } else {
                        needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                    }
                } else if (GameUtil.isPlayAhMj(play)) {
                    playerCount = StringUtil.getIntValue(params, 8, 4);// 人数

                    if (playerCount < 2 || playerCount > 4) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                        return null;
                    }
                    payType = StringUtil.getIntValue(params, 10, 2);
                    if (payType == 1) {
                        needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                    } else {
                        needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                    }
                } else if (GameUtil.isPlayAhPhz(play)) {
                    payType = StringUtil.getIntValue(params, 10, 2);
                    if (playerCount == 0) {
                        playerCount = 3;
                    }
                    if (payType == 1) {
                        needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                    } else {
                        needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                    }
                } else if (GameUtil.isPlayAhCsMj(play) || GameUtil.isPlayAhZzOrHzMj(play)) {
                    payType = StringUtil.getIntValue(params, 10, 2);
                    if (playerCount == 0) {
                        playerCount = 4;
                    }
                    if (payType == 1) {
                        needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                    } else {
                        needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                    }
                }else if(GameUtil.isPlayBSMj(play)) {
                	 playerCount = StringUtil.getIntValue(params, 3, 4);// 人数
                	 payType = StringUtil.getIntValue(params, 2, 1);
                     if (playerCount == 0) {
                         playerCount = 4;
                     }
                     if (payType == 1) {
                         needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                     } else {
                         needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                     }
                }else if (GameUtil.isPlayYjMj(play)) {
                    payType = StringUtil.getIntValue(params, 10, 1);
                    needCard = table.getNeedCard(params);
                } else if (GameUtil.isPlayYjPdk(play)) {
                    payType = StringUtil.getIntValue(params, 9, 1);
                    needCard = table.getNeedCard(params);
                } else if (GameUtil.isPlayYjGhz(play)) {
                    payType = StringUtil.getIntValue(params, 2, 1);
                    needCard = table.getNeedCard(params);
                } else {
                    if (GameUtil.isPlayDn(play)) {
                        payType = StringUtil.getIntValue(params, 10, 1);

                        if (!SharedConstants.isKingOfBull()) {
                            allowGroupMember = StringUtil.getIntValue(params, 18, 0);
                        } else {
                            allowGroupMember = StringUtil.getIntValue(params, 17, 0);
                        }
                    } else if (GameUtil.isPlayTenthirty(play) || GameUtil.isPlayThreeMonkeys(play)) {
                        payType = StringUtil.getIntValue(params, 9, 1);
                    } else if (GameUtil.isPlayDdz(play)) {
                        payType = StringUtil.getIntValue(params, 9, 1);
                    } else if (GameUtil.isPlayGSMajiang(play)) {
                        payType = StringUtil.getIntValue(params, 10, 1);
                    } else if (GameUtil.isPlayCCMajiang(play)) {
                        payType = StringUtil.getIntValue(params, 10, 2);
                    } else if (GameUtil.isPlaySp(play) || GameUtil.isPlayMajiang(play) || GameUtil.isPlayBbtz(play)) {
                        payType = StringUtil.getIntValue(params, 2, 1);
                    } else if (GameUtil.isPlayQianFen(play)) {
                        payType = StringUtil.getIntValue(params, 2, 1);
                        bureauCount = 100;
                    } else {
                        payType = StringUtil.getIntValue(params, 10, 1);
                    }

                    if (GameUtil.isPlayDtz(play)) {
                        payType = StringUtil.getIntValue(params, 2, 1);
                        int score_max = StringUtil.getIntValue(params, 3, 600);
                        bureauCount = bureauCount > 0 ? bureauCount : 30;

                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 0, score_max);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 1, score_max);
                        }
                    } else if (GameUtil.isPlayPdk(play) || GameUtil.isPlayYzPdk(play)) {
                        payType = StringUtil.getIntValue(params, 9, 1);
                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                        }
                    } else if (GameUtil.isPlaySyPhz(play)) {
                        if (GameUtil.isPlayBopi(play)&&bureauCount!=1) {
                            bureauCount = 50;
                        }
                        payType = StringUtil.getIntValue(params, 9, 1);
                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                        }
                    } else if(GameUtil.isLdfpf(play)) {
                        payType = StringUtil.getIntValue(params, 9, 1);
                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                        }
                    }else if (GameUtil.isPlayZzMj(play) || GameUtil.isPlayCsMj(play) || GameUtil.isPlayHzMj(play) || GameUtil.isPlaySyMj(play)) {
                        payType = StringUtil.getIntValue(params, 2, 2);
                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, playerCount, 0, null);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, playerCount, 1, null);
                        }
                    } else {
                        if (payType == 1) {
                            needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 0);
                        } else {
                            needCard = (!checkPay) ? 0 : PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 1);
                        }
                    }
                }
                // 如果玩家的钻石小于玩一局需要的钻石，则返回
                if (table.isCheckPay()) {
                    if (StringUtils.isNotBlank(groupId)) {

                        //俱乐部游戏免费检测
                        boolean free = GameConfigUtil.freeGameOfGroup(play, groupId);
                        if (free) {
                            needCard = 0;
                            pay0 = 0;
                        }

                        GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.parseLong(groupId), 0);
                        if (groupInfo == null) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_242));
                            return null;
                        }
                        GroupUser groupUser0 = requiredReload ? player.loadGroupUser(groupId) : player.getGroupUser();

                        if (groupUser0 == null) {
                            if (isGroupMatch) {
                                properties.put("errorMsg", "玩家[" + player.getName() + "]不是亲友圈成员无法创房");
                            } else {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_62));
                            }
                            return null;
                        } else if (groupUser0.getUserLevel() == null || groupUser0.getUserLevel().intValue() <= 0) {
                            if (isGroupMatch) {
                                properties.put("errorMsg", "玩家[" + player.getName() + "]已被禁止游戏，请联系群主");
                            } else {
                                player.writeErrMsg("您已被禁止游戏，请联系群主");
                            }
                            return null;
                        }
                        requiredReload = false;

                        if (groupInfo.getExtMsg() != null && !recreate) {
                            if (!isGroupMatch && groupInfo.getExtMsg().contains("-r")) {
                                if (groupUser0.getUserRole().intValue() >= 2 && !GameUtil.isPlayDtz(play)) {       //打筒子去掉普通成员开房限制
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_243));
                                    return null;
                                }
                            }
                            if ((payType == 1 || payType == 2) && groupInfo.getExtMsg().contains("+p3")) {
                                if (isGroupMatch) {
                                    properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_244));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_244));
                                }
                                return null;
                            }
                        }

                        if (payType == 1) {
                            if (tableCount > 1) {

                            } else {
                                if ((needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) && !GameConfigUtil.freeGame(play, player.getUserId())) {
                                    if (isGroupMatch) {
                                        properties.put("errorMsg", "玩家[" + player.getName() + "]" + LangHelp.getMsg(LangMsg.code_diamond_err));
                                    } else {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                                    }
                                    return null;
                                }
                            }
                            pay0 = needCard;

                            payMark = 1;
                        } else if (payType == 2 && !recreate) {
                            if (isGroupMatch) {
                                properties.put("errorMsg", "匹配模式不支持房主支付，请联系群主更改支付方式");
                                return null;
                            }

                            payMark = 2;
                            pay0 = needCard;
                            if (tableCount > 1) {
                                needCard = needCard * tableCount;
                                if ((needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) && !GameConfigUtil.freeGame(play, player.getUserId())) {
                                    if (autoCreate) {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                    } else {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                                    }
                                    return null;
                                }
                            } else {
                                if ((needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) && !GameConfigUtil.freeGame(play, player.getUserId())) {
                                    if (isGroupMatch) {
                                        properties.put("errorMsg", "玩家[" + player.getName() + "]" + LangHelp.getMsg(LangMsg.code_diamond_err));
                                    } else {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                                    }
                                    return null;
                                }
                            }
                        } else if (payType == 3 && !recreate) {

                            if (groupInfo.getExtMsg() == null || !groupInfo.getExtMsg().contains("+p3")) {
                                if (isGroupMatch) {
                                    properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_246));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_246));
                                }
                                return null;
                            }

                            //代付
                            payMark = 3;

                            if (!free) {
                                if (GameUtil.isPlayDtz(play)) {
                                    int score_max = StringUtil.getIntValue(params, 3, 600);
                                    needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 3, score_max);
                                } else {
                                    needCard = PayConfigUtil.get(play, bureauCount, table.calcPlayerCount(playerCount), 3);
                                }
                            }

                            pay0 = needCard;
                            needCard = needCard * (tableCount > 1 ? tableCount : 1);
                            GroupUser groupUser = GroupDao.getInstance().loadGroupMaster(groupId);
                            if (groupUser != null) {
                                if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))) {
                                    creatorId = player.getUserId();
                                } else {
                                    creatorId = groupUser.getUserId();
                                }
                                if (needCard < 0) {
                                    if (isGroupMatch) {
                                        properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                                    } else {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    }
                                    return null;
                                }
                                Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
                                if (player1 != null) {
                                    if (needCard > 0 && player1.getCards() + player1.getFreeCards() < needCard) {
                                        if (autoCreate) {
                                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                        } else {
                                            if (isGroupMatch) {
                                                properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                                            } else {
                                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                            }
                                        }
                                        return null;
                                    } else {
                                        payPlayer = player1;
//										player1.changeCards(0,-payValue,true,play);
                                    }
                                } else {
                                    RegInfo user = UserDao.getInstance().selectUserByUserId(groupUser.getUserId());
                                    if (user == null) {
                                        if (autoCreate) {
                                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                        } else {
                                            if (isGroupMatch) {
                                                properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                                            } else {
                                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                            }
                                        }
                                        return null;
                                    } else {
                                        if (needCard > 0 && user.getCards() + user.getFreeCards() < needCard) {
                                            if (autoCreate) {
                                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                            } else {
                                                if (isGroupMatch) {
                                                    properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                                                } else {
                                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                                }
                                            }
                                            return null;
                                        } else {
                                            Player player2 = ObjectUtil.newInstance(player.getClass());
                                            player2.loadFromDB(user);
                                            payPlayer = player2;
                                            payMark = 4;
                                        }
                                    }
                                }
                            } else {
                                if (autoCreate) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                } else {
                                    if (isGroupMatch) {
                                        properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                                    } else {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    }
                                }
                                return null;
                            }
                        }
                    } else {
                        if (PayConfigUtil.loadPayResourceType(play) == UserResourceType.TILI) {
                            if (needCard < 0 || needCard > 0 && (!GameConfigUtil.freeGame(play, player.getUserId())) && player.getUserTili() < needCard) {
                                if (isGroupMatch) {
                                    properties.put("errorMsg", "玩家[" + player.getName() + "]" + UserResourceType.TILI.getName() + "不足");
                                } else {
                                    player.writeErrMsg(UserResourceType.TILI.getName() + "不足");
                                }
                                return null;
                            }
                        } else {
                            if (needCard < 0 || needCard > 0 && (!GameConfigUtil.freeGame(play, player.getUserId())) && player.loadAllCards() < needCard) {
                                if (isGroupMatch) {
                                    properties.put("errorMsg", "玩家[" + player.getName() + "]" + LangHelp.getMsg(LangMsg.code_diamond_err));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                                }
                                return null;
                            }
                        }
                    }
                }
            }
        }

        // 如果创房的玩家非军团玩家则返回
        if (allowGroupMember > 0) {
            if (player.getIsGroup() <= 0 && groupId != null && requiredReload) {
                player.loadGroupUser(groupId);
                requiredReload = false;
            }
            if (player.getIsGroup() <= 0) {
                if (isGroupMatch) {
                    properties.put("errorMsg", "玩家[" + player.getName() + "]不是亲友圈成员");
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_52));
                }
                return null;
            }
        }
        if (!autoCreate) {
            // 智能补房，不强制
            player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        }

        int isOpenGps = 0;
        if (GameUtil.isPlayDdz(play)) {
            isOpenGps = StringUtil.getIntValue(params, 12, 0);
        }
//        if (GameUtil.isPlayDtz(play)) {
//            isOpenGps = StringUtil.getIntValue(params, 14, 0);
//        }
        if (isOpenGps == 1 && (StringUtils.isBlank(player.getMyExtend().getLatitudeLongitude()) || "error".equals(player.getMyExtend().getLatitudeLongitude()))) {
            LogUtil.msg("userId:" + player.getUserId() + " " + table + LangHelp.getMsg(LangMsg.code_200));
            if (isGroupMatch) {
                properties.put("errorMsg", "玩家[" + player.getName() + "]未开启GPS，创建房间失败");
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_200));
            }
            return null;
        }

        if (properties != null) {
            Object serverType = properties.get("serverType");
            Object serverKey = properties.get("serverKey");
            if (serverType != null) {
                table.setServerType((serverType instanceof Number) ? ((Number) serverType).intValue() : Integer.parseInt(String.valueOf(serverType)));
            }
            if (serverKey != null) {
                table.setServerKey(String.valueOf(serverKey));
            }
            table.changeExtend();
        }

        if (groupTableConfig != null) {
            table.setGroupTableConfig(groupTableConfig);
            table.changeExtend();
        }

        if (groupId != null) {
            table.setServerType(1);
            table.setServerKey("group" + groupId);
            table.changeExtend();
        }

        table.setGroupTable(groupTable);

        if (!recreate && groupId != null) {

            if (!isGroupMatch && (player.getGroupUser() == null || player.getGroupUser().getUserRole().intValue() >= 2)) {
                Integer count = GroupDao.getInstance().loadMyGroupTableCount(groupId, player.getUserId());
                if (count == null || count.intValue() > 0 && !("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa")))) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_247));
                    return null;
                }
            } else if (player.getGroupUser().getUserLevel() == null || player.getGroupUser().getUserLevel().intValue() <= 0) {
                if (isGroupMatch) {
                    properties.put("errorMsg", "玩家[" + player.getName() + "]已被禁止游戏，请联系群主");
                } else {
                    player.writeErrMsg("您已被禁止游戏，请联系群主");
                }
                return null;
            }

            Integer count = GroupDao.getInstance().loadGroupTableCount(groupId);
            if (count != null) {
                int tempCount = ResourcesConfigsUtil.getGroupTableCountLimit() - count.intValue();
                if (tempCount <= 0) {
                    if (isGroupMatch) {
                        properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_248));
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_248));
                    }
                    return null;
                } else {
                    if (tableCount > tempCount) {
                        tableCount = tempCount;
                    }
                }
            }
        }

        Object users = properties != null ? properties.get("groupMatchUsers") : null;

        List<Player> playerList = new ArrayList<>(table.getMaxPlayerCount());
        playerList.add(player);

        if (!recreate) {
            if (payMark == 1) {//AA支付
                if (users != null) {
                    String[] userIds = users.toString().split("_");
                    if (userIds.length >= 2) {
                        for (String userStr : userIds) {
                            long userId = Long.parseLong(userStr);
                            if (userId != player.getUserId()) {
                                Player player1 = PlayerManager.getInstance().getPlayer(userId);
                                if (player1 == null) {
                                    player1 = PlayerManager.getInstance().loadPlayer(userId, table.getPlayType());
                                }
                                if (pay0 > 0 && player1.loadAllCards() < pay0) {
                                    LogUtil.errorLog.info("group match create table fail:player cards too few,player={},cards={},pay={},userIds={},groupId={}"
                                            , player1.getUserId(), player1.loadAllCards(), pay0, users, groupId);

                                    if (isGroupMatch) {
                                        properties.put("errorMsg", "玩家[" + player1.getName() + "]" + LangHelp.getMsg(LangMsg.code_diamond_err));
                                    }
                                    return null;
                                }
                                playerList.add(player1);
                            }
                        }
                    }
                }
            } else {
                CardSourceType sourceType = table.getCardSourceType(payType);
                if (payMark == 2) {
                    needCard = pay0 * tableCount;

                    //相当于房主支付、代开
                    if (!player.changeCards(0, -needCard, true, play, false, sourceType)) {
                        if (isGroupMatch) {
                            properties.put("errorMsg", "玩家[" + player.getName() + "]" + LangHelp.getMsg(LangMsg.code_diamond_err));
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        }
                        return null;
                    }
                    table.setCreatorId(player.getUserId());
                } else if (payMark == 3) {
                    needCard = pay0 * tableCount;
                    //相当于军团长支付
                    if (!payPlayer.changeCards(0, -needCard, true, play, false, sourceType)) {
                        if (isGroupMatch) {
                            properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                        }
                        return null;
                    }
                    table.setCreatorId(payPlayer.getUserId());
                } else if (payMark == 4) {
                    //相当于军团长支付
                    needCard = pay0 * tableCount;
                    if (!payPlayer.changeCards(0, -needCard, true, play, false, sourceType)) {
                        if (isGroupMatch) {
                            properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_50));
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                        }
                        return null;
                    }
                    table.setCreatorId(payPlayer.getUserId());
                    int c1 = 0, c2 = 0;
                    if (payPlayer.getFreeCards() > 0) {
                        if (payPlayer.getFreeCards() >= needCard) {
                            c1 = needCard;
                        } else {
                            c1 = (int) payPlayer.getFreeCards();
                        }
                    }
                    if (c1 < needCard) {
                        c2 = needCard - c1;
                    }

                    if (payPlayer.getEnterServer() > 0) {
                        Server server1 = ServerManager.loadServer(payPlayer.getEnterServer());
                        if (server1 != null) {
                            String url = server1.getIntranet();
                            if (StringUtils.isBlank(url)) {
                                url = server1.getHost();
                            }

                            if (StringUtils.isNotBlank(url)) {
                                int idx = url.indexOf(".");
                                if (idx > 0) {
                                    idx = url.indexOf("/", idx);
                                    if (idx > 0) {
                                        url = url.substring(0, idx);
                                    }
                                    url += "/online/notice.do?type=playerCards&userId=" + payPlayer.getUserId();
                                    if (c2 > 0) {
                                        String noticeRet = HttpUtil.getUrlReturnValue(url + "&message=-" + c2, 2);
                                        LogUtil.msgLog.info("notice result:url=" + url + ",ret=" + noticeRet);
                                    }
                                    if (c1 > 0) {
                                        String noticeRet = HttpUtil.getUrlReturnValue(url + "&free=1&message=-" + c1, 2);
                                        LogUtil.msgLog.info("notice result:url=" + url + ",ret=" + noticeRet);
                                    }
                                }
                            }
                        }
                    }
                }

                if (users != null) {
                    String[] userIds = users.toString().split("_");
                    if (userIds.length >= 2) {
                        for (String userStr : userIds) {
                            long userId = Long.parseLong(userStr);
                            if (userId != player.getUserId()) {
                                Player player1 = PlayerManager.getInstance().getPlayer(userId);
                                if (player1 == null) {
                                    player1 = PlayerManager.getInstance().loadPlayer(userId, table.getPlayType());
                                }
                                playerList.add(player1);
                            }
                        }
                    }
                }
            }
        }

        List<BaseTable> tableList = new ArrayList<>(tableCount);
        if (maxTableCount > 1) {
            for (int i = 0; i < tableCount; i++) {
                try {
                    BaseTable baseTable = TableManager.getInstance().getInstanceTable(play);
                    Server server = ServerManager.loadServer(play, 1);
                    int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;

                    long tableId = generateId(player.getUserId(), play, 1, serverId);

                    if (tableId > 0) {
                        baseTable.setId(tableId);
                        baseTable.setServerId(serverId);
                        tableList.add(baseTable);
                    }
                } catch (Throwable t) {
                    LogUtil.errorLog.error("Exception:" + t.getMessage(), t);
                }
            }
        } else {
            table.createTable(player, play, bureauCount, params, strParams);
            if (table.getId() <= 0) {
                if (isGroupMatch) {
                    properties.put("errorMsg", LangHelp.getMsg(LangMsg.code_0));
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_0));
                }
                return null;
            }
            table.setIntParams(params);
            table.setStrParams(strParams);
            tableList.add(table);

            if (groupTable != null) {
                int room = groupTable.loadGroupRoom();
                if (room > 0) {
                    table.setServerKey("group" + groupTable.getGroupId() + "_" + groupTable.getKeyId() + "_" + room);
                } else {
                    table.setServerKey("group" + groupTable.getGroupId() + "_" + groupTable.getKeyId());
                }

                table.changeExtend();
            }
        }

        if (StringUtils.isNotBlank(groupId)) {
            if (table.getAllowGroupMember() > 0) {
                if (player.getGroupUser() != null && groupId.equals(player.getGroupUser().getGroupId().toString())) {
                } else {
                    player.loadGroupUser(groupId);
                }
                table.setAllowGroupMember(Integer.parseInt(groupId));
                table.changeExtend();
            }
        }

        if (creatorId > 0) {
            table.setCreatorId(creatorId);
        }
        if (GameServerConfig.isDebug()) {
            if (ShutDownAction.testWanFa == play) {
                table.setZp(ShutDownAction.testPai);
            }
        }

        if (maxTableCount == 1) {
            LogUtil.msgLog.info("create table:tableId={},master={},userId={},creatorId:{}", table.getId(), table.getMasterId(), player.getUserId(), creatorId);
        }

        if (groupTable != null) {
            if (!recreate) {
                table.initCreditMsg(groupTableConfig.getCreditMsg());
                table.setRoomName(groupTableConfig.getTableName());
                table.initGroupConfig(groupTable.getGroupId());
                groupTable.setConfigId(groupTableConfig.getKeyId());
                groupTable.setCreatedTime(new Date());
                groupTable.setCurrentCount(0);
                groupTable.setCurrentState("0");
                groupTable.setGroupId(groupTableConfig.getParentGroup().longValue() == 0 ? groupTableConfig.getGroupId() : groupTableConfig.getParentGroup());
                groupTable.setTableName(groupTableConfig.getTableName());
                groupTable.setMaxCount(groupTableConfig.getPlayerCount());
                groupTable.setServerId(String.valueOf(GameServerConfig.SERVER_ID));
                groupTable.setTableId(Long.valueOf(table.getId()).intValue());
                groupTable.setTableMsg(groupTableConfig.getModeMsg());
                groupTable.setType(table.getCreditMode() == 1 ? 2 : 1);
                groupTable.setCreditMsg(groupTableConfig.getCreditMsg());
                Long tableKeyId = GroupDao.getInstance().createGroupTable(groupTable);
                groupTable.setKeyId(tableKeyId);

                table.setGroupTable(groupTable);
                table.setServerKey(tableKeyId.toString());
                table.setGroupTableConfig(groupTableConfig);
                table.changeExtend();
            } else if (recreate) {
                table.initCreditMsg(groupTable.getCreditMsg());
                table.setRoomName(groupTable.getTableName());
                table.initGroupConfig(groupTable.getGroupId());
                String tableMsg = groupTable.getTableMsg();
                if (StringUtils.isNotBlank(tableMsg)) {
                    JsonWrapper jsonWrapper = new JsonWrapper(tableMsg);
                    String strs = jsonWrapper.getString("strs");
                    if (StringUtils.isNotBlank(strs)) {
                        String[] tempStrs = strs.split(";")[0].split("_");
                        if (tempStrs.length >= 4) {
                            if ("2".equals(tempStrs[0]) || "3".equals(tempStrs[0])) {
                                table.setCreatorId(Long.valueOf(tempStrs[2]));
                            } else {
                                table.setCreatorId(NumberUtils.toLong(groupTable.getUserId(), 0));
                            }
                        }
                    }
                }
            }
        }

        if (maxTableCount == 1) {
            TableManager.getInstance().addTable(table);
        }

        if (groupId != null && !recreate) {
            List<GroupTable> gtList = new ArrayList<>(tableList.size());
            for (BaseTable baseTable : tableList) {
                GroupTable gt = new GroupTable();
                gt.setUserId(String.valueOf(creatorId > 0L ? creatorId : player.getUserId()));
                gt.setCurrentCount(0);
                gt.setConfigId(modeVal == null ? 0L : modeVal);
                gt.setCreatedTime(new Date());
                gt.setCurrentState("0");
                gt.setGroupId(NumberUtils.toLong(groupId));
                GroupTableConfig groupTableConfig0 = null;
                if (modeId.length() > 0 && NumberUtils.isDigits(modeId) && Integer.parseInt(modeId) > 0 && !recreate) {
                    groupTableConfig0 = groupTableConfig != null ? groupTableConfig : GroupDao.getInstance().loadGroupTableConfig(Long.parseLong(modeId));
                }
                gt.setTableName(groupTableConfig0 != null ? groupTableConfig0.getTableName() : "");
                gt.setMaxCount(baseTable.calcPlayerCount(playerCount));
                gt.setServerId(String.valueOf(baseTable.getServerId()));
                gt.setTableId((int) baseTable.getId());
                gt.setPlayedBureau(0);

                JsonWrapper jsonWrapper = new JsonWrapper("");
                jsonWrapper.putString("ints", StringUtil.implode(params, ","));
                jsonWrapper.putString("strs", new StringBuilder().append(payType).append("_").append(player.getUserId()).append("_").append(payPlayer == null ? player.getUserId() : payPlayer.getUserId()).append("_").append(pay0).append(";").append(StringUtil.implode(strParams, ",")).toString());
                jsonWrapper.putString("props", tableVisible);
                jsonWrapper.putInt("type", baseTable.getPlayType());
                if (isGroupMatch) {
                    jsonWrapper.putString("match", "1");
                }
                if (groupRoom > 0) {
                    jsonWrapper.putInt("room", groupRoom);
                }

                gt.setTableMsg(jsonWrapper.toString());
                baseTable.initCreditMsg(groupTableConfig0 != null ? groupTableConfig0.getCreditMsg() : "");
                baseTable.setRoomName(groupTableConfig0 != null ? groupTableConfig0.getTableName():"");
                table.initGroupConfig(NumberUtils.toLong(groupId));
                boolean isCreditTable = baseTable.getCreditMode() == 1;
                if (!isCreditTable) {
                    //兼容旧版本
                    BaseTable tmpTable = TableManager.getInstance().getInstanceTable(play);
                    isCreditTable = tmpTable != null && tmpTable.isCreditTable(params);
                }
                gt.setType(isCreditTable ? 2 : 1);
                gt.setCreditMsg(groupTableConfig0 != null ? groupTableConfig0.getCreditMsg() : "");
                Long groupKey = GroupDao.getInstance().createGroupTable(gt);
                gt.setKeyId(groupKey);
                if (groupRoom > 0) {
                    baseTable.setServerKey("group" + groupId + "_" + groupKey + "_" + groupRoom);
                } else {
                    baseTable.setServerKey("group" + groupId + "_" + groupKey);
                }

                baseTable.changeExtend();

                gtList.add(gt);

                LogUtil.msgLog.info("create group table:userId={},msg={}", player.getUserId(), JacksonUtil.writeValueAsString(gt));
            }

            if (Redis.isConnected() && gtList.size() > 0) {
                Map<String, Double> gtMap = new HashMap<>();
                Map<String, String> gtMap0 = new HashMap<>();
                for (GroupTable gt : gtList) {
                    if (gt.getKeyId() != null) {
                        gtMap.put(gt.getKeyId().toString(), Double.valueOf(GroupRoomUtil.loadWeight(gt.getCurrentState(), gt.getCurrentCount(), gt.getCreatedTime())));
                        gtMap0.put(gt.getKeyId().toString(), JSON.toJSONString(gt));
                    }
                }
                if (gtMap.size() > 0) {
                	
                	
                    RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupId, groupRoom), gtMap);
                    
                    Map<String, String> groupRooms =   RedisUtil.hgetAll(GroupRoomUtil.loadGroupKey(groupId, 0));
                    if(groupRooms == null) {
                    	groupRooms = new HashMap<String,String>();
                    }
                    
                    groupRooms.put(groupRoom+"", "0");
                    //存储开了房的包厢id
                    RedisUtil.hmset(GroupRoomUtil.loadGroupTableKey(groupId, 0), groupRooms);
                    
                    RedisUtil.hmset(GroupRoomUtil.loadGroupTableKey(groupId, groupRoom), gtMap0);
                }
            }

            if (maxTableCount > 1) {
//                player.writeComMessage(WebSocketMsgType.MULTI_CREATE_TABLE, 1, play);
                return null;
            }
        }

        //设置语音房间ID
        long gotyeRoomId = GotyeChatManager.getInstance().loadGotyeRoomId(table.getId(), table.getPlayType(), player.getOs(), player.getVc());
        if (gotyeRoomId > 0) {
            table.setGotyeRoomId(gotyeRoomId);
            table.changeExtend();
        }

        // 牌桌进入准备阶段
        table.changeTableState(table_state.ready);

        for (int i = 0, len = playerList.size(); i < len; i++) {
            Player player1 = playerList.get(i);
            Player player2 = PlayerManager.getInstance().changePlayer(player1, table.getPlayerClass());
            if (player1 != player2) {
                playerList.set(i, player2);
            }
            if (!table.joinPlayer(player2)) {
                table.updateDaikaiTableInfo();
                LogUtil.errorLog.info("createTable joinPlayer fail:tableId={},userId={}", table.getId(), player2.getUserId());
                return null;
            }
            if (isGroupMatch) {
                player2.getMyExtend().setGroupMatch(false);
                player2.setPlayingTableId(table.getId());
                player2.setEnterServer(table.getServerId());
                player2.saveBaseInfo();
            }
        }

        // 房主创建房间自动准备
        if (table.isGroupRoom()) {
            if (playerList.size() == 1) {
                if (table.autoReadyForFirstPlayerOfGroup()) {
                    table.ready(playerList.get(0));
                }
            } else {
                if (!table.allowChooseSeat()) {
                    for (Player player1 : playerList)
                        table.ready(player1);
                }
            }
        } else {
            if (playerList.size() == 1) {
                if (table.autoReadyForFirstPlayerOfCommon()) {
                    table.ready(playerList.get(0));
                }
            } else {
                if (!table.allowChooseSeat()) {
                    for (Player player1 : playerList)
                        table.ready(player1);
                }
            }
        }

        table.updateDaikaiTableInfo();

        if (table.isGoldRoom()) {
            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
        }

        for (Player player1 : playerList) {
            player1.writeSocket(table.buildCreateTableRes(player1.getUserId()));
        }

        for (Map.Entry<Long, Player> kv : table.getRoomPlayerMap().entrySet()) {
            long currentUserId = kv.getKey().longValue();
            if (currentUserId != player.getUserId() && !table.getPlayerMap().containsKey(kv.getKey()) && kv.getValue().getSeat() > 0) {
                TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
                TableRes.PlayerInTableRes.Builder builder = kv.getValue().buildPlayInTableInfo();
                if (builder == null) {
                    continue;
                }
                joinRes.setPlayer(builder);
                //玩法
                joinRes.setWanfa(table.getPlayType());
                GeneratedMessage msg1 = joinRes.build();
                for (Player tablePlayer : table.getSeatMap().values()) {
                    //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                    tablePlayer.writeSocket(msg1);
                }

                for (Player player0 : table.getRoomPlayerMap().values()) {
                    if (player0.getUserId() != currentUserId) {
                        if ((GameUtil.isPlayThreeMonkeys(table.getPlayType()) || GameUtil.isPlayTenthirty(table.getPlayType())) && player.getUserId() != player0.getUserId()) {
                            player0.writeSocket(table.buildCreateTableRes(player0.getUserId()));
                        } else {
                            player0.writeSocket(msg1);
                        }
                    }
                }
            }
        }

        player.sendActionLog(LogConstants.reason_createtable, "tableId:" + table.getId());

        if (table.isTest()) {
            // 加入牌桌
            int playerCountId = PlayerManager.robotId - table.getMaxPlayerCount() + 1;
            for (int i = PlayerManager.robotId - 1; i >= playerCountId; i--) {
                Player robot1 = PlayerManager.getInstance().getRobot(i, play);
                table.joinPlayer(robot1);
                table.ready(robot1);

                JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
                TableRes.PlayerInTableRes.Builder builder = robot1.buildPlayInTableInfo();
                if (builder == null) {
                    continue;
                }
                joinRes.setPlayer(builder);
                joinRes.setWanfa(table.getPlayType());
                player.writeSocket(joinRes.build());
                // ////////////////////////////////////////////////////////////
            }
            table.setRuning(true);
            // 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
            table.ready();
            table.checkDeal();

        } else {
            boolean isRun = playerList.size() == table.getMaxPlayerCount();
            table.setRuning(isRun);
            if (isRun) {
                table.ready();
                table.checkDeal();
            }
        }
        table.sendPlayerStatusMsg();
        if (table.isKaiYiJu()) {
            AssisServlet.sendRoomStatus(table, "0");
        }
        return table;
    }

    public static Class<? extends Player> getPlayerByCode(int code) {
        return msgPlayerTypes.get(Integer.valueOf(code));
    }

    public BaseTable reconnect(BaseTable table, Player player) {
        synchronized (table) {
            if (table.getPlayerMap().isEmpty() && table.getRoomPlayerMap().isEmpty()) {
                table.diss();
                player.setPlayingTableId(0);
                player.saveBaseInfo();
                player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_8, table.getId()), WebSocketMsgType.sc_code_err_table);
                return null;
            }
            if (!table.checkPlayer(player)) {
                LogUtil.e("check player err:" + player.getUserId());
                player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_15, player.getPlayingTableId()), WebSocketMsgType.sc_code_err_table);
                return null;
            }

            table.ready();

            table.checkDeal();
            table.broadIsOnlineMsg(player, SharedConstants.table_online);
            LogUtil.d_msg("table-->" + table.getId() + " " + table.getClass().getName());

            if (GameUtil.isPlayBaiRenWanfa(table.getWanFa())) {
                BaiRenTableRes res = table.buildBaiRenTableRes(player.getUserId(), true, false);
                player.writeSocket(res);
            } else {
                CreateTableRes res = table.buildCreateTableRes(player.getUserId(), true, false);
                player.writeSocket(res);
            }
            table.checkDiss();
            table.checkReconnect(player);
            table.checkSendDissMsg(player);
            table.sendPlayerStatusMsg();
            table.broadOnlineStateMsg();
            return null;
        }
    }

    public void checkAutoQuit(){
        if (!SharedConstants.isAutoQuit()) {
            return;
        }
        BaseTable[] tables = tableMap.values().toArray(new BaseTable[0]);
        int count = tables.length;
        if (count > 0) {
            long startTime = System.currentTimeMillis();
            CountDownLatch countDownLatch = new CountDownLatch(count);
            for (BaseTable table : tables) {
                executeAutoQuit(countDownLatch, table);
            }
            boolean isOk = false;
            try {
                isOk = countDownLatch.await(AUTO_TASK_TIMEOUT, TimeUnit.SECONDS);
            } catch (Throwable e) {
                LogUtil.errorLog.error("Exception:count=" + countDownLatch.getCount() + ",msg=" + e.getMessage(), e);
            } finally {
                if (System.currentTimeMillis() - startTime > 50) {
                    LogUtil.msgLog.info("checkAutoQuit|" + count + "|" + (System.currentTimeMillis() - startTime) + "|" + isOk);
                }
            }
        }
    }

    private static void executeAutoQuit(final CountDownLatch countDownLatch, final BaseTable table) {
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable(){
            @Override
            public void run() {
                int referenceCount;
                if ((referenceCount = table.getReferenceCounter().get()) >= 1 || referenceCount < 0) {
                    countDownLatch.countDown();
                    LogUtil.errorLog.warn("table is dead ? tableId={},referenceCount={},players={}", table.getId(), referenceCount, table.getPlayerMap().keySet());
                } else {
                    table.getReferenceCounter().addAndGet(1);
                    try {
                        table.checkAutoQuit();
                    } catch (Throwable e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                    table.getReferenceCounter().addAndGet(-1);
                    countDownLatch.countDown();
                }
            }
        });
    }

    /**
     * 俱乐部暂停开房
     *
     * @return
     */
    public static boolean isStopCreateGroupRoom(GroupInfo group) {
        if (group != null && StringUtils.isNotBlank(group.getExtMsg())) {
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String stopCreate = json.getString("stopCreate");
            if ("1".equals(stopCreate)) {
                return true;
            }
        }
        return false;
    }

}
