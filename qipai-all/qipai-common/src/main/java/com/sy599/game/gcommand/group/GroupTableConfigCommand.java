package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.sy599.game.websocket.constant.WebSocketMsgType.res_com_group_table_config;

public class GroupTableConfigCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();
        int paramsSize = params == null ? 0 : params.size();
        int strParamsSize = strParams == null ? 0 : strParams.size();
        if (paramsSize == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        int groupId = params.get(0);
        int groupRoom = strParamsSize > 0 ? Integer.parseInt(strParams.get(0)) : 0;
        GroupUser groupUser = player.getGroupUser();
        if (groupUser == null || groupUser.getGroupId().intValue() != groupId) {
            groupUser = player.loadGroupUser(String.valueOf(groupId));
            if (groupUser == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            }
        }

        MessageBuilder msg = MessageBuilder.newInstance();
        msg.builder("code", 0);
        msg.builder("groupId", groupId);

        // 获取包厢
        List<HashMap<String, Object>> list = GroupDao.getInstance().loadSubGroups(groupId, null);
        if (list == null || list.size() == 0) {
            list = createGroupRoom(groupUser);
        } else {
            List<HashMap<String, Object>> list2 = GroupDao.getInstance().loadAllLastGroupTableConfig(null, String.valueOf(groupId));
            if (list2 != null && list2.size() > 0) {
                for (HashMap<String, Object> map1 : list) {
                    String parentGroup = String.valueOf(map1.get("parentGroup"));
                    String groupId0 = String.valueOf(map1.get("groupId"));
                    for (HashMap<String, Object> map2 : list2) {
                        if (parentGroup.equals(String.valueOf(map2.get("parentGroup"))) && groupId0.equals(String.valueOf(map2.get("groupId")))) {
                            if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("groupTableConfigListSimplify"))){
                                // 清理不需要的字段值
                                map2.remove("gameType");
                                map2.remove("payType");
                                map2.remove("tableOrder");
                                map2.remove("gameCount");
                                map2.remove("playerCount");
                                map2.remove("playCount");
                                map2.remove("descMsg");
                                map2.remove("tableMode");
                                map2.remove("createdTime");
                                map2.remove("parentGroup");
                            }
                            map1.put("config", map2);
                            break;
                        }
                    }
                    if (map1.containsKey("config")) {
                        HashMap<String, Object> config1 = (HashMap<String, Object>) map1.get("config");
                        map1.put("startedNum", GroupDao.getInstance().countGroupStartedTables(parentGroup, String.valueOf(config1.get("keyId"))));
                    }
                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("groupTableConfigListSimplify"))) {
                        // 清理不需要的字段值
                        map1.remove("parentGroup");
                        map1.remove("groupLevel");
                        map1.remove("createdUser");
                        map1.remove("creditAllotMode");
                        map1.remove("creditRate");
                        map1.remove("currentCount");
                        map1.remove("maxCount");
                        map1.remove("isCredit");
                        map1.remove("extMsg");
                        map1.remove("descMsg");
                        map1.remove("content");
                        map1.remove("groupMode");
                        map1.remove("createdTime");
                        map1.remove("modifiedTime");
                    }
                }
            }
        }
        if (list == null || list.size() == 0) {
            msg.builder("list", Collections.emptyList());
        } else {
            msg.builder("list", list);
        }

        List<GroupTableConfig> configList;
        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))) {  //安化多玩法特殊处理
            configList = groupRoom <= 0 ? GroupDao.getInstance().loadGroupTableConfig2(groupId, 0) : GroupDao.getInstance().loadGroupTableConfig2(groupRoom, groupId);
            configList = shrinkageCapacityList(configList);
        } else {
            configList = groupRoom <= 0 ? GroupDao.getInstance().loadGroupTableLastConfig(groupId, 0) : GroupDao.getInstance().loadGroupTableLastConfig(groupRoom, groupId);
        }
        msg.builder("configs", configList == null ? Collections.EMPTY_LIST : configList);
        player.writeComMessage(res_com_group_table_config, groupId, msg.toString());
    }


    /**
     * 安化俱乐部玩法信息清除无用字段
     *
     * @param configs
     * @return
     */
    private List<GroupTableConfig> shrinkageCapacityList(List<GroupTableConfig> configs) {
        for (GroupTableConfig config : configs) {
            config.setDescMsg(null);
            config.setTableMode(null);
            config.setTableName(null);
            config.setTableOrder(null);
            config.setParentGroup(null);
            config.setPlayCount(null);
        }
        return configs;
    }


    @Override
    public void setMsgTypeMap() {
    }


    /**
     * 创建包间
     *
     * @param groupUser
     * @return
     * @throws Exception
     */
    private List<HashMap<String, Object>> createGroupRoom(GroupUser groupUser) throws Exception {
        if (!"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_room_mode"))) {
            return null;
        }
        if (groupUser == null || groupUser.getUserRole() != 0) {
            return null;
        }
        GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupUser.getGroupId(), 0);
        if (groupInfo == null) {
            return null;
        }
        JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
        String str = jsonObject.getString("oq");
        if (str == null || (!str.contains("+q") && str.contains("-q"))) {
            //非快速开房
            return null;
        }
        List<GroupInfo> groupRooms = GroupDao.getInstance().loadAllGroupRoom(groupUser.getGroupId().toString());
        if (groupRooms != null && groupRooms.size() > 0) {
            return null;
        }
        GroupTableConfig config = GroupDao.getInstance().loadLastGroupTableConfig(groupUser.getGroupId(), 0);
        if (config == null) {
            return null;
        }

        Date now = new Date();
        int subId = 1;
        GroupInfo groupNew = new GroupInfo();
        groupNew.setCreatedTime(now);
        groupNew.setCreatedUser(groupUser.getUserId());
        groupNew.setCurrentCount(1);
        groupNew.setDescMsg("");
        groupNew.setGroupId(subId);
        groupNew.setParentGroup(groupUser.getGroupId());
        groupNew.setExtMsg("");
        groupNew.setGroupLevel(0);
        groupNew.setGroupMode(0);
        groupNew.setGroupName("包厢1");
        groupNew.setMaxCount(0);
        groupNew.setGroupState("1");
        groupNew.setModifiedTime(now);
        long subGroupKeyId = GroupDao.getInstance().createGroup(groupNew);

        if (subGroupKeyId > 0) {
            GroupTableConfig configNew = new GroupTableConfig();
            configNew.setCreatedTime(now);
            configNew.setDescMsg(config.getDescMsg());
            configNew.setGroupId(Long.valueOf(subId));
            configNew.setParentGroup(groupUser.getGroupId().longValue());
            configNew.setModeMsg(config.getDescMsg());
            configNew.setPlayCount(0L);
            configNew.setTableMode(config.getTableMode());
            configNew.setTableName(config.getTableName());
            configNew.setTableOrder(config.getTableOrder());
            configNew.setGameType(config.getGameType());
            configNew.setGameCount(config.getGameCount());
            configNew.setPayType(config.getPayType());
            configNew.setPlayerCount(config.getPlayerCount());
            configNew.setConfigState("1");
            long configId = GroupDao.getInstance().createGroupTableConfig(configNew);
            if (configId <= 0) {
                GroupDao.getInstance().deleteGroupInfoByKeyId(String.valueOf(subGroupKeyId));
            }

        }
        return GroupDao.getInstance().loadSubGroups(groupUser.getGroupId(), null);
    }

}
