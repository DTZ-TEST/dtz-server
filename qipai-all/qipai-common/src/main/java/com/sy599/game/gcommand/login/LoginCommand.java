package com.sy599.game.gcommand.login;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.task.TenMinuteFixedRateTask;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.LuckyRedbagCommand;
import com.sy599.game.gcommand.com.activity.OldPlayerBackActivityCmd;
import com.sy599.game.gcommand.login.util.LoginDataUtil;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.bean.MatchUser;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.OpenMsg.Open;
import com.sy599.game.staticdata.bean.GradeExpConfig;
import com.sy599.game.staticdata.bean.GradeExpConfigInfo;
import com.sy599.game.util.*;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class LoginCommand extends BaseCommand {
    private long loginUserId;
    private boolean returnPlayer = false;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
    }

    public Player login(MessageUnit message, MyWebSocket socket) throws Exception {
        Open openMsg = Open.parseFrom(message.getContent());
        String userId = openMsg.getUserId();
        String t = openMsg.getT();
        String s = openMsg.getS();
        String c = openMsg.getC();
        String v = openMsg.getV();
        int isCrossServer = openMsg.getIsCrossServer();

        boolean loginSuccess = false;
        long startTime = System.currentTimeMillis();

        String fromurl = openMsg.getFromUrl();
        if (StringUtils.isBlank(fromurl)) {
            fromurl = "";
        }

        if (NumberUtils.toLong(userId,0)<=0){
            return null;
        }

        // 验证md5
        if (!Md5CheckUtil.checkLoginMd5(userId, t, s)) {
            LogUtil.e(userId + "-->login md5 err");
            socket.send(WebSocketMsgType.res_code_err, "验证错误，请稍后再试");
            return null;
        }
        loginUserId = Long.parseLong(userId);
        String currentCommand = GameUtil.USER_COMMAND_MAP.get(loginUserId);
        if (currentCommand != null) {
            LogUtil.e("LoginCommand|login|waitCommand|" + userId + "|" + currentCommand);
            return null;
        }
        LogUtil.msgLog.info("LoginCommand|login|start|{}|{}|{}|{}|{}", userId, t, s, c, v);
        long now = TimeUtil.currentTimeMillis();
        player = PlayerManager.getInstance().getPlayer(loginUserId);
        if (player == null) {
            // 需要获取用户数据
            boolean ret = loadPlayer(loginUserId, socket);
            if (returnPlayer){
                return player;
            }
            if (!ret) {
                LogUtil.e(userId + "获取用户数据错误");
                return null;
            }
        } else {
            // 如果有数据没有保存先保存
            player.saveBaseInfo();
            if (isCrossServer == 1) {
                player.refreshPlayer();
            } else if (player.getSyncTime() == null || (now - player.getSyncTime().getTime() > 20 * SharedConstants.SENCOND_IN_MINILLS)) {
                player.refreshPlayer();
            }
        }

        if (!TenMinuteFixedRateTask.checkBlack(player) || player.isForbidLogin()) {
            LogUtil.e(userId + " 您已被禁止登陆");
            socket.sendComMessage(WebSocketMsgType.res_code_err, "您已被禁止登陆");
            return null;
        }

        if (StringUtils.isBlank(player.getSessionId()) || c == null) {
            LogUtil.e("login err:" + player.getUserId() + " sessionId is null");
            socket.accountConflict(player);
            return null;
        }

        // 验证
        if (!c.equals(player.getSessionId())) {
            // session验证不一样，进行过登录登录操作
            // 需要获取用户数据
            String oldSession = player.getSessionId();
            player.refreshPlayer();

            if (!c.equals(player.getSessionId())) {
                LogUtil.e("login err:" + player.getUserId() + "sessionId err c:" + c + ",old=" + oldSession + ",new=" + player.getSessionId());

                socket.accountConflict(player);
                return null;
            }
        }

        String matchId = player.loadMatchId();
        MatchBean matchBean;
        if (StringUtils.isNotBlank(matchId)&&JjsUtil.hasMatch()){
            matchBean = JjsUtil.loadMatch(Long.valueOf(matchId));
            if (matchBean == null) {
                matchBean = MatchDao.getInstance().selectOne(matchId);
            }
            if (matchBean == null){
                player.quitMatch();
                MatchDao.getInstance().deleteMatchUser(matchId,String.valueOf(player.getUserId()));
            }else if (JjsUtil.isOver(matchBean)){
                player.quitMatch();
                HashMap<String ,Object> map = new HashMap<>();
                map.put("currentState", "2");
                MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(),player.getUserId(),map);

                matchBean = null;
            }else if (matchBean.getServerId().intValue()!=GameServerConfig.SERVER_ID){
                LogUtil.e(userId + "应进入比赛场服:" +matchBean.getServerId());
                PlayerManager.getInstance().removePlayer(player);
                socket.sendComMessage(WebSocketMsgType.res_code_login);
                GameServerUtil.sendChangeServerCommand(socket,player.getTotalCount(),ServerManager.loadServer(matchBean.getServerId()));
                return player;
            }
        }else{
            matchBean = null;
        }

        int cards = (int) player.loadAllCards();
        int golds = (int) player.loadAllGolds();
        long playTableId = 0;
        int playerMark = 0;

        String ip = NettyUtil.userIpMap.get(socket.getCtx().channel());
        if (StringUtils.isBlank(ip)){
            ip = NettyUtil.getRemoteAddr(socket.getCtx());
        }

        try {
            if (player.getPlayingTableId() != 0) {
                BaseTable table = TableManager.getInstance().getTable(player.getPlayingTableId());
                Player player1;
                if (table != null && (player1 = table.getPlayerMap().get(player.getUserId())) != null) {
                    if (player1 != player) {
                        PlayerManager.getInstance().addPlayer(player1, true);
                        player = player1;
                    }
                } else if (GoldRoomUtil.isNotGoldRoom(player.getPlayingTableId())) {
                    RoomBean room = TableDao.getInstance().queryRoom(player.getPlayingTableId());
                    if (room != null && room.getServerId() != GameServerConfig.SERVER_ID) {
                        Server server = ServerManager.loadServer(room.getServerId());
                        if (server == null) {
                            LogUtil.errorLog.error("Not Found Server:" + room.getServerId() + " tableId=" + player.getPlayingTableId());
                            player.clearTableInfo();
                            player.setEnterServer(GameServerConfig.SERVER_ID);
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        } else {
                            LogUtil.e(userId + "应进入:" + player.getEnterServer() + " 房间:" + player.getPlayingTableId() + " -" + room.getServerId() + "-" + GameServerConfig.SERVER_ID);
//                            player.writeErrMsg(LangMsg.code_222, player.getEnterServer(), player.getPlayingTableId(), room.getServerId(), GameServerConfig.SERVER_ID);
                            PlayerManager.getInstance().removePlayer(player);
                            socket.sendComMessage(WebSocketMsgType.res_code_login);
                            GameServerUtil.sendChangeServerCommand(socket,player.getTotalCount(),server);
                            return player;
                        }
                    }
                }
            }

            // 是否弹出签到提示
            String isOut = "0";
            // 是否在房间标示
            String isInRoom = "0";

            String signType = ResourcesConfigsUtil.loadServerPropertyValue("signType");
            boolean checkSign = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("pop_login_sign", "1"));
            if (checkSign) {
                Calendar cal = Calendar.getInstance();

                if ("2".equals(signType)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    String end = sdf.format(cal.getTime());
                    String ymdDate = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.add(Calendar.DAY_OF_YEAR, 1 - NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("signDays"), 5));
                    String start = sdf.format(cal.getTime());
                    player.loadSigns(start, end, 2);

                    if (!player.getYmdSigns().contains(ymdDate)) {
                        isOut = "1";
                    } else {
                        isOut = "0";
                    }
                } else {
                    player.loadSigns();

                    int date = cal.get(Calendar.DATE);
                    if (!player.getSigns().contains(date)) {
                        isOut = "1";
                    } else {
                        isOut = "0";
                    }
                }
            }

            OldPlayerBackActivityCmd.oldPlayerBackReward(player);

            if (GameServerConfig.SERVER_ID != player.getEnterServer()) {
                player.setEnterServer(GameServerConfig.SERVER_ID);
                playerMark = 1;
                LogUtil.msgLog.info("update player enterServer:userId=" + player.getUserId() + ",enterServer=" + GameServerConfig.SERVER_ID);
            }

            player.setIsOnline(1, false);

            // 需要远程去获取数据(银币等)
            player.setMyWebSocket(socket);
            socket.setPlayer(player);

            Player player1 = PlayerManager.getInstance().addPlayer(player, false);
            if (player1 != player) {
                LogUtil.e("player already login:" + player.getUserId());
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_223));
                return null;
            }

            WebSocketManager.addWebSocket(socket);

            player.initMsgCheckCode();
            boolean sendMatchMsg = false;

            String matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");

            BaseTable table = player.getPlayingTable();

            GoldRoom goldRoom = null;
