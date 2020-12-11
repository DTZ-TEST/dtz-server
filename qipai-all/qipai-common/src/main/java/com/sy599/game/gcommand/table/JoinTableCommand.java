package com.sy599.game.gcommand.table;

import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DaikaiTable;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.JoinTableRes;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class JoinTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getMyExtend().isGroupMatch()){
            player.writeErrMsg("正在为您匹配房间，请不要进行其他操作");
            return;
        }

        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        execute(player,req.getParamsList(),req.getStrParamsList());
    }

    public void execute(Player player, List<Integer> intParamList, List<String> strParamList) throws Exception {
        //判断玩家是否在打比赛场
        if (player.isMatching() || player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }

        int paramsCount = intParamList != null ? intParamList.size() : 0;
        int strParamsCount = strParamList != null ? strParamList.size() : 0;
        //获得玩家加入的房间id
        long tableId = paramsCount > 0 ? intParamList.get(0).longValue() : 0;

        // gameType
        int gameType = paramsCount > 1 ? intParamList.get(1).intValue() : 0;

        int serverType = paramsCount > 2 ? intParamList.get(2).intValue() : 1;//游戏服类型0练习场1普通场2金币场3竞技场
        int playNo = paramsCount > 3 ? intParamList.get(3).intValue() : -1;//房间玩法编号
        int gId = paramsCount > 4 ? intParamList.get(4).intValue() : -1;//俱乐部id

        int modeId = strParamsCount > 0 ? NumberUtils.toInt(strParamList.get(0),-1) : -1;//牌局模式ID，创建房间参数具体信息需查看数据库

        String matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");//金币场匹配模式

        List<Integer> wanfaList;
        if (strParamsCount > 1) {// 传入APP对应的玩法
            wanfaList = StringUtil.explodeToIntList(strParamList.get(1));
            if (wanfaList == null){
                wanfaList = Collections.emptyList();
            }
        }else{
            wanfaList = Collections.emptyList();
        }

        //判断玩家是否有正在玩的房间
        BaseTable table = player.getPlayingTable();
        if (table != null) {
            CreateTableRes res = table.buildCreateTableRes(player.getUserId(), true, false);
            player.writeSocket(res);
            table.broadOnlineStateMsg();
            return;
        }

        if (player.getPlayingTableId() != 0) {
            if (GoldRoomUtil.isNotGoldRoom(player)){
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_2, player.getPlayingTableId()));
                LogUtil.e("JoinTableCommand -->" + player.getUserId() + " a:" + player.getPlayingTableId());
                return;
            }else{
                if ("1".equals(matchType)) {
                    GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(),player.getUserId());
                    if (goldRoomUser!=null) {
                        GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                        if (goldRoom==null||goldRoom.isOver()){
                            player.setPlayingTableId(0);
                            player.saveBaseInfo();
                        }else if(goldRoom.isPlaying()){
                            //游戏正在进行中
                            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                            return;
                        }else{
                            //等待开始游戏
                            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);

                            JsonWrapper json=new JsonWrapper(goldRoom.getTableMsg());
                            BaseTable table1 = TableManager.getInstance().createSimpleTable(player,StringUtil.explodeToIntList(json.getString("ints"),",")
                                    ,StringUtil.explodeToStringList(json.getString("strs"),","),goldRoom,JjsUtil.loadMatch(goldRoom));

                            if (table1 != null)
                                synchronized (Constants.GOLD_LOCK){
                                    TableManager.getInstance().addGoldTable(table1);
                                }
                            return;
                        }
                    }else{
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    }
                }
            }
        }

        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }

        /**
         * 重置玩家俱乐部成员数据
         */
        player.setGroupUser(null);
        player.setIsGroup(0);

        GroupTable groupTable=null;
        Server server;

        if (tableId == 1 && modeId > 0){
            // 匹配金币场
            GoldRoomUtil.joinGoldRoom(player, gameType, serverType, String.valueOf(modeId), matchType,null,false,null);
            return;
        }

        boolean reloadGroupTable = true;

        if (tableId<=0&&(server=ServerManager.loadServer(GameServerConfig.SERVER_ID))!=null&&serverType==server.getServerType()){
            if ((playNo>0&&server.getServerType()==0)){
                String serverKey=server.getServerType()+"_config_"+playNo;

                synchronized (JoinTableCommand.class){
                    List<BaseTable> list=TableManager.getServerTypeMap().get(serverKey);
                    if (list!=null&&list.size()>0){
                        for (BaseTable tempTable:list){
                            if (tempTable.getState()==table_state.ready&&(tempTable.getPlayBureau()<=1)&&tempTable.getPlayerCount()<tempTable.getMaxPlayerCount()){
                                table=tempTable;
                                break;
                            }
                        }
                    }
                    if (table==null){
                        Map<String,Object> properties=new HashMap<>();
                        properties.put("serverType",serverType);
                        properties.put("serverKey",serverKey);
                        table=TableManager.getInstance().createTable(player, GameConfigUtil.getIntsList(server.getServerType(),String.valueOf(playNo)), GameConfigUtil.getStringsList(server.getServerType(), String.valueOf(playNo)), 0, 0,properties);
                        if (table!=null){
                            if (list==null){
                                list=new ArrayList<>();
                                list.add(table);
                                TableManager.getServerTypeMap().put(serverKey,list);
                            }else{
                                list.add(0,table);
                            }
                        }
                        return;
                    }else{
                        tableId=table.getId();
                    }
                }
            }else if (modeId>0){
                synchronized (JoinTableCommand.class){
                    groupTable= GroupDao.getInstance().loadRandomGroupTable(modeId,String.valueOf(server.getId()));

                    if (groupTable!=null){
                        table=TableManager.getInstance().getTable(groupTable.getTableId());
                    }
                    if (table==null||!table.isCanJoin(player)||table.getPlayBureau()>1||(table.getPlayBureau()<=1&&table.getState()!=table_state.ready)){
                        GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(modeId);
                        if (groupTableConfig==null){
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48, tableId));
                            return;
                        }

                        GroupUser groupUser0 = player.getGroupUser();
                        if (groupUser0==null||groupUser0.getGroupId().longValue()!=(groupTableConfig.getParentGroup().longValue()<=0?groupTableConfig.getGroupId().longValue():groupTableConfig.getParentGroup().longValue())){
                            groupUser0=GroupDao.getInstance().loadGroupUser(player.getUserId(),groupTableConfig.getParentGroup().longValue()<=0?groupTableConfig.getGroupId().toString():groupTableConfig.getParentGroup().toString());
                            player.setGroupUser(groupUser0);
                        }

                        if (groupUser0==null){
                            LogUtil.e("createTable fail:userId="+player.getUserId()+",type=" + gameType + ",modeId=" + modeId);
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_49, tableId));
                            return;
                        }else if (groupUser0.getUserLevel()==null||groupUser0.getUserLevel().intValue()<=0){
                            player.writeErrMsg("您已被禁止游戏，请联系群主");
                            return;
                        }

                        JsonWrapper json;
                        List<Integer> intsList = null;
                        List<String> strsList = null;
                        try {
                            json = new JsonWrapper(groupTableConfig.getModeMsg());
                            intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                            strsList = GameConfigUtil.string2List(json.getString("strs"));
                        }catch (Throwable th){
                        }finally {
                            if ((intsList==null||intsList.size()==0)&&(strsList==null||strsList.size()==0)){
                                intsList = GameConfigUtil.string2IntList(groupTableConfig.getModeMsg());
                                strsList = Collections.emptyList();
                            }
                        }

                        Map<String,Object> properties=new HashMap<>();
                        properties.put("serverType",serverType);
                        properties.put("serverKey",0);

                        if (groupTableConfig.getPayType().intValue()==1){//AA支付
                            groupTable = new GroupTable();
                            groupTable.setUserId(String.valueOf(player.getUserId()));
                            TableManager.getInstance().createTable(player, intsList, strsList, 0, 0,true,properties,groupTable,groupTableConfig);
                        }else{
                            int payValue=PayConfigUtil.get(groupTableConfig.getGameType(),groupTableConfig.getGameCount(),groupTableConfig.getPlayerCount(),1);
                            GroupUser groupUser=GroupDao.getInstance().loadGroupMaster(groupTableConfig.getParentGroup().longValue()==0?groupTableConfig.getGroupId().toString():groupTableConfig.getParentGroup().toString());
                            if (groupUser!=null) {
                                if (payValue < 0) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    return;
                                }
                                if (!GameConfigUtil.freeGameOfGroup(groupTableConfig.getGameType(), groupUser.getGroupId().toString())) {
                                    Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
                                    if (player1 != null) {
                                        if (player1.getCards() + player1.getFreeCards() < payValue) {
                                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                            return;
                                        } else {
                                            player1.changeCards(0, -payValue, true, groupTableConfig.getGameType(), CardSourceType.groupTable_AA);
                                        }
                                    } else {
                                        RegInfo user = UserDao.getInstance().selectUserByUserId(groupUser.getUserId());
                                        if (user == null) {
                                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                            return;
                                        } else {
                                            if (user.getCards() + user.getFreeCards() < payValue) {
                                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                                return;
                                            } else {
                                                int c1 = 0, c2 = 0;
                                                if (user.getFreeCards() > 0) {
                                                    if (user.getFreeCards() >= payValue) {
                                                        c1 = payValue;
                                                    } else {
                                                        c1 = (int) user.getFreeCards();
                                                    }
                                                }
                                                if (c1 < payValue) {
                                                    c2 = payValue - c1;
                                                }

                                                UserDao.getInstance().updateUserCards(user.getUserId(), user.getFlatId(), user.getPf(), -c2, -c1);
                                                user.setUsedCards(user.getUsedCards() - payValue);
                                                Player player2 = ObjectUtil.newInstance(player.getClass());
                                                player2.loadFromDB(user);
                                                PlayerManager.getInstance().changeConsume(player2, -c2, -c1, groupTableConfig.getGameType());

                                                if (user.getIsOnLine() == 1 && user.getEnterServer() > 0) {
                                                    Server server1 = ServerManager.loadServer(user.getEnterServer());
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
                                                                url += "/online/notice.do?type=playerCards&userId=" + user.getUserId();
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
                                        }
                                    }
                                }else{
                                    payValue = 0;
                                }
                            }else{
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                return;
                            }

                            groupTable = new GroupTable();
                            groupTable.setUserId(String.valueOf(player.getUserId()));
                            table=TableManager.getInstance().createTable(player, intsList, strsList, 0, player.getUserId(),false,properties,groupTable,groupTableConfig);

                            if (table!=null && payValue >0){
                                MessageUtil.sendMessage(UserMessageEnum.TYPE0,groupUser.getUserId(),"军团房间【"+table.getId()+"】创建成功，消耗钻石x"+payValue,null);
                            }
                        }
                        return;
                    }else{
                        tableId=table.getId();

//						if (gameType<=0){
//							GroupTableConfig groupTableConfig=GroupDao.getInstance().loadGroupTableConfig(modeId);
//							if (groupTableConfig!=null){
//								gameType=groupTableConfig.getGameType();
//							}
//						}
                    }
                }
            } else {
                return;
            }
        }else{
            //获得要加入的房间的信息
            table = TableManager.getInstance().getTable(tableId);

            if (table!=null){
                GroupTable groupTable1;
                if (NumberUtils.isDigits(table.getServerKey())){
                    groupTable1 = GroupDao.getInstance().loadGroupTableByKeyId(table.getServerKey());
                }else if (table.isGroupRoom()&&table.getServerKey().contains("_")){
                    groupTable1 = GroupDao.getInstance().loadGroupTableByKeyId(table.getServerKey().split("_")[1]);
                }else{
                    groupTable1 = null;
                }

                if (groupTable1!=null && groupTable1.isOver()){
                    TableManager.getInstance().delTable(table,true);
                    table = null;
                }else{
                    table.setGroupTable(groupTable1);
                    reloadGroupTable = false;

                    if (groupTable1!=null&&groupTable1.getTableMsg().contains("match")){
                        LogUtil.errorLog.warn("joinTable is error:group match:tableId={},groupTableKey={},groupId={},userId={}"
                        ,table.getId(),groupTable1.getKeyId(),groupTable1.getGroupId(),player.getUserId());
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_21, tableId));
                        return;
                    }
                }
            }

            //加入的房间不存在
            if (table == null) {
                synchronized (JoinTableCommand.class){
                    table = TableManager.getInstance().getTable(tableId);
                    if (table==null){
                        //查看是否在代开房间里
                        DaikaiTable daikaiTable = TableDao.getInstance().getDaikaiTableById(tableId);
                        // 走代开房间
                        if (daikaiTable != null) {
                            if ( 0 == daikaiTable.getState()){
                                if(daikaiTable.getServerId() != GameServerConfig.SERVER_ID){
                                    RoomBean room = TableDao.getInstance().queryRoom(tableId);
                                    if (room==null){
                                        LogUtil.e("joinTable is error:room is null,type:" + gameType + " tableId:" + tableId+",state="+daikaiTable.getState()+",tableServerId="+daikaiTable.getServerId()+",serverId="+GameServerConfig.SERVER_ID);
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                                        return;
                                    }else{
                                        if (daikaiTable.getServerId()!=room.getServerId()){
                                            daikaiTable.setServerId(room.getServerId());
                                            HashMap<String,Object> map=new HashMap<>();
                                            map.put("serverId",room.getServerId());
                                            map.put("tableId",daikaiTable.getTableId());
                                            TableDao.getInstance().updateDaikaiTable(map);
                                            LogUtil.monitorLog.warn("updateDaikaiTable serverId:type:" + gameType + " tableId:" + tableId+",state="+daikaiTable.getState()+",tableServerId="+daikaiTable.getServerId()+",serverId="+GameServerConfig.SERVER_ID);

                                        }
                                    }

                                    if (GameServerConfig.SERVER_ID!=room.getServerId()){
                                        LogUtil.e("joinTable is error:get server fail,type:" + gameType + " tableId:" + tableId+",state="+daikaiTable.getState()+",tableServerId="+daikaiTable.getServerId()+",serverId="+GameServerConfig.SERVER_ID);
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                                        return;
                                    }
                                }

                                if(!wanfaList.isEmpty() && !wanfaList.contains(daikaiTable.getPlayType())) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                                    return;
                                }
                                String createPara = daikaiTable.getCreatePara();
                                String createStrPara = daikaiTable.getCreateStrPara();
                                List<Integer> params = StringUtil.explodeToIntList(createPara);
                                String[] strArrays = createStrPara.split("#");
                                List<String> strParams = Arrays.asList(strArrays);
                                Map<String, Object> properties=new HashMap<>();
                                if (SharedConstants.isAssisOpen()) {
                                    properties.put("assisCreateNo", daikaiTable.getAssisCreateNo());
                                    properties.put("assisGroupNo", daikaiTable.getAssisGroupNo());
                                }
                                TableManager.getInstance().createTable(player, params, strParams, tableId, daikaiTable.getDaikaiId(),properties);
                            }else{
                                LogUtil.e("joinTable is error:type:" + gameType + " tableId:" + tableId+",state="+daikaiTable.getState()+",tableServerId="+daikaiTable.getServerId()+",serverId="+GameServerConfig.SERVER_ID);
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                            }

                            return;
                        }

                        GroupTable gt = GroupDao.getInstance().loadGroupTable(player.getUserId(),tableId);
                        if (gt != null){
                            if (!gt.getServerId().equals(String.valueOf(GameServerConfig.SERVER_ID))){
                                LogUtil.e("join group table is error:get server fail,type:" + gameType + " tableId:" + tableId+",tableServerId="+gt.getServerId()+",serverId="+GameServerConfig.SERVER_ID);
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                            }else{
                                if (gt.getTableMsg().contains("match")){
                                    LogUtil.errorLog.warn("joinTable is error:group match:tableId={},groupTableKey={},groupId={},userId={}"
                                            ,table.getId(),gt.getKeyId(),gt.getGroupId(),player.getUserId());
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_21, tableId));
                                    return;
                                }


                                JsonWrapper jsonWrapper=new JsonWrapper(gt.getTableMsg());
                                List<Integer> params = StringUtil.explodeToIntList(jsonWrapper.getString("ints"),",");
                                List<String> strParams = StringUtil.explodeToStringList(jsonWrapper.getString("strs").split(";")[1],",");

                                Map<String,Object> properties=new HashMap<>();
                                properties.put("recreate",gt.getGroupId());
                                properties.put("reTableId",gt.getTableId());
                                properties.put("reGroupId",gt.getGroupId());
                                properties.put("reGroupKeyId",gt.getKeyId());
                                TableManager.getInstance().createTable(player, params, strParams, 0, 0,true,properties,gt,null);
                            }

                            return;
                        } else if (gId>0 && modeId>0) {
                            GroupTableConfig gtc = GroupDao.getInstance().loadGroupTableConfig(modeId);
                            if (gtc == null) {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                                return;
                            }
                            JsonWrapper json;
                            List<Integer> intsList = null;
                            List<String> strsList = null;
                            try {
                                json = new JsonWrapper(gtc.getModeMsg());
                                intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                                strsList = GameConfigUtil.string2List(json.getString("strs"));
                            }catch (Throwable th){
                            }finally {
                                if ((intsList==null||intsList.size()==0)&&(strsList==null||strsList.size()==0)){
                                    intsList = GameConfigUtil.string2IntList(gtc.getModeMsg());
                                    strsList = new ArrayList<>();
                                }
                            }
                            strsList.add(String.valueOf(gId));
                            strsList.add("1");
                            strsList.add("1");
                            strsList.add(String.valueOf(modeId));
                            TableManager.getInstance().createTable(player, intsList, strsList, 0, 0,true,null,null,null);
                            return;
                        }

                        LogUtil.e("joinTable is null:type:" + gameType + " tableId:" + tableId);
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                        return;
                    }
                }
                if (table == null){
                    LogUtil.e("joinTable is null:type:" + gameType + " tableId:" + tableId);
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                    return;
                }else{
                    if(!wanfaList.isEmpty() && !wanfaList.contains(table.getPlayType())) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                        return;
                    }
                }
            } else {
                if(!wanfaList.isEmpty() && !wanfaList.contains(table.getPlayType())) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                    return;
                }
            }
        }

        boolean requiredReload=true;
        //军团房间
        if (NumberUtils.isDigits(table.getServerKey())){
            if (!reloadGroupTable){
                groupTable=table.getGroupTable();
            }else if (groupTable==null){
                groupTable=GroupDao.getInstance().loadGroupTableByKeyId(table.getServerKey());
            }

            if (groupTable!=null){
                GroupUser groupUser=GroupDao.getInstance().loadGroupUser(player.getUserId(),groupTable.getGroupId().toString());
                player.setGroupUser(groupUser);
                if (groupUser==null){
                    LogUtil.e("joinTable fail:userId="+player.getUserId()+",type=" + gameType + ",tableId=" + tableId+",groupId="+groupTable.getGroupId()+",groupUser="+(groupUser==null?"null":groupUser.getGroupId()));
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_49, tableId));
                    return;
                }else if (groupUser.getUserLevel()==null||groupUser.getUserLevel().intValue()<=0){
                    player.writeErrMsg("您已被禁止游戏，请联系群主");
                    return;
                }
            }

        }else if (table.isGroupRoom()){
            int groupId=Integer.parseInt(table.loadGroupId());
            GroupUser groupUser=player.getGroupUser();
            if (groupUser==null||groupUser.getGroupId().intValue()!=groupId){
                groupUser=player.loadGroupUser(String.valueOf(groupId));
                requiredReload = false;
                if (groupUser==null){
                    LogUtil.e("joinTable fail:userId="+player.getUserId()+",type=" + gameType + ",tableId=" + tableId+",groupId="+groupId+",groupUser="+(groupUser==null?"null":groupUser.getGroupId()));
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_49, tableId));
                    return;
                }else if (groupUser.getUserLevel()==null||groupUser.getUserLevel().intValue()<=0){
                    player.writeErrMsg("您已被禁止游戏，请联系群主");
                    return;
                }
            }
            if (table.getServerKey().contains("_")){
                GroupTable groupTable1;
                if (!reloadGroupTable){
                    groupTable1=table.getGroupTable();
                }else{
                    groupTable1=GroupDao.getInstance().loadGroupTableByKeyId(table.getServerKey().split("_")[1]);
                }

                if (groupTable1==null||groupTable1.isOver()){
                    TableManager.getInstance().delTable(table,true);
                    LogUtil.e("join group table fail:userId="+player.getUserId()+",type=" + gameType + ",tableId=" + tableId+",groupId="+groupId+",ServerKey="+table.getServerKey());
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_8, tableId));
                    return;
                }
            }

            /**离线超过时间自动剔除**/
            long timeout = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","group_room_fire",0)*1000L;
            if (timeout>0L){
                synchronized (table){
                    if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau()>0||table.getPlayBureau()>1||table.getPlayerCount()<1){
                    }else{
                        for (Map.Entry<Long,Player> kv : table.getPlayerMap().entrySet()){
                            Player player1 = kv.getValue();
                            if (player1.getIsOnline()==0&&player1.getLogoutTime()!=null&&(System.currentTimeMillis()-player1.getLogoutTime().getTime()>=timeout)){
                                if (table.canQuit(player1)&&table.quitPlayer(player1)){
                                    table.onPlayerQuitSuccess(player1);
                                    table.updateRoomPlayers();
                                }
                            }
                        }
                    }
                }
            }
        }

        // 转化玩家
        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());

        // 房主自动准备
        long currentUserId=player.getUserId();
        boolean isObserver="1".equals(table.getRoomModeMap().get("1"));
