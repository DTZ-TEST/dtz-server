package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class AutoPlayCommand extends BaseCommand {

	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		DtzPlayer player=(DtzPlayer)player0;
		if(player==null)return;
		DtzTable table = player.getPlayingTable(DtzTable.class);
		if(table==null)return;

		if(table.getIsAutoPlay() != 1){
			return;
		}
//		if(player.getAutoPlay() != 0){
//			return;
//		}
		if(table.getNowDisCardSeat() == player.getSeat()){
			table.setLastActionTime(System.currentTimeMillis());
		}
		player.setAutoPlay(player.getAutoPlay()==1?0:1);

		LogUtil.msg("房间号="+table.getId()+",玩家="+player.getUserId()+"["+player.getName()+"]座位="+player.getSeat()+"手动取消托管"+player.getAutoPlay());
		ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DTZ_AUTOPLAY, player.getSeat(), player.getAutoPlay(), (int)player.getUserId());
		table.broadMsg(build.build());
	}

	@Override
	public void setMsgTypeMap() {
		// TODO Auto-generated method stub

	}

}
