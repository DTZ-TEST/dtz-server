package com.sy599.game.gcommand.play;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class StartNextCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		BaseTable table = player.getPlayingTable();

		if (table == null || table.isGoldRoom() || player.getSeat() <= 0) {
			return;
		}
		player.setLastActionBureau(table.getPlayBureau());  //记录最近操作的局号
		if (player.getState() == player_state.entry || player.getState() == player_state.over) {
		} else {
			// player.writeErrMsg("已经开始了下一局");
			return;
		}
		ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
		player.writeComMessage(WebSocketMsgType.res_code_isstartnext);

		GeneratedMessage msg1 = com.build();

		// 是否能切牌准备
		if (player.isStartNextReady()) {
			table.ready(player);
			for (Player tableplayer : table.getSeatMap().values()) {
				if (tableplayer.getUserId() == player.getUserId()) {
					continue;
				}
				tableplayer.writeSocket(msg1);
			}
			for (Player player0 : table.getRoomPlayerMap().values()) {
				player0.writeSocket(msg1);
			}
		}


		if (table.isTest()) {
			for (Player tableplayer : table.getSeatMap().values()) {
				if (tableplayer.isRobot())
					table.ready(tableplayer);
			}
		}

		List<Long> news = new ArrayList<>();
		long mUserId = player.getUserId();
		synchronized (table) {
			// 加入等待下一局的玩家
            if(!table.checkCredit()){
                return;
            }
			boolean joinWaitNext = table.joinWaitNext();
			if (joinWaitNext) {
				for (Player player0 : table.getRoomPlayerMap().values()) {
					String str = player0.getMyExtend().getPlayerStateMap().get("2");
					if ("0".equals(str)) {
						if (table.getPlayerMap().containsKey(player0.getUserId())) {
							continue;
						}
						if (table.joinPlayer(player0)) {
							news.add(player0.getUserId());
							table.ready(player0);
							TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
							joinRes.setPlayer(player0.buildPlayInTableInfo());
							// 玩法
							joinRes.setWanfa(table.getPlayType());
							GeneratedMessage msg2 = joinRes.build();
							//
							for (Player tablePlayer : table.getSeatMap().values()) {
								// 如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
								if (mUserId != tablePlayer.getUserId()) {
									if (tablePlayer.getUserId() == player0.getUserId()) {
										// tablePlayer.writeSocket(table.buildCreateTableRes(player0.getUserId(),true));
									} else {
										tablePlayer.writeSocket(msg2);
									}
								}
							}

							for (Player player1 : table.getRoomPlayerMap().values()) {
								if (mUserId != player1.getUserId()) {
									player1.writeSocket(msg2);
								}
							}

						} else {
							player0.getMyExtend().getPlayerStateMap().remove("2");
							player0.getMyExtend().getPlayerStateMap().remove("cur");
							player0.changeExtend();
							player0.writeErrMsg(LangHelp.getMsg(LangMsg.code_5, table.getId()));
						}
					}
				}
			}
		}

		//
		// if (SharedConstants.isPayPhz(table.getPlayType())) {
		// // 是跑胡子
		// // 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
		// // table.startNext(com);
		// player.writeSocket(table.buildCreateTableRes(player.getUserId()));
		// // table.startNext(com);
		// table.ready();
		// table.checkDeal();
		//
		// } else {
		if (GameUtil.isPlaySp(table.getPlayType())) {
			for (Long temp : news) {
				table.getPlayerMap().get(temp).writeSocket(table.buildCreateTableRes(temp, true, false));

				if (table.getPlayerMap().get(temp).getIsOnline() == 0) {
					table.broadIsOnlineMsg(temp, SharedConstants.table_offline);
				}
			}

			for (Player player0 : table.getRoomPlayerMap().values()) {
				TableRes.CreateTableRes.Builder res0 = table.buildCreateTableRes(player0.getUserId(), true, false).toBuilder();
				res0.setFromOverPop(0);
				player0.writeSocket(res0.build());
			}

			// 如果是最后一个点继续的人需要告诉前台
			boolean isLastStart = false;
			if (player.getState() == player_state.play) {
				isLastStart = true;
			}
			TableRes.CreateTableRes.Builder res = table.buildCreateTableRes(player.getUserId(), true, isLastStart).toBuilder();
			res.setFromOverPop(1);
			player.writeSocket(res.build());
			table.ready();
			table.checkDeal(player.getUserId());
			table.startNext();
		} else {
			table.ready();
			table.checkDeal(player.getUserId());
			// 前台处理有问题，两个消息因为头像原因 有前后问题，直接发重连
			// player.writeSocket(table.buildCreateTableRes(player.getUserId(),
			// true));
			// if (isSend){
			// 如果是最后一个点继续的人需要告诉前台
			boolean isLastStart = false;
			if (player.getState() == player_state.play) {
				isLastStart = true;
			}

			TableRes.CreateTableRes.Builder res = table.buildCreateTableRes(player.getUserId(), true, isLastStart).toBuilder();
			res.setFromOverPop(1);
			player.writeSocket(res.build());
			// }
			for (Long temp : news) {
				table.getPlayerMap().get(temp).writeSocket(table.buildCreateTableRes(temp, true, false));

				if (table.getPlayerMap().get(temp).getIsOnline() == 0) {
					table.broadIsOnlineMsg(temp, SharedConstants.table_offline);
				}
			}

			for (Player player0 : table.getRoomPlayerMap().values()) {
				TableRes.CreateTableRes.Builder res0 = table.buildCreateTableRes(player0.getUserId(), true, false).toBuilder();
				res0.setFromOverPop(0);
				player0.writeSocket(res0.build());
			}
			table.startNext();
			// }
		}
	}

	@Override
	public void setMsgTypeMap() {

	}

}
