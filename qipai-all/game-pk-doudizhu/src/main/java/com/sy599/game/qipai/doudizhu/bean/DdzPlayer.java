package com.sy599.game.qipai.doudizhu.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.doudizhu.bean.DdzTable.DdzPhase;
import com.sy599.game.qipai.doudizhu.command.DdzCommandProcessor;
import com.sy599.game.qipai.doudizhu.tool.CardTypeTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DdzPlayer extends Player {
	// private long tableId;
	// private long roomId;
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int boomCount;
	private int winCount;
	private int lostCount;
	private int point;
	private int playPoint;
	private int isNoLet;
	private int cutCard;// 是否需要切牌
	private int landLordCount;//该牌局当地主的次数

	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;

	private volatile long autoPlayTime = 0;//自动操作时间

	public long getNextAutoDisCardTime() {
		return nextAutoDisCardTime;
	}

	public void setNextAutoDisCardTime(long nextAutoDisCardTime) {
		this.nextAutoDisCardTime = nextAutoDisCardTime;
	}

	public long getAutoPlayTime() {
		return autoPlayTime;
	}

	public void setAutoPlayTime(long autoPlayTime) {
		this.autoPlayTime = autoPlayTime;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public boolean isAutoPlay() {
		return autoPlay;
	}

	public void setAutoPlay(boolean autoPlay,BaseTable table) {
		if (this.autoPlay != autoPlay && !isRobot()){
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat,autoPlay?1:0, (int)userId);
			GeneratedMessage msg = res.build();
			for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
				Player player=kv.getValue();
				if (player.getIsOnline() == 0) {
					continue;
				}
				player.writeSocket(msg);
			}
		}

		this.autoPlay = autoPlay;
	}

	public long getLastOperateTime() {
		return lastOperateTime;
	}

	public void setLastOperateTime(long lastOperateTime) {
		this.lastCheckTime = 0;
		this.lastOperateTime = lastOperateTime;
		this.autoPlayTime = 0;
	}

	public DdzPlayer() {
		// this.tableId = tableId;
		// this.userId = userId;
		// this.roomId = roomId;
		handPais = new ArrayList<Integer>();
		outPais = new ArrayList<List<Integer>>();
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(String hand, String out) {
		if (!StringUtils.isBlank(hand)) {
			this.handPais = StringUtil.explodeToIntList(hand);

		}
		if (!StringUtils.isBlank(out)) {
			this.outPais = StringUtil.explodeToLists(out);

		}
	}

	public void initPais(List<Integer> hand, List<List<Integer>> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void addHandPais(List<Integer> pais) {
		this.handPais.addAll(pais);
		CardTypeTool.setOrder(this.handPais);
		getPlayingTable().changeCards(seat);
	}
	
	public void dealHandPais(List<Integer> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public List<Integer> getHandPais() {
		return handPais;
	}

	public List<List<Integer>> getOutPais() {
		return outPais;
	}

	/**
	 * 是否出过牌用于结算
	 * 
	 * @return
	 */
	public boolean isOutCards() {
		boolean isOut = false;
		for (List<Integer> list : outPais) {
			if (!list.isEmpty() && !list.contains(0)) {
				isOut = true;
			}

		}
		return isOut;
	}

	public void addOutPais(List<Integer> cards) {
		int len = 0;
		for(int card : cards) {
			if(card / 100 == 6) {
				++len;
			}
		}
		
		if(len == 0) {
			handPais.removeAll(cards);
		} else {
			// 去掉相同数量的癞子牌，排序过后癞子牌都放在最前面
			for(int i = 0; i < len; ++i) {
				handPais.remove(0);
			}
		}
 		outPais.add(cards);
		getPlayingTable().changeCards(seat);
	}

	public String toInfoStr() {
		// private int boomCount;
		// private int winCount;
		// private int lostCount;
		// private int point;
		StringBuffer sb = new StringBuffer();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(boomCount).append(",");
		sb.append(getTotalBoom()).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(loadScore()).append(",");
		sb.append(isNoLet).append(",");
		sb.append(playPoint).append(",");
		sb.append(cutCard).append(",");
        sb.append(landLordCount).append(",");
		return sb.toString();
	}

	@Override
	public void initPlayInfo(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(",");
			long duserId = StringUtil.getLongValue(values, i++);
			if (duserId != getUserId()) {
				return;
			}
			this.seat = StringUtil.getIntValue(values, i++);
			int stateVal = StringUtil.getIntValue(values, i++);
			this.state = SharedConstants.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
			this.boomCount = StringUtil.getIntValue(values, i++);
			setTotalBoom(StringUtil.getIntValue(values, i++));
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.isNoLet = StringUtil.getIntValue(values, i++);
			this.playPoint = StringUtil.getIntValue(values, i++);
			if (playPoint == 0 && this.point != 0) {
				playPoint = point;
			}
			this.cutCard = StringUtil.getIntValue(values, i++);
            this.landLordCount = StringUtil.getIntValue(values, i++);
		}
	}

	public player_state getState() {
		return state;
	}

	public void changeState(player_state state) {
		this.state = state;
		changeTableInfo();
	}

	public int getIsEntryTable() {
		return isEntryTable;
	}

	public void setIsEntryTable(int isEntryTable) {
		this.isEntryTable = isEntryTable;
		changeTableInfo();
	}

	public PlayerInTableRes.Builder buildPlayInTableInfo() {
		return buildPlayInTableInfo(0, false);
	}

	/**
	 * @param isrecover
	 *            是否重连
	 * @return
	 */
	public PlayerInTableRes.Builder buildPlayInTableInfo(long recUserId, boolean isrecover) {
		DdzTable table = getPlayingTable(DdzTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(this.userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);

		} else {
			res.setIp("");
		}
		if (table.getAnonymous()== 1) {
			res.setName("匿名");
			res.setIcon("");
		} else {
			res.setName(name);
			if (!StringUtils.isBlank(getHeadimgurl())) {
				res.setIcon(getHeadimgurl());
			} else {
				res.setIcon("");
			}
		}

		res.setSeat(seat);
		res.setSex(sex);
		res.setPoint(loadScore());

		if (state == player_state.ready || state == player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(SharedConstants.state_player_ready);

		} else {
			res.setStatus(0);

		}
		
		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();

			int nowDisSeat = table.getNextDisCardSeat();
			if (recUserId == this.userId  && nowDisSeat == seat) {
				recover.add(0);
			} else {
				// 非自己出牌 isNoLet 1显示要不起 , 0不显示
				recover.add(isNoLet);
			}

			if (handPais.size() == 1) {
				recover.add(1);
			} else {
				recover.add(0);
			}
			recover.add(isEntryTable);
			
			//控制出牌，不要，提示按钮
			if(table.getPhase().getId() >= DdzPhase.play.getId() && nowDisSeat == seat) {
				if(table.getDisCardSeat() != seat && !table.getNowDisCardIds().isEmpty()) {
					// 自己重连,现在轮到自己出牌,去打别人出的牌
					if (CardTypeTool.ifCanPlay(handPais, table.getNowDisCardIds(), table.getMagnaCardValue())) {
						// 可以出牌，可以不出
						recover.add(3);
					} else {
						// 打不起
						recover.add(2);
					}
				} else {
					// 必须出牌
					recover.add(1);
				}
			} else {
				//  非出牌位置
				recover.add(0);
			}
			
			res.addAllRecover(recover);
		}
		List<Integer> extList = new ArrayList<>();
		extList.add(cutCard);
		int cardNumber = this.getHandPais().size();
		if (0 == cardNumber) {
			if (table != null) {
//				cardNumber = table.getPlayType();
				cardNumber = 17;
			}
		}
		extList.add(cardNumber);

		// 是否叫过地主
		if(table.getPhase() == DdzPhase.robBanker) {
			if(!table.getRobLandLordMap().isEmpty() && table.getRobLandLordMap().containsKey(seat)) {
				extList.add(table.getRobLandLordMap().get(seat));
			} else {
				extList.add(-1);
			}
		} else {
			extList.add(-1);
		}
		
		
		// 是否地主
		if (table.getPhase().getId() >= DdzPhase.play.getId()) {
			extList.add(table.getLandLord() == seat? 1 : 0);
		} else {
			extList.add(0);
		}
		// 三张底牌
		if(table.getPhase().getId() >= DdzPhase.play.getId()) {
			res.addAllAngangIds(table.getUnderCards());
		}
		
		// 出牌阶段
//		if(!table.getNowDisCardIds().isEmpty()) {
//			res.addAllOutCardIds(table.getNowDisCardIds());
//		}
		//4
		extList.add(table.isGoldRoom()?(int)loadAllGolds():0);//4
		extList.add(isAutoPlay()&&!isRobot()?1:0);//5
		int tuoGuanCountDown = table.getAutoTimeOut();
		if (nextAutoDisCardTime > 0) {
			tuoGuanCountDown = (int) (nextAutoDisCardTime - TimeUtil.currentTimeMillis());
			if (tuoGuanCountDown < 0) {
				tuoGuanCountDown = 0;
			}
		}
		extList.add(tuoGuanCountDown);//6 托管倒计时
		
		res.addAllExt(extList);
		return buildPlayInTableInfo1(res);
	}

	public int getBoomCount() {
		return boomCount;
	}

	public void setBoomCount(int boomCount) {
		this.boomCount = boomCount;
	}

	public void changeBoomCount(int boomCount) {
		this.boomCount += boomCount;
		myExtend.setPdkFengshen(FirstmythConstants.firstmyth_index3, boomCount);
		changeTotalBoom(boomCount);
		changeTableInfo();
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLostCount() {
		return lostCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public void calcLost(DdzTable table,int lostCount, int point) {
		this.lostCount += lostCount;
		changePlayPoint(point);
		changePoint(this.playPoint);

		if (table!=null&&table.isGoldRoom()&&(!table.isMatchRoom())){
			changeGoldPlayCount();
			changeGoldLoseCount();
			if (isRobot()){

			}else
				GoldDao.getInstance().updateGoldUserCount(userId,0,1,0,1);

		}
	}

	public void calcWin(DdzTable table,int winCount, int point) {
		this.winCount += winCount;
		changePlayPoint(point);
		changePoint(this.playPoint);

		if (table!=null&&table.isGoldRoom()&&(!table.isMatchRoom())){
			changeGoldPlayCount();
			changeGoldWinCount();
			if (isRobot()){

			}else
				GoldDao.getInstance().updateGoldUserCount(userId,1,0,0,1);
		}
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getPlayPoint() {
		return playPoint;
	}

	public void setPlayPoint(int playPoint) {
		this.playPoint = playPoint;
	}

	public void changePlayPoint(int playPoint) {
		this.playPoint += playPoint;
		changeTableInfo();
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		myExtend.setPdkFengshen(FirstmythConstants.firstmyth_index0, point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}

	public void changeCutCard(int cutCard) {
		this.cutCard = cutCard;
		changeTableInfo();
	}

	public void clearTableInfo() {
		BaseTable table = getPlayingTable();
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setIsEntryTable(0);
		changeIsLeave(0);
		getHandPais().clear();
		getOutPais().clear();
		setMaxPoint(0);
		setPlayPoint(0);
		changeState(null);
		setBoomCount(0);
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		setCutCard(0);
		// getPlayingTable().changePlayers();
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		setLandLordCount(0);
		saveBaseInfo();
	}

	/**
	 * 单局详情
	 * 
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		DdzTable ddzTable = getPlayingTable(DdzTable.class);
		if (ddzTable!=null && ddzTable.getAnonymous() == 1 && ddzTable.getPlayBureau() < ddzTable.getTotalBureau()) {
			res.setName("匿名");
			res.setIcon("");
		} else {
			res.setName(name);
			if (!StringUtils.isBlank(getHeadimgurl())) {
				res.setIcon(getHeadimgurl());
			} else {
				res.setIcon("");
			}
		}
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(table.isMatchRoom()? JjsUtil.loadMatch(table.getMatchId()).loadUserScore(userId):(table.isGoldRoom()?(int)loadAllGolds():loadScore()));
		res.setSeat(seat);
		res.setBoom(boomCount);
		res.setSex(sex);
		return res;
	}

	/**
	 * 总局详情
	 * 
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.setMaxPoint(getMaxPoint());
		res.setTotalBoom(getTotalBoom());
		res.setWinCount(getWinCount());
		res.setLostCount(getLostCount());
		res.setTotalPoint(table.isMatchRoom()?JjsUtil.loadMatch(table.getMatchId()).loadUserScore(userId):(table.isGoldRoom()?(int)loadAllGolds():loadScore()));
		res.addAllCards(handPais);
		res.setBoom(boomCount);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);

        res.addExt(""+landLordCount);
		return res;
	}

	public int getIsNoLet() {
		return isNoLet;
	}

	/**
	 * 用于断线重连---
	 * 
	 * @param isNoLet
	 *            1要不起0要的起
	 */
	public void setIsNoLet(int isNoLet) {
		this.isNoLet = isNoLet;
		changeTableInfo();
	}

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		getHandPais().clear();
		getOutPais().clear();
		setPoint(0);
		setPlayPoint(0);
		setBoomCount(0);
		setIsNoLet(0);
		changeState(player_state.entry);
		changeSeat();
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}

	/**
	 * 开始下一局准备
	 * 
	 * @return
	 */
//	public boolean isStartNextReady() {
//		// 前台版本号在213以下的直接准备(因为没有切牌功能)
//		if (myExtend.getVersions() <= 213) {
//			return true;
//		}
//		return getCutCard() == 0;
//	}

	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}

    public int getLandLordCount() {
        return landLordCount;
    }

    public void setLandLordCount(int landLordCount) {this.landLordCount = landLordCount;}

    public void changeLandLordCount() {
        this.landLordCount += 1;
        changeExtend();
    }

	public static final List<Integer> wanfaList = Arrays.asList(91,92,93);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, DdzCommandProcessor.getInstance());
		}
	}
}
