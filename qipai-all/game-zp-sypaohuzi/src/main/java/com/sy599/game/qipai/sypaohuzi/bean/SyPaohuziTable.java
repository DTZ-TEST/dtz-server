package com.sy599.game.qipai.sypaohuzi.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.sypaohuzi.constant.PaohuziConstant;
import com.sy599.game.qipai.sypaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.sypaohuzi.rule.PaohuziIndex;
import com.sy599.game.qipai.sypaohuzi.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.sypaohuzi.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.sypaohuzi.rule.RobotAI;
import com.sy599.game.qipai.sypaohuzi.tool.PaohuziHuLack;
import com.sy599.game.qipai.sypaohuzi.tool.PaohuziResTool;
import com.sy599.game.qipai.sypaohuzi.tool.PaohuziTool;
import com.sy599.game.staticdata.KeyValuePair;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SyPaohuziTable extends BaseTable {
    /*** 玩家map */
    private Map<Long, SyPaohuziPlayer> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, SyPaohuziPlayer> seatMap = new ConcurrentHashMap<>();
    /**
     * 开局所有底牌
     **/
    private volatile List<Integer> startLeftCards = new ArrayList<>();
    /**
     * 当前桌面底牌
     **/
    private volatile List<PaohzCard> leftCards = new ArrayList<>();
    /*** 摸牌flag */
    private volatile int moFlag;
    /*** 应该要打牌的flag */
    private volatile int toPlayCardFlag;
    private volatile PaohuziCheckCardBean autoDisBean;
    private volatile int moSeat;
    private volatile PaohzCard zaiCard;
    private volatile PaohzCard beRemoveCard;
    private volatile int maxPlayerCount = 3;
    private volatile List<Integer> huConfirmList = new ArrayList<>();
    /*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> moSeatPair;
    /*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> checkMoMark;
    private volatile int sendPaoSeat;
    private volatile boolean firstCard = true;
    private volatile int shuXingSeat = 0;
    private volatile int ceiling =0;
    /**
     * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    private volatile List<PaohzCard> nowDisCardIds = new ArrayList<>();
    //红黑点
    private volatile int isRedBlack;
    //可连庄
    private volatile int isLianBanker;
    //息转屯比例 3三息一屯 5五息一屯
    private volatile int xiTotun;

    private volatile int catCardCount = 0;//抽掉的牌数量
    
    /**
	 * 托管时间
	 */
	private volatile int autoTimeOut = Integer.MAX_VALUE;
	private volatile int autoTimeOut2 = Integer.MAX_VALUE;

    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;

    private volatile int timeNum = 0;
    //栽提胡
    private int zaiTiHu=0;

    public boolean getFirstCard(){
    	return firstCard;
    }
    public int getXiTotun() {
		return xiTotun;
	}

	public void setXiTotun(int xiTotun) {
		this.xiTotun = xiTotun;
	}

	public int getIsRedBlack() {
		return isRedBlack;
	}

	public void setIsRedBlack(int isRedBlack) {
		this.isRedBlack = isRedBlack;
	}

	public int getIsLianBanker() {
		return isLianBanker;
	}

	public void setIsLianBanker(int isLianBanker) {
		this.isLianBanker = isLianBanker;
	}

    public int getCeiling() {
        return ceiling;
    }

    public void setCeiling(int ceiling) {
        this.ceiling = ceiling;
        changeExtend();
    }


    /**
     * 获取所有底牌内容
     */
    public List<Integer> getStartLeftCards() {
        return startLeftCards;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        String hu = wrapper.getString(1);
        if (!StringUtils.isBlank(hu)) {
            huConfirmList = StringUtil.explodeToIntList(hu);
        }
        moFlag = wrapper.getInt(2, 0);
        toPlayCardFlag = wrapper.getInt(3, 0);
        moSeat = wrapper.getInt(4, 0);
        String moSeatVal = wrapper.getString(5);
        if (!StringUtils.isBlank(moSeatVal)) {
            moSeatPair = new KeyValuePair<>();
            String[] values = moSeatVal.split("_");
            String idStr = StringUtil.getValue(values, 0);
            if (!StringUtil.isBlank(idStr)) {
                moSeatPair.setId(Integer.parseInt(idStr));
            }

            moSeatPair.setValue(StringUtil.getIntValue(values, 1));
        }
        String autoDisPhz = wrapper.getString(6);
        if (!StringUtils.isBlank(autoDisPhz)) {
            autoDisBean = new PaohuziCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = PaohzCard.getPaohzCard(wrapper.getInt(7, 0));
        sendPaoSeat = wrapper.getInt(8, 0);
        firstCard = wrapper.getInt(9, 1) == 1 ? true : false;
        beRemoveCard = PaohzCard.getPaohzCard(wrapper.getInt(10, 0));
        shuXingSeat = wrapper.getInt(11, 0);
        maxPlayerCount = wrapper.getInt(12, 3);
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
        ceiling = wrapper.getInt(13, 0);
        isRedBlack = wrapper.getInt(14, 0);
        isLianBanker = wrapper.getInt(15, 0);
        xiTotun = wrapper.getInt(16, 3);
        if (payType== -1) {
            String isAAStr =  wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume"))?1:2;
            } else {
                payType=1;
            }
        }

        catCardCount = wrapper.getInt("catCardCount", catCardCount);

        jiaBei = wrapper.getInt(17, 0);
        jiaBeiFen = wrapper.getInt(18, 0);
        jiaBeiShu = wrapper.getInt(19, 0);
    }

    private List<Integer> loadStartLeftCards(String json) {
        List<Integer> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            list.add(Integer.valueOf(val.toString()));
        }
        return list;
    }

    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        String val1 = wrapper.getString(1);
        if (!StringUtils.isBlank(val1)) {
            actionSeatMap = DataMapUtil.toListMap(val1);
        }
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
        return wrapper.toString();
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
    public void calcOver() {
        if (state == table_state.ready) {
            return;
        }
        boolean isHuangZhuang = false;
        List<Integer> winList = new ArrayList<>(huConfirmList);
        if (winList.size() == 0 && leftCards.size() == 0) {
            // 流局
            isHuangZhuang = true;
        }
        int goldPay = 0;//服务费
		int goldRatio = 1;//倍率
		boolean isGold = false;
		try {
			if(isGoldRoom()){
				GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
				if (goldRoom != null) {
					isGold = true;
					modeId = goldRoom.getModeId();
					goldPay = PayConfigUtil.get(playType, goldRoom.getGameCount(), goldRoom.getMaxCount(), 0, goldRoom.getModeId());
					if (goldPay < 0) {
						goldPay = 0;
					}
					goldRatio = GameConfigUtil.loadGoldRatio(modeId);
				}
			}
		}catch (Exception e){
		}
        List<Integer> mt = null;
        int winFen = 0;
        int totalTun = 0;// 赢的胡息数或屯数
        boolean isOver = false;
        Map<Long,Integer> outScoreMap = new HashMap<>();
        Map<Long,Integer> ticketMap = new HashMap<>();
        if(isGold){
        	isOver = true;
        	if(winList.isEmpty()){
        		for (SyPaohuziPlayer player : seatMap.values()){
        			player.changeGold(-goldPay,playType);
        			player.calcResult(this, 1, 0, isHuangZhuang);
        		}
        	}else{
        		SyPaohuziPlayer winPlayer = seatMap.get(winList.get(0));
        		mt = PaohuziMingTangRule.calcMingTang(winPlayer);
                winFen = PaohuziMingTangRule.calcMingTangFen(winPlayer, mt);
                long totalWin = 0;
                for (int seat : seatMap.keySet()){
                	if (!winList.contains(seat)){
                		SyPaohuziPlayer player = seatMap.get(seat);
                		int lossPoint = winFen;
                		if (goldRatio>1){
                			lossPoint*=goldRatio;
    					}
                		long allGold = player.loadAllGolds();
                		if (allGold<goldPay+lossPoint){
                			totalWin += (allGold-goldPay);
                			player.changeGold((int)-allGold,playType);
                			player.calcResult(this, 1, (int)(goldPay-allGold), isHuangZhuang);
                		}else{
                			totalWin += lossPoint;
                			player.changeGold((int)-(lossPoint + goldPay),playType);
                			player.calcResult(this, 1, -lossPoint, isHuangZhuang);
                		}
                        player.updateGoldRoomActivity(false);
                	}

                }

                winPlayer.calcResult(this, 1, (int)totalWin, isHuangZhuang);

                long allGold = winPlayer.loadAllGolds();
                if(totalWin > allGold){
                	outScoreMap.put(winPlayer.getUserId(), (int)(totalWin - allGold));

                    totalWin = allGold;
                }

                if (totalWin>0){
                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig","gold_room_award"+modeId);
                    if (tmpConfig!=null&&tmpConfig.intValue()>0){
                        int  ticketCount = (int)(totalWin /(tmpConfig.intValue()));
                        if (ticketCount>0) {
                            ticketMap.put(winPlayer.getUserId(),ticketCount);
                            UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                    String.valueOf(winPlayer.getUserId()), UserResourceType.TICKET.name(),String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                            LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}",modeId,winPlayer.getUserId(),ticketCount);
                        }
                    }
                }

                winPlayer.changeGold((int)(totalWin-goldPay),playType);
                winPlayer.updateGoldRoomActivity(true);
        	}
        }else{
        	for (int winSeat : winList) {
                // 赢的玩家
                boolean isSelfMo = winSeat == moSeat;
                SyPaohuziPlayer winPlayer = seatMap.get(winSeat);
                int getPoint = 0;
    			winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
                if(isSelfMo){
                	winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
                }
                mt = PaohuziMingTangRule.calcMingTang(winPlayer);
                winFen = PaohuziMingTangRule.calcMingTangFen(winPlayer, mt);
                if (isBoPi()) {
                    // 剥皮按胡息
                    getPoint = totalTun + winFen;
                } else {
                    // 字牌算几等
                    totalTun = winPlayer.calcHuPoint(winFen, getXiTotun());//得屯数
                }
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        SyPaohuziPlayer player = seatMap.get(seat);
                        if (isBoPi()) {
                            if (4 == getMaxPlayerCount() && seat == shuXingSeat) {
                                // 数醒不为输
                                continue;
                            }
                            player.calcResult(this, 1, 0, isHuangZhuang);
                        } else {
                            getPoint += totalTun;
                            player.calcResult(this, 1, -totalTun, isHuangZhuang);
                        }
                    }
                }
                winPlayer.calcResult(this, 1, getPoint, isHuangZhuang);
                if (isSiRenBoPi()) {
                    SyPaohuziPlayer shuXingPlayer = seatMap.get(shuXingSeat);
                    int shuXingAddPoint = getPoint / 2;
                    if (getPoint % 2 != 0) {
                        shuXingAddPoint += 1;
                    }
                    shuXingPlayer.calcResult(this, 1, shuXingAddPoint, isHuangZhuang);
                }
            }
            SyPaohuziPlayer winPlayer = null;
            //boolean selfMo = false;
            if (!winList.isEmpty()) {
                winPlayer = seatMap.get(winList.get(0));
                //selfMo = winPlayer.getSeat() == moSeat;
            } else {
                SyPaohuziPlayer lastWinPlayer = seatMap.get(lastWinSeat);
                if (isBoPi()) {
                    lastWinPlayer.calcResult(this, 1, -10, isHuangZhuang);
                    if (4 == getMaxPlayerCount()) {
                        SyPaohuziPlayer shuXingPlayer = seatMap.get(shuXingSeat);
                        shuXingPlayer.calcResult(this, 1, 10, isHuangZhuang);
                    }
                }
            }
            if (isBoPi()) {
                if (!winList.isEmpty()) {
                    int winSeat = winList.get(0);
                    if (winSeat == lastWinSeat && getIsLianBanker() == 1) { //连庄
    					SyPaohuziPlayer bankerPlayer;
                        bankerPlayer=winPlayer==null?seatMap.get(lastWinSeat):winPlayer;
                        //if (ceiling>0&&bankerPlayer.loadScore()>=ceiling) {
                        if (ceiling>0&&bankerPlayer.getTotalPoint()>=ceiling) {
                            isOver=true;
                        }
                    } else {
                        // 下庄只要一个玩家胡息>=100就结束
                        for (SyPaohuziPlayer temPlayer : seatMap.values()) {
                            if (temPlayer.getTotalPoint() >= 100) {
                                isOver = true;
                                break;
                            }
                        }
                    }
                } else if (isSiRenBoPi()) {
                    // 数醒的大于100也结束
                    SyPaohuziPlayer shuXingPlayer = seatMap.get(shuXingSeat);
                    if (shuXingPlayer.getTotalPoint() >= 100) {
                        isOver = true;
                    }
                }
            }
        }
        if(playBureau == totalBureau){
            isOver=true;
            changeTableState(table_state.over);
        }
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, winFen, mt, totalTun, false, outScoreMap,ticketMap);
        saveLog(isOver,0L, res.build());
        if (!winList.isEmpty()) {
            setLastWinSeat(winList.get(0));
        } else {
            if (!isBoPi()) {
                int next = getNextSeat(lastWinSeat);
                setLastWinSeat(next);
            }
        }
        calcAfter();
        if (isOver) {
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

    @Override
    public void saveLog(boolean over,long winId, Object resObject) {
        ClosingPhzInfoRes res = (ClosingPhzInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
        Date now = TimeUtil.now();
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setUserId(creatorId);
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
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        if (!isGoldRoom()){
        	for (SyPaohuziPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = PaohuziTool.explodePhz(info.getNowDisCardIds(), ",");
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = PaohuziTool.explodePhz(info.getLeftPais(), ",");
        }
        if (isGoldRoom()){
			autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhz",10*1000);
            autoTimeOut2=autoTimeOut;
		}else{
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal",60*1000);
            autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal2",10*1000);
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {
        // 天胡或者暗杠
//        int lastCardIndex = RandomUtils.nextInt(21);
        SyPaohuziPlayer winPlayer = seatMap.get(lastWinSeat);

        for (SyPaohuziPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getSeat() == shuXingSeat?winPlayer.getHandPais():tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            if (isSiRenBoPi()) {
                res.addXiaohu(shuXingSeat);
            }

            tablePlayer.writeSocket(res.build());
        }
    }

    @Override
    public void startNext() {
        checkAction();
    }

    public void play(SyPaohuziPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(SyPaohuziPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action)) {
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(0) != 1) {
            return;
        }

        PaohuziHuLack paoHu = player.checkPaoHu(nowDisCard, isSelfMo(player), (isBoPi() && firstCard));
        PaohuziHuLack pingHu = player.checkHu(nowDisCard, isSelfMo(player));
        if (!pingHu.isHu()) {
            PaohuziCheckCardBean paoHuBean = player.checkPaoHu();
            if (paoHuBean.isHu()) {
                pingHu = paoHuBean.getLack();
            }
        }
        PaohuziHuLack hu = pingHu;
        if (paoHu.isHu()) {
            int paoHuOutHuXi = player.getPaoOutHuXi(nowDisCard, isSelfMo(player), (isBoPi() && firstCard));
            if (paoHu.getHuxi() + paoHuOutHuXi > pingHu.getHuxi()) {
                play(player, PaohuziTool.toPhzCardIds(paoHu.getPaohuList()), paoHu.getPaohuAction(), false, true, false);
                hu = player.checkHu(null, isSelfMo(player));
            }
        } else {
            hu = pingHu;
        }

        if (hu.isHu()&&player.getSiShou()==0) {
            player.setHuxi(hu.getHuxi());
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            sendActionMsg(player, action, null, PaohzDisAction.action_type_action);
            calcOver();
        } else {
            broadMsg(player.getName() + " 不能胡牌");
        }
    }

    /**
     * 是否自摸
     *
     * @param player
     * @return
     */
    public boolean isSelfMo(SyPaohuziPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat() || (player.getSeat()==shuXingSeat&&moSeatPair.getValue().intValue()==lastWinSeat);
        }
        return false;
    }

    /**
     * 提
     */
    private void ti(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai) {
        // cards肯定是4个相同的
        if (cardList == null) {
            System.out.println("提不合法:" + cardList);
            player.writeErrMsg("提不合法:" + cardList);
            return;
        }

        if (cardList.size() == 1) {
            List<PaohzCard> tiCards = player.getTiCard(cardList.get(0));
            if (tiCards == null || tiCards.size() != 3) {
                System.out.println("提不合法:" + tiCards);
                player.writeErrMsg("提不合法:" + tiCards);
                return;
            }
            cardList.addAll(tiCards);
        } else {
            if (!player.getHandPhzs().contains(cardList.get(0))) {
                return;
            }
        }
        // 是否栽跑
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());

        if (cardList.size() != 4 && !cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }

        if (cardList.size() != 4) {
            return;
        }

        if (!PaohuziTool.isSameCard(cardList)) {
            System.out.println("提不合法:" + cardList);
            player.writeErrMsg("提不合法:" + cardList);
            return;
        }
		player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_TI, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        if (nowDisCard != null) {
            getDisPlayer().removeOutPais(nowDisCard);
        }
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

        // 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);

        // 是否能出牌
        if (!moPai) {
            // 不是轮到自己摸牌的时候提的牌
            boolean disCard = setDisPlayer(player, action, checkCard.isHu());
            if (!disCard) {
                // checkMo();
            }

        }
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
        // player.getName() + "------提-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }

    }

    /**
     * 栽(臭栽)
     *
     * @param cardList 要栽的牌
     */
    private void zai(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        if (action == PaohzDisAction.action_zai) {
            setZaiCard(nowDisCard);

        }
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);
        // 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);
        // 是否能出牌
        boolean disCard = setDisPlayer(player, action, isFristDisCard, checkCard.isHu());
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
        // player.getName() + "------栽-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }
        if (!disCard) {
            // checkMo();
        }

    }

    /**
     * 跑
     */
    private void pao(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu) {
        if (cardList.size() != 3 && cardList.size() != 1) {
            broadMsg("跑的张数不对:" + cardList);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (!isHu && actionList.get(5) != 1) {
            return;
        }

        // 能跑胡的情况下不 胡挡不住跑
        if (!isHu && !checkAction(player, action)) {
            // 发现别人能胡
            // 能跑能胡的情况下
            if (actionList.get(0) == 1) {
                actionList.set(0, 0);
                addAction(player.getSeat(), actionList);
                // 更新前台数据
                setSendPaoSeat(player.getSeat());
                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        if (cardList.size() == 1) {
            // 如果是一张牌说明已经在出的牌里面了
            List<PaohzCard> list = player.getSameCards(nowDisCard);
            cardList.addAll(list);
        }

        if (cardList.size() != 4) {
            return;
        }

        // 检测是否能提
        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
		player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_PAO, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        if (!isHu && !isPassHu && isMoFlag()) {
            PaohuziCheckCardBean checkCard = player.checkPaoHu();
            checkPaohuziCheckCard(checkCard);
        }

        // 是否能出牌
        if (!isHu) {
            boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, !disCard);
            if (!disCard) {
                if (PaohuziConstant.isAutoMo) {
                    checkMo();
                }
            }
        } else {
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        }

        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
        // player.getName() + "------跑-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }

    }

    private void relieveFangZhao(long userId) {
        for (Player player : seatMap.values()) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, userId + "", 0 + "");
        }
    }

    /**
     * 出牌
     */
    private void disCard(SyPaohuziPlayer player, List<PaohzCard> cardList, int action) {
        if (!actionSeatMap.isEmpty()) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
            return;
        }

        if (toPlayCardFlag != 1) {
    		player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            LogUtil.e(player.getName() + "错误 toPlayCardFlag:" + toPlayCardFlag + "出牌");
            checkMo();
            return;
        }

        if (player.getSeat() != nowDisCardSeat) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("轮到:" + nowDisCardSeat + "出牌");
            return;
        }
        if (cardList.size() != 1) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("出牌数量不对:" + cardList);
            return;
        }

        PaohuziHandCard cardBean = player.getPaohuziHandCard();
        if (!cardBean.isCanoperateCard(cardList.get(0))) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("该牌不能单出:" + cardList);
            LogUtil.e("该牌不能单出:" + cardList);
            return;
        }

        // 判断是否为放招
        boolean paoFlag = isFangZhao(player, cardList.get(0));
        if (paoFlag) {
        	if(player.isAutoPlay()){//托管自动放招
        		player.setFangZhao(1);
        		for (Player playerTemp : getSeatMap().values()) {
    				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
    			}
        	}else if (!player.isFangZhao() && !player.isRobot()) {
            	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
                LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" + player.getName() + "------是否确定放招:--->>>>>>" + cardList.get(0));
                player.writeComMessage(WebSocketMsgType.res_com_code_fangzhao, cardList.get(0).getId());
                return;
            }
        }

        // 检查剥皮玩法的天胡？地胡？
        boolean canDiHu = false;
        if (player.isFristDisCard() && player.getSeat() == lastWinSeat && isBoPi()) {
            canDiHu = true;
        } else {
            if (firstCard) {
                firstCard = false;
            }
        }

        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        checkFreePlayerTi(player, action);// 检查闲家提
        player.disCard(action, cardList);
        setMoFlag(0);
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
        setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        PaohuziCheckCardBean autoDisCard = checkDisAction(player, action, cardList.get(0), canDiHu);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_dis);
        // if (autoDisCard != null) {
        // // 系统自动出牌
        // playAutoDisCard(autoDisCard);
        // } else {
        checkAutoMo();
        // }
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();

        }
    }

    private void tiLong(SyPaohuziPlayer player) {
        boolean isTiLong = false;
        List<PaohzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PaohzCard card = null;
            if (GameServerConfig.isDebug()) {
                if (card == null) {
                    card = getNextCard(106);
                }
                if (card == null) {
                    card = getNextCard(4);
                }
            }

            if (card == null) {
                card = getNextCard();

            }
            player.tiLong(card);
            cardList.add(card);

            StringBuilder sb = new StringBuilder("SyPhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(player.getAutoPlayCheckedTime());
            sb.append("|").append("tiLong");
            sb.append("|").append(card);
            LogUtil.msgLog.info(sb.toString());
        }

        if (isTiLong) {
            sendActionMsg(player, PaohzDisAction.action_tilong, cardList, PaohzDisAction.action_type_action, false, false);

            PaohuziCheckCardBean checkCard = player.checkCard(null, true, true, false);
            if (checkPaohuziCheckCard(checkCard)) {
                playAutoDisCard(checkCard);
                if (player.getSeat() != lastWinSeat && checkCard.isTi()) {
                    player.setOweCardCount(player.getOweCardCount() - 1);
                }
                tiLong(player);
            }
        }
    }

    public void checkFreePlayerTi(SyPaohuziPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                SyPaohuziPlayer nowPlayer = seatMap.get(seat);
                PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
                if (checkPaohuziCheckCard(checkCard)) {
                    playAutoDisCard(checkCard);
                    if (nowPlayer.isFristDisCard()) {
                        nowPlayer.setFristDisCard(false);
                    }
                    tiLong(nowPlayer);
                    /*-- 暂时屏蔽两条龙的处理
					boolean needBuPai = false;
					if (checkCard.isTi()) {
						PaohuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
						PaohzCardIndexArr valArr = cardBean.getIndexArr();
						PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
						if (index3 != null && index3.getLength() >= 2) {
							needBuPai = true;
						}
					}
					playAutoDisCard(checkCard);
					if (nowPlayer.isFristDisCard()) {
						nowPlayer.setFristDisCard(false);
					}

					if (needBuPai) {
						PaohzCard buPai = leftCards.remove(0);
						for (SyPaohuziPlayer tempPlayer : seatMap.values()) {
							if (seat == tempPlayer.getSeat()) {
								tempPlayer.getHandPhzs().add(buPai);
								System.out.println("----------------------------------谁补:" + tempPlayer.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
							}
							tempPlayer.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, seat, buPai.getId(), leftCards.size());
						}
					}
					 */

                }
                checkSendActionMsg();
            }
        }
    }

    /**
     * 碰
     */
    private void peng(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action)) {
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        cardList = player.getPengList(nowDisCard, cardList);
        if (cardList == null) {
            player.writeErrMsg("不能碰");
            return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);

        }
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            // checkMo();
        }

        // 碰的情况,把所有玩家的过牌去掉
        if (isMoFlag()) {
            for (SyPaohuziPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.removePassChi(nowDisCard.getVal());
            }
        }
    }

    /**
     * 过
     */
    private void pass(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            // player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
        // 栽，提，跑是不可以过的
        if (list.contains(PaohzDisAction.action_zai) || list.contains(PaohzDisAction.action_ti) || list.contains(PaohzDisAction.action_pao) || list.contains(PaohzDisAction.action_chouzai)) {
            return;
        }
        // 如果没有吃，碰，胡也是不可以过的
        if (!list.contains(PaohzDisAction.action_chi) && !list.contains(PaohzDisAction.action_peng) && !list.contains(PaohzDisAction.action_hu)) {
            return;
        }

        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        if (actionList.get(0) == 1 && player.getHandPhzs().isEmpty()) {
            player.writeErrMsg("手上已经没有牌了");
            return;
        }

        if(player.getOperateCards().isEmpty()){
            player.setSiShou(1);
        }

        int val = 0;
        if (nowDisCard != null) {
            val = nowDisCard.getVal();
        }

        boolean addPassChi = false;
        if (player.getSeat() == moSeat) {
            addPassChi = true;
        }

        // 将pass的吃碰值添加到passChi或passPeng中
        for (int passAction : list) {
            player.pass(passAction, val, addPassChi);

        }
        removeAction(player.getSeat());
        // 自动出牌
        if (autoDisBean != null) {
            playAutoDisCard(autoDisBean);
        } else {
            PaohuziCheckCardBean checkCard = player.checkCard(nowDisCard, isSelfMo(player), isPassHu, false, false, true);
            checkCard.setPassHu(isPassHu);
            boolean check = checkPaohuziCheckCard(checkCard);
            markMoSeat(player.getSeat(), action);
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
            if (check) {
                playAutoDisCard(checkCard, true);

            } else {
                if (PaohuziConstant.isAutoMo) {
                    checkMo();
                } else {
                    if (isTest()) {
                        checkMo();

                    }
                }

            }
        }

        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
        }

    }

    /**
     * 吃
     */
    private void chi(SyPaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (cardList != null) {
            if (cardList.size() % 3 != 0) {
                player.writeErrMsg("不能吃" + cardList);
                return;
            }

            if (!cardList.contains(nowDisCard)) {
                return;
            }
        }

        cardList = player.getChiList(nowDisCard, cardList);
        if (cardList == null) {
            player.writeErrMsg("不能吃");
            return;
        }

        if (cardList.size() > 3) {
            PaohuziHandCard card = player.getPaohuziHandCard();
            if (card.getOperateCards().size() <= cardList.size()) {
                player.writeErrMsg("您手上没有剩余的牌可打，不能吃");
                return;
            }
        }

        if (!checkAction(player, action)) {
            // 能吃能碰的情况下
            if (actionList.get(1) == 1) {
                actionList.set(1, 0);
                // 选择了吃，那不能碰了
                player.pass(PaohzDisAction.action_peng, nowDisCard.getVal());
                addAction(player.getSeat(), actionList);
                // 更新前台数据
                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        if (PaohuziTool.isPaohuziRepeat(cardList)) {
            player.writeErrMsg("不能吃");
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            if (PaohuziConstant.isAutoMo) {
                checkMo();
            }
        }

    }

    public synchronized void play(SyPaohuziPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
        // 检查play状态
        if (state != table_state.play || player.getSeat() == shuXingSeat) {
            return;
        }

        PaohzCard nowDisCard = null;
        List<PaohzCard> cardList = null;
        // 非摸牌非过牌要检查能否出牌,并将要出的牌id集合变成跑胡子牌
        if (action != PaohzDisAction.action_mo) {
            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
                nowDisCard = nowDisCardIds.get(0);
            }
            if (action != PaohzDisAction.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = PaohuziTool.toPhzCards(cardIds);
            }
        }

        if (action != PaohzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("SyPhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(player.getAutoPlayCheckedTime());
            sb.append("|").append(PaohzDisAction.getActionName(action));
            sb.append("|").append(cardList);
            sb.append("|").append(nowDisCard);
            if (actionSeatMap.containsKey(player.getSeat())) {
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
            }
            LogUtil.msgLog.info(sb.toString());
        }

        // //////////////////////////////////////////////////////

        if (action == PaohzDisAction.action_ti) {
            if (cardList.size() > 4) {
                // 有多个提
                PaohzCardIndexArr arr = PaohuziTool.getMax(cardList);
                PaohuziIndex index = arr.getPaohzCardIndex(3);
                for (List<PaohzCard> tiCards : index.getPaohzValMap().values()) {
                    ti(player, tiCards, nowDisCard, action, moPai);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai);
            }
        } else if (action == PaohzDisAction.action_hu) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == PaohzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pao) {
            pao(player, cardList, nowDisCard, action, isHu, isPassHu);
        } else if (action == PaohzDisAction.action_zai || action == PaohzDisAction.action_chouzai) {
            zai(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_mo) {
            if (isTest()) {
                return;
            }
            if (checkMoMark != null) {
                int cAction = cardIds.get(0);
                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
                    checkMo();
                } /*else {
					// System.out.println("发现请求-->" + player.getName() +
					// " seat:" + player.getSeat() + "-" + checkMoMark.getId() +
					// " action:" + cAction + "- " + checkMoMark.getValue());
				}*/
            }

        } else {
            disCard(player, cardList, action);
        }
        if (!moPai && !isHu) {
            // 摸牌的时候提不需要做操作
            robotDealAction();
        }

    }

    private boolean setDisPlayer(SyPaohuziPlayer player, int action, boolean isHu) {
        return setDisPlayer(player, action, false, isHu);
    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setDisPlayer(SyPaohuziPlayer player, int action, boolean isFirstDis, boolean isHu) {
        if (this.leftCards.isEmpty()) {
            // 手上已经没有牌了
            if (!isHu) {
                calcOver();
            }
            return false;
        }

        boolean canDisCard = true;
        if (player.getHandPhzs().isEmpty()) {
            canDisCard = false;

        } else if (player.getOperateCards().isEmpty()) {
            canDisCard = false;
            if(!isHu)
                player.setSiShou(1);
        }
        if (canDisCard && ((player.getSeat() == lastWinSeat && isFirstDis) || player.isNeedDisCard(action))) {
            setNowDisCardSeat(player.getSeat());
            setToPlayCardFlag(1);
            return true;
        } else {
            // 不需要出牌 下一家直接摸牌
            setToPlayCardFlag(0);
            player.compensateCard();
            int next = calcNextSeat(player.getSeat());
            setNowDisCardSeat(next);

            if (actionSeatMap.isEmpty()) {
                markMoSeat(player.getSeat(), action);
            }
            return false;
        }
    }

    /**
     * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(SyPaohuziPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = PaohzDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = PaohzDisAction.canDis(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = PaohuziDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
                    // 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }

                }

            }

        }
        return true;
    }

    /**
     * 获得出牌位置的玩家
     */
    private SyPaohuziPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(SyPaohuziPlayer player, int action, List<PaohzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (SyPaohuziPlayer player : seatMap.values()) {
        // if (player.getIsEntryTable() != PdkConstants.table_online) {
        // // 通知其他人离线
        // broadIsOnlineMsg(player, player.getIsEntryTable());
        // return 2;
        // }
        // }
        return 0;
    }

    private synchronized void checkMo() {
        if (autoDisBean != null) {
            playAutoDisCard(autoDisBean);
        }

        // 0胡 1碰 2栽 3提 4吃 5跑
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (nowDisCardSeat == 0) {
            return;
        }

        // // 下一个要摸牌的人
        SyPaohuziPlayer player = seatMap.get(nowDisCardSeat);
        // if (moSeat == player.getSeat()) {
        // if (moSeat == disCardSeat) {
        // broadMsg("moSeat == disCardSeat" + moSeat + " " + disCardSeat);
        // return;
        //
        // }
        // }

        if (toPlayCardFlag == 1) {
            // 接下来应该打牌
            return;
        }

        if (leftCards == null) {
            return;
        }
        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
            return;
        }

        clearMarkMoSeat();

        // PaohzCard card = PaohzCard.getPaohzCard(59);
        // PaohzCard card = getNextCard();
        PaohzCard card = null;
        if (player.getFlatId().startsWith("vkscz2855914")) {
            card = getNextCard(102);
            // if (card == null) {
            // card = PaohzCard.getPaohzCard(61);
            // }
            if (card == null) {
                card = getNextCard();
            }
        } else {
            if (GameServerConfig.isDebug() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    List<PaohzCard> cardList = PaohuziTool.findPhzByVal(getLeftCards(), zpMap.get(player.getUserId()));
                    if (cardList != null && cardList.size() > 0) {
                        zpMap.remove(player.getUserId());
                        card = cardList.get(0);
                        getLeftCards().remove(card);
                    }
                }
            }
            if(card == null){
                card = getNextCard();
            }
        }

        addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");

        StringBuilder sb = new StringBuilder("SyPhz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("moPai");
        sb.append("|").append(card);
        LogUtil.msgLog.info(sb.toString());

        if (card != null) {
            if (isTest()) {
                sleep();

            }
            // 玩家没有托管状态下，重置玩家操作时间
            if (!player.isAutoPlay()) {
                player.setLastOperateTime(System.currentTimeMillis());
            }

            setMoFlag(1);
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setFirstCard(false);
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());

            PaohuziCheckCardBean autoDisCard = null;
            for (Entry<Integer, SyPaohuziPlayer> entry : seatMap.entrySet()) {
                PaohuziCheckCardBean checkCard = entry.getValue().checkCard(card, entry.getKey() == player.getSeat(), false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }

            }

            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            if (autoDisCard != null && autoDisCard.getAutoAction() == PaohzDisAction.action_zai) {
                sendMoMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);

            } else {
                sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);

            }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
            }

            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }

