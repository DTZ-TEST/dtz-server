package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.SslUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetServerCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		List<Integer> params = req.getParamsList();
		List<String> strParams = req.getStrParamsList();

		int gameType = StringUtil.getIntValue(params, 0, 0);

		//游戏服类型0练习场1普通场2金币场3h5金币场4现金红包瓜分活动5比赛场
		int serverType = StringUtil.getIntValue(params, 1, 1);

		long totalCount = StringUtil.getIntValue(params, 2, 0);

		int appType = StringUtil.getIntValue(params, 3, 0);//0安装包,1h5

		int gId = 0;

		String tableIdStr = strParams.get(0);
		long tableId = Long.parseLong(tableIdStr);

		long modeId=0;

		String wanfaIds = "";
		if (strParams.size()>=2){
			modeId = NumberUtils.toLong(strParams.get(1),-1);
			if (strParams.size()>=3){
				gId = NumberUtils.toInt(strParams.get(2),-1);
				if (strParams.size()>=4) {
					wanfaIds = strParams.get(3);
				}
			}
		}

		long userId = player.getUserId();

		StringBuilder strBuilder=new StringBuilder("loadServer");
		strBuilder.append("|").append(gameType);
		strBuilder.append("|").append(tableId);
		strBuilder.append("|").append(userId);
		strBuilder.append("|").append(modeId);
		strBuilder.append("|").append(serverType);
		strBuilder.append("|").append(gId);
		strBuilder.append("|").append(wanfaIds);
        LogUtil.msgLog.info("GetServerCommand|start|" + strBuilder.toString());
		if (userId != 0) {

//			UserGameSite userGameSite = GameSiteDao.getInstance().queryUserGameSite(userId);
//			if (userGameSite != null && userGameSite.getGameSiteId() > 0 && userGameSite.getRoundNum() > 0) {
//				this.writeMsg(-1, null);
//				return "result";
//			}

			RegInfo info = UserDao.getInstance().selectUserByUserId(userId);

			if (info!=null){
				//局数+充值》》》用于用户分级
				//((-usedCards+cards)/150+totalCount)
				totalCount=(-info.getUsedCards()+info.getCards())/150+info.getTotalCount();
				if (info.getPlayingTableId() != 0) {
					tableId = info.getPlayingTableId();
				}
			}
		}

		boolean loadFromCheckNet=true;
		Map<String, Object> result = new HashMap<String, Object>();
		Server server = null;
		int serverId = 0;
		String matchType = "";
		String[] gameUrls=null;
		if (tableId==1&&modeId>0){//无房号金币场
			matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");
			serverType = NumberUtils.toInt(appType == 1 ? ResourcesConfigsUtil.loadServerPropertyValue("gold_h5_server_type") : ResourcesConfigsUtil.loadServerPropertyValue("gold_server_type"),serverType);

			loadFromCheckNet=false;
			GoldRoom goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId));
			if ("2".equals(matchType) && goldRoom == null) {
				goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId), "1");
			}

			if (goldRoom!=null){
				server=ServerManager.loadServer(goldRoom.getServerId());
			}
			if (server==null){
				server=ServerManager.loadServer(gameType,serverType);
			}
			if (server!=null){
				serverId = server.getId();
				gameUrls = CheckNetUtil.loadGameUrl(serverId,totalCount);
				if (gameUrls!=null){
					server=new Server();
					server.setId(serverId);
					if (gameUrls[0].startsWith("ws:")){
						server.setChathost(gameUrls[0]);
					}else if (gameUrls[0].startsWith("wss:")){
						server.setWssUri(gameUrls[0]);
					}
				}
			}
		}else if (tableId<=0&&modeId>0){//军团房
			try {
				if (gId>0) {
					// 俱乐部快速加入
					GroupTable groupTable = null;
					GroupTableConfig gtc = GroupDao.getInstance().loadGroupTableConfig(modeId);
					if (gtc == null) {
						player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
						return;
					}
					try {
						groupTable = GroupDao.getInstance().loadRandomSameModelTable(modeId, gId);
					}catch (Throwable th){
						LogUtil.e("get server err-->userId:"+player.getUserId()+",modeId:"+modeId);
					}
					if (groupTable != null) {
						serverId = NumberUtils.toInt(groupTable.getServerId());
						tableId=groupTable.getTableId();
						gameType=getGameType(groupTable.getTableMsg());
						server = ServerManager.loadServer(serverId);
						if (server == null) {
							LogUtil.e("get server err-->serverId:"+serverId+",tableId:"+groupTable.getServerId());
							tableId=0;
							gameUrls= CheckNetUtil.loadGameUrl(serverId,totalCount);
							if (gameUrls!=null){
								server=new Server();
								server.setId(serverId);
								if (gameUrls[0].startsWith("ws:")){
									server.setChathost(gameUrls[0]);
								}else if (gameUrls[0].startsWith("wss:")){
									server.setWssUri(gameUrls[0]);
								}
								loadFromCheckNet=false;
							}
						}
					}
					strBuilder.append(",clubTableId=").append(tableId);
				} else {
					GroupTable groupTable = GroupDao.getInstance().loadRandomGroupTable(modeId);

					if (groupTable!=null){
						tableId=groupTable.getTableId();
						strBuilder.append(",groupTableId=").append(tableId);
					}

					if (gameType<=0){
						GroupTableConfig groupTableConfig=GroupDao.getInstance().loadGroupTableConfig(modeId);
						if (groupTableConfig!=null){
							gameType=groupTableConfig.getGameType();
							strBuilder.append(",groupGameType=").append(gameType);
						}
					}
				}
			}catch (Exception e){
				LogUtil.e("Exception:"+e.getMessage(),e);
			}
		}else if (tableId > 0 && GoldRoomUtil.isNotGoldRoom(tableId)) {
			RoomBean room = TableDao.getInstance().queryRoom(tableId);
			if (room != null) {
				serverId = room.getServerId();
				gameType = room.getType();
				if(serverId>0){
					server = ServerManager.loadServer(serverId);

					if (server==null){
						gameUrls= CheckNetUtil.loadGameUrl(serverId,totalCount);
						if (gameUrls!=null){
							server=new Server();
							server.setId(serverId);
							if (gameUrls[0].startsWith("ws:")){
								server.setChathost(gameUrls[0]);
							}else if (gameUrls[0].startsWith("wss:")){
								server.setWssUri(gameUrls[0]);
							}
							loadFromCheckNet=false;
						}
					}
				}else if (room.getUsed()==0){
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
					return;
				}
			}else{
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
				return;
			}
			if (server == null) {
				result.put("code", -1);
				player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
				return;
			}

		}else if (GoldRoomUtil.isGoldRoom(tableId)){
			serverType = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("gold_server_type"),serverType);
			loadFromCheckNet=false;
			GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(tableId);
			if (goldRoom==null||goldRoom.isOver()){
				goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId));
			}

			if (goldRoom!=null){
				server=ServerManager.loadServer(goldRoom.getServerId());
			}
			if (server==null){
				server=ServerManager.loadServer(gameType,serverType);
			}
			if (server!=null){
				serverId = server.getId();
				gameUrls = CheckNetUtil.loadGameUrl(serverId,totalCount);
				if (gameUrls!=null){
					server=new Server();
					server.setId(serverId);
					if (gameUrls[0].startsWith("ws:")){
						server.setChathost(gameUrls[0]);
					}else if (gameUrls[0].startsWith("wss:")){
						server.setWssUri(gameUrls[0]);
					}
				}
			}
		}

		if(gameType == 0 && serverType == 4) {//serverType为4：现金红包瓜分活动
			server = ServerManager.loadServer(gameType, serverType);
		}else if (gameType != 0 && tableId == 0) {
			// 创建房间的时候
			server = ServerManager.loadServer(gameType,serverType);
		}

		if (server==null){
			LogUtil.errorLog.error("get server error:player="+player.getUserId()+",strs="+strParams+",ints="+params);
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
			return;
		}

		if (!StringUtils.isBlank(wanfaIds) && gameType>0) {
			boolean check = false;
            String[] wanfas = wanfaIds.split(",");
            for (String wanfa : wanfas) {
                if (wanfa.equals(String.valueOf(gameType))) {
                    check = true;
                    break;
                }
            }

			if (!check) {
				LogUtil.errorLog.error("get server error:player="+player.getUserId()+",strs="+strParams+",ints="+params+",gameType:"+gameType);
				if (tableId != 0) {
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_212));
				} else {
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_58));
				}
				return;
			}
		}

		Map<String, Object> serverMap = new HashMap<>();
		serverMap.put("serverId", server.getId());

		boolean useSsl = SslUtil.hasSslHandler(player.getMyWebSocket().getCtx());

		if (loadFromCheckNet){
			serverMap.put("httpUrl", useSsl?server.getHttpsUri():server.getHost());
			gameUrls= CheckNetUtil.loadGameUrl(server.getId(),totalCount);
		}

		if (gameUrls==null){
			serverMap.put("connectHost",useSsl ? server.getWssUri() : server.getChathost());
			serverMap.put("connectHost1","");
			serverMap.put("connectHost2","");
		}else{
			String url0;
			if (useSsl){
				url0 = (StringUtils.isNotBlank(gameUrls[0])&&gameUrls[0].startsWith("wss:"))?gameUrls[0]:server.getWssUri();
			}else{
				url0 = (StringUtils.isNotBlank(gameUrls[0])&&gameUrls[0].startsWith("ws:"))?gameUrls[0]:server.getChathost();
			}

			serverMap.put("connectHost",url0);
			serverMap.put("connectHost1",gameUrls[1]);
			serverMap.put("connectHost2",gameUrls[2]);
		}

		result.put("server", serverMap);
		result.put("blockIconTime", 0);
		result.put("code", 0);
		// 俱乐部快速加入切服返回桌子号
		result.put("tId", (modeId>0&&gId>0)?tableId:0);

		strBuilder.append("|").append(result);
        LogUtil.msgLog.info("GetServerCommand|end|" + strBuilder.toString());
		player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
	}

    private int getGameType(String tableMsg) {
		try {
			int gameType = 0;
			JSONObject jsonObject = JSONObject.parseObject(tableMsg);
			String ints = jsonObject.getString("ints");
			if (!StringUtils.isBlank(ints)) {
				gameType = NumberUtils.toInt(ints.split(",")[1]);
			}
			return gameType;
		} catch (Exception e) {
			LogUtil.e("getGameType err-->tableMsg:"+tableMsg);
			return -1;
		}
    }

    @Override
	public void setMsgTypeMap() {

	}

}