//        boolean isObserver=false;
            if (table != null) {
//            if ("1".equals(table.getRoomModeMap().get("1"))&&"1".equals(player.getMyExtend().getPlayerStateMap().get("1"))){
////                isObserver=true;
//                table.getRoomPlayerMap().put(player.getUserId(),player);
//            }
                playTableId = table.getId();
                isInRoom = "1";

                player.setIsEntryTable(SharedConstants.table_online);
                if (player.getState() == null) {
                    player.changeState(SharedConstants.player_state.entry);
                }
            } else if (GoldRoomUtil.isGoldRoom(player.getPlayingTableId())) {
                GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(), player.getUserId());
                if (goldRoomUser != null) {
                    GoldRoom goldRoom0 = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                    if (goldRoom0 != null && "2".equals(matchType)) {
                        BaseTable baseTable = TableManager.getInstance().getTable(goldRoom0.getKeyId());
                        if (baseTable != null) {
                            playTableId = player.getPlayingTableId();
                            isInRoom = "1";
                        } else {
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        }
                    } else {
                        if (goldRoom0 == null || goldRoom0.isOver()) {
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        } else if (goldRoom0.isPlaying()) {
                            playTableId = player.getPlayingTableId();
                            isInRoom = "1";
                        } else {
                            playTableId = player.getPlayingTableId();
                            goldRoom = goldRoom0;
                            isInRoom = "1";
                        }
                    }
                } else {
                    player.setPlayingTableId(0);
                    playerMark = 1;
                }
            } else {
                player.changeState(SharedConstants.player_state.entry);
                if (player.getMyExtend().getPlayerStateMap().size() > 0) {
                    player.getMyExtend().getPlayerStateMap().clear();
                    player.changeExtend();
                    playerMark = 1;
                }

                if (player.getPlayingTableId() != 0) {
                    LogUtil.e(player.getUserId() + " login tableId null-->" + player.getPlayingTableId());
                    player.setPlayingTableId(0);
                    playerMark = 1;
                }
                player.setSeat(0);
                // 重新登录标示已经退出房间
                player.setIsEntryTable(0);
                playerMark = 1;
            }

            if ("1".equals(isInRoom)) {
                isOut = "0";
            }

            int seat = NumberUtils.toInt(player.getMyExtend().getPlayerStateMap().get("seat"), 0);
            if (seat > 0) {
                player.setSeat(seat);
            }

            if (playTableId <= 0L){
                //正在比赛场
                if (matchBean != null){
                    sendMatchMsg = true;
                }else if (JjsUtil.hasMatch()){
                    MatchUser mu = MatchDao.getInstance().selectPlayingMatchUser(String.valueOf(player.getUserId()));
                    if (mu != null){
                        matchBean = JjsUtil.loadMatch(mu.getMatchId());
                        if (matchBean==null){
                            matchBean=MatchDao.getInstance().selectOne(mu.getMatchId().toString());
                        }
                        if (matchBean!=null){
                            player.joinMatch(matchBean.getKeyId().toString(),true);
                            if (matchBean.getServerId().intValue()==GameServerConfig.SERVER_ID){
                                sendMatchMsg = true;
                                LogUtil.msgLog.warn("login match warn:matchId={},userId={}",matchBean.getKeyId(),player.getUserId());
                            }else{
                                PlayerManager.getInstance().removePlayer(player);
                                socket.sendComMessage(WebSocketMsgType.res_code_login);
                                GameServerUtil.sendChangeServerCommand(socket,player.getTotalCount(),ServerManager.loadServer(matchBean.getServerId()));
                                return player;
                            }
                        }else{
                            MatchDao.getInstance().deleteMatchUser(mu.getMatchId().toString(),mu.getUserId());
                        }
                    }
                }
            }

            // 获得称号信息
