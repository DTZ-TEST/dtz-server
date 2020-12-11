package com.sy599.game.qipai.bbtz.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.bbtz.command.BbtzCommandProcessor;
import com.sy599.game.qipai.bbtz.constant.BbtzConstants;
import com.sy599.game.qipai.bbtz.constant.EnumHelper;
import com.sy599.game.qipai.bbtz.tool.CardTypeTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class BbtzPlayer extends Player {
	// 座位id
	private volatile int seat;
	// 状态
	private volatile player_state state;// 1进入 2已准备 3正在玩 4已结束
	private volatile int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int winCount;
	private int lostCount;
	private int point;
	private int playPoint;
	private int isNoLet;
	
	private volatile int chui;//1 锤 2不锤
	private volatile int kaiqiang;//1 开枪 2投降
	private volatile int robBanker;//1 抢庄 2不抢
	private volatile int dou;//1 陡 2不陡
	
	private volatile int zhudou;//1 点了助陡 0没点
	
	private List<Integer> scoreCard = new ArrayList<>();//本局得的分牌
	private int bankerCount;//庄家次数
	private int firstCount;//上游次数

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

	public int getZhudou() {
		return zhudou;
	}

	public void setZhudou(int zhudou) {
		this.zhudou = zhudou;
	}

	public int getBankerCount() {
		return bankerCount;
	}

	public void setBankerCount(int bankerCount) {
		this.bankerCount = bankerCount;
		changeTableInfo();
	}

	public int getFirstCount() {
		return firstCount;
	}

	public void setFirstCount(int firstCount) {
		this.firstCount = firstCount;
		changeTableInfo();
	}

	public List<Integer> getScoreCard() {
		return scoreCard;
	}

	public void setScoreCard(List<Integer> scoreCard) {
		this.scoreCard = scoreCard;
		changeTableInfo();
	}
	
	public void addScoreCard(List<Integer> scoreCard) {
		getScoreCard().addAll(scoreCard);
		changeTableInfo();
	}

	public int getChui() {
		return chui;
	}

	public void setChui(int chui) {
		this.chui = chui;
		changeTableInfo();
	}

	public int getKaiqiang() {
		return kaiqiang;
	}

	public void setKaiqiang(int kaiqiang) {
		this.kaiqiang = kaiqiang;
		changeTableInfo();
	}

	public int getRobBanker() {
		return robBanker;
	}

	public void setRobBanker(int robBanker) {
		this.robBanker = robBanker;
		changeTableInfo();
	}

	public int getDou() {
		return dou;
	}

	public void setDou(int dou) {
		this.dou = dou;
		changeTableInfo();
	}

	public BbtzPlayer() {
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

	public void dealHandPais(List<Integer> pais) {
		this.handPais = pais;

		Collections.sort(pais, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2%100 - o1%100;
			}
		});

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
		handPais.removeAll(cards);
		outPais.add(cards);
		getPlayingTable().changeCards(seat);
	}

	public String toInfoStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(getTotalBoom()).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(getTotalPoint()).append(",");
		sb.append(isNoLet).append(",");
		sb.append(playPoint).append(",");
		sb.append(chui).append(",");
		sb.append(kaiqiang).append(",");
		sb.append(robBanker).append(",");
		sb.append(dou).append(",");
		sb.append(StringUtil.implode(scoreCard,"#")).append(",");
		sb.append(bankerCount).append(",");
		sb.append(firstCount).append(",");
		sb.append(zhudou).append(",");
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
			this.state = EnumHelper.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
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
			this.chui = StringUtil.getIntValue(values, i++);
			this.kaiqiang = StringUtil.getIntValue(values, i++);
			this.robBanker = StringUtil.getIntValue(values, i++);
			this.dou = StringUtil.getIntValue(values, i++);
			String scoreCard = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(scoreCard)){
				this.scoreCard = StringUtil.explodeToIntList(scoreCard, "#");
			}
			this.bankerCount = StringUtil.getIntValue(values, i++);
			this.firstCount = StringUtil.getIntValue(values, i++);
			this.zhudou = StringUtil.getIntValue(values, i++);
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
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(this.userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);

		} else {
			res.setIp("");
		}
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		res.setPoint(getTotalPoint());
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());

		} else {
			res.setIcon("");
		}

		if (state == player_state.ready || state == player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(BbtzConstants.state_player_ready);
		} else {
			res.setStatus(0);
		}
		BbtzTable table = getPlayingTable(BbtzTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}

		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();

			int nowDisSeat = table.getNowDisCardSeat();
			if (recUserId == this.userId && table.getDisCardSeat() != seat && !table.getNowDisCardIds().isEmpty() && nowDisSeat == seat) {
				// 自己重连,现在轮到自己出牌,去打别人出的牌
				recover.add(CardTypeTool.canPlayCompare(handPais, table.getNowDisCardIds(), CardTypeTool.jugdeType(table.getNowDisCardIds(), table), table));
			} else {
				if (isNoLet == 1) {
					// 是否要不起
					recover.add(0);
				} else {
					// 1要的起
					recover.add(1);
				}
			}

			if (handPais.size() == 1) {
				recover.add(1);
			} else {
				recover.add(0);
			}
			recover.add(isEntryTable);
			res.addAllRecover(recover);
		}
		List<Integer> extList = new ArrayList<>();
		int cardNumber = this.getHandPais().size();
		if (0 == cardNumber) {
			if (table != null && table.getTableStatus() != BbtzConstants.TABLE_PLAY) {
				cardNumber = 17;
			}
		}
		extList.add(cardNumber); //0
		if(table == null || table.getMasterId() == userId){
			extList.add(1);//1
		}else{
			extList.add(0);//1
		}
		extList.add(getChui());//2
		extList.add(getKaiqiang());//3
		extList.add(getRobBanker());//4
		extList.add(getDou());//5
		extList.add(table.findPlayerMingCi(getSeat()));//6
		extList.add(getZhudou());//7
		extList.add(isAutoPlay()?1:0);//8
        if (getLastCheckTime() > 0) {
            if (isAutoPlay()) {
                extList.add(0);
            } else {
                extList.add(table.getAutoTimeOut()/1000 - ((int) (System.currentTimeMillis() - getLastCheckTime()) / 1000));
            }
        } else {
            extList.add(0);
        }
		res.addAllExt(extList);
		if(getScoreCard() != null && !getScoreCard().isEmpty()){
			res.addAllScoreCard(getScoreCard());
		}

        if(table.isCreditTable()) {
            GroupUser gu = getGroupUser();
            String groupId = table.loadGroupId();
            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
            }
            res.setCredit(gu != null ? gu.getCredit() : 0);
        }
		return buildPlayInTableInfo1(res);
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

	public void calcLost(int lostCount, int point) {
		this.lostCount += lostCount;
		changePlayPoint(point);
		changePoint(this.playPoint);
	}

	public void calcWin(int winCount, int point) {
		this.winCount += winCount;
		changePlayPoint(point);
		changePoint(this.playPoint);

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
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
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
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		// getPlayingTable().changePlayers();
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		setChui(0);
		setKaiqiang(0);
		setRobBanker(0);
		setDou(0);
		setBankerCount(0);
		setFirstCount(0);
		setZhudou(0);
		if(getScoreCard() != null && !getScoreCard().isEmpty()){
			getScoreCard().clear();
		}
		autoPlay = false;
		setLastCheckTime(0);
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
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(getTotalPoint());
		res.setSeat(seat);
		//res.setBoom(boomCount);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

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
		res.setTotalPoint(getTotalPoint());
		res.addAllCards(handPais);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
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
		setIsNoLet(0);
		//setChui(0);锤不用初始化
		setKaiqiang(0);
		setRobBanker(0);
		setDou(0);
		getScoreCard().clear();
		setZhudou(0);
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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_bbtz);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, BbtzCommandProcessor.getInstance());
		}
	}

}
