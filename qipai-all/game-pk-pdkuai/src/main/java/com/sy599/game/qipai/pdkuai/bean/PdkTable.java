package com.sy599.game.qipai.pdkuai.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.pdkuai.tool.CardTool;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PdkTable extends BaseTable {
    public static final String GAME_CODE = "pdk";
    private static final int JSON_TAG = 1;
    /*** 当前牌桌上出的牌 */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, PdkPlayer> playerMap = new ConcurrentHashMap<Long, PdkPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, PdkPlayer> seatMap = new ConcurrentHashMap<Integer, PdkPlayer>();
    /*** 最大玩家数量 */
    private volatile int max_player_count = 3;

    private volatile int isFirstRoundDisThree;// 首局是否出黑挑三

    public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数

    private volatile List<Integer> cutCardList = new ArrayList<>();// 切掉的牌

    private volatile int showCardNumber = 0; // 是否显示剩余牌数量

    private volatile int redTen;//是否红10  1:5分  2:10分 3:翻倍
    private volatile int siDai;//四带 0不带 1带1 2带2 3带3
    private volatile int isFirstCardType32;//是否第一个出牌3带2
    private volatile int card3Eq = 1;//三张/飞机可少带接完(玩家最后一手牌可少带牌接三张或飞机)0是1否（牌个数必须相等）

    private volatile int timeNum = 0;

    //新的一轮，3人为2人pass之后为新的一轮出牌
    private boolean newRound=false;
    //pass累计
    private volatile int passNum=0;
    /**
     * 托管时间
     */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
    private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    //是否勾选无炸弹
    private int isNoBoom;
    //直到为出现要不起，出的牌都会临时存在这里
    private volatile List<List<Integer>> noPassDisCard = new ArrayList<>();
    //回放手牌
    private volatile List<List<Integer>> replayDisCard = new ArrayList<>();

    public int getCard3Eq() {
        return card3Eq;
    }

    public void setCard3Eq(int card3Eq) {
        this.card3Eq = card3Eq;
        changeExtend();
    }

    public int getIsFirstCardType32() {
        return isFirstCardType32;
    }

    public void setIsFirstCardType32(int isFirstCardType32) {
        this.isFirstCardType32 = isFirstCardType32;
        changeExtend();
    }

    public int getSiDai() {
        return siDai;
    }

    public void setSiDai(int siDai) {
        this.siDai = siDai;
        changeExtend();
    }

    public int getRedTen() {
        return redTen;
    }

    public void setRedTen(int redTen) {
        this.redTen = redTen;
        changeExtend();
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
        }
        if (isMatchRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutPdk", 15 * 1000);
        } else if (isGoldRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdk", 30 * 1000);
            autoTimeOut2=autoTimeOut;
        }else{
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal", 30 * 1000);
            autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal2", 20 * 1000);
        }
    }

    public long getId() {
        return id;
    }

    public PdkPlayer getPlayer(long id) {
        return playerMap.get(id);
    }

