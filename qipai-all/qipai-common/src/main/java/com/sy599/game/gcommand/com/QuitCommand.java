package com.sy599.game.gcommand.com;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 退出
 *
 * @author lc
 */
public class QuitCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.isPlayingMatch()){
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }

        if (GoldRoomUtil.isGoldRoom(player.getPlayingTableId())) {
            synchronized (Constants.GOLD_LOCK) {
                String matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");
                if ("2".equals(matchType)) {
                    BaseTable baseTable = TableManager.getInstance().getTable(player.getPlayingTableId());
                    if (baseTable != null) {
                        if (baseTable.canQuit(player)) {
                            boolean quit = baseTable.quitPlayer(player);
                            if (quit) {
                                baseTable.onPlayerQuitSuccess(player);
                                GoldRoomDao.getInstance().deleteGoldRoomUser(baseTable.getId(), player.getUserId());
                                GoldRoomDao.getInstance().updateGoldRoom(baseTable.getId(), -1, "0");
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    }
                } else {
                    GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(), player.getUserId());
                    if (goldRoomUser != null) {
                        GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                        if (goldRoom != null) {
                            if (goldRoom.isPlaying()) {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
                                return;
                            } else if (goldRoom.isNotStart()) {
                                GoldRoomDao.getInstance().deleteGoldRoomUser(goldRoomUser.getRoomId(), player.getUserId());
                                GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, "0");
                                player.setPlayingTableId(0);
                                player.saveBaseInfo();
                            } else {
                                player.setPlayingTableId(0);
                                player.saveBaseInfo();
                            }
                        } else {
                            player.setPlayingTableId(0);
                            player.saveBaseInfo();
                        }
                    } else {
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    }
                    ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, player.getUserId() + "");
                    player.writeSocket(com.build());
                }
            }
            return;
        }

        BaseTable table = player.getPlayingTable();
        if (table == null) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1));
            return;
        }

        if (table.getPlayerMap().containsKey(player.getUserId()) && player.getMyExtend().getPlayerStateMap().get("2") != null) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_9, table.getPlayBureau()));
            return;
        }

        String pre = player.getMyExtend().getPlayerStateMap().get("1");
        int groupId = 0;
        if (table.isGroupRoom()) {
            groupId = Integer.parseInt(table.loadGroupId());
        }
        if ("1".equals(pre)) {
            table.getRoomPlayerMap().remove(player.getUserId());
            player.clearTableInfo();
            player.getMyExtend().getPlayerStateMap().clear();
            player.changeExtend();
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, player.getUserId() + "", table.getPlayType(), 1, 0, groupId, groupId);
            player.writeSocket(com.build());
            return;
        } else if ("0".equals(pre)) {
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, player.getUserId() + "", table.getPlayType(), 1, 0, groupId, groupId);
            player.writeSocket(com.build());
            return;
        }

        if (!table.getPlayerMap().containsKey(player.getUserId()) && !table.getRoomPlayerMap().containsKey(player.getUserId())) {
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, player.getUserId() + "", table.getPlayType());

            GeneratedMessage msg = com.build();
            player.writeSocket(msg);
            return;
        }

        if (table.isCompetition()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_20));
            return;
        }
        if (table.getPlayBureau() != 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_9, table.getPlayBureau()));
            return;
        }

        if (table.getState() != table_state.ready || !table.canQuit(player)) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
            return;
        }

        boolean quit = table.quitPlayer(player);
        if (quit) {
            table.onPlayerQuitSuccess(player);

            // 修改代开房间玩家的信息记录
            table.updateDaikaiTablePlayer();
            // 修改room表
            table.updateRoomPlayers();
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
