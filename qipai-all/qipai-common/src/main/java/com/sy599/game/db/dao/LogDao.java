package com.sy599.game.db.dao;

import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogDao extends BaseDao {
	private static LogDao _inst = new LogDao();

	public static LogDao getInstance() {
		return _inst;
	}

	public int updateConsumeCards(int date, int cards, int freeCards, Map<Integer, Integer> playTypeMap) {

		StringBuilder builder1=new StringBuilder();
		StringBuilder builder2=new StringBuilder();
		StringBuilder builder3=new StringBuilder();

		builder1.append("insert into roomcard_consume_statistics(consumeDate,commonCards,freeCards");
		builder2.append("\"").append(date).append("\"").append(",")
				.append(cards).append(",").append(freeCards);
		builder3.append("consumeDate=").append("\"").append(date).append("\"").append(",")
				.append("commonCards=commonCards+").append(cards).append(",")
				.append("freeCards=freeCards+").append(freeCards);

//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("date", date);
//		map.put("commonCards", cards);
//		map.put("freeCards", freeCards);
		for (Map.Entry<Integer,Integer> kv: playTypeMap.entrySet()) {
//			map.put("playType" + key, playTypeMap.get(key));
			int key = kv.getKey();
			int val = kv.getValue();
			if (key>0&&val!=0){
				builder1.append(",").append("playType").append(key);
				builder2.append(",").append(val);
				builder3.append(",").append("playType").append(key).append("=").append("playType").append(key).append("+").append(val);
			}
		}
		builder1.append(") values (").append(builder2).append(") on duplicate key update ").append(builder3);

		String sql=builder1.toString();

		int update = 0;
		try {
			update = getSqlLoginClient().update("log.updateConsumeCards", sql);
		} catch (Exception e) {
//			LogUtil.e("log.updateConsumeCards err:"+JacksonUtil.writeValueAsString(map), e);
			LogUtil.e("log.updateConsumeCards err:"+e.getMessage(), e);
		}finally {
			LogUtil.msgLog.info("roomcard_consume_statistics:result="+update+",sql="+sql);
		}
		return update;
	}

	public int insertCardsConsumeCards(Long userId,int regBindId,Long consumeCard,int consumeNum,Date regTime) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("regBindId", regBindId);
		map.put("consumeCard", consumeCard);
		map.put("consumeNum", consumeNum);
		map.put("regTime", regTime);
		int update = 0;
		try {
			update = getSqlLoginClient().update("log.insertCardsConsume", map);
		} catch (Exception e) {
			LogUtil.e("log.insertCardsConsume err", e);
		}
		return update;
	}

	public int insertGoldConsumeCards(Long userId,int regBindId,Long consumeGold,Date regTime,int playType) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("regBindId", regBindId);
		map.put("consumeGold", consumeGold);
		map.put("regTime", regTime);
		map.put("playType", playType);
		int update = 0;
		try {
			update = getSqlLoginClient().update("log.insertGoldConsume", map);
		} catch (Exception e) {
			LogUtil.e("log.insertGoldConsume err", e);
		}
		return update;
	}

	public void insetDrawLog(long userId, Date time, int itemId, String itemName, int itemNum) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("time", time);
		map.put("itemId", itemId);
		map.put("itemName", itemName);
		map.put("itemNum", itemNum);
		try {
			getSqlLoginClient().insert("log.insetDrawLog", map);
		} catch (SQLException e) {
			LogUtil.e("log.insetDrawLog err", e);
		}
	}

    public int insertLogActivityReward(Long userId, int activityType, int rewardType, int rewardValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("activityType", activityType);
        map.put("rewardType", rewardType);
        map.put("rewardValue", rewardValue);
        map.put("createdTime", new Date());
        int update = 0;
        try {
            update = getSqlLoginClient().update("log.insertLogActivityReward", map);
        } catch (Exception e) {
            LogUtil.e("log.insertGoldRoomActivityReward err", e);
        }
        return update;
    }

}
