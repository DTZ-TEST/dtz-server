package com.sy599.game.qipai.bbtz.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.bbtz.constant.BbtzConstants;
import com.sy599.game.qipai.bbtz.rule.CardType;
import com.sy599.game.qipai.bbtz.tool.CardTool;
import com.sy599.game.qipai.bbtz.tool.CardTypeTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MathUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BbtzTable extends BaseTable {
	private static final int JSON_TAG = 1;
	public static final int MAX_TIMEOUT = 10000;//最大等待时间
	/*** 当前牌桌上出的牌 */
	private List<Integer> nowDisCardIds = new ArrayList<>();
	/*** 玩家map */
	private Map<Long, BbtzPlayer> playerMap = new ConcurrentHashMap<Long, BbtzPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, BbtzPlayer> seatMap = new ConcurrentHashMap<Integer, BbtzPlayer>();
	/*** 最大玩家数量 */
	private int max_player_count = 0;

	private int isFirstRoundDisThree;// 首局是否出黑挑三

	private int showCardNumber = 0; // 是否显示剩余牌数量
	
	private volatile int tableStatus = 0;//特殊状态
	private volatile int wangSeat;//大王位置
	private volatile int bankerSeat;//庄位置
	
	private volatile int kechui;//1可锤
	private int zheng510k;//1 正510k
	private volatile int zhuDou;//1 助陡
	private int is4_3;//是否4带3
	private int isDaiWang;//是否带王
    private List<Integer> maiPaiList = new ArrayList<>();

	private int planeLength;//临时存放一轮中第一个出飞机的长度
    private List<Integer> nowScoreCard = new ArrayList<>();//当前轮的分牌
	private Map<Integer, Integer> groupScore = new HashMap<>();//1 庄家分 2闲家分
	private List<Integer> rank = new ArrayList<>();//排名 0上游位置 1中游位置

	/**
	 * 托管时间
	 */
	private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
	private volatile int autoReadyTimeOut = 5 * 24 * 60 * 60 * 1000;
    private CardType roundCardType = CardType.c0;

	
	public int getIs4_3() {
		return is4_3;
	}

	public void setIs4_3(int is4_3) {
		this.is4_3 = is4_3;
	}
	
	public List<Integer> getRank() {
		return rank;
	}

	public void setRank(List<Integer> rank) {
		this.rank = rank;
		changeExtend();
	}
	
	public void addRank(int seat){
		getRank().add(seat);
		changeExtend();
	}

	public List<Integer> getNowScoreCard() {
		return nowScoreCard;
	}

	public void setNowScoreCard(List<Integer> nowScoreCard) {
		this.nowScoreCard = nowScoreCard;
		changeExtend();
	}
	
	public void addNowScoreCard(List<Integer> nowScoreCard) {
		getNowScoreCard().addAll(nowScoreCard);
		changeExtend();
	}
	public void clearNowScoreCard(){
		getNowScoreCard().clear();
		changeExtend();
	}

	public Map<Integer, Integer> getGroupScore() {
		return groupScore;
	}

	public void setGroupScore(Map<Integer, Integer> groupScore) {
		this.groupScore = groupScore;
		changeExtend();
	}
	
	public void addGroupScore(int group, int score){
		if(getGroupScore().containsKey(group)){
			getGroupScore().put(group, getGroupScore().get(group)+score);
		}else{
			getGroupScore().put(group, score);
		}
		changeExtend();
	}

	public int getPlaneLength() {
		return planeLength;
	}

	public void setPlaneLength(int planeLength) {
		this.planeLength = planeLength;
		changeExtend();
	}

	public int getBankerSeat() {
		return bankerSeat;
	}

	public void setBankerSeat(int bankerSeat) {
		this.bankerSeat = bankerSeat;
		changeExtend();
	}
	
	public int getWangSeat() {
		return wangSeat;
	}

	public void setWangSeat(int wangSeat) {
		this.wangSeat = wangSeat;
		changeExtend();
	}

	public int getKechui() {
		return kechui;
	}

	public void setKechui(int kechui) {
		this.kechui = kechui;
	}

	public int getZheng510k() {
		return zheng510k;
	}

	public void setZheng510k(int zheng510k) {
		this.zheng510k = zheng510k;
	}

	public int getZhuDou() {
		return zhuDou;
	}

	public void setZhuDou(int zhuDou) {
		this.zhuDou = zhuDou;
	}

	public int getTableStatus() {
		return tableStatus;
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
		changeExtend();
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
		}
		if (isMatchRoom()) {
			autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutBbtz", 15 * 1000);
		} else if (isGoldRoom()) {
			autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutBbtz", 30 * 1000);
		}else if(isAutoPlay()){
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutBbtzNormal", 20 * 1000);
            autoReadyTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoReadyTimeOutBbtzNormal", 20 * 1000);
        }
	}

	public long getId() {
		return id;
	}

	public BbtzPlayer getPlayer(long id) {
		return playerMap.get(id);
	}

	/**
	 * 一局结束
	 */
	public void calcOver() {
		int bankerScore = getGroupScore().containsKey(1) ? getGroupScore().get(1) : 0;
		int playerScore = getGroupScore().containsKey(2) ? getGroupScore().get(2) : 0;
		int bankerChaoFen = bankerChaoFen();
		if(bankerChaoFen != 0){
			bankerScore += bankerChaoFen;
			playerScore -= bankerChaoFen;
		}
		BbtzPlayer bankerPlayer = (BbtzPlayer)getSeatMap().get(getBankerSeat());

		if (bankerPlayer == null){
			LogUtil.errorLog.error("calcOver error:tableId="+id+",bankerSeat="+getBankerSeat()+",seats="+getSeatMap().keySet());
			return;
		}

		int bankerPoint = 0;
		for (BbtzPlayer player : seatMap.values()) {
			player.changeState(player_state.over);
			if(player.getSeat() == bankerPlayer.getSeat()){
				continue;
			}
			int point = 1;
			if(player.getChui() == 1){
				point *= 2;
			}
			if(player.getDou() == 1){
				point *= 2;
			}
			if(bankerPlayer.getChui() == 1){
				point *= 2;
			}
			if(bankerPlayer.getKaiqiang() == 1){
				point *= 2;
			}else if( getMaxPlayerCount() == 3 && (playerScore > bankerScore
					|| (playerScore == bankerScore && !getRank().isEmpty() && getRank().get(0) != getBankerSeat()))){
				point *= 2;
			}
			if(bankerPlayer.getDou() == 1){
				point *= 2;
			}
			if(bankerScore > playerScore){
				if(playerScore <= 0){
					point *= 2;
				}
				bankerPoint += point;
				player.calcLost(1, -point);
			}else if(bankerScore < playerScore){
				if(bankerScore <= 0){
					point *= 2;
				}
				bankerPoint -= point;
				player.calcWin(1, point);
			}else{
				if(getRank().isEmpty()){
					bankerPoint -= point;
					player.calcWin(1, point);
				}else{
					if(getRank().get(0)==getBankerSeat()){
						bankerPoint += point;
						player.calcLost(1, -point);
					}else{
						bankerPoint -= point;
						player.calcWin(1, point);
					}
				}
			}
		}
		if(bankerPoint > 0){
			bankerPlayer.calcWin(1, bankerPoint);
		}else{
			bankerPlayer.calcLost(1, bankerPoint);
		}
		
		ClosingInfoRes.Builder res = sendAccountsMsg(playBureau >= totalBureau, null, false);
		
		saveLog(playBureau == totalBureau, 0, res.build());
		setLastWinSeat(getRank().isEmpty() ? 0 : getRank().get(0));
		calcAfter();
		if (playBureau >= totalBureau) {
			calcOver1();
			calcOver2();
            calcCreditNew();
			diss();
		} else {
			initNext();
			calcOver1();
		}
		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}
	/**
	 * 庄朝分
	 */
	public int bankerChaoFen(){
		if(getRank().isEmpty()){
			return 0;
		}
		int mingci = findPlayerMingCi(getBankerSeat());
		switch(mingci){
			case 1:
				return 20;
			case 2: {
                if (getMaxPlayerCount() == 2) {
                    // 两人没有中游
                    return -20;
                } else {
                    return 0;
                }
            }
			default:
				return -20;
		}
	}

	public void saveLog(boolean over,long winId, Object resObject) {
		ClosingInfoRes res=(ClosingInfoRes)resObject;
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
		String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
		String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		Date now = TimeUtil.now();

		UserPlaylog userLog = new UserPlaylog();
		userLog.setUserId(creatorId);
		userLog.setLogId(playType);
		userLog.setTableId(id);
		userLog.setRes(logRes);
		userLog.setTime(now);
		userLog.setTotalCount(totalBureau);
		userLog.setCount(playBureau);
		userLog.setStartseat(lastWinSeat);
		userLog.setOutCards(playLog);
		userLog.setExtend(logOtherRes);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
		long logId = TableLogDao.getInstance().save(userLog);
		saveTableRecord(logId, over, playBureau);

		if (!isGoldRoom()) {
			for (BbtzPlayer player : playerMap.values()) {
				player.addRecord(logId, playBureau);
			}
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	public Map<String,Object>  saveDB(boolean asyn) {
		if (id < 0) {
			return null;
		}

		Map<String, Object> tempMap = loadCurrentDbMap();
		if (!tempMap.isEmpty()) {
			tempMap.put("tableId", id);
			tempMap.put("roomId", roomId);
			if (tempMap.containsKey("players")) {
				tempMap.put("players", buildPlayersInfo());
			}
			if (tempMap.containsKey("outPai1")) {
				tempMap.put("outPai1", StringUtil.implodeLists(seatMap.get(1).getOutPais()));
			}
			if (tempMap.containsKey("outPai2")) {
				tempMap.put("outPai2", StringUtil.implodeLists(seatMap.get(2).getOutPais()));
			}
			if (tempMap.containsKey("outPai3")) {
				tempMap.put("outPai3", StringUtil.implodeLists(seatMap.get(3).getOutPais()));
			}
			if (tempMap.containsKey("handPai1")) {
				tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
			}
			if (tempMap.containsKey("handPai2")) {
				tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
			}
			if (tempMap.containsKey("handPai3")) {
				tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
			}
			if (tempMap.containsKey("answerDiss")) {
				tempMap.put("answerDiss", buildDissInfo());
			}
			if (tempMap.containsKey("nowDisCardIds")) {
				tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
			}
			if (tempMap.containsKey("extend")) {
				tempMap.put("extend", buildExtend());
			}
//			TableDao.getInstance().save(tempMap);
		}
		return tempMap.size() > 0 ? tempMap : null;
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		wrapper.putInt(1, max_player_count);
		wrapper.putInt(2, tableStatus);
		wrapper.putInt(3, wangSeat);
		wrapper.putInt(4, bankerSeat);
		wrapper.putInt(5, kechui);
		wrapper.putInt(6, zheng510k);
		wrapper.putInt(7, zhuDou);
		wrapper.putInt(8, planeLength);
		wrapper.putString(9, strListToJSON(nowScoreCard));
		wrapper.putString(10, strMapToJSON(groupScore));
		wrapper.putString(11, strListToJSON(rank));
		wrapper.putInt(12, showCardNumber);
		wrapper.putInt(13, is4_3);
        wrapper.putInt(14, isDaiWang);
        wrapper.putString(15, StringUtil.implode(maiPaiList, ","));
		return wrapper;
	}
	private String strListToJSON(List<Integer> list){
		JSONArray jsonArray = new JSONArray();
        for (Integer obj : list) {
            jsonArray.add(obj);
        }
        return jsonArray.toString();
	}
	private String strMapToJSON(Map<Integer, Integer> map){
		JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            jsonObject.put(entry.getKey().toString(), entry.getValue());
        }
        return jsonObject.toString();
	}

	protected String buildPlayersInfo() {
		StringBuffer sb = new StringBuffer();
		for (BbtzPlayer pdkPlayer : playerMap.values()) {
			sb.append(pdkPlayer.toInfoStr()).append(";");
		}
		// playerInfos = sb.toString();
		return sb.toString();
	}

	public void changePlayers() {
		dbParamMap.put("players", JSON_TAG);
	}

	public void changeCards(int seat) {
		dbParamMap.put("outPai" + seat, JSON_TAG);
		dbParamMap.put("handPai" + seat, JSON_TAG);
	}

	/**
	 * 下一次出牌的seat
	 * 
	 * @return
	 */
	public int getNextDisCardSeat() {
		int seat = 0;
		if (state != table_state.play) {
			return seat;
		}
		if (disCardRound == 0) {
			// 还没有出过牌 看黑桃3在谁手里
			for (BbtzPlayer player : playerMap.values()) {
				if (player.getHandPais().contains(403)) {
					seat = player.getSeat();
					break;
				}
			}
            if(seat == 0 || getMaxPlayerCount() == 2 ){
                // 两人玩法没有找到黑桃3，随机出头
                seat = new Random().nextInt(getMaxPlayerCount()) + 1;
            }
		} else {
			if (nowDisCardSeat != 0) {
				seat = nowDisCardSeat >= max_player_count ? 1 : nowDisCardSeat + 1;
				int i = 0;
				while (i < 5) {
		            if (seatMap.get(seat).getHandPais().size() == 0) {
		                seat = (seat + 1) > max_player_count ? 1 : seat + 1;
		            } else break;
		            i++;
		        }
				if(i>=5){
					seat = 0;
				}
			}
		}
		return seat;
	}

	public BbtzPlayer getPlayerBySeat(int seat) {
		int next = seat >= max_player_count ? 1 : seat + 1;
		return seatMap.get(next);

	}

	public Map<Integer, Player> getSeatMap() {
		Object o = seatMap;
		return (Map<Integer, Player>) o;
	}

	@Override
	public CreateTableRes buildCreateTableRes(long userId, boolean isrecover,boolean isLastReady) {
		CreateTableRes.Builder res = CreateTableRes.newBuilder();
		buildCreateTableRes0(res);
		res.setNowBurCount(getPlayBureau());
		res.setTotalBurCount(getTotalBureau());
		res.setGotyeRoomId(gotyeRoomId + "");
		res.setTableId(getId() + "");
		res.setWanfa(playType);
		List<PlayerInTableRes> players = new ArrayList<>();
		int outWang = 0;//大王是否打出去
		for (BbtzPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
			if (playerRes==null){
				continue;
			}
			if (player.getUserId() == userId) {
				// 如果是自己重连能看到手牌
				playerRes.addAllHandCardIds(player.getHandPais());
			} else {
				// 如果是别人重连，轮到出牌人出牌时要不起可以去掉
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(nowDisCardIds);
			}
			players.add(playerRes.build());
			if(player.getSeat() == wangSeat && !player.getHandPais().contains(517)){
				outWang = 1;
			}
		}
		res.addAllPlayers(players);
		int nextSeat = getNowDisCardSeat();
		if (nextSeat != 0) {
			res.setNextSeat(nextSeat);
		}
		res.setRenshu(this.max_player_count);
		res.addExt(this.payType); //0
		res.addExt(this.tableStatus);//1
		res.addExt(this.wangSeat);//2
		res.addExt(this.bankerSeat);//3
		res.addExt(this.kechui);//4
		res.addExt(this.zheng510k);//5
		res.addExt(this.zhuDou);//6
		res.addExt(getGroupScore().containsKey(1) ? getGroupScore().get(1) : 0);//7
		res.addExt(getGroupScore().containsKey(2) ? getGroupScore().get(2) : 0);//8
		res.addExt(this.showCardNumber);//9
		res.addExt(this.is4_3);//10
		res.addExt(outWang);//11
		res.addExt(isDaiWang);//12
        // 信用分
        res.addExt(creditMode); //13
        res.addExt(creditJoinLimit);//14
        res.addExt(creditDissLimit);//15
        res.addExt(creditDifen);//16
        res.addExt(creditCommission);//17
        res.addExt(creditCommissionMode1);//18
        res.addExt(creditCommissionMode2);//19
        res.addExt(isAutoPlay()?1:0);//20
        res.addExt(autoTimeOut);//21
        res.addExt(autoReadyTimeOut);//22
		res.addAllScoreCard(this.nowScoreCard);
		return res.build();
	}
	public int findPlayerMingCi(int seat){
		if(getRank().isEmpty()){
			return 0;
		}
		for(int i=1;i<=getRank().size();i++){
			if(seat == getRank().get(i-1)){
				return i;
			}
		}
		return 0;
	}

	public int getOnTablePlayerNum() {
		int num = 0;
		for (BbtzPlayer player : seatMap.values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 要不起
	 * @param player
	 */
	public void notLet(BbtzPlayer player) {
		// 要不起
		player.setIsNoLet(1);
		List<Integer> cards = new ArrayList<>();
		cards.add(0);
		player.addOutPais(cards);
		int nextSeat = getNextDisCardSeat();
		setNowDisCardSeat(nextSeat);
		PlayCardRes.Builder res = PlayCardRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setNextSeat(nextSeat);
		res.setCardType(0);
		if(getNowDisCardIds().size()==1 && getNowDisCardIds().get(0) == 517){
			res.setIsPlay(2);
		}else{
			res.setIsPlay(0);
		}
		if (player.getHandPais().size() == 1) {
			// 报单
			res.setIsBt(1);
		}else if(player.getHandPais().size() == 2){
			res.setIsBt(2);
		}
		res.setIsLet(1);
		boolean checkRound = checkRound();
		BbtzPlayer passPlayer = null;
		for (BbtzPlayer pdkPlayer : seatMap.values()) {
			PlayCardRes.Builder copy = res.clone();
			if(pdkPlayer.getSeat() == getNowDisCardSeat() && pdkPlayer.getSeat() != getDisCardSeat() && !checkRound){
				int isLet = CardTypeTool.canPlayCompare(seatMap.get(nextSeat).getHandPais(), getNowDisCardIds(), CardTypeTool.jugdeType(getNowDisCardIds(), this), this);
				copy.setIsLet(isLet);
				if(isLet == 0 && getNowDisCardIds().size()==1 && getNowDisCardIds().get(0) == 517){
					passPlayer = pdkPlayer;
				}
			}
			pdkPlayer.writeSocket(copy.build());
		}
		addPlayLog(player.getSeat() + "_");
        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("buYao");
        LogUtil.msgLog.info(sb.toString());
		if(checkRound){//本轮结束
			sendRound();
			getNowDisCardIds().clear();
			clearIsNotLet();
			if (checkOver()) {
				state = table_state.over;
				changeTableStatus(BbtzConstants.TABLE_OVER, false);
			}
			return;
		}
//		if(!getRank().isEmpty() && getRank().get(0) == getBankerSeat() && calcNextSeat(player.getSeat()) == getBankerSeat()){
//			sendRound();
//			clearIsNotLet();
//			state = table_state.over;
//			changeTableStatus(BbtzConstants.TABLE_OVER, false);
//		}
		if(passPlayer != null && !isOver()){
			this.playCommand(passPlayer, null, null);
		}
	}
	/**
	 * 发送本轮结算消息
	 */
	private void sendRound(){
		int group = getDisCardSeat() == getBankerSeat() ? 1 : 2;//1庄2闲
		if(!getNowScoreCard().isEmpty()){
			BbtzPlayer winPlayer = (BbtzPlayer)getSeatMap().get(getDisCardSeat());
			winPlayer.addScoreCard(getNowScoreCard());
			int fen = CardTypeTool.calcScore(getNowScoreCard());
			addGroupScore(group, fen);
			ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_ROUND_OVER, winPlayer.getSeat(), group, getGroupScore().get(group), getNowScoreCard());
	        broadMsg(build.build());
	        addPlayLog(group + "000_" + getGroupScore().get(group));
			clearNowScoreCard();

            StringBuilder sb = new StringBuilder("Bbtz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(winPlayer.getUserId());
            sb.append("|").append(winPlayer.getSeat());
            sb.append("|").append(winPlayer.isAutoPlay() ? 1 : 0);
            sb.append("|").append(winPlayer.getAutoPlayCheckedTime());
            sb.append("|").append("roundOver");
            sb.append("|").append((group == 1 ? "庄" : "闲"));
            sb.append("|").append(getGroupScore().get(group));
            LogUtil.msgLog.info(sb.toString());
		}else{
			ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_ROUND_OVER);
	        broadMsg(build.build());
	        addPlayLog(group + "000_" + (getGroupScore().containsKey(group)?getGroupScore().get(group):0));

            StringBuilder sb = new StringBuilder("Bbtz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("roundOver");
            sb.append("|").append((group == 1 ? "庄" : "闲"));
            sb.append("|").append(""+(getGroupScore().containsKey(group) ? getGroupScore().get(group) : 0));
            LogUtil.msgLog.info(sb.toString());
		}
	}
	/**
	 * 本轮是否结束
	 */
	public boolean checkRound(){
		int count = 0;
		for (BbtzPlayer player : seatMap.values()){
			if(player.getSeat() == getDisCardSeat()){
				continue;
			}
			if(player.getIsNoLet() == 1 || player.getHandPais().isEmpty()){
				count++;
			}
		}
		if(count >= getMaxPlayerCount()-1){
			return true;
		}
		return false;
	}
	/**
	 * 本局是否结束
	 */
	private boolean checkOver(){
		if(getRank().size() >= getMaxPlayerCount()-1){
			return true;
		}else if(!getRank().isEmpty()){
			if(getRank().get(0) == getBankerSeat()){
				return true;
			}else{
				if(groupScore.containsKey(2) && groupScore.get(2) == 100){
					return true;
				}else if(groupScore.size() == 2 && groupScore.get(1) >= 25 && groupScore.get(2) >= 50){
					return true;
				}else if(groupScore.size() == 2 && groupScore.get(1) > 70 && groupScore.get(2) > 0){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 出牌
	 * 
	 * @param player
	 * @param cards
	 */
	public void disCards(BbtzPlayer player, List<Integer> cards, CardType cardType) {
		clearIsNotLet();

		if (cardType == null){
			cardType = CardTypeTool.jugdeType(cards, this);
		}

		setDisCardSeat(player.getSeat());
		player.addOutPais(new ArrayList<>(cards));
		setNowDisCardIds(cards);
		List<Integer> scoreCard = CardTypeTool.getScoreCard(cards);
		if(!scoreCard.isEmpty()){
			addNowScoreCard(scoreCard);
		}
		int playeRrank = 0;
		if(player.getHandPais().isEmpty()){
			addRank(player.getSeat());
			if(getRank().size() == 1){
				player.setFirstCount(player.getFirstCount()+1);
			}
			playeRrank = getRank().size();
		}
		int nextSeat = getNextDisCardSeat();
		setNowDisCardSeat(nextSeat);

		// 构建出牌消息
		PlayCardRes.Builder res = PlayCardRes.newBuilder();
		res.addAllCardIds(getNowDisCardIds());
		res.setCardType(cardType.getType());
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setNextSeat(getNowDisCardSeat());
		res.setIsPlay(1);
		if (player.getHandPais().size() == 1) {
			res.setIsBt(1);
		}else if (player.getHandPais().size() == 2) {
			res.setIsBt(2);
		}
		res.addAllScoreCard(scoreCard);
		res.setIsLet(1);
		res.setIsFirstOut(playeRrank);
		BbtzPlayer passPlayer = null;
		for (BbtzPlayer pdkPlayer : seatMap.values()) {
			PlayCardRes.Builder copy = res.clone();
			if(pdkPlayer.getSeat() == getNowDisCardSeat()){
				int isLet = CardTypeTool.canPlayCompare(seatMap.get(nextSeat).getHandPais(), cards, cardType, this);
				copy.setIsLet(isLet);
				if(isLet == 0 && cardType == CardType.c517){
					passPlayer = pdkPlayer;
				}
			}
			pdkPlayer.writeSocket(copy.build());
		}
		addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards, ","));
        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("chuPai");
        sb.append("|").append(cardType.name());
        sb.append("|").append(JacksonUtil.writeValueAsString(cards));
        LogUtil.msgLog.info(sb.toString());
		if(checkRound()){//本轮结束
			sendRound();
			if (checkOver()) {
				state = table_state.over;
				changeTableStatus(BbtzConstants.TABLE_OVER, false);
			}
			return;
		}
		if(!getRank().isEmpty() && getRank().get(0) == getBankerSeat() && player.getSeat() != getBankerSeat()){
			sendRound();
			state = table_state.over;
			changeTableStatus(BbtzConstants.TABLE_OVER, false);
		}
		if(passPlayer != null && !isOver()){
			this.playCommand(passPlayer, null, null);
		}
	}
	/**
	 * 清理要不起的状态
	 */
	public void clearIsNotLet() {
		for (BbtzPlayer player : seatMap.values()) {
			player.setIsNoLet(0);
		}
	}

	/**
	 * 打牌
	 * 
	 * @param player
	 * @param cards
	 */
	public void playCommand(BbtzPlayer player, List<Integer> cards, CardType cardType) {
		synchronized (this) {
			if (player.getSeat() != getNowDisCardSeat()) {
				return;
			}

			changeDisCardRound(1);
			setLastActionTime(TimeUtil.currentTimeMillis());
			if (cards != null && !cards.isEmpty()) {
				// 出牌了
				disCards(player, cards, cardType);
			} else {
				notLet(player);
			}
			//addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards, ","));
			//LogUtil.msg("房间号="+getId()+",玩家="+player.getUserId()+"["+player.getName()+"]-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + cardType.name());
			if (isOver()) {
				calcOver();
			}
		}
	}

	/**
	 * 人数未满或者人员离线
	 * 
	 * @return 0 可以打牌 1人数未满 2人员离线
	 */
	public int isCanPlay() {
		if (seatMap.size() < getMaxPlayerCount()) {
			return 1;
		}
		for (BbtzPlayer player : seatMap.values()) {
			if (player.getIsEntryTable() != BbtzConstants.table_online) {
				// 通知其他人离线
				broadIsOnlineMsg(player, player.getIsEntryTable());
				return 2;
			}
		}
		return 0;
	}

	@Override
	public <T> T getPlayer(long id, Class<T> cl) {
		return (T) playerMap.get(id);
	}

	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	protected boolean joinPlayer1(Player player) {
		return false;
	}

	@Override
	protected void initNext1() {
		setNowDisCardIds(null);
		setWangSeat(0);
		setBankerSeat(0);
		setPlaneLength(0);
		getNowScoreCard().clear();
		getGroupScore().clear();
		getRank().clear();
		changeTableStatus(BbtzConstants.TABLE_READY, true);
	}

	@Override
	public int getPlayerCount() {
		return playerMap.size();
	}

	@Override
	protected void sendDealMsg() {
		sendDealMsg(0);
	}

	@Override
	protected void sendDealMsg(long userId) {
		int nextSeat = getNextDisCardSeat();
		setNowDisCardSeat(nextSeat);
		if(getMaxPlayerCount() == 2){
            setBankerSeat(nextSeat);
            ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_BANKER, nextSeat);
            broadMsg(build.build());
        }
		for (Player tablePlayer : getSeatMap().values()) {
			if (userId == tablePlayer.getUserId()) {
				continue;
			}
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(nextSeat);
			res.setGameType(getWanFa());// 1跑得快 2麻将
			res.setBanker(getWangSeat());//大王位置
			tablePlayer.writeSocket(res.build());
		}
	}

	@Override
	protected void robotDealAction() {
	}

	@Override
	protected void deal() {
		List<List<Integer>> list = CardTool.fapai(getMaxPlayerCount(), isDaiWang == 1, zp);
		int i = 0;
		for (BbtzPlayer player : playerMap.values()) {
			player.changeState(player_state.play);
			player.dealHandPais(list.get(i));
			if(getWangSeat()==0 && list.get(i).contains(517)){
				setWangSeat(player.getSeat());
			}
			i++;
		}
		if(getMaxPlayerCount() == 2){
            maiPaiList = list.get(2);
        }
//        this.totalBureau = 200000;
	}

	@Override
	public Map<Long, Player> getPlayerMap() {
		Object o = playerMap;
		return (Map<Long, Player>) o;
	}

	@Override
	public int getMaxPlayerCount() {
		return max_player_count;
	}


	public void setMaxPlayerCount(int maxPlayerCount) {
		this.max_player_count = maxPlayerCount;
		changeExtend();
	}

	public List<Integer> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setNowDisCardIds(List<Integer> nowDisCardIds) {
		if (nowDisCardIds == null) {
			this.nowDisCardIds.clear();

		} else {
			this.nowDisCardIds = nowDisCardIds;

		}
		dbParamMap.put("nowDisCardIds", JSON_TAG);
	}

	public boolean saveSimpleTable() throws Exception {
		TableInf info = new TableInf();
		info.setMasterId(masterId);
		info.setRoomId(0);
		info.setPlayType(playType);
		info.setTableId(id);
		info.setTotalBureau(totalBureau);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);

		changeExtend();

		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		return true;
	}

	public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
		return createTable(player, play, bureauCount, saveDb, params);
	}

	public boolean createTable(Player player, int play, int bureauCount,boolean saveDb, List<Integer> params) throws Exception {
		long id = getCreateTableId(player.getUserId(), play);
		if (saveDb) {
			TableInf info = new TableInf();
			info.setMasterId(player.getUserId());
			info.setRoomId(0);
			info.setPlayType(play);
			info.setTableId(id);
			info.setTotalBureau(bureauCount);
			info.setPlayBureau(1);
			info.setServerId(GameServerConfig.SERVER_ID);
			info.setCreateTime(new Date());
			info.setDaikaiTableId(daikaiTableId);
			info.setExtend(buildExtend());
			TableDao.getInstance().save(info);
			loadFromDB(info);
		}else{
			setPlayType(play);
			setDaikaiTableId(daikaiTableId);
			this.id = id;
			this.totalBureau = bureauCount;
			this.playBureau = 1;
		}
		setLastActionTime(TimeUtil.currentTimeMillis());
		int payType = StringUtil.getIntValue(params, 2, 1);//支付方式
		int kechui = StringUtil.getIntValue(params, 3, 0);
		int zheng510k = StringUtil.getIntValue(params, 4, 0);
		int zhuDou = StringUtil.getIntValue(params, 5, 0);
		int showCardNumber = StringUtil.getIntValue(params, 6, 0);
		this.max_player_count = StringUtil.getIntValue(params, 7, 0);// 比赛人数
		int is4_3 = StringUtil.getIntValue(params, 8, 0);//是否4带3
        isDaiWang = StringUtil.getIntValue(params, 9, 1);//是否带王
        if(getMaxPlayerCount() != 2){
            isDaiWang = 1 ;
        }else{
            //两人玩法没有锤
            kechui = 0;
            zhuDou = 0;
        }
        this.autoPlay = StringUtil.getIntValue(params,10,0) == 1;

		setPayType(payType);
		setKechui(kechui);
		setZheng510k(zheng510k);
		setZhuDou(zhuDou);
		setShowCardNumber(showCardNumber);
		setIs4_3(is4_3);

		if (isGoldRoom()) {
			try {
				GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
				if (goldRoom != null) {
					modeId = goldRoom.getModeId();
					max_player_count = goldRoom.getMaxCount();
					payType = 1;
				}
			} catch (Exception e) {
			}

			if (isMatchRoom()) {
				autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutBbtz", 15 * 1000);
			} else if (isGoldRoom()) {
				autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutBbtz", 30 * 1000);
			}
		}else if(autoPlay){
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutBbtzNormal", 20 * 1000);
            autoReadyTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoReadyTimeOutBbtzNormal", 20 * 1000);
        }
		changeExtend();

		return true;
	}

	@Override
	protected void initNowAction(String nowAction) {

	}

	@Override
	public void initExtend0(JsonWrapper wrapper) {
		this.max_player_count = wrapper.getInt(1, 3);
		if (max_player_count == 0) {
			max_player_count = 3;
		}
		this.tableStatus = wrapper.getInt(2, 0);
		this.wangSeat = wrapper.getInt(3, 0);
		this.bankerSeat = wrapper.getInt(4, 0);
		this.kechui = wrapper.getInt(5, 0);
		this.zheng510k = wrapper.getInt(6, 0);
		this.zhuDou = wrapper.getInt(7, 0);
		this.planeLength = wrapper.getInt(8, 0);
		this.nowScoreCard = loadListToInt(wrapper.getString(9));
		this.groupScore = loadMapToInt_Int(wrapper.getString(10));
		this.rank = loadListToInt(wrapper.getString(11));
		this.showCardNumber = wrapper.getInt(12, 0);
		this.is4_3 = wrapper.getInt(13, 0);
        if (payType== -1) {
            String isAAStr =  wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume"))?1:2;
            } else {
                payType=1;
            }
        }
        this.isDaiWang = wrapper.getInt(14, 1);
        String maiPaiStr = wrapper.getString(15);
        if (!StringUtils.isBlank(maiPaiStr)) {
            maiPaiList = StringUtil.explodeToIntList(maiPaiStr);
        }
	}
	private List<Integer> loadListToInt(String json){
		List<Integer> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            list.add(Integer.valueOf(val.toString()));
        }
        return list;
	}
	private Map<Integer, Integer> loadMapToInt_Int(String json){
		Map<Integer, Integer> map = new HashMap<>();
		if (json == null || json.isEmpty()){
			return map;
		}
		JSONObject jsonObject = JSONObject.parseObject(json);
		for (Object obj : jsonObject.keySet()) {
            map.put(Integer.valueOf(obj.toString()), jsonObject.getIntValue(obj.toString()));
        }
        return map;
	}

	@Override
	protected String buildNowAction() {
		return null;
	}

	@Override
	public void setConfig(int index, int val) {

	}

	/**
	 * 发送结算msg
	 * 
	 * @param over
	 *            是否已经结束
	 * @param winPlayer
	 *            赢的玩家
	 * @return
	 */
	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
		List<ClosingPlayerInfoRes> list = new ArrayList<>();

        //信用分计算
        if (isCreditTable()) {
            for (BbtzPlayer player : seatMap.values()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
            //计算信用负分
            calcNegativeCredit();
            int dyjCredit = 0;
            for (BbtzPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (BbtzPlayer player : seatMap.values()) {
                calcCommissionCredit(player, dyjCredit);
            }
        }
		for (BbtzPlayer player : seatMap.values()) {
			ClosingPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes();
			} else {
				build = player.bulidOneClosingPlayerInfoRes();
			}
			//添加本局所有牌和炸弹分
			//所有牌
			//添加手牌
			JSONArray jsonArray = new JSONArray();
			for(Integer v:player.getHandPais()){
				jsonArray.add(v);
			}
			build.addExt(jsonArray.toString());  //0
			//添加已出的牌
			JSONArray outJsonArray = new JSONArray();
			for(List<Integer> c:player.getOutPais()){
				if(c.isEmpty() || c.get(0)==0){
					continue;
				}
				JSONArray out = new JSONArray();
				for(Integer v:c){
					out.add(v);
				}
				outJsonArray.add(out);
			}
            build.addExt(outJsonArray.toString());//1
            
            JSONArray jsonScoreCard = new JSONArray();
            if(player.getScoreCard() != null && !player.getScoreCard().isEmpty()){
            	for(Integer card : player.getScoreCard()){
                	if(card != 0){
                		jsonScoreCard.add(card);
    				}
                }
            }
            //捡的分牌
            build.addExt(jsonScoreCard.toString());//2
            build.addExt(player.getBankerCount()+"");//3
            build.addExt(player.getFirstCount()+"");//4
            build.addExt(player.getChui()+"");//5
            build.addExt(player.getKaiqiang()+"");//6
            build.addExt(player.getRobBanker()+"");//7
            build.addExt(player.getDou()+"");//8
            if(isCreditTable()){
                build.addExt(player.getWinLoseCredit() + ""); //9
                build.addExt(player.getCommissionCredit() + "");//10
            }else{
                build.addExt(0+"");//9
                build.addExt(0+"");//10
            }
            // 2019-02-26更新
            build.setWinLoseCredit(player.getWinLoseCredit());
            build.setCommissionCredit(player.getCommissionCredit());

//			if (winPlayer != null) {
//				if (player.getSeat() == minPointSeat) {
//					build.setIsHu(1);
//					//player.changeCutCard(1);
//				} else {
//					build.setIsHu(0);
//					//player.changeCutCard(0);
//				}
//			}

//			if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
//				// 手上没有剩余的牌放第一位为赢家
//				list.add(0, build.build());
//			} else {
				list.add(build.build());
//			}


		}

		ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(getWanFa());
		res.addAllClosingPlayers(list);
		res.addAllExt(buildAccountsExt());
		res.addAllCutDtzCard(maiPaiList);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
		for (BbtzPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;
	}

	public List<String> buildAccountsExt() {
		List<String> ext = new ArrayList<>();
		ext.add(id + "");//0
		ext.add(masterId + "");//1
		ext.add(TimeUtil.formatTime(TimeUtil.now()));//2
		ext.add(playType + "");//3
		//设置当前第几局
		ext.add(playBureau+"");//4
		ext.add(totalBureau+"");//5
		ext.add(getWangSeat()+"");//6
		ext.add(getBankerSeat()+"");//7
		ext.add(getKechui()+"");//8
		ext.add(getZheng510k()+"");//9
		ext.add(getZhuDou()+"");//10
		ext.add(calhasWinGroup()+"");//11
		int bankerFen = getGroupScore().containsKey(1) ? getGroupScore().get(1) : 0;
		ext.add(bankerFen+"");//12
		int xianFen = getGroupScore().containsKey(2) ? getGroupScore().get(2) : 0;
		ext.add(xianFen+"");//13
		int bankerChaoFen = bankerChaoFen();
		ext.add(bankerChaoFen+"");//14
		ext.add(bankerChaoFen==0 ? "0" : -bankerChaoFen+"");//15
		ext.add((bankerFen+bankerChaoFen)+"");//16
		ext.add((xianFen-bankerChaoFen)+"");//17
		ext.add(isGroupRoom() ? "1" : "0");//18
		ext.add(isGroupRoom()?loadGroupId():"");//19
		ext.add(isDaiWang+"");//20

        ext.add(creditMode+""); //21
        ext.add(creditJoinLimit+"");//22
        ext.add(creditDissLimit+"");//23
        ext.add(creditDifen+"");//24
        ext.add(creditCommission+"");//25
        ext.add(creditCommissionMode1+"");//26
        ext.add(creditCommissionMode2+"");//27
		return ext;
	}
	public int calhasWinGroup(){
		int bankerScore = getGroupScore().containsKey(1) ? getGroupScore().get(1) : 0;
		int playerScore = getGroupScore().containsKey(2) ? getGroupScore().get(2) : 0;
		if(bankerScore > playerScore){
			return 1;
		}else if(bankerScore < playerScore){
			return 2;
		}else{
			if(getRank().isEmpty()){
				BbtzPlayer bankerPlayer = (BbtzPlayer)getSeatMap().get(getBankerSeat());
				if(bankerPlayer!=null && bankerPlayer.getKaiqiang() == 2){
					return 2;
				}else{
					return 0;
				}
			}else{
				return getRank().get(0)==getBankerSeat() ? 1 : 2;
			}
		}
	}

	@Override
	public void sendAccountsMsg() {
		ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
		saveLog(true,0l, builder.build());
	}

	@Override
	public Class<? extends Player> getPlayerClass() {
		return BbtzPlayer.class;
	}

	@Override
	public int getWanFa() {
		return SharedConstants.game_type_bbtz;
	}

	@Override
	public boolean isTest() {
		return BbtzConstants.isTest;
	}

	@Override
	public void checkReconnect(Player player) {
	}


	// 是否显示剩余牌的数量
	public boolean isShowCardNumber() {
		return 1 == getShowCardNumber();
	}

	@Override
	public void checkCompetitionPlay() {
		checkAutoPlay();
	}

	@Override
	public void checkAutoPlay() {
		synchronized (this) {
			if (isGoldRoom()){
				switch (tableStatus){
					case BbtzConstants.TABLE_CHUI:
						{
							if (System.currentTimeMillis()-lastActionTime>=autoTimeOut){
								for (Map.Entry<Integer,BbtzPlayer> kv : seatMap.entrySet()){
									CardTool.chui(this,kv.getValue(),2);
								}
							}
						}
						break;
					case BbtzConstants.TABLE_KAIQIANG:
						{
							if (System.currentTimeMillis()-lastActionTime>=autoTimeOut){
								for (Map.Entry<Integer,BbtzPlayer> kv : seatMap.entrySet()){
									CardTool.kaiQiang(this,kv.getValue(),1);
								}
							}
						}
						break;
					case BbtzConstants.TABLE_ROB_BANKER:
						{
							if (System.currentTimeMillis()-lastActionTime>=autoTimeOut){
								for (Map.Entry<Integer,BbtzPlayer> kv : seatMap.entrySet()){
									CardTool.robBanker(this,kv.getValue(),1);
								}
							}
						}
						break;
					case BbtzConstants.TABLE_DOU:
						{
							if (System.currentTimeMillis()-lastActionTime>=autoTimeOut){
								for (Map.Entry<Integer,BbtzPlayer> kv : seatMap.entrySet()){
									CardTool.dou(this,kv.getValue(),2);
								}
							}
						}
						break;
					case BbtzConstants.TABLE_PLAY:
						{
							BbtzPlayer player = seatMap.get(nowDisCardSeat);
							if (player!=null){
								if (checkAuto(player)){
//									List<Integer> oppo;

									if (disCardSeat == player.getSeat()){
//										oppo = null;
										List<Integer> list = player.getHandPais();
										int size = list.size();
										if (size>0){
											List<Integer> list1 = new ArrayList<>(4);
											list1.add(list.get(size-1));
											if (size>1&&list.get(size-1)%100==list.get(size-2)%100){
												list1.add(list.get(size-2));
												if (size>3&&list.get(size-1)%100==list.get(size-4)%100){
													list1.add(list.get(size-3));
													list1.add(list.get(size-4));
												}
											}
											playCommand(player, list1, null);
										}

										return;
									}else {
//										if (player.getSeat() == bankerSeat || disCardSeat == bankerSeat){
//											oppo = getNowDisCardIds();
//										}else{
//											playCommand(player, null, null);
//											return;
//										}
									}

//									List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
									playCommand(player, null, null);
								}
							}
						}
						break;
				}
				return;
			}else if (isAutoPlay()){
                if (getPlayBureau() > 1 && state == table_state.ready) {
                    // 自动准备
                    if (System.currentTimeMillis() - lastActionTime >= autoReadyTimeOut) {
                        for (BbtzPlayer player : seatMap.values()) {
                            autoReady(player);
                        }
                    }
                    return;
                }
                switch (tableStatus) {
                    case BbtzConstants.TABLE_CHUI: {
                        for (BbtzPlayer player : seatMap.values()) {
                            if(player.getChui() > 0){
                                continue;
                            }
                            if (checkAuto(player)) {
                                CardTool.chui(this, player, 2);
                            }
                        }
                    }
                    break;
                    case BbtzConstants.TABLE_KAIQIANG: {
                        for (BbtzPlayer player : seatMap.values()) {
                            if (player.getSeat() != getWangSeat() || player.getKaiqiang() > 0) {
                                continue;
                            }
                            if (checkAuto(player)) {
                                CardTool.kaiQiang(this, player, 2);
                            }
                        }
                    }
                    break;
                    case BbtzConstants.TABLE_ROB_BANKER: {
                        for (BbtzPlayer player : seatMap.values()) {
                            if(player.getSeat() == getWangSeat() || player.getRobBanker() > 0){
                                continue;
                            }
                            if (checkAuto(player)) {
                                CardTool.robBanker(this, player, 2);
                            }
                        }
                    }
                    break;
                    case BbtzConstants.TABLE_DOU: {
                        for (BbtzPlayer player : seatMap.values()) {
                            if (player.getDou() > 0) {
                                continue;
                            }
                            boolean needCheck = false;
                            if (getMaxPlayerCount() == 3) {
                                if (player.getSeat() == getBankerSeat()) {
                                    for (BbtzPlayer tmp : seatMap.values()) {
                                        if (tmp.getSeat() != getBankerSeat() && tmp.getDou() == 1) {
                                            // 有任意闲家位选择了
                                            needCheck = true;
                                            break;
                                        }
                                    }
                                } else {
                                    if (getZhuDou() == 1) {//助陡
                                        int zhaNum = CardTypeTool.getZaiDanNum(player.getHandPais());
                                        if (zhaNum >= 2) {
                                            needCheck = true;
                                        }
                                    } else {
                                        needCheck = true;
                                    }
                                }
                            } else {
                                needCheck = true;
                            }
                            if (needCheck && checkAuto(player)) {
                                CardTool.dou(this, player, 2);
                            }
                        }
                    }
                    break;
                    case BbtzConstants.TABLE_PLAY: {
                        BbtzPlayer player = seatMap.get(nowDisCardSeat);
                        if (player == null) {
                            return;
                        }
                        boolean auto = checkAuto(player);
                        if (!auto) {
                            return;
                        }

                        if (player.getHandPais().size() == 0) {
                            return;
                        }
                        Player nextPlayer = seatMap.get(calcNextSeat(player.getSeat()));
                        boolean nextDan = nextPlayer == null ? false : nextPlayer.getHandPais().size() == 1;
                        if (nowDisCardIds.size() == 0) {
                            // 没人要,该自己重新出牌
                            long start = System.currentTimeMillis();
                            List<Integer> pais = CardTypeTool.calcAutoChuPai(player.getHandPais(), nextDan, this);
                            long timeUse = System.currentTimeMillis() - start;
                            if (timeUse > 20) {
                                StringBuilder sb = new StringBuilder("Bbtz|calcAutoChuPai");
                                sb.append("|").append(getId());
                                sb.append("|").append(getPlayBureau());
                                sb.append("|").append(player.getUserId());
                                sb.append("|").append(player.getSeat());
                                sb.append("|").append(timeUse);
                                sb.append("|").append(player.getHandPais());
                                sb.append("|").append(nextDan);
                                LogUtil.monitorLog.info(sb.toString());
                            }
                            playCommand(player, pais, null);
                        } else {
                            List<Integer> oppoPaiList = getNowDisCardIds();
                            if (oppoPaiList == null) {
                                return;
                            }
                            List<Integer> selfPaiList = player.getHandPais();
                            if (selfPaiList.isEmpty()) {
                                return;
                            }
                            long start = System.currentTimeMillis();
                            List<Integer> jiePaiList = CardTypeTool.calcAutoJiePai(selfPaiList, oppoPaiList, nextDan, this);
                            long timeUse = System.currentTimeMillis() - start;
                            if (timeUse > 20) {
                                StringBuilder sb = new StringBuilder("Bbtz|calcAutoJiePai");
                                sb.append("|").append(getId());
                                sb.append("|").append(getPlayBureau());
                                sb.append("|").append(player.getUserId());
                                sb.append("|").append(player.getSeat());
                                sb.append("|").append(timeUse);
                                sb.append("|").append(player.getHandPais());
                                sb.append("|").append(oppoPaiList);
                                sb.append("|").append(nextDan);
                                LogUtil.monitorLog.info(sb.toString());
                            }
                            playCommand(player, jiePaiList, null);
                        }
                    }
                    break;
                }
			}else {
				return;
			}

			if (nowDisCardIds.size() > 0 && disCardSeat != nowDisCardSeat) {
				long time = TimeUtil.currentTimeMillis();
				if ((time - lastActionTime) > MAX_TIMEOUT) {
					BbtzPlayer player = seatMap.get(nowDisCardSeat);
					if (player != null && CardTypeTool.canPlayCompare(player.getHandPais(), getNowDisCardIds(), CardTypeTool.jugdeType(getNowDisCardIds(), this), this) == 0) {
						this.playCommand(player, null, null);
					}
				}
			}

		}
	}

	private boolean checkAuto(BbtzPlayer player){
		long timeout;

		if (isGoldRoom()) {
			if (isMatchRoom()) {
				if ("self".equals(player.getPf()) && player.getName().equals("test" + player.getUserId())) {
					timeout = 2000;
				} else if (disCardRound == 0) {
					timeout = autoTimeOut + 5000;
				} else {
					timeout = autoTimeOut;
				}
			} else {
				timeout = autoTimeOut;
			}
		} else if (player.isRobot()) {
			timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
		} else if(isAutoPlay()){
            timeout = autoTimeOut;
		}else{
		    return false;
        }
		long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimeBbtz", 2 * 1000);
        if(isAutoPlay()){
            autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimeBbtzNormal", 2 * 1000);
        }
		long now = TimeUtil.currentTimeMillis();
		boolean auto = player.isAutoPlay();
		if (!auto) {
			if (GameServerConfig.isAbroad()) {
				if (!player.isRobot() && now >= player.getNextAutoDisCardTime()) {
					auto = true;
					player.setAutoPlay(true, this);
				}
			} else {
//				if (now-player.getLastOperateTime()>=timeout){
				if (player.getLastCheckTime() > 0) {
					if (now - player.getLastCheckTime() >= timeout) {
						auto = true;
						player.setAutoPlay(true, this);
					}
				} else {
					player.setLastCheckTime(now);
				}
//				}
			}
		}

		if (auto || player.isRobot()) {
			boolean autoPlay = false;
			if (GameServerConfig.isAbroad()) {
				if (player.isRobot()) {
					autoPlayTime = MathUtil.mt_rand(2, 6) * 1000;
				} else {
					autoPlay = true;
				}
			}
			if (player.getAutoPlayTime() == 0L && !autoPlay) {
				player.setAutoPlayTime(now);
			} else if (autoPlay || (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime)) {
				player.setAutoPlayTime(0L);
				return true;
			}
		}
		return false;
	}

	public int getIsFirstRoundDisThree() {
		return isFirstRoundDisThree;
	}

	public void setIsFirstRoundDisThree(int isFirstRoundDisThree) {
		this.isFirstRoundDisThree = isFirstRoundDisThree;
		changeExtend();
	}

	public int getShowCardNumber() {
		return showCardNumber;
	}

	public void setShowCardNumber(int showCardNumber) {
		this.showCardNumber = showCardNumber;
		changeExtend();
	}


	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		createTable(player, play, bureauCount,true, params);
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

	}
	public boolean isCanJoin0(Player player) {
		int needCard = 0;
        if(consumeCards()){
	          if(payType == 1) {
	        	  //AA制
	              needCard = PayConfigUtil.get(playType,getTotalBureau(),getMaxPlayerCount(),0,null);
	          }
        }
        // 如果玩家的钻石小于玩一局需要的钻石，则返回
        if (checkPay&&(needCard<0||needCard>0&&player.getFreeCards() + player.getCards() < needCard) && !player.isRobot()) {
             player.writeErrMsg(LangMsg.code_diamond_err);
             return false;
        }
        return true;
	}
	
	@Override
    public void startNext(){
	}
	/**
	 * 修改桌状态
	 * @param status
	 */
	public void changeTableStatus(int status, boolean push, Object... params){
		setTableStatus(status);
		if(push){
			ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_STATE, getTableStatus(), params);
	        broadMsg(build.build());
		}
	}
	/**
     * 检查所有玩家都是否准好了
     */
	@Override
    public synchronized void ready(){
    	if(isAllReady() && kechui  ==1 && playedBureau==0){
    		changeTableStatus(BbtzConstants.TABLE_CHUI, true);
    	}
    }
	/**
     * 重写发牌等待锤完成之后开始
     */
    public synchronized void checkDeal(long userId){
    	if (isAllReady()) {
    		checkDeal1();
        } else {
            robotDealAction();
        }
    }
    public void checkDeal1(){
    	if(checkChuiOver()){
    		// 发牌
            fapai();
            setLastActionTime(TimeUtil.currentTimeMillis());
            for (int i = 1; i <= getMaxPlayerCount(); i++) {
                Player player = getSeatMap().get(i);
                addPlayLog(StringUtil.implode(player.getHandPais(), ","));
            }
            // 发牌msg
            sendDealMsg(0);
            if(getMaxPlayerCount() == 3) {
                changeTableStatus(BbtzConstants.TABLE_KAIQIANG, true);//进入开枪状态
            }else{
                changeTableStatus(BbtzConstants.TABLE_DOU, true);//进入陡
            }
            robotDealAction();
            updateGroupTableDealCount();
    	}
    }
    /**
     * 是否全部锤
     */
    public boolean checkChuiOver(){
    	if(this.getKechui()==1 && playedBureau==0){
    		if(seatMap.size() == getMaxPlayerCount()){
    			for (BbtzPlayer player : seatMap.values()){
    				if(player.getChui()<=0){
    					return false;
    				}
    			}
    			return true;
    		}
    	}else{
    		return true;
    	}
    	return false;
    }
    /**
     * 推送陡
     * @param seat 庄家位置
     */
    public void pushDou(int seat){
    	changeTableStatus(BbtzConstants.TABLE_DOU, false);
    	int mark = 0;
    	for(BbtzPlayer player : seatMap.values()){
    		if(player.getSeat()==seat){
    			player.writeComMessage(WebSocketMsgType.RES_BBTZ_STATE, getTableStatus(), seat, 0);//0 庄 1可陡 2不可陡
    			continue;
    		}
    		if(getZhuDou()==1){
    			int zaidanNum = CardTypeTool.getZaiDanNum(player.getHandPais());
        		if(zaidanNum >= 2){
        			player.writeComMessage(WebSocketMsgType.RES_BBTZ_STATE, getTableStatus(), seat, 1);
        		}else{
        			mark++;
        			player.setDou(2);
        			player.writeComMessage(WebSocketMsgType.RES_BBTZ_STATE, getTableStatus(), seat, 2);
        		}
    		}else{
    			player.writeComMessage(WebSocketMsgType.RES_BBTZ_STATE, getTableStatus(), seat, 1);
    		}
    	}
    	if(mark == (getMaxPlayerCount()-1)){//直接开始打牌
    		changeTableStatus(BbtzConstants.TABLE_PLAY, true, getNowDisCardSeat());
    	}
    }
    /**
     * 是否抢庄完成
     */
    public void checkRobBankerOver(){
    	List<Integer> robBankerNum = new ArrayList<Integer>();
    	int noRobBankerNum = 0;
		for (BbtzPlayer player : seatMap.values()){
			if(player.getSeat() == getWangSeat()){
				continue;
			}
			if(player.getRobBanker() == 1){
				robBankerNum.add(player.getSeat());
			}else if(player.getRobBanker() == 2){
				noRobBankerNum++;
			}
		}
		if((robBankerNum.size() + noRobBankerNum) == (getMaxPlayerCount()-1)){//结束抢庄
			if(noRobBankerNum == (getMaxPlayerCount()-1)){ //都不抢
				//投降输一半 算锤
				setBankerSeat(getWangSeat());
				calcOver(); 
			}else{
				int seat;
				if(robBankerNum.size() == 1){
					seat = robBankerNum.get(0);
					ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_BANKER, seat);
			        broadMsg(build.build());
				}else{
					seat = MathUtil.draw(robBankerNum);
					ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_BANKER, seat, JacksonUtil.writeValueAsString(robBankerNum));
			        broadMsg(build.build());
				}
				setBankerSeat(seat);
				BbtzPlayer player = (BbtzPlayer)getSeatMap().get(seat);
				player.setBankerCount(player.getBankerCount()+1);
				pushDou(seat);
			} 
		}
    }
    /**
     * 是否陡完成
     */
    public boolean checkDouOver(){
    	int douNum = 0,noDouNum = 0; 
    	for (BbtzPlayer player : seatMap.values()){
    		if(player.getDou() == 1){
    			douNum++;
    		}else if(player.getDou() == 2){
    			noDouNum++;
    		}
    	}
        BbtzPlayer player = (BbtzPlayer)getSeatMap().get(getBankerSeat());
    	if((douNum+noDouNum) == getMaxPlayerCount()){
    		return true;
    	}else if(getMaxPlayerCount() == 3 && player.getDou() == 0 && (noDouNum + 1) == getMaxPlayerCount()){
    	    //3人玩法，闲家没有选择陡，庄家默认为不陡
    		player.setDou(2);
    		return true;
    	}else{
    		return false;
    	}
    }

	@Override
	public void calcDataStatistics2() {
		//俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
		if(isGroupRoom()){
			String groupId=loadGroupId();
			int maxPoint=0;
			int minPoint=0;
			Long dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			//Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue

			calcDataStatistics3(groupId);

			for (BbtzPlayer player:playerMap.values()){
				//总小局数
				DataStatistics dataStatistics1=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"xjsCount",playedBureau);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1,3);

				//总大局数
				DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"djsCount",1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,3);
				//总积分
				DataStatistics dataStatistics6=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"zjfCount",player.loadScore());
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6,3);

				if (player.loadScore()>0){
					if (player.loadScore()>maxPoint){
						maxPoint=player.loadScore();
					}
					//单大局赢最多
					DataStatistics dataStatistics2=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"winMaxScore",player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2,4);
				}else if (player.loadScore()<0){
					if (player.loadScore()<minPoint){
						minPoint=player.loadScore();
					}
					//单大局输最多
					DataStatistics dataStatistics3=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"loseMaxScore",player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3,5);
				}
			}

			for (BbtzPlayer player:playerMap.values()){
				if (maxPoint>0&&maxPoint==player.loadScore()){
					//单大局大赢家
					DataStatistics dataStatistics4=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dyjCount",1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4,1);
				}else if (minPoint<0&&minPoint==player.loadScore()){
					//单大局大负豪
					DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dfhCount",1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,2);
				}
			}
		}
	}

    @Override
    public boolean canQuit(Player player) {
        if (tableStatus == BbtzConstants.TABLE_CHUI && getPlayerCount() == max_player_count){
            return false;
        }else{
            return super.canQuit(player);
        }
    }

    public String getGameName(){
        return "半边天炸";
    }

	@Override
	public boolean canDissTable(Player player) {
		return (tableStatus == BbtzConstants.TABLE_CHUI && getPlayerCount() == max_player_count) || super.canDissTable(player);
	}

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 10 && StringUtil.getIntValue(params, 10, 0) == 1;
    }

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_bbtz);

	public static void loadWanfaTables(Class<? extends BaseTable> cls){
		for (Integer integer:wanfaList){
			TableManager.wanfaTableTypesPut(integer,cls);
		}
	}

    public CardType getRoundCardType() {
        return roundCardType;
    }

    public void setRoundCardType(CardType roundCardType) {
        this.roundCardType = roundCardType;
        changeExtend();
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public void setAutoTimeOut(int autoTimeOut) {
        this.autoTimeOut = autoTimeOut;
    }
}
