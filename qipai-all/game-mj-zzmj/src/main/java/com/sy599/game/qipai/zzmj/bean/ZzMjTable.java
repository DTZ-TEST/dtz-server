package com.sy599.game.qipai.zzmj.bean;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.zzmj.constant.ZzMjConstants;
import com.sy599.game.qipai.zzmj.rule.ZzMj;
import com.sy599.game.qipai.zzmj.rule.ZzMjRobotAI;
import com.sy599.game.qipai.zzmj.tool.ZzMjHelper;
import com.sy599.game.qipai.zzmj.tool.ZzMjQipaiTool;
import com.sy599.game.qipai.zzmj.tool.ZzMjResTool;
import com.sy599.game.qipai.zzmj.tool.ZzMjTool;
import com.sy599.game.qipai.zzmj.tool.hulib.util.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


public class ZzMjTable extends BaseTable {
    /**
     * 当前打出的牌
     */
    private List<ZzMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, ZzMjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<ZzMj> leftMajiangs;
    /*** 玩家map */
    private Map<Long, ZzMjPlayer> playerMap = new ConcurrentHashMap<Long, ZzMjPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, ZzMjPlayer> seatMap = new ConcurrentHashMap<Integer, ZzMjPlayer>();
    private List<Integer> huConfirmList = new ArrayList<>();//胡牌数组
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 摸杠的麻将
     */
    private ZzMj moGang;
    /**
     * 当前杠的人
     */
    private int moGangSeat;
    private int moGangSameCount;
    /**
     * 摸杠胡
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * 骰子点数
     **/
    private int dealDice;
    /**
     * 0点炮胡 1自摸胡
     **/
    private int dianPaoZimo;
    /**
     * 选了庄闲就算分，不选就不额外算分
     **/
    private int isCalcBanker;
    /**
     * 抓鸟个数
     **/
    private int birdNum;
    //抓鸟玩法
    private int birdType;
    /**
     * 胡7对
     **/
    private int hu7dui;

    private int isAutoPlay;//是否开启自动托管

    //private int auto_ready_time = 15000;//自动落座最大等待时间

    /**
     * 抢杠胡开关
     **/
    private int qiangGangHu;
    /**
     * 点杠可胡
     **/
    private int dianGangKeHu;
    /**
     * 抢杠胡包三家
     **/
    private int qiangGangHuBaoSanJia;
//    /**
//     * 上下中鸟
//     **/
//    private int shangZhongXiaNiao;
    /**
     * 抢杠胡算自摸
     **/
    private int qiangGangHuSuanZiMo;
    /**
     * 有炮必胡
     **/
    private int youPaoBiHu;
    /**
     * 底分
     **/
    private int diFen;
    private int huFen;
    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;

    //是否勾选首局庄家随机
    private boolean bankerRand=false;

    /*** 摸屁股的座标号*/
    private List<Integer> moTailPai = new ArrayList<>();



    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        long id = getCreateTableId(player.getUserId(), play);
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


        //0局数，1玩法Id
        payType = StringUtil.getIntValue(params, 2, 1);//支付方式
        this.huFen=(huFen<=2?huFen:1);
        isCalcBanker = StringUtil.getIntValue(params, 3, 0);//庄闲算分
        hu7dui = StringUtil.getIntValue(params, 4, 0);//可胡7对
        if (dianPaoZimo == 0) {
            qiangGangHu = StringUtil.getIntValue(params, 5, 1);//抢杠胡
        } else {
            qiangGangHu = StringUtil.getIntValue(params, 5, 0);//抢杠胡
        }
        qiangGangHuBaoSanJia = StringUtil.getIntValue(params, 6, 0);//抢杠胡包三家
        maxPlayerCount=StringUtil.getIntValue(params, 7, 4);//人数
        youPaoBiHu = StringUtil.getIntValue(params, 8, 0);//有炮必胡
        isAutoPlay = StringUtil.getIntValue(params, 9, 0);//托管
        birdType=StringUtil.getIntValue(params, 10, 0);//0：不抓鸟，2,4,6,8：抓2,4,6,8(159中鸟)鸟，10：一鸟全中，12：胡几奖几
        if (birdType>=0&&birdType<=8)
            birdNum=birdType;
        diFen = StringUtil.getIntValue(params, 11, 1);//底分
        jiaBei = StringUtil.getIntValue(params, 12, 0);
        jiaBeiFen = StringUtil.getIntValue(params, 13, 100);
        jiaBeiShu = StringUtil.getIntValue(params, 14, 1);
        dianPaoZimo = StringUtil.getIntValue(params, 15, 0);//0:是否可点炮胡
        //是否首局庄家随机
        int bankerRand=StringUtil.getIntValue(params, 16, 0);
        if(bankerRand==1){
            this.bankerRand=true;
            Random rand=new Random();
            this.lastWinSeat=rand.nextInt(maxPlayerCount) + 1;
        }



//        maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 比赛人数
//        payType = StringUtil.getIntValue(params, 2, 1);//支付方式
//        dianPaoZimo = StringUtil.getIntValue(params, 3, 0);//点炮胡 自摸胡
//        isCalcBanker = StringUtil.getIntValue(params, 4, 0);//分庄闲
//        birdNum = StringUtil.getIntValue(params, 5, 0);//抓几个鸟
//        hu7dui = StringUtil.getIntValue(params, 6, 0);//可胡7对
//        isAutoPlay = StringUtil.getIntValue(params, 8, 0);//托管
//        if (dianPaoZimo == 0) {
//            qiangGangHu = StringUtil.getIntValue(params, 9, 1);//抢杠胡
//        } else {
//            qiangGangHu = StringUtil.getIntValue(params, 9, 0);//抢杠胡
//        }
//        qiangGangHuBaoSanJia = StringUtil.getIntValue(params, 10, 0);//抢杠胡包三家
//        shangZhongXiaNiao = StringUtil.getIntValue(params, 11, 0);//上中下鸟
//        qiangGangHuSuanZiMo = StringUtil.getIntValue(params, 12, 0);//抢杠胡算自摸
//        youPaoBiHu = StringUtil.getIntValue(params, 13, 0);//有炮必胡
//        dianGangKeHu = StringUtil.getIntValue(params, 14, 0);//点杠可抢
//        diFen = StringUtil.getIntValue(params, 15, 1);


        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
//            getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }



