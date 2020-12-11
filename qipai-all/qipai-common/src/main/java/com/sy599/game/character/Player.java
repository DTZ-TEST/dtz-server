package com.sy599.game.character;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.activity.goldroom.GoldRoomActivity;
import com.sy599.game.base.BaseTable;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.common.service.FuncConsumeStatics;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.extend.MyExtend;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.SystemCommonInfoManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MyMessage;
import com.sy599.game.msg.MarqueeMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.GoldRoomActivityProto;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.staticdata.bean.GradeExpConfig;
import com.sy599.game.staticdata.bean.GradeExpConfigInfo;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.constant.WebSocketMsgType.TipsEnum;
import com.sy599.game.websocket.netty.WebSocketServerHandler;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Player {
	private static final int JSON_TAG = 1;
	private AtomicInteger msgCheckCode = new AtomicInteger(1);
	protected volatile long userId;
	protected String flatId;
	protected String name;
	protected String rawName;
	protected String ip;
	protected String deviceCode;
	private int maxPoint;
	private int totalPoint;
	private int totalBoom;
	private String headimgurl;
	private int enterServer;
	private String pf;
	protected int sex;
	private String identity;
	private volatile long freeCards;
	private volatile long cards;
	private volatile long usedCards;
	private long synUsedCards;
	private Date syncTime;
	private Date reginTime;
	private volatile Date loginTime;
	private Date logoutTime;
	private Date lastPlayTime;
	private Date payBindTime;
	private String channel;
	private int loginDays;
	private String pay;
	private String config;
	private volatile String sessionId;
	private ReentrantLock lock;
	private MyWebSocket myWebSocket;
	private MessageUnit messageUnit;
	private long actionTime;
	private int isLeave;
	private int drawLottery;
	private volatile int isOnline;
	private volatile List<List<Long>> record;
	protected volatile long playingTableId;
	private FuncConsumeStatics funcConsume;
	private MyActivity myActivity;
	private MyMessage myMessage;
	protected MyExtend myExtend;
	private GeneratedMessage recMsg;
	private boolean isLoad;
	private int actionCount;
	private long loginActionTime;
	private int regBindId;
	private String os;
	private String vc;
	private long payBindId;
	/** 玩家当月已签到的集合 **/
	private List<Integer> signs;
	private Integer userState;//玩家状态：0禁止登陆，1正常，2红名
	protected String loginExtend;
	protected int totalCount;
	protected int totalBureau;// 非金币场房间局数统计
	protected volatile BaseTable table;

	protected GroupUser groupUser;

	protected GoldPlayer goldPlayer;

	/*** 中途退出房间标识*/
	private int quitTable = 0;

	private volatile String matchId;//比赛场标识

	/*** 是否牌友群成员标识*/
	private Integer isGroup;// 0非牌友群成员，1牌友群成员

	private String version;

	private volatile int userTili = 0;//体力

	private List<String> ymdSigns = new ArrayList<>();

	/**
	 * 保存在内存中，不需要持久化的有限数据
	 */
	protected Map<String,Long> propertiesCache = new ConcurrentHashMap<>();

	private Map<String, Object> dbParamMap = new ConcurrentHashMap<>();

	protected volatile long lastSaveDbTime = 0;//最后保存db的时间

	private String errerMsg;//错误消息

	/**
	 * 赢或输的信用分：正数为赢分，负数为负分
	 */
	private int winLoseCredit = 0;

	/**
	 * 信用分佣金
	 */
	private int commissionCredit = 0;

	/**
	 * 最近手动操作的小局
	 */
	protected int lastActionBureau;

    /**
     * 托管累计计时:时间毫秒
     */
	private int autoPlayCheckedTime;
    /**
     * 托管累计计时辅助值
     */
    private boolean autoPlayCheckedTimeAdded;
    /**
     * 托管累计计时辅助值
     */
    private boolean autoPlayChecked;

    private long joinTime;

    private int dissCount;


    public Player() {
		record = new ArrayList<>();
		myActivity = new MyActivity(this);
		myMessage = new MyMessage(this);
		myExtend = new MyExtend(this);
		joinTime = System.currentTimeMillis();
	}

	public String getErrerMsg() {
		return errerMsg;
	}

	public void setErrerMsg(String errerMsg) {
		this.errerMsg = errerMsg;
	}

	public String getMatchId() {
		return matchId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setMsgCheckCode(AtomicInteger msgCheckCode) {
		this.msgCheckCode = msgCheckCode;
	}

	public void setLastSaveDbTime(long lastSaveDbTime) {
		this.lastSaveDbTime = lastSaveDbTime;
	}

	public long getLastSaveDbTime() {
		return lastSaveDbTime;
	}

	/**
	 * 获取totalPoint
	 * @see #getTotalPoint()
	 * @return
	 */
	public int loadScore(){
		return getTotalPoint();
	}

	public Date getPayBindTime() {
		return payBindTime;
	}

	public void setPayBindTime(Date payBindTime) {
		this.payBindTime = payBindTime;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Date getLastPlayTime() {
		return lastPlayTime;
	}

	public void setLastPlayTime(Date lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
		this.dbParamMap.put("lastPlayTime", this.lastPlayTime);
	}

	/**
	 * 变更用户体力
	 * @param tili
	 * @return
	 */
	public int changeTili(int tili){
		return changeTili(tili,false);
	}

	public int changeTili(int tili,boolean db){
		synchronized (this){
			userTili += tili;
		}

		if (db){
			UserDao.getInstance().updateUserExtendIntValue(String.valueOf(userId),UserResourceType.TILI,tili);
			writeUserResourceMessage(UserResourceType.TILI,tili,userTili);
		}
		return userTili;
	}

	public int getUserTili() {
		return userTili;
	}

	public void setUserTili(int userTili) {
		this.userTili = userTili;
	}

	public List<String> getYmdSigns() {
		return ymdSigns;
	}

	public String getRawName() {
		return rawName;
	}

	public void setRawName(String rawName) {
		this.rawName = rawName;
	}

	public int getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}

	public int getQuitTable() {
		return quitTable;
	}

	public void setQuitTable(int quitTable) {
		this.quitTable = quitTable;
	}

	public GoldPlayer getGoldPlayer() {
		return isRobot()?loadGoldRobot():goldPlayer;
	}

	public void setGoldPlayer(GoldPlayer goldPlayer) {
		this.goldPlayer = goldPlayer;
	}

	public GroupUser getGroupUser() {
		return groupUser;
	}

	public void setGroupUser(GroupUser groupUser) {
		this.groupUser = groupUser;
	}

	public Map<String, Long> getPropertiesCache() {
		return propertiesCache;
	}

	public void setPropertiesCache(Map<String, Long> propertiesCache) {
		this.propertiesCache = propertiesCache;
	}

	public Integer getUserState() {
		return userState;
	}

	public void setUserState(Integer userState) {
		this.userState = userState;
	}

	public void changeUserState(Integer userState) {
		setUserState(userState);

		if (isForbidLogin()){
			forbid();
		}
	}

	/**
	 * 是否被禁止登陆
	 * @return
	 */
	public boolean isForbidLogin(){
		return userState!=null&&userState.intValue()==0;
	}

	/**
	 * 是否红名
	 * @return
	 */
	public boolean isRedPlayer(){
		return userState!=null&&userState.intValue()==2;
	}

	/**
	 * 是否是正常玩家
	 * @return
	 */
	public boolean isOkPlayer(){
		return userState==null||userState.intValue()==1;
	}

	public final void loadFromDB(RegInfo info) {
		this.messageUnit = null;
		this.loginExtend = info.getLoginExtend();
		this.headimgurl = info.getHeadimgurl();
		this.userId = info.getUserId();
		this.flatId = info.getFlatId();
		this.rawName = info.getName();
		this.name = info.getName();
		if (org.apache.commons.lang3.StringUtils.isNotBlank(this.name)){
			this.name = this.name.replaceAll("\\,|\\;|\\_|\\||\\*","");
		}
		this.pf = info.getPf();
		this.sex = info.getSex();
		this.sessionId = info.getSessCode();
		this.cards = info.getCards();
		// this.syncCards = info.getSyncCards();
		this.loginDays = info.getLoginDays();
		this.syncTime = info.getSyncTime();
		this.reginTime = info.getRegTime();
		this.loginTime = info.getLogTime();
		this.logoutTime = info.getLogoutTime();
		this.playingTableId = info.getPlayingTableId();
		this.payBindTime = info.getPayBindTime();
		this.channel = info.getChannel();
		this.enterServer = info.getEnterServer();
		this.config = info.getConfig();
		this.ip = info.getIp();
		this.freeCards = info.getFreeCards();
		this.usedCards = info.getUsedCards();
		this.drawLottery = info.getDrawLottery();
		this.isOnline = info.getIsOnLine();
		this.myActivity.loadFromDB(info.getActivity());
		this.myExtend.initData(info.getExtend());
		this.os = info.getOs();
		this.vc = info.getSyvc();
		this.regBindId = info.getRegBindId();
		this.payBindId = info.getPayBindId();
		this.identity = info.getIdentity();
		if (!StringUtils.isBlank(info.getRecord())) {
			this.record = StringUtil.explodeToLongLists(info.getRecord());
		}
		this.userState=info.getUserState();
		if (this.userState==null){
			this.userState=1;
		}
		//改为从extend计算
//		this.totalCount=info.getTotalCount();

		if (playingTableId>0){
			getPlayingTable();
		}

		// 加载是否牌友群成员标识
		loadGroupUser();

		// 加载金币玩家身份
		loadGoldPlayer(false);

		matchId = loadMatchId();

		loadFromDB0(info);
	}

	protected void loadFromDB0(RegInfo info){

	}

	/**
	 * 自动解绑
	 */
	public int autoRemoveBind(Long id){
		int res = UserDao.getInstance().removeBindById(id);
		if (res == 1) {
			UserDao.getInstance().addRBRecordByPlayer(this);
		}
		return res;
	}

	/**
	 * 加载军团数据
	 */
	public GroupUser loadGroupUser(String groupId){
		isGroup = 0;
		GroupUser groupUser = null;
		if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("isGroupUser_load"))||(groupId!=null&&groupId.length()>=4)) {
			groupUser = GroupDao.getInstance().loadGroupUser(this.getUserId(),groupId);
			if (groupUser!=null) {
				setIsGroup(1);
			} else {
				setIsGroup(0);
			}
		} else {
			setIsGroup(0);
		}
		setGroupUser(groupUser);
		return groupUser;
	}
	/**
	 * 加载军团数据
	 */
	public void loadGroupUser(){
		loadGroupUser(null);
	}

	public void changeTotalCount(){
		this.totalCount++;
		dbParamMap.put("totalCount", JSON_TAG);
	}

	public void changeTotalBureau() {
		this.totalBureau++;
		dbParamMap.put("totalBureau", JSON_TAG);
	}

	public boolean refreshPlayer() {
		RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
		if (info != null) {
			loadFromDB(info);
			return true;
		}
		return false;
	}

	public abstract void initPlayInfo(String data);

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getTotalBureau() {
		return totalBureau;
	}

	public void setTotalBureau(int totalBureau) {
		this.totalBureau = totalBureau;
	}

	public long getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(long payBindId) {
		this.payBindId = payBindId;
	}

	public List<Integer> getSigns() {
		return signs;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public MessageUnit getMessageUnit() {
		return messageUnit;
	}

	public int getMsgCheckCode() {
		return msgCheckCode.get();
	}

	public AtomicInteger getMsgCheckCode0() {
		return msgCheckCode;
	}

	public void initMsgCheckCode() {
		this.msgCheckCode.set(1+new SecureRandom().nextInt(100000));
	}

	public int incrementAndGetMsgCheckCode() {
		return msgCheckCode.addAndGet(1);
	}

	public void setMessageUnit(MessageUnit msgUnit) {
		this.messageUnit = msgUnit;
	}

	public MyWebSocket getMyWebSocket() {
		return myWebSocket;
	}

	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return this.lock.tryLock(timeout, unit);
	}

	public boolean tryLock() {
		return this.lock.tryLock();
	}

	public void unLock() {
		this.lock.unlock();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		this.dbParamMap.put("sessionId", this.sessionId);
	}

	public int getWinCount(){
		return 0;
	}
	public int getLostCount(){
		return 0;
	}

	public long getActionTime() {
		return actionTime;
	}

	public void setActionTime(long actionTime) {
		this.actionTime = actionTime;
	}

	public String getFlatId() {
		return flatId;
	}

	public void setFlatId(String flatId) {
		this.flatId = flatId;
		this.dbParamMap.put("flatId", this.flatId);
	}

	public void setMyWebSocket(MyWebSocket myWebSocket) {
		this.myWebSocket = myWebSocket;
	}

	public void changeConsumeNum() {

	}

	public FuncConsumeStatics getFuncConsume() {
		return funcConsume;
	}

	public void changeRefreshTime() {

	}

	public void changeExtend() {
		dbParamMap.put("extend", JSON_TAG);
	}

	public String getIp() {
		return ip;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setIp(String ip) {
		setIp(ip,true);
	}

	public void setIp(String ip,boolean save) {
		this.ip = ip;
		if (save){
			this.dbParamMap.put("ip", this.ip);
		}
	}

	public void exit(String channelId) {
        if (myWebSocket != null && !channelId.equals(myWebSocket.getCtx().channel().id().asShortText())) {
            return;
        }
		setLogoutTime(TimeUtil.now());
		setIsOnline(0,false);

		if (WebSocketServerHandler.isOpen) {
			saveBaseInfo();
			BaseTable table = getPlayingTable();
			if (table != null) {
				table.broadIsOnlineMsg(this, SharedConstants.table_offline);
			}
			sendActionLog(LogConstants.reason_logout, "");
		}

		PlayerManager.getInstance().removePlayer(this);
		if (myWebSocket != null) {
			myWebSocket.setPlayer(null);
			setMyWebSocket(null);
		}

        dbParamMap.remove("isOnline");
        UserDao.getInstance().saveOffLine(GameServerConfig.SERVER_ID, userId);
		LogUtil.i(userId + " exit");
	}

	public void setTable(BaseTable table) {
		this.table = table;
	}

	public Map<String,Object> loadCurrentDbMap(){
		// copy 一份map
		Map<String, Object> tempMap = new HashMap<>();
		synchronized (this) {
			Iterator<Map.Entry<String, Object>> it = dbParamMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> kv = it.next();
				tempMap.put(kv.getKey(), kv.getValue());
				it.remove();
			}
		}
		return tempMap;
	}

	private final void forbid(){
		try{
			setIsOnline(0);
			LogUtil.e(userId + " 您已被禁止登陆");
			writeComMessage(WebSocketMsgType.res_code_err,"您已被禁止登陆");
			if (myWebSocket!=null){
				PlayerManager.getInstance().removePlayer(this);
				myWebSocket.close();
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public final void saveBaseInfo(){
		saveBaseInfo(false);
	}

	public final void saveBaseInfo(boolean asyn) {
		if (userId < 0) {
			return;
		}

		if (isForbidLogin()){
			forbid();
		}

		if (asyn){
			TaskExecutor.coreExecutor.execute(new Runnable() {
				public void run() {
					// copy 一份map
					Map<String, Object> tempMap = loadCurrentDbMap();
					synUsedCards = 0;
					int count = tempMap.size();
					if (count > 0) {
						Object syncTime;
						if (count==1&&(syncTime=tempMap.get("syncTime"))!=null&&(System.currentTimeMillis()-lastSaveDbTime)<10*60*1000){
							dbParamMap.put("syncTime",syncTime);
						}else{
							buildBaseSaveMap(tempMap);
							UserDao.getInstance().save(flatId, pf, tempMap);
						}
					}
				}
			});
		}else{
			// copy 一份map
			Map<String, Object> tempMap = loadCurrentDbMap();

			synUsedCards = 0;
			if (!tempMap.isEmpty()) {
				buildBaseSaveMap(tempMap);
				UserDao.getInstance().save(flatId, pf, tempMap);
			}
		}
	}

	public final Map<String, Object> saveDB(boolean asyn) {
		actionCount = 0;
		if (userId < 0) {
			return null;
		}

		if (isForbidLogin()){
			forbid();
		}

		// copy 一份map
		Map<String, Object> tempMap = loadCurrentDbMap();
		synUsedCards = 0;

		int count = tempMap.size();
		if (count > 0) {
			Object syncTime;
			if (asyn && count == 1 && (syncTime = tempMap.get("syncTime")) != null&&(System.currentTimeMillis()-lastSaveDbTime)<10*60*1000){
				dbParamMap.put("syncTime",syncTime);
				return null;
			}else{
				buildBaseSaveMap(tempMap);
			}
			return tempMap;
		}else{
			return null;
		}
	}

	private void buildBaseSaveMap(Map<String, Object> tempMap) {
		tempMap.put("userId", userId);
		if (tempMap.containsKey("refreshTime")) {
			tempMap.put("refreshTime", "");
		}
		if (tempMap.containsKey("activity")&&myActivity!=null) {
			tempMap.put("activity", myActivity.toJson());
		}
		if (tempMap.containsKey("record")) {
			tempMap.put("record", StringUtil.implodeLongLists(record));
		}
		if (tempMap.containsKey("extend")&&myExtend!=null) {
			tempMap.put("extend", myExtend.toJson());
		}
		if (tempMap.containsKey("totalCount")) {
			tempMap.put("totalCount", this.totalCount);
		}
		if (tempMap.containsKey("totalBureau")) {
			tempMap.put("totalBureau", this.totalBureau);
		}
		if (tempMap.containsKey("enterServer")) {
			tempMap.put("enterServer", this.enterServer);
		}
		if (tempMap.containsKey("playingTableId")) {
			tempMap.put("playingTableId", this.playingTableId);
		}
		// if (PlayerManager.syncTime == null) {
		// PlayerManager.syncTime = TimeUtil.now();
		// }
		// tempMap.put("syncTime", PlayerManager.syncTime);

		lastSaveDbTime = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.dbParamMap.put("name", this.name);
	}

	public void writeSocket(MessageUnit message) {
		if (myWebSocket != null) {
			myWebSocket.send(message);
		}
	}

	/**
	 * 推送消息给前台
	 *
	 * @param message
	 */
	public void writeSocket(GeneratedMessage message) {
		if (myWebSocket != null) {
			myWebSocket.send(message);
		}/* else {
			LogUtil.e("myWebSocket is null-->send message error");
		}*/
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
		this.dbParamMap.put("pf", this.pf);
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
		this.dbParamMap.put("sex", this.sex);
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
		this.dbParamMap.put("identity", this.identity);
	}

	public long getCards() {
		return cards;
	}

	public void setCards(long cards) {
		this.cards += cards;
		this.dbParamMap.put("cards", cards);
	}

	/**
	 * 如果距离上一次请求有25秒并且socket已经断开 判断离线
	 *
	 * @return
	 */
	public boolean isOnline() {
		long lastIntervalTime = 0;
		Date syncTime = getSyncTime();
		if (syncTime != null) {
			lastIntervalTime = TimeUtil.currentTimeMillis() - syncTime.getTime();
		}

		if (lastIntervalTime == 0) {
			return myWebSocket != null;
		}

		if (lastIntervalTime != 0 && lastIntervalTime > SharedConstants.SENCOND_IN_MINILLS * 25) {
			// 距离上一次请求有25秒 并且socket已经断开
			if (myWebSocket == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 前台最后一次请求的时间
	 *
	 * @return
	 */
	public Date getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
		this.dbParamMap.put("syncTime", this.syncTime);
	}

	public Date getReginTime() {
		return reginTime;
	}

	public void setReginTime(Date reginTime) {
		this.reginTime = reginTime;
		this.dbParamMap.put("reginTime", this.reginTime);
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
		this.dbParamMap.put("logoutTime", this.logoutTime);
	}

	public String getPay() {
		return pay;
	}

	public void setPay(String pay) {
		this.pay = pay;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
		this.dbParamMap.put("config", this.config);
	}

	public long getPlayingTableId() {
		return playingTableId;
	}

	public void setPlayingTableId(long playingTableId) {
		this.playingTableId = playingTableId;

		if (playingTableId<=0){
			table = null;
		}

		dbParamMap.put("playingTableId", playingTableId);
	}

	public boolean isHasCards(int needCard) {
		return this.freeCards + this.cards >= needCard;

	}

	public <T> T getPlayer(AbstractBaseCommandProcessor processor) {
		return getClass() == PlayerManager.getInstance().getPlayer(processor) ? ((T) this) : null;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public void setTotalPoint(int totalPoint) {
		this.totalPoint = totalPoint;
	}

	public void changeTotalPoint(int totalPoint) {
		this.totalPoint += totalPoint;
	}

	public int getMaxPoint() {
		return maxPoint;
	}

	public void setMaxPoint(int maxPoint) {
		this.maxPoint = maxPoint;
	}

	public int getTotalBoom() {
		return totalBoom;
	}

	public void setTotalBoom(int totalBoom) {
		this.totalBoom = totalBoom;
	}

	public void changeTotalBoom(int totalBoom) {
		this.totalBoom += totalBoom;
	}

	public int getIsLeave() {
		if (isLeave == 0 && !isOnline()) {
			return 1;
		}
		return isLeave;
	}

	public void changeIsLeave(int isLeave) {
		this.isLeave = isLeave;
	}

	/**
	 * 发送通用消息
	 *
	 * @param code
	 * @param notCheckCode
	 *            不需要检查checkCode
	 * @param params
	 */
	public void writeOutSyncComMessage(int code, boolean notCheckCode, Object... params) {
		MessageUnit messageUnit = new MessageUnit();
		messageUnit.setNotCheckCode(notCheckCode);
		ComRes.Builder res = SendMsgUtil.buildComRes(code, params);
		messageUnit.setMessage(res.build());
		writeSocket(messageUnit);

	}

	/**
	 * 发送通用消息
	 *
	 * @param code
	 * @param params
	 */
	public void writeComMessage(int code, Object... params) {
		ComRes.Builder res = SendMsgUtil.buildComRes(code, params);
		writeSocket(res.build());
	}

	/**
	 * 推送前台房卡更新
	 *
	 * @param cards
	 */
	public void writeCardsMessage(int cards) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_cards, cards,String.valueOf(loadAllCards()),String.valueOf(loadAllGolds()));
		writeSocket(res.build());
//		writeOutSyncComMessage(WebSocketMsgType.res_code_cards,true,cards,String.valueOf(loadAllCards()),String.valueOf(loadAllGolds()));
	}

	/**
	 * 推送前台金币更新
	 */
	public void writeGoldMessage(long gold) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_gold, String.valueOf(gold), getGoldPlayer().getShowGold());
		writeSocket(res.build());
//		writeOutSyncComMessage(WebSocketMsgType.res_com_gold,true,String.valueOf(gold), getGoldPlayer().getShowGold());
	}

	/**
	 * 推送前台段位更新
	 */
	public void writeGradeMessage(int grade, String gradeDesc) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_sendGrade, String.valueOf(grade), gradeDesc);
		writeSocket(res.build());
	}

	/**
	 * 推送前台用户资源更新
	 */
	public void writeUserResourceMessage(UserResourceType type,long count,long total) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_resource,type.getType(), type.name(), String.valueOf(count), String.valueOf(total));
		writeSocket(res.build());
	}

	public <T extends BaseTable> T getPlayingTable() {
		if (table==null||table.getId()!=playingTableId){
			table=TableManager.getInstance().getTable(playingTableId);

			if (table==null&&playingTableId>0&&GoldRoomUtil.isNotGoldRoom(playingTableId)&&!GameUtil.isPlayBaiRenWanfa((int)playingTableId)){
				LogUtil.errorLog.error("player table not found:userId="+userId+",tableId="+playingTableId);
//				setPlayingTableId(0);
//				saveBaseInfo();
			}
		}
		return (T) table;
	}

	public <T extends BaseTable> T getPlayingTable(Class<T> clazz) {
		return (T) getPlayingTable();
	}

	public void writeErrMsg(String errMsg) {
		if (errMsg!=null&&errMsg.startsWith("code_")){
			writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(errMsg));
			setErrerMsg(LangHelp.getMsg(errMsg));
		}else{
			writeComMessage(WebSocketMsgType.res_code_err, errMsg);
			setErrerMsg(errMsg);
		}
	}

	public void writeErrMsgs(Object... msgs) {
		writeComMessage(WebSocketMsgType.res_code_err, msgs);
	}

	public void writeErrMsg(String langKey, Object... o) {
		writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(langKey, o));
	}

	// public void writeFormMsg(String errMsg) {
	// writeComMessage(WebSocketMsgType.sc_code_shutdown, errMsg);
	// }

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public int getEnterServer() {
		return enterServer;
	}

	public void setEnterServer(int enterServer) {
		this.enterServer = enterServer;
		dbParamMap.put("enterServer", enterServer);
	}

	public long getFreeCards() {
		return freeCards;
	}

	public boolean changeCards(long freeCards, long cards, boolean isWrite, int playType){
		return changeCards(freeCards, cards, isWrite, playType, CardSourceType.unknown);
	}

	/**
	 * @param freeCards
	 *            免费房卡(增加免费房卡)
	 * @param cards
	 *            收费房卡(消耗房卡，会优化消耗免费房卡)
	 * @param isWrite
	 *            是否推送给前台
	 */
	public boolean changeCards(long freeCards, long cards, boolean isWrite, CardSourceType sourceType) {
		return changeCards(freeCards, cards, isWrite, 0, sourceType);
	}

	// 消耗房卡过渡方法
	public boolean changeCards(long freeCards, long cards, boolean isWrite, int playType, CardSourceType sourceType) {
		return changeCards(freeCards, cards, isWrite, playType, true, sourceType);
	}

	/**
	 * @param freeCards
	 *            免费房卡 (增加免费房卡)
	 * @param cards
	 *            收费房卡 (消耗房卡，会优化消耗免费房卡)
	 * @param isWrite
	 *            是否推送给前台
	 * @param playType
	 *            消耗的玩法
	 */
	public boolean changeCards(final long freeCards, final long cards, boolean isWrite, int playType, boolean isRecord, CardSourceType sourceType) {
		long temp1=0;//free
		long temp2=0;//common
		synchronized(this) {
			if (cards < 0) {
				// temp等于绑定房卡 + cards
				long temp = this.freeCards + cards;
				if (temp >= 0) {
					// 房卡足够
					this.freeCards = temp;
					temp2 = 0;
					temp1 = -cards;
				} else {
					// 房卡不足，先用完绑定房卡，再用普通房卡
					this.freeCards = 0;
					temp2 = -temp;
					temp1 = (-cards) - temp2;
				}

				this.cards -= temp2;
			} else {
				this.cards += cards;
			}

			this.freeCards += freeCards;
			if (temp1 > 0 && isRecord) {
				changeUsedCards(-temp1);
			}
			if (temp2 > 0 && isRecord) {
				changeUsedCards(-temp2);
			}

			if (isRobot()) {
				return true;
			}

			Map<String, Object> log = new HashMap<>();
			log.put("freeCards", -temp1);
			log.put("cards", -temp2);
			log.put("playType", playType);
			log.put("isRecord", isRecord ? 1 : 0);

			LogUtil.msgLog.info("statistics:userId=" + userId + ",tableId=" + playingTableId + ",isRecord=" + isRecord + " ,playType=" + playType + ",freeCards=" + freeCards + ",cards=" + cards + ",rest freeCards=" + this.freeCards + ",rest cards=" + this.cards + ",isWrite=" + isWrite + ",temp1=" + temp1 + ",temp2=" + temp2);

			long t1 = temp1, t2 = temp2;

			temp1 += -freeCards;
			if (cards > 0) {
				temp2 += cards;
			}
			if (temp2 != 0 || temp1 != 0) {
				if (UserDao.getInstance().updateUserCards(userId, flatId, pf, -temp2, -temp1) > 0) {
					if (isWrite) {
						writeCardsMessage((int) (-temp1 - temp2));
					}
				} else {
					long[] cs = UserDao.getInstance().loadUserCards(String.valueOf(userId));
					LogUtil.errorLog.warn("updateUserCards fail:userId={},cards={},freeCards={},cards(current)={},freeCards(current)={},cards(db)={},freeCards(db)={}"
							, userId, -temp2, -temp1, this.cards, this.freeCards, cs[0], cs[1]);

					this.cards = cs[0];
					this.freeCards = cs[1];
					return false;
				}
			}

			if (isRecord && (t2 != 0 || t1 != 0)) {
				PlayerManager.getInstance().changeConsume(this, (int) -t2, (int) -t1, playType);
			}

			sendActionLog(LogConstants.reason_consumecards, JacksonUtil.writeValueAsString(log));
			PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int)-temp1, (int)-temp2, playType, sourceType));
		}

		return true;
	}

	/**
	 * 改变房卡数量（消耗房卡不能使用该方法）
	 * @param freeCards
	 * @param commonCards
	 * @param saveDb
	 * @param sendClient
	 */
	public void changeCards(long freeCards,long commonCards,boolean saveDb,boolean sendClient, CardSourceType sourceType){
		synchronized (this) {
			this.freeCards += freeCards;
			this.cards += commonCards;
		}
		if (saveDb){
			UserDao.getInstance().updateUserCards(userId, flatId, pf, commonCards, freeCards);
		}
		if (sendClient){
			writeCardsMessage((int) (freeCards+commonCards));
		}
		PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int)freeCards, (int)commonCards, 0, sourceType));
	}

	public void updatePayCards(int cards, int freeCards) {
		synchronized (this) {
			RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
			if (info != null) {
				this.cards = info.getCards();
				this.freeCards = info.getFreeCards();
			}
		}
		Map<String, Object> log = new HashMap<>();
		log.put("freeCards", freeCards);
		log.put("cards", cards);
		sendActionLog(LogConstants.reason_pay, JacksonUtil.writeValueAsString(log));
	}

	public void updatePayGold(int gold, int freeGold) {
		GoldPlayer goldPlayer = getGoldPlayer();
		if (goldPlayer == null) {
			LogUtil.e("updatePayGold err-->goldPlayer is null,userId:"+userId);
			return;
		}
		GoldPlayer goldPlayer0;
		try {
			synchronized (this) {
				goldPlayer0 = GoldDao.getInstance().selectGoldUserByUserId(userId);
				if (goldPlayer0 != null) {
					goldPlayer.setGold(goldPlayer0.getGold());
					goldPlayer.setFreeGold(goldPlayer0.getFreeGold());
					getMyExtend().updateUserMaxGold();
				}
			}
			Map<String, Object> log = new HashMap<>();
			log.put("freeGold", freeGold);
			log.put("gold", gold);

			sendActionLog(LogConstants.reason_pay, JacksonUtil.writeValueAsString(log));
		} catch (Exception e) {
			LogUtil.e("updatePayGold err-->", e);
		}
	}

	public long getUsedCards() {
		return usedCards;
	}

	public void newRecord(){
		synchronized (this) {
			if (record.size() == 0) {
				List<Long> list0 = new ArrayList<>();
				list0.add(0L);
				record.add(list0);
				dbParamMap.put("record", JSON_TAG);
			} else {
				List<Long> list = record.get(record.size() - 1);
				if (!(list.size() == 0 || (list.size() == 1 && list.get(0) == 0L))) {
					List<Long> list0 = new ArrayList<>();
					list0.add(0L);
					record.add(list0);
					dbParamMap.put("record", JSON_TAG);
				}
			}
		}
	}

	public List<List<Long>> getRecord() {
		return record;
	}

	public void sendActionLog(LogConstants reason, String properties) {
		UdpLogger.getInstance().sendActionLog(this, reason, properties);
	}

	public void addRecord(long logId, int playCount) {
		List<Long> recordList;
		//调整为加入房间时判断
		/**playCount == 1 || **/

		synchronized(this) {
			if (record.isEmpty()) {
				recordList = new ArrayList<>();
				recordList.add(0L);
				record.add(recordList);

			} else {
				recordList = record.get(record.size() - 1);
			}
			recordList.add(logId);
            int saveCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","savePlayLogCount",10);
			while (record.size() > saveCount) {
				record.remove(0);
			}
		}
		dbParamMap.put("record", JSON_TAG);
	}

	public void changeUsedCards(long usedCards) {
		synchronized (this) {
			this.usedCards += usedCards;
			this.synUsedCards += usedCards;
		}
		dbParamMap.put("usedCards", synUsedCards);
	}

	public int getDrawLottery() {
		return drawLottery;
	}

	public void changeDrawLottery(int drawLottery) {
		this.drawLottery += drawLottery;
		dbParamMap.put("drawLottery", this.drawLottery);
	}

	public void changeActivity() {
		dbParamMap.put("activity", JSON_TAG);
	}

	public MyActivity getMyActivity() {
		return myActivity;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public MyMessage getMyMessage() {
		return myMessage;
	}

	public boolean isLoad() {
		return isLoad;
	}

	public void setLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}

	public int getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(int isOnline) {
		setIsOnline(isOnline,true);
	}

	public void setIsOnline(int isOnline,boolean save) {
		this.isOnline = isOnline;
		if (save){
			dbParamMap.put("isOnline", isOnline);
		}
	}

	public GeneratedMessage getRecMsg() {
		return recMsg;
	}

	public void setRecMsg(GeneratedMessage recMsg) {
		this.recMsg = recMsg;
	}

	public boolean isRobot() {
		return userId < 0;
	}

	public abstract void clearTableInfo();

	public void clearTableInfo(BaseTable table,boolean save){
		clearTableInfo();
	}

	public abstract player_state getState();

	public abstract int getIsEntryTable();

	public abstract void setIsEntryTable(int tableOnline);

	public abstract int getSeat();

	public abstract void setSeat(int randomSeat);

	public abstract void changeState(player_state entry);

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	public void changeSeat(int seat) {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}

	/**
	 * 初始化下一局
	 */
	public abstract void initNext();

	/**
	 * 手牌
	 *
	 * @return
	 */
	public abstract List<Integer> getHandPais();

	/**
	 * 打牌saveDBInfo
	 *
	 * @return
	 */
	public abstract String toInfoStr();

	/**
	 * 玩家在房间内的res
	 *
	 * @return
	 */
	public abstract PlayerInTableRes.Builder buildPlayInTableInfo();

	/**
	 * 获取称号
	 * @return
	 */
	public int loadDesignation(){
		int name;
		if (totalCount>=180000){
			name = 21;
		}else if(totalCount>=165000){
			name = 20;
		}else if(totalCount>=150000){
			name = 19;
		}else if(totalCount>=135000){
			name = 18;
		}else if(totalCount>=120000){
			name = 17;
		}else if(totalCount>=105000){
			name = 16;
		}else if(totalCount>=95000){
			name = 15;
		}else if(totalCount>=85000){
			name = 14;
		}else if(totalCount>=75000){
			name = 13;
		}else if(totalCount>=65000){
			name = 12;
		}else if(totalCount>=55000){
			name = 11;
		}else if(totalCount>=45000){
			name = 10;
		}else if(totalCount>=36000){
			name = 9;
		}else if(totalCount>=28000){
			name = 8;
		}else if(totalCount>=21000){
			name = 7;
		}else if(totalCount>=15000){
			name = 6;
		}else if(totalCount>=10000){
			name = 5;
		}else if(totalCount>=5000){
			name = 4;
		}else if(totalCount>=2000){
			name = 3;
		}else if(totalCount>=500){
			name = 2;
		}else if(totalCount>=100){
			name = 1;
		} else {
			name = 0;
		}
		return name;
	}


	public PlayerInTableRes.Builder buildPlayInTableInfo1(PlayerInTableRes.Builder res) {
		boolean bl;
		if (GameUtil.isPlayAhGame()){
			String version = ResourcesConfigsUtil.loadServerPropertyValue("gps_version","");
			if (StringUtils.isNotBlank(version)&&StringUtils.isNotBlank(this.version)&&!LoginUtil.checkVersion(version,1,this.version,1)){
				bl = true;
			}else{
				bl = false;
			}
		}else{
			bl = true;
		}

		if (bl){
			if (!StringUtils.isBlank(myExtend.getLatitudeLongitude())) {
				res.setGps(myExtend.getLatitudeLongitude());
			}
			if (this.userState!=null){
				res.setUserSate(this.userState);
			}else{
				res.setUserSate(1);
			}

			res.setDesignation(loadDesignation());
		}

		return res;
	}

	public abstract void initPais(String handPai, String outPai);

	/**
	 * 比赛场结束之后
	 */
	public void endCompetition() {
		endCompetition1();
	}

	/**
	 * 创建者大结算后的处理
	 */
	public void endCreateBigResultBureau(int playBureau) {
		if (playBureau > 10) {
			playBureau = 2;
		} else {
			playBureau = 1;
		}
		if (!isRobot())
			LogDao.getInstance().insertCardsConsumeCards(userId, regBindId, usedCards, playBureau, reginTime);
	}

	/**
	 * 比赛场结束之后
	 */
	public abstract void endCompetition1();

	public int getActionCount() {
		return actionCount;
	}

	public void changeActionCount(int actionCount) {
		this.actionCount += actionCount;
	}

	public long getLoginActionTime() {
		return loginActionTime;
	}

	public void setLoginActionTime(long loginActionTime) {
		this.loginActionTime = loginActionTime;
	}

	public MyExtend getMyExtend() {
		return myExtend;
	}

	public int getRegBindId() {
		return regBindId;
	}

	public void setRegBindId(int regBindId) {
		this.regBindId = regBindId;
	}

	public String getVc() {
		return vc;
	}

	public String getOs() {
		return os;
	}

	/**
	 * 开始下一局准备
	 *
	 * @return
	 */
	public boolean isStartNextReady() {
		return true;
	}

	/**
	 * 可以抽奖的大转盘次数
	 *
	 * @return
	 */
	public int getCanDrawCount() {
		int drawCount = (int) (-usedCards / 15);
		drawCount = drawCount - getDrawLottery();
		return drawCount;
	}

	/**
	 * 登陆检查
	 *
	 * @return
	 */
	public void checkLogin() {
		Date noticeDate = NoticeDao.getInstance().selectNewNoticeTime();

		List<Integer> tips = new ArrayList<>();
		if (noticeDate != null && noticeDate.getTime() > myExtend.getNoticeTime()) {
			// 发送消息提示
			tips.add(TipsEnum.message.getId());
		}

		boolean canGetAward = myActivity.isCanGetAward(ActivityConstant.activity_logindays, getLoginDays());
		if (getCanDrawCount() > 0 || canGetAward) {
			tips.add(TipsEnum.draw.getId());
		}
		writeOutSyncComMessage(WebSocketMsgType.res_code_tips, true, tips);

		// 检查跑马灯
		List<MarqueeMsg> marqueeMsgs = MarqueeManager.getInstance().getMarquee();
		writeOutSyncComMessage(WebSocketMsgType.res_code_marquee, true, JacksonUtil.writeValueAsString(marqueeMsgs));

	}

	public boolean isLc() {
		return GameServerConfig.isDeveloper() && (flatId.startsWith("lc") || flatId.startsWith("cc") || flatId.equals("bd1001"));
	}

	/**
	 * 是否在打比赛场
	 *
	 * @return
	 */
	public boolean isMatching() {
//		UserGameSite userGameSite = GameSiteDao.getInstance().queryUserGameSite(getUserId());
//		if (userGameSite == null) {
//			return false;
//		}
//		if (userGameSite.getGameSiteId() > 0 && userGameSite.getRoundNum() > 0) {
//			return true;
//		} else {
//			return false;
//		}
		return false;
	}

	public void loadSigns() {
		Calendar ca = Calendar.getInstance();
		int year = ca.get(Calendar.YEAR);
		int month = ca.get(Calendar.MONTH) + 1;
		ca.set(year, month, 1);
		ca.add(Calendar.DATE, -1);
		int endDay = ca.get(Calendar.DATE);
		String begin = year + "-" + (month>=10?month:("0"+month)) + "-01 00:00:00";
		String end = year + "-" + (month>=10?month:("0"+month)) + "-" + endDay+" 23:59:59";
		loadSigns(begin,end,1);
	}

	/**
	 * 获得玩家的当月已签到情况
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param type 按天1，年月日2
	 */
	public void loadSigns(String begin,String end,int type) {
		synchronized (this){
			Calendar ca = Calendar.getInstance();
			List<UserSign> lists = UserSignDao.getInstance().getUserSign(userId, begin, end);
			if (signs == null) {
				signs = new ArrayList<>();
			} else {
				signs.clear();
			}
			ymdSigns.clear();

			if (lists != null && lists.size() != 0) {
				if (type==1){
					for (UserSign sign : lists) {
						Calendar now = Calendar.getInstance();
						ca.setTime(sign.getSignTime());
						// 日期正确
						if (ca.get(Calendar.DATE) <= now.get(Calendar.DATE)) {
							// 避免同一天的重复记录
							if (!signs.contains(ca.get(Calendar.DATE))) {
								signs.add(ca.get(Calendar.DATE));
							}
						}
					}
				}else{
					SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
					for (UserSign sign : lists) {
						if (sign.getSignTime()!=null){
							String ymd=sdf.format(sign.getSignTime());
							if (!ymdSigns.contains(ymd)){
								ymdSigns.add(ymd);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 发送签到信息
	 * @param isOut 是否弹出
	 * @param isInRoom
	 * @param type 按天1，年月日2
	 */
	public void writeSignInfo(String isOut, String isInRoom,int type) {
		// 发送签到的消息
		Calendar ca = Calendar.getInstance();

		if (type==1){
			int year = ca.get(Calendar.YEAR);
			int month = ca.get(Calendar.MONTH) + 1;
			int date = ca.get(Calendar.DATE);
			ca.set(Calendar.DATE, 1);
			int week = ca.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : ca.get(Calendar.DAY_OF_WEEK) - 1;
			int days = ca.getActualMaximum(Calendar.DATE);
			writeComMessage(WebSocketMsgType.res_code_signinfo, signs, ""+ year, ""+ month, ""+ date, ""+ week, ""+ days, isOut, isInRoom);
		}else{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
			String ymdDate=sdf.format(ca.getTime());
			int today=0;
			int num=1;

			try {
				int len = ymdSigns.size();
				if (len > 0) {
					if (ymdSigns.contains(ymdDate)) {
						today = 1;
					}

					SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(ca.getTime());
					calendar.add(Calendar.DAY_OF_YEAR, -1);
					String ymdDate0 = sdf0.format(calendar.getTime());
					List<UserSign> userSigns = UserSignDao.getInstance().getUserSign(getUserId(),ymdDate0+" 00:00:00",ymdDate0+" 23:59:59");

					if (userSigns==null || userSigns.size()==0){
					}else {
						String[] msgs = userSigns.get(0).getExtend().split(",");
						if (msgs.length >= 3 && NumberUtils.isDigits(msgs[2])) {
							int signDays = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("signDays"), 7);
							int cur = NumberUtils.toInt(msgs[2]);
							num = cur >= signDays ? 1 : (cur + 1);
						} else {
							if (len == 1) {
								ca.add(Calendar.DAY_OF_YEAR, -1);
								if (ymdSigns.get(0).equals(sdf.format(ca.getTime()))) {
									num = 2;
								}
							} else {
								len = len - 1;
								ca.set(Calendar.HOUR_OF_DAY, 12);

								boolean bl = true;
								if (Integer.parseInt(ymdSigns.get(len)) >= Integer.parseInt(ymdDate)) {
									today = 1;
								}else{
									today = 0;
									ca.add(Calendar.DAY_OF_YEAR, -1);
									if (!ymdSigns.get(len).equals(sdf.format(ca.getTime()))){
										num = 1;
										bl = false;
									}
								}

								if (bl){
									int series = today == 1 ? 0 : 1;
									for (int i = len; i > 0; i--) {
										ca.setTime(sdf.parse(ymdSigns.get(i)));
										ca.add(Calendar.DAY_OF_YEAR, -1);
										if (!ymdSigns.get(i - 1).equals(sdf.format(ca.getTime()))) {
											break;
										} else {
											series++;
										}
									}

									num = series + 1;
								}
							}
						}
					}
				}
			}catch (Exception e){
				LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
			}

			writeComMessage(WebSocketMsgType.res_code_signinfo,today,num,isOut,isInRoom,ymdDate,ymdSigns);
		}

	}

	/**
	 * 签到
	 */
	public void sign(int date, Date now, int diamond, int gold,int index) {
		// 在已签集合里加入今天
		if (date>10000000){
			ymdSigns.add(String.valueOf(date));
		}else{
			signs.add(date);
		}

		UserSign userSign = new UserSign();
		userSign.setUserId(userId);
		userSign.setSignTime(now);
		StringBuilder sb = new StringBuilder();
		if (diamond>0) {
			// 更新钻石
			changeCards(diamond, 0, false, CardSourceType.sign);
		}
		sb.append(diamond).append(",");
		if (gold>0) {
			// 更新金币
			changeGold(gold,0, 998);
		}
		sb.append(gold).append(",");
		sb.append(index);
		userSign.setExtend(sb.toString());
		// 更新签到记录
		UserSignDao.getInstance().sign(userSign);
	}

	/**
	 * 获取玩家所有房卡或钻石
	 * @return
	 */
	public long loadAllCards(){
		return freeCards + cards;
	}

	/**
	 * 获取玩家所有金币
	 * @return
	 */
	public long loadAllGolds(){
		if (isRobot()){
			return loadGoldRobot().getAllGold();
		}else if (goldPlayer == null){
			return 0;
		}else{
			return goldPlayer.getAllGold();
		}
	}

	/**
	 * 加载金币玩家身份
	 */
	public void loadGoldPlayer(boolean canCreate) {
		if (isRobot()){
			loadGoldRobot();
		}else{
			GoldPlayer goldInfo;
			if (GoldConstans.isGoldSiteOpen()) {
				try {
					goldInfo = GoldDao.getInstance().selectGoldUserByUserId(userId);
					int drawRemedyCount = GoldDao.getInstance().selectDrawRemedyCount(TimeUtil.formatDayTime2(TimeUtil.now()), userId);
					if (goldInfo == null) {
						if (!canCreate && !"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("init_user_gold_msg","1"))){
							goldInfo = new GoldPlayer();
							goldInfo.setUserId(userId);
							setGoldPlayer(goldInfo);
							return;
						}
						goldInfo = new GoldPlayer();
						goldInfo.setUserId(getUserId());
						goldInfo.setUserName(getName());
						goldInfo.setUserNickName(getName());
						goldInfo.setHeadimgurl(getHeadimgurl());
						goldInfo.setSex(getSex());

						SystemCommonInfo systemCommonInfo = SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.goldGive);

						long give;
						if (systemCommonInfo == null || !NumberUtils.isDigits(systemCommonInfo.getContent())){
							give = Long.parseLong(SystemCommonInfoType.goldGive.getContent());
						} else{
							give = Long.parseLong(systemCommonInfo.getContent());
						}

						goldInfo.setFreeGold(give);
						goldInfo.setRegTime(new Date());
						goldInfo.setLastLoginTime(goldInfo.getRegTime());
						GoldDao.getInstance().createGoldUser(goldInfo);

						setGoldPlayer(goldInfo);
						writeGoldMessage(goldInfo.getFreeGold());
					} else {
						goldInfo.setLastLoginTime(new Date());
						if (canCreate){
							Map<String ,Object> map=new HashMap<>();
							map.put("lastLoginTime",goldInfo.getLastLoginTime());
							map.put("userId",userId);
							GoldDao.getInstance().updateGoldUser(map);
						}
						goldInfo.setDrawRemedyCount(drawRemedyCount);
						setGoldPlayer(goldInfo);
					}
				} catch (Exception e) {
					LogUtil.e("loadGoldPlayer err-->",e);
				}
			} else {
				goldInfo = new GoldPlayer();
				goldInfo.setUserId(userId);
				setGoldPlayer(goldInfo);
			}
		}
	}

	public void changeGold(int num, int playType) {
		changeGold(0, num, true, playType, true);
	}

	public void changeGold(int freeNum, int num, int playType) {
		changeGold(freeNum, num, true, playType, true);
	}

	/**
	 * 改变金币
	 * @param isRecord
	 * @param isWrite 是否推送
	 */
	public void changeGold(int freeNum, int num, boolean isRecord, int playType, boolean isWrite) {
		synchronized(this) {
			long gold = getGoldPlayer().getGold();
			long freeGold = getGoldPlayer().getFreeGold();
			long temp1 = 0;//free
			long temp2 = 0;//common

			if (num < 0) {
				// temp等于绑定金币 + cards
				long temp = freeGold + num;
				if (temp >= 0) {
					// 金币足够
					freeGold = temp;
					temp2 = 0;
					temp1 = -num;
				} else {
					// 金币不足，先用完绑定金币，再用普通金币
					freeGold = 0;
					temp2 = -temp;
					temp1 = (-num) - temp2;
				}
				gold -= temp2;
			} else {
				gold += num;
			}
			// 增加免费金币
			freeGold += freeNum;

			// 统计消耗
			long usedGoldChange = 0;
			if (temp1 > 0 && isRecord) {
				changeUsedGold(-temp1);
				usedGoldChange += -temp1;
			}
			if (temp2 > 0 && isRecord) {
				changeUsedGold(-temp2);
				usedGoldChange += -temp2;
			}
			if (temp2 != 0 || temp1 != 0) {
				PlayerManager.getInstance().changeConsumeGold(this, (int) -temp2, (int) -temp1, playType);
			}

			// 统计增加
			if (freeNum > 0) {
				temp1 = -freeNum;
			}
			if (num > 0) {
				temp2 = -num;
			}

			changeGold(freeGold, gold);

			if (isRobot()) {
				return;
			}

			Map<String, Object> log = new HashMap<>();
			log.put("freeGold", -temp1);
			log.put("gold", -temp2);
			log.put("playType", playType);
			log.put("isRecord", isRecord ? 1 : 0);

			LogUtil.msgLog.info("statistics:userId=" + userId + ",tableId=" + playingTableId + ",isRecord=" + isRecord + " ,playType=" + playType + ",freeGold=" + temp1 + ",gold=" + temp2 + ",rest freeGold=" + freeGold + ",rest gold=" + gold);
			sendActionLog(LogConstants.reason_consumecards, JacksonUtil.writeValueAsString(log));

			if (temp2 != 0 || temp1 != 0) {
				GoldDao.getInstance().updateUserGold(userId, -temp1, -temp2, usedGoldChange);
				if (isWrite) {
					writeGoldMessage((int) (-temp1 - temp2));
				}
			}
		}
	}

	/**
	 * 更新玩家的金币
	 */
	private void changeGold(long freeGold, long gold) {
		GoldPlayer player = getGoldPlayer();
		player.setFreeGold(freeGold);
		player.setGold(gold);
		getMyExtend().updateUserMaxGold();
	}

	/**
	 * 改变玩家消耗的金币
	 */
	private void changeUsedGold(long gold) {
		GoldPlayer player = getGoldPlayer();
		player.changeUsedGold(gold);
	}

	/**
	 * 增加金币场总局数
	 */
	protected void changeGoldPlayCount() {
		getGoldPlayer().changePlayCount();
		getMyExtend().getUserTaskInfo().alterDailyGoldGameNum();
		changeExtend();
	}

	/**
	 * 增加金币场胜局数
	 */
	protected void changeGoldWinCount() {
		getGoldPlayer().changeWinCount();
		getMyExtend().getUserTaskInfo().alterDailyWinGameNum();
		changeExtend();
	}

	/**
	 * 增加金币场败局数
	 */
	protected void changeGoldLoseCount() {
		getGoldPlayer().changeLoseCount();
	}

	/**
	 * 芒果跑得快增加积分
	 * @param addNum 获取积分数
	 * @param sourceType 积分来源  需约定来源类型(1金币场2比赛场)
	 */
	public void addJiFen(int addNum, int sourceType){
		try {
			Date currentDate = new Date();
			UserExtend userExtend = new UserExtend();
			userExtend.setUserId(String.valueOf(userId));
			userExtend.setCreatedTime(currentDate);
			userExtend.setModifiedTime(currentDate);
			userExtend.setMsgDesc(UserResourceType.JIFEN.getName());
			userExtend.setMsgKey(UserResourceType.JIFEN.name());
			userExtend.setMsgType(UserResourceType.JIFEN.getType());
			userExtend.setMsgState("1");
			userExtend.setMsgValue(String.valueOf(addNum));
			UserDao.getInstance().saveOrUpdateUserExtend(userExtend);
			// 积分获取增加操作日志
			JiFenRecordLogDao.getInstance().saveJiFenRecordLog(new JiFenRecordLog(userId, addNum, sourceType, currentDate));
			upgradeExp(addNum);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	/**
	 * 芒果跑得快更新段位积分
	 * @param addExp
	 * @return
	 */
	protected boolean upgradeExp(int addExp) {
		if (addExp < 1) {
			return false;
		}
		GoldPlayer goldPlayer = getGoldPlayer();
		int curGrade = goldPlayer.getGrade();
		int curGradeExp = goldPlayer.getGradeExp();
		GradeExpConfigInfo curConfigInfo = GradeExpConfig.getGradeExpConfigInfo(curGrade);
		if (curGrade == GradeExpConfig.maxGrade) {// 已满级
			if (curGradeExp == curConfigInfo.getNeedExp()) {//满经验
				return false;
			}
			int exp = curGradeExp + addExp;
			if (exp > curConfigInfo.getNeedExp()) {
				exp = curConfigInfo.getNeedExp();
			}
			goldPlayer.setGradeExp(exp);
			// 推送前端经验值更新消息
			return false;
		}
		// 未满级
		int exp = curGradeExp + addExp;
		boolean upgrade = false;
		while (exp >= curConfigInfo.getNeedExp()) {
			upgrade = true;
			exp = exp - curConfigInfo.getNeedExp();
			goldPlayer.changeGrade();
			//下一级配置
			curConfigInfo = GradeExpConfig.getGradeExpConfigInfo(goldPlayer.getGrade());
			if (goldPlayer.getGrade() == GradeExpConfig.maxGrade
					&& exp >= GradeExpConfig.getGradeExpConfigInfo(GradeExpConfig.maxGrade).getNeedExp()) {
				exp = curConfigInfo.getNeedExp();
				break;
			}
		}
		goldPlayer.setGradeExp(exp);
		if (upgrade) {// 段位升级
			// 推送前端经验值更新消息
			GradeExpConfigInfo gradeExpConfigInfo = GradeExpConfig.getGradeExpConfigInfo(goldPlayer.getGrade());
			String gradeDesc = (gradeExpConfigInfo != null ) ? gradeExpConfigInfo.getDesc() : "";
			this.writeGradeMessage(goldPlayer.getGrade(), gradeDesc);
		}
		GoldDao.getInstance().updateGoldUserGrade(userId,goldPlayer.getGrade(),goldPlayer.getGradeExp());
		return upgrade;
	}

	public void writeRemoveBindMessage() {
		writeComMessage(WebSocketMsgType.res_code_removebind);
	}

	/**
	 * 获取金币场机器人
	 *
	 * @return
	 */
	public GoldPlayer loadGoldRobot(){
		if (goldPlayer == null){
			goldPlayer = new GoldPlayer();
			table = getPlayingTable();

			if (table!=null){
				List<Integer> list = GameConfigUtil.getIntsList(2, table.getModeId());
				if (list!=null&&list.size()>0){
					goldPlayer.setFreeGold(list.get(0).longValue());
				}
			}

			goldPlayer.setUserId(userId);
		}
		return goldPlayer;
	}

	/**
	 * 获取累计分
	 *
	 * @return
	 */
	public int loadAggregateScore(){
		return totalPoint;
	}

	public int loadTzScore() {
		return 0;
	}

	/**
	 * 是否在打比赛场
	 * @return
	 */
	public boolean isPlayingMatch(){
		return org.apache.commons.lang3.StringUtils.isNotBlank(loginExtend)&&JSON.parseObject(loginExtend).containsKey("match");
	}

	/**
	 * 获取玩家当前比赛场ID
	 * @return
	 */
	public String loadMatchId(){
		return StringUtils.isNotBlank(loginExtend)?JSON.parseObject(loginExtend).getString("match"):null;
	}

	public boolean joinMatch(String configId,boolean force){
		try {
			synchronized (this) {
				if (org.apache.commons.lang3.StringUtils.isBlank(loginExtend)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("match", configId);
					String loginExt = jsonObject.toString();
					Map<String, Object> map = new HashMap<>();
					map.put("loginExtend", loginExt);

					this.loginExtend = loginExt;
					this.matchId = configId;
					UserDao.getInstance().updateUser(String.valueOf(userId), map);
				} else {
					JSONObject jsonObject = JSON.parseObject(loginExtend);
					if (force || org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getString("match"))) {
						jsonObject.put("match", configId);
						String loginExt = jsonObject.toString();
						Map<String, Object> map = new HashMap<>();
						map.put("loginExtend", loginExt);
						this.loginExtend = loginExt;
						this.matchId = configId;

						UserDao.getInstance().updateUser(String.valueOf(userId), map);
					}
				}
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return false;
	}

	/**
	 *加入比赛场
	 * @return
	 */
	public boolean joinMatch(String configId){
		return joinMatch(configId,false);
	}

	/**
	 *退出比赛场
	 * @return
	 */
	public boolean quitMatch(){
		synchronized (this){
			if (org.apache.commons.lang3.StringUtils.isNotBlank(loginExtend)) {
				JSONObject jsonObject = JSON.parseObject(loginExtend);
				if (jsonObject.remove("match")!=null) {
					String loginExt = jsonObject.toString();
					Map<String, Object> map = new HashMap<>();
					map.put("loginExtend", loginExt);

					try {
						this.loginExtend = loginExt;
						this.matchId = null;
						UserDao.getInstance().updateUser(String.valueOf(userId), map);
					}catch (Exception e){
						LogUtil.errorLog.error("Exception:"+e.getMessage(),e);

						return false;
					}
				}else{
					this.matchId = null;
				}
			}else{
				this.matchId = null;
			}
		}
		return true;
	}

	/**
	 * 加载玩法配置，子类必须重写
	 * todo
	 */
	public static void loadWanfaPlayers(Class<? extends Player> cls){
	}

	public int getWinLoseCredit() {
		return winLoseCredit;
	}

	public void setWinLoseCredit(int winLoseCredit) {
		this.winLoseCredit = winLoseCredit;
	}

	public int getCommissionCredit() {
		return commissionCredit;
	}

	public void setCommissionCredit(int commissionCredit) {
		this.commissionCredit = commissionCredit;
	}


	public void setLastActionBureau(int lastActionBureau) {
		if(lastActionBureau < 0)
			lastActionBureau = 0;
		this.lastActionBureau = lastActionBureau;
	}

	public int getLastActionBureau() {
		return lastActionBureau;
	}

    public int getAutoPlayCheckedTime() {
        return autoPlayCheckedTime;
    }

    public void setAutoPlayCheckedTime(int autoPlayCheckedTime) {
        this.autoPlayCheckedTime = autoPlayCheckedTime;
    }

    public void addAutoPlayCheckedTime(int time){
        this.autoPlayCheckedTime += time;
    }

    public boolean isAutoPlayCheckedTimeAdded() {
        return autoPlayCheckedTimeAdded;
    }

    public void setAutoPlayCheckedTimeAdded(boolean autoPlayCheckedTimeAdded) {
        this.autoPlayCheckedTimeAdded = autoPlayCheckedTimeAdded;
    }

    public boolean isAutoPlayChecked() {
        return autoPlayChecked;
    }

    public void setAutoPlayChecked(boolean autoPlayChecked) {
        this.autoPlayChecked = autoPlayChecked;
    }


    /**
     * 改变房卡数量（消耗房卡不能使用该方法）
     *
     * @param cards
     * @param freeCards
     */
    public void notifyChangeCards(long cards, long freeCards, CardSourceType sourceType) {
        synchronized (this) {
            RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
            if (info != null) {
                this.cards = info.getCards();
                this.freeCards = info.getFreeCards();
            }
        }
        writeCardsMessage((int) (freeCards + cards));
        PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int) freeCards, (int) cards, 0, sourceType));
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public int getDissCount() {
        return dissCount;
    }

    public void addDissCount() {
        this.dissCount += 1;
    }

    public void setDissCount(int dissCount) {
        this.dissCount = dissCount;
    }

    public void updateGoldRoomActivity(boolean isWin) {
        try {
            GoldRoomActivity activity = getMyActivity().getGoldRoomActivity();
            boolean needSendComboWin = false;
            if (isWin) {
                // 累计胜利
                activity.setTotalWinCount(activity.getTotalWinCount() + 1);

                if(activity.getTotalWinCount() >= 8) {
                    needSendComboWin = true;
                    activity.setComboWinCount(activity.getComboWinCount() + 1);
                    // 连胜次数
                    if (activity.getComboWinCount() >= 3) {
                        activity.getComboWinAward().add(activity.getComboWinCount());
                    }
                }

                GoldRoomActivityProto.GoldRoomActivityMsg.Builder totalWinMsg =  GoldRoomActivityProto.GoldRoomActivityMsg.newBuilder();
                totalWinMsg.setReqCode("100");
                totalWinMsg.setTotalWinCount(activity.getTotalWinCount());
                totalWinMsg.setTotalWinAward(activity.getTotalWinAward());
                totalWinMsg.setTotalWinAwardCount(activity.getTotalWinAwardCount() - activity.getTotalWinAward());
//                writeSocket(totalWinMsg.build());
            } else {
                // 中断连胜
                activity.setComboWinCount(0);
                needSendComboWin = true;
            }
            changeActivity();
            saveBaseInfo();

            if(needSendComboWin) {
                GoldRoomActivityProto.GoldRoomActivityMsg.Builder comboWinMsg = GoldRoomActivityProto.GoldRoomActivityMsg.newBuilder();
                comboWinMsg.setReqCode("101");
                comboWinMsg.setComboWinCount(activity.getComboWinCount());
                comboWinMsg.addAllComboWinAward(activity.getComboWinAward());
//                writeSocket(comboWinMsg.build());
            }

        } catch (Exception e) {
            LogUtil.errorLog.error("updateGoldRoomActivity|error|" + getUserId() + "|" + isWin + "|" + e.getMessage(), e);
        }

    }
}
