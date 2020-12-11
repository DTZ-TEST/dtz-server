package com.sy599.game.qipai.ldfpf.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.ldfpf.been.LdfpfPlayer;
import com.sy599.game.qipai.ldfpf.been.LdfpfTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand<LdfpfPlayer> {
	@Override
	public void execute(LdfpfPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		LdfpfTable table = player.getPlayingTable(LdfpfTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
			int pai = req.getParams(0);
			player.setFangZhao(1);

            StringBuilder sb = new StringBuilder("Ldfpf");
            sb.append("|").append(table.getId());
            sb.append("|").append(table.getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append((player.isAutoPlay() ? 1 : 0));
            sb.append("|").append("fangZhao");
            sb.append("|").append(pai);

			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));

            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());

			table.play(player, cards, 0);

			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			}
		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