//	/**
//	 * 同意解散的人数
//	 */
//	public int getDissPlayerAgreeCount() {
//		int temp=(int)Math.ceil(getPlayerMap().size()*2.0/3);
//		return temp;
//	}

    /**
     * 一局结束
     */
    public void calcOver() {
        PdkPlayer winPlayer = null;
        int winPoint = 0;
        Map<Integer, Integer> lossPoint = new HashMap<Integer, Integer>();
        int closeNum = 0;

        int goldPay = 0;
        int goldRatio = 1;
        boolean isGold = false;
        if (isGoldRoom()) {
            try {
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
                if (goldRoom != null) {
                    isGold = true;
                    modeId = goldRoom.getModeId();
                    if (isMatchRoom()) {
                        goldPay = 0;
                        goldRatio = (int) matchRatio;
                    } else {
                        goldPay = PayConfigUtil.get(playType, goldRoom.getGameCount(), goldRoom.getMaxCount(), 0, goldRoom.getModeId());
                        if (goldPay < 0) {
                            goldPay = 0;
                        }
                        goldRatio = GameConfigUtil.loadGoldRatio(modeId);
                    }
                }
            } catch (Exception e) {
            }
        }

        for (PdkPlayer player : seatMap.values()) {
            player.changeState(player_state.over);
            int left = player.getHandPais().size();

            int currentLs = player.getCurrentLs();
            int maxLs = player.getMaxLs();
            if (left == 0) {
                winPlayer = player;

                currentLs++;
                player.setCurrentLs(currentLs);
                UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "currentLs", String.valueOf(currentLs));
                if (currentLs > maxLs) {
                    maxLs = currentLs;
                    player.setMaxLs(maxLs);
                    UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "maxLs", String.valueOf(maxLs));
                }
            } else {
                if (currentLs > 0) {
                    currentLs = 0;
                    player.setCurrentLs(currentLs);
                    UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "currentLs", String.valueOf(currentLs));
                }

                // 需要大于1爆单不扣分
                int point = left <= 1 ? 0 : left;
                if (!player.isOutCards()) {
                    // 一张牌都没出算双倍
                    point = point * 2;
                    closeNum++;
                    player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index6, 1);
                }

                lossPoint.put(player.getSeat(), point);
            }
        }
        if (winPlayer == null) {
            return;
        }
        if (closeNum > 0) {
            winPlayer.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index1, closeNum);
        }

        for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
            PdkPlayer player = seatMap.get(entry.getKey());
            int point0 = entry.getValue();
            int point = calcRedTen(winPlayer, player, point0);
            if (point != point0) {
                entry.setValue(point);
            }
            winPoint += point;
        }

        Map<Long, Integer> ticketMap = new HashMap<>();
        Map<Long, Integer> outScoreMap = new HashMap<>();
        if (isMatchRoom()) {
            Map<Long, Integer> map = new HashMap<>();
            int tempScore = 0;
            int tempScore0 = 0;
            for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                PdkPlayer player = seatMap.get(entry.getKey());
                int tempSc = -entry.getValue() + player.getPlayBoomPoint();
                tempScore += tempSc;
                map.put(player.getUserId(), tempSc);

                int tempSc0 = (int) (tempSc * matchRatio);
                tempScore0 += tempSc0;
                player.calcLost(this, 1, tempSc0);
            }
            map.put(winPlayer.getUserId(), -tempScore);
            winPlayer.calcWin(this, 1, -tempScore0);
            changeMatchData(map);
        } else if (isGold) {
            Map<Integer, Integer> scoreMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                PdkPlayer player = seatMap.get(entry.getKey());
                scoreMap.put(entry.getKey(), -entry.getValue() + player.getPlayBoomPoint());
                player.setPlayPoint(0);
                player.setPoint(0);
            }
            winPlayer.setPlayPoint(0);
            winPlayer.setPoint(0);
            scoreMap.put(winPlayer.getSeat(), winPoint + winPlayer.getPlayBoomPoint());

            int jifen = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("jifen_gold_room_" + modeId, "0"), 0);
            if (jifen > 0) {
                winPlayer.addJiFen(jifen, 1);
            }

            long total0 = 0;
            long total1 = 0;
            int winCount = 0;
            for (Map.Entry<Integer, Integer> kv : scoreMap.entrySet()) {
                PdkPlayer player = seatMap.get(kv.getKey());
                int point = kv.getValue().intValue();
                if (point <= 0) {
                    if (goldRatio > 1) {
                        point *= goldRatio;
                    }
                    total1 += point;
                    long allGold = player.loadAllGolds();
                    if (allGold < goldPay - point) {
                        point = (int) (allGold - goldPay);
                        if (point < 0) {
                            point = 0;
                        }
                        point = -point;
                    }
                    total0 += point;
                    player.changeGold(-goldPay + point, playType);
                    player.calcLost(this, 1, point);
                    player.updateGoldRoomActivity(false);
                } else {
                    winCount++;
                    player.updateGoldRoomActivity(true);
                }
            }
            if (total0 != 0 && winCount > 0 && total1 != 0) {
                int i = 0;
                int restPoint = 0;
                for (Map.Entry<Integer, Integer> kv : scoreMap.entrySet()) {
                    PdkPlayer player = seatMap.get(kv.getKey());
                    int point = kv.getValue().intValue();
                    if (point > 0) {
                        if (goldRatio > 1) {
                            point *= goldRatio;
                        }
                        if (GameServerConfig.isAbroad()) {//赢家扣税
                            String tax = ResourcesConfigsUtil.loadStringValue("ServerConfig", "gold_won_player_tax", "0.05");
                            float taxFloat = new Float(tax).floatValue();
                            int pay2Tax = new Double(Math.ceil(point * taxFloat)).intValue();
                            point -= pay2Tax;
                        }
                        if (total0 != total1) {
                            if (winCount == 1) {
                                int ticketCount = 0;
                                if (-total0 < goldPay) {
                                    player.changeGold((int) (-total0 - goldPay), playType);
                                    player.calcWin(this, 1, (int) -total0);

                                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                    if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                        ticketCount = (int) ((-total0) / (tmpConfig.intValue()));
                                    }
                                } else {
                                    long allGold = player.loadAllGolds();
                                    if (-total0 > allGold) {
                                        player.changeGold(allGold > 0 ? (int) (allGold - goldPay) : 0, 0, playType);
                                        player.calcWin(this, 1, (int) -total0);
                                        outScoreMap.put(player.getUserId(), (int) (-total0 - allGold));
                                        LogUtil.msgLog.info("out player gold:tableId={},playerId={},modeId={},allGold={},{}(win)={}(get)+{}(out),goldPay={}"
                                                , id, player.getUserId(), modeId, allGold, -total0, allGold, (-total0 - allGold), goldPay);

                                        Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                        if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                            ticketCount = (int) (allGold / (tmpConfig.intValue()));
                                        }
                                    } else {
                                        player.changeGold((int) (-total0 - goldPay), 0, playType);
                                        player.calcWin(this, 1, (int) -total0);

                                        Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                        if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                            ticketCount = (int) ((-total0) / (tmpConfig.intValue()));
                                        }
                                    }
                                }

                                if (ticketCount > 0) {
                                    ticketMap.put(player.getUserId(),ticketCount);
                                    UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                            String.valueOf(player.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                                    LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}", modeId, player.getUserId(), ticketCount);
                                }
                            } else if (winCount == 2) {
                                int ticketCount = 0;
                                if (i == 0) {
                                    point = (int) Math.round(point * (total0 * 1.0 / total1));
                                    restPoint = (int) (-total0 - point);
                                } else {
                                    point = restPoint;
                                }
                                if (point < goldPay) {
                                    player.changeGold((int) (point - goldPay), playType);
                                    player.calcWin(this, 1, point);

                                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                    if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                        ticketCount = (int) (point / (tmpConfig.intValue()));
                                    }
                                } else {
                                    long allGold = player.loadAllGolds();
                                    if (point > allGold) {
                                        player.changeGold(allGold > 0 ? (int) (allGold - goldPay) : 0, 0, playType);
                                        player.calcWin(this, 1, point);
                                        outScoreMap.put(player.getUserId(), (int) (point - allGold));
                                        LogUtil.msgLog.info("out player gold:tableId={},playerId={},modeId={},allGold={},{}(win)={}(get)+{}(out),goldPay={}"
                                                , id, player.getUserId(), modeId, allGold, point, allGold, (point - allGold), goldPay);

                                        Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                        if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                            ticketCount = (int) (allGold / (tmpConfig.intValue()));
                                        }
                                    } else {
                                        player.changeGold((int) (point - goldPay), 0, playType);
                                        player.calcWin(this, 1, (int) point);

                                        Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                        if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                            ticketCount = (int) (point / (tmpConfig.intValue()));
                                        }
                                    }
                                }

                                if (ticketCount > 0) {
                                    ticketMap.put(player.getUserId(),ticketCount);
                                    UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                            String.valueOf(player.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                                    LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}", modeId, player.getUserId(), ticketCount);
                                }

                                i++;
                            }
                        } else {
                            int ticketCount = 0;
                            if (point < goldPay) {
                                player.changeGold(point - goldPay, playType);
                                player.calcWin(this, 1, point);

                                Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                    ticketCount = (int) (point / (tmpConfig.intValue()));
                                }
                            } else {
                                long allGold = player.loadAllGolds();
                                if (point > allGold) {
                                    player.changeGold(allGold > 0 ? (int) (allGold - goldPay) : 0, 0, playType);
                                    player.calcWin(this, 1, point);
                                    outScoreMap.put(player.getUserId(), (int) (point - allGold));
                                    LogUtil.msgLog.info("out player gold:tableId={},playerId={},modeId={},allGold={},{}(win)={}(get)+{}(out),goldPay={}"
                                            , id, player.getUserId(), modeId, allGold, point, allGold, (point - allGold), goldPay);

                                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                    if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                        ticketCount = (int) (allGold / (tmpConfig.intValue()));
                                    }
                                } else {
                                    player.changeGold((int) (point - goldPay), 0, playType);
                                    player.calcWin(this, 1, (int) point);

                                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                                    if (tmpConfig != null && tmpConfig.intValue() > 0) {
                                        ticketCount = (int) (point / (tmpConfig.intValue()));
                                    }
                                }
                            }

                            if (ticketCount > 0) {
                                ticketMap.put(player.getUserId(),ticketCount);
                                UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                        String.valueOf(player.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                                LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}", modeId, player.getUserId(), ticketCount);
                            }
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                seatMap.get(entry.getKey()).calcLost(this, 1, -entry.getValue());
            }
            winPlayer.calcWin(this, 1, winPoint);
        }

        boolean isOver = playBureau >= totalBureau;

        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, winPlayer, false, outScoreMap,ticketMap);

        saveLog(isOver, winPlayer.getUserId(), res.build());
        setLastWinSeat(winPlayer.getSeat());
        calcAfter();
        if (isOver) {
            calcOver1();
            calcOver2();
            calcCreditNew();
            diss();

            for (Player player : seatMap.values()) {
                player.saveBaseInfo();
            }
        } else {
            initNext();
            calcOver1();

            for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
                int seat = kv.getKey().intValue();
                if (seat != kv.getValue().getSeat()) {
                    LogUtil.errorLog.warn("table user seat error3:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

                    kv.getValue().setSeat(seat);
                    kv.getValue().setPlayingTableId(id);
                    changePlayers();
                }
                kv.getValue().saveBaseInfo();
            }
        }

    }

    @Override
    public void calcDataStatistics2() {
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //俱乐部活动总大局数
            calcDataStatistics3(groupId);

            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (PdkPlayer player : playerMap.values()) {
                //总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                //总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                //总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.loadScore() > 0) {
                    if (player.loadScore() > maxPoint) {
                        maxPoint = player.loadScore();
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.loadScore() < 0) {
                    if (player.loadScore() < minPoint) {
                        minPoint = player.loadScore();
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (PdkPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
                    //单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.loadScore()) {
                    //单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }

    public int calcRedTen(PdkPlayer winPlayer, PdkPlayer player, int point) {
        if (this.getRedTen() > 0 && (winPlayer.getRedTenPai() == 1 || player.getRedTenPai() == 1)) {
            switch (this.getRedTen()) {
                case 1:
                    point += 5;
                    break;
                case 2:
                    point += 10;
                    break;
                case 3:
                    point *= 2;
                    break;
            }
        }
        return point;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
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
        userLog.setMaxPlayerCount(max_player_count);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (PdkPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }

        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

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
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putInt(1, isFirstRoundDisThree);
        wrapper.putInt(2, max_player_count);
        wrapper.putInt(3, showCardNumber);
        wrapper.putInt(4, redTen);
        wrapper.putInt(5, siDai);
        wrapper.putInt(6, isFirstCardType32);
        wrapper.putString("card3Eq", String.valueOf(card3Eq));
//		return wrapper.toString();
        wrapper.putInt(7, jiaBei);
        wrapper.putInt(8, jiaBeiFen);
        wrapper.putInt(9, jiaBeiShu);
        wrapper.putInt(10,isNoBoom);
        return wrapper;
    }

    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (PdkPlayer pdkPlayer : playerMap.values()) {
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
     * 开始发牌
     */
    public void fapai() {
        synchronized (this) {
            changeTableState(table_state.play);
            timeNum = 0;
            if (playedBureau <= 0) {
                for (PdkPlayer player : playerMap.values()) {
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
            }

            if (isGoldRoom()) {
                if (playedBureau == 0 || lastWinSeat <= 0) {
                    List<Long> list0 = new ArrayList<>(3);
                    try {
                        List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(), id);
                        if (list != null) {
                            for (HashMap<String, Object> map : list) {
                                if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult", "0")), 0) > 0) {
                                    list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId", "0")), 0));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                    if (list0.size() > 0) {
                        Long userId = list0.get(new SecureRandom().nextInt(list0.size()));
                        Player player = playerMap.get(userId);
                        if (player != null) {
                            setLastWinSeat(player.getSeat());
                        }
                    }
                    if (lastWinSeat <= 0) {
                        setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
                    }
                }
            }

            List<List<Integer>> list;
            if(isNoBoom==1){
                list = CardTool.fapaiNoBoom(max_player_count, playType, zp);
            }else {
                list = CardTool.fapai(max_player_count, playType, zp);
            }
            int i = 0;
            for (PdkPlayer player : playerMap.values()) {
                player.changeState(player_state.play);
                player.dealHandPais(list.get(i), this);
                if (getRedTen() > 0 && list.get(i).contains(310)) {//是否有红心10
                    player.setRedTenPai(1);
                }
                player.setIsNoLet(0);
                i++;
                faPaiGamLog(player,0);
            }
            if (isTwoPlayer()) {
                this.cutCardList.clear();
                this.cutCardList.addAll(list.get(i));
            }
        }
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
            if (lastWinSeat == 0) {
                // 还没有出过牌 看黑桃3在谁手里
                for (PdkPlayer player : playerMap.values()) {
                    if (player.getHandPais().contains(403)) {
                        seat = player.getSeat();
                        return seat;
                    }
                }
                // 当黑桃3在切牌里面时
                if (0 == seat) {
                    for (int temp : seatMap.keySet()) {
                        if (seat == 0) {
                            seat = temp;
                        } else {
                            if (seat > temp) {
                                seat = temp;
                            }
                        }
                    }
//					seat = RandomUtils.nextInt(2) + 1;
                }
            } else {
                return lastWinSeat;
            }

        } else {
            if (nowDisCardSeat != 0) {
                seat = nowDisCardSeat >= max_player_count ? 1 : nowDisCardSeat + 1;
            }
        }
        return seat;
    }

    public PdkPlayer getPlayerBySeat(int seat) {
        int next = seat >= max_player_count ? 1 : seat + 1;
        return seatMap.get(next);

    }

    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        synchronized (this) {
            res.setNowBurCount(getPlayBureau());
            res.setTotalBurCount(getTotalBureau());
            res.setGotyeRoomId(gotyeRoomId + "");
            res.setTableId(getId() + "");
            res.setWanfa(playType);
            List<PlayerInTableRes> players = new ArrayList<>();
            for (PdkPlayer player : playerMap.values()) {
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
                if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size()>0) {
                    playerRes.addAllOutCardIds(nowDisCardIds);
                    int cardType = CardUtils.cardResult2ReturnType(CardUtils.calcCardValue(CardUtils.loadCards(nowDisCardIds),siDai));
                    playerRes.addRecover(cardType);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
            res.setRenshu(this.max_player_count);
            res.addExt(this.showCardNumber);//0
            res.addExt(this.isFirstRoundDisThree);//1
            res.addExt(this.payType);//2
            res.addExt(this.redTen);//3
            res.addExt(this.siDai);//4

            res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);//5
            int ratio;
            int pay;
            MatchBean matchBean = isMatchRoom() ? JjsUtil.loadMatch(matchId) : null;
            if (matchBean != null) {
                ratio = (int) matchRatio;
                pay = 0;
            } else if (isGoldRoom()) {
                ratio = GameConfigUtil.loadGoldRatio(modeId);
                pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
            } else {
                ratio = 1;
                pay = consumeCards() ? loadPayConfig(payType) : 0;
            }

            res.addExt(ratio);//6
            res.addExt(pay);//7
            res.addExt(lastWinSeat);//8
            if (matchBean != null) {
                int num = JjsUtil.loadMatchCurrentGameNo(matchBean);
                res.addExt(num);//9
                res.addExt(num == 0 ? JjsUtil.loadMinScore(matchBean, getMatchRatio()) : 0);//10
            } else {
                res.addExt(0);
                res.addExt(0);
            }
            res.addExtStr(String.valueOf(matchId));//0
            res.addExtStr(cardMarkerToJSON());//1
            res.addTimeOut((isGoldRoom() || autoPlay) ? autoTimeOut : 0);
            if (matchBean != null) {
                if (disCardRound == 0) {
                    res.addTimeOut((autoTimeOut + 5000));
                } else {
                    res.addTimeOut(autoTimeOut);
                }
            } else if(autoPlay){
                if (disCardRound == 0) {
                    res.addTimeOut((autoTimeOut + 5000));
                } else {
                    res.addTimeOut(autoTimeOut);
                }
            }else{
                res.addTimeOut(0);
            }

            res.addExt(playedBureau);//11
            res.addExt(disCardRound);//12

            res.addExt(card3Eq);//13

            res.addExt(creditMode); //14
            res.addExt(creditJoinLimit);//15
            res.addExt(creditDissLimit);//16
            res.addExt(creditDifen);//17
            res.addExt(creditCommission);//18
            res.addExt(creditCommissionMode1);//19
            res.addExt(creditCommissionMode2);//20
            res.addExt(autoPlay ? 1 : 0);//21
            res.addExt(jiaBei); //22
            res.addExt(jiaBeiFen);//23
            res.addExt(jiaBeiShu);//24
        }

        return res.build();
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (PdkPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }

    public void notLet(PdkPlayer player) {
        // 要不起
        setNowDisCardSeat(player.getSeat());
        player.setIsNoLet(1);
        List<Integer> cards = new ArrayList<>();
        cards.add(0);
        player.addOutPais(cards, this);
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        res.setIsPlay(1);
        if (player.getHandPais().size() == 1) {
            // 报单
            res.setIsBt(1);
        }

        for (PdkPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds, false, pdkPlayer, this);
            if (disCardSeat == pdkPlayer.getSeat() || !canPlayList.isEmpty()) {
                copy.setIsLet(1);

            }
            pdkPlayer.writeSocket(copy.build());
        }
    }

    /**
     * 出牌
     *
     * @param player
     * @param cards
     */
    public void disCards(PdkPlayer player, List<Integer> cards) {
        if (disCardSeat == player.getSeat()) {
            clearIsNotLet();
        } else {
            player.setIsNoLet(0);

        }
//        CardType cardType = CardTypeTool.jugdeType(cards, this);
        CardUtils.Result cardResult = CardUtils.calcCardValue(CardUtils.loadCards(cards),siDai);

        int cardType = -1;

        if (cardResult.getType()==4&&siDai==3&&cards.size()==7&&disCardSeat!=player.getSeat()){
            CardUtils.Result result1 = CardUtils.calcCardValue(CardUtils.loadCards(nowDisCardIds),siDai);
            if (result1.getType()==33){
                cardType = 33;
            }
        }

        if (cardType==33||cardResult.getType()==33) {
            // 飞机
            player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index5, 1);
        }

        cards = CardUtils.loadSortCards(cards,cardResult,siDai);

        setDisCardSeat(player.getSeat());
        player.addOutPais(cards, this);
        setNowDisCardIds(cards);
        setNowDisCardSeat(player.getSeat());
        noPassDisCard.add(cards);


        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.addAllCardIds(getNowDisCardIds());
        if (newRound){
            passNum=0;
            newRound=false;
            //推送消息清桌子
            res.setIsClearDesk(1);
        }else {
            res.setIsClearDesk(0);
        }

        if (cardType<=0) {
            cardType=CardUtils.cardResult2ReturnType(cardResult);
        }

        res.setCardType(cardType);
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(2);
        if (player.getHandPais().size() == 1) {
            res.setIsBt(1);
        }

        boolean let = false;
        boolean isOver = player.getHandPais().size() == 0;

        Map<Long, Integer> stateMap = new HashMap<>();

        for (PdkPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if (pdkPlayer.getUserId() == player.getUserId()) {
                pdkPlayer.writeSocket(copy.build());
                continue;
            }

            if (isOver || pdkPlayer.getHandPais().size() == 0) {
                // 如果玩家出完了最后一张牌，不需要提示要不起
                copy.setIsLet(1);

            } else {
                List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds, false, pdkPlayer, this);

                if (canPlayList.size()>0) {
                    CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(canPlayList),siDai);
                    if ((result.getType()==3||result.getType()==33)&&canPlayList.size()%5!=0){
                        if (canPlayList.size()==pdkPlayer.getHandPais().size()&&card3Eq!=1){
                            copy.setIsLet(1);
                            let = true;
                            stateMap.put(pdkPlayer.getUserId(), 1);
                        }else{
                            stateMap.put(pdkPlayer.getUserId(), 0);
                        }
                    }else{
                        copy.setIsLet(1);
                        let = true;
                        // pdkPlayer.setIsNoLet(0);
                        stateMap.put(pdkPlayer.getUserId(), 1);
                    }
                } else {
                    // 记录当前的状态
                    // pdkPlayer.setIsNoLet(1);
                    stateMap.put(pdkPlayer.getUserId(), 0);
                    passNum++;
                    if (passNum==max_player_count-1){
                        newRound=true;
                        setReplayDisCard();
                    }

                }
            }
            pdkPlayer.writeSocket(copy.build());
        }
        if (cardResult.getType()==100) {
            if (!isGoldRoom() || !let) {
                player.changeBoomCount(1);
            }

            if (!let) {
                // 别人打不起 算炸弹积分
                if (isMatchRoom()) {
                    for (PdkPlayer pdkPlayer : seatMap.values()) {
                        if (pdkPlayer.getUserId() == player.getUserId()) {
                            if (isTwoPlayer()) {
                                pdkPlayer.changePlayBoomPoint(10);
                            } else {
                                pdkPlayer.changePlayBoomPoint(20);
                            }
                        } else {
                            pdkPlayer.changePlayBoomPoint(-10);
                        }
                    }
                } else {
                    for (PdkPlayer pdkPlayer : seatMap.values()) {
                        if (pdkPlayer.getUserId() == player.getUserId()) {
                            if (isTwoPlayer()) {
                                pdkPlayer.changePlayPoint(10);
                                pdkPlayer.changePlayBoomPoint(10);
                            } else {
                                pdkPlayer.changePlayPoint(20);
                                pdkPlayer.changePlayBoomPoint(20);
                            }
                        } else {
                            pdkPlayer.changePlayPoint(-10);
                            pdkPlayer.changePlayBoomPoint(-10);
                        }
                    }
                }
            }
        }
        if (isOver) {
            state = table_state.over;
        } else {
            int nextSeat = calcNextSeat(player.getSeat());
            PdkPlayer nextPlayer = seatMap.get(nextSeat);
            while (nextSeat != player.getSeat() && nextPlayer.getHandPais().size() == 0) {
                nextSeat = calcNextSeat(nextPlayer.getSeat());
                nextPlayer = seatMap.get(nextSeat);
            }

            Integer state = stateMap.remove(nextPlayer.getUserId());
            while (state != null && state.intValue() == 0) {

                if (nextPlayer.getUserId()!=player.getUserId()) {
                    playCommand(nextPlayer, null);
                }

                nextSeat = calcNextSeat(nextPlayer.getSeat());
                nextPlayer = seatMap.get(nextSeat);
                if (nextPlayer == null) {
                    break;
                }
                while (nextSeat != player.getSeat() && nextPlayer.getHandPais().size() == 0) {
                    nextSeat = calcNextSeat(nextPlayer.getSeat());
                    nextPlayer = seatMap.get(nextSeat);
                }

                state = stateMap.remove(nextPlayer.getUserId());
            }
        }

    }

    public void setReplayDisCard(){
        List<List<Integer>> cards = new ArrayList<>();
        int size = noPassDisCard.size();
        for (int i = 0; i < 3&&i<size; i++) {
            cards.add(noPassDisCard.get(size-1-i));
        }
        replayDisCard=cards;
    }

    /**
     * 清理要不起的状态
     */
    public void clearIsNotLet() {
        for (PdkPlayer player : seatMap.values()) {
            player.setIsNoLet(0);
        }
    }

    /**
     * 打牌
     *
     * @param player
     * @param cards
     */
    public void playCommand(PdkPlayer player, List<Integer> cards) {
        synchronized (this) {
            if (state != table_state.play) {
                return;
            }

            addPlayLog(player.getSeat(), cards, ",");
            StringBuilder sb = new StringBuilder("Pdk");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(player.getAutoPlayCheckedTime());
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            LogUtil.msgLog.info(sb.toString());

            if (cards != null && cards.size() > 0) {
                changeDisCardRound(1);
                // 出牌了
                disCards(player, cards);
            } else {
                if (disCardRound > 0) {
                    changeDisCardRound(1);
                }
                notLet(player);
            }

            setLastActionTime(TimeUtil.currentTimeMillis());

            if (isOver()) {
                calcOver();
            } else {
                int nextSeat = calcNextSeat(player.getSeat());
                PdkPlayer nextPlayer = seatMap.get(nextSeat);
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
        for (PdkPlayer player : seatMap.values()) {
            if (player.getIsEntryTable() != SharedConstants.table_online) {
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
        setIsFirstCardType32(0);
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
        PdkPlayer nextPlayer = seatMap.get(nextSeat);
        int timeout;
        if (!nextPlayer.isRobot()) {
            if (matchId > 0L && disCardRound == 0) {
                timeout = autoTimeOut + 5000;
            } else if(autoPlay && disCardRound == 0){
                timeout = autoTimeOut + 5000;
            }else{
                timeout = autoTimeOut;
            }
            nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + timeout);
        } else {
            timeout = autoTimeOut;
        }

        for (Player tablePlayer : getSeatMap().values()) {
            if (userId == tablePlayer.getUserId()) {
                continue;
            }
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(nextSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setBanker(lastWinSeat);

            res.addXiaohu(nextPlayer.isAutoPlay() ? 1 : 0);
            res.addXiaohu(timeout);

            tablePlayer.writeSocket(res.build());
        }
    }

    @Override
    protected void robotDealAction() {
    }

    @Override
    protected void deal() {

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
        return createTable(player, play, bureauCount, params, saveDb);
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(player, play, bureauCount, params, true);
    }

    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
        //objects对象的值列表  [局数,玩法（15或者16张）,this.niao,this.leixing,this.zhuang,this.niaoPoint,必出黑桃3,人数,显示剩余牌数
        long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
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

        setLastActionTime(TimeUtil.currentTimeMillis());
        if (params.size() >= 7) {
            setIsFirstRoundDisThree(params.get(6).intValue());
        }
        // 是否二人比赛
        if (params.size() >= 8) {
            int isTwoPlayer = params.get(7).intValue();
            setMaxPlayerCount(isTwoPlayer);
        }
        if (params.size() >= 9) {
            setShowCardNumber(params.get(8).intValue());
        }
        if (params.size() >= 10) {
            setPayType(params.get(9).intValue());
        }
        if (params.size() >= 11) {
            setRedTen(params.get(10).intValue());
        }
        int siDai = StringUtil.getIntValue(params, 11, 0);
        if (siDai == 3 || siDai == 2) {
            setSiDai(siDai);
        } else {
            setSiDai(0);
        }
//        if (isTwoPlayer()) {
//            setIsFirstRoundDisThree(0);
//        }

        card3Eq = StringUtil.getIntValue(params, 12, card3Eq);

        //信用分相关
        this.creditMode = StringUtil.getIntValue(params, 13, 0);
        this.creditJoinLimit = StringUtil.getIntValue(params, 14, 0);
        this.creditDissLimit = StringUtil.getIntValue(params, 15, 0);
        this.creditDifen= StringUtil.getIntValue(params, 16, 0);
        this.creditCommission = StringUtil.getIntValue(params, 17, 0);
        this.creditCommissionMode1 = StringUtil.getIntValue(params, 18, 1);
        this.creditCommissionMode2 = StringUtil.getIntValue(params, 19, 1);
        this.creditCommissionLimit = StringUtil.getIntValue(params, 20, 100);

        this.autoPlay = StringUtil.getIntValue(params, 21, 0) == 1;

        this.jiaBei = StringUtil.getIntValue(params, 22, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 23, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 24, 1);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        this.isNoBoom=StringUtil.getIntValue(params, 25, 0);
        if (isGoldRoom()) {
            try {
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
                if (goldRoom != null) {
                    modeId = goldRoom.getModeId();
                }
            } catch (Exception e) {
            }

            if (isMatchRoom()) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutPdk", 15 * 1000);
            } else if (isGoldRoom()) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdk", 30 * 1000);
            }
            autoTimeOut2=autoTimeOut;
        }else{
            if(autoPlay) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal", 30 * 1000);
                autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal2", 20 * 1000);
            }
        }
        changeExtend();

        return true;
    }

    @Override
    protected void initNowAction(String nowAction) {

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper(info);
        isFirstRoundDisThree = wrapper.getInt(1, 0);
        max_player_count = wrapper.getInt(2, 3);
        if (max_player_count == 0) {
            max_player_count = 3;
        }
        showCardNumber = wrapper.getInt(3, 0);
        redTen = wrapper.getInt(4, 0);
        siDai = wrapper.getInt(5, 0);
        isFirstCardType32 = wrapper.getInt(6, 0);
        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }

        card3Eq = wrapper.getInt("card3Eq", card3Eq);

        jiaBei = wrapper.getInt(7, 0);
        jiaBeiFen = wrapper.getInt(8, 0);
        jiaBeiShu = wrapper.getInt(9, 0);
        isNoBoom = wrapper.getInt(10,0);
    }

    @Override
    protected String buildNowAction() {
        return null;
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
        return sendAccountsMsg(over, winPlayer, isBreak, null,null);
    }

    /**
     * 发送结算msg
     *
     * @param over      是否已经结束
     * @param winPlayer 赢的玩家
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak, Map<Long, Integer> outScoreMap,Map<Long, Integer> ticketMap) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
        if (winPlayer != null) {
            for (PdkPlayer player : seatMap.values()) {
                if (player.getUserId() == winPlayer.getUserId()) {
                    continue;
                }
                if (minPoint == 0 || player.getPoint() < minPoint) {
                    minPoint = player.getPlayPoint();
                    minPointSeat = player.getSeat();
                }
            }
        }

        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (PdkPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (PdkPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }



        for (PdkPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes(this);
            } else {
                build = player.bulidOneClosingPlayerInfoRes(this);

            }
            //添加本局所有牌和炸弹分
            //所有牌
            //添加手牌
            List<Integer> allCard = new ArrayList<Integer>();
            for (Integer v : player.getHandPais()) {
                if (!allCard.contains(v)) {
                    allCard.add(v);
                }
            }
            //添加已出的牌
            for (List<Integer> c : player.getOutPais()) {
                for (Integer v : c) {
                    if (!allCard.contains(v)) {
                        allCard.add(v);
                    }
                }
            }

            JSONArray jsonArray = new JSONArray();
            for (int card : allCard) {
                if (card != 0) {
                    jsonArray.add(card);
                }
            }
            build.addExt(jsonArray.toString()); //0
            //炸弹分
            build.addExt(player.getPlayBoomPoint() + "");//1
            build.addExt(player.getRedTenPai() + "");//2

            if (isGoldRoom()) {
                build.addExt("1");//3
                build.addExt(player.loadAllGolds() <= 0 ? "1" : "0");//4
                build.addExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//5
            } else {
                build.addExt("0");//3
                build.addExt("0");//4
                build.addExt("0");//5
            }

            build.addExt(String.valueOf(player.getCurrentLs()));//6
            build.addExt(String.valueOf(player.getMaxLs()));//7
            build.addExt(String.valueOf(matchId));//8
            build.addExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//9

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
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }

            //信用分
            if(isCreditTable()){
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }

        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            int dyjCredit = 0;
            for (PdkPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                PdkPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addExt(player.getWinLoseCredit() + "");      //10
                builder.addExt(player.getCommissionCredit() + "");   //11

                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
                list.add(builder.build());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                builder.addExt(0 + ""); //10
                builder.addExt(0 + ""); //11
                list.add(builder.build());
            }
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt());
        if (isTwoPlayer()) {
            res.addAllCutCard(this.cutCardList);
        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (PdkPlayer player : seatMap.values()) {
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
        ext.add(playBureau + "");//4
        ext.add(isGroupRoom() ? "1" : "0");//5
        //金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");//6
        int ratio;
        int pay;
        if (isMatchRoom()) {
            ratio = (int) matchRatio;
            pay = 0;
        } else if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));//7
        ext.add(String.valueOf(pay >= 0 ? pay : 0));//8
        ext.add(String.valueOf(payType));//9
        ext.add(String.valueOf(playedBureau));//10

        ext.add(String.valueOf(matchId));//11
        ext.add(isGroupRoom() ? loadGroupId() : "");//12

        ext.add(creditMode + ""); //13
        ext.add(creditJoinLimit + "");//14
        ext.add(creditDissLimit + "");//15
        ext.add(creditDifen + "");//16
        ext.add(creditCommission + "");//17
        ext.add(creditCommissionMode1 + "");//18
        ext.add(creditCommissionMode2 + "");//19
        ext.add((autoPlay ? 1 : 0) + "");//20
        ext.add(jiaBei + "");//21
        ext.add(jiaBeiFen + "");//22
        ext.add(jiaBeiShu + "");//23
        return ext;
    }

    @Override
    public String loadGameCode() {
        return GAME_CODE;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return PdkPlayer.class;
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_pdk;
    }

    @Override
    public void checkReconnect(Player player) {
    }

    // 是否二人跑得快
    public boolean isTwoPlayer() {
        return max_player_count == 2;
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
            if (getSendDissTime() > 0) {
                return;
            }
            if (autoPlay && state == table_state.ready && playedBureau > 0 ) {
                if (++timeNum >= ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoReadyPdkNormal", 10)) {
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
            PdkPlayer player = seatMap.get(getNextDisCardSeat());
            if (player == null || state != table_state.play) {
                return;
            } else if (isGoldRoom()) {
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
            }else if(autoPlay){
                timeout = autoTimeOut;
                if (disCardRound == 0) {
                    timeout = autoTimeOut + 5000;
                }
            } else if (player.isRobot()) {
                timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
            }else{
                return;
            }
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePdk", 2 * 1000);
            long now = TimeUtil.currentTimeMillis();
            boolean auto = player.isAutoPlay();
            if (!auto) {
                if (GameServerConfig.isAbroad()) {
                    if (!player.isRobot() && now >= player.getNextAutoDisCardTime()) {
                        auto = true;
                        player.setAutoPlay(true, this);
                    }
                } else {
                    auto = checkPlayerAuto(player,timeout);
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

                    if (state == table_state.play) {
                        List<Integer> oppo;
                        if (disCardSeat != player.getSeat()) {
                            oppo = getNowDisCardIds();
                        } else {
                            oppo = null;
                        }
                        List<Integer> curList = player.getHandPais();
                        if (curList.isEmpty()) {
                            return;
                        }

                        List<Integer> list =  CardTypeTool.getBestAI2(new ArrayList<>(curList), oppo == null ? null : new ArrayList<>(oppo), seatMap.get(calcNextSeat(player.getSeat())).getHandPais().size() == 1,this);
                        if (oppo!=null&&oppo.size()>0&&oppo.size()%5==0&&list!=null&&list.size()%5!=0&&card3Eq==1){
                            CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(list),siDai);
                            if (result.getType()==3||result.getType()==33){
                                list=null;
                            }
                        }

                        playCommand(player,list);
                    }
                }
            }
        }
    }

    public boolean checkPlayerAuto(PdkPlayer player ,int timeout){
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
        } else {
            player.setLastCheckTime(now);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, PdkPlayer> entry : seatMap.entrySet()) {
            jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
        }
        return jsonObject.toString();
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
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
    }

    public String getGameName(){
        return "跑得快";
    }

    public static final List<Integer> wanfaList = Arrays.asList(15, 16);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    @Override
    public boolean allowRobotJoin() {
        return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""), new StringBuilder().append("|").append(modeId).append("|").toString());
    }

    private void faPaiGamLog(PdkPlayer player,int peiType) {
        StringBuilder sb = new StringBuilder("Pdk");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getName());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        if(peiType==0){
            sb.append("|").append("faPai");
        }else{
            sb.append("|").append("peiPai");
        }
        sb.append("|").append(player.getHandPais());
        sb.append("|").append(peiType);
        LogUtil.msgLog.info(sb.toString());
    }

}
