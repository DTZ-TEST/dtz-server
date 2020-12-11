package com.sy599.game.db.dao;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.activityRecord.ActivityReward;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityDao extends BaseDao {

	private static ActivityDao _inst = new ActivityDao();

	public static ActivityDao getInstance() {
		return _inst;
	}
	
	/**
	 * 修改福袋数据
	 * 
	 * @param userId
	 * @param cards
	 * @return
	 */
	public int updateFudai(Player player, int cards) {
		
		// userId, username, sex, inviteeCount, invitorId, feedbackCount, openCount, activityStartTime, prizeFlag
		
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", player.getUserId());
		paramMap.put("feedbackCount", cards);
		paramMap.put("username", player.getName());
		paramMap.put("sex", player.getSex());
		paramMap.put("inviteeCount", 0);
		paramMap.put("invitorId", 0);
		paramMap.put("openCount", 0);
		paramMap.put("activityStartTime", new Date());
		paramMap.put("prizeFlag", 0);

		try {
			return getSqlLoginClient().update("activity.updateFudai", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.updateFudai err:", e);
		}
		
		return 0;
	}
	
	/**
	 * 增加新年红包记录
	 * 
	 * @param paramMap
	 */
	public void addHbFafangRecord(Map<String, Object> paramMap) {
		try {
			getSqlLoginClient().insert("activity.addHbFafangRecord", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.addHbFafangRecord err:", e);
		}
	}

	/**
	 * 增加玩家红包总额
	 * 
	 * @param userId
	 * @param money
	 */
	public int insertUserTotalMoney(Player player, double money) {
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", player.getUserId());
		paramMap.put("cdk", "");
		paramMap.put("extend", "");
		paramMap.put("totalMoney", money);
		paramMap.put("myConsume", "");
		paramMap.put("shengMoney", 0);
		paramMap.put("prizeFlag", 0);
		paramMap.put("name", player.getName());

		try {
			return getSqlLoginClient().update("activity.insertUserTotalMoney", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("activity.insertUserTotalMoney err:", e);
		}
		
		return 0;
	}
	
	/**
	 * 增加玩家领取活动奖励
	 * @param activityReward
	 */
	public void addActivityReward(ActivityReward activityReward){
		try{
			this.getSqlLoginClient().insert("activity.addActivityReward", activityReward);
		}catch(SQLException e) {
			LogUtil.dbLog.error("activity.addActivityReward err:", e);
		}
	}
	
}
