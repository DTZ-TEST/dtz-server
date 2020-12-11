package com.sy599.game.util;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.GoldPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class GoldRoomUtil {

    public final static boolean isGoldRoom(Player player) {
        return isGoldRoom(player.getPlayingTableId());
    }

    public final static boolean isGoldRoom(long id) {
        return id >= Constants.MIN_GOLD_ID;
    }

    public final static boolean isNotGoldRoom(Player player) {
        return isNotGoldRoom(player.getPlayingTableId());
    }

    public final static boolean isNotGoldRoom(long id) {
        return id < Constants.MIN_GOLD_ID;
    }

    public final static GoldRoom joinGoldRoom(Player player, int gameType, int serverType, String modeId, String matchType, MatchBean matchBean, boolean newRoom, GoldRoom goldRoom) throws Exception {
        List<Integer> intsList;
        List<String> strsList;

        if (matchBean == null) {
            intsList = GameConfigUtil.getIntsList(serverType, modeId);//minGold,maxGold,gameCount,maxCount
            strsList = GameConfigUtil.getStringsList(serverType, modeId);

            if ((intsList == null || intsList.size() <= 4) && (strsList == null || strsList.size() == 0)) {
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 0, "暂未开放");
                LogUtil.e("gold room mode not found-->serverType=" + serverType + ",modeId=" + modeId + ",-->userId:" + player.getUserId());
                return null;
            }
            intsList = new ArrayList<>(intsList);
            int minGold = intsList.remove(0);
            int maxGold = intsList.remove(0);

            int pay = PayConfigUtil.get(gameType, intsList.get(0), intsList.get(1), 0, modeId);

            GoldPlayer goldPlayer = player.getGoldPlayer();
            if (goldPlayer == null) {
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 0, "暂未开放");
                LogUtil.msgLog.info("goldPlayer is null");
                return null;
            } else {
                long gold = goldPlayer.getAllGold();
                if (pay < 0) {
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 0, "暂未开放");
                    return null;
                } else {
                    if (gold < pay || gold < minGold) {
                        String modeIdStr = String.valueOf(modeId);
                        int minPay;
                        if (!modeIdStr.endsWith("1")) {
                            minPay = PayConfigUtil.get(gameType, intsList.get(0), intsList.get(1), 0, modeIdStr.substring(0, modeIdStr.length() - 1) + "1");
                        } else {
                            minPay = pay;
                        }

                        if (minPay < 0 || gold >= minPay) {
                            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 1, ResourcesConfigsUtil.loadServerPropertyValue("gold_tip_pay", "金币不足，请充值！"));
                        } else {
                            Date date = new Date();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                            int freeCount = GoldDao.getInstance().selectRemedyCount(sdf.format(date), player.getUserId());
                            String remedyConfig = ResourcesConfigsUtil.loadServerPropertyValue("gold_remedy_count", "0");
                            if (StringUtils.isBlank(remedyConfig)) {
                                remedyConfig = "0";
                            }
                            String[] remedys = remedyConfig.split(",");

                            if (freeCount >= remedys.length) {
                                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 1, ResourcesConfigsUtil.loadServerPropertyValue("gold_tip_pay", "金币不足，请充值！"));
                            } else {
                                int c = NumberUtils.toInt(remedys[freeCount], 0);
                                if (c > 0) {
                                    GoldDao.getInstance().drawRemedy(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date), player.getUserId(), c);
                                    player.changeGold(c, 0, gameType);
                                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 2, ResourcesConfigsUtil.loadServerPropertyValue("gold_tip_give", "您已破产，系统自动赠送您{0}金币！").replace("{0}", String.valueOf(c)));
                                } else {
                                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 1, ResourcesConfigsUtil.loadServerPropertyValue("gold_tip_pay", "金币不足，请充值！"));
                                }
                            }
                        }

                        return null;
                    }

                    if (maxGold > 0 && gold > maxGold) {
                        player.writeComMessage(WebSocketMsgType.GOLD_JOIN_TIP, 3, "场次不适合");
                        return null;
                    }
                }
            }
        } else {
            modeId = "match" + matchBean.getKeyId().toString();
            intsList = matchBean.loadTableInts();
            strsList = matchBean.loadTableStrings();
        }

        synchronized (Constants.GOLD_LOCK) {
            if (goldRoom == null && !newRoom) {
                if (GoldRoomUtil.isGoldRoom(player.getPlayingTableId())) {
                    goldRoom = GoldRoomDao.getInstance().loadGoldRoom(player.getPlayingTableId());
                    if (goldRoom == null || goldRoom.isOver()) {
                        goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(modeId, GameServerConfig.SERVER_ID);
                    }
                } else {
                    goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(modeId, GameServerConfig.SERVER_ID);
                }
            }
            if (matchBean == null && "2".equals(matchType)) {
                return joinGoldRoom2(player, modeId, intsList, strsList, goldRoom);
            } else {
                return joinGoldRoom1(player, gameType, modeId, intsList, strsList, goldRoom, matchBean);
            }
        }
    }

    /**
     * 匹配金币场1
     */
    private final static GoldRoom joinGoldRoom1(Player player, int gameType, String modeId, List<Integer> intsList, List<String> strsList, GoldRoom goldRoom, MatchBean matchBean) throws Exception {
        if (goldRoom == null) {
            goldRoom = newGoldRoom(modeId, intsList, strsList, matchBean);
            Long roomId = GoldRoomDao.getInstance().saveGoldRoom(goldRoom);
            if (roomId != null && roomId.longValue() > 0) {
                goldRoom.setKeyId(roomId);
                LogUtil.msgLog.info("create gold room:room=" + JacksonUtil.writeValueAsString(goldRoom));

                GoldRoomUser goldRoomUser = newGoldRoomUser(player, goldRoom);
                Long keyId = GoldRoomDao.getInstance().saveGoldRoomUser(goldRoomUser);
                goldRoomUser.setKeyId(keyId);

                if (keyId != null && keyId.longValue() > 0) {
                    LogUtil.msgLog.info("create gold room user:user=" + JacksonUtil.writeValueAsString(goldRoomUser));
                } else {
                    GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, null);

                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }
            } else {
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            }
        } else {
            GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(goldRoom.getKeyId(), player.getUserId());
            if (goldRoomUser == null) {
                goldRoomUser = new GoldRoomUser();
                goldRoomUser.setCreatedTime(new Date());
                goldRoomUser.setGameResult(0);
                goldRoomUser.setLogIds("");
                goldRoomUser.setRoomId(goldRoom.getKeyId());
                goldRoomUser.setUserId(String.valueOf(player.getUserId()));

                Long keyId = GoldRoomDao.getInstance().saveGoldRoomUser(goldRoomUser);
                goldRoomUser.setKeyId(keyId);

                if (keyId != null && keyId.longValue() > 0) {
                    int ret = GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), 1, null);
                    LogUtil.msgLog.info("create gold room user:user=" + JacksonUtil.writeValueAsString(goldRoomUser) + ",result=" + ret);

                    if (ret > 0) {
                        goldRoom.setCurrentCount(goldRoom.getCurrentCount() + 1);
                    } else {
                        GoldRoomDao.getInstance().deleteGoldRoomUser(goldRoom.getKeyId(), player.getUserId());
                        player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                        return null;
                    }
                } else {
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }
            }
        }

        player.setPlayingTableId(goldRoom.getKeyId());
        player.setEnterServer(goldRoom.getServerId());
        player.saveBaseInfo();
        if (goldRoom.getCurrentCount().intValue() == goldRoom.getMaxCount().intValue() && goldRoom.isNotStart()) {
            //开始游戏
            GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), 0, "1");
            goldRoom.setCurrentState("1");
            List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsers(goldRoom.getKeyId());
            List<Player> playerList = new ArrayList<>(list.size());
            for (HashMap<String, Object> map : list) {
                long tempUserId = Long.parseLong(map.get("userId").toString());
                Player tempPlayer = PlayerManager.getInstance().getPlayer(tempUserId);
                if (tempPlayer == null) {
                    tempPlayer = PlayerManager.getInstance().loadPlayer(tempUserId, gameType);
                    tempPlayer.setIsOnline(0);
                    tempPlayer.setIsEntryTable(SharedConstants.table_offline);
                    LogUtil.msgLog.info("gold user online?false:userId=" + tempUserId);
                }
                playerList.add(tempPlayer);
            }

            JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
            final BaseTable table1 = TableManager.getInstance().createSimpleTable(player, StringUtil.explodeToIntList(json.getString("ints"), ",")
                    , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, matchBean);

            if (table1 != null) {
                if (matchBean != null)
                    table1.setMatchId(matchBean.getKeyId());
                //随机房主
                Player master;

                String masterMark = ResourcesConfigsUtil.loadServerPropertyValue(new StringBuilder(28).append("gold_room_master_").append(goldRoom.getModeId()).toString());

                if ("lastWinner".equals(masterMark) || "lastLoser".equals(masterMark)) {
                    List<String> userIds = new ArrayList<>(playerList.size());
                    for (Player player1 : playerList) {
                        userIds.add(String.valueOf(player1.getUserId()));
                    }
                    List<HashMap<String, Object>> mapList = GoldRoomDao.getInstance().loadGoldRoomMsgs(userIds);
                    if (mapList != null && mapList.size() > 0) {
                        for (HashMap<String, Object> map : mapList) {
                            int gameResult = Integer.parseInt(map.getOrDefault("gameResult", 0).toString());
                            String userId = map.getOrDefault("userId", 0).toString();
                            if ("lastWinner".equals(masterMark)) {
                                if (gameResult <= 0) {
                                    userIds.remove(userId);
                                }
                            } else {
                                if (gameResult >= 0) {
                                    userIds.remove(userId);
                                }
                            }
                        }
                    }

                    if (userIds.size() == playerList.size() || userIds.size() == 0) {
                        master = playerList.remove(new SecureRandom().nextInt(playerList.size()));
                    } else {
                        String mUserId = userIds.get(new SecureRandom().nextInt(userIds.size()));
                        int i = 0, len = playerList.size();
                        for (; i < len; i++) {
                            if (mUserId.equals(String.valueOf(playerList.get(i).getUserId()))) {
                                break;
                            }
                        }
                        master = playerList.remove(i >= len ? 0 : i);
                    }
                } else {
                    master = playerList.remove(new SecureRandom().nextInt(playerList.size()));
                }

                playerList.add(0, master);

                // 牌桌进入准备阶段
                table1.changeTableState(SharedConstants.table_state.ready);
                if (TableManager.getInstance().addTable(table1) == 1) {
                    table1.saveSimpleTable();
                }

                table1.setMasterId(master.getUserId());
                table1.setModeId(goldRoom.getModeId());
                table1.setMatchId(matchBean == null ? null : matchBean.getKeyId());
                table1.setMatchRatio(goldRoom.loadMatchRatio());
                table1.changeExtend();

                LogUtil.msgLog.info("create gold room table:tableId={},master={},playType={},modeId={}", table1.getId(), master.getUserId(), table1.getPlayType(), goldRoom.getModeId());

                List<Player> tablePlayers = new ArrayList<>(playerList.size());
                for (Player player1 : playerList) {
                    Player player2 = PlayerManager.getInstance().changePlayer(player1, table1.getPlayerClass());

                    // 加入牌桌
                    if (!table1.joinPlayer(player2)) {
                        continue;
                    } else {
                        tablePlayers.add(player2);
                    }
                    if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue(new StringBuilder("gold_room_ready").append(goldRoom.getModeId()).toString(), "1"))) {
                        table1.ready(player2);
                    }
                    player2.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                }

                Map<Long, GeneratedMessage> msgMap = new HashMap<>();
                //先发给自己
                for (Player player2 : tablePlayers) {
                    TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
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
                table1.broadOnlineStateMsg();

                int delay = matchBean != null ? matchBean.loadExtFieldIntVal("delayFp"):-1;
                if (delay == 0){
                    delay = 2;
                }
                if (delay>0) {
                    TaskExecutor.delayExecutor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            table1.checkDeal();
                            table1.startNext();
                        }
                    }, delay, TimeUnit.SECONDS);
                } else {
                    table1.checkDeal();
                    table1.startNext();
                }
            } else {
                GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), 0, "2");
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            }
        } else {
            //等待开始游戏
            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);

            JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
            BaseTable table1 = TableManager.getInstance().createSimpleTable(player, StringUtil.explodeToIntList(json.getString("ints"), ",")
                    , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, matchBean);

            if (table1 != null) {
                TableManager.getInstance().addGoldTable(table1);
            }
        }
        return goldRoom;
}

    /**
     * 匹配金币场2
     */
    private final static GoldRoom joinGoldRoom2(Player player, String modeId, List<Integer> intsList, List<String> strsList, GoldRoom goldRoom) throws Exception {
        if (goldRoom == null) {
            // 查找已开局未满人房
            goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(modeId, GameServerConfig.SERVER_ID, "1");
        }
        if (goldRoom == null) {
            // 没查到符合条件的房，新创一个
            goldRoom = newGoldRoom(modeId, intsList, strsList, null);
            Long roomId = GoldRoomDao.getInstance().saveGoldRoom(goldRoom);
            if (roomId != null && roomId.longValue() > 0) {
                goldRoom.setKeyId(roomId);
                LogUtil.msgLog.info("create gold room:room=" + JacksonUtil.writeValueAsString(goldRoom));
                // 创建金币场玩家信息
                GoldRoomUser goldRoomUser = newGoldRoomUser(player, goldRoom);
                Long keyId = GoldRoomDao.getInstance().saveGoldRoomUser(goldRoomUser);
                goldRoomUser.setKeyId(keyId);
                if (keyId != null && keyId.longValue() > 0) {
                    LogUtil.msgLog.info("create gold room user:user=" + JacksonUtil.writeValueAsString(goldRoomUser));
                } else {
                    GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, null);
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }

                // 创建桌子
                BaseTable table = TableManager.getInstance().createSimpleTable(player, intsList, strsList, goldRoom, JjsUtil.loadMatch(goldRoom));
                if (table == null) {
                    GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, "2");
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                } else {
                    // 牌桌进入准备阶段
                    table.changeTableState(SharedConstants.table_state.ready);
                    TableManager.getInstance().addTable(table);
                    player.sendActionLog(LogConstants.reason_createtable, "tableId:" + table.getId());
                    table.ready(player);
                    // 加入牌桌
                    if (!table.joinPlayer(player)) {
                        GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, "2");
                        player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                        return null;
                    }
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                    player.setPlayingTableId(goldRoom.getKeyId());
                    player.setEnterServer(goldRoom.getServerId());
                    player.saveBaseInfo();

                    return goldRoom;
                }
            } else {
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            }
        } else {
            // 找到符合条件的房
            BaseTable table = TableManager.getInstance().getTable(goldRoom.getKeyId());
            if (table == null) {
                GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, "2");
                LogUtil.e("join gold room err-->myServerId:" + GameServerConfig.SERVER_ID + ",roomServerId:" + goldRoom.getServerId());
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            } else {
                player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
                // 加入牌桌
                if (!table.joinPlayer(player)) {
                    player.setPlayingTableId(0);
                    player.saveBaseInfo();
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));

                TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
                joinRes.setPlayer(player.buildPlayInTableInfo());
                //玩法
                joinRes.setWanfa(table.getPlayType());
                for (Player player1 : table.getSeatMap().values()) {
                    //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                    if (player1.getUserId() != player.getUserId()) {
                        player1.writeSocket(joinRes.build());
                    }
                }
                for (Player player1 : table.getRoomPlayerMap().values()) {
                    if (player1.getUserId() != player.getUserId()) {
                        player1.writeSocket(joinRes.build());
                    }
                }
            }
        }
        return goldRoom;
    }

    private final static GoldRoomUser newGoldRoomUser(Player player, GoldRoom goldRoom) {
        GoldRoomUser goldRoomUser = new GoldRoomUser();
        goldRoomUser.setCreatedTime(new Date());
        goldRoomUser.setGameResult(0);
        goldRoomUser.setLogIds("");
        goldRoomUser.setRoomId(goldRoom.getKeyId());
        goldRoomUser.setUserId(String.valueOf(player.getUserId()));
        return goldRoomUser;
    }

    private final static GoldRoom newGoldRoom(String modeId, List<Integer> intsList, List<String> strsList, MatchBean matchBean) {
        GoldRoom goldRoom;
        goldRoom = new GoldRoom();
        goldRoom.setCreatedTime(new Date());
        goldRoom.setCurrentCount(1);
        goldRoom.setCurrentState("0");

        if (matchBean == null) {
            goldRoom.setGameCount(intsList.remove(0));
            goldRoom.setMaxCount(intsList.remove(0));
            goldRoom.setModeId(modeId);
        } else {
            goldRoom.setGameCount(JjsUtil.loadMatchCurrentGameCount(matchBean));
            goldRoom.setMaxCount(matchBean.getTableCount());
            goldRoom.setModeId(("match" + matchBean.getKeyId().toString()));
        }

        goldRoom.setModifiedTime(goldRoom.getCreatedTime());

        JsonWrapper jsonWrapper = new JsonWrapper("");
        jsonWrapper.putString("ints", StringUtil.implode(intsList, ","));
        jsonWrapper.putString("strs", StringUtil.implode(strsList, ","));

        if (matchBean != null) {
            jsonWrapper.putString("matchId", matchBean.getKeyId().toString());
            jsonWrapper.putInt("matchRatio", JjsUtil.loadMatchRatio(matchBean));
        }

        goldRoom.setTableMsg(jsonWrapper.toString());
        goldRoom.setServerId(GameServerConfig.SERVER_ID);
        return goldRoom;
    }


    private static void directEnterGoldRoom(GoldRoom goldRoom, int gameType, Player player, MatchBean matchBean) throws Exception{
        GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), 0, "1");
        goldRoom.setCurrentState("1");
        JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
        final BaseTable table = TableManager.getInstance().createSimpleTable(player, StringUtil.explodeToIntList(json.getString("ints"), ",")
                , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, matchBean);
        if (table != null) {// 牌桌进入准备阶段
            table.changeTableState(SharedConstants.table_state.ready);
            if (TableManager.getInstance().addTable(table) == 1) {
                table.saveSimpleTable();
            }
            table.setMasterId(0);
            table.setModeId(goldRoom.getModeId());
            table.setMatchId(matchBean == null ? null : matchBean.getKeyId());
            table.setMatchRatio(goldRoom.loadMatchRatio());
            table.changeExtend();
            table.joinPlayer(player);
            table.ready(player);
            table.checkDeal();// 进入房间
            table.startNext();
        } else {
            GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), 0, "2");
            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
        }
    }
}