//			if (PaohuziConstant.isAutoMo) {
            // if (actionSeatMap.isEmpty()) {
            // markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            // }
            // checkMo();
//			}
            checkAutoMo();
        }
    }

    /**
     * 除了吃和碰之外的都是特殊动作
     */
    private boolean isHasSpecialAction() {
        boolean b = false;
        for (List<Integer> actionList : actionSeatMap.values()) {
            if (actionList.get(0) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1 || actionList.get(5) == 1 || actionList.get(6) == 1) {
                // 除了吃和碰之外的都是特殊动作
                b = true;
                break;
            }
        }
        return b;
    }

    /**
     * @return 是否有系统帮助自动出牌
     */
    private PaohuziCheckCardBean checkDisAction(SyPaohuziPlayer player, int action, PaohzCard disCard, boolean isFirstCard) {
        PaohuziCheckCardBean autoDisCheck = null;
        for (Entry<Integer, SyPaohuziPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

//            PaohuziCheckCardBean checkCard = entry.getValue().checkCard(disCard, false, isFirstCard);
            PaohuziCheckCardBean checkCard = entry.getValue().checkCard(disCard,false,!isFirstCard,false,isFirstCard,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(SyPaohuziPlayer player, PaohzCard disCard) {

        for (Entry<Integer, SyPaohuziPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

            boolean flag = entry.getValue().canFangZhao(disCard);
            if (flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查自动提
     */
    private PaohuziCheckCardBean checkAutoDis(SyPaohuziPlayer player, boolean isMoPaiIng) {
        PaohuziCheckCardBean checkCard = player.checkTi();
        checkCard.setMoPaiIng(isMoPaiIng);
        boolean check = checkPaohuziCheckCard(checkCard);
        if (check) {
            return checkCard;
        } else {
            return null;
        }
    }

    public boolean checkPaohuziCheckCard(PaohuziCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return false;
        }

        addAction(checkCard.getSeat(), list);
        List<PaohzCard> autoDisList = checkCard.getAutoDisList();
        if (autoDisList != null) {
            // 不能胡就自动出牌
            if (!checkCard.isHu()) {
                setAutoDisBean(checkCard);
                return true;
            }
        }
        return false;

    }

    public void setAutoDisBean(PaohuziCheckCardBean autoDisBean) {
        this.autoDisBean = autoDisBean;
        changeExtend();
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        addPlayLog(seat, PaohzDisAction.action_hasaction + "", StringUtil.implode(actionList));
        saveActionSeatMap();
    }

    private List<Integer> removeAction(int seat) {
        if (sendPaoSeat == seat) {
            setSendPaoSeat(0);
        }
        List<Integer> list = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return list;
    }

    private void clearAction() {
        setSendPaoSeat(0);
        actionSeatMap.clear();
        saveActionSeatMap();
    }

    private void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    private void sendActionMsg(SyPaohuziPlayer player, int action, List<PaohzCard> cards, int actType) {
        sendActionMsg(player, action, cards, actType, false, false);
    }

    /**
     * 发送所有玩家动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendMoMsg(SyPaohuziPlayer player, int action, List<PaohzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
        LogUtil.msgLog.debug("SyPhz|sendMoMsg|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + player.getName() + "|" + (player.isAutoPlay() ? 1 : 0) + "|" + PaohzDisAction.getActionName(action));
    }

    /**
     * 发送该玩家动作msg
     */
    private void sendPlayerActionMsg(SyPaohuziPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PaohzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        // builder.addAllPhzIds(PaohuziTool.toPhzCardIds(nowDisCardIds));
        builder.setActType(0);
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
        if (actionList != null) {
            builder.addAllSelfAct(actionList);
        }
        player.writeSocket(builder.build());

        if (player.getSeat()==lastWinSeat&&shuXingSeat>0){
            SyPaohuziPlayer paohuziPlayer = seatMap.get(shuXingSeat);
            paohuziPlayer.writeSocket(builder.build());
        }
    }

    private void setNextSeatMsg(PlayPaohuziRes.Builder builder) {
        // if (!GameServerConfig.isDebug()) {
        // builder.setNextSeat(nowDisCardSeat);
        //
        // } else {
        builder.setTimeSeat(nowDisCardSeat);
        if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else {
            builder.setNextSeat(0);

        }

        // }

    }

    /**
     * 发送动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendActionMsg(SyPaohuziPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        if (isZaiPao) {
            builder.setIsZaiPao(1);
        }
        if (isChongPao) {
            builder.setIsChongPao(1);
        }
        sendMsgBySelfAction(builder);
        LogUtil.msgLog.debug("SyPhz|sendActionMsg|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + player.getName() + "|" + (player.isAutoPlay() ? 1 : 0) + "|" + PaohzDisAction.getActionName(action));
    }

    /**
     * 目前的动作中是否有人有栽或者是提
     *
     * @return
     */
    private KeyValuePair<Boolean, Integer> getZaiOrTiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1 || entry.getValue().get(3) == 1) {
                isHasZaiOrTi = true;
                zaiSeat = entry.getKey();
                break;
            }
        }
        keyValue.setId(isHasZaiOrTi);
        keyValue.setValue(zaiSeat);
        return keyValue;
    }

    private List<Integer> getSendSelfAction(KeyValuePair<Boolean, Integer> zaiKeyValue, int seat, List<Integer> actionList) {
        boolean isHasZaiOrTi = zaiKeyValue.getId();
        int zaiSeat = zaiKeyValue.getValue();
        if (isHasZaiOrTi) {
            if (zaiSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(0) == 1) {
            return actionList;
        } else if (actionList.get(5) == 1) {
            if (sendPaoSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(2) == 1 || actionList.get(3) == 1) {
            // 0胡 1碰 2栽 3提 4吃 5跑
            // 如果能自动出牌的话 不需要提示
            // ...
            return null;
        } else {
            return actionList;
        }
        return null;

    }

    /**
     * 发送消息带入自己动作
     *
     * @param builder
     */
    private void sendMoMsgBySelfAction(PlayPaohuziRes.Builder builder, int seat) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        SyPaohuziPlayer winPlayer = seatMap.get(lastWinSeat);
        for (SyPaohuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
                // copy.clearPhzIds();
                // copy.addPhzIds(0);
                if (seat==lastWinSeat&&player.getSeat()==shuXingSeat){
                    copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                }
            } else {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
            }
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }else if (seat==lastWinSeat&&shuXingSeat==player.getSeat()&&actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }
            player.writeSocket(copy.build());
        }
    }

    /**
     * 发送消息带入自己动作
     *
     * @param builder
     */
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        int actType = builder.getActType();
        boolean noShow = false;
        // boolean hasHu = false;
        int paoSeat = 0;
        if (PaohzDisAction.action_type_dis == actType || PaohzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }

                // if (1 == entry.getValue().get(0)) {
                // hasHu = true;
                // }
            }

            // if (hasHu) {
            // noShow = false;
            // }
        }

        SyPaohuziPlayer winPlayer = seatMap.get(lastWinSeat);

        for (SyPaohuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                if(player.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                	copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }else if(copy.getSeat()==lastWinSeat&&player.getSeat()==shuXingSeat){
                copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                if(winPlayer.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }

            // 需要特殊处理一下栽
            if (copy.getAction() == PaohzDisAction.action_zai) {
                if (copy.getSeat() != player.getSeat()) {
                    if (copy.getSeat()==lastWinSeat&&player.getSeat()==shuXingSeat){
                    }else{
                        // 需要替换成0
                        List<Integer> ids = PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
                        copy.clearPhzIds();
                        copy.addAllPhzIds(ids);
                    }
                }
            }

            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != player.getSeat()) {
                        // 出牌时，别人有跑的情况不提示吃碰
                        if (1 == actionList.get(0)) {
                            copy.addAllSelfAct(actionList);
                        }
                    } else {
                        copy.addAllSelfAct(actionList);
                    }
                }

            }else if (player.getSeat()==shuXingSeat&&actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != winPlayer.getSeat()) {
                        // 出牌时，别人有跑的情况不提示吃碰
                        if (1 == actionList.get(0)) {
                            copy.addAllSelfAct(actionList);
                        }
                    } else {
                        copy.addAllSelfAct(actionList);
                    }
                }

            }

            player.writeSocket(copy.build());

            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("SyPhz");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append(player.getAutoPlayCheckedTime());
                sb.append("|").append("actList");
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
                LogUtil.msgLog.info(sb.toString());
            }
        }
    }

    /**
     * 推送给有动作的人消息
     */
    private void checkSendActionMsg() {
        if (actionSeatMap.isEmpty()) {
            return;
        }

        PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
        SyPaohuziPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
        PaohuziResTool.buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getOutHuxi() + disCSMajiangPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            SyPaohuziPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
            if (shuXingSeat>0&&seatPlayer.getSeat()==lastWinSeat){
                seatMap.get(shuXingSeat).writeSocket(copy.build());
            }
        }

    }

    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
        // 先判断拿牌的玩家
        SyPaohuziPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
        PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
        if (checkPaohuziCheckCard(checkCard)) {
            playAutoDisCard(checkCard);
            tiLong(nowPlayer);
			/*-- 暂时屏蔽两条龙的处理
			boolean needBuPai = false;
			if (checkCard.isTi()) {
				PaohuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
				PaohzCardIndexArr valArr = cardBean.getIndexArr();
				PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
				if (index3 != null && index3.getLength() >= 2) {
					needBuPai = true;
				}
			}
			playAutoDisCard(checkCard);

			if (needBuPai) {
				PaohzCard buPai = leftCards.remove(0);
				for (SyPaohuziPlayer player : seatMap.values()) {
					if (nowSeat == player.getSeat()) {
						player.getHandPhzs().add(buPai);
						System.out.println("----------------------------------谁补:" + player.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
					}
					player.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, nowSeat, buPai.getId(), leftCards.size());
				}
			}
			 */
        }
        checkSendActionMsg();
    }

    /**
     * 自动出牌
     */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard) {
        playAutoDisCard(checkCard, false);
    }

    /**
     * 自动出牌
     *
     * @param moPai 是否是摸牌 如果是摸牌，需要
     */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard, boolean moPai) {
        if (checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            SyPaohuziPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
            List<Integer> list = PaohuziTool.toPhzCardIds(checkCard.getAutoDisList());
            play(player, list, checkCard.getAutoAction(), moPai, false, checkCard.isPassHu());

            if (actionSeatMap.isEmpty()) {
                setAutoDisBean(null);
            }
        }

    }

    private void sleep() {
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            if (leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }
            if (actionSeatMap.isEmpty()) {
                int nextseat = getNowDisCardSeat();
                SyPaohuziPlayer player = seatMap.get(nextseat);
                if (player != null && player.isRobot()) {
                    // 普通出牌
                    PaohuziHandCard paohuziHandCardBean = player.getPaohuziHandCard();
                    int card = RobotAI.getInstance().outPaiHandle(0, PaohuziTool.toPhzCardIds(paohuziHandCardBean.getOperateCards()), new ArrayList<Integer>());
                    if (card == 0) {
                        return;
                    }
                    sleep();
                    List<Integer> cardList = new ArrayList<>(Arrays.asList(card));
                    play(player, cardList, 0);
                }
            } else {
                // (Entry<Integer, List<Integer>> entry :
                // actionSeatMap.entrySet())
                Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    List<Integer> value = actionSeatMap.get(key);
                    SyPaohuziPlayer player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
                        // player.writeErrMsg(player.getName() + " 有动作" +
                        // entry.getValue());
                        continue;
                    }
                    List<Integer> actions = PaohzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkAction(player, action)) {
                            continue;
                        }
                        sleep();
                        if (action == PaohzDisAction.action_hu) {
                            broadMsg(player.getName() + "胡牌");
                            play(player, null, action);
                        } else if (action == PaohzDisAction.action_peng) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_chi) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_pao) {
                            // play(player, null, action);
                        } else if (action == PaohzDisAction.action_ti) {
                            // play(player,
                            // PaohuziTool.toPhzCardIds(nowDisCardIds), action);
                        }

                        break;

                    }
                }
            }

        }
    }

    @Override
    public int getPlayerCount() {
        return seatMap.size();
    }

    @Override
    protected void initNext1() {
        setSendPaoSeat(0);
        setZaiCard(null);
        setBeRemoveCard(null);
        setAutoDisBean(null);
        clearMarkMoSeat();
        clearMoSeatPair();
        clearHuList();
        setLeftCards(null);
        setStartLeftCards(null);
        setMoFlag(0);
        setMoSeat(0);
        clearAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        setFirstCard(true);
        if (isSiRenBoPi()) {
            setShuXingSeat(calcNextNextSeat(getLastWinSeat()));
        }
    }

    @Override
    public Map<String, Object> saveDB(boolean asyn) {
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
                tempMap.put("outPai1", seatMap.get(1).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai2")) {
                tempMap.put("outPai2", seatMap.get(2).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai3")) {
                tempMap.put("outPai3", seatMap.get(3).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai4")) {
                tempMap.put("outPai4", seatMap.get(4).buildOutPaiStr());
            }
            if (tempMap.containsKey("handPai1")) {
                tempMap.put("handPai1", seatMap.get(1).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", seatMap.get(2).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", seatMap.get(3).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai4")) {
                tempMap.put("handPai4", seatMap.get(4).buildHandPaiStr());
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(PaohuziTool.toPhzCardIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(PaohuziTool.toPhzCardIds(leftCards), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
            //            TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(2, moFlag);
        wrapper.putInt(3, toPlayCardFlag);
        wrapper.putInt(4, moSeat);
        if (moSeatPair != null) {
            String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
            wrapper.putString(5, moSeatPairVal);
        }
        if (autoDisBean != null) {
            wrapper.putString(6, autoDisBean.buildAutoDisStr());

        } else {
            wrapper.putString(6, "");
        }
        if (zaiCard != null) {
            wrapper.putInt(7, zaiCard.getId());
        }
        wrapper.putInt(8, sendPaoSeat);
        wrapper.putInt(9, firstCard ? 1 : 0);
        if (beRemoveCard != null) {
            wrapper.putInt(10, beRemoveCard.getId());
        }
        wrapper.putInt(11, shuXingSeat);
        wrapper.putInt(12, maxPlayerCount);
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putInt(13, ceiling);
		wrapper.putInt(14, isRedBlack);
        wrapper.putInt(15, isLianBanker);
        wrapper.putInt(16, xiTotun);
        wrapper.putInt("catCardCount", catCardCount);

        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        return wrapper;
    }

    private String startLeftCardsToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (int card : startLeftCards) {
            jsonArray.add(card);
        }
        return jsonArray.toString();
    }

    @Override
    public void fapai() {
        synchronized (this){
            if (maxPlayerCount<=1||maxPlayerCount>4){
                return;
            }

            changeTableState(table_state.play);
            deal();
        }
    }

    @Override
    protected void deal() {
    	if (playedBureau<=0){
			for (SyPaohuziPlayer player : playerMap.values()) {
				player.setAutoPlay(false,this);
				player.setLastOperateTime(System.currentTimeMillis());
			}
		}
    	if (isGoldRoom()){
			List<Long> list0=new ArrayList<>(3);
			try {
				List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(),id);
				if(list!=null){
					for (HashMap<String, Object> map:list){
						if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult","0")),0)>0){
							list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId","0")),0));
						}
					}
				}
			}catch (Exception e){
			}
			if (list0.size()>0){
				Long userId=list0.get(new SecureRandom().nextInt(list0.size()));
				Player player = playerMap.get(userId);
				if (player!=null){
					setLastWinSeat(player.getSeat());
				}
			}
			if (lastWinSeat<=0){
				setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
			}
		}else{
			if(getPlayBureau()==1 && isGroupRoom()){
	    		setLastWinSeat(new Random().nextInt(getMaxPlayerCount())+1);
	    	}
		}
        if (lastWinSeat == 0) {
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        if (isSiRenBoPi()) {
            setShuXingSeat(calcNextNextSeat(lastWinSeat));// 设置数醒的座位号
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<Integer> copy = new ArrayList<>(PaohuziConstant.cardList);
        List<List<PaohzCard>> list = PaohuziTool.fapai(copy, zp,getMaxPlayerCount());
        int i = 1;
        for (SyPaohuziPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(0))));//将初始手牌保存结算时发给客户端
                continue;
            }
            // 数醒不发牌,设置为空List
            if (player.getSeat() == shuXingSeat) {
                player.dealHandPais(new ArrayList<PaohzCard>());
                continue;
            }
            player.dealHandPais(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));//将初始手牌保存结算时发给客户端
            i++;
        }

        List<PaohzCard> cardList = new ArrayList<>(list.get(3));
        if (maxPlayerCount<=2){
            cardList.addAll(list.get(2));
        }

        //桌上所有剩余牌
        setStartLeftCards(PaohuziTool.toPhzCardIds(cardList));

        // 桌上剩余的牌
        if (catCardCount<=0){
            setLeftCards(cardList);
        }else if (catCardCount>=cardList.size()){
            setLeftCards(null);
        }else{
            setLeftCards(new ArrayList<>(cardList.subList(catCardCount,cardList.size())));
        }

    }

    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return lastWinSeat;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
     * 计算seat右边的座位
     */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        if (nextSeat == shuXingSeat) {
            nextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
        }
        return nextSeat;
    }

    /**
     * 计算seat前面的座位
     */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
        if (frontSeat == shuXingSeat) {
            frontSeat = frontSeat - 1 < 1 ? maxPlayerCount : frontSeat - 1;
        }
        return frontSeat;
    }

    /**
     * 获取数醒座位
     */
    public int calcNextNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        int nextNextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
        return nextNextSeat;
    }

    @Override
    public Player getPlayerBySeat(int seat) {
        return seatMap.get(seat);
    }

    @Override
    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        res.setRenshu(maxPlayerCount);
        if (leftCards != null) {
            res.setRemain(leftCards.size());
        } else {
            res.setRemain(0);
        }

        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        int autoCheckTime = 0;
        List<PlayerInTableRes> players = new ArrayList<>();
        for (SyPaohuziPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes==null){
                continue;
            }
            //是否为庄
            playerRes.addRecover((player.getSeat() == lastWinSeat) ? 1 : 0);
            if (player.getUserId() == userId) {
                if (player.getSeat()==shuXingSeat){
                    SyPaohuziPlayer winPlayer = seatMap.get(lastWinSeat);

                    playerRes.addAllHandCardIds(winPlayer.getHandPais());
                    if (actionSeatMap.containsKey(winPlayer.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                        if (actionList != null) {
                            playerRes.addAllRecover(actionList);
                        }
                    }
                }else{
                    playerRes.addAllHandCardIds(player.getHandPais());
                    if (actionSeatMap.containsKey(player.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                        if (actionList != null) {
                            playerRes.addAllRecover(actionList);

                        }
                    }
                }
            }
//            if (isSiRenBoPi()) {
//                playerRes.addExt(shuXingSeat);
//                playerRes.addExt(state.getId());
//            }else{
//            	playerRes.addExt(0);
//                playerRes.addExt(0);
//            }
//            playerRes.addExt(isGoldRoom()?(int)player.loadAllGolds():0);
            players.add(playerRes.build());

            if (autoPlay && player.isCheckAuto()) {
                int timeOut = autoTimeOut;
                if (player.getAutoPlayCheckedTime() >= autoTimeOut && !player.isAutoPlayCheckedTimeAdded()) {
                    timeOut = autoTimeOut2;
                }
                autoCheckTime = timeOut - (int) (System.currentTimeMillis() - player.getLastCheckTime());
            }
        }
        res.addAllPlayers(players);
        if (actionSeatMap.isEmpty()) {
            // int nextSeat = getNextDisCardSeat();
            if (nowDisCardSeat != 0) {
                if (toPlayCardFlag == 1) {
                    res.setNextSeat(nowDisCardSeat);
                } else {
                    res.setNextSeat(0);
                }
            }
        }
        res.addExt(nowDisCardSeat); // 0
        res.addExt(payType);// 1
        //下标二
        res.addExt(ceiling);// 2
		res.addExt(isRedBlack);// 3
        res.addExt(isLianBanker);// 4
        res.addExt(xiTotun);// 5
        res.addExt(modeId.length()>0?Integer.parseInt(modeId):0);//6
		int ratio;
		int pay;
		if (isGoldRoom()){
			ratio = GameConfigUtil.loadGoldRatio(modeId);
			pay = PayConfigUtil.get(playType,totalBureau,getMaxPlayerCount(),payType == 1 ? 0 : 1,modeId);
		}else{
			ratio = 1;
			pay = consumeCards()?loadPayConfig(payType):0;
		}
		res.addExt(ratio);// 7
		res.addExt(pay);// 8
        res.addExt(catCardCount);// 9

        res.addExt(creditMode);     // 10
        res.addExt(creditJoinLimit);// 11
        res.addExt(creditDissLimit);// 12
        res.addExt(creditDifen);    // 13
        res.addExt(creditCommission);// 14
        res.addExt(creditCommissionMode1);// 15
        res.addExt(creditCommissionMode2);// 16
        res.addExt(autoPlay ? 1 : 0);// 17
        res.addExt(jiaBei);// 18
        res.addExt(jiaBeiFen);// 19
        res.addExt(jiaBeiShu);// 20

        res.addTimeOut((isGoldRoom() || autoPlay) ?(int)autoTimeOut:0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ?(int) autoTimeOut2 :0);
        return res.build();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public int randNumber(int number) {
        int ret = 0;
        if (number > 0) {
            ret = (number + 5) / 10 * 10;
        } else if (number < 0) {
            ret = (number - 5) / 10 * 10;
        }

        return ret;
    }

    public int getBopiPoint(SyPaohuziPlayer player) {
        if (!isBoPi()) {
            return 0;
        }

        int selfPoint = 0;
        int otherPoint = 0;
        int retPoint = 0;
        for (SyPaohuziPlayer temp : seatMap.values()) {
            if (player.getUserId() == temp.getUserId()) {
                selfPoint = randNumber(temp.getTotalPoint());
            } else {
                otherPoint += randNumber(temp.getTotalPoint());
            }
        }

        retPoint = selfPoint * (seatMap.size() - 1) - otherPoint;
        return retPoint;
    }

    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, List<Integer> mt, int totalTun, boolean isBreak,Map<Long,Integer> outScoreMap,Map<Long,Integer> ticketMap) {
        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        SyPaohuziPlayer winPlayer = null;
        boolean isBoPi = isBoPi();
        for (SyPaohuziPlayer player : seatMap.values()) {
            int bopiPint = getBopiPoint(player);
            player.setWinLossPoint(bopiPint);//剥皮分
            player.setTotalPoint(over && isBoPi ? randNumber(player.getTotalPoint()) : player.getTotalPoint());//非剥皮分
        }
        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            if(isBoPi){
                for (SyPaohuziPlayer player : seatMap.values()) {
                    if (player.getWinLossPoint() > 0 && player.getWinLossPoint() < jiaBeiFen) {
                        jiaBeiPoint += player.getWinLossPoint() * (jiaBeiShu - 1);
                        player.setWinLossPoint(player.getWinLossPoint() * jiaBeiShu);
                    } else if (player.getWinLossPoint() < 0) {
                        loserCount++;
                    }
                }
                if (jiaBeiPoint > 0) {
                    for (SyPaohuziPlayer player : seatMap.values()) {
                        if (player.getWinLossPoint() < 0) {
                            player.setWinLossPoint(player.getWinLossPoint() - (jiaBeiPoint / loserCount));
                        }
                    }
                }
            }else{
                for (SyPaohuziPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                        jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                        player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                    } else if (player.getTotalPoint() < 0) {
                        loserCount++;
                    }
                }
                if (jiaBeiPoint > 0) {
                    for (SyPaohuziPlayer player : seatMap.values()) {
                        if (player.getTotalPoint() < 0) {
                            player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                        }
                    }
                }
            }
        }

        for (SyPaohuziPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
            int bopiPint = getBopiPoint(player);
//        	player.setWinLossPoint(bopiPint);
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes(isBoPi, true, player.getWinLossPoint());
            } else {
                build = player.bulidOneClosingPlayerInfoRes(isBoPi, false, player.getWinLossPoint());
            }
            if (isSiRenBoPi()) {
                build.setIsShuXing(shuXingSeat);
            }
            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
            for(int action : player.getActionTotalArr()){
            	build.addStrExt(action+"");
    		}
            if (isGoldRoom()){
				build.addStrExt("1");//4
				build.addStrExt(player.loadAllGolds()<=0?"1":"0");//5
				build.addStrExt(outScoreMap==null?"0":outScoreMap.getOrDefault(player.getUserId(),0).toString());//6
			}else{
				build.addStrExt("0");
				build.addStrExt("0");
				build.addStrExt("0");
			}
            build.addStrExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//7

            builderList.add(build);

            //信用分
            if(isCreditTable()){
                if(isBoPi){
                    player.setWinLoseCredit(player.getWinLossPoint() * creditDifen);
                }else{
                    player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
                }
            }
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            int dyjCredit = 0;
            for (SyPaohuziPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                SyPaohuziPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9

                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
                list.add(builder.build());
            }
        } else {
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                builder.addStrExt(0 + ""); //8
                builder.addStrExt(0 + ""); //9
                list.add(builder.build());
            }
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(PaohuziTool.toPhzCardIds(leftCards));
        if (mt != null) {
            res.addAllFanTypes(mt);
        }
        if (winPlayer != null) {
            res.setTun(isBoPi() ? 0 : totalTun);// 剥皮算0等
            res.setFan(winFen);
            res.setHuxi(winPlayer.getTotalHu());
            res.setTotalTun(totalTun);
            res.setHuSeat(winPlayer.getSeat());
            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over));
        res.addAllStartLeftCards(startLeftCards);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (SyPaohuziPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, null, 0, null, 0, true, null,null);
        saveLog(true,0L, res.build());

    }

    public List<String> buildAccountsExt(boolean isOver) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");
        ext.add(masterId + "");
        ext.add(TimeUtil.formatTime(TimeUtil.now()));
        ext.add(playType + "");
        ext.add(getConifg(0) + "");
        ext.add(playBureau + "");
        ext.add(isOver ? 1 + "" : 0 + "");
        ext.add(maxPlayerCount + "");
        ext.add(isGroupRoom() ? "1" : "0");
		ext.add(isOver ? dissInfo() : "");
		//金币场大于0
		ext.add(modeId);
		int ratio;
		int pay;
		if (isGoldRoom()){
			ratio = GameConfigUtil.loadGoldRatio(modeId);
			pay = PayConfigUtil.get(playType,totalBureau,getMaxPlayerCount(),payType == 1 ? 0 : 1,modeId);
		}else{
			ratio = 1;
			pay = loadPayConfig(payType);
		}
		ext.add(String.valueOf(ratio));
		ext.add(String.valueOf(pay>=0?pay:0));
        ext.add(isGroupRoom()?loadGroupId():"");//13
        ext.add(String.valueOf(catCardCount));//14


        //信用分
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//20
        ext.add(jiaBei + "");//22
        ext.add(jiaBeiFen + "");//23
        ext.add(jiaBeiShu + "");//24
        return ext;
    }
	private String dissInfo(){
    	JSONObject jsonObject = new JSONObject();
    	if(getSpecialDiss() == 1){
            jsonObject.put("dissState", "1");//群主解散
        }else{
            if(answerDissMap != null && !answerDissMap.isEmpty()){
                jsonObject.put("dissState", "2");//玩家申请解散
                StringBuilder str = new StringBuilder();
                for(Entry<Integer, Integer> entry : answerDissMap.entrySet()){
                    Player player0 = getSeatMap().get(entry.getKey());
                    if(player0 != null){
                        str.append(player0.getUserId()).append(",");
                    }
                }
                if(str.length()>0){
                    str.deleteCharAt(str.length()-1);
                }
                jsonObject.put("dissPlayer", str.toString());
            }else{
                jsonObject.put("dissState", "0");//正常打完
            }
        }
    	return jsonObject.toString();
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
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
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		return true;
	}
    
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
		return createTable(player,play,bureauCount,params,saveDb);
	}
    
    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
		createTable(player,play,bureauCount,params,true);
	}
    
    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
        long id = getCreateTableId(player.getUserId(), play);
        if (id<=0){
			return false;
		}
        if(saveDb){
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
			this.id=id;
			this.totalBureau=bureauCount;
			this.playBureau=1;
        }
		int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        int payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        int ceiling = StringUtil.getIntValue(params, 10, 0);// 剥皮上限
        int isRedBlack = StringUtil.getIntValue(params, 11, 0);//红黑点
        if (play == PaohuziConstant.play_type_shaoyang){
        	isRedBlack = 1;
        }
        int isLianBanker = StringUtil.getIntValue(params, 12, 0);//可连庄
        int xiTotun = StringUtil.getIntValue(params, 13, 3);//息转屯比例 3三息一屯 5五息一屯
        int catCardCount = StringUtil.getIntValue(params, 14, 0);//除掉的牌数量


        //信用分相关
        this.creditMode = StringUtil.getIntValue(params, 15, 0);
        this.creditJoinLimit = StringUtil.getIntValue(params, 16, 0);
        this.creditDissLimit = StringUtil.getIntValue(params, 17, 0);
        this.creditDifen= StringUtil.getIntValue(params, 18, 0);
        this.creditCommission = StringUtil.getIntValue(params, 19, 0);
        this.creditCommissionMode1 = StringUtil.getIntValue(params, 20, 1);
        this.creditCommissionMode2 = StringUtil.getIntValue(params, 21, 1);
        this.creditCommissionLimit = StringUtil.getIntValue(params, 22, 100);

        this.autoPlay = StringUtil.getIntValue(params, 23, 0) == 1;

        this.jiaBei = StringUtil.getIntValue(params, 24, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 25, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 26, 1);

        if (playerCount<=1||playerCount>4){
            return false;
        }
        if(playerCount == 3 || playerCount == 4){
            catCardCount = 0 ;
        }
        this.catCardCount = catCardCount;
        setMaxPlayerCount(playerCount);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        setPayType(payType);
        if (PaohuziConstant.isPlayBopi(play)){
        	setCeiling(ceiling);
        }
        setIsRedBlack(isRedBlack);
        setIsLianBanker(isLianBanker);
        setXiTotun(xiTotun);
        if (isGoldRoom()){
			try{
				GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
				if (goldRoom!=null){
					modeId = goldRoom.getModeId();
				}
			}catch(Exception e){
			}
			autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhz",10*1000);
		}else{
            if(autoPlay){
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal",60*1000);
                autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal2",10*1000);
            }
        }
        changeExtend();
        LogUtil.msgLog.info("createTable tid:"+getId()+" "+player.getName() + " params"+params.toString());
        return true;
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_paohuzi;
    }

    @Override
    public boolean isTest() {
        return PaohuziConstant.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
        checkMo();
        // PaohuziCheckCardBean checkCard = player.checkCard(card, true);
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if (getSendDissTime() > 0) {
                return;
            }
            if (autoPlay && state == table_state.ready && playedBureau > 0 ) {
                if (++timeNum >= ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoReadyPhzNormal", 10)) {
                    timeNum = 0;
                    for (Player player : playerMap.values()) {
                        ready(player);

                        ready();
                        checkDeal(player.getUserId());

                        boolean isLastStart = false;
                        if (player.getState() == player_state.play) {
                            isLastStart = true;
                        }

                        TableRes.CreateTableRes.Builder res = buildCreateTableRes(player.getUserId(), true, isLastStart).toBuilder();
                        res.setFromOverPop(1);
                        player.writeSocket(res.build());

                        startNext();
                    }
                }
                return;
            }

            int timeout;
            if(state != table_state.play){
                return;
            }else if (isGoldRoom()){
                timeout = autoTimeOut;
            }else if(autoPlay){
                timeout = autoTimeOut;
            }else{
                return;
            }
            //timeout = 10*1000;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
            long now = TimeUtil.currentTimeMillis();
            if(!actionSeatMap.isEmpty()){
                int action = 0,seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()){
                    List<Integer> list = PaohzDisAction.parseToDisActionList(entry.getValue());
                    int minAction = Collections.min(list);
                    if(action == 0){
                        action = minAction;
                        seat = entry.getKey();
                    }else if(minAction < action){
                        action = minAction;
                        seat = entry.getKey();
                    }else if(minAction == action){
                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                        seat = nearSeat;
                    }
                }
                if(action > 0 && seat > 0){
                    SyPaohuziPlayer player = seatMap.get(seat);
                    if (player==null){
                        LogUtil.errorLog.error("auto play error:tableId={},seat={} is null,seatMap={},playerMap={}",id,seat,seatMap.keySet(),playerMap.keySet());
                        return;
                    }

                    boolean auto = player.isAutoPlay();
                    if(!auto){
                        auto = checkPlayerAuto(player,timeout);
                    }
                    if(auto){
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
                            player.setAutoPlayTime(0L);
                            if(action == PaohzDisAction.action_chi){
                                action = PaohzDisAction.action_pass;
                            }
                            if(action == PaohzDisAction.action_pass || action == PaohzDisAction.action_peng || action == PaohzDisAction.action_hu){
                                play(player, new ArrayList<Integer>(), action);
                            }else{
                                checkMo();
                            }
                        }
                        return;
                    }
                    if(action == PaohzDisAction.action_pao && player.getLastCheckTime()>0){
                        checkMo();
                    }
                }
            }else{
                SyPaohuziPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null) {
                    return;
                }
                if(toPlayCardFlag==1){
                    boolean auto = player.isAutoPlay();
                    if(!auto){
                        auto = checkPlayerAuto(player,timeout);
                    }
                    if(auto){
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
                            player.setAutoPlayTime(0L);
                            PaohzCard paohzCard = PaohuziTool.autoDisCard(player.getHandPhzs());
                            if(paohzCard != null){
                                play(player, Arrays.asList(paohzCard.getId()), 0);
                            }
                        }
                    }
                }else{
                    checkMo();
                }
            }
        }
    }

    public boolean checkPlayerAuto(SyPaohuziPlayer player ,int timeout){
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        if (player.getLastCheckTime() > 0) {
            int checkedTime = (int) (now - player.getLastCheckTime());
            if (checkedTime > 10 * 1000) {
                player.addAutoPlayCheckedTime(1 * 1000);
                if (!player.isAutoPlayCheckedTimeAdded()) {
                    player.setAutoPlayCheckedTimeAdded(true);
                    player.addAutoPlayCheckedTime(10 * 1000);
                }
                if(!player.isAutoPlayChecked() && player.getAutoPlayCheckedTime() >= timeout){
                    // 推送消息
                    ComMsg.ComRes msg = SendMsgUtil.buildComRes(133, player.getSeat(), (int) player.getUserId()).build();
                    broadMsg(msg);
                    broadMsg0(msg);
                    auto = true;

                }
            }
            if (checkedTime >= timeout) {
                auto = true;
            }
            if(auto){
                player.setAutoPlay(true, this);
            }
//            System.out.println("checkPlayerAuto----" + player.getSeat() + "|" + player.getUserId() + "|" + player.getAutoPlayCheckedTime() + "|" + checkedTime + "|" + auto);
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return SyPaohuziPlayer.class;
    }

    public PaohzCard getNextCard(int val) {
        if (this.leftCards.size() > 0) {
            Iterator<PaohzCard> iterator = this.leftCards.iterator();
            PaohzCard find = null;
            while (iterator.hasNext()) {
                PaohzCard paohzCard = iterator.next();
                if (paohzCard.getVal() == val) {
                    find = paohzCard;
                    iterator.remove();
                    break;
                }
            }
            dbParamMap.put("leftPais", JSON_TAG);
            return find;
        }
        return null;
    }

    public PaohzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            PaohzCard card = this.leftCards.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return card;
        }
        return null;
    }

    public List<PaohzCard> getLeftCards() {
        return leftCards;
    }

    public void setLeftCards(List<PaohzCard> leftCards) {
        if (leftCards == null) {
            this.leftCards.clear();
        } else {
            this.leftCards = leftCards;

        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    public void setStartLeftCards(List<Integer> startLeftCards) {
        if (startLeftCards == null) {
            this.startLeftCards.clear();
        } else {
            this.startLeftCards = startLeftCards;

        }
        changeExtend();
    }

    public int getMoSeat() {
        return moSeat;
    }

    public void setMoSeat(int lastMoSeat) {
        this.moSeat = lastMoSeat;
        changeExtend();
    }

    public List<PaohzCard> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<PaohzCard> nowDisCardIds) {
        this.nowDisCardIds = nowDisCardIds;
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
     * 打出的牌是刚刚摸的
     */
    public boolean isMoFlag() {
        return moFlag == 1;
    }

    public void setMoFlag(int moFlag) {
        if (this.moFlag != moFlag) {
            this.moFlag = moFlag;
            changeExtend();
        }
    }

    public void markMoSeat(int seat, int action) {
        checkMoMark = new KeyValuePair<>();
        checkMoMark.setId(seat);
        checkMoMark.setValue(action);
        changeExtend();
    }

    private void clearMarkMoSeat() {
        checkMoMark = null;
        changeExtend();
    }

    public void markMoSeat(PaohzCard card, int seat) {
        moSeatPair = new KeyValuePair<>();
        if (card != null) {
            moSeatPair.setId(card.getId());
        }
        moSeatPair.setValue(seat);
        changeExtend();
    }

    public void clearMoSeatPair() {
        moSeatPair = null;
    }

    // public boolean checkMo

    public int getToPlayCardFlag() {
        return toPlayCardFlag;
    }

    public void setToPlayCardFlag(int toPlayCardFlag) {
        if (this.toPlayCardFlag != toPlayCardFlag) {
            this.toPlayCardFlag = toPlayCardFlag;
            changeExtend();
        }

    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    public PaohzCard getZaiCard() {
        return zaiCard;
    }

    public void setZaiCard(PaohzCard zaiCard) {
        this.zaiCard = zaiCard;
        changeExtend();
    }

    public int getSendPaoSeat() {
        return sendPaoSeat;
    }

    public void setSendPaoSeat(int sendPaoSeat) {
        if (this.sendPaoSeat != sendPaoSeat) {
            this.sendPaoSeat = sendPaoSeat;
            changeExtend();
        }

    }


//	public int calcNeedRoomCards(int needCard, int playerCount) {
//		long endTime = 1482940800000L;// 2016-12-29 00:00:00
//		long nowTime = TimeUtil.currentTimeMillis();
//		if (nowTime < endTime) {
//			return 0;
//		}
//		if (isSiRenBoPi()) {
//			needCard = 2;
//		} else if (isBoPi()) {
//			needCard = 1;
//		}
//		super.calcNeedRoomCards(needCard, playerCount);
//		return needCard;
//	}

    public boolean isBoPi() {
        return playType == PaohuziConstant.play_type_bopi;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }

    public boolean isFirstCard() {
        return firstCard;
    }

    public void setFirstCard(boolean firstCard) {
        this.firstCard = firstCard;
        changeExtend();
    }

    /**
     * 对应的座位cardId-seat
     */
    public KeyValuePair<Integer, Integer> getMoSeatPair() {
        return moSeatPair;
    }

    public PaohzCard getBeRemoveCard() {
        return beRemoveCard;
    }

    /**
     * 桌子上移除的牌
     */
    public void setBeRemoveCard(PaohzCard beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }

    /**
     * 是否是该玩家摸的牌
     */
    public boolean isMoByPlayer(SyPaohuziPlayer player) {
        if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                if (nowDisCardIds.get(0).getId() == moSeatPair.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
        changeExtend();
    }

    public int getShuXingSeat() {
        return shuXingSeat;
    }

    public void setShuXingSeat(int shuXingSeat) {
        this.shuXingSeat = shuXingSeat;
    }

    public boolean isSiRenBoPi() {
        return isBoPi() && 4 == getMaxPlayerCount();
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
                            Object... objects) throws Exception {
//        // 添加剥皮上限
//        if (PaohuziConstant.isPlayBopi(play)) {
//            if (params.size()>= 11) {
//                setCeiling(params.get(10));
//            }
////            setCeiling(150);
//        }
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public boolean isCanJoin0(Player player) {

        int needCard = 0;
        if (consumeCards()) {
            if (payType == 1) {
                //AA制
                needCard = PayConfigUtil.get(playType, getTotalBureau(), getMaxPlayerCount(), 0,null);
            }
        }
        // 如果玩家的钻石小于玩一局需要的钻石，则返回
        if (checkPay&&(needCard < 0 || needCard>0&&player.getFreeCards() + player.getCards() < needCard) && !player.isRobot() && !GameConfigUtil.freeGame(playType,player.getUserId())) {
            player.writeErrMsg(LangMsg.code_diamond_err);
            return false;
        }
        return true;
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

            for (SyPaohuziPlayer player:playerMap.values()){
                //总小局数
                DataStatistics dataStatistics1=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"xjsCount",playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1,3);
                int finalPoint;
                if (isBoPi()) {
                    finalPoint = getBopiPoint(player);
                } else {
                    finalPoint = player.loadScore();
                }

                //总大局数
                DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"djsCount",1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,3);
                //总积分
                DataStatistics dataStatistics6=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"zjfCount",finalPoint);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6,3);

                if (finalPoint >0){
                    if (finalPoint >maxPoint){
                        maxPoint= finalPoint;
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"winMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2,4);
                }else if (finalPoint <0){
                    if (finalPoint <minPoint){
                        minPoint= finalPoint;
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"loseMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3,5);
                }
            }

            for (SyPaohuziPlayer player:playerMap.values()){
                int finalPoint;
                if (isBoPi()) {
                    finalPoint = getBopiPoint(player);
                } else {
                    finalPoint = player.loadScore();
                }
                if (maxPoint>0&&maxPoint== finalPoint){
                    //单大局大赢家
                    DataStatistics dataStatistics4=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dyjCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4,1);
                }else if (minPoint<0&&minPoint== finalPoint){
                    //单大局大负豪
                    DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dfhCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,2);
                }
            }
        }
    }

    public long saveUserGroupPlaylog() {
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (SyPaohuziPlayer player : seatMap.values()) {
            players += player.getUserId() + ",";
            if (isBoPi()) {
                score += player.getWinLossPoint() + ",";
                diFenScore += player.getWinLossPoint() + ",";
            } else {
                score += player.getTotalPoint() + ",";
                diFenScore += player.getTotalPoint() + ",";
            }
        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(maxPlayerCount);
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    @Override
    public String getGameName() {
        String res = "";
        if (playType == 32) {
            res = "邵阳字牌";
        } else if (playType == 33) {
            res = "邵阳剥皮";
        }
        return res;
    }


    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public int getAutoTimeOut2() {
        return autoTimeOut2;
    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 15 && StringUtil.getIntValue(params, 15, 0) == 1;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_shaoyang,GameUtil.play_type_bopi);

    public static void loadWanfaTables(Class<? extends BaseTable> cls){
        for (Integer integer:wanfaList){
            TableManager.wanfaTableTypesPut(integer,cls);
        }
    }
}
