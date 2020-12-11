package com.sy599.game.qipai.doudizhu.bean;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.AhGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.doudizhu.constant.DdzConstants;
import com.sy599.game.qipai.doudizhu.rule.CardType;
import com.sy599.game.qipai.doudizhu.tool.CardTool;
import com.sy599.game.qipai.doudizhu.tool.CardTypeTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DdzTable extends BaseTable{
    private static final int JSON_TAG = 1;
    /*** 当前牌桌上出的牌 */
    private List<Integer> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, DdzPlayer> playerMap = new ConcurrentHashMap<Long, DdzPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, DdzPlayer> seatMap = new ConcurrentHashMap<Integer, DdzPlayer>();
    /*** 最大玩家数量 */
    private int max_player_count = 3;
    /*** 发牌位置 */
    private int fapaiSeat = 0;
    /*** 切牌 */
    private int cutCard = 0;
    /*** 叫地主，抢地主的集合 */
    private Map<Integer, Integer> robLandLordMap = new LinkedHashMap<>();
    /*** 地主的位置 */
    private int landLord = 0;
    /*** 斗地主阶段 */
    private DdzPhase phase = DdzPhase.entry;
    /*** 桌子当局的倍数 */
    private int ratio = 1;
    /*** 桌子的底牌 */
    private List<Integer> underCards = new ArrayList<>();
    /*** 癞子牌 */
    private int magnaCardValue = 0;
    /*** 桌子的底分 */
    private int underPoint = 1;
    /*** 地主出牌次数 */
    private int landlordPlayTimes = 0;
    /*** 最大抢庄倍率 */
    private int max_rob_ratio = 3;
    /*** 三带一*/
    private int canThreeAndOne = 1;
    /*** 三带二*/
    private int canThreeAndTwo = 1;
    /***开启Gps*/
    private int isOpenGps = 0;
    /*** 上家的牌型*/
    private int nowDisCardType;
    /*** 匿名功能*/
    private int anonymous = 0;

    /**
     * 托管时间
     */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;

    private int currentCount = 1;//发牌次数

    public int getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(int anonymous) {
        this.anonymous = anonymous;
    }

    public int getNowDisCardType() {
        return nowDisCardType;
    }

    public void setNowDisCardType(int nowDisCardType) {
        this.nowDisCardType = nowDisCardType;
    }

    public boolean canThreeAndTwo() {
        return canThreeAndTwo == 1;
    }

    public void setCanThreeAndTwo(int canThreeAndTwo) {
        this.canThreeAndTwo = canThreeAndTwo;
    }

    public boolean canThreeAndOne() {
        return canThreeAndOne == 1;
    }

    public void setCanThreeAndOne(int canThreeAndOne) {
        this.canThreeAndOne = canThreeAndOne;
    }

    public int getIsOpenGps() {
        return isOpenGps;
    }

    public void setIsOpenGps(int isOpenGps) {
        this.isOpenGps = isOpenGps;
        changeExtend();
    }

    public enum DdzPhase {
        entry(0), ready(1), robBanker(2), bet(3), play(4), over(5);
        private int id;

        DdzPhase(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static DdzPhase getPhase(int id) {
            for (DdzPhase phase : DdzPhase.values()) {
                if (phase.getId() == id) {
                    return phase;
                }
            }
            return null;
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
        }
        if (isMatchRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutDdz", 15 * 1000);
        } else if (isGoldRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutDdz", 30 * 1000);
        }
    }

    public int getLandlordPlayTimes() {
        return landlordPlayTimes;
    }

    public void setLandlordPlayTimes(int landlordPlayTimes) {
        this.landlordPlayTimes = landlordPlayTimes;
        changeExtend();
    }

    public int getUnderPoint() {
        return underPoint;
    }

    public void setUnderPoint(int underPoint) {
        this.underPoint = underPoint;
    }

    public int getMagnaCardValue() {
        return magnaCardValue;
    }

    public void setMagnaCardValue(int magnaCardValue) {
        this.magnaCardValue = magnaCardValue;
        changeExtend();
    }

    public List<Integer> getUnderCards() {
        return underCards;
    }

    public void setUnderCards(List<Integer> underCards) {
        this.underCards = underCards;
        changeExtend();
    }

    public void changeRatio(int ratio) {
        this.ratio *= ratio;
        changeExtend();
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public DdzPhase getPhase() {
        return phase;
    }

    public void setPhase(DdzPhase phase) {
        this.phase = phase;
        changeExtend();
    }

    public int getLandLord() {
        return landLord;
    }

    public void setLandLord(int lordLand) {
        this.landLord = lordLand;
        changeExtend();
    }

    public Map<Integer, Integer> getRobLandLordMap() {
        return robLandLordMap;
    }

    public void setRobLandLordMap(Map<Integer, Integer> robLandLordMap) {
        this.robLandLordMap = robLandLordMap;
    }

    public int getCutCard() {
        return cutCard;
    }

    public void setCutCard(int cutCard) {
        this.cutCard = cutCard;
    }

    public int getFapaiSeat() {
        return fapaiSeat;
    }

    public void setFapaiSeat(int fapaiSeat) {
        this.fapaiSeat = fapaiSeat;
        changeExtend();
    }

    public long getId() {
        return id;
    }

    public DdzPlayer getPlayer(long id) {
        return playerMap.get(id);
    }

    /**
     * 一局结束
     */
    public void calcOver() {
        DdzPlayer winPlayer = null;
        int winPoint = ratio;
        int goldRatio = 1;
        int goldPay = 0;

        boolean isOver;

        Map<Long, Integer> ticketMap = new HashMap<>();
        Map<Long, Integer> outScoreMap = new HashMap<>();
        Map<Long, Integer> scoreMap = new HashMap<>();

        boolean isGold = isGoldRoom();
        if (isGold){
            isOver = true;

            try {
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
                if (goldRoom != null) {
                    modeId = goldRoom.getModeId();
                    goldPay = PayConfigUtil.get(playType, goldRoom.getGameCount(), goldRoom.getMaxCount(), 0, goldRoom.getModeId());
                    if (goldPay < 0) {
                        goldPay = 0;
                    }
                    goldRatio = GameConfigUtil.loadGoldRatio(modeId);
                }
            } catch (Exception e) {
            }
        }else {
            isOver = playBureau >= totalBureau;
        }

            if (isTwoPlayer()) {
                int tempLostPoint = 0;
                for (DdzPlayer player : playerMap.values()) {
                    player.changeState(player_state.over);
                    if (player.getHandPais().isEmpty()) {
                        winPlayer = player;
                        scoreMap.put(player.getUserId(),winPoint * goldRatio);
//                        player.calcWin(1, winPoint);
                    } else {
                        int tempPoint = -winPoint * goldRatio;
                        if (isGold&&(goldPay-tempPoint>player.loadAllGolds())){
                            tempPoint =(int) -player.loadAllGolds()+goldPay;
                            if (tempPoint>0){
                                tempPoint=0;
                            }
                        }
                        tempLostPoint+=tempPoint;

                        scoreMap.put(player.getUserId(),tempPoint);
//                        player.calcLost(1, -winPoint);
                    }
                }
                scoreMap.put(winPlayer.getUserId(),-tempLostPoint);
            } else {
                int tempLostPoint = 0;
                for (DdzPlayer player : seatMap.values()) {
                    player.changeState(player_state.over);
                    int left = player.getHandPais().size();
                    if (left == 0) {
                        winPlayer = player;
                    } else {
                    }
                }
                if (winPlayer == null) {
                    return;
                }
                boolean isLandLordWin = winPlayer.getSeat() == landLord;
                if (isLandLordWin) {
                    for (DdzPlayer player : seatMap.values()) {
                        if (player.getSeat() == landLord) {
                            scoreMap.put(player.getUserId(),winPoint*2 * goldRatio);
//                            player.calcWin(1, winPoint * 2);
                        } else {
                            int tempPoint = -winPoint * goldRatio;
                            if (isGold&&(goldPay-tempPoint>player.loadAllGolds())){
                                tempPoint =(int) -player.loadAllGolds()+goldPay;
                                if (tempPoint>0){
                                    tempPoint=0;
                                }
                            }
                            tempLostPoint+=tempPoint;

                            scoreMap.put(player.getUserId(),tempPoint);
//                            player.calcLost(1, -winPoint);
                        }
                    }
                    scoreMap.put(seatMap.get(landLord).getUserId(),-tempLostPoint);
                } else {
                    for (DdzPlayer player : seatMap.values()) {
                        if (player.getSeat() == landLord) {
                            int tempPoint = - 2*winPoint * goldRatio;
                            if (isGold&&(goldPay-tempPoint>player.loadAllGolds())){
                                tempPoint =(int) -player.loadAllGolds()+goldPay;
                                if (tempPoint>0){
                                    tempPoint=0;
                                }
                            }
                            tempLostPoint+=tempPoint;

                            scoreMap.put(player.getUserId(),tempPoint);
//                            player.calcLost(1, -winPoint * 2);
                        } else {
                            scoreMap.put(player.getUserId(),winPoint);
//                            player.calcWin(1, winPoint);
                        }
                    }

                    if (isGold) {
                        int i = 0;
                        for (DdzPlayer player : seatMap.values()) {
                            if (player.getSeat() != landLord) {
                                if (i == 0) {
                                    scoreMap.put(player.getUserId(), -tempLostPoint / 2);
                                } else {
                                    scoreMap.put(player.getUserId(), -tempLostPoint - (-tempLostPoint / 2));
                                }
                                i++;
                            }
                        }
                    }

                }
            }

            if (isGold){
                for (Map.Entry<Long,Integer> kv : scoreMap.entrySet()){
                    int score = kv.getValue().intValue();
                    DdzPlayer player = playerMap.get(kv.getKey());
                    if (score>0){
                        player.calcWin(this,1,score);

                        int tempWin;
                        long allGold = player.loadAllGolds();
                        if (score>allGold){
                            tempWin = (int) allGold;
                            outScoreMap.put(player.getUserId(),(int)(score-allGold));

                            LogUtil.msgLog.info("out player gold:tableId={},playerId={},modeId={},allGold={},{}(win)={}(get)+{}(out),goldPay={}"
                                    , id, player.getUserId(), modeId, allGold, score, allGold, score-allGold, goldPay);
                        }else{
                            tempWin = score;
                        }
                        player.changeGold((tempWin - goldPay), playType);

                        Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                        if (tmpConfig != null && tmpConfig.intValue() > 0) {
                            int ticketCount = (int) (tempWin / (tmpConfig.intValue()));
                            if (ticketCount > 0) {
                                ticketMap.put(player.getUserId(),ticketCount);
                                UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                        String.valueOf(player.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                                LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}", modeId, player.getUserId(), ticketCount);
                            }
                        }
                        player.updateGoldRoomActivity(true);
                    }else{
                        player.calcLost(this,1,score);
                        player.changeGold(-goldPay + score, playType);

                        player.updateGoldRoomActivity(false);
                    }
                }
            }else{
                for (Map.Entry<Long,Integer> kv : scoreMap.entrySet()){
                    int score = kv.getValue().intValue();
                    DdzPlayer player = playerMap.get(kv.getKey());
                    if (score>0){
                        player.calcWin(this,1,score);
                    }else{
                        player.calcLost(this,1,score);
                    }
                }
            }

        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, winPlayer, false, outScoreMap,ticketMap);
        saveLog(isOver, winPlayer.getUserId(), res.build());
        setLastWinSeat(winPlayer.getSeat());
        for (DdzPlayer player : playerMap.values()) {
            player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index4, 1);
        }

        calcAfter();
        if (isOver) {
            calcOver1();
            calcOver2();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
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
//        userLog.setStartseat(lastWinSeat);
        userLog.setStartseat(fapaiSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (DdzPlayer player : playerMap.values()) {
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
//            TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        wrapper.putInt(1, phase.getId());
        wrapper.putInt(2, max_player_count);
        wrapper.putString(3, JacksonUtil.writeValueAsString(robLandLordMap));
        wrapper.putInt(4, landLord);
        wrapper.putInt(5, ratio);
        wrapper.putString(6, JacksonUtil.writeValueAsString(underCards));
        wrapper.putInt(7, magnaCardValue);
        wrapper.putInt(8, landlordPlayTimes);
        wrapper.putInt(9, fapaiSeat);
//        wrapper.putInt(10, payType);
        wrapper.putInt(11, canThreeAndOne);
        wrapper.putInt(12, canThreeAndTwo);
        wrapper.putInt(13, underPoint);
        wrapper.putInt(14, isOpenGps);
        return wrapper;
    }

    protected String buildPlayersInfo() {
        StringBuffer sb = new StringBuffer();
        for (DdzPlayer ddzPlayer : playerMap.values()) {
            sb.append(ddzPlayer.toInfoStr()).append(";");
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
            return landLord;
        } else {
            if (nowDisCardSeat != 0) {
                seat = nowDisCardSeat >= max_player_count ? 1 : nowDisCardSeat + 1;
            }
        }
        return seat;
    }

    public DdzPlayer getPlayerBySeat(int seat) {
        int next = seat >= max_player_count ? 1 : seat + 1;
        return seatMap.get(next);

    }

    @SuppressWarnings("unchecked")
    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        // 0
        res.addExt(phase.getId());
        List<PlayerInTableRes> players = new ArrayList<>();
        for (DdzPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes == null) {
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
        }
        res.addAllPlayers(players);

        int nextSeat = 0;
        if (phase == DdzPhase.robBanker) {
            nextSeat = nowDisCardSeat;
        } else if (phase == DdzPhase.play) {
            nextSeat = getNextDisCardSeat();
        }
        res.setNextSeat(nextSeat);
        res.setRenshu(this.max_player_count);

        //1 桌子的倍数
        res.addExt(ratio);
        //2 癞子牌值
        res.addExt(magnaCardValue);
        //3 房费支付方式
        res.addExt(payType);
        //4 三带一
        res.addExt(canThreeAndOne);
        //5 三带二 下标五
        res.addExt(canThreeAndTwo);
        //6 Gps
        res.addExt(isOpenGps);
        buildCreateTableRes1(res);
        //11 匿名
        res.addExt(anonymous);
        res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);//12
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = consumeCards() ? loadPayConfig(payType) : 0;
        }

        res.addExt(ratio);//13
        res.addExt(pay);//14

        res.addTimeOut(isGoldRoom() ? autoTimeOut : 0);
        return res.build();
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (DdzPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }

    public void notLet(DdzPlayer player) {
        // 当前操作玩家的位置
        setNowDisCardSeat(player.getSeat());
        // 玩家不要
        player.setIsNoLet(1);
        List<Integer> cards = new ArrayList<>();
        cards.add(0);
        player.addOutPais(cards);
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        // 不要
        res.setIsPlay(1);
        if (player.getHandPais().size() == 1) {
            // 报单
            res.setIsBt(1);
        }

        for (DdzPlayer ddzPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            boolean canPlay = CardTypeTool.ifCanPlay(ddzPlayer.getHandPais(), nowDisCardIds, magnaCardValue);
            if (disCardSeat == ddzPlayer.getSeat() || canPlay) {
                copy.setIsLet(1);
            }
            ddzPlayer.writeSocket(copy.build());
        }
    }

    /**
     * 出牌
     */
    public void disCards(Player player0, List<Integer> list, int action) {
        DdzPlayer player = (DdzPlayer) player0;
        List<Integer> cards = new ArrayList<>(list);
        if (player.getSeat() == landLord) {
            ++landlordPlayTimes;
        }

        if (disCardSeat == player.getSeat()) {
            // 清除要不起的状态
            clearIsNotLet();
        } else {
            // 1要不起，0要得起
            player.setIsNoLet(0);
        }
        if (action == 0) {
//            LogUtil.e("action == 0");
            CardType cardType = CardTypeTool.jugdeType(cards);
            action = cardType.getType();
        }

        // if (cardType.getType() == 6) {
        if (action == CardType.c111222.getType() || action == CardType.c11122234.getType() || action == CardType.c1112223344.getType()) {
            // 飞机
            player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index5, 1);
        }
        setDisCardSeat(player.getSeat());
        player.addOutPais(cards);
        setNowDisCardIds(cards);
        setNowDisCardType(action);
        setNowDisCardSeat(player.getSeat());

        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.addAllCardIds(getNowDisCardIds());
        res.setCardType(action);
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        // 0表示要不起，1表示要的起，2表示出完了？
        res.setIsPlay(2);
        // 报单
        int size = player.getHandPais().size();
        if (size == 1 || size == 2 || size == 3) {
            res.setIsBt(size);
        }

        boolean isOver = size == 0;
        for (DdzPlayer ddzPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if (ddzPlayer.getUserId() == player.getUserId()) {
                ddzPlayer.writeSocket(copy.build());
                continue;
            }

            if (isOver) {
                // 如果玩家出完了最后一张牌，不需要提示要不起
                copy.setIsLet(1);

            } else {
                boolean canPlay = CardTypeTool.ifCanPlay(ddzPlayer.getHandPais(), nowDisCardIds, magnaCardValue);
                if (canPlay) {
                    copy.setIsLet(1);
                    // ddzPlayer.setIsNoLet(0);
                } else {
                    // 记录当前的状态
                    // ddzPlayer.setIsNoLet(1);
                    copy.setIsLet(0);
                }
                // copy.setIsLet(1);
                // let = true;
                // ddzPlayer.setIsNoLet(0);
            }
            ddzPlayer.writeSocket(copy.build());
        }
        if (action == CardType.c4.getType() || action == CardType.c1617.getType() || action == CardType.c666.getType()) {
            player.changeBoomCount(1);
            // 炸弹翻两倍
            changeRatio(2);
            sendRatioMsg();
        }
        if (isOver) {
            int spring = checkSpring();
            if (spring != 0) {
                changeRatio(2);
                sendRatioMsg();
                ComRes build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_spring, spring).build();
                broadMsg(build);
            }
            state = table_state.over;
        }

    }

    /**
     * 检查春天,反春天
     */
    private int checkSpring() {
        if (landlordPlayTimes == 1) {
            // 反春
            return 2;
        }
        // 春天
        boolean isSpring = true;
        for (DdzPlayer player : playerMap.values()) {
            if (player.getSeat() == landLord) {
                continue;
            }
            if (player.getHandPais().size() != 17) {
                isSpring = false;
            }
        }
        if (isSpring) {
            return 1;
        }
        return 0;
    }

    /**
     * 清理不要的状态
     */
    public void clearIsNotLet() {
        for (DdzPlayer player : seatMap.values()) {
            player.setIsNoLet(0);
            // player.writeComMessage(code, params);
        }
    }

    /**
     * 打牌
     */
    public void playCommand(DdzPlayer player, List<Integer> cards, int action) {
        synchronized (this) {
            if (player.getSeat() == disCardSeat && cards.isEmpty() || state!=table_state.play) {
                return;
            }

            addPlayLog(player.getSeat(), cards, ",");

            changeDisCardRound(1);
            if (cards != null && !cards.isEmpty()) {
                // 出牌了
                disCards(player, cards, action);
            } else {
                // 不要
                notLet(player);
            }
            // /////////////////////////////////////////////////////
            setLastActionTime(TimeUtil.currentTimeMillis());
            if (isTest() && !isOver()) {
                DdzPlayer next = seatMap.get(getNextDisCardSeat());
                if (next.getUserId() < 0) {
                    // 机器人
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    List<Integer> oppo = null;
                    if (disCardSeat != next.getSeat()) {
                        oppo = getNowDisCardIds();
                    }
                    List<Integer> curList = next.getHandPais();
                    if (curList.isEmpty()) {
                        return;
                    }
                    List<Integer> list = null;
                    if (getMagnaCardValue() > 0) {
                        list = CardTypeTool.getBestAI2(curList, oppo, getNowDisCardType(), getMagnaCardValue());
                    } else {
                        list = CardTypeTool.getBestAI(curList, oppo);
                    }
                    playCommand(next, list, 0);
                }
            }

            if (isOver()) {
                calcOver();
            } else {
                int nextSeat = calcNextSeat(player.getSeat());
                DdzPlayer nextPlayer = seatMap.get(nextSeat);
                if (!nextPlayer.isRobot()) {
                    nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + autoTimeOut);
                }
            }
        }
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
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
        for (DdzPlayer player : seatMap.values()) {
            if (player.getIsEntryTable() != DdzConstants.table_online) {
                // 通知其他人离线
                broadIsOnlineMsg(player, player.getIsEntryTable());
                return 2;
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
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
        robLandLordMap.clear();
        setNowDisCardIds(null);
        setRatio(1);
        setMagnaCardValue(0);
        setPhase(DdzPhase.over);
        setLandlordPlayTimes(0);
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
        // int nextSeat = getNextDisCardSeat();
        for (Player tablePlayer : getSeatMap().values()) {
            // if (userId == tablePlayer.getUserId()) {
            // continue;
            // }
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(fapaiSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            tablePlayer.writeSocket(res.build());
        }

    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            if (getPhase() == DdzPhase.robBanker) {
                DdzPlayer next = seatMap.get(getNowDisCardSeat());
                if (next != null && next.getUserId() < 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LogUtil.e("robotDealAction rob err", e);
                    }
                    int action = getMaxRobPoint()+1;
                    rob(next, action);
                }
            } else {
                DdzPlayer next = seatMap.get(getNextDisCardSeat());
                if (next != null && next.getUserId() < 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LogUtil.e("robotDealAction err", e);
                    }
                    List<Integer> oppo = getNowDisCardIds();
                    List<Integer> curList = next.getHandPais();
                    List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
                    if (list != null) {
                        playCommand(next, list, 0);
                    } else {
                        setNowDisCardSeat(next.getSeat());
                    }
                }
            }
        }
    }

    @Override
    public void ready() {
    }

    @Override
    public synchronized void checkDeal(long userId) {
        if (isAllReady()) {
            // 发公牌，发手牌
            fapai();

            setLastActionTime(TimeUtil.currentTimeMillis());
            for (int i = 1; i <= getMaxPlayerCount(); i++) {
                Player player = getSeatMap().get(i);
                addPlayLog(StringUtil.implode(player.getHandPais(), ","));
            }

            if (playType == DdzConstants.ddz_two) {
                // 第一把房主先叫，随后轮流叫地主
                if (playBureau == 1) {
                    setFapaiSeat(playerMap.get(masterId).getSeat());
                } else {
                    int tempSeat = 0;
                    for (int seat : seatMap.keySet()) {
                        if (seat == fapaiSeat) {
                            continue;
                        }
                        tempSeat = seat;
                    }
                    setFapaiSeat(tempSeat);
                }
                // 当前出牌玩家
                setNowDisCardSeat(fapaiSeat);
            } else {
                if (playBureau == 1) {
                    // 发到公牌的玩家先选择是否叫地主
                    int fapaiSeat = checkFapaiSeat();
                    setFapaiSeat(fapaiSeat);
                    setNowDisCardSeat(fapaiSeat);
                    // 添加公牌记录
                    addPlayLog(fapaiSeat, "" + DdzConstants.play_publicCard, "" + cutCard);
                } else {
                    setFapaiSeat(lastWinSeat);
                    setNowDisCardSeat(fapaiSeat);
                }
            }

            // 发牌msg
            sendDealMsg(userId);
            // 进入抢地主阶段
            setPhase(DdzPhase.robBanker);
            ComRes build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_phase, phase.getId()).build();
            broadMsg(build);
            if (DdzConstants.isTest) {
                robotDealAction();
            }
        } else {
            robotDealAction();
        }
    }

    /**
     * 检查发牌的位置
     */
    private int checkFapaiSeat() {
        int seat = 0;
        for (DdzPlayer player : playerMap.values()) {
            if (player.getHandPais().contains(cutCard)) {
                seat = player.getSeat();
                break;
            }
        }
        return seat;
    }

    /**
     * 发牌
     */
    protected void deal() {
        List<List<Integer>> lists;
        if (playType == DdzConstants.ddz_two) {
            List<Integer> cardIds = new ArrayList<>(DdzConstants.cardList_16);
            cardIds.removeAll(DdzConstants.List_34);
            Collections.shuffle(cardIds);
            for (int i = 0; i < cardIds.size() && i < 9; i++) {
                cardIds.remove(i);
            }
            lists = CardTool.fapai(cardIds, max_player_count, zp);
        } else {
            if (playBureau == 1) {
                // 发公牌
                List<Integer> cardIds = cutCard();
                // 发牌
                lists = CardTool.fapai(cardIds, max_player_count, zp);
            } else {
                // 发牌
                lists = CardTool.fapai(max_player_count, playType, zp);
            }
        }

        int i = 0;
        for (DdzPlayer tablePlayer : seatMap.values()) {
            List<Integer> list = lists.get(i);
            CardTypeTool.setOrder(list);
            tablePlayer.dealHandPais(list);
            ++i;
        }
        setUnderCards(lists.get(i));

        if (playedBureau <= 0) {
            for (DdzPlayer player : playerMap.values()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }

        if (isGoldRoom()) {
//            if (playedBureau == 0 || lastWinSeat <= 0) {
//                List<Long> list0 = new ArrayList<>(3);
//                try {
//                    List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(), id);
//                    if (list != null) {
//                        for (HashMap<String, Object> map : list) {
//                            if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult", "0")), 0) > 0) {
//                                list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId", "0")), 0));
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                }
//
//                if (list0.size() > 0) {
//                    Long userId = list0.get(new SecureRandom().nextInt(list0.size()));
//                    Player player = playerMap.get(userId);
//                    if (player != null) {
//                        setLastWinSeat(player.getSeat());
//                    }
//                }
                if (lastWinSeat <= 0) {
                    setLastWinSeat(new SecureRandom().nextInt(playerMap.size())+1);
                }
//            }
        }
    }

    @SuppressWarnings("unchecked")
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
        return createTable(player, play, bureauCount, saveDb, new Object[0]);
    }

    public boolean createTable(Player player, int play, int bureauCount,boolean saveDb, Object... objects) throws Exception {
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
        } else {
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id = id;
            this.totalBureau = bureauCount;
            this.playBureau = 1;
        }

        setPhase(DdzPhase.entry);
        setLastActionTime(TimeUtil.currentTimeMillis());
        if (objects.length >= 1) {
            int playerCount = (int) objects[0];
            setMaxPlayerCount(playerCount);

            if (objects.length >= 3) {
                setPayType((int) objects[2]);

                if (objects.length >= 4) {
                    setIsOpenGps((int) objects[3]);
                }
            }
        }

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
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutDdz", 15 * 1000);
            } else if (isGoldRoom()) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutDdz", 30 * 1000);
            }
        }
        changeExtend();

        return true;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
        createTable(player,play,bureauCount,true,objects);
    }

    @Override
    protected void initNowAction(String nowAction) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void initExtend0(JsonWrapper wrapper) {
        int phaseId = wrapper.getInt(1, 0);
        phase = DdzPhase.getPhase(phaseId);
        max_player_count = wrapper.getInt(2, 3);
        String robStr = wrapper.getString(3);
        if (robStr != null && !robStr.isEmpty()) {
            robLandLordMap = JacksonUtil.readValue(robStr, Map.class);
        }
        landLord = wrapper.getInt(4, 0);
        ratio = wrapper.getInt(5, 1);
        String underCardsStr = wrapper.getString(6);
        if (underCardsStr != null && !underCardsStr.isEmpty()) {
            underCards = JacksonUtil.readValue(underCardsStr, List.class);
        }
        magnaCardValue = wrapper.getInt(7, 0);
        landlordPlayTimes = wrapper.getInt(8, 0);
        fapaiSeat = wrapper.getInt(9, 0);
//        payType = wrapper.getInt(10, 1);
        canThreeAndOne = wrapper.getInt(11, 1);
        canThreeAndTwo = wrapper.getInt(12, 1);
        underPoint = wrapper.getInt(13, 1);
        isOpenGps = wrapper.getInt(14, 0);
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
     * @param over      是否已经结束
     * @param winPlayer 赢的玩家
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak, Map<Long, Integer> outScoreMap,Map<Long, Integer> ticketMap) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
        if (winPlayer != null) {
            for (DdzPlayer player : seatMap.values()) {
                if (player.getUserId() == winPlayer.getUserId()) {
                    continue;
                }
                if (minPoint == 0 || player.getPoint() < minPoint) {
                    minPoint = player.getPlayPoint();
                    minPointSeat = player.getSeat();
                }
            }
        }

        for (DdzPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();

            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }

            if (isGoldRoom()) {
                build.addExt("1");//1
                build.addExt(player.loadAllGolds() <= 0 ? "1" : "0");//2
                build.addExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//3
            } else {
                build.addExt("0");
                build.addExt("0");
                build.addExt("0");
            }

            build.addExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//4

            if (winPlayer != null) {
                if (player.getSeat() == minPointSeat) {
                    build.setIsHu(1);
                    player.changeCutCard(1);
                } else {
                    build.setIsHu(0);
                    player.changeCutCard(0);
                }
            }

            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
                // 手上没有剩余的牌放第一位为赢家
                list.add(0, build.build());
            } else {
                list.add(build.build());
            }

        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt());
        if (isTwoPlayer()) {
        }
        for (DdzPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt() {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");
        ext.add(masterId + "");
        ext.add(TimeUtil.formatTime(TimeUtil.now()));
        ext.add(playType + "");
        // 地主位置
        ext.add(landLord + "");
        // 底分
        ext.add(1 + "");
        // 倍数
        ext.add(ratio + "");
        // 人数 下标七
        ext.add(max_player_count + "");
        // 局数
        ext.add(totalBureau + "");
        // 房费支付方式
        ext.add(payType + "");
        // 是否春天
        ext.add(checkSpring() + "");
        // 底分(叫地主分数) 下标十一
        ext.add(underPoint + "");

        //12
        //金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));//13
        ext.add(String.valueOf(pay >= 0 ? pay : 0));//14
        ext.add(isGroupRoom()?loadGroupId() : 0+""); //15
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true,null,null);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return DdzPlayer.class;
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_ddz;
    }

    @Override
    public void checkReconnect(Player player) {
    }

    // 是否二人跑得快
    public boolean isTwoPlayer() {
        return max_player_count == 2;
    }

    @Override
    public void checkCompetitionPlay() {
        checkAutoPlay();
    }

    private boolean checkAuto(DdzPlayer player){
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
        } else {
            return false;
        }
        long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimeDdz", 2 * 1000);
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

                if (phase == DdzPhase.play) {
                    List<Integer> curList = player.getHandPais();
                    if (curList.isEmpty()) {
                        return false;
                    }

                    List<Integer> oppo;

                    if (disCardSeat == player.getSeat()){
                        oppo = null;
                    }else {
                        if (player.getSeat() == landLord || disCardSeat == landLord){
                            oppo = getNowDisCardIds();
                        }else{
                            playCommand(player, null, 0);
                            return true;
                        }
                    }

                    List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
                    playCommand(player, list, 0);
                    return true;
                }else if (phase == DdzPhase.robBanker){
                    int point = getMaxRobPoint();
                    return rob(player,point > 0 ? 0 : 1);
                }
            }
        }
        return false;
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {
            if (phase == DdzPhase.robBanker){
                DdzPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null) {
                    return;
                }
                if(!getRobLandLordMap().containsKey(player.getSeat())) {
                    if (checkAuto(player)){
                        player = seatMap.get(nowDisCardSeat);
                        if (!player.isAutoPlay()) {
                            player.setLastOperateTime(System.currentTimeMillis());
                        }
                        return;
                    }
                }
            }else if (state == table_state.play && phase == DdzPhase.play){
                DdzPlayer player = seatMap.get(getNextDisCardSeat());
                if (player == null) {
                    return;
                }
                checkAuto(player);
            }
        }
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        int showCardNumber = StringUtil.getIntValue(params, 8, 0);// 是否显示剩余牌数量
        if (playerCount == 0) {
            playerCount = 3;
        }
        if (!DdzConstants.isPlayDdz(play)) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        // 玩法和人数要对应
        if ((play == DdzConstants.ddz_three && playerCount != 3) || (play == DdzConstants.ddz_two && playerCount != 2)) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        int payType = StringUtil.getIntValue(params, 9, 1);

        // 是否选了三带一
