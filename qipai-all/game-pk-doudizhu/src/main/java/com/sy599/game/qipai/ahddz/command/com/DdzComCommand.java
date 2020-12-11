package com.sy599.game.qipai.ahddz.command.com;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.ahddz.bean.DdzPlayer;
import com.sy599.game.qipai.ahddz.bean.DdzTable;
import com.sy599.game.qipai.ahddz.bean.DdzTable.DdzPhase;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DdzComCommand extends BaseCommand<DdzPlayer> {

	@Override
	public void execute(DdzPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		DdzTable table = player.getPlayingTable(DdzTable.class);
		if (table == null) {
			return;
		}

		player.setAutoPlay(false,table);
		player.setLastOperateTime(System.currentTimeMillis());

		if (req.getCode()==131){
			return;
		}

		synchronized (table) {
			if (table.getNowDisCardSeat() != player.getSeat()) {
				player.writeErrMsg(LangMsg.code_36);
				return;
			}
//			if (req.getCode() == WebSocketMsgType.req_com_ddz_cutcard) {
//				if (!table.isAllReady()) {
//					player.writeErrMsg("还有玩家没有准备");
//					return;
//				}
//				table.cutCard();
//				return;
//			}
			if (req.getCode() == WebSocketMsgType.req_com_ddz_roblandlord) {
				int action = req.getParams(0);
				if (table.getPhase() != DdzPhase.robBanker) {
					player.writeErrMsg("抢庄阶段错误");
					return;
				}
				
				// 不能重复抢庄操作
				if(table.getRobLandLordMap().containsKey(player.getSeat())) {
					return;
				}

				if(action < 0 || action > 3) {
					player.writeErrMsg(LangMsg.code_3);
					return;
				}
				
				// action 只能大于桌上抢庄分数中的最大值，或者为0
				if(action != 0) {
					if(action <= table.getMaxRobPoint()) {
						return;
					}
				}

				table.rob(player, action);
				return;
			}
		}
	}

	@Override
	public void setMsgTypeMap() {
	}

}
