package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSON;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.GroupRoomUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupRoomInvitationCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intParams = req.getParamsList();
        int intSize = intParams == null ? 0 : intParams.size();
        if (intSize < 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        int type = intParams.get(0);
        //获取邀请列表
        if (type == 0) {
            BaseTable table = player.getPlayingTable();
            if (table == null) {
                player.writeErrMsg("您不在房间里，无法获取可邀请的成员");
                return;
            }

            if (!table.isGroupRoom()) {
                player.writeErrMsg("不是亲友圈房间，无法获取可邀请的成员");
                return;
            }

            if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau() > 0 || table.getPlayBureau() > 1) {
                player.writeErrMsg("牌局已开始，不能邀请其他玩家加入");
                return;
            }

            if (table.getPlayerCount() >= table.getMaxPlayerCount()) {
                player.writeErrMsg("房间已满员，不可邀请");
                return;
            }

            int count = intSize > 1 ? intParams.get(1) : 5;
            if (count <= 0) {
                count = 5;
            } else if (count > 50) {
                count = 50;
            }

            String groupId = table.loadGroupId();

            List<HashMap<String, Object>> list = GroupDao.getInstance().loadRandomGroupUsers(groupId, count);
            if (list == null || list.size() == 0) {
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, type, count, groupId, "[]");
            } else {
                player.writeComMessage(WebSocketMsgType.com_group_room_invite, type, count, groupId, JSON.toJSONString(list));
            }
        } else if (type == 1) {//一键邀请所有人或指定人员
            BaseTable table = player.getPlayingTable();
            if (table == null) {
                player.writeErrMsg("您不在房间里，无法邀请玩家");
                return;
            }

            if (!table.isGroupRoom()) {
                player.writeErrMsg("不是亲友圈房间，无法邀请玩家");
                return;
            }

            if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau() > 0 || table.getPlayBureau() > 1) {
                player.writeErrMsg("牌局已开始，不能邀请其他玩家加入");
                return;
            }

            if (table.getPlayerCount() >= table.getMaxPlayerCount()) {
                player.writeErrMsg("房间已满员，不可邀请");
                return;
            }

            String groupId = table.loadGroupId();

            GroupTable groupTable = table.getGroupTable();
            if (groupTable == null) {
                groupTable = GroupDao.getInstance().loadGroupTableByKeyId(table.loadGroupTableKeyId());
                table.setGroupTable(groupTable);
            }

            String currentState = GroupDao.getInstance().selectCurrentStateById(groupTable.getKeyId());
            int currentCount = GroupDao.getInstance().selectCurrentCountById(groupTable.getKeyId());
            if (groupTable != null && "0".equals(currentState) && currentCount < groupTable.getMaxCount().intValue()) {

            } else {
                player.writeErrMsg("邀请失败：牌局已开始或已满员");
                return;
            }

            List<Integer> noticeInts = new ArrayList<>(2);
            List<String> noticeStrs = new ArrayList<>(2);
            noticeInts.add(100);

            noticeStrs.add(MessageBuilder.newInstance().builder("name", player.getRawName()).builder("userId", player.getUserId()).builder("icon", player.getHeadimgurl() == null ? "" :player.getHeadimgurl())
                    .builder("currentCount", groupTable.getCurrentCount()).builder("maxCount", groupTable.getMaxCount())
                    .builder("groupId", groupTable.getGroupId()).builder("tableKey", groupTable.getKeyId())
                    .builder("tableId", groupTable.getTableId()).builder("tableMsg", groupTable.getTableMsg()).toString());


            List<String> strParams = req.getStrParamsList();
            String userIdStr = strParams != null && strParams.size() > 0 ? strParams.get(0) : "";
            if (StringUtils.isBlank(userIdStr)) {//邀请所有在线玩家
                List<HashMap<String, Object>> list = GroupDao.getInstance().loadOnlineGroupUsers(groupId, null);
                if (list == null || list.size() == 0) {
                    player.writeErrMsg("邀请失败：该亲友圈没有可邀请的成员");
                    return;
                }

                Map<Integer, StringBuilder> userServer = new HashMap<>();
                for (HashMap<String, Object> map : list) {
                    int serverId = CommonUtil.object2Int(map.getOrDefault("enterServer", 0));
                    if (serverId == GameServerConfig.SERVER_ID) {
                        Player player1 = PlayerManager.getInstance().getPlayer(CommonUtil.object2Long(map.getOrDefault("userId", 0)));
                        if (player1 != null && GroupRoomUtil.canJoinTable(player1)) {
                            player1.writeComMessage(WebSocketMsgType.com_group_room_invite, noticeInts, noticeStrs);
                        }
                    } else {
                        StringBuilder strBuilder = userServer.get(serverId);
                        if (strBuilder == null) {
                            strBuilder = new StringBuilder();
                            strBuilder.append(map.getOrDefault("userId", 0));
                            userServer.put(serverId, strBuilder);
                        } else {
                            strBuilder.append(",").append(map.getOrDefault("userId", 0));
                        }
                    }
                }

                for (Map.Entry<Integer, StringBuilder> kv : userServer.entrySet()) {
                    Server server = ServerManager.loadServer(kv.getKey());
                    if (server != null) {
                        Map<String, String> paramsMap = new HashMap<>();
                        paramsMap.put("type", "notice");
                        paramsMap.put("checkRoom", "1");
                        paramsMap.put("userId", kv.getValue().toString());
                        paramsMap.put("code", String.valueOf(WebSocketMsgType.com_group_room_invite));
                        paramsMap.put("ints", JSON.toJSONString(noticeInts));
                        paramsMap.put("strs", JSON.toJSONString(noticeStrs));
                        HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                    }
                }

                player.writeErrMsg("已成功发送邀请!");
            } else if (CommonUtil.isPureNumber(userIdStr)) {//邀请单个在线玩家
                GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userIdStr), groupId);
                if (groupUser == null) {
                    player.writeErrMsg("邀请失败：该玩家不是亲友圈成员");
                    return;
                } else {
                	if (groupUser.getRefuseInvite() == 0) {
                		player.writeErrMsg("邀请失败：该玩家设置拒绝亲友圈游戏邀请");
                        return;
                	}
                    Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
                    if (player1 != null) {
                        if (GroupRoomUtil.canJoinTable(player1)) {
                            player1.writeComMessage(WebSocketMsgType.com_group_room_invite, noticeInts, noticeStrs);
                        }
                    } else {
                        int serverId = CommonUtil.object2Int(UserDao.getInstance().getUserServerId(groupUser.getUserId()+""));
                        Server server = ServerManager.loadServer(serverId);
                        if (server != null) {
                            Map<String, String> paramsMap = new HashMap<>();
                            paramsMap.put("type", "notice");
                            paramsMap.put("checkRoom", "1");
                            paramsMap.put("userId", groupUser.getUserId()+"");
                            paramsMap.put("code", String.valueOf(WebSocketMsgType.com_group_room_invite));
                            paramsMap.put("ints", JSON.toJSONString(noticeInts));
                            paramsMap.put("strs", JSON.toJSONString(noticeStrs));
                            HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                        }
                    }
                }
                player.writeErrMsg("已成功发送邀请!");
            } else {//邀请多个在线玩家
                String[] strs = userIdStr.split(",");
                List<String> userIds = new ArrayList<>(strs.length);
                for (String str : strs) {
                    if (CommonUtil.isPureNumber(str)) {
                        userIds.add(str);
                    }
                }

                List<HashMap<String, Object>> list = GroupDao.getInstance().loadOnlineGroupUsers(groupId, userIds);
                if (list == null || list.size() == 0) {
                    player.writeErrMsg("邀请失败：被邀请的玩家不在线或已加入房间");
                    return;
                }

                Map<Integer, StringBuilder> userServer = new HashMap<>();
                for (HashMap<String, Object> map : list) {
                    int serverId = CommonUtil.object2Int(map.getOrDefault("enterServer", 0));
                    if (serverId == GameServerConfig.SERVER_ID) {
                        Player player1 = PlayerManager.getInstance().getPlayer(CommonUtil.object2Long(map.getOrDefault("userId", 0)));
                        if (player1 != null && GroupRoomUtil.canJoinTable(player1)) {
                            player1.writeComMessage(WebSocketMsgType.com_group_room_invite, noticeInts, noticeStrs);
                        }
                    } else {
                        StringBuilder strBuilder = userServer.get(serverId);
                        if (strBuilder == null) {
                            strBuilder = new StringBuilder();
                            strBuilder.append(map.getOrDefault("userId", 0));
                            userServer.put(serverId, strBuilder);
                        } else {
                            strBuilder.append(",").append(map.getOrDefault("userId", 0));
                        }
                    }
                }

                for (Map.Entry<Integer, StringBuilder> kv : userServer.entrySet()) {
                    Server server = ServerManager.loadServer(kv.getKey());
                    if (server != null) {
                        Map<String, String> paramsMap = new HashMap<>();
                        paramsMap.put("type", "notice");
                        paramsMap.put("checkRoom", "1");
                        paramsMap.put("userId", kv.getValue().toString());
                        paramsMap.put("code", String.valueOf(WebSocketMsgType.com_group_room_invite));
                        paramsMap.put("ints", JSON.toJSONString(noticeInts));
                        paramsMap.put("strs", JSON.toJSONString(noticeStrs));
                        HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                    }
                }

                player.writeErrMsg("已成功发送邀请!");
            }
        }
    }

}