//        int totalCount = UserDao.getInstance().selectTotalCountByUserId(loginUserId);
            int name = player.loadDesignation();

            player.getMyExtend().setVersions(StringUtil.parseVersions(v));
            player.setVersion(v);

            List<Integer> msgs = openMsg.getMsgList();
            int tili = 0;
            if (msgs!=null&&msgs.size()>0){
                if (1 == msgs.get(0).intValue()){
                    tili = UserDao.getInstance().queryUserExtendValue(player.getUserId(),UserResourceType.TILI.getType());
                    player.setUserTili(tili);
                }
            }
            String groupId = "";
            long restTime = 0;
            if (player.getMyExtend().isGroupMatch()){
                HashMap<String,Object> map = GroupDao.getInstance().loadGroupMatch(player.getUserId());
                if (map!=null){
                    groupId=String.valueOf(map.get("groupCode"));
                    GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(),groupId);
                    if (groupUser == null || groupUser.getUserLevel()==null||groupUser.getUserLevel().intValue()<=0) {
                        player.getMyExtend().setGroupMatch(false);
                        groupId = "";
                        GroupDao.getInstance().quitGroupMatch(player.getUserId());
                    }else{
                        restTime = GroupRoomUtil.loadGroupMatchMinTimeForCancel() - ((System.currentTimeMillis() - TimeUtil.object2Long(map.get("createdTime")))/1000);
                    }
                }else{
                    player.getMyExtend().setGroupMatch(false);
                }
            }

            // 芒果跑的快段位信息
            int grade = player.getGoldPlayer().getGrade();
            GradeExpConfigInfo gradeExpConfigInfo = GradeExpConfig.getGradeExpConfigInfo(grade);
            String gradeDesc = (gradeExpConfigInfo != null ) ? gradeExpConfigInfo.getDesc() : "";
            int luckRedbagOpen = LuckyRedbagCommand.canOpenLuckyDraw(player);

            List<String> strMsgs = new ArrayList<>();
            List<Integer> intMsgs = new ArrayList<>();
            strMsgs.add(String.valueOf(playTableId));//0房间号
            strMsgs.add(String.valueOf(player.getConfig()));//1配置
            strMsgs.add(String.valueOf(fromurl));//2来源
            strMsgs.add(String.valueOf(groupId));//3匹配模式的俱乐部ID
            strMsgs.add(String.valueOf(gradeDesc));//4芒果段位描述信息
            strMsgs.add(socket.getCtx().channel().id().asShortText());  // 5连接id

            intMsgs.add(cards);//0房卡或钻石
            intMsgs.add(name);//1官衔
            intMsgs.add((int) player.getPayBindId());//2绑定的邀请码
            intMsgs.add(golds);//3金币
            intMsgs.add(tili);//4体力
            intMsgs.add(player.getGoldPlayer().getGrade());//5芒果段位
            intMsgs.add(luckRedbagOpen);//6幸运红包开关
            intMsgs.add(sendMatchMsg?1:0);//7是否在比赛场
            intMsgs.add((int)restTime);//8俱乐部匹配模式可取消的剩余时间

            /**发送登录成功消息**/
            player.writeComMessage(WebSocketMsgType.res_code_login, strMsgs,intMsgs);

            player.setLoginActionTime(now);

            if (sendMatchMsg){
                MatchBean mb = JjsUtil.loadMatch(matchBean.getKeyId());
                if (mb!=null){
                    int restTableCount = mb.getRestTable();
                    if (restTableCount>0){
                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 3, JjsUtil.loadMatchCurrentGameNo(mb), mb.getRestTable(),mb.loadGameType());
                        LogUtil.msgLog.info("match wait other:matchId={},userId={}",mb.getKeyId(),loginUserId);
                    }
                }else{
                    JSONObject matchJson = (JSONObject) JSONObject.toJSON(matchBean);
                    if ("share".equals(matchBean.getMatchPay())){
                        matchJson.put("shared", 1);
                    }
                    player.writeComMessage(WebSocketMsgType.req_com_match_code, matchBean.getMatchType(),matchBean.getKeyId().toString(),matchBean.loadAward().toString(), matchJson.toString(), 1,matchBean.getMaxCount(),matchBean.getCurrentCount(),matchBean.getMaxCount()-matchBean.getCurrentCount());
                }
            }

            if (goldRoom != null && "1".equals(matchType)) {
                //等待开始游戏
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);

                JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
                BaseTable table1 = TableManager.getInstance().createSimpleTable(player, StringUtil.explodeToIntList(json.getString("ints"), ",")
                        , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom,JjsUtil.loadMatch(goldRoom));

                if (table1 != null)
                    synchronized (Constants.GOLD_LOCK) {
                        TableManager.getInstance().addGoldTable(table1);
                    }
            }

            if (isCrossServer == 0) {
                if (checkSign) {
                    // 发送签到信息
                    player.writeSignInfo(isOut, isInRoom, "2".equals(signType) ? 2 : 1);
                }

                player.checkLogin();
            }

            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("ip_from_game"))) {
                if (StringUtils.isNotBlank(ip)){
                    player.setIp(ip);
                }
            }

            player.setIsOnline(1, true);
            playerMark = 2;
            loginSuccess = true;

            if (table != null){
                table.broadIsOnlineMsg(player,SharedConstants.table_online);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (player != null) {
                if (playerMark > 0) {
                    player.saveBaseInfo(true);
                }
            }

            if (!loginSuccess){
                WebSocketManager.removeWebSocket(socket.getPlayer());
            }
        }

        socket.setLoginSuccess(true);
        Date date = new Date();
        player.setLoginTime(date);

        LoginDataUtil.loginData(userId,date);

        LogUtil.msgLog.info("LoginCommand|login|end|{}|playingTableId:{},payBindId:{},cards:{},golds:{},time(ms):{},channel:{},ip:{}", userId, playTableId, player.getPayBindId(), cards, golds, System.currentTimeMillis() - startTime, socket.getCtx().channel(),ip);
        player.sendActionLog(LogConstants.reason_login, "");

        return player;
    }

    private boolean loadPlayer(long userId, MyWebSocket socket) throws Exception {
        RegInfo info = UserDao.getInstance().selectUserByUserId(userId);

        if (info == null) {
            LogUtil.e("login err:user is null-->" + userId);
            socket.send(WebSocketMsgType.sc_err_login, "登录错误:userId-" + userId);
            socket.accountConflict(info);
            return false;
        }
        boolean newPlayer = true;

        if (info.getEnterServer() != 0 && info.getPlayingTableId() != 0) {
            GoldRoom goldRoom;

            if (GoldRoomUtil.isGoldRoom(info.getPlayingTableId())) {
                goldRoom = GoldRoomDao.getInstance().loadGoldRoom(info.getPlayingTableId());

                if (goldRoom != null) {
                    if (goldRoom.isPlaying() || "2".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1"))) {
                        BaseTable playIngTable = TableManager.getInstance().getTable(info.getPlayingTableId());
                        if (playIngTable == null) {
                            if (player == null) {
                                player = ObjectUtil.newInstance(TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                                player.loadFromDB(info);
                            } else {
                                player = PlayerManager.getInstance().changePlayer(player, TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                            }
                        } else {
                            player = playIngTable.getPlayer(info.getUserId(), playIngTable.getPlayerClass());
                            if (player == null) {
                                player = playIngTable.getRoomPlayerMap().get(info.getUserId());
                                if (player == null) {
                                    player = ObjectUtil.newInstance(playIngTable.getPlayerClass());
                                    player.loadFromDB(info);
                                    playIngTable.initPlayers(info.getUserId(), player);
                                } else {
                                    if (player.getMyExtend().getPlayerStateMap().containsKey("seat")) {
                                        player.changeState(SharedConstants.player_state.ready);
                                    } else {
                                        player.changeState(SharedConstants.player_state.entry);
                                    }
                                }
                            } else {
                                if (playIngTable.getState() == SharedConstants.table_state.play) {
                                    player.changeState(SharedConstants.player_state.play);
                                }
                            }
                        }
                    } else if (goldRoom.isNotStart()) {
                        if (player == null) {
                            player = ObjectUtil.newInstance(TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                            player.loadFromDB(info);
                        } else {
                            player = PlayerManager.getInstance().changePlayer(player, TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                        }

                    } else {
                        if (player == null) {
                            player = ObjectUtil.newInstance(TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                            player.loadFromDB(info);
                        } else {
                            player = PlayerManager.getInstance().changePlayer(player, TableManager.getInstance().getInstanceTable(StringUtil.explodeToIntList(new JsonWrapper(goldRoom.getTableMsg()).getString("ints"), ",").get(1)).getPlayerClass());
                        }

                        info.setPlayingTableId(0);
                        Map<String, Object> paramMap = new HashMap<String, Object>();
                        paramMap.put("playingTableId", 0);
                        UserDao.getInstance().save(info.getFlatId(), info.getPf(), info.getUserId(), paramMap);
                    }

                    newPlayer = false;
                } else {
                    info.setPlayingTableId(0);
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("playingTableId", 0);
                    UserDao.getInstance().save(info.getFlatId(), info.getPf(), info.getUserId(), paramMap);
                }
            } else {
                BaseTable playIngTable = TableManager.getInstance().getTable(info.getPlayingTableId());
                if (playIngTable == null) {
                    if (GameServerConfig.SERVER_ID == info.getEnterServer()) {
                        LogUtil.e("login loadPlayer err-->" + "没有找到该牌桌-" + info.getEnterServer() + "-房间号:" + info.getPlayingTableId());
                        info.setPlayingTableId(0);
                        Map<String, Object> paramMap = new HashMap<String, Object>();
                        paramMap.put("playingTableId", 0);
                        UserDao.getInstance().save(info.getFlatId(), info.getPf(), info.getUserId(), paramMap);
                    } else {
                        Server server = ServerManager.loadServer(info.getEnterServer());
                        if (server == null) {
                            LogUtil.errorLog.error("Not Found Server:" + info.getEnterServer() + " tableId=" + info.getPlayingTableId());
                            if (player == null) {
                                player = new CommonPlayer();
                                player.loadFromDB(info);
                                newPlayer = false;
                            }
                            player.clearTableInfo();
                            player.setEnterServer(GameServerConfig.SERVER_ID);
                            player.setPlayingTableId(0);
                            player.saveBaseInfo();
                        } else {
                            LogUtil.e("login loadPlayer err-->" + "登录错误:应进入-" + info.getEnterServer() + "-房间号:" + info.getPlayingTableId());
//                            socket.send(WebSocketMsgType.sc_err_login, "登录错误:应进入-" + info.getEnterServer() + "-房间号:" + info.getPlayingTableId());
//                            socket.accountConflict(info);
                            if (player == null) {
                                player = new CommonPlayer();
                                player.loadFromDB(info);
                            }
                            PlayerManager.getInstance().removePlayer(player);
                            socket.sendComMessage(WebSocketMsgType.res_code_login);
                            returnPlayer = true;
                            GameServerUtil.sendChangeServerCommand(socket,player.getTotalCount(),server);
                            return false;
                        }
                    }
                    if (newPlayer) {
                        player = new CommonPlayer();
                        player.loadFromDB(info);
                        newPlayer = false;
                    }
                } else {
                    player = playIngTable.getPlayer(info.getUserId(), playIngTable.getPlayerClass());
                    if (player == null) {
                        player = playIngTable.getRoomPlayerMap().get(info.getUserId());
                        if (player == null) {
                            player = ObjectUtil.newInstance(playIngTable.getPlayerClass());
                            player.loadFromDB(info);
                            playIngTable.initPlayers(info.getUserId(), player);
                        } else {
                            if (player.getMyExtend().getPlayerStateMap().containsKey("seat")) {
                                player.changeState(SharedConstants.player_state.ready);
                            } else {
                                player.changeState(SharedConstants.player_state.entry);
                            }
                        }
                    } else {
                        if (playIngTable.getState() == SharedConstants.table_state.play) {
                            player.changeState(SharedConstants.player_state.play);
                        }
                    }
                    newPlayer = false;
                }
            }
        }

        if (newPlayer) {
            player = new CommonPlayer();
            player.loadFromDB(info);
//            player.loadGoldPlayer(info);
        }
        return true;
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(ComRes.class, WebSocketMsgType.sc_com);
    }

    public long getLoginUserId() {
        return loginUserId;
    }

}
