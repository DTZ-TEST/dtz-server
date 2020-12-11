package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.UserCardRecordInfo;
import com.sy.sanguo.game.bean.UserExtend;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.db.dao.UserMessageDao;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class UserDao extends BaseDao {
	private static UserDao _inst = new UserDao();

	public static UserDao getInstance() {
		return _inst;
	}

	/**
	 * 查找用户
	 * 
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUser(String username, String pf) throws SQLException {
		RegInfo user;
		try {
//            user = getFromCache(username, pf);
			// if (user != null) {
			// if (user.getIsOnLine() == 0) {
			// return user;
			// }
			// }

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("username", username);

			if ("true".equals(PropertiesCacheUtil.getValue("weixin_openid",Constants.GAME_FILE))){
				param.put("pf", pf);
				user = (RegInfo) this.getSql().queryForObject("user.getUser0", param);
			}else{
				param.put("pf", pf.startsWith("weixin")?"weixin%":pf);
				user = (RegInfo) this.getSql().queryForObject("user.getUser", param);
			}
//            if (user != null)
//                setToCache(user);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 保存用户扩展信息
	 *
	 * @param userExtend
	 * @throws SQLException
	 */
	public int saveUserExtend(UserExtend userExtend) throws SQLException {
		try {
			return getSql().update("user.save_or_update_user_extend",userExtend);
		} catch (SQLException e) {
			throw e;
		}
	}

	public int updateUserExtend(HashMap<String ,Object> map) throws SQLException {
		try {
			return getSql().update("user.update_user_extend",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查询用户扩展信息
	 *
	 * @param userId
	 * @throws SQLException
	 */
	public List<HashMap<String,Object>> queryUserExtend(String userId) throws SQLException {
		try {
			HashMap<String,String> map=new HashMap<>();
			map.put("userId",userId);
			return getSql().queryForList("user.select_user_extend_all",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查询用户扩展信息
	 *
	 * @param userId
	 * @param msgType
	 * @throws SQLException
	 */
	public HashMap<String,Object> queryUserExtend(String userId,int msgType) throws SQLException {
		try {
			HashMap<String,Object> map=new HashMap<>();
			map.put("userId",userId);
			map.put("msgType",msgType);
			return (HashMap<String,Object>)getSql().queryForObject("user.select_user_extend_single",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找用户
	 * 
	 * @param unionid
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUserByUnionid(String unionid) {
		RegInfo user = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("identity", unionid);
			user = (RegInfo) this.getSql().queryForObject("user.getUserByUnionid", param);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("getUserByUnionid err:" + unionid, e);
		}
		return user;
	}

	public List<Map<String,Object>> load(String sqlMark,HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList(sqlMark,params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找今日邀请、今日达标、总邀请、总达标，已发送红包金额、今日支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyInviteeData(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyInviteeData",params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找总邀请、总达标，已发送红包金额、今日支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyTotalData(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyTotalData",params);
		} catch (SQLException e) {
			throw e;
		}
	}


	/**
	 * 添加红包记录
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public Object addHbExchangeRecord(HashMap<String,String> params) throws SQLException {
		return this.getSql().insert("user.addHbExchangeRecord",params);
	}

	/**
	 * 查找今日邀请(最近N人)
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyTotayUsers(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyTotayUsers",params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 获取支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int loadMyTotayPayCount(HashMap<String,String> params) throws SQLException {
		try {
			return (Integer) this.getSql().queryForObject("user.loadMyTotayPayCount",params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找用户
	 * 
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUser(long userId) throws SQLException {
		RegInfo user = null;
		try {
			user = (RegInfo) this.getSql().queryForObject("user.getUserById", userId);
			if (user != null)
				setToCache(user);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/****************** Cache *******************/

	private void setToCache(RegInfo userInfo) {
	}
	/**
	 * 是否首冲
	 * @param userId
	 * @param minItem
	 * @param maxItem
	 * @return
	 * @throws SQLException
	 */
	public boolean isFirstPay(long userId,int minItem,int maxItem) throws SQLException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userId", String.valueOf(userId));
		param.put("minItem", minItem);
		param.put("maxItem", maxItem);

		ActivityBean activityBean = StaticDataManager.getSingleActivityBaseInfo(111);

		boolean ret;
		if (activityBean != null){
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			param.put("startTime",sdf.format(activityBean.getStartDateTime()));
			param.put("endTime",sdf.format(activityBean.getEndDateTime()));
			Date date = new Date();
			if (date.after(activityBean.getStartDateTime())&&date.before(activityBean.getEndDateTime())){
				HashMap<String,Object> result=(HashMap<String,Object>)this.getSql().queryForObject("order.isFirstPay", param);
				ret = result==null||result.size()==0;
			}else{
				ret = false;
			}
		}else{
			HashMap<String,Object> result=(HashMap<String,Object>)this.getSql().queryForObject("order.isFirstPay", param);
			ret=result==null||result.size()==0;
		}

		LogUtil.i("check isFirstPay:result="+ret+",params="+param);

		return ret;
	}

	/**
	 * 提交用户更新
	 *
	 * @param modify
	 * @return
	 * @throws SQLException
	 */
	public int addUserCards(RegInfo user, long cards, long freeCards, long payBindId, Map<String, Object> modify, UserMessage info, CardSourceType sourceType) throws SQLException {
		if (user==null){
			return  0;
		}
		if (modify == null) {
			modify = new HashMap<String, Object>();
		}
		modify.put("userId", user.getUserId());
		modify.put("cards", cards);
		modify.put("freeCards", freeCards);
		if (payBindId != 0) {
			modify.put("payBindId", payBindId);
			user.setPayBindId((int) payBindId);
		}
		int update = this.getSql().update("user.addUserCards", modify);
		setToCache(user);

//		String showContent = "";

		// 推送
//		Date now = TimeUtil.now();

//		if (0 == payBindId) {
//			showContent = TimeUtil.formatTime(now) + " 您获得了:房卡x" + (freeCards + cards);
//		} else {
//			showContent = TimeUtil.formatTime(now) + " 您充值获得了:房卡x" + (freeCards + cards);
//		}

//		UserMessage info = new UserMessage();
//		info.setTime(now);
//		info.setType(1);
//		info.setUserId(user.getUserId());
//		info.setContent(showContent);
		UserMessageDao.getInstance().saveUserMessage(info);
		GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   enterServer:" + user.getEnterServer());
		if (user.getEnterServer() != 0) {
			String str = GameUtil.sendPay(user.getEnterServer(), user.getUserId(), (int) (cards), (int) freeCards, info, "1");
			GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   " + "sendPay cards"+cards+" freeCards="+freeCards +" currency=1" +",result="+str);
		}

		if(cards + freeCards != 0) {
			long curCard = user.getCards() + cards;
			curCard = (curCard < 0) ? 0 : curCard;
			long curFreeCard = user.getFreeCards() + freeCards;
			curFreeCard = (curFreeCard < 0) ? 0 : curFreeCard;
			UserCardRecordDao.getInstance().insert(new UserCardRecordInfo(user.getUserId(), curCard, curFreeCard, (int)cards, (int)freeCards, 0, sourceType));
		}
		return update;
	}

	/**
	 * 提交用户金币更新
	 */
	public int addUserGold(RegInfo user, long gold, long freeGold, long payBindId, Map<String, Object> modify, UserMessage info) throws SQLException {
		if (modify == null) {
			modify = new HashMap<>();
		}
		modify.put("userId", user.getUserId());
		modify.put("gold", gold);
		modify.put("freeGold", freeGold);
		if (payBindId != 0) {
			modify.put("payBindId", payBindId);
			user.setPayBindId((int) payBindId);
		}
		int update = this.getSql().update("gold.addUserGold", modify);
		setToCache(user);

		UserMessageDao.getInstance().saveUserMessage(info);
		GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   enterServer:" + user.getEnterServer());
		if (user.getEnterServer() != 0) {
			String str = GameUtil.sendPay(user.getEnterServer(), user.getUserId(), (int) (gold), (int) freeGold, info, "2");
			GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   " + "sendPay gold"+gold+" freeGold="+freeGold+" currency=2" +",result="+str);
		}
		return update;
	}


	public void insertRBInfo(Map map) {
		try {
			this.getSql().insert("user.insertRemoveBind", map);
		} catch (SQLException e) {
			LogUtil.e("insertRemoveBindInfo err-->"+e);
		}
	}

	public Integer getRemoveBindCount(Long userId) {
		try {
			Object o = getSql().queryForObject("user.selectRemoveBindCount", userId);
			if (o != null) {
				return (Integer) o;
			} else {
				return 0;
			}
		} catch (SQLException e) {
			LogUtil.e("getRemoveBindCount err-->"+e);
		}
		return 0;
	}

	public long getIdentityUserId(String identity) {
		try {
			Object o = getSql().queryForObject("user.selectIdentityUserId", identity);
			if (o != null) {
				return (long) o;
			} else {
				return 0;
			}
		} catch (SQLException e) {
			LogUtil.e("getIdentityUserId err-->"+e);
		}
		return 0;
	}
}
