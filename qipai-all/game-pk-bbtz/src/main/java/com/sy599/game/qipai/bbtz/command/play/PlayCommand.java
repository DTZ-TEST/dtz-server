package com.sy599.game.qipai.bbtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.qipai.bbtz.bean.BbtzPlayer;
import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.constant.BbtzConstants;
import com.sy599.game.qipai.bbtz.rule.CardType;
import com.sy599.game.qipai.bbtz.tool.CardTypeTool;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand {
	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		BbtzPlayer player = (BbtzPlayer)player0;
		BbtzTable table = player.getPlayingTable(BbtzTable.class);
		if (table == null) {
			return;
		}
		if (table.getState() != table_state.play||table.getTableStatus() != BbtzConstants.TABLE_PLAY||player.getSeat() != table.getNowDisCardSeat()) {
			return;
		}

		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());
		List<Integer> disCardIds = table.getNowDisCardIds();
		CardType cardType = CardTypeTool.jugdeType(cards, table);
		if (playCard.getCardType() != 0) {
			// 该牌局是否能打牌
			int canPlay = table.isCanPlay();
			//其他玩家掉线也可以继续出牌
			if (canPlay != 0 && canPlay != 2) {
				if (canPlay == 1) {
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
				} else if (canPlay == 2) {
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
				}
				return;
			}
			if (cardType == CardType.c0) {
				// 牌型检查错误
				return;
			}
			// 出的牌是否正确
			if (!player.getHandPais().containsAll(cards)) {
				LogUtil.d_msg(player.getUserId() + "-discard-no cards-->" + JacksonUtil.writeValueAsString(cards));
				return;
			}
			if (table.getDisCardSeat() != 0 && table.getDisCardSeat() != player.getSeat() && !table.checkRound() && disCardIds != null && !disCardIds.isEmpty()) {
				// 上一张的牌不是自己出的
				//if (disCardIds != null && !disCardIds.isEmpty()) {
					// 检查牌能不能出
					int check = CardTypeTool.cardTypeCompare(cards, cardType, disCardIds, table);
					if (check != 1) {
						LogUtil.d_msg(player.getUserId() + "-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + JacksonUtil.writeValueAsString(disCardIds));
						return;
					}
				//}
			} else {
				// 新的一轮开始
				if(cardType == CardType.c111222){//保存飞机长度
					table.setPlaneLength(CardTypeTool.getPlaneLen(cards));
				}
			}
		} else {
			// 是否真的要不起
			// 桌子的没有牌
			if (disCardIds.isEmpty()) {
				return;
			}
			// 桌子上的牌是自己出的
			if(table.getDisCardSeat() == player.getSeat()){
				return;
			}
//			if (table.getNowDisCardSeat() == player.getSeat()) {
//				return;
//			}
		}

		player.setAutoPlay(false,table);
		player.setLastOperateTime(System.currentTimeMillis());

		table.playCommand(player, cards, cardType);
	}

	@Override
	public void setMsgTypeMap() {
		msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
		msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
	}

}