//		boolean canJoin=table.isCanJoin(player);
        //加入的房间已经开始游戏
//		if((table.getPlayBureau() == 1 && table.getState() != table_state.ready) || table.getPlayBureau() != 1 || !canJoin) {
        //观战
        if (isObserver){
            // 如果加房的玩家非本军团玩家则返回
            if (table.getAllowGroupMember() > 0 && (player.getGroupUser()==null||player.getGroupUser().getGroupId().intValue()!=table.getAllowGroupMember())) {
                if (table.isGroupRoom()){
                    String groupId=table.loadGroupId();
                    if (groupId!=null){
                        GroupUser groupUser = player.loadGroupUser(groupId);
                        if((groupUser==null||groupUser.getGroupId()!=table.getAllowGroupMember())) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                            return;
                        }else if (groupUser.getUserLevel()==null||groupUser.getUserLevel().intValue()<=0){
                            player.writeErrMsg("您已被禁止游戏，请联系群主");
                            return;
                        }
                    }else{
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return;
                    }
                }else{
                    GroupUser groupUser = player.loadGroupUser(String.valueOf(table.getAllowGroupMember()));
                    if((groupUser==null||groupUser.getGroupId().intValue()!=table.getAllowGroupMember())) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return;
                    }else if (groupUser.getUserLevel()==null||groupUser.getUserLevel().intValue()<=0){
                        player.writeErrMsg("您已被禁止游戏，请联系群主");
                        return;
                    }
                }
            }

            if(!table.getRoomPlayerMap().containsKey(player.getUserId())&&table.getRoomPlayerMap().size()>= NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("observer_count"),50)){
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5, tableId));
                return;
            }

            table.getRoomPlayerMap().put(player.getUserId(),player);
            player.getMyExtend().getPlayerStateMap().put("1","1");
            player.changeState(player_state.entry);
            player.setPlayingTableId(table.getId());
            player.setSeat(0);
            player.changeIsLeave(0);
            player.setTotalPoint(0);
            player.setMaxPoint(0);
            player.setTotalBoom(0);
            player.changeExtend();

            LogUtil.msgLog.info("room observer:userId="+player.getUserId()+",tableId="+table.getId()+",masterId="+table.getMasterId());

            // 允许中途加入
            if ("1".equals(table.getRoomModeMap().get("2"))){
                if(currentUserId== table.getMasterId()) {
                    table.ready(player);
                    // 加入牌桌
                    if (!table.joinPlayer(player)) {
                        return;
                    }
                }


//					JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
//					joinRes.setPlayer(player.buildPlayInTableInfo());
                //玩法
//					joinRes.setWanfa(table.getPlayType());
//					for (Player tablePlayer : table.getSeatMap().values()) {
//						//如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
//						if (tablePlayer.getUserId() != currentUserId) {
//							tablePlayer.writeSocket(joinRes.build());
//						}
//					}
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
            }else {
                // 不允许中途加入只能在第一局准备时加入
                if (player.getUserId() == table.getMasterId()) {
                    table.ready(player);
                }
                // 加入牌桌
                if (table.getPlayBureau()<=1&&table.getState()==table_state.ready){
                    boolean result = table.joinPlayer(player);
                    if (result) {
                        JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
                        joinRes.setPlayer(player.buildPlayInTableInfo());
                        //玩法
                        joinRes.setWanfa(table.getPlayType());
                        for (Player tablePlayer : table.getSeatMap().values()) {
                            //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                            if (tablePlayer.getUserId() != currentUserId) {
                                tablePlayer.writeSocket(joinRes.build());
                            }
                        }
                    }

                }
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
            }
            table.broadOnlineStateMsg();
            return;
        }else{
            // 非观战
            if(player.getUserId() == table.getMasterId()) {
                table.ready(player);
            }

            // 加入牌桌
            if (!table.joinPlayer(player)) {
                return;
            }

            JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
            joinRes.setPlayer(player.buildPlayInTableInfo());
            //玩法
            joinRes.setWanfa(table.getPlayType());
            for (Player tablePlayer : table.getSeatMap().values()) {
                //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                if (tablePlayer.getUserId() == player.getUserId()) {
                    tablePlayer.writeSocket(table.buildCreateTableRes(player.getUserId()));
                } else {
                    tablePlayer.writeSocket(joinRes.build());
                }
            }

            //房主以外的其他玩家选座发送消息
            table.sendPlayerStatusMsg();
            table.broadOnlineStateMsg();
        }

        // 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
        table.checkDeal();
    }

    @Override
    public void setMsgTypeMap() {
    }

}
