package com.sy.sanguo.game.dao.group;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.game.bean.group.*;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class GroupDaoManager extends BaseDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDaoManager.class);

    private static GroupDaoManager _inst = new GroupDaoManager();

    public static GroupDaoManager getInstance() {
        return _inst;
    }

    public List<GroupConfig> loadALLGroupConfig() throws Exception{
        return this.getSql().queryForList("group.all_group_config");
    }
    public GroupConfig loadGroupConfig(int groupLevel) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupLevel",String.valueOf(groupLevel));
        return (GroupConfig)this.getSql().queryForObject("group.group_config",map);
    }
    public GroupInfo loadGroupInfo(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (GroupInfo)this.getSql().queryForObject("group.group_info_id",map);
    }

    public GroupInfo loadGroupInfoByKeyId(long keyId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("keyId",String.valueOf(keyId));
        return (GroupInfo)this.getSql().queryForObject("group.group_info_keyId",map);
    }

    public GroupUser loadGroupMaster(String groupId) throws Exception{
        return (GroupUser)this.getSql().queryForObject("group.group_user_master_userId",groupId);
    }

    public HashMap<String,Object> searchGroupInfo(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (HashMap<String,Object>)this.getSql().queryForObject("group.search_group_info_id",map);
    }

    public GroupInfo loadGroupInfo(String groupName,String parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupName",groupName);
        map.put("parentGroup",parentGroup);
        return (GroupInfo)this.getSql().queryForObject("group.group_info_name",map);
    }

    public GroupTable loadRandomGroupTable(long modeId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("configId",String.valueOf(modeId));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        map.put("myDate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return (GroupTable)this.getSql().queryForObject("group.one_group_table_random",map);
    }

    public Integer loadMaxGroupId() throws Exception{
        return (Integer) this.getSql().queryForObject("group.group_info_max_groupId");
    }

    public Integer countSubGroup(String parentGroup) throws Exception{
        return (Integer) this.getSql().queryForObject("group.group_info_count_parentGroup",parentGroup);
    }

    public List<GroupInfo> loadSubGroups(String parentGroup) throws Exception{
        return (List<GroupInfo>) this.getSql().queryForList("group.all_group_info_parentGroup",parentGroup);
    }

    public Object createGroup(GroupInfo groupInfo) throws Exception{
        return this.getSql().insert("group.create_group_info",groupInfo);
    }

    public GroupUser loadGroupUser(long userId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("userId",String.valueOf(userId));
        return (GroupUser)this.getSql().queryForObject("group.group_user_userId",map);
    }

    public GroupTableConfig loadGroupTableConfig(int groupId,int parentGroup, String modeMsg) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        map.put("modeMsg",String.valueOf(modeMsg));
        return (GroupTableConfig)this.getSql().queryForObject("group.one_group_table_config",map);
    }

    public GroupTableConfig loadGroupTableConfig(long keyId) throws Exception{
        return (GroupTableConfig)this.getSql().queryForObject("group.one_group_table_config_keyId",String.valueOf(keyId));
    }

    public Object createGroupUser(GroupUser groupUser) throws Exception{
        return this.getSql().insert("group.create_group_user",groupUser);
    }

    public List<GroupTableConfig> loadGroupTableConfig(int groupId,int parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (List<GroupTableConfig>)this.getSql().queryForList("group.all_group_table_config",map);
    }

    public List<HashMap<String,Object>> statisticsMilitaryExploits(String groupId,String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>();
        map.put("groupId",groupId);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        map.put("parentGroup","0");
        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.statistics_military_exploits",map);
    }

    public List<HashMap<String,Object>> loadGroupUserRecords(String groupId,String userId,String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>();
        map.put("groupId",groupId);
        map.put("userId",userId);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        map.put("parentGroup","0");
        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.load_table_users",map);
    }

    public List<HashMap<String,Object>> loadTableUserInfo(String tableNos) throws Exception{
        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.load_table_user_info",tableNos);
    }