    public int getDealDice() {
        return dealDice;
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public void setDealDice(int dealDice) {
        this.dealDice = dealDice;
    }

    public boolean isHu7dui() {
        return hu7dui == 1;
    }

    public void setHu7dui(int hu7dui) {
        this.hu7dui = hu7dui;
    }

    public int getDianPaoZimo() {
        return dianPaoZimo;
    }

    public void setDianPaoZimo(int dianPaoZimo) {
        this.dianPaoZimo = dianPaoZimo;
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
    public int isCanPlay() {
        return 0;
    }

    /**
     * 计算庄闲
     *
     * @return
     */
    public boolean isCalBanker() {
        return 1 == isCalcBanker;
    }

    @Override
    public void calcOver() {
        List<Integer> winList = new ArrayList<>(huConfirmList);
        boolean selfMo = false;
        List<Integer> cardsId=null;//鸟牌ID
        Map<Integer, Integer> seatBridMap = new HashMap<>();//位置,中鸟数
        int catchBirdSeat = 0;//抓鸟人座位
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {
            // 流局
        } else {
            // 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            // 红中麻将是否清胡
            boolean isQingHu = false;
            //抓鸟
            if (getBirdNum() > 0 && !leftMajiangs.isEmpty()) {
//                // 先抓鸟
//                birdMjIds = zhuaNiao(isQingHu, getBirdNum());
//                // 如果通炮按放炮的座位开始算
//                int startSeat = winList.size() > 1 ? disCardSeat : winList.get(0);
//                // 抓到鸟的座位
//                seatBirds = birdToSeat(birdMjIds, startSeat);
//                catchBirdSeat = startSeat;
            }
            List<ZzMj> birdCards = getBirdCards();
            ZzMj huCards=null;
            if(nowDisCardIds!=null){
                if(nowDisCardIds.size()==1){
                    huCards=nowDisCardIds.get(0);
                } else {
                    huCards=null;
                }
            }
            int birdScore = getBirdScore(birdCards, huCards);
            cardsId=getBirdCardsId(birdCards,huCards);
//            int birdPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, winList.get(0));
            if (selfMo) {
                // 看庄家抓了几个鸟
                seatBridMap.put(winList.get(0), birdScore);
                // 自摸
                int loseTotalPoint = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        int losePoint = diFen*huFen*2;

                        losePoint += birdScore;

                        if (isCalBanker() && (seat == lastWinSeat || winList.get(0) == lastWinSeat)) {
                            // 分庄闲多算一分
                            losePoint += 1;
                        }
                        loseTotalPoint += losePoint;
                        ZzMjPlayer player = seatMap.get(seat);
                        player.changeLostPoint(-losePoint);
                    }
                }
                for (int seat : winList) {
                    ZzMjPlayer player = seatMap.get(seat);
                    player.changeAction(ZzMjConstants.ACTION_COUNT_INDEX_ZIMO, 1);
                    player.changeLostPoint(loseTotalPoint);
                    player.addHuNum(1);
                }
            } else {
                // 小胡接炮 每人1分
                // 如果庄家输牌失分翻倍
                ZzMjPlayer losePlayer = seatMap.get(disCardSeat);
                losePlayer.addDianPaoNum(1);
                boolean isQiangGangHuBaoSanJia = false;
                for (int winnerSeat : winList) {
//                    int point = isTwoPlayer() ? 2 : 1;
                    int winpoint = diFen*huFen;
                    if (moGangHuList.contains(winnerSeat) && qiangGangHuBaoSanJia == 1) {
                        isQiangGangHuBaoSanJia = true;
                        winpoint += (qiangGangHuSuanZiMo == 1 ? 2 : 1);
                    }



                    //中鸟
//                    if (winList.size() > 1) {
//                        //一炮多响
//                        winpoint += seatBirds == null ? 0 : calcBirdPoint(seatBirds, disCardSeat);
//                        seatBridMap.put(disCardSeat, winpoint);
//                    } else {
//                        winpoint += seatBirds == null ? 0 : calcBirdPoint(seatBirds, winnerSeat);
//                        seatBridMap.put(winnerSeat, winpoint);
//                    }
                    winpoint+=birdScore;
                    if (isCalBanker() && (winnerSeat == lastWinSeat || losePlayer.getSeat() == lastWinSeat)) {
                        // 分庄闲多算一分
                        winpoint += 1;
                    }

                    // 胡牌
                    ZzMjPlayer winPlayer = seatMap.get(winnerSeat);
                    winPlayer.changeAction(ZzMjConstants.ACTION_COUNT_INDEX_JIEPAO, 1);
                    losePlayer.changeAction(ZzMjConstants.ACTION_COUNT_INDEX_DIANPAO, 1);
                    winPlayer.addHuNum(1);
                    seatBridMap.put(winPlayer.getSeat(),birdScore);
                    if (isQiangGangHuBaoSanJia) {
                        //抢杠胡包三家
                        winpoint *= (getMaxPlayerCount() - 1);
                        losePlayer.changeLostPoint(-winpoint);
                        winPlayer.changeLostPoint(winpoint);
                    } else {
                        losePlayer.changeLostPoint(-winpoint);
                        winPlayer.changeLostPoint(winpoint);
                    }
                }
            }
        }
        // 不管流局都加分
        for (ZzMjPlayer player : seatMap.values()) {
            player.changePoint(player.getLostPoint());
        }

        ClosingMjInfoRes.Builder res = sendAccountsMsg(playBureau == totalBureau, selfMo, winList, cardsId, null, seatBridMap, catchBirdSeat, false);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
                // 一炮多响设置放炮的人为庄家
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
        } else if (leftMajiangs.isEmpty()) {//黄庄
            setLastWinSeat(moMajiangSeat);
        }
        calcAfter();
        saveLog(playBureau == totalBureau, 0l, res.build());
        if (playBureau >= totalBureau) {
            calcOver1();
            calcOver2();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (ZzMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }

    }

    private List<Integer> getBirdCardsId(List<ZzMj> cards,ZzMj huCard){
        List<Integer> cardsId=new ArrayList<>();
        switch (birdType){
            case 2:
            case 4:
            case 6:
            case 8:
                //159抓鸟数
                if(cards!=null&&cards.size()!=0){
                    for (int i = 0; i < cards.size(); i++) {
                        cardsId.add(cards.get(i).getVal());
                    }
                }
                break;
            case 10://一鸟全中
                if(cards!=null&&cards.size()!=0){
                    cardsId.add(cards.get(0).getVal());
                }
                break;
            case 12://胡几奖几
                if (huCard!=null)
                    cardsId.add(huCard.getVal());
                break;
        }
        return cardsId;
    }