//        int threeAndOne = StringUtil.getIntValue(params, 10, 1);
        int threeAndOne = 1;


        // 是否选了三带二
//        int threeAndTwo = StringUtil.getIntValue(params, 11, 1);
        int threeAndTwo = 1;

        int isOpenGps = StringUtil.getIntValue(params, 12, 0);

        // 匿名功能
        int anonymous = StringUtil.getIntValue(params, 13, 0);

        // 斗地主
        createTable(player, play, bureauCount, playerCount, showCardNumber, payType, isOpenGps, anonymous);
    }

    /**
     * 切公牌
     */
    public List<Integer> cutCard() {
        List<Integer> copy = new ArrayList<>(DdzConstants.cardList_16);
        Collections.shuffle(copy);
        Random random = new Random();
        // 从牌集中随机出一张牌
        int cutCard = copy.get(random.nextInt(copy.size() - 3));
        setCutCard(cutCard);
        sendCutCardMsg();
        return copy;
    }

    /**
     * 根据切牌计算发牌的位置
     */
    // private int getFaPaiSeatByCutCard(int cutCard) {
    // // 从lastWinSeat 开始算起
    // if (lastWinSeat == 0) {
    // lastWinSeat = playerMap.get(masterId).getSeat();
    // }
    // List<Integer> seatList = new ArrayList<>(seatMap.keySet());
    // int index = seatList.indexOf(lastWinSeat);
    // if (index != -1) {
    // return CalcSeatTool.getSeat(seatList, index, cutCard % 100);
    // } else {
    // return 0;
    // }
    // }

    /**
     * 发送切牌消息
     */
    public void sendCutCardMsg() {
        // for (DdzPlayer player : playerMap.values()) {
        // player.writeComMessage(WebSocketMsgType.res_code_ddz_cutcard,
        // cutCard);
        // }
        ComRes build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_cutcard, cutCard).build();
        broadMsg(build);
    }

    /**
     * 检查地主
     */
    public void checkLandlord() {
        if (phase != DdzPhase.robBanker) {
            return;
        }
        // 庄家的位置
        int seat = 0;
        // 出牌玩家的位置
        int disCardSeat = getDisCardSeat();
        // 空间上下一个玩家的位置
        int roomNextSeat = getNextSeat(disCardSeat);
        // 3分直接成为地主
        if (robLandLordMap.values().contains(max_rob_ratio)) {
            for (int key : robLandLordMap.keySet()) {
                int robPoint = robLandLordMap.get(key);
                if (robPoint == max_rob_ratio) {
                    sureLandLord(key);
                    return;
                }
            }
        }

        if (roomNextSeat == fapaiSeat) {
            if (noBodyRob()) {
                Map<Long,Boolean> autoStates = new HashMap<>();

                for (Map.Entry<Long,DdzPlayer> kv : playerMap.entrySet()){
                    autoStates.put(kv.getKey(),kv.getValue().isAutoPlay());
                }

                if (isGoldRoom()){
                    if (currentCount++<=ResourcesConfigsUtil.loadIntegerValue("ServerConfig","ddzFpCount",3)){
                        // 无人抢庄，重新发牌
                        changePlayBureau(-1);
                        initNext();
                        for (DdzPlayer player : playerMap.values()) {
                            player.changeState(player_state.ready);
                        }
                        checkDeal();
                    }else{
                        int s = fapaiSeat;
                        robLandLordMap.put(s,1);
                        sureLandLord(s);
                    }
                }else{
                    // 无人抢庄，重新发牌
                    changePlayBureau(-1);
                    initNext();
                    for (DdzPlayer player : playerMap.values()) {
                        player.changeState(player_state.ready);
                    }
                    checkDeal();
                }

                for (Map.Entry<Long,DdzPlayer> kv : playerMap.entrySet()){
                    kv.getValue().setAutoPlay(autoStates.getOrDefault(kv.getKey(),false),this);
                }
            } else {
                int maxPoint = 0;
                for (int key : robLandLordMap.keySet()) {
                    int robPoint = robLandLordMap.get(key);
                    if (robPoint > maxPoint) {
                        maxPoint = robPoint;
                        seat = key;
                    }
                }
                sureLandLord(seat);
            }
        }
    }

    /**
     * 没有人抢庄
     */
    private boolean noBodyRob() {
        boolean noBodyRob = true;
        if (robLandLordMap.size() != max_player_count) {
            return false;
        }
        for (int robPoint : robLandLordMap.values()) {
            if (robPoint != 0) {
                noBodyRob = false;
            }
        }
        return noBodyRob;
    }

    /**
     * 确定地主
     */
    private void sureLandLord(int seat) {
        setLandLord(seat);
        DdzPlayer lorder = seatMap.get(seat);

        for (Map.Entry<Integer,DdzPlayer> kv : seatMap.entrySet()){
            if (!kv.getValue().isAutoPlay()){
                kv.getValue().setLastOperateTime(System.currentTimeMillis());
            }
        }

        // 底牌给地主
        lorder.addHandPais(underCards);
        lorder.changeLandLordCount();
        // 进入play阶段
        setPhase(DdzPhase.play);
        changeTableState(table_state.play);
        // 设置地主为出牌位置
        setNowDisCardSeat(seat);
        addPlayLog(seat, "" + DdzConstants.play_sureLandLord, StringUtil.implode(underCards, ","));
        // 倍数
        changeRatio(robLandLordMap.get(seat));
        sendRatioMsg();
        // 叫分
        setUnderPoint(robLandLordMap.get(seat));

        // 随机出赖子牌
        int random = 0;

        if (playType == DdzConstants.ddz_three_niggle) {
            random = (int) (Math.random() * 13 + 3);
            // 保存癞子牌
            setMagnaCardValue(random);
//            addPlayLog("" + random);
        }
        ComRes build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_surelandlord, seat, random, JacksonUtil.writeValueAsString(underCards)).build();
        broadMsg(build);

        // 对手牌排序
        order(random);
    }

    /**
     * 对手牌进行排序
     */
    private void order(int magnaCardValue) {
        if (magnaCardValue == 0) {
            for (DdzPlayer player : playerMap.values()) {
                CardTypeTool.setOrder(player.getHandPais());
            }
        } else {
            // 新顺序的牌
            List<Integer> newPais = new ArrayList<>();
            for (DdzPlayer player : playerMap.values()) {
                CardTypeTool.setOrder(player.getHandPais());
                List<Integer> copy = new ArrayList<>(player.getHandPais());
//                for (Integer card : player.getHandPais()) {
//                    if (card % 100 == magnaCardValue) {
//                        copy.remove((Object) card);
//                        newPais.add(600 + card % 100);
//                    }
//                }
                if (!newPais.isEmpty()) {
                    newPais.addAll(copy);
                    player.dealHandPais(newPais);
                }
            }
        }
    }

    /**
     * 发送桌子倍数的消息
     */
    private void sendRatioMsg() {
        ComRes ratioBuild = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_ratio, ratio).build();
        broadMsg(ratioBuild);
    }

    /**
     * 抢地主
     */
    public void robLandlord(Player player) {
        int seat = player.getSeat();
        if (robLandLordMap.containsKey(seat)) {
            // 叫地主者可以进行抢地主
            int action = robLandLordMap.get(seat);
            if (action != 3) {
                return;
            }
            robLandLordMap.put(seat, 5);
        } else {
            // 抢地主
            robLandLordMap.put(player.getSeat(), 2);
        }
        // 添加日志
        addPlayLog(seat, "" + DdzPhase.robBanker.getId(), "" + DdzConstants.rob_landlord);
        setDisCardSeat(seat);
    }

    /**
     * 获得下一个选择叫地主的位置
     */
    private int checkNextRob() {
        if (phase != DdzPhase.robBanker) {
            return 0;
        }
        // 实际应该出牌的下一个位置
        int seat = 0;
        // 出牌玩家的位置
        int disCardSeat = getDisCardSeat();
        // 空间上下一个玩家的位置
        int roomNextSeat = getNextSeat(disCardSeat);
        // 如果是第一轮最后一个人
        if (roomNextSeat == fapaiSeat || robLandLordMap.containsValue(3)) {
            seat = 0;
        } else {
            seat = roomNextSeat;
        }
        return seat;
    }

    /**
     * 第一轮结束后至少两人抢庄
     */
    // private boolean atLeastTwoRob() {
    // if (robLandLordMap.size() != max_player_count) {
    // return false;
    // }
    //
    // int count = 0;
    // for (int value : robLandLordMap.values()) {
    // if (value > 1) {
    // count++;
    // }
    // }
    // if (count > 1) {
    // return true;
    // }
    // return false;
    // }

    /**
     * 是否第一轮抢庄，叫地主者可进行二轮抢庄
     */
    // private boolean isFirstRobRound() {
    // boolean isFirstRound = true;
    // for (int se : robLandLordMap.keySet()) {
    // int action = robLandLordMap.get(se);
    // if (action > 3) {
    // isFirstRound = false;
    // }
    // }
    // return isFirstRound;
    // }

    public boolean rob(Player player, int action) {
        if(robLandlord(player, action)) {
            int nextRob = checkNextRob();
            setNowDisCardSeat(nextRob);
            ComRes build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ddz_roblandlord, player.getSeat(), action, nextRob).build();
            broadMsg(build);
            checkLandlord();
            robotDealAction();
            return true;
        }
        return false;
    }

    /**
     * 抢庄
     *
     * @param action 0,1,2,3
     */
    private boolean robLandlord(Player player, int action) {
        int seat = player.getSeat();
        // 抢地主
        if (robLandLordMap.putIfAbsent(seat, action) == null) {
            // 添加日志
            addPlayLog(seat, "" + DdzPhase.robBanker.getId(), "" + action);
            setDisCardSeat(seat);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCanJoin0(Player player) {
        if (isOpenGps == 1 && (StringUtils.isBlank(player.getMyExtend().getLatitudeLongitude()) || "error".equals(player.getMyExtend().getLatitudeLongitude()))) {
            player.writeErrMsg(LangMsg.code_201);
            LogUtil.msg("userId:" + player.getUserId() + " tableId:" + id + " " + LangHelp.getMsg(LangMsg.code_201));
            return false;
        }
        if (!isDaikaiTable() && (payType == 1 || (payType == 2 && player.getUserId() == masterId))) {
            int needCard = 0;
            if (payType == 1) {
                needCard = PayConfigUtil.get(playType, totalBureau, max_player_count, 0);
            } else {
                needCard = PayConfigUtil.get(playType, totalBureau, max_player_count, 1);
            }
            // 钻石不足不能加入房间
            if (needCard < 0 || !player.isRobot() && needCard > 0&&player.getFreeCards() + player.getCards() < needCard) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                return false;
            }
        }
        return true;
    }

    @Override
    public int calcPlayerCount(int playerCount) {
        return playerCount > 0 ? playerCount : 3;
    }

    /**
     * 是否有人叫过地主
     *
     * @return
     */
    // public boolean hasCalled() {
    // boolean hasCall = false;
    // if (robLandLordMap != null && !robLandLordMap.isEmpty()) {
    // for (int value : robLandLordMap.values()) {
    // if (value > 2) {
    // hasCall = true;
    // }
    // }
    // }
    // return hasCall;
    // }

    /**
     * 检查是否能抢地主
     *
     * @param action
     * @return
     */
    public boolean checkRob(Player player, int action) {
        if (action == 0) {
            return false;
        }
        // 如果玩家已经抢过地主不能再抢
        if (robLandLordMap.containsKey(player.getSeat())) {
            return false;
        }

        if (robLandLordMap.containsValue(action)) {
            return false;
        }
        return false;
    }

    public int getMaxRobPoint() {
        int maxPoint = 0;
        if (robLandLordMap.isEmpty()) {
            return 0;
        }
        for (int point : robLandLordMap.values()) {
            if (point > maxPoint) {
                maxPoint = point;
            }
        }
        return maxPoint;
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    /**
     * 找出所有已经执行过抢庄操作的位置
     *
     * @return
     */
    // public List<Integer> didRobList() {
    // if (phase != DdzPhase.robBanker) {
    // return new ArrayList<>();
    // }
    // List<Integer> didRobList = new ArrayList<>();
    // List<Integer> seatList = new ArrayList<>(seatMap.keySet());
    // int findIndex = seatList.indexOf(fapaiSeat);
    // int endIndex = seatList.indexOf(nowDisCardSeat);
    // if (findIndex != -1 && endIndex != -1) {
    // int index = findIndex;
    // while (index != endIndex) {
    // index = findIndex + 1 > seatList.size() - 1 ? 0 : findIndex + 1;
    // didRobList.add(seatList.get(index));
    // }
    // }
    // return didRobList;
    // }

    public static final List<Integer> wanfaList = Arrays.asList(91,92,93);

    public static void loadWanfaTables(Class<? extends BaseTable> cls){
        for (Integer integer:wanfaList){
            TableManager.wanfaTableTypesPut(integer,cls);
        }
    }
}
