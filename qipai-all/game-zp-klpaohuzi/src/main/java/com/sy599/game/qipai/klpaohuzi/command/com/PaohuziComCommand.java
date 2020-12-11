package com.sy599.game.qipai.klpaohuzi.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.klpaohuzi.bean.KlPaohuziPlayer;
import com.sy599.game.qipai.klpaohuzi.bean.KlPaohuziTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        KlPaohuziPlayer player = (KlPaohuziPlayer) player0;
        ComReq req = (ComReq) this.recognize(ComReq.class, message);

        KlPaohuziTable table = player.getPlayingTable(KlPaohuziTable.class);
        if (table == null) {
            return;
        }
        if (req.getCode() == WebSocketMsgType.REQ_DTZ_AUTOPLAY) {
            //取消托管
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
        } else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
            int pai = req.getParams(0);
            LogUtil.msgLog.info("----tableId:" + table.getId() + "---userName:" + player.getName() + "------->已确认放招:" + pai);
            player.setFangZhao(1);
            List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
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
