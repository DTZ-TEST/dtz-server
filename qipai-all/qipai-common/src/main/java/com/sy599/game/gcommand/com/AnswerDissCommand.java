package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 应答解散
 * 
 * @author lc
 */
public class AnswerDissCommand extends BaseCommand {
	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		BaseTable table = player.getPlayingTable();
		if (table == null) {
			return;
		}

		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		boolean agree = req.getParams(0) == 1;

		table.answerDiss(player.getSeat(), req.getParams(0));
		if (agree) {
			table.checkDiss(player);
		} else {
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_nodisstable, player.getUserId() + "", player.getName(), table.getOnTablePlayerNum());
			for (Player tableplayer : table.getSeatMap().values()) {
				tableplayer.writeSocket(com.build());
			}
			table.clearAnswerDiss();
			//table.setLastActionTime(TimeUtil.currentTimeMillis());
		}
		LogUtil.msgLog.info("table answer diss:" + table.getId() + " player:" + player.getUserId() + " play:" + table.getPlayType() + " pb" + table.getPlayBureau() + " agree:" + agree);
//		if (table.getAnswerDissCount() >= table.getMaxPlayerCount()) {
//			table.clearAnswerDiss();
//		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
