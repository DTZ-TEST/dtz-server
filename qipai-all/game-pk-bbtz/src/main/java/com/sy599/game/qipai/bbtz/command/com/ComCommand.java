package com.sy599.game.qipai.bbtz.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.bbtz.bean.BbtzPlayer;
import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.tool.CardTool;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class ComCommand extends BaseCommand<BbtzPlayer> {

	@Override
	public void execute(BbtzPlayer player, MessageUnit message) throws Exception {
		BbtzTable table = player.getPlayingTable(BbtzTable.class);
		if (table == null) {
			return;
		}

		ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());

            if (req.getCode() == 131) {
                return;
            }

            synchronized (table) {
                switch (req.getCode()) {
                    case WebSocketMsgType.REQ_BBTZ_CHUI:
                        CardTool.chui(table, player, req.getParams(0));
                        break;
                    case WebSocketMsgType.REQ_BBTZ_KAIQIANG:
                        CardTool.kaiQiang(table, player, req.getParams(0));
                        break;
                    case WebSocketMsgType.REQ_BBTZ_ROB_BANKER:
                        CardTool.robBanker(table, player, req.getParams(0));
                        break;
                    case WebSocketMsgType.REQ_BBTZ_DOU:
                        CardTool.dou(table, player, req.getParams(0));
                        break;
                }
            }
        }
	}
	
	@Override
	public void setMsgTypeMap() {
	}

}
