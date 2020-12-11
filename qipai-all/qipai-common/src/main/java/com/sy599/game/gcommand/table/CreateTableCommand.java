package com.sy599.game.gcommand.table;

import java.util.*;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CreateTableCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		if (player.getMyExtend().isGroupMatch()){
			player.writeErrMsg("正在为您匹配房间，请不要进行其他操作");
			return;
		}

		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		List<Integer> params = req.getParamsList();
		List<String> strParams = req.getStrParamsList();

		TableManager.getInstance().createTable(player, params, strParams, 0, 0,null);
	}

	@Override
	public void setMsgTypeMap() {
		msgTypeMap.put(CreateTableRes.class, WebSocketMsgType.sc_createtable);
	}

}
