package com.sy599.game.qipai.pdkuai.command.play;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.qipai.pdkuai.bean.PdkPlayer;
import com.sy599.game.qipai.pdkuai.bean.PdkTable;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand<PdkPlayer> {
	@Override
	public void execute(PdkPlayer player, MessageUnit message) throws Exception {
		PdkTable table = player.getPlayingTable(PdkTable.class);
		if (table == null) {
			return;
		}

		if (table.getState() != table_state.play) {
			return;
		}

		if (player.getSeat() != table.getNextDisCardSeat()) {
			return;
		}

		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		if (action == 100){//取消托管
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
			return;
		}else if (action == 101){//托管
			player.setAutoPlay(true,table);
			return;
		}

		List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());
		List<Integer> disCardIds = table.getNowDisCardIds();

//		CardType cardType = CardTypeTool.jugdeType(cards, table);
		CardUtils.Result result = (cards.size()==0||(cards.size()==1&&cards.get(0).intValue()==0))? new CardUtils.Result(0,0,0):CardUtils.calcCardValue(CardUtils.loadCards(cards),table.getSiDai());
		if (result.getType()>0) {
			synchronized (table){
				if (player.getSeat() != table.getNextDisCardSeat()) {
					return;
				}

				// 该牌局是否能打牌
				int canPlay = table.isCanPlay();
				//其他玩家掉线也可以继续出牌
				if (canPlay != 0 && canPlay != 2) {
					if (canPlay == 1) {
						player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
					}
					return;
				}
				// 自己要牌
				LogUtil.d_msg(player.getUserId() + "-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + result.toString());
//				if (cardType == CardType.c0) {
//					// 牌型检查错误
//					return;
//				}
				// 是否第一局必出黑桃3
				if (table.getIsFirstRoundDisThree() == 1) {
					if (table.getPlayBureau() == 1 && table.getDisCardRound() == 0) {
						if (player.getHandPais().contains(403) && !cards.contains(403)) {
							player.writeErrMsg(LangHelp.getMsg(LangMsg.code_17));
							return;
						}
					}
				}

				List<Integer> list = new ArrayList<>(player.getHandPais());
				for (Integer tempCard :cards){
					if (!list.remove(tempCard)){
						LogUtil.d_msg(player.getUserId() + "-discard-no cards-->" + JacksonUtil.writeValueAsString(cards)+",handCards="+player.getHandPais());
						return;
					}
				}

				if (table.getDisCardSeat() != player.getSeat()) {
					// 上一张的牌不是自己出的
					if (disCardIds != null && disCardIds.size()>0) {
						// 如果出的是单张下一家出牌只剩下一张了只能出最大的
//						if (!check(cardType, table, player, cards)) {
//							return;
//						}

//					//暂时调整为3飘数量必须相等才能出牌player
						// 检查牌能不能出
//						int check = CardTypeTool.isCanPlay(cards, disCardIds, true,player,table);
//						if (check != 1) {
//							LogUtil.d_msg(player.getUserId() + "-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + JacksonUtil.writeValueAsString(disCardIds));
//							return;
//						}

						if (cards.size()==1&&table.getSeatMap().get(table.getNextSeat(player.getSeat())).getHandPais().size()==1){
							List<Integer> cards0 = player.getHandPais();
							CardUtils.sortCards(cards0);
							if (CardUtils.loadCardValue(cards.get(0))!=CardUtils.loadCardValue(cards0.get(cards0.size()-1))){
								return;
							}
						}

						CardUtils.Result result1 = CardUtils.calcCardValue(CardUtils.loadCards(disCardIds),table.getSiDai());
						if (result1.getType()<=0){
							return;
						}else{
							if (result1.getType()==33&&table.getSiDai()==3&&cards.size()==7&&result.getType()==4&&result.getMax()>result1.getMax()&&cards.size()==player.getHandPais().size()){
							}else if (result.compareTo(result1)<=0){
								return;
							}else if (result.getType()==3||result.getType()==33){
								if (cards.size()%5!=0){
									if (cards.size()==player.getHandPais().size()){
										if (table.getCard3Eq()==1){
											return;
										}
									}else{
										return;
									}
								}
							}else if (result.getType()==4){
								if (disCardIds.size()!=cards.size()){
									return;
								}
							}
						}
					}else{

						if (result.getType()==3||result.getType()==33){
							if (cards.size()%5!=0){
								if (cards.size()==player.getHandPais().size()){
								}else{
									return;
								}
							}
						}else if (result.getType()==4){
							if (table.getSiDai()+4<cards.size()){
								return;
							}
						}

						// 新的一轮开始
						if(result.getType() == 3){
							table.setIsFirstCardType32(1);
						}else{
							table.setIsFirstCardType32(0);
						}
					}

				} else {

					if (result.getType()==3||result.getType()==33){
						if (cards.size()%5!=0){
							if (cards.size()==player.getHandPais().size()){
							}else{
								return;
							}
						}
					}else if (result.getType()==4){
						if (table.getSiDai()+4<cards.size()){
							return;
						}
					}

					// 新的一轮开始
					if(result.getType() == 3){
						table.setIsFirstCardType32(1);
					}else{
						table.setIsFirstCardType32(0);
					}
					table.clearIsNotLet();
				}
				if(!player.isAutoPlay()) {
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
			}

		} else {
			return;
		}

		table.playCommand(player, cards);
	}

	@Override
	public void setMsgTypeMap() {
		msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
		msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
	}

}