//    public List<HashMap<String,Object>> loadGroupTableConfigNum(String tableNos) throws Exception{
//        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.load_group_table_config_num",tableNos);
//    }

    public List<HashMap<String,Object>> loadTableRecordByTableNo(String tableNo) throws Exception{
        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.load_table_record_tableNo",tableNo);
    }

    public Integer countGroupUser(int groupId,String keyWord) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord",keyWord);
        }
        return (Integer)this.getSql().queryForObject("group.count_group_user_keyword",map);
    }

    public Object createGroupTableConfig(GroupTableConfig groupTableConfig) throws Exception{
        return this.getSql().insert("group.insert_group_table_config",groupTableConfig);
    }

    public GroupReview loadGroupReview0(long groupId,long userId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("userId",String.valueOf(userId));
        return (GroupReview)this.getSql().queryForObject("group.one0_group_review",map);
    }
    public GroupReview loadGroupReviewByKeyId(long keyId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("keyId",String.valueOf(keyId));
        return (GroupReview)this.getSql().queryForObject("group.one_group_review_keyId",map);
    }

    public int updateGroupReviewByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSql().update("group.update_group_review_keyId",map);
    }

    public int updateGroupTableConfigByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSql().update("group.update_group_table_config_keyId",map);
    }

    public int updateGroupUser(String groupName,long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("groupName",groupName);
        return this.getSql().update("group.update_group_user_groupId",map);
    }

    public int updateGroupInfoByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSql().update("group.update_group_info_keyId",map);
    }

    public int updateGroupInfoCount(int count,long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup","0");
        map.put("count",count);
        return this.getSql().update("group.update_group_info_count_groupId",map);
    }

    public int countGroupUser(int groupId) throws Exception{
        return (Integer) this.getSql().queryForObject("group.count_group_user",String.valueOf(groupId));
    }

    public int updateGroupUserByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSql().update("group.update_group_user_keyId",map);
    }

    public int deleteGroupTableConfigByKeyId(Long keyId) throws Exception{
        return this.getSql().delete("group.delete_group_table_config_keyId",keyId.toString());
    }

    public int deleteGroupTableConfig(String groupId,String parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",groupId);
        map.put("parentGroup",parentGroup);
        return this.getSql().delete("group.update_group_table_config_state",map);
    }

    public int deleteGroupUserByKeyId(Long keyId) throws Exception{
        return this.getSql().delete("group.delete_group_user_keyId",keyId.toString());
    }

    public int deleteGroupUserByGroupId(Integer groupId) throws Exception{
        return this.getSql().delete("group.delete_group_user_groupId",groupId.toString());
    }

    public int deleteGroupInfoByGroupId(Integer groupId,Integer parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",groupId.toString());
        map.put("parentGroup",parentGroup.toString());
        return this.getSql().delete("group.delete_group_info_groupId",map);
    }

    public int deleteGroupInfoByParentGroup(Integer parentGroup) throws Exception{
        return this.getSql().delete("group.delete_group_info_parentGroup",parentGroup.toString());
    }

    public int deleteGroupInfoByKeyId(String keyId) throws Exception{
        return this.getSql().delete("group.delete_group_info_keyId",keyId);
    }

    public Object createGroupReview(GroupReview groupReview) throws Exception{
        return this.getSql().insert("group.create_group_review",groupReview);
    }

    public List<GroupReview> loadGroupReviewByUserId(long userId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("userId",String.valueOf(userId));
        return this.getSql().queryForList("group.all0_group_review_userId",map);
    }

    public List<GroupReview> loadGroupReviewByGroupId(long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("groupId",String.valueOf(groupId));
        return this.getSql().queryForList("group.all0_group_review_groupId",map);
    }

    public List<HashMap<String,Object>> loadGroupTables(String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        return (List<HashMap<String,Object>>)this.getSql().queryForList("group.load_group_table_range",map);
    }

    public int updateGroupTableByKeyId(String keyId) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("keyId",keyId);
        return this.getSql().update("group.update_group_table_keyId",map);
    }

    /**
     * 创建10个空玩法的桌子
     * @param groupId
     * @param tableMode 1普通房 2比赛房
     * @param createNum
     */
    public List<Long> createEmptyTableConfigs(long groupId, int tableMode, int createNum) {
        List<GroupTableConfig> list = new ArrayList<>();
        for(int i = 0; i < createNum; i++) {
            GroupTableConfig groupTableConfig = new GroupTableConfig();
            groupTableConfig.setCreatedTime(new Date());
            groupTableConfig.setDescMsg("");
            groupTableConfig.setGroupId(groupId);
            groupTableConfig.setParentGroup(Long.valueOf(0));
            groupTableConfig.setModeMsg("");
            groupTableConfig.setPlayCount(0L);
            groupTableConfig.setTableMode(tableMode + "");
            groupTableConfig.setTableName("");
            groupTableConfig.setTableOrder(1);
            groupTableConfig.setGameType(0);// 空桌子默认不传玩法
            groupTableConfig.setGameCount(0);
            groupTableConfig.setPayType(0);
            groupTableConfig.setPlayerCount(0);
            groupTableConfig.setConfigState("1");
            list.add(groupTableConfig);
        }
        List<Long> result = new ArrayList<>();
        if (list != null && list.size() > 0) {
            try {
                SqlMapClient sqlMapClient = getSql();
                if(list.size() == 1) {
                    long keyId = (Long) sqlMapClient.insert("group.insert_group_table_config", list.get(0));
                    result.add(keyId);
                } else {
                    sqlMapClient.startBatch();
                    for (int i = 0, n = list.size(); i < n; i++) {
                        long keyId = (Long) sqlMapClient.insert("group.insert_group_table_config", list.get(i));
                        result.add(keyId);
                    }
                    sqlMapClient.executeBatch();
                }
            } catch (Exception e) {
                LOGGER.error("批量创建空玩法俱乐部桌子异常", e);
            }
        }
        return result;
    }

    public int clearGroupCreditLog(){
        try {
            return this.getSql().delete("group.clear_group_credit_log", null);
        } catch (Exception e) {
            LOGGER.error("clearGroupCreditLog|error|", e);
        }
        return -1;
    }
}
