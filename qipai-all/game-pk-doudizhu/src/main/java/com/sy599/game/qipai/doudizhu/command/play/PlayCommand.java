package com.sy599.game.qipai.doudizhu.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.doudizhu.bean.DdzPlayer;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.doudizhu.bean.DdzTable;
import com.sy599.game.qipai.doudizhu.bean.DdzTable.DdzPhase;
import com.sy599.game.qipai.doudizhu.rule.CardType;
import com.sy599.game.qipai.doudizhu.tool.CardTypeTool;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand<DdzPlayer> {
    @Override
    public void execute(DdzPlayer player, MessageUnit message) throws Exception {

        DdzTable table = player.getPlayingTable(DdzTable.class);
        if (table == null) {
            return;
        }

        if (table.getPhase() != DdzPhase.play) {
            return;
        }

        // 该牌局是否能打牌
        int canPlay = table.isCanPlay();
        if (canPlay != 0) {
            if (canPlay == 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
                return;
            } else if (canPlay == 2) {
//				player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_7), player.getHandPais());
            }

        }

        // if (table.getPhase() != DdzPhase.play) {
        // return;
        // }
        if (player.getSeat() != table.getNextDisCardSeat()) {
            return;
        }
        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
        int action = playCard.getCardType();
        List<Integer> cardIds = playCard.getCardIdsList();
        if (!cardIds.isEmpty()) {
            // 检查出牌存在的合法性
            if (!checkCardIs(player, cardIds)) {
                return;
            }

            // 获得出的牌
            CardType cardType = CardTypeTool.jugdeType(cardIds);
            if (cardType == CardType.c0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            if (cardType.getType() != action) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }

            if (!table.canThreeAndOne() && (cardType == CardType.c31 || cardType==CardType.c11122234)) {
                player.writeErrMsg(LangMsg.code_46);
                return;
            }
            if (!table.canThreeAndTwo() && (cardType == CardType.c32|| cardType==CardType.c1112223344)) {
                player.writeErrMsg(LangMsg.code_47);
                return;
            }

            // 牌小，打不起
            if (table.getDisCardSeat() != player.getSeat() && !table.getNowDisCardIds().isEmpty()) {
                if (!CardTypeTool.isBigger(cardIds, table.getNowDisCardIds())) {
                    LogUtil.e("play card error: seat:"+player.getSeat()+" player:"+player.getUserId()+" playCards:"+cardIds+" nowDisCardIds:"+table.getNowDisCardIds());
                    return;
                }
            }

        } else {
            if (action != 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
        }

        player.setAutoPlay(false,table);
        player.setLastOperateTime(System.currentTimeMillis());

        table.playCommand(player, cardIds, action);
    }

    /**
     * 检查出牌的牌本身的合法性（兼容癞子）
     */
    private boolean checkCardIs(Player player, List<Integer> cardIds) {
        List<Integer> copy = new ArrayList<>(player.getHandPais());
        int m = 0;
        int n = 0;
        for (int myCard : player.getHandPais()) {
            if (myCard / 100 == 6) {
                m++;
                copy.remove((Object) myCard);
            }
        }
        for (int card : cardIds) {
            if (card / 100 == 6) {
                n++;
                continue;
            }
            if (!copy.contains(card)) {
                return false;
            }
            copy.remove((Object) card);
        }
        // 癞子牌数目不够
        if (m < n) {
            return false;
        }
        return true;
    }

    @Override
    public void setMsgTypeMap() {

    }

}