    private List<ZzMj> getBirdCards(){
        List<ZzMj> list=new ArrayList<>();
        if(birdType<=0||leftMajiangs.isEmpty())
            return list;
        switch (birdType){
            case 2:
            case 4:
            case 6:
            case 8:
                //159抓鸟数
                if(getLeftMajiangCount()>birdType){
                    list.addAll(leftMajiangs.subList(0,birdType));
                }else {
                    list.addAll(leftMajiangs);
                }
                break;
            case 10://一鸟全中
                list.add(getLeftMajiang());
                break;
            case 12://胡几奖几
                break;
        }
        return list;
    }

    /**
     *
     * @param birdCards
     * @param huCards 可以为null
     * @return
     */
    private int getBirdScore(List<ZzMj> birdCards,ZzMj huCards){
        int birdScore=0;
        switch (birdType){
            case 2:
            case 4:
            case 6:
            case 8:
                //159抓鸟数
                birdScore=birdCards.size();
                break;
            case 10://一鸟全中
                if (birdCards!=null&&birdCards.size()>0)
                    birdScore=(birdCards.get(0).getVal())%10;
                break;
            case 12://胡几奖几
                if (huCards!=null)
                    birdScore=huCards.getVal()%10;
                break;
        }
        return birdScore;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
        Date now = TimeUtil.now();
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setTableId(id);
        userLog.setRes(logRes);
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
//		userLog.setMasterName(getMasterName());
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (ZzMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    public String getMasterName() {
        Player master = PlayerManager.getInstance().getPlayer(creatorId);
        String masterName = "";
        if (master == null) {
            masterName = UserDao.getInstance().selectNameByUserId(creatorId);
        } else {
            masterName = master.getName();
        }
        return masterName;
    }

    private int calcBirdPoint(int[] seatBirdArr, int seat) {
        return seatBirdArr[seat];
    }

    /**
     * 抓鸟
     *
     * @return
     */
    private int[] zhuaNiao(boolean isQingHu, int birdNum) {
        int[] birdMjIds = null;
        if (birdNum == 10) {
            ZzMj birdMj = getLeftMajiang();
            if (birdMj != null) {
                if (birdMj.isFeng() || birdMj.isZhongFaBai()) {
                    birdMjIds = new int[1];
                    birdMjIds[0] = birdMj.getId();
                } else {
                    birdMjIds = new int[3];
                    int[] birdMjVals = new int[3];
                    if (birdMj.getVal() % 10 == 1) {
                        birdMjVals[0] = birdMj.getHuase() * 10 + 9;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    } else if (birdMj.getVal() % 10 == 9) {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getHuase() * 10 + 1;
                    } else {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    }
                    for (int i = 0; i < birdMjVals.length; i++) {
                        if (i == 1) {
                            birdMjIds[i] = birdMj.getId();
                        } else {
                            ZzMj mj = ZzMj.getMajiangByValue(birdMjVals[i]);
                            if (mj != null) {
                                birdMjIds[i] = mj.getId();
                            }
                        }
                    }
                }
            }
        } else {
            birdNum = isQingHu ? birdNum + 1 : birdNum;
            if (birdNum > leftMajiangs.size()) {
                birdNum = leftMajiangs.size();
            }
            // 先砸鸟
            birdMjIds = new int[birdNum];
            for (int i = 0; i < birdNum; i++) {
                ZzMj birdMj = getLeftMajiang();
                if (birdMj != null) {
                    birdMjIds[i] = birdMj.getId();
                }
            }
        }
        return birdMjIds;
    }

    /**
     * 中鸟
     *
     * @param birdMjIds
     * @param winSeat
     * @return arr[seat] = 中鸟数
     */
    private int[] birdToSeat(int[] birdMjIds, int winSeat) {
        int[] seatArr = new int[getMaxPlayerCount() + 1];
        if (birdNum == 10) {
            Map<Integer, Integer> mjValCountMap = new HashMap();
            for (int birdMjId : birdMjIds) {
                ZzMj mj = ZzMj.getMajang(birdMjId);
                if (mjValCountMap.containsKey(mj.getVal())) {
                    mjValCountMap.put(mj.getVal(), mjValCountMap.get(mj.getVal()) + 1);
                } else {
                    mjValCountMap.put(mj.getVal(), 1);
                }
            }
            ZzMjPlayer player = seatMap.get(winSeat);
            List<ZzMj> allMj = new ArrayList(player.getHandMajiang());
            allMj.addAll(player.getGang());
            allMj.addAll(player.getPeng());
            allMj.addAll(player.getaGang());
            for (ZzMj handMj : allMj) {
                if (mjValCountMap.containsKey(handMj.getVal())) {
                    seatArr[player.getSeat()] = seatArr[player.getSeat()] + mjValCountMap.get(handMj.getVal());
                }
            }
        } else {
            for (int i = 0; i < birdMjIds.length; i++) {
                ZzMj mj = ZzMj.getMajang(birdMjIds[i]);
                if (mj != null && !mj.isFeng() && !mj.isZhongFaBai()) {
                    int birdMjVal = mj.getPai();
                    birdMjVal = (birdMjVal - 1) % 4;// 从自己开始算 所以减1
                    int birdSeat = birdMjVal + winSeat > 4 ? birdMjVal + winSeat - 4 : birdMjVal + winSeat;
//					seatArr[birdSeat] = seatArr[birdSeat] + 1;
                    if (birdSeat == winSeat) {
                        seatArr[birdSeat] = seatArr[birdSeat] + 1;
                    }
                }
            }
        }
        return seatArr;
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
                tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai4")) {
                tempMap.put("handPai4", StringUtil.implode(seatMap.get(4).getHandPais(), ","));
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(ZzMjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(ZzMjHelper.toMajiangIds(leftMajiangs), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (ZzMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, birdNum);
        wrapper.putInt(7, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(8, moGang.getId());
        } else {
            wrapper.putInt(8, 0);
        }
        wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
        wrapper.putInt(10, dianPaoZimo);
        wrapper.putInt(11, isCalcBanker);
        wrapper.putInt(12, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(13, maxPlayerCount);
        wrapper.putInt(14, dealDice);
        wrapper.putInt(15, isAutoPlay);
        wrapper.putInt(16, qiangGangHu);
        wrapper.putInt(17, qiangGangHuBaoSanJia);
        wrapper.putInt(18, 0);
        wrapper.putInt(19, qiangGangHuSuanZiMo);
        wrapper.putInt(20, youPaoBiHu);
        wrapper.putInt(21, dianGangKeHu);
        wrapper.putInt(22, moGangSeat);
        wrapper.putInt(23, moGangSameCount);
        wrapper.putString(24, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(25, diFen);
        return wrapper;
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
        int dealDice = 0;
        Random r = new Random();
        dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + ZzMjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        // 天胡或者暗杠
        logFaPaiTable();
        for (ZzMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            if (lastWinSeat == tablePlayer.getSeat()) {
                List<Integer> actionList = tablePlayer.checkMo(null);
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
//			if (userId == tablePlayer.getUserId()) {
//				continue;
//			}
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            logFaPaiPlayer(tablePlayer, null);
        }
        for (Player player : getRoomPlayerMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
            player.writeSocket(res.build());
        }
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    public void moMajiang(ZzMjPlayer player, boolean isBuZhang) {
        if (state != table_state.play) {
            return;
        }
        if (player.isRobot()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 摸牌
        ZzMj majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            majiang = getLeftMajiang();
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + ZzMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
        }
        // 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        if (isBuZhang) {
            addMoTailPai(-1);
        }

        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang);
        if (!arr.isEmpty()) {
            addActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        for (ZzMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                if (majiang != null) {
                    copy.setMajiangId(majiang.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || (actionList.get(ZzMjConstants.ACTION_INDEX_HU) != 1 && actionList.get(ZzMjConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
//		if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {// 检查优先度，胡杠碰吃 如果同时出现一个事件，按出牌座位顺序优先
//			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//			LogUtil.msg("有优先级更高的操作需等待！");
//			return;
//		}//一炮多响去掉
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<ZzMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        ZzMjPlayer fangPaoPlayer;
        if (!zimo) {
            if (moGangHuList.contains(player.getSeat())) {
                // 抢杠胡
                huHand.add(moGang);
                builder.setFromSeat(nowDisCardSeat);
                builder.addHuArray(ZzMjConstants.HU_QIANGGANGHU);
                player.getHuType().add(ZzMjConstants.HU_QIANGGANGHU);
                fangPaoPlayer = seatMap.get(nowDisCardSeat);
                fangPaoPlayer.addGongGangNum(-1);
                fangPaoPlayer.getHuType().add(ZzMjConstants.HU_FANGPAO);
            } else {
                // 放炮
                huHand.addAll(nowDisCardIds);
                builder.setFromSeat(disCardSeat);
                player.getHuType().add(ZzMjConstants.HU_JIPAO);
                fangPaoPlayer = seatMap.get(disCardSeat);
                fangPaoPlayer.getHuType().add(ZzMjConstants.HU_FANGPAO);
            }
        } else {
            builder.addHuArray(ZzMjConstants.HU_ZIMO);
            player.getHuType().add(ZzMjConstants.HU_ZIMO);
        }
        if (!ZzMjTool.isHu(huHand, isHu7dui())) {
            return;
        }
        if (moGangHuList.contains(player.getSeat())) {
            ZzMjPlayer moGangPlayer = seatMap.get(moGangSeat);
            if (moGangPlayer == null) {
                moGangPlayer = getPlayerByHasMajiang(moGang);
            }
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<ZzMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, ZzMjDisAction.action_chupai, 0);
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
//			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + ZzMjDisAction.action_chupai + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            moGangPlayer.qGangUpdateOutPais(moGang);
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // 胡
        for (ZzMjPlayer seat : seatMap.values()) {
            // 推送消息
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        // 加入胡牌数组
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<ZzMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<ZzMj> majiangs) {
        ZzMjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * 找出拥有这张麻将的玩家
     *
     * @param majiang
     * @return
     */
    private ZzMjPlayer getPlayerByHasMajiang(ZzMj majiang) {
        for (ZzMjPlayer player : seatMap.values()) {
            if (player.getHandMajiang() != null && player.getHandMajiang().contains(majiang)) {
                return player;
            }
            if (player.getOutMajing() != null && player.getOutMajing().contains(majiang)) {
                return player;
            }
        }
        return null;
    }

    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        if (!huActionList.isEmpty()) {
            over = true;
            ZzMjPlayer moGangPlayer = null;
            if (!moGangHuList.isEmpty()) {
                // 如果有抢杠胡
                moGangPlayer = seatMap.get(moGangSeat);
                LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);

            }
            for (int huseat : huActionList) {
                if (moGangPlayer != null) {
                    // 被抢杠的人可以胡的话 跳过
                    if (moGangPlayer.getSeat() == huseat) {
                        continue;
                    }
                }
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == ZzMjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            ZzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                ZzMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        for (ZzMjPlayer player : seatMap.values()) {
            if (player.isAlreadyMoMajiang() && !huConfirmList.contains(player.getSeat())) {
                over = false;
            }
        }

        return over;
    }

    /**
     * 碰杠
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chiPengGang(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }
        if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {
            LogUtil.msg("有优先级更高的操作需等待！");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        ZzMj disMajiang = null;
        if (nowDisCardIds.size() > 1) {
            // 当前出牌不能操作
            return;
        }
        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());
        if (!huList.isEmpty()) {
            return;
        }
        if (!nowDisCardIds.isEmpty()) {
            disMajiang = nowDisCardIds.get(0);
        }
        int sameCount = 0;
        if (majiangs.size() > 0) {
            sameCount = ZzMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
        // 如果是杠 后台来找出是明杠还是暗杠
        if (action == ZzMjDisAction.action_minggang) {
            majiangs = ZzMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // 有4张一样的牌是暗杠
                action = ZzMjDisAction.action_angang;
            }
            // 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == ZzMjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == ZzMjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            player.addAnGangNum(1);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == ZzMjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            player.addGongGangNum(1);
            ArrayList<ZzMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

            // 特殊处理一张牌明杠的时候别人可以胡
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
            //点杠可枪
            if (sameCount == 3 && dianGangKeHu == 1) {
                if (checkQGangHu(player, mjs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == ZzMjDisAction.action_minggang && sameCount == 3)
                    || action == ZzMjDisAction.action_peng || action == ZzMjDisAction.action_chi) {
                if (action == ZzMjDisAction.action_chi) {
                    majiangs.add(1, disMajiang);// 吃的牌放第二位
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds, action);
            }
        }
        chiPengGang(builder, player, majiangs, action, hasQGangHu, sameCount);
    }

    /**
     * 抢杠胡
     *
     * @param player
     * @param majiangs
     * @param action
     * @return
     */
    private boolean checkQGangHu(ZzMjPlayer player, List<ZzMj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (ZzMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            // 推送消息
            List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu() || dianGangKeHu == 1);
            if (!hu.isEmpty() && hu.get(0) == 1) {
                addActionSeat(seatPlayer.getSeat(), hu);
                huListMap.put(seatPlayer.getSeat(), hu);
            }
        }
        // 可以胡牌
        if (!huListMap.isEmpty()) {
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()), player, sameCount);
            buildPlayRes(builder, player, action, majiangs);
            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
                PlayMajiangRes.Builder copy = builder.clone();
                ZzMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, ZzMjPlayer player, List<ZzMj> majiangs, int action, boolean hasQGangHu, int sameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == ZzMjDisAction.action_peng && actionList.get(ZzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == ZzMjDisAction.action_chi || action == ZzMjDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (ZzMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if (!hasQGangHu && action == ZzMjDisAction.action_minggang || action == ZzMjDisAction.action_angang) {
            // 明杠和暗杠摸牌
            moMajiang(player, true);
        }
        robotDealAction();
        logAction(player, action, majiangs, actList);
    }

    /**
     * 普通出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chuPai(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
            LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
            // 还没有摸牌
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!actionSeatMap.isEmpty()) {//出牌自动过掉手上操作
            guo(player, null, ZzMjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        // 普通出牌
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (ZzMjPlayer seat : seatMap.values()) {
            List<Integer> list;
            if (seat.getUserId() != player.getUserId()) {
                list = seat.checkDisMajiang(majiangs.get(0), this.canDianPao());
                if (list.contains(1)) {
                    addActionSeat(seat.getSeat(), list);
                    seat.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(seat, majiangs.get(0), list);
                }
            }
        }
        sendDisMajiangAction(builder, player);

        // 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(ZzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(ZzMjConstants.ACTION_INDEX_ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, ZzMjPlayer player) {
        for (ZzMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
            // 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            if (seatPlayer.getSeat() == player.getSeat()) {
                copy.addExt(ZzMjTool.isTing(seatPlayer.getHandMajiang(), isHu7dui()) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(ZzMjPlayer player, List<ZzMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
        // 被人抢杠胡
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
                // 自己杠的时候被人抢杠胡了 不能做其他操作
                return;
            }
        }

        if (ZzMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
        // 手上没有要出的麻将
        if (action != ZzMjDisAction.action_minggang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        changeDisCardRound(1);
        if (action == ZzMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
        // 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void passMoHu(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        //ZzMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            ZzMjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, ZzMjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, ZzMjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
            }

            moMajiang(moGangPlayer, true);
//			builder = PlayMajiangRes.newBuilder();
//			chiPengGang(builder, moGangPlayer, majiangs, ZzMjDisAction.action_minggang, false);
        }

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(ZzMjPlayer player, List<ZzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!moGangHuList.isEmpty()) {
            // 有摸杠胡的优先处理
            passMoHu(player, majiangs, action);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            ZzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    ZzMjPlayer seatPlayer = seatMap.get(seat);
                    seatPlayer.writeSocket(copy.build());
                }
            }
        }
        refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        checkMo();
    }

    private void calcPoint(ZzMjPlayer player, int action, int sameCount, List<ZzMj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == ZzMjDisAction.action_peng) {
            return;

        } else if (action == ZzMjDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == ZzMjDisAction.action_minggang) {
            if (sameCount == 1) {
                // 碰牌之后再抓一个牌每人出1分
                // 放杠的人出3分

                if (player.isPassGang(majiangs.get(0))) {
                    // 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
                    return;
                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);
            }
            // 放杠
            if (sameCount == 3) {
                ZzMjPlayer disPlayer = seatMap.get(disCardSeat);
                //disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
                int point = (getMaxPlayerCount() - 1);
                disPlayer.changeLostPoint(-point);
                seatPointArr[disPlayer.getSeat()] = -point;
                player.changeLostPoint(point);
                seatPointArr[player.getSeat()] = point;
            }
        }

        if (lostPoint != 0) {
            for (ZzMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
                    player.changeLostPoint(getPoint);
                    seatPointArr[player.getSeat()] = getPoint;
                } else {
                    seat.changeLostPoint(lostPoint);
                    seatPointArr[seat.getSeat()] = lostPoint;
                }
            }
        }

        String seatPointStr = "";
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            seatPointStr += seatPointArr[i] + ",";
        }
        seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
        ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen, seatPointStr);
        GeneratedMessage msg = res.build();
        broadMsgToAll(msg);

        if (action != ZzMjDisAction.action_chi) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<ZzMj> majiangs, ZzMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<ZzMj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<ZzMj> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
     * 检查摸牌
     */
    public void checkMo() {
        if (actionSeatMap.isEmpty()) {
            if (nowDisCardSeat != 0) {
                moMajiang(seatMap.get(nowDisCardSeat), false);
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                ZzMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<ZzMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = ZzMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(ZzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(ZzMjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<ZzMj>(), ZzMjDisAction.action_hu);

                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, ZzMjDisAction.action_angang);

                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, ZzMjDisAction.action_minggang);

                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, ZzMjDisAction.action_peng);
                    }
                }
                // else {
                // // 是玩家需要发送消息
                // player.writeSocket(builder.build());
                // }

            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            ZzMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<ZzMj> list = null;
                    if (actionList.get(ZzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(ZzMjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<ZzMj>(), ZzMjDisAction.action_hu);
                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = ZzMjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = ZzMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, ZzMjDisAction.action_angang);

                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = ZzMjHelper.toMajiangValMap(next.getPeng());
                        for (ZzMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, ZzMjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(ZzMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, ZzMjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    ZzMjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = ZzMjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<ZzMj> majiangList = ZzMjHelper.toMajiang(Arrays.asList(maJiangId));
                    if (next.isRobot()) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    playCommand(next, majiangList, 0);
                }

            }
        }
    }

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            if (seatMap.containsKey(masterId)) {
                setLastWinSeat(playerMap.get(masterId).getSeat());
            } else {
                setLastWinSeat(1);
            }
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);

//        List<Integer> copy = ZzMjConstants.getMajiangList(getMaxPlayerCount());
        List<Integer> copy = ZzMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<ZzMj>> list = null;
        if (zp != null) {
            list = ZzMjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = ZzMjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (ZzMjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
        // 桌上剩余的牌
        setLeftMajiangs(list.get(getMaxPlayerCount()));
    }

    @Override
    public void startNext() {
        // 直接胡牌
        // autoZiMoHu();
    }

    /**
     * 初始化桌子上剩余牌
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<ZzMj> leftMajiangs) {
        if (leftMajiangs == null) {
            this.leftMajiangs.clear();
        } else {
            this.leftMajiangs = leftMajiangs;

        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    /**
     * 剩余牌的第一张
     *
     * @return
     */
    public ZzMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            ZzMj majiang = this.leftMajiangs.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return majiang;
        }
        return null;
    }

    @Override
    public int getNextDisCardSeat() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) {
            return lastWinSeat;
        } else {
            return nowDisCardSeat;
        }
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        return seat + 1 > maxPlayerCount ? 1 : seat + 1;
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
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        res.addExt(payType);                //0
        res.addExt(birdNum);                //1
        res.addExt(dianPaoZimo);            //2
        res.addExt(isCalcBanker);           //3
        res.addExt(hu7dui);                 //4
        res.addExt(isAutoPlay);             //5
        res.addExt(qiangGangHu);            //6
        res.addExt(qiangGangHuBaoSanJia);   //7
        res.addExt(0);      //8
        res.addExt(qiangGangHuSuanZiMo);    //9
        res.addExt(youPaoBiHu);             //10
        res.addExt(dianGangKeHu);           //11
        res.addExt(diFen);                  //12

        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (ZzMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && ZzMjTool.isTing(player.getHandMajiang(), isHu7dui())) {
                        playerRes.setUserSate(3);
                    }
                }
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null && moGangHuList.isEmpty()) {
                playerRes.addAllOutCardIds(ZzMjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
                if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// 如果已做临时操作 则不发送前端可做的操作 或者已经操作胡了
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        if (actionSeatMap.isEmpty()) {
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
        } else if (!moGangHuList.isEmpty()) {
            for (ZzMjPlayer player : seatMap.values()) {
                if (player.getmGang() != null && player.getmGang().contains(moGang)) {
                    res.setNextSeat(player.getSeat());
                    break;
                }
            }
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) ZzMjConstants.AUTO_TIMEOUT);
        return res.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    protected void initNext1() {
        clearHuList();
        clearActionSeatMap();
        setLeftMajiangs(null);
        setNowDisCardIds(null);
        clearMoGang();
        setDealDice(0);
        clearMoTailPai();
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        if (moGangHuList.contains(seat)) {
            removeMoGang(seat);
        }
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        ZzMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + ZzMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
        saveActionSeatMap();
    }

    public void clearActionSeatMap() {
        if (!actionSeatMap.isEmpty()) {
            actionSeatMap.clear();
            saveActionSeatMap();
        }
    }

    private void clearTempAction() {
        if (!tempActionMap.isEmpty()) {
            tempActionMap.clear();
            changeExtend();
        }
    }

    public void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void addHuList(int seat) {
        if (!huConfirmList.contains(seat)) {
            huConfirmList.add(seat);

        }
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        for (int i = 1; i <= 4; i++) {
            String val = wrapper.getString(i);
            if (!StringUtils.isBlank(val)) {
                actionSeatMap.put(i, StringUtil.explodeToIntList(val));

            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            nowDisCardIds = ZzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = ZzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (ZzMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        birdNum = wrapper.getInt(6, 0);
        moMajiangSeat = wrapper.getInt(7, 0);
        int moGangMajiangId = wrapper.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = ZzMj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        dianPaoZimo = wrapper.getInt(10, 1);
        isCalcBanker = wrapper.getInt(11, 0);
        hu7dui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        isAutoPlay = wrapper.getInt(15, 0);
        if (dianPaoZimo == 0) {
            qiangGangHu = wrapper.getInt(16, 1);
        } else {
            qiangGangHu = wrapper.getInt(16, 0);
        }
        qiangGangHuBaoSanJia = wrapper.getInt(17, 0);
        qiangGangHuSuanZiMo = wrapper.getInt(19, 0);
        youPaoBiHu = wrapper.getInt(20, 0);
        dianGangKeHu = wrapper.getInt(21, 0);
        moGangSeat = wrapper.getInt(22, 0);
        moGangSameCount = wrapper.getInt(23, 0);

        String moTailPaiStr = wrapper.getString(24);
        if (!StringUtils.isBlank(moTailPaiStr)) {
            moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
        }
        diFen = wrapper.getInt(25, 1);
    }

    private Map<Integer, ZzMjTempAction> loadTempActionMap(String json) {
        Map<Integer, ZzMjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            ZzMjTempAction tempAction = new ZzMjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(ZzMjPlayer player, List<ZzMj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (!canAction) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new ZzMjTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后  选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (ZzMjTempAction temp : tempActionMap.values()) {
                    if (temp.getAction() < maxAction) {
                        maxAction = temp.getAction();
                        maxSeat = temp.getSeat();
                    }
                    prioritySeats.put(temp.getSeat(), temp.getAction());
                }
                Set<Integer> maxPrioritySeats = new HashSet<>();
                for (int mActionSet : prioritySeats.keySet()) {
                    if (prioritySeats.get(mActionSet) == maxAction) {
                        maxActionSize++;
                        maxPrioritySeats.add(mActionSet);
                    }
                }
                if (maxActionSize > 1) {
                    maxSeat = getNearSeat(disCardSeat, new ArrayList<>(maxPrioritySeats));
                    maxAction = prioritySeats.get(maxSeat);
                }
                ZzMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<ZzMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
                List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeActionSeat(removeSeat);
                    }
                }
                clearTempAction();
                playCommand(tempPlayer, tempCardList, tempHuCards, maxAction);// 系统选取优先级最高操作
            } else {
                if (isCalcOver()) {
                    calcOver();
                }
            }
        } else {// 能操作 清理所有临时操作
            clearTempAction();
        }
        return canAction;
    }

    /**
     * 执行可做操作里面优先级最高的玩家操作
     *
     * @param player
     */
    private void refreshTempAction(ZzMjPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = ZzMjDisAction.parseToDisActionList(actionList);
            int priorityAction = ZzMjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
        boolean isSame = true;//是否有相同操作
        for (int seat : prioritySeats.keySet()) {
            if (maxPrioritySeat != Integer.MAX_VALUE && maxPrioritySeat != prioritySeats.get(seat)) {
                isSame = false;
            }
            if (prioritySeats.get(seat) < maxPriorityAction) {
                maxPriorityAction = prioritySeats.get(seat);
                maxPrioritySeat = seat;
            }
        }
        if (isSame) {
            maxPrioritySeat = getNearSeat(disCardSeat, new ArrayList<>(prioritySeats.keySet()));
        }
        Iterator<ZzMjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            ZzMjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<ZzMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                ZzMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
                playCommand(tempPlayer, tempCardList, tempHuCards, action);// 系统选取优先级最高操作
                break;
            }
        }
        changeExtend();
    }

    /**
     * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
     *
     * @param player
     * @param action
     * @return
     */
    public boolean checkCanAction(ZzMjPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = ZzMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = ZzMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = ZzMjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
                    // 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(nowDisCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 是否能碰
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canPeng(ZzMjPlayer player, List<ZzMj> majiangs, int sameCount) {
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        if (sameCount != 2) {
            return false;
        }
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        if (majiangs.get(0).getVal() != nowDisCardIds.get(0).getVal()) {
            return false;
        }
        return true;
    }

    /**
     * 是否能明杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canAnGang(ZzMjPlayer player, List<ZzMj> majiangs, int sameCount) {
        if (sameCount != 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        return true;
    }

    /**
     * 是否能暗杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(ZzMjPlayer player, List<ZzMj> majiangs, int sameCount) {
        List<ZzMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = ZzMjHelper.toMajiangVals(player.getPeng());
        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount != 3) {
                return false;
            }
            if (nowDisCardIds.size() != 1 || nowDisCardIds.get(0).getVal() != majiangs.get(0).getVal()) {
                return false;
            }
            return true;
        }

        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }

    public int getBirdNum() {
        return birdNum;
    }

    public void setBirdNum(int birdNum) {
        this.birdNum = birdNum;
    }

    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        changeExtend();
    }

    /**
     * 摸杠别人可以胡
     *
     * @param moGang
     * @param moGangHuList
     */
    public void setMoGang(ZzMj moGang, List<Integer> moGangHuList, ZzMjPlayer player, int sameCount) {
        this.moGang = moGang;
        this.moGangHuList = moGangHuList;
        this.moGangSeat = player.getSeat();
        this.moGangSameCount = sameCount;
        changeExtend();
    }

    /**
     * 清除摸刚胡
     */
    public void clearMoGang() {
        this.moGang = null;
        this.moGangHuList.clear();
        this.moGangSeat = 0;
        this.moGangSameCount = 0;
        changeExtend();
    }

    /**
     * pass 摸杠胡
     *
     * @param seat
     */
    public void removeMoGang(int seat) {
        this.moGangHuList.remove((Object) seat);
        changeExtend();
    }

    public int getMoMajiangSeat() {
        return moMajiangSeat;
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            wrapper.putString(entry.getKey(), StringUtil.implode(entry.getValue(), ","));
        }
        return wrapper.toString();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
     * 能抢杠胡
     *
     * @return
     */
    public boolean canGangHu() {
        return qiangGangHu == 1;
    }

    // 能否点炮
    public boolean canDianPao() {
        if (getDianPaoZimo() == 0) {
            return true;
        }
        return false;
    }

    /**
     * @param over
     * @param selfMo
     * @param winList
     * @param prickBirdMajiangIds 鸟ID
     * @param seatBirds           鸟位置
     * @param seatBridMap         鸟分
     * @param isBreak
     * @return
     */
    public ClosingMjInfoRes.Builder sendAccountsMsg1(boolean over, boolean selfMo, List<Integer> winList, int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {
        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        for (ZzMjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.setBirdPoint(seatBridMap.get(player.getSeat()));
            } else {
                build.setBirdPoint(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                if (!selfMo) {
                    // 不是自摸
                    ZzMj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
                }
            }
            if (winList != null && winList.contains(player.getSeat())) {
                // 手上没有剩余的牌放第一位为赢家
                list.add(0, build.build());
            } else {
                list.add(build.build());
            }
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt());
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (prickBirdMajiangIds != null) {
            res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
        }
        res.addAllLeftCards(ZzMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        for (ZzMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;
    }

    /**
     * @param over
     * @param selfMo
     * @param winList
     * @param birdCardsId 鸟ID
     * @param seatBirds           鸟位置
     * @param seatBridMap         鸟分
     * @param isBreak
     * @return
     */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, List<Integer> birdCardsId, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {

        //大结算计算加倍分
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (ZzMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (ZzMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;
        for (ZzMjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.setBirdPoint(seatBridMap.get(player.getSeat()));
            } else {
                build.setBirdPoint(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                if (!selfMo) {
                    // 不是自摸
                    ZzMj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                // 手上没有剩余的牌放第一位为赢家
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }
            //信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();
            int dyjCredit = 0;
            for (ZzMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                ZzMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
                list.add(builder.build());
            }
        } else {
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {

                list.add(builder.build());
            }
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt());
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3ClosingMjInfoRes
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (birdCardsId != null) {
            res.addAllBird(birdCardsId);
        }
        res.addAllLeftCards(ZzMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        for (ZzMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;

    }


    public List<String> buildAccountsExt() {
        List<String> ext = new ArrayList<>();
        if (isGroupRoom()) {
            ext.add(loadGroupId());
        } else {
            ext.add("0");
        }
        ext.add(id + "");                                //1
        ext.add(masterId + "");                            //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                            //4
        ext.add(dianPaoZimo + "");                        //5
        ext.add(birdNum + "");                            //6
        ext.add(isCalcBanker + "");                        //7
        ext.add(hu7dui + "");                            //8
        ext.add(isAutoPlay + "");                        //9
        ext.add(qiangGangHu + "");                        //10
        ext.add(qiangGangHuBaoSanJia + "");                //11
        ext.add(0 + "");                //12
        ext.add(qiangGangHuSuanZiMo + "");                //13
        ext.add(youPaoBiHu + "");                        //14
        ext.add(dianGangKeHu + "");                        //15
        ext.add(diFen + "");                            //16
        ext.add(isLiuJu() + "");                        //17
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return ZzMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return super.isTest() && ZzMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        ((ZzMjPlayer) player).checkAutoPlay(0, true);
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            return;
        }
        if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            for (ZzMjPlayer player : seatMap.values()) {
                if (player.checkAutoPlay(1, false)) {
                    autoReady(player);
                }
            }
        }

    }

    /**
     * 自动出牌
     */
    public synchronized void autoPlay() {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.isEmpty()) {
            List<Integer> huSeatList = getHuSeatByActionMap();
            if (!huSeatList.isEmpty()) {
                //有胡处理胡
                for (int seat : huSeatList) {
                    ZzMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), ZzMjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = ZzMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = ZzMjDisAction.getAutoMaxPriorityAction(actList);
                    ZzMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == ZzMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), ZzMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                ZzMj mj = nowDisCardIds.get(0);
                                List<ZzMj> mjList = new ArrayList<>();
                                for (ZzMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, ZzMjDisAction.action_peng);
//							autoChuPai(player);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), ZzMjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            ZzMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(ZzMjPlayer player) {

        //ZzMjQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = ZzMjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = handMjIds.get(handMjIds.size() - 1);
        } else {
            Collections.sort(handMjIds);
            mjId = handMjIds.get(handMjIds.size() - 1);
        }
        if (mjId != -1) {
            List<ZzMj> mjList = ZzMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, ZzMjDisAction.action_chupai);
        }
    }

    public int getIsCalcBanker() {
        return isCalcBanker;
    }

    public void setIsCalcBanker(int isCalcBanker) {
        this.isCalcBanker = isCalcBanker;
    }

    @Override
    public boolean isCanJoin0(Player player) {
        if (getPayType() == 2 || getPayType() == 3) {
            //房主支付
            return true;
        }
        int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), getPayType() == 1 ? 0 : 1);
        if (needCards < 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return false;
        }
        // 钻石不足不能加入房间
        if (!player.isRobot() && player.getFreeCards() + player.getCards() < needCards) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return false;
        }
        return true;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }



    // 是否两人麻将
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    public void autoReady(Player player) {
        if (playBureau > 1) {
            if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                return;
            }
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
            GeneratedMessage playerReadyMsg = com.build();
            this.ready(player);
            for (Player seatPlayer : seatMap.values()) {
                if (seatPlayer.getUserId() == player.getUserId()) {
                    continue;
                }
                seatPlayer.writeSocket(playerReadyMsg);
            }
            for (Player roomPlayer : this.getRoomPlayerMap().values()) {
                roomPlayer.writeSocket(playerReadyMsg);
            }
            player.writeComMessage(WebSocketMsgType.res_code_isstartnext);
            if (this.isTest()) {
                for (Player tableplayer : seatMap.values()) {
                    if (tableplayer.isRobot()) {
                        this.ready(tableplayer);
                    }
                }
            }

            ready();
            checkDeal(player.getUserId());
            TableRes.CreateTableRes.Builder msg = buildCreateTableRes(player.getUserId(), true, false).toBuilder();
            if (getState() == SharedConstants.table_state.play) {
                //点下一局，触发发牌时设置为1，前端用来判断是否播放发牌动作
                msg.setFromOverPop(1);
            }
            player.writeSocket(msg.build());
            for (Player roomPlayer : getRoomPlayerMap().values()) {
                TableRes.CreateTableRes.Builder msg0 = buildCreateTableRes(roomPlayer.getUserId(), true, false).toBuilder();
                msg0.setFromOverPop(0);
                roomPlayer.writeSocket(msg0.build());
            }
        }
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_zzmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
        HuUtil.init();
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(ZzMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(ZzMjPlayer player, int action, List<ZzMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        String actStr = "unKnown-" + action;
        if (action == ZzMjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == ZzMjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == ZzMjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == ZzMjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == ZzMjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == ZzMjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(ZzMjPlayer player, ZzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("moPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(ZzMjPlayer player, ZzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(ZzMjPlayer player, List<ZzMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("ZzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getAutoPlayCheckedTime());
        sb.append("|").append("huPai");
        sb.append("|").append(mjs);
        sb.append("|").append(daHuNames);
        LogUtil.msg(sb.toString());
    }

    public String actListToString(List<Integer> actList) {
        if (actList == null || actList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < actList.size(); i++) {
            if (actList.get(i) == 1) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                if (i == ZzMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == ZzMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == ZzMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == ZzMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == ZzMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == ZzMjConstants.ACTION_INDEX_ZIMO) {
                    sb.append("ziMo");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 桌上剩余的牌数
     *
     * @return
     */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }

    public void addMoTailPai(int gangDice) {
        int leftMjCount = getLeftMajiangCount();
        int startIndex = 0;
        if (moTailPai.contains(0)) {
            int lastIndex = moTailPai.get(0);
            for (int i = 1; i < moTailPai.size(); i++) {
                if (moTailPai.get(i) == lastIndex + 1) {
                    lastIndex++;
                } else {
                    break;
                }
            }
            startIndex = lastIndex + 1;
        }
        if (gangDice == -1) {
            //补张，取一张
            for (int i = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
            //开杠打色子，取两张
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
                    j++; //取到第几剁
                }
                if (moTailPai.contains(nowIndex)) {
                    if (nowIndex % 2 == 1) {
                        duo++;
                        leftMjCount = leftMjCount + 2;
                    }
                } else {
                    if (j == duo) {
                        moTailPai.add(nowIndex);
                        moTailPai.add(nowIndex - 1);
                        break;
                    }

                }
            }

        }
        Collections.sort(moTailPai);
        changeExtend();
    }

    /**
     * 清除摸屁股
     */
    public void clearMoTailPai() {
        this.moTailPai.clear();
        changeExtend();
    }

    /**
     * 是否流局
     *
     * @return
     */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }
}
