package com.sy599.game.manager;

import com.sy599.game.msg.serverPacket.*;
import com.sy599.game.msg.serverPacket.ActivityMsg.ActivityConfigInfo;
import com.sy599.game.msg.serverPacket.ActivityMsg.ActivityLists;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.ComMsg.PingRes;
import com.sy599.game.msg.serverPacket.MessageResMsg.NoticelistRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.msg.serverPacket.RankMsg.RankLists;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg.BaiRenTableRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.JoinTableRes;
import com.sy599.game.msg.serverPacket.TaskMsg.TaskLists;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class MsgManager {
	private static MsgManager _inst = new MsgManager();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();
	static {
		msgClassToMsgTypeMap.put(ComRes.class, WebSocketMsgType.sc_com);

		msgClassToMsgTypeMap.put(CreateTableRes.class, WebSocketMsgType.sc_createtable);
		msgClassToMsgTypeMap.put(JoinTableRes.class, WebSocketMsgType.sc_jointable);
		msgClassToMsgTypeMap.put(DealInfoRes.class, WebSocketMsgType.sc_dealcards);
		msgClassToMsgTypeMap.put(TableRes.YjDealInfoRes.class, WebSocketMsgType.sc_dealcards);
		msgClassToMsgTypeMap.put(PingRes.class, WebSocketMsgType.sc_ping);
		msgClassToMsgTypeMap.put(NoticelistRes.class, WebSocketMsgType.sc_message);

		msgClassToMsgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
		msgClassToMsgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
		msgClassToMsgTypeMap.put(PlayMajiangRes.class, WebSocketMsgType.sc_playmajiang);
		msgClassToMsgTypeMap.put(MoMajiangRes.class, WebSocketMsgType.sc_momajiang);
		msgClassToMsgTypeMap.put(PlayCardResMsg.BuFaMoMajiangRes.class, WebSocketMsgType.sc_bufamomajiang);
		msgClassToMsgTypeMap.put(GangMoMajiangRes.class, WebSocketMsgType.sc_csgangplay);
		msgClassToMsgTypeMap.put(PlayPaohuziRes.class, WebSocketMsgType.sc_playpaohuzi);
		msgClassToMsgTypeMap.put(ClosingPhzInfoRes.class, WebSocketMsgType.sc_closingphzinfores);
		msgClassToMsgTypeMap.put(TablePhzResMsg.ClosingAhPhzInfoRes.class, WebSocketMsgType.sc_closingphzinfores);
		msgClassToMsgTypeMap.put(ClosingMjInfoRes.class, WebSocketMsgType.sc_closingmjinfores);
		msgClassToMsgTypeMap.put(ActivityLists.class, WebSocketMsgType.sc_activity);
		msgClassToMsgTypeMap.put(ActivityConfigInfo.class, WebSocketMsgType.sc_single_activity);
		msgClassToMsgTypeMap.put(TableRes.YjClosingInfoRes.class, WebSocketMsgType.sc_closingyjmjinfores);
		msgClassToMsgTypeMap.put(TableGhzResMsg.ClosingGhzInfoRes.class, WebSocketMsgType.sc_closingghzinfores);
		msgClassToMsgTypeMap.put(TaskLists.class, WebSocketMsgType.sc_task);
		msgClassToMsgTypeMap.put(RankLists.class, WebSocketMsgType.sc_rank);
		msgClassToMsgTypeMap.put(BaiRenTableRes.class, WebSocketMsgType.sc_bairen_tableInfo);
		msgClassToMsgTypeMap.put(BaiRenTableMsg.TrendInfo.class, WebSocketMsgType.sc_bairen_trend);
		msgClassToMsgTypeMap.put(BaiRenTableMsg.GameBetInfo.class, WebSocketMsgType.sc_bairen_bet);
		msgClassToMsgTypeMap.put(TingPaiRes.class, WebSocketMsgType.sc_tingpaiinfo);
		msgClassToMsgTypeMap.put(DaPaiTingPaiRes.class, WebSocketMsgType.sc_dapaitingpaiinfo);
		msgClassToMsgTypeMap.put(GoldRoomActivityProto.GoldRoomActivityMsg.class, WebSocketMsgType.sc_goldRoomActivity);
	}

	public static MsgManager getInstance() {

		return _inst;
	}

	public short getMsgType(Class<?> clazz) {
		Short type = msgClassToMsgTypeMap.get(clazz);
		return type == null ? 0 : type;
	}
}
