package com.sy599.game.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.credit.CreditCommission;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.group.*;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupCreditDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.dao.group.GroupWarnDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.com.LuckyRedbagCommand;
import com.sy599.game.gcommand.com.activity.NewPlayerGiftActivityCmd;
import com.sy599.game.gcommand.com.activity.OldBackGiftActivityCmd;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.*;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg.BaiRenTableRes;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.ActivityConfigInfo;
import com.sy599.game.staticdata.model.ActivityBean;
import com.sy599.game.staticdata.model.GameReBate;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseTable {
    protected static final int JSON_TAG = 1;
    protected volatile int playType;
    /*** 当前牌桌状态 */
    protected volatile table_state state;
    /*** 牌桌Id */
    protected volatile long id;
    /*** 房间id(暂定) */
    protected volatile int roomId;
    /*** 创建人*/
    protected volatile long creatorId;
    /*** 房主 */
    protected volatile long masterId;
    /*** 创建时间 */
    protected Date createTime;
    /*** 总局数 */
    protected volatile int totalBureau;
    /*** 当前玩的局数 */
    protected volatile int playBureau;
    /*** 当前完成局数 */
    protected volatile int finishBureau=0;
    /*** 出牌的轮数 */
    protected volatile int disCardRound;
    /*** 当前要出牌的座位 */
    protected volatile int nowDisCardSeat;
    /*** 当前出牌人的座位 */
    protected volatile int disCardSeat;
    /*** 应答解散请求玩家的答案 */
    protected Map<Integer, Integer> answerDissMap = new LinkedHashMap<>();
    protected volatile String playLog = "";
    protected Map<String, Object> dbParamMap = new ConcurrentHashMap<>();
    // public static final int max_player_count = 4;
    protected volatile int lastWinSeat = 0;
    protected long gotyeRoomId;
    protected boolean isRuning;
    protected List<Integer> config;
    private long sendDissTime;
    /*** 做出玩家动作的时间 动作超时用 */
    protected volatile long lastActionTime;
    protected int isCompetition;
    protected TableInf tableInf;
    protected List<List<Integer>> zp;
    /**
     * 玩家摸牌做牌
     */
    protected Map<Long, Integer> zpMap = new HashMap<>();
    protected volatile long daikaiTableId;// 代开房间ID
    protected volatile boolean tiqianDiss = false;
    protected int specialDiss = 0;
    private ReentrantLock lock;
    /**
     * 房费支付方式
     **/
    protected volatile int payType;

    //是否检查支付，代开房和军团房不需要
    protected volatile boolean checkPay = true;

    /**
     * 是否允许群组牌友加入的桌子
     **/
    protected int allowGroupMember = 0;
    /**
     * 群助手创房编号
     **/
    protected String assisCreateNo = "";
    /**
     * 群助手群编号
     **/
    protected String assisGroupNo = "";
    /**
     * 桌子是否能搓牌
     **/
    protected int isShuffling = 0;

    /*** 金币场模式id*/
    protected volatile String modeId = "0";//金币场

    private volatile boolean deleted = false;

    protected long lastCheckTime = 0L;//定时任务最后检查该房间的时间

    /**
     * 已结算的小局数
     **/
    protected volatile int playedBureau = 0;
    /**
     * 是否开启gps验证
     **/
    protected int isOpenGps = 0;
    /**
     * 比赛场ID
     **/
    protected long matchId = 0L;
    protected long matchRatio = 1L;  //比赛场倍率

    /**
     * 是否信用分房间
     */
    protected int creditMode = 0;
    /**
     * 进房最低信用分限制
     */
    protected int creditJoinLimit = 0;
    /**
     * 解散最低信用分限制
     */
    protected int creditDissLimit = 0;
    /**
     * 信用分低分:
     */
    protected int creditDifen = 0;
    /**
     * 佣金：送给群主的信用分
     */
    protected int creditCommission = 0;
    /**
     * 佣金模式：1：固定赠送，2：比例赠送: 百分制
     */
    protected int creditCommissionMode1 = 0;
    /**
     * 佣金模式：1：大赢家，2：全部赢家
     */
    protected int creditCommissionMode2 = 0;
    /**
     * 佣金模式:佣金限制分,当玩家赢得分大于或等于此分时,才计算佣金
     */
    protected int creditCommissionLimit = 0;
    /**
     * 保低增送分
     */
    protected int creditCommissionBaoDi = 0;
    /**
     * 是否产生了保低抽水
     */
    protected boolean isBaoDiCommission = false;

    /**
     * 是否开启托管
     */
    protected boolean autoPlay = false;

    /**
     * 上次autoPlay时间
     */
    protected long lastAutoPlayTime = 0;

    /**
     * 创房int参数列表
     */
    protected List<Integer> intParams;

    /**
     * 创房str参数列表
     */
    protected List<String> strParams;

    protected long tablePayStartTime;  //牌桌游戏开始时间

    /**
     * 包间名字
     */
    protected String roomName;

    private int chatConfig;  //牌桌内聊天配置

    public long getLastAutoPlayTime() {
        return lastAutoPlayTime;
    }

    public void setLastAutoPlayTime(long lastAutoPlayTime) {
        this.lastAutoPlayTime = lastAutoPlayTime;
    }

    /**引用计数器**/
    protected final AtomicInteger referenceCounter = new AtomicInteger(0);

    public AtomicInteger getReferenceCounter() {
        return referenceCounter;
    }

    public void setMatchId(Long matchId) {
        if (matchId != null) {
            if (this.matchId != matchId.longValue()) {
                this.matchId = matchId.longValue();
                changeExtend();
            }
        } else if (this.matchId != 0L) {
            this.matchId = 0L;
            changeExtend();
        }
    }

    public long getTablePayStartTime() {
        return tablePayStartTime;
    }

    public void setTablePayStartTime(long tablePayStartTime) {
        this.tablePayStartTime = tablePayStartTime;
    }

    public GroupTableConfig getGroupTableConfig() {
        return groupTableConfig;
    }

    public void setMatchRatio(long matchRatio) {
        this.matchRatio = matchRatio;
    }

    public int getIsOpenGps() {
        return isOpenGps;
    }

    public void setIsOpenGps(int isOpenGps) {
        this.isOpenGps = isOpenGps;
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
    }

    public int getSpecialDiss() {
        return specialDiss;
    }

    public void setSpecialDiss(int specialDiss) {
        this.specialDiss = specialDiss;
    }

    /**
     * 是否群主解散
     */
    public boolean isGroupMasterDiss() {
        return getSpecialDiss() == 1;
    }

    /**
     * 已结算的小局数
     **/
    public int getPlayedBureau() {
        return playedBureau;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public int getIsShuffling() {
        return isShuffling;
    }

    public void setIsShuffling(int isShuffling) {
        this.isShuffling = isShuffling;
    }

    public String getAssisCreateNo() {
        return assisCreateNo;
    }

    public void setAssisCreateNo(String assisCreateNo) {
        this.assisCreateNo = assisCreateNo;
        changeExtend();
    }

    public String getAssisGroupNo() {
        return assisGroupNo;
    }

    public void setAssisGroupNo(String assisGroupNo) {
        this.assisGroupNo = assisGroupNo;
        changeExtend();
    }

    public int getAllowGroupMember() {
        return allowGroupMember;
    }

    public void setAllowGroupMember(int allowGroupMember) {
        this.allowGroupMember = allowGroupMember;
    }

    public boolean isCheckPay() {
        return checkPay;
    }

    public void setCheckPay(boolean checkPay) {
        this.checkPay = checkPay;
        changeExtend();
    }

    /**
     * 房间模式0固定，1可观战，2可中途加入，3可下注
     */
    protected Map<String, String> roomModeMap = new ConcurrentHashMap<>();

    /**
     * 记录房间人员信息
     */
    protected Map<Long, Player> roomPlayerMap = new ConcurrentHashMap<>();

    /**
     * 是否是AA开房(所有)
     */
    protected boolean isAAConsume = Boolean.parseBoolean(ResourcesConfigsUtil.loadServerPropertyValue("table.isAAConsume", "false"));

    protected int serverId = GameServerConfig.SERVER_ID;

    protected volatile int serverType = 1;//游戏服类型0练习场1普通场

    protected volatile String serverKey = "";

    protected GroupTableConfig groupTableConfig = null;
    protected GroupTable groupTable = null;

    public GroupTable getGroupTable() {
        return groupTable;
    }

    public void setGroupTable(GroupTable groupTable) {
        this.groupTable = groupTable;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    /**
     * 记录房间人员信息
     */
    public Map<Long, Player> getRoomPlayerMap() {
        return roomPlayerMap;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public BaseTable() {
        lock = new ReentrantLock();
    }

    /**
     * 房间模式0固定，1可观战，2可中途加入，3可下注
     */
    public Map<String, String> getRoomModeMap() {
        return roomModeMap;
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

    public void loadFromDB(TableInf info) {
        if (info.getServerId() > 0) {
            serverId = info.getServerId();
        }
        this.tableInf = info;
        this.id = info.getTableId();
        this.playType = info.getPlayType();
        this.roomId = info.getRoomId();
        this.masterId = info.getMasterId();
        this.createTime = info.getCreateTime();
        this.totalBureau = info.getTotalBureau();
        this.playBureau = info.getPlayBureau();
        this.state = SharedConstants.getTableState(info.getState());
        this.lastActionTime = info.getLastActionTime();
        this.isCompetition = info.getIsCompetition();
        this.disCardRound = info.getDisCardRound();
        this.nowDisCardSeat = info.getNowDisCardSeat();
        this.disCardSeat = info.getDisCardSeat();
        this.gotyeRoomId = info.getGotyeRoomId();
        this.daikaiTableId = info.getDaikaiTableId();
        this.finishBureau=info.getFinishBureau();
        String answer = info.getAnswerDiss();
        if (!StringUtils.isBlank(answer)) {
            String[] answerArr = answer.split("_");
            answerDissMap = DataMapUtil.implode(StringUtil.getValue(answerArr, 0));
            sendDissTime = StringUtil.getLongValue(answerArr, 1);

        }

        if (!StringUtils.isBlank(info.getConfig())) {
            config = StringUtil.explodeToIntList(info.getConfig());
        }
        this.lastWinSeat = info.getLastWinSeat();
        if (!StringUtils.isBlank(info.getPlayLog())) {
            this.playLog = info.getPlayLog();
        } else {
            this.playLog = "";
        }
        initPlayers();
        if (!checkPlayer(null)) {
            diss();
            return;
        }
        initExtend(info.getExtend());
        if (!StringUtils.isBlank(info.getNowAction())) {
            initNowAction(info.getNowAction());
        }
        loadFromDB1(info);

    }

    public int initPlayers(Long mUserId, Player mPlayer) {
        String playerInfos = tableInf.getPlayers();
        if (!StringUtils.isBlank(playerInfos)) {
            int count = 0;
            Set<Integer> seatSet = new HashSet<>();
            if (StringUtils.isNotBlank(tableInf.getHandPai1()) || StringUtils.isNotBlank(tableInf.getOutPai1())) {
                seatSet.add(1);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai2()) || StringUtils.isNotBlank(tableInf.getOutPai2())) {
                seatSet.add(2);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai3()) || StringUtils.isNotBlank(tableInf.getOutPai3())) {
                seatSet.add(3);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai4()) || StringUtils.isNotBlank(tableInf.getOutPai4())) {
                seatSet.add(4);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai5()) || StringUtils.isNotBlank(tableInf.getOutPai5())) {
                seatSet.add(5);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai6()) || StringUtils.isNotBlank(tableInf.getOutPai6())) {
                seatSet.add(6);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai7()) || StringUtils.isNotBlank(tableInf.getOutPai7())) {
                seatSet.add(7);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai8()) || StringUtils.isNotBlank(tableInf.getOutPai8())) {
                seatSet.add(8);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai9()) || StringUtils.isNotBlank(tableInf.getOutPai9())) {
                seatSet.add(9);
            }
            if (StringUtils.isNotBlank(tableInf.getHandPai10()) || StringUtils.isNotBlank(tableInf.getOutPai10())) {
                seatSet.add(10);
            }

            List<Player> playerList0 = new ArrayList<>();
            List<Player> playerList1 = new ArrayList<>();

            String[] playerArr = playerInfos.split(";");
            for (String str : playerArr) {
                if (StringUtils.isBlank(str) || str.equals("null")) {
                    continue;
                }
                count++;

                String[] values = str.split(",");
                long userId = StringUtil.getLongValue(values, 0);

                if (mUserId != null && mUserId.longValue() != userId) {
                    continue;
                }

                Player player;
                if (mPlayer == null) {

                    if (userId < 0) {
                        player = PlayerManager.getInstance().getRobot(userId, playType);
                    } else {
                        player = PlayerManager.getInstance().getPlayer(userId);
                    }
                    if (player == null) {
                        player = PlayerManager.getInstance().loadPlayer(userId, playType);
                    }
                } else {
                    player = mPlayer;
                }

                if (player == null) {
                    continue;
                }

                if (!PlayerManager.getInstance().checkPlayer(playType, player)) {
                    player = PlayerManager.getInstance().changePlayer(player, getPlayerClass());
                }

                if (player.getPlayingTableId() == 0) {
                    player.setPlayingTableId(id);
                    player.saveBaseInfo();
                }
                if (player.getPlayingTableId() != id) {
                    continue;
                }
                player.initPlayInfo(str);
                player.setJoinTime(System.currentTimeMillis());
                int seat = player.getSeat();
                if (seat != 0) {
                    playerList0.add(player);
                    initPlayerCards(tableInf, seat, player);
                    getPlayerMap().put(player.getUserId(), player);
                    getSeatMap().put(seat, player);
                } else {
                    playerList1.add(player);
                    LogUtil.errorLog.warn("table user seat error0:userId={},tableId={},seat={}", player.getUserId(), id, seat);
                }
            }

            if ((mUserId == null && mPlayer == null) && seatSet.size() > 0 && playerList1.size() == 1 && (count == playerList1.size() + playerList0.size())) {
                int maxSeat = 0;
                for (Player player : playerList0) {
                    seatSet.remove(Integer.valueOf(player.getSeat()));

                    if (player.getSeat() > maxSeat) {
                        maxSeat = player.getSeat();
                    }
                }
                boolean recoverPlayer = false;
                int size = seatSet.size();
                if (size == 1) {
                    playerList1.get(0).setSeat(seatSet.iterator().next());
                    recoverPlayer = true;
                } else if (size == 0) {
                    List<Integer> seatList = new ArrayList<>(maxSeat > 0 ? maxSeat : 1);
                    for (int i = 1; i <= maxSeat; i++) {
                        seatList.add(i);
                    }
                    for (Player player : playerList0) {
                        seatList.remove(Integer.valueOf(player.getSeat()));
                    }
                    if (seatList.size() == 1) {
                        playerList1.get(0).setSeat(seatList.get(0));
                        recoverPlayer = true;
                    } else if (seatList.size() == 0) {
                        playerList1.get(0).setSeat(maxSeat + 1);
                        recoverPlayer = true;
                    }
                }
                if (recoverPlayer) {
                    Player player = playerList1.get(0);
                    int seat = player.getSeat();
                    initPlayerCards(tableInf, seat, player);
                    getPlayerMap().put(player.getUserId(), player);
                    getSeatMap().put(seat, player);

                    changePlayers();
                }
            }

            return playerList0.size();
        } else {
            return 0;
        }
    }

    private static void initPlayerCards(TableInf tableInf, int seat, Player player) {
        if (seat == 1) {
            player.initPais(tableInf.getHandPai1(), tableInf.getOutPai1());
        } else if (seat == 2) {
            player.initPais(tableInf.getHandPai2(), tableInf.getOutPai2());
        } else if (seat == 3) {
            player.initPais(tableInf.getHandPai3(), tableInf.getOutPai3());
        } else if (seat == 4) {
            player.initPais(tableInf.getHandPai4(), tableInf.getOutPai4());
        } else if (seat == 5) {
            player.initPais(tableInf.getHandPai5(), tableInf.getOutPai5());
        } else if (seat == 6) {
            player.initPais(tableInf.getHandPai6(), tableInf.getOutPai6());
        } else if (seat == 7) {
            player.initPais(tableInf.getHandPai7(), tableInf.getOutPai7());
        } else if (seat == 8) {
            player.initPais(tableInf.getHandPai8(), tableInf.getOutPai8());
        } else if (seat == 9) {
            player.initPais(tableInf.getHandPai9(), tableInf.getOutPai9());
        } else if (seat == 10) {
            player.initPais(tableInf.getHandPai10(), tableInf.getOutPai10());
        }
    }

    public int initPlayers() {
        return initPlayers(null, null);
    }

    public abstract void initExtend0(JsonWrapper extend);

    public final void initExtend(String extend) {
        JsonWrapper wrapper = new JsonWrapper(extend);

        playedBureau = wrapper.getInt("playedBureau", playBureau);

        serverType = wrapper.getInt("-1", 1);
        String tempServerKey = wrapper.getString("-2");
        if (StringUtils.isNotBlank(tempServerKey)) {
            serverKey = tempServerKey;
        }

        String str = wrapper.getString(0);
        if (StringUtils.isNotBlank(str)) {
            String[] temps = str.split("\\;");
            for (String temp : temps) {
                int idx = temp.indexOf(":");
                if (idx > 0) {
                    roomModeMap.put(temp.substring(0, idx), temp.substring(idx + 1));
                }
            }
        }

        str = wrapper.getString("-3");
        if (StringUtils.isNotBlank(str)) {
            String[] temps = str.split("\\;");
            try {
                for (String temp : temps) {
                    if (NumberUtils.isDigits(temp)) {
                        long userId = Long.parseLong(temp);
                        if (!getPlayerMap().containsKey(userId)) {
                            Player player = ObjectUtil.newInstance(getPlayerClass());

                            RegInfo user = UserDao.getInstance().selectUserByUserId(userId);
                            player.loadFromDB(user);
                            if (player.getState() == null) {
                                player.changeState(player_state.entry);
                            }
                            player.setIsOnline(0);

                            Player addPlayer = PlayerManager.getInstance().addPlayer(player);
                            if (addPlayer != player) {
                                addPlayer.loadFromDB(user);
                            }
                            roomPlayerMap.put(userId, addPlayer);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }

        payType = wrapper.getInt("payType", -1);

        creatorId = wrapper.getLong("creatorId", 0);
        allowGroupMember = wrapper.getInt("allowGroupMember", 0);
        if (SharedConstants.isAssisOpen() && StringUtils.isNotBlank(wrapper.getString("assisCreateNo"))) {
            assisCreateNo = wrapper.getString("assisCreateNo");
            assisGroupNo = wrapper.getString("assisGroupNo");
        }
        isShuffling = wrapper.getInt("isShuffling", 0);

        checkPay = wrapper.getInt("checkPay", 1) == 1;
        isOpenGps = wrapper.getInt("isOpenGps", 0);

        matchId = wrapper.getLong("matchId", 0);
        matchRatio = wrapper.getLong("matchRatio", 1);
        String modeId = wrapper.getString("modeId");
        if (StringUtils.isNotBlank(modeId)) {
            this.modeId = modeId;
        }

        //信用房信息
        creditMode = wrapper.getInt("creditMode", 0);
        creditJoinLimit = wrapper.getInt("creditJoinLimit", 0);
        creditDissLimit = wrapper.getInt("creditDissLimit", 0);
        creditDifen = wrapper.getInt("creditDifen", 0);
        creditCommission = wrapper.getInt("creditCommission", 0);
        creditCommissionMode1 = wrapper.getInt("creditCommissionMode1", 0);
        creditCommissionMode2 = wrapper.getInt("creditCommissionMode2", 0);
        creditCommissionLimit = wrapper.getInt("creditCommissionLimit", 0);
        creditCommissionBaoDi = wrapper.getInt("creditCommissionBaoDi", 0);

        autoPlay = wrapper.getInt("autoPlay", 0) == 1;

        String intParamsStr = wrapper.getString("intParams");
        if (!StringUtils.isBlank(intParamsStr)) {
            intParams = StringUtil.explodeToIntList(intParamsStr);
        }

        String strParamsStr = wrapper.getString("strParams");
        if (!StringUtils.isBlank(strParamsStr)) {
            strParams = StringUtil.explodeToStringList(strParamsStr,",");
        }

        roomName = wrapper.getString("roomName");
        chatConfig = wrapper.getInt("chatConfig",0);
        initExtend0(wrapper);
    }

    public abstract Map<String, Object> saveDB(boolean asyn);

    public abstract JsonWrapper buildExtend0(JsonWrapper extend);

    public final String buildExtend() {
        JsonWrapper wrapper = new JsonWrapper("");

        wrapper.putInt("playedBureau", playedBureau);

        if (roomModeMap != null && roomModeMap.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Map.Entry<String, String> kv : roomModeMap.entrySet()) {
                strBuilder.append(";").append(kv.getKey()).append(":").append(kv.getValue());
            }
            wrapper.putString(0, strBuilder.substring(1));
        }

        wrapper.putInt("-1", serverType);
        if (StringUtils.isNotBlank(serverKey)) {
            wrapper.putString("-2", serverKey);
        }

        if (roomPlayerMap != null && roomPlayerMap.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Map.Entry<Long, Player> kv : roomPlayerMap.entrySet()) {
                strBuilder.append(";").append(kv.getKey().toString());
            }
            wrapper.putString("-3", strBuilder.substring(1));
        }

        wrapper.putInt("payType", payType);

        wrapper.putLong("creatorId", creatorId);
        if (allowGroupMember != 0)
            wrapper.putInt("allowGroupMember", allowGroupMember);
        if (isKaiYiJu()) {
            if (StringUtils.isNotBlank(assisCreateNo))
                wrapper.putString("assisCreateNo", assisCreateNo);
            if (StringUtils.isNotBlank(assisGroupNo))
                wrapper.putString("assisGroupNo", assisGroupNo);
        }
        if (isShuffling != 0)
            wrapper.putInt("isShuffling", isShuffling);

        wrapper.putInt("checkPay", checkPay ? 1 : 0);
        if (isOpenGps != 0)
            wrapper.putInt("isOpenGps", isOpenGps);
        if (matchId != 0L)
            wrapper.putLong("matchId", matchId);
        if (matchRatio != 1L)
            wrapper.putLong("matchRatio", matchRatio);
        if (StringUtils.isNotBlank(modeId) && !"0".equals(modeId)) {
            wrapper.putString("modeId", modeId);
        }

        //信用房信息
        wrapper.putInt("creditMode", creditMode);
        wrapper.putInt("creditJoinLimit", creditJoinLimit);
        wrapper.putInt("creditDissLimit", creditDissLimit);
        wrapper.putInt("creditDifen", creditDifen);
        wrapper.putInt("creditCommission", creditCommission);
        wrapper.putInt("creditCommissionMode1", creditCommissionMode1);
        wrapper.putInt("creditCommissionMode2", creditCommissionMode2);
        wrapper.putInt("creditCommissionLimit", creditCommissionLimit);
        wrapper.putInt("creditCommissionBaoDi", creditCommissionBaoDi);

        wrapper.putInt("autoPlay", autoPlay ? 1 : 0);

        wrapper.putString("intParams", StringUtil.implode(intParams, ","));
        wrapper.putString("strParams", StringUtil.implode(strParams, ","));

        wrapper.putString("roomName",roomName);
        wrapper.putInt("chatConfig",chatConfig);

        return buildExtend0(wrapper).toString();
    }

    /**
     * 获取游戏code
     *
     * @return
     */
    public String loadGameCode() {
        return "gameCode" + playType;
    }

    /**
     * 是否比赛场
     *
     * @return
     */
    public boolean isMatchRoom() {
        return matchId > 0L;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public abstract <T> T getPlayer(long id, Class<T> cl);

    /**
     * 玩家退出
     *
     * @param player
     */
    public boolean quitPlayer(Player player) {
        synchronized (this) {
            if (!getPlayerMap().containsKey(player.getUserId())) {
                return false;
            }

            if (!canQuit(player)) {
                return false;
            }

            if (getMasterId() == player.getUserId()) {
                if (isGoldRoom() || isDaikaiTable() || StringUtils.isNotBlank(serverKey) || isGroupRoom()) {
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_11));
                    return false;
                }
            }

            quitPlayer1(player);
            getSeatMap().remove(player.getSeat());
            getPlayerMap().remove(player.getUserId());
            player.clearTableInfo();
            if (isKaiYiJu()) {
                String nStatus = getPlayerCount() == getMaxPlayerCount() ? "2" : "0";
                AssisServlet.sendRoomStatus(this, nStatus);
            }
            changePlayers();
            return true;
        }
    }

    public void onPlayerQuitSuccess(Player player) {
        onPlayerQuitSuccess(player, 0, true);
    }

    /**
     * @param reason 1主动退出 2被踢出
     * @param callme 是否给自己发退出消息
     */
    public void onPlayerQuitSuccess(Player player, int reason, boolean callme) {
        try {
            //是否发生房主变动
            boolean change = false;
            String tableKeyId = getServerKey();
            if (getMasterId() == player.getUserId()) {
                change = makeOverMasterId(player);
            }
            LogUtil.msgLog.info("quit table:tableId=" + getId() + ",master=" + getMasterId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",tableKeyId=" + tableKeyId);

            boolean checkDiss = true;
            if (org.apache.commons.lang3.math.NumberUtils.isDigits(tableKeyId)) {
                int ret = GroupDao.getInstance().deleteTableUser(tableKeyId, String.valueOf(player.getUserId()));
                if (ret > 0) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", tableKeyId);
                    map.put("count", -1);
                    if (groupTable != null) {
                        groupTable.changeCurrentCount(-1);
                    }
                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                }
            } else if (tableKeyId != null && tableKeyId.startsWith("group") && tableKeyId.contains("_")) {

                String tableKey = tableKeyId.split("_")[1];

                HashMap<String, Object> map = new HashMap<>();
                map.put("count", -1);
                map.put("keyId", tableKey);
                if (GroupDao.getInstance().updateGroupTableByKeyId(map) > 0) {
                    if (Redis.isConnected() && NumberUtils.isDigits(tableKey)) {
                        groupTable = GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
                        RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), GroupRoomUtil.loadWeight(groupTable.getCurrentState(), getPlayerCount(), groupTable.getCreatedTime()), tableKey);
                        groupTable.setCurrentCount(getPlayerCount());
                        RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), tableKey, JSON.toJSONString(groupTable));
                    }
                }

                GroupDao.getInstance().deleteTableUser(tableKey, String.valueOf(player.getUserId()));
                checkDiss = false;
            }

            int groupId = 0;
            if (isGroupRoom()) {
                groupId = Integer.parseInt(loadGroupId());
            }

            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, player.getUserId() + "", getPlayType(), reason, 0, groupId, groupId);

            GeneratedMessage msg = com.build();
            if (callme) {
                player.writeSocket(msg);
            }

            broadMsg(msg);
//            for (Player tableplayer : getSeatMap().values()) {
//                tableplayer.writeSocket(msg);
//            }
            if (change) {
                Player player1 = getPlayerMap().get(getMasterId());
                if (player1 != null) {
                    //代开房间新生成的房主id消息
                    ComRes.Builder com2 = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_daikaimasterid, "" + player1.getUserId());
                    //将新房主id消息发给房主一个人
//                    player1.writeSocket(com2.build());
                    GeneratedMessage msg2 = com2.build();

                    for (Player tableplayer : getSeatMap().values()) {
                        tableplayer.writeSocket(msg2);
                    }
                    for (Player tableplayer : getRoomPlayerMap().values()) {
                        tableplayer.writeSocket(msg2);
                    }
                }
            }

            if (checkDiss && getPlayerCount() <= 0 && StringUtils.isNotBlank(tableKeyId)) {

                // 解散前发送解散消息
                ComRes.Builder com1 = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, playType, groupId, playedBureau);

                GeneratedMessage msg1 = com1.build();
                for (Player player0 : getSeatMap().values()) {
                    if (player0.getUserId() != player.getUserId())
                        player0.writeSocket(msg1);
                }

                for (Player player0 : roomPlayerMap.values()) {
                    if (player0.getUserId() != player.getUserId())
                        player0.writeSocket(msg1);
                }

                diss();
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    public boolean ready(Player player) {
        player.changeState(player_state.ready);
        return true;
    }

    /**
     * 加入桌子
     *
     * @param player
     */
    public final boolean joinPlayer(Player player) {
        synchronized (this) {
            if (!isCanJoin(player, false)) {
                if (!getPlayerMap().containsKey(player.getUserId())) {
                    player.setSeat(0);
                    player.saveBaseInfo();
                }
                return false;
            }

            int seat = randomSeat();
            if (seat <= 0) {
                return false;
            } else {
                int seat0 = player.getSeat();
                if (seat0 > 0 && seat0 <= getMaxPlayerCount()) {
                    if (getSeatMap().containsKey(seat0)) {
                        player.setSeat(seat);
                    } else {
                        player.setSeat(seat0);
                        seat = seat0;
                    }
                } else {
                    player.setSeat(seat);
                }
            }

            player.setPlayingTableId(id);
            player.setTable(this);
            player.setLastActionBureau(getPlayBureau() <= 1 ? 0 : getPlayBureau());
            if (player_state.ready != player.getState()) {
                player.changeState(player_state.entry);
            }
            player.getMyExtend().setLatitudeLongitude("");
            player.changeIsLeave(0);
            player.setTotalPoint(0);
            player.setMaxPoint(0);
            player.setTotalBoom(0);
            player.setIsEntryTable(SharedConstants.table_online);

            player.saveBaseInfo();

            if (isDaikaiTable() && getPlayerCount() < 1) {
                setMasterId(player.getUserId());
            } else if (StringUtils.isNotBlank(serverKey) && (getPlayerCount() == 0 || getMasterId() <= 0)) {
                setMasterId(player.getUserId());
            }

            player.setSeat(seat);
            getPlayerMap().put(player.getUserId(), player);
            getSeatMap().put(seat, player);

            roomPlayerMap.remove(player.getUserId());
            player.getMyExtend().getPlayerStateMap().remove("1");
            player.getMyExtend().getPlayerStateMap().remove("seat");
            player.getMyExtend().getPlayerStateMap().put("0", "1");
            if (getPlayBureau() > 1 || (getPlayBureau() == 1 && state != table_state.ready && masterId != player.getUserId())) {
                player.getMyExtend().getPlayerStateMap().put("2", "1");
                player.getMyExtend().getPlayerStateMap().put("cur", getPlayBureau() + "_0");
            }
            LogUtil.msgLog.info("join table:tableId=" + getId() + ",master=" + getMasterId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat());

            player.newRecord();

            updateDaikaiTablePlayer();
            updateRoomPlayers();

            joinPlayer1(player);
            changePlayers();

            if (groupTable == null && NumberUtils.isDigits(serverKey)) {
                try {
                    groupTable = GroupDao.getInstance().loadGroupTableByKeyId(serverKey);
                } catch (Throwable t) {
                    LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                }
            }

            if (isGroupRoom()) {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    try {
                        GroupTable gt = GroupDao.getInstance().loadGroupTableByKeyId(msgs[1]);

                        if (gt != null) {
                            gt.setCurrentCount(getPlayerCount());
                            groupTable = gt;
                            if (Redis.isConnected()) {
                                RedisUtil.zadd(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(), gt.loadGroupRoom()), GroupRoomUtil.loadWeight(gt.getCurrentState(), gt.getCurrentCount(), gt.getCreatedTime()), gt.getKeyId().toString());
                                RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(), gt.loadGroupRoom()), msgs[1], JSON.toJSONString(gt));
                            }

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("count", 1);
                            map.put("keyId", gt.getKeyId().toString());
                            GroupDao.getInstance().updateGroupTableByKeyId(map);

                            TableUser tableUser = new TableUser();
                            tableUser.setCreatedTime(new Date());
                            tableUser.setGroupId(gt.getGroupId());
                            tableUser.setTableId(gt.getTableId());
                            tableUser.setPlayResult(0);
                            tableUser.setTableNo(gt.getKeyId());
                            tableUser.setUserId(player.getUserId());

                            GroupDao.getInstance().createTableUser(tableUser);
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }
                }
            } else if (groupTable != null) {
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", groupTable.getKeyId().toString());
                    groupTable.changeCurrentCount(1);
                    map.put("count", 1);

                    GroupDao.getInstance().updateGroupTableByKeyId(map);

                    TableUser tableUser = new TableUser();
                    tableUser.setCreatedTime(new Date());
                    tableUser.setGroupId(groupTable.getGroupId());
                    tableUser.setTableId(groupTable.getTableId());
                    tableUser.setPlayResult(0);
                    tableUser.setTableNo(groupTable.getKeyId());
                    tableUser.setUserId(player.getUserId());

                    GroupDao.getInstance().createTableUser(tableUser);
                } catch (Throwable t) {
                    LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                }
            }
            player.setJoinTime(System.currentTimeMillis());
            player.changeExtend();
            if (isKaiYiJu()) {
                String nStatus = getPlayerCount() == getMaxPlayerCount() ? "2" : "0";
                AssisServlet.sendRoomStatus(this, nStatus);
            }
            return true;
        }
    }

    public int updateRoomPlayers() {
        if (!isGoldRoom()) {
            HashMap<String, Object> paramMap = new HashMap<String, Object>(4);
            List<Long> idList = new ArrayList<>(getPlayerMap().keySet());
            paramMap.put("players", StringUtil.implode(idList));
            return TableDao.getInstance().updateRoom(id, paramMap);
        }
        return -1;
    }

    public Map<String, Object> loadCurrentDbMap() {
        // copy 一份map
        Map<String, Object> tempMap = new HashMap<String, Object>();
        if (deleted) {
            return tempMap;
        }
        synchronized (this) {
            if (deleted) {
                return tempMap;
            }
            Iterator<Entry<String, Object>> it = dbParamMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> kv = it.next();
                tempMap.put(kv.getKey(), kv.getValue());
                it.remove();
            }
        }
        return tempMap;
    }

    protected abstract void initNowAction(String nowAction);

    protected abstract String buildNowAction();

    protected abstract boolean quitPlayer1(Player player);

    public int randomSeat() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            list.add(i);
        }

        List<Integer> seatlist = new ArrayList<>(getSeatMap().keySet());
        list.removeAll(seatlist);
        if (list.isEmpty()) {
            return 0;
        }

        int index = MathUtil.mt_rand(0, list.size() - 1);
        return list.get(index);
    }

    protected abstract boolean joinPlayer1(Player player);

    /**
     * 解散
     */
    public int diss() {

        synchronized (this) {

            if (deleted) {
                return 0;
            }

            final Collection<Player> players = new ArrayList<>(getPlayerMap().values());

            try {
                if (playedBureau > 0) {
                    if (GameServerConfig.checkDataStatistics()) {
                        try {
                            calcDataStatistics2();
                            calcDataStatisticsBjd();
                        } catch (Exception e) {
                            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                        }
                    }
                }

                if (isKaiYiJu()) {
                    if (playedBureau > 0) {
                        AssisServlet.sendRoomResult(this);
                    }
                    AssisServlet.sendRoomStatus(this, "1");
                }
                try {
                    GroupTable groupTable = null;
                    if (NumberUtils.isDigits(serverKey)) {
                        groupTable = GroupDao.getInstance().loadGroupTableByKeyId(getServerKey());

                        if (groupTable != null && !groupTable.isOver()) {
                            HashMap<String, Object> tableMap = new HashMap<>();
                            tableMap.put("keyId", getServerKey());
                            tableMap.put("currentState", getDissCurrentState());
                            tableMap.put("playedBureau", getPlayedBureau());
                            tableMap.put("players", getPlayerNameString());
                            GroupDao.getInstance().updateGroupTableByKeyId(tableMap);

                            if (playBureau > 1) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("keyId", groupTable.getConfigId().toString());
                                map.put("count", 1);
                                GroupDao.getInstance().updateGroupTableConfigByKeyId(map);
                            } else {
                                int ret1 = GroupDao.getInstance().deleteTableUser(getServerKey());
                                int ret2 = GroupDao.getInstance().deleteGroupTableByKeyId(groupTable.getKeyId());

                                LogUtil.msgLog.info("group table diss:deleteTableUser=" + ret1 + ",deleteGroupTable=" + ret2 + ",groupTable=" + JacksonUtil.writeValueAsString(groupTable));

                                //退还钻石/房卡
                                GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
                                if (groupTableConfig != null && groupTableConfig.getPayType().intValue() != 1) {
                                    int payValue = PayConfigUtil.get(groupTableConfig.getGameType(), groupTableConfig.getGameCount(), groupTableConfig.getPlayerCount(), 1);
                                    GroupUser groupUser = GroupDao.getInstance().loadGroupMaster(groupTableConfig.getParentGroup().longValue() == 0 ? groupTableConfig.getGroupId().toString() : groupTableConfig.getParentGroup().toString());
                                    if (groupUser != null && payValue > 0) {
                                        Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
                                        if (player1 != null) {
                                            player1.changeCards(payValue, 0, true, true, CardSourceType.groupTable_diss_FZ);
                                        } else {
                                            RegInfo user = UserDao.getInstance().selectUserByUserId(groupUser.getUserId());
                                            if (user != null) {
                                                UserDao.getInstance().updateUserCards(user.getUserId(), user.getFlatId(), user.getPf(), 0, payValue);

                                                if (user.getIsOnLine() == 1 && user.getEnterServer() > 0) {
                                                    Server server1 = ServerManager.loadServer(user.getEnterServer());
                                                    if (server1 != null) {
                                                        String url = server1.getIntranet();
                                                        if (StringUtils.isBlank(url)) {
                                                            url = server1.getHost();
                                                        }

                                                        if (StringUtils.isNotBlank(url)) {
                                                            int idx = url.indexOf(".");
                                                            if (idx > 0) {
                                                                idx = url.indexOf("/", idx);
                                                                if (idx > 0) {
                                                                    url = url.substring(0, idx);
                                                                }
                                                                url += "/online/notice.do?type=playerCards&userId=" + user.getUserId();
                                                                String noticeRet = HttpUtil.getUrlReturnValue(url + "&free=1&message=" + payValue);
                                                                LogUtil.msgLog.info("notice result:url=" + url + ",ret=" + noticeRet);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        MessageUtil.sendMessage(true, true, UserMessageEnum.TYPE0, player1 != null ? player1 : groupUser.getUserId(), "军团房间【" + groupTable.getTableId() + "】未开局被解散，获得钻石x" + payValue, null);
                                    }
                                }

                                groupTable = null;
                            }
                        } else {
                            deleted = true;
                        }
                    } else if (isGroupRoom()) {
                        String[] msgs = serverKey.split("_");
                        if (msgs.length >= 2) {
                            if (Redis.isConnected()) {
                                RedisUtil.zrem(GroupRoomUtil.loadGroupKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                                RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                            }
                            groupTable = GroupDao.getInstance().loadGroupTableByKeyId(msgs[1]);
                            if (groupTable != null && !groupTable.isOver()) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("currentState", getDissCurrentState());
                                map.put("keyId", msgs[1]);
                                map.put("playedBureau", getPlayedBureau());
                                map.put("players", getPlayerNameString());
                                GroupDao.getInstance().updateGroupTableByKeyId(map);
                                LogUtil.msgLog.info("diss group table success:msg=" + JacksonUtil.writeValueAsString(groupTable));

                                if (groupTable.isNotStart() || isGroupRoomReturnConsume()) {
                                    //根据payType返回钻石currentState
                                    String[] tempMsgs = new JsonWrapper(groupTable.getTableMsg()).getString("strs").split(";")[0].split("_");
                                    String payType = tempMsgs[0];

                                    if (tempMsgs.length >= 4) {
                                        int tempPay = Integer.parseInt(tempMsgs[3]);
                                        if (("2".equals(payType) || "3".equals(payType)) && tempPay > 0) {
                                            CardSourceType sourceType = getCardSourceType(Integer.parseInt(payType));
                                            Player payPlayer = PlayerManager.getInstance().getPlayer(Long.valueOf(tempMsgs[2]));
                                            if (payPlayer != null) {
                                                payPlayer.changeCards(tempPay, 0, true, sourceType);
                                            } else {
                                                RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(tempMsgs[2]));
                                                payPlayer = ObjectUtil.newInstance(getPlayerClass());
                                                payPlayer.loadFromDB(user);
                                                payPlayer.changeCards(tempPay, 0, true, sourceType);

                                                if (payPlayer.getEnterServer() > 0 && user.getIsOnLine() == 1) {
                                                    Server server1 = ServerManager.loadServer(payPlayer.getEnterServer());
                                                    if (server1 != null) {
                                                        String url = server1.getIntranet();
                                                        if (StringUtils.isBlank(url)) {
                                                            url = server1.getHost();
                                                        }

                                                        if (StringUtils.isNotBlank(url)) {
                                                            int idx = url.indexOf(".");
                                                            if (idx > 0) {
                                                                idx = url.indexOf("/", idx);
                                                                if (idx > 0) {
                                                                    url = url.substring(0, idx);
                                                                }
                                                                url += "/online/notice.do?type=playerCards&userId=" + payPlayer.getUserId();

                                                                String noticeRet = HttpUtil.getUrlReturnValue(url + "&free=1&message=" + tempMsgs[3], 2);
                                                                LogUtil.msgLog.info("notice result:url=" + url + ",ret=" + noticeRet);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                GameUtil.autoCreateGroupTable(groupTable.getGroupId().toString(), getPlayerClass(),groupTable.getConfigId());
                            } else {
                                deleted = true;
                            }
                        }
                    }

                    int maxPoint = 0;
                    for (Player player : players) {
                        if (player.loadScore() > maxPoint) {
                            maxPoint = player.loadScore();
                        }
                    }

                    //统计系统打牌数据
                    if (playedBureau > 0) {
                        UserStatistics userStatistics0 = new UserStatistics("system", 0, isMatchRoom() ? "match" : (isGoldRoom() ? "gold" : (isGroupRoom() ? "group" : "common")), playType, playedBureau);
                        UserDao.getInstance().saveUserStatistics(userStatistics0);
                    }

                    List<Player> bigWin = new ArrayList<>();
                    int bigWinPoint = 0;
                    for (Player player : players) {
                        if (groupTable != null && getPlayedBureau() > 0) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("groupId", groupTable.getGroupId().toString());
                            map.put("userId", String.valueOf(player.getUserId()));
//					map.put("count1", 1);
                            map.put("count2", 1);
                            GroupDao.getInstance().updateGroupUser(map);

                            updatePlayerScore(player, player.loadScore() == maxPoint ? 1 : -1);
                        }

                        String timeRemoveBindStr = ResourcesConfigsUtil.loadServerPropertyValue("periodRemoveBind");
                        if (this.playBureau > 1 && !StringUtil.isBlank(timeRemoveBindStr) && !"0".equals(timeRemoveBindStr)) {
                            // 刷新最后游戏时间
                            player.setLastPlayTime(TimeUtil.now());
//                    player.getMyExtend().setLastPlayTime(TimeUtil.formatTime(TimeUtil.now()));
                        }

                        //统计用户输赢数据
                        UserStatistics userStatistics = player.isRobot() ? loadRobotUserStatistics(player) : loadPlayerUserStatistics(player);

                        if (userStatistics != null) {
                            UserDao.getInstance().saveUserStatistics(userStatistics);
                        }
                        if (player.loadScore() > bigWinPoint) {
                            bigWin.clear();
                            bigWin.add(player);
                            bigWinPoint = player.loadScore();
                        } else if (player.loadScore() > 0 && player.loadScore() == bigWinPoint) {
                            bigWin.add(player);
                        }
                    }
                    if (playedBureau == totalBureau) {
                        if (!bigWin.isEmpty()) {// 统计玩家大赢家次数
                            for (Player player : bigWin) {
                                NewPlayerGiftActivityCmd.updateBigWinNum(player);  //大赢家次数更新
                            }
                        }
                    } else {
                        //打筒子
                        if (playedBureau > 0 && !bigWin.isEmpty() && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("isDtzApp"))) {
                            for (Player player : bigWin) {
                                NewPlayerGiftActivityCmd.updateBigWinNum(player);  //大赢家次数更新
                            }
                        }
                    }
                    for (Player player : players) {
                        if (player.isRobot()) {
                            PlayerManager.getInstance().removePlayer(player);
                        }

                        UserFirstmyth bean = player.getMyExtend().buildFrstmyth(getWanFa(), isGroupRoom() ? Integer.parseInt(loadGroupId()) : 0);
                        if (bean != null) {
                            UserFirstmythDao.getInstance().saveUserFirstmyth(bean);
                        }

                        // 牌局统计活动
                        if (isNormalOver() && !isGoldRoom() && !isMatchRoom() && ActivityConfig.isActivityActive(ActivityConfig.activity_game_bureau)) {
                            UserGameBureauDao.getInstance().saveUserGameBureau(new UserGameBureau(player.getUserId(), player.getName(), 0, "", 0));
                        }

                        OldBackGiftActivityCmd.setPlayOne(player);

                        if (playedBureau > 0) {
                            UserDao.getInstance().changePlayCount1(player.getUserId(), 1);
                        }

                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                }

                if (this.playBureau > 1) {
                    // 新年红包
                    calcActivity(ActivityConstant.activity_xn_hb, isTiqianDiss() ? 1 : 0);
                    recordUserGameRebate();
                }

                if (isGoldRoom()) {
                    try {
                        GoldRoomDao.getInstance().updateGoldRoom(id > 0 ? id : daikaiTableId, 0, "2");
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            } finally {
                Collection<Player> players1 = new ArrayList<>(roomPlayerMap.values());
                getPlayerMap().clear();
                getSeatMap().clear();
                roomPlayerMap.clear();
                roomModeMap.clear();

                for (Player player : players) {
                    player.setAutoPlayCheckedTime(0);
                    player.setAutoPlayChecked(false);
                    player.setAutoPlayCheckedTimeAdded(false);
                    player.clearTableInfo(this, false);
                    player.getMyExtend().getPlayerStateMap().clear();
                    player.changeExtend();
                    player.setDissCount(0);
                    player.saveBaseInfo(false);
                }
                for (Player player : players1) {
                    player.clearTableInfo(this, false);
                    player.getMyExtend().getPlayerStateMap().clear();
                    player.changeExtend();
                    player.saveBaseInfo(false);
                }

                int ret = TableManager.getInstance().delTable(this, deleted);

                deleted = true;

                if (isMatchRoom()) {
                    final MatchBean matchBean = JjsUtil.loadMatch(matchId);
                    if (matchBean != null) {
                        final long matchRatio = getMatchRatio();
                        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
                            @Override
                            public void run() {
                                JjsUtil.doMatch(players, matchRatio, matchBean);
                            }
                        });
                    } else {
                        LogUtil.msgLog.info("current match is null: matchId={}", matchId);
                    }
                }

                return ret;
            }
        }
    }

    public long getMatchId() {
        return matchId;
    }

    public long getMatchRatio() {
        return matchRatio;
    }

    /**
     * 更新比赛场中玩家的分数信息
     *
     * @param userScores
     */
    public void changeMatchData(Map<Long, Integer> userScores) {
        if (isMatchRoom()) {
            MatchBean matchBean = JjsUtil.loadMatch(matchId);
            if (matchBean != null) {
                synchronized (matchBean) {
                    int currentNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
                    for (Map.Entry<Long, Integer> kv : userScores.entrySet()) {
                        int temp = (int) (kv.getValue().intValue() * matchRatio);
                        int score = matchBean.addUserMsg(kv.getKey().longValue(), currentNo, temp, -2, true);

                        HashMap<String, Object> map0 = new HashMap<>();
                        map0.put("currentScore", score);
                        MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), kv.getKey().longValue(), map0);
                    }
                    matchBean.sort();

                    //推送排名
                    JjsUtil.sendRank(matchBean);
                }

                for (Map.Entry<Long, Player> kv : getPlayerMap().entrySet()) {
                    kv.getValue().getMyExtend().getUserTaskInfo().alterDailyMatchGameNum();
                }
            }
        }
    }

    public boolean isGroupRoomReturnConsume() {
        return playBureau <= 1 && playedBureau == 0;
    }

    public boolean isAAConsume0() {
        return payType == 1;
    }

    /**
     * 一局结束
     */
    public abstract void calcOver();

    public void calcOver0() {
        synchronized (this) {
            try {
                for (Player player : getPlayerMap().values()) {
                    String cur = player.getMyExtend().getPlayerStateMap().get("cur");
                    if (cur != null && cur.endsWith("_0")) {
                        String str = cur.substring(0, cur.length() - 2);
                        int temp = Integer.parseInt(str);
                        cur = temp + "_1";
                        player.getMyExtend().getPlayerStateMap().put("cur", cur);
                        if (consumeCards() && isAAConsume0()) {
                            int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 0);
                            if (needCards <= 0) {
                                continue;
                            }
                            if (temp > (getTotalBureau() / 2)) {
                                needCards = needCards / 2;
                            }
                            if (!isGoldRoom()) {
                                player.changeCards(0, -needCards, true, playType, getCardSourceType(payType));
                                player.saveBaseInfo();
                            }
                        }
                        // 非金币场房间局数统计
                        player.changeTotalBureau();
                    }
                    RedBagManager.getInstance().updateTodayRedBagGameNum(player.getUserId());
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }

        }
    }

    /**
     * 小局数据统计
     */
    public void calcDataStatistics1() {

    }

    /**
     * 大局数据统计
     */
    public void calcDataStatistics2() {
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (Player player : getPlayerMap().values()) {
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

            for (Player player : getPlayerMap().values()) {
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

            calcDataStatistics3(groupId);
        }
    }

    /**
     * 自定义的数据统计
     *
     * @see #calcDataStatistics2()
     */
    public void calcDataStatistics3(String groupId) {
        //俱乐部活动总大局数
        if (ActivityConfig.isActivityOpen(ActivityConfig.activity_group_conquest) && loadGroupRoomPay() > 0 && isCommonOver()) {
            try {
                DataStatistics dataStatistics7 = new DataStatistics(Long.valueOf(ActivityConfig.activity_group_conquest), "group" + groupId, groupId, String.valueOf(playType), "jlbCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics7, 3);
            } catch (Exception e) {
                LogUtil.e("calcDataStatistics7 err-->groupId:" + groupId + ",tableId:" + getId(), e);
            }
        }

        if (ActivityConfig.isActivityOpen(ActivityConfig.activity_group_megabucks) && loadGroupRoomPay() > 0) {
            // 百万大奖活动
            try {
                DataStatistics dataStatistics8 = new DataStatistics(Long.valueOf(ActivityConfig.activity_group_megabucks), "group" + groupId, groupId, String.valueOf(playType), "jlbCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics8, 3);
            } catch (Exception e) {
                LogUtil.e("calcDataStatistics8 err-->groupId:" + groupId + ",tableId:" + getId(), e);
            }
        }
        //俱乐部总大局数统计
        try {
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            DataStatistics jlbDjs = new DataStatistics(dataDate, "group" + groupId, groupId, "1", "jlbDjs", 1);
            DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(jlbDjs, 3);
        } catch (Exception e) {
            LogUtil.e("calcDataStatistics7 err-->groupId:" + groupId + ",tableId:" + getId(), e);
        }
    }

    /**
     * 是否开一局桌子
     */
    public boolean isKaiYiJu() {
        return SharedConstants.isAssisOpen() && !StringUtil.isBlank(getAssisCreateNo());
    }

    public void calcOver1() {
        if (isFirstBureauOverConsume() && totalBureau == 1) {
            consume();
        }

        calcOver0();
        if (GameServerConfig.checkDataStatistics()) {
            try {
                calcDataStatistics1();
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
        try {
            GroupTable groupTable = null;
            if (NumberUtils.isDigits(getServerKey())) {
                groupTable = GroupDao.getInstance().loadGroupTableByKeyId(getServerKey());
            }

            for (Player player : getPlayerMap().values()) {
                if (groupTable != null) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("groupId", groupTable.getGroupId().toString());
                    map.put("userId", String.valueOf(player.getUserId()));
                    map.put("count1", 1);
                    GroupDao.getInstance().updateGroupUser(map);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }

        // 幸运红包
        calcActivity(ActivityConstant.activity_luck_hb, isTiqianDiss() ? 1 : 0);
        // calcActivity(ActivityConstant.activity_xn_hb);
    }

    public UserStatistics loadRobotUserStatistics(Player player) {
        return null;
    }

    public UserStatistics loadPlayerUserStatistics(Player player) {
        if (playedBureau > 0) {
            return new UserStatistics(String.valueOf(player.getUserId()), player.loadScore(), isMatchRoom() ? "match" : (isGoldRoom() ? "gold" : (isGroupRoom() ? "group" : "common")), playType, playedBureau);
        } else {
            return null;
        }
    }

    /**
     * 是否允许机器人加入
     *
     * @return
     */
    public boolean allowRobotJoin() {
        return false;
    }

    /**
     * 大结算
     */
    public void calcOver2() {
        for (Player player : getPlayerMap().values()) {
            if (masterId == player.getUserId() && playBureau == totalBureau) {
                player.endCreateBigResultBureau(playBureau);
            }
            LuckyRedbagCommand.setInnings(player);
            NewPlayerGiftActivityCmd.updateMatchNum(player);
        }

        // 新年红包
        // calcActivity(ActivityConstant.activity_xn_hb);
    }

    // 随机红包
    public static List<Double> getHongBaoList(double total, int count) {
        double sheng = total;
        int cnt = count - 1;

        double min = 0.1;
        double max = sheng - min * cnt;
        List<Double> list = new ArrayList<>();
        double money = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        String moneyStr = "";
        for (int i = 0; i < count; i++) {
            if (0 == cnt) {
                money = sheng;
            } else {
                money = MathUtil.random(min, max);
                cnt--;
            }
            moneyStr = df.format(money);
            money = Double.parseDouble(moneyStr);
            sheng = sheng - money;
            max = sheng - min * cnt;
            list.add(money);
        }

        return list;
    }

    /**
     * 新年活动
     */
    private void calcActivity(int activityType, Object... para) {
        if (ActivityConstant.activity_xn_hb == activityType) {// 新年红包
            ActivityBean xnAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_xn_hb);
            if (xnAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            float xnHbMoney = xnAcitvity.shakeXnHbMoney();
            if (xnHbMoney <= 0) {
                return;
            }

            // double xnHbMoney = 0.5;
            // 总红包金额=平均金额*牌桌人数*（0.5+本局消耗房卡数量*0.5）
            // 红包金额最小值0.1 最大值为总红包金额-0.1*（总人数-1）

            int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 0);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", id);
            paramMap.put("hbType", 1);
            paramMap.put("createTime", new Date());

            int playerCount = getPlayerCount();
            double totalMoney = xnHbMoney * playerCount * (0.5f + needCards * 0.5f);
            List<Double> hbList = getHongBaoList(totalMoney, playerCount);
            double money = 0;
            List<Map<String, Object>> paramMaps = new ArrayList<>();

            int index = 0;
            for (Player player : getPlayerMap().values()) {
                money = hbList.get(index++);
                paramMap.put("userId", player.getUserId());
                paramMap.put("userName", player.getName());
                paramMap.put("money", money);
                ActivityDao.getInstance().addHbFafangRecord(paramMap);
                ActivityDao.getInstance().insertUserTotalMoney(player, money);
                paramMaps.add(new HashMap<>(paramMap));
            }

            for (Player player : getPlayerMap().values()) {
                player.writeComMessage(WebSocketMsgType.res_com_code_hb, activityType, (int) para[0], JacksonUtil.writeValueAsString(paramMaps));
            }

        } else if (ActivityConstant.activity_luck_hb == activityType) {// 幸运红包
            ActivityBean luckAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_luck_hb);
            if (luckAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            int money = 0;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", 0);
            paramMap.put("hbType", 2);
            paramMap.put("createTime", new Date());
            List<Map<String, Object>> paramMaps = null;

            for (Player player : getPlayerMap().values()) {
                // 统计小局数
                player.getMyActivity().changeTimeFlag(activityType, luckAcitvity.getStartTime(), 1);
                int curPlayCount = player.getMyActivity().getTimeFlag(activityType, luckAcitvity.getStartTime());
                money = luckAcitvity.drawLuckMoney(curPlayCount);
                if (money <= 0) {
                    continue;
                }
                // 触发后，当前局数清为0
                player.getMyActivity().clearTimeFlag(activityType, luckAcitvity.getStartTime());

                paramMap.put("userId", player.getUserId());
                paramMap.put("userName", player.getName());
                paramMap.put("money", money);
                paramMaps = new ArrayList<>();
                paramMaps.add(paramMap);
                player.writeComMessage(WebSocketMsgType.res_com_code_hb, activityType, isTiqianDiss() ? 1 : 0, JacksonUtil.writeValueAsString(paramMaps));
                ActivityDao.getInstance().addHbFafangRecord(paramMap);
                ActivityDao.getInstance().insertUserTotalMoney(player, money);
                String content = "恭喜" + player.getName() + "获得" + money + "元幸运红包，在1月27日至2月2日19：00~23:00，参与牌局，都有概率获得幸运红包哦！";
                int round = 1;
                MarqueeManager.getInstance().sendMarquee(content, round);
                // writeMessage("发送:" + content + " 轮数:" + round);
                LogUtil.monitor_i("幸运红包:userId" + player.getUserId() + ",获得红包:" + money + ", 牌桌号:" + this.id);
            }

        } else if (ActivityConstant.activity_fudai == activityType) {// 福袋
            ActivityBean fudaiAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
            if (fudaiAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            ActivityDao.getInstance().updateFudai((Player) para[0], (int) para[1]);
        }

    }

    /**
     * 牌局返利活动统计
     */
    private void recordUserGameRebate() {
        GameReBate gameRebateBean = StaticDataManager.getGameRebate(playType);
        if (gameRebateBean != null) {// 暂时没做配置表
            int baseBureau = gameRebateBean.getBaseBureau();// 牌局返利基础奖励局数
            Date openServerDate = new Date(TimeUtil.parseTimeInMillis(gameRebateBean.getOpenServerDate()));
            Date endDate = DateUtils.addDays(openServerDate, gameRebateBean.getRebateRangeTime());
            Date curDate = new Date();
            if (playBureau >= baseBureau && curDate.before(endDate)) {
                int rewardBureau = (int) (playBureau / baseBureau);// 获得的基础点数
                for (Player player : getPlayerMap().values()) {
                    UserGameRebateDao.getInstance().saveUserGameRebate(new UserGameRebate(player.getUserId(), player.getName(), this.playType, rewardBureau, new Date(), (int) player.getPayBindId()));
                }
            }
        }
        if (ActivityConfig.isActivityOpen(ActivityConfig.activity_new_payBind_bureau_static, playType)) {
            int baseBureau = 4;
            if (playBureau >= baseBureau) {
                int rewardBureau = (int) (playBureau / baseBureau);// 获得的基础局数
                String gameTime = TimeUtil.formatDayTime2(TimeUtil.now());
                ActivityConfigInfo config = ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_new_payBind_bureau_static);
                for (Player player : getPlayerMap().values()) {
                    if (player.getPayBindId() > 0 && player.getPayBindTime().after(config.getStartTime()) && player.getPayBindTime().before(config.getEndTime())) {// 玩家已绑码 并且是在活动期间绑码
                        UserBindGameBureau record = new UserBindGameBureau(player.getUserId(), player.getName(), this.playType, gameTime, rewardBureau, (int) player.getPayBindId());
                        UserBindGameBureauDao.getInstance().saveUserBindGameBureau(record);
                    }
                }
            }
        }
    }

    // 是否AA制消耗
    public boolean isAAConsume() {
        return isAAConsume;
    }

    public boolean setAAConsume(boolean isAA) {
        return isAAConsume = isAA;
    }

    /**
     * 获取支付配置
     *
     * @return
     */
    public int loadPayConfig(int payType) {
        int ret;
        switch (payType) {
            case 1:
                ret = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 0);
                break;
            case 2:
                ret = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 1);
                break;
            case 3:
                ret = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 3);
                break;
            default:
                LogUtil.errorLog.error("loadPayConfig not found:tableId=" + id + ",playType=" + playType + ",payType=" + payType);
                ret = -1;
        }
        return ret;
    }

    /**
     * 获取所需要的钻石
     *
     * @param params
     * @return
     */
    public int getNeedCard(List<Integer> params) {
        return Integer.MAX_VALUE;
    }

    /**
     * 获取支付配置
     *
     * @return
     */
    public int loadPayConfig() {
        return loadPayConfig(payType);
    }

    public void initNext() {
        initNext(playBureau >= totalBureau);
    }

    public void initNext(boolean over) {
        boolean requriedCard = false;
        if (isFirstBureauOverConsume()) {
            requriedCard = consume();
        }

        // 如果为第一局，则添加table_playlog

        if (over) {
            return;
        }
        for (Player player : getSeatMap().values()) {
            player.initNext();
        }

//        setCreateTime(TimeUtil.now());
        clearPlayLog();
        changePlayBureau(1);
        setDisCardRound(0);
        setDisCardSeat(0);
        setNowDisCardSeat(0);

        changeTableState(table_state.ready);

        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable != null) {
                        groupTable.setPlayedBureau(getPlayedBureau());
                    }

                    if (Redis.isConnected()) {
                        String str = RedisUtil.hget(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                        if (StringUtils.isNotBlank(str)) {
                            JSONObject jsonObject = JSONObject.parseObject(str);
                            jsonObject.put("playedBureau", getPlayedBureau());
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1], jsonObject.toString());
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", msgs[1]);
                    map.put("playedBureau", getPlayedBureau());
                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }

        initNext1();
        initNext0(requriedCard);
        changePlayers();
    }

    /**
     * 俱乐部已打完局数入库（小甘）
     */
    public void grouplogBureauStorage() {
        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable != null) {
                        groupTable.setPlayedBureau(getPlayedBureau());
                    }

                    if (Redis.isConnected()) {
                        String str = RedisUtil.hget(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                        if (StringUtils.isNotBlank(str)) {
                            JSONObject jsonObject = JSONObject.parseObject(str);
                            jsonObject.put("playedBureau", getPlayedBureau());
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1], jsonObject.toString());
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", msgs[1]);
                    map.put("playedBureau", getPlayedBureau());
                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    public boolean isFirstBureauOverConsume() {
        return true;
    }


    public final int loadGroupRoomPay() {
        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable == null || groupTable.getKeyId() == null || !groupTable.getKeyId().toString().equals(msgs[1])) {
                        groupTable = GroupDao.getInstance().loadGroupTableByKeyId(msgs[1]);
                    }

                    String[] tempMsgs = new JsonWrapper(groupTable.getTableMsg()).getString("strs").split(";")[0].split("_");
//                    String payType = tempMsgs[0];

                    if (tempMsgs.length >= 4) {
//                        if (!"1".equals(payType)){
                        return Integer.parseInt(tempMsgs[3]);
//                        }else{
//                            //老数据AA扣钻数据错误,兼容处理
//                            String date = ResourcesConfigsUtil.loadServerPropertyValue("group_room_aa_date");
//                            if (StringUtils.isNotBlank(date)&&groupTable.getCreatedTime()!=null&&groupTable.getCreatedTime().after(com.sy.general.GeneralHelper.str2Date(date,"yyyy-MM-dd HH:mm:ss"))){
//                                return Integer.parseInt(tempMsgs[3]);
//                            }
//                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
        return -1;
    }

    protected boolean consume() {
        boolean requriedCard = false;

        // 如果是第一句扣掉房卡
        if (playBureau == 1 && consumeCards() && !isGoldRoom()) {
            int needCards = loadGroupRoomPay();
            if (needCards == 0) {
            } else {
                CardSourceType sourceType = getCardSourceType(payType);
                if (isAAConsume() || isAAConsume0()) {
                    if (needCards < 0) {
                        needCards = loadPayConfig();
                    }
                    if (needCards <= 0) {
                        return requriedCard;
                    }

                    for (Player player : getPlayerMap().values()) {
                        if (!GameConfigUtil.freeGame(playType, player.getUserId())) {
                            // 如果是AA开房每人扣一张
                            if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                player.changeTili(-needCards, true);
                            } else {
                                player.changeCards(0, -needCards, true, playType, sourceType);
                                player.saveBaseInfo();
                                calcActivity(ActivityConstant.activity_fudai, player, needCards);
                            }

                            requriedCard = true;
                        }
                    }
                } else {
                    if (NumberUtils.isDigits(getServerKey()) || isGroupRoom()) {
                        LogUtil.msgLog.info("group master pay:group table keyId=" + serverKey + ",tableId=" + getId());
                        changeConsume(needCards);
                    } else if (!isDaikaiTable()) {
                        if (needCards < 0) {
                            needCards = loadPayConfig();
                        }
                        if (needCards <= 0) {
                            return requriedCard;
                        }
                        Player player = getPlayerMap().get(masterId);
                        if (player != null && !GameConfigUtil.freeGame(playType, player.getUserId())) {
                            if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                player.changeTili(-needCards, true);
                            } else {
                                player.changeCards(0, -needCards, true, playType, sourceType);
                                player.saveBaseInfo();
                                calcActivity(ActivityConstant.activity_fudai, player, needCards);
                            }
                        } else {
                            if (player == null) {
                                RegInfo user = UserDao.getInstance().selectUserByUserId(masterId);
                                if (user != null && !GameConfigUtil.freeGame(playType, user.getUserId())) {
                                    try {
                                        player = ObjectUtil.newInstance(getPlayerClass());
                                        player.loadFromDB(user);
                                        if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                            player.changeTili(-needCards, true);
                                        } else {
                                            player.changeCards(0, -needCards, true, playType, sourceType);
                                            player.saveBaseInfo();
                                            calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                        }
                                    } catch (Exception e) {
                                        LogUtil.errorLog.error("consume err-->Exception:" + e.getMessage(), e);
                                    }
                                } else {
                                    LogUtil.e("consume err-->tableId:" + id + ",masterId:" + masterId);
                                }
                            }
                        }
                    } else if (isDaikaiTable()) {
                        changeConsume(needCards);
                    }
                }
            }
        } else if (playBureau == 1 && (NumberUtils.isDigits(getServerKey()) || isGroupRoom() || isDaikaiTable())) {
            int needCards = loadGroupRoomPay();
            if (needCards == 0) {
            } else {
                changeConsume(needCards);
            }
        }
        return requriedCard;
    }

    protected void changeConsume() {
        if (SharedConstants.consumecards && playBureau == 1) {
            changeConsume(loadPayConfig());
        }
    }

    public CardSourceType getCardSourceType(int payType) {
        if (isDaikaiTable()) {
            if (payType == 1) {
                return CardSourceType.daikaiTable_AA;
            } else if (payType == 2) {
                return CardSourceType.daikaiTable_FZ;
            }
        } else if (isGroupRoom()) {
            if (payType == 1) {
                return CardSourceType.groupTable_AA;
            } else if (payType == 2) {
                return CardSourceType.groupTable_FZ;
            } else if (payType == 3) {
                return CardSourceType.groupTable_QZ;
            }
        } else {
            if (payType == 1) {
                return CardSourceType.commonTable_AA;
            } else if (payType == 2) {
                return CardSourceType.commonTable_FZ;
            }
        }
        return CardSourceType.unknown;
    }

    /**
     * 军团长支付，房主代开需要第一局打完后统计消耗
     */
    protected void changeConsume(int needCards) {
        if (SharedConstants.consumecards && playBureau == 1) {
            if (needCards < 0) {
                needCards = loadPayConfig();
            }

            if (needCards <= 0) {
                return;
            }
            Player creator = PlayerManager.getInstance().getPlayer(creatorId);
            boolean count = false;
            if (creator == null) {
                RegInfo user = UserDao.getInstance().selectUserByUserId(creatorId);
                if (user != null) {
                    try {
                        creator = ObjectUtil.newInstance(getPlayerClass());
                        creator.loadFromDB(user);
                        count = true;
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                } else {
                    LogUtil.e("changeConsume err-->tableId:" + id + ",creatorId:" + creatorId);
                }
            } else {
                count = true;
            }
            if (count) {
                PlayerManager.getInstance().changeConsume(creator, -needCards, 0, playType);
                creator.changeUsedCards(-needCards);
            }
        }
    }

    /**
     * 是否是俱乐部房间
     *
     * @return
     */
    public final boolean isGroupRoom() {
        return serverKey != null && serverKey.startsWith("group");
    }

    public void initNext0(boolean requriedCard) {
    }

    public int calcPlayerCount(int playerCount) {
        return playerCount > 0 ? playerCount : 6;
    }

    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (Player player : getPlayerMap().values()) {
            sb.append(player.toInfoStr()).append(";");
        }
        // playerInfos = sb.toString();
        return sb.toString();
    }

    public void setLastActionTime(long lastActionTime) {
        this.lastActionTime = lastActionTime;
        dbParamMap.put("lastActionTime", lastActionTime);
    }

    public void changePlayers() {
        dbParamMap.put("players", JSON_TAG);
    }

    public void changeCards(int seat) {
        dbParamMap.put("outPai" + seat, JSON_TAG);
        dbParamMap.put("handPai" + seat, JSON_TAG);
    }

    public void changeExtend() {
        dbParamMap.put("extend", JSON_TAG);
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
        dbParamMap.put("createTime", createTime);
    }

    public int getTotalBureau() {
        return totalBureau;
    }

    public void setTotalBureau(int totalBureau) {
        this.totalBureau = totalBureau;
        dbParamMap.put("totalBureau", totalBureau);
    }

    public int getPlayBureau() {
        return playBureau;
    }

    public void changePlayBureau(int playBureau) {
        this.playBureau += playBureau;
        dbParamMap.put("playBureau", this.playBureau);
    }

    public void changeFinishBureau(int finishBureau) {
        this.finishBureau += finishBureau;
        dbParamMap.put("finishBureau", this.finishBureau);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
        dbParamMap.put("roomId", roomId);
    }

    public long getMasterId() {
        return masterId;
    }

    public void setMasterId(long masterId) {
        this.masterId = masterId;
        dbParamMap.put("masterId", masterId);
    }

    /**
     * 每小局结算之后调用
     */
    protected void calcAfter() {
        this.playedBureau = playBureau;
        changeFinishBureau(1);
        changeExtend();
    }


    /**
     * 已经加入房间的不能再次加入
     * <br/>房间已满不能再次加入
     *
     * @param player
     * @return
     */
    public final boolean isCanJoin(Player player) {
        return isCanJoin(player, true);
    }

    public final boolean isCanJoin(Player player, boolean syn) {
        if (syn) {
            synchronized (this) {
                if (getPlayerMap().containsKey(player.getUserId())) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_4));
                    return false;
                }

                if (getPlayerCount() >= getMaxPlayerCount()) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5));
                    return false;
                }

                if (getAllowGroupMember() > 0 && (player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                    if (isGroupRoom()) {
                        String groupId = loadGroupId();
                        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                        if (groupUser != null && groupUser.getGroupId().intValue() == allowGroupMember) {
                            player.setGroupUser(groupUser);
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                            return false;
                        }
                    } else {
                        player.loadGroupUser(String.valueOf(getAllowGroupMember()));
                        if ((player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                            return false;
                        }
                    }
                }

                if (SharedConstants.isRestrictOpen() && isKaiYiJu()) {
                    if (!AssisServlet.chatRoomUserCheck(this, player)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_54));
                        return false;
                    }
                }

                if (isGroupRoom()) {
                    String groupId = loadGroupId();
                    GroupInfo group = null;
                    try {
                        // 进房间金币限制
                        group = GroupDao.getInstance().loadGroupInfo(groupId,"0");
                        if(group == null){
                            player.writeErrMsg("亲友圈数据异常：" + groupId);
                            return false;
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:",e);
                    }

                    //信用房：未达到最低信用分时不可以进入房间
                    if (creditMode == 1) {
                        GroupUser gu = player.loadGroupUser(groupId);
                        if (gu == null || gu.getCredit() < creditJoinLimit) {
                            player.writeErrMsg(LangMsg.code_64, creditJoinLimit, gu.getCredit());
                            return false;
                        }
                        if(!checkGroupWarn(player,groupId)){
                            return false;
                        }
                    }

                    if(TableManager.isStopCreateGroupRoom(group)){
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_68));
                        return false;
                    }


                    //////   验证是否开房间免费   ////////////////
                    //////   这一步必须放在最后   ////////////////
                    if (GameConfigUtil.freeGameOfGroup(playType, groupId)) {
                        return true;
                    }
                }

                return isGoldRoom() || isCanJoin0(player);
            }
        } else {
            if (getPlayerMap().containsKey(player.getUserId())) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_4));
                return false;

            }
            if (getPlayerCount() >= getMaxPlayerCount()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5));
                return false;
            }

            if (getAllowGroupMember() > 0 && (player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                if (isGroupRoom()) {
                    String groupId = loadGroupId();
                    GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                    if (groupUser != null && groupUser.getGroupId().intValue() == allowGroupMember) {
                        player.setGroupUser(groupUser);
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return false;
                    }
                } else {
                    player.loadGroupUser(String.valueOf(getAllowGroupMember()));
                    if ((player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return false;
                    }
                }
            }

            if (SharedConstants.isRestrictOpen() && isKaiYiJu()) {
                if (!AssisServlet.chatRoomUserCheck(this, player)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_54));
                    return false;
                }
            }

            if (isGroupRoom()) {
                String groupId = loadGroupId();

                GroupInfo group = null;
                try {
                    // 进房间金币限制
                    group = GroupDao.getInstance().loadGroupInfo(groupId,"0");
                    if(group == null){
                        player.writeErrMsg("亲友圈数据异常：" + groupId);
                        return false;
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:",e);
                }

                //信用房：未达到最低信用分时不可以进入房间
                if (creditMode == 1) {
                    GroupUser gu = player.loadGroupUser(groupId);
                    if (gu == null || gu.getCredit() < creditJoinLimit) {
                        player.writeErrMsg(LangMsg.code_64, creditJoinLimit, gu.getCredit());
                        return false;
                    }
                    if(!checkGroupWarn(player,groupId)){
                        return false;
                    }
                }

                if(TableManager.isStopCreateGroupRoom(group)){
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_68));
                    return false;
                }


                //////   验证是否开房间免费   ////////////////
                //////   这一步必须放在最后   ////////////////
                if (GameConfigUtil.freeGameOfGroup(playType, groupId)) {
                    return true;
                }
            }
            return isGoldRoom() || isCanJoin0(player);
        }
    }

    /**
     * 获取俱乐部Id
     *
     * @return
     */
    public final String loadGroupId() {
        return serverKey.contains("_") ? serverKey.split("_")[0].substring(5) : serverKey.substring(5);
    }

    /**
     * 获取俱乐部房间的keyId
     *
     * @return
     */
    public final String loadGroupTableKeyId() {
        return serverKey.contains("_") ? serverKey.split("_")[1] : null;
    }

    /**
     * 发送解散俱乐部房间消息
     */
    public void dissGroupRoom() {
        if (!isGroupRoom()) {
            return;
        }
        String groupId = loadGroupId();
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, playType, groupId, playedBureau);
        GeneratedMessage msg = com.build();
        for (Player player1 : getPlayerMap().values()) {
            player1.writeSocket(msg);
            player1.writeErrMsg(LangHelp.getMsg(isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, id));
        }
        for (Player player2 : getRoomPlayerMap().values()) {
            player2.writeSocket(msg);
            player2.writeErrMsg(LangHelp.getMsg(isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, id));
        }

        if (isGroupMasterDiss() && isDissSendAccountsMsg()) {
            try {
                sendAccountsMsg();
            } catch (Throwable e) {
                LogUtil.errorLog.error("tableId=" + getId() + ",total calc Exception:" + e.getMessage(), e);
                GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + getId() + "被解散").build();
                for (Player player0 : getPlayerMap().values()) {
                    player0.writeSocket(errorMsg);
                }
            }
            calcCreditNew();
            setTiqianDiss(true);
        }
        this.diss();
    }

    /**
     * 判断是否能加入房间（房卡/钻石等数据检查）
     *
     * @param player
     * @return
     * @see #isCanJoin(Player)
     */
    public boolean isCanJoin0(Player player) {
        if (consumeCards() && !isGoldRoom() && !GameConfigUtil.freeGame(playType, player.getUserId()) && !player.isRobot()) {
            if (payType > 0 && (isAAConsume() || isAAConsume0() || (masterId == player.getUserId() && !isGroupRoom() && !NumberUtils.isDigits(getServerKey()) && !isDaikaiTable()))) {
                int needCard = loadPayConfig();
                // 如果玩家的钻石小于玩一局需要的钻石，则返回

                if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                    if (needCard < 0 || needCard > 0 && player.getUserTili() < needCard) {
                        player.writeErrMsg(UserResourceType.TILI.getName() + "不足");
                        return false;
                    }
                } else {
                    if (needCard < 0 || needCard > 0 && player.loadAllCards() < needCard) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public table_state getState() {
        return state;
    }

    public final void changeTableState(table_state state) {
        synchronized (this) {
            if (this.state == state) {
                return;
            }
            this.state = state;
        }

        dbParamMap.put("state", this.state.getId());
        if (state == table_state.play) {
            for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
                int seat = kv.getKey().intValue();
                if (seat != kv.getValue().getSeat()) {
                    LogUtil.errorLog.warn("table user seat error2:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

                    kv.getValue().setSeat(seat);
                    kv.getValue().setPlayingTableId(id);
                    changePlayers();
                }
            }

            if (getPlayBureau() == 1 && isKaiYiJu()) {
                AssisServlet.sendRoomStatus(this, "3");
            }
        }
        if (playedBureau == 0 && playBureau <= 1 && state == table_state.play && (NumberUtils.isDigits(serverKey) || isGroupRoom())) {
            String groupKey;
            String groupId;
            if (isGroupRoom()) {
                if (serverKey.contains("_")) {
                    String[] temps = serverKey.split("_");
                    groupKey = temps[1];
                    groupId = temps[0].substring(5);
                } else {
                    groupKey = null;
                    groupId = serverKey.substring(5);
                }
            } else {
                groupKey = serverKey;
                groupId = null;
            }
            if (groupKey != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", groupKey);
                if (buildCurrentState(state, map)) {
                    try {
                        if (Redis.isConnected() && StringUtils.isNotBlank(groupId) && NumberUtils.isDigits(groupKey)) {
                            String currentState = String.valueOf(map.get("currentState"));
                            groupTable = GroupDao.getInstance().loadGroupTableByKeyId(groupKey);
                            groupTable.setCurrentState(currentState);
                            groupTable.setCurrentCount(getPlayerCount());
                            RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupId, groupTable.loadGroupRoom()), GroupRoomUtil.loadWeight(currentState, getPlayerCount(), groupTable.getCreatedTime()), groupKey);
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), groupKey, JSON.toJSONString(groupTable));
                        }

                        GroupDao.getInstance().updateGroupTableByKeyId(map);
                    } catch (Exception e) {
                        LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
                    }
                }
            }
            if (groupId != null) {
                if (groupKey != null) {
                    try {
                        GroupTable gt = GroupDao.getInstance().loadGroupTableByKeyId(groupKey);
                        if (gt != null) {
                            GameUtil.autoCreateGroupTable(groupId, getPlayerClass(), gt.getConfigId());
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
                    }
                } else {
                    GameUtil.autoCreateGroupTable(groupId, getPlayerClass());
                }
            }
        }

        if (state != null && state != table_state.ready) {
            TableManager.removeUnavailableTable(this);
        }

        if ((state == table_state.over || state == table_state.play) && isGoldRoom() && "2".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType"))) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", getId());
            map.put("currentState", state == table_state.play ? "1" : "0");
            if (state == table_state.over) {
                map.put("gameCount", 1);
            }
            try {
                GoldRoomDao.getInstance().updateGoldRoomByKeyId(map);
            } catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }
        }
        changeTableState0(state);
    }

    public void changeTableState0(table_state state) {

    }

    private boolean buildCurrentState(table_state state, HashMap<String, Object> map) {
        if (playedBureau == 0 && playBureau <= 1 && state == table_state.play) {
            map.put("currentState", "1");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 人数已满并且都准备好了
     *
     * @return
     */
    public boolean isAllReady() {
        if (state == table_state.play) {
            return false;
        }
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if (!player.isRobot() && player.getState() != player_state.ready) {
                return false;
            }
        }
        return true;
    }

    /**
     * 准备后触发
     */
    public void ready() {

    }

    /**
     * 开始下一局时候触发
     *
     * @param com
     */
    public void startNext(ComRes.Builder com) {

    }

    public synchronized void checkDeal() {
        checkDeal(0);
    }

    /**
     * 检查发牌
     */
    public synchronized void checkDeal(long userId) {
        if (isAllReady()) {
            // ------ 开局前检查信用分是否足够----------------
            if(!checkCreditOnTableStart()){
                return;
            }

            // 发牌
            fapai();
            setLastActionTime(TimeUtil.currentTimeMillis());
            for (int i = 1; i <= getMaxPlayerCount(); i++) {
                Player player = getSeatMap().get(i);
                addPlayLog(StringUtil.implode(player.getHandPais(), ","));
            }
            // 发牌msg
            sendDealMsg(userId);
            // if (PdkConstants.isTest) {
            robotDealAction();
            // }
            updateGroupTableDealCount();
        } else {
            robotDealAction();
        }
    }

    /**
     * 开始下一局
     */
    public void startNext() {

    }

    /**
     * 开始发牌
     */
    public void fapai() {
        changeTableState(table_state.play);
        deal();
    }

    protected abstract void loadFromDB1(TableInf info);

    protected abstract void sendDealMsg();

    protected abstract void sendDealMsg(long userId);

    protected abstract void robotDealAction();

    public synchronized int getPlayerCount() {
        return getPlayerMap().size();
    }

    protected abstract void initNext1();

    protected abstract void deal();

    /**
     * 下一次出牌的seat
     *
     * @return
     */
    public abstract int getNextDisCardSeat();

    public abstract Player getPlayerBySeat(int seat);

    public abstract <T extends Player> Map<Integer, T> getSeatMap();

    public abstract <T extends Player> Map<Long, T> getPlayerMap();

    public void answerDiss(int seat, int answer) {
        if (answer == 1 && sendDissTime == 0) {
            sendDissTime = TimeUtil.currentTimeMillis();
        }
        answerDissMap.put(seat, answer);
        dbParamMap.put("answerDiss", JSON_TAG);
        if (answer == 1) {
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_agreediss, sendDissTime + "", JacksonUtil.writeValueAsString(answerDissMap));
            broadMsg(builder.build());
            LogUtil.monitor_i("table diss:" + getId() + " sendTime:" + sendDissTime);
        }

    }

    public void checkSendDissMsg(Player player) {
        if (answerDissMap != null && !answerDissMap.isEmpty()) {
            sendDissRoomMsg(player, false);
        }

    }

    public void clearAnswerDiss() {
        sendDissTime = 0;
        answerDissMap.clear();
        dbParamMap.put("answerDiss", JSON_TAG);
    }

    public boolean checkDiss() {
        return checkDiss(null);
    }

    /**
     * 检查应答解散
     *
     * @return
     */
    public synchronized boolean checkDiss(Player sendplayer) {
        if (isTiqianDiss()) {
            return false;
        }
        int dissCount = 0;
        // boolean isHasLeave = false;
        if (answerDissMap.isEmpty()) {
            return false;
        }
        long now = TimeUtil.currentTimeMillis();
        for (Entry<Integer, Player> entry : getSeatMap().entrySet()) {
            if (entry.getValue().isRobot()) {
                dissCount++;
                continue;
            }

            if (answerDissMap.containsKey(entry.getKey())) {
                int answer = answerDissMap.get(entry.getKey());
                if (answer == 1) {
                    dissCount++;

                }
                // diss = false;

            } else {
                // 离线超时自动算同意
//				if (entry.getValue().getIsEntryTable() == SharedConstants.table_offline) {
                if (now - sendDissTime >= getDissTimeout()) {
                    dissCount++;
                }
//				}
            }
        }

        boolean diss = dissCount >= loadAgreeCount();
        LogUtil.monitor_i("table check diss:" + getId() + " dissCount:" + dissCount + " playerCount:" + loadAgreeCount() + " pb" + getPlayBureau() + " result:" + diss + " sendTime:"
                + sendDissTime);

        if (!diss) {// && totalBureau > 1
            if (sendplayer != null && playBureau == 1) {
                if (StringUtils.isBlank(serverKey)) {
                    if (sendplayer.getUserId() == masterId && state == table_state.ready) {
                        diss = true;
                    }
                }
            }
        }
        
        
        
        if (diss) {
            //boolean back = false;
            if (isDissSendAccountsMsg()) {
                try {
                    sendAccountsMsg();
                    calcCreditNew();
                } catch (Throwable e) {
                    LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                    for (Player player : getPlayerMap().values()) {
                        player.writeSocket(errorMsg);
                    }
                }
            }
//            else {
//                if (getPayType() != 1) {
//                    back = true;
//                }
//            }
            // 解散前发送解散消息
            int groupId = 0;
            if (isGroupRoom()) {
                groupId = Integer.parseInt(loadGroupId());
            }

            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, playType, groupId, playedBureau);
            Player dissPlayer = sendplayer;

            GeneratedMessage msg = com.build();
            for (Player player : getSeatMap().values()) {
                if (dissPlayer == null) {
                    dissPlayer = player;
                }
                player.writeSocket(msg);
            }

            for (Player player : roomPlayerMap.values()) {
                player.writeSocket(msg);
            }

            if (answerDissMap != null && !answerDissMap.isEmpty()) {

                List<Integer> tempList = new ArrayList<>(answerDissMap.keySet());
                int applySeat = tempList.get(0);
                Player applyPlayer = getSeatMap().get(applySeat);

                if (dissPlayer != null) {
                    dissPlayer.sendActionLog(LogConstants.reason_diss,
                            "id:" + id + " send :" + (applyPlayer != null ? applyPlayer.getUserId() : 0) + " map:" + JacksonUtil.writeValueAsString(answerDissMap));

                }
            }

            setTiqianDiss(true);
            if (isDaikaiTable()) {
                Integer returnCard = 0;
                Player player = null;
                if (isDaikaiRoomReturnConsume()) {
                    returnCard = loadPayConfig();
                    player = PlayerManager.getInstance().getPlayer(getCreatorId());
                    if (player == null) {
                        try {
                            player = ObjectUtil.newInstance(sendplayer.getClass());
                        } catch (Throwable e) {
                            LogUtil.errorLog.error("Throwable:" + e.getMessage(), e);
                        }

                        RegInfo user = UserDao.getInstance().selectUserByUserId(getCreatorId());
                        player.loadFromDB(user);
                    }
                }

                int result = diss();

                LogUtil.msg("start daikai table diss:tableId=" + getId() + ",creatorId=" + getCreatorId() + ",returnCard=" + returnCard + ",result=" + result + (player != null));

                if (result == 1 && returnCard > 0 && player != null) {
                    CardSourceType sourceType = getCardSourceType(payType);
                    player.changeCards(returnCard, 0, true, sourceType);
                    LogUtil.msg("finish daikai table diss:tableId=" + getId() + ",creatorId=" + getCreatorId() + ",returnCard=" + returnCard + ",result=" + result);
                    // 存到消息
                    MessageUtil.sendMessage(true, true, UserMessageEnum.TYPE1, player, "解散代开房间[" + getId() + "]返还:房卡x" + returnCard, null);
                }
            } else {
                diss();
            }
        } else {
            sendDissRoomMsg(sendplayer, true);
        }
        return diss;
    }

    public boolean isDissSendAccountsMsg() {
        return playBureau > 1;
    }

    public boolean isDaikaiRoomReturnConsume() {
        return getPayType() != 1 && playBureau <= 1;
    }

    /**
     * 是否自动加人、踢人的房间
     *
     * @return
     */
    public boolean isAutoKickMinGoldRoom() {
        return false;
    }

    /**
     * 获取超时时间
     *
     * @return
     */
    public long getDissTimeout() {
        if (StringUtils.isNotBlank(serverKey) && !NumberUtils.isDigits(serverKey) && !isGroupRoom()) {
            return Long.parseLong(ResourcesConfigsUtil.loadServerPropertyValue("table0_diss_timeout", "30000"));
        } else if (isGroupRoom()) {
            String timeout = ResourcesConfigsUtil.loadServerPropertyValue("table_group_diss_timeout");
            return NumberUtils.isDigits(timeout) ? Long.parseLong(timeout) : SharedConstants.diss_timeout;
        } else {
            String timeout = ResourcesConfigsUtil.loadServerPropertyValue("table_diss_timeout");
            return NumberUtils.isDigits(timeout) ? Long.parseLong(timeout) : SharedConstants.diss_timeout;
        }
    }

    /**
     * 普通房间第一个加入的人是否自动准备
     *
     * @return
     */
    public boolean autoReadyForFirstPlayerOfCommon() {
        String strs = ResourcesConfigsUtil.loadServerPropertyValue("auto_ready_first_player");
        if (StringUtils.isBlank(strs)) {
            return true;
        } else if (strs.length() <= 2) {
            return false;
        } else {
            return "ALL".equals(strs) || strs.contains(new StringBuilder(8).append("|").append(playType).append("|").toString());
        }
    }

    /**
     * 俱乐部房间第一个加入的人是否自动准备
     *
     * @return
     */
    public boolean autoReadyForFirstPlayerOfGroup() {
        String strs = ResourcesConfigsUtil.loadServerPropertyValue("auto_ready_group_first_player");
        if (StringUtils.isBlank(strs)) {
            return true;
        } else if (strs.length() <= 2) {
            return false;
        } else {
            return "ALL".equals(strs) || strs.contains(new StringBuilder(8).append("|").append(playType).append("|").toString());
        }
    }

    /**
     * 同意解散的人数
     *
     * @return
     */
    public int getDissPlayerAgreeCount() {
        int temp = (int) Math.ceil(getPlayerMap().size() * 2.0 / 3);
        return temp;
    }

    /**
     * 房间解散人数
     *
     * @return
     */
    public int loadAgreeCount() {
        if (StringUtils.isBlank(serverKey)) {
            return loadAgreeCountForCommon();
        } else if (NumberUtils.isDigits(serverKey) || isGroupRoom()) {
            return loadAgreeCountForGroup();
        } else {
            return loadAgreeCountForTraining();
        }
    }

    /**
     * 普通房间解散人数
     *
     * @return
     */
    public int loadAgreeCountForCommon() {
        return getDissPlayerAgreeCount();
    }

    /**
     * 军团房间解散人数
     *
     * @return
     */
    public int loadAgreeCountForGroup() {
        return loadAgreeCountForCommon();
    }

    /**
     * 练习场房间解散人数
     *
     * @return
     */
    public int loadAgreeCountForTraining() {
        int temp = (int) Math.ceil(getPlayerCount() * 2.0 / 3);
        return temp;
    }

    public void sendDissRoomMsg(Player sendplayer, boolean sendAll) {
        if (sendplayer == null) {
            return;
        }

        long nowTime = TimeUtil.currentTimeMillis();
        int countDown = (int) ((getDissTimeout() - (nowTime - sendDissTime)) / 1000);

        ComRes.Builder com;
        List<Integer> tempList = new ArrayList<>(answerDissMap.keySet());
        int applySeat = tempList.get(0);

        List<String> statusStr = new ArrayList<>();
        List<String> applyStr = new ArrayList<>();
        StringBuilder sb;
        Integer answerStatus;

        BaseTable table = sendplayer.getPlayingTable();
        if (table == null) {
            return;
        }

        for (Player player : getSeatMap().values()) {
            sb = new StringBuilder();
            // sb.append("玩家[" + player.getName() + "]");
            sb.append(player.getUserId()).append(",");

            answerStatus = answerDissMap.get(player.getSeat());
            if (answerStatus != null && answerStatus == 1) {
                if (applySeat == player.getSeat()) {
//					if (isDn) {
                    sb.append("2");
//					} else {
//						sb.append("申请解散房间");
//					}
                } else {
//					if (isDn) {
                    sb.append("1");
//					} else {
//						sb.append("同意解散房间");
//					}
                }
            } else {
//				if (isDn) {
                sb.append("0");
//				} else {
//					sb.append("等待选择");
//				}
            }
            sb.append(",").append(player.getName());
            statusStr.add(sb.toString());
        }
        applyStr.addAll(statusStr);

        for (Player tableplayer : getSeatMap().values()) {
            if (!sendAll && tableplayer.getUserId() != sendplayer.getUserId()) {
                continue;
            }
            answerStatus = answerDissMap.get(tableplayer.getSeat());
            if (answerStatus == null) {
                answerStatus = 0;// 等待选择
            }
            com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_senddisstable, answerStatus, countDown, applyStr);
            tableplayer.writeSocket(com.build());

            LogUtil.msgLog.info("diss table msg:userId=" + tableplayer.getUserId() + ",answerStatus=" + answerStatus + ",countDown=" + countDown + ",applyStr=" + applyStr);
        }
    }

    /**
     * 是否允许选座位（分组游戏该值可能为true）
     *
     * @return
     */
    public boolean allowChooseSeat() {
        return false;
    }

    public boolean checkDissByDate() {
        boolean diss;
        if (getCreateTime() != null && System.currentTimeMillis() - getCreateTime().getTime() >= SharedConstants.DAY_IN_MINILLS) {
            diss = true;
            for (Player player : getSeatMap().values()) {
                if (player.getIsLeave() == 0 || player.getIsOnline() == 1) {
                    diss = false;
                    break;
                }
            }
        } else if (getState() == table_state.ready && StringUtils.isNotBlank(serverKey) && getPlayBureau() <= 1 && !serverKey.startsWith("group")) {
            diss = true;
            Iterator<Entry<Long, Player>> it = getPlayerMap().entrySet().iterator();
            while (it.hasNext()) {
                diss = false;
                Player player = it.next().getValue();
                if (player.getIsLeave() == 0 || player.getIsOnline() == 1) {
                } else {
                    if (player.getLogoutTime() != null && (System.currentTimeMillis() - player.getLogoutTime().getTime() >= 3 * 60 * 1000)) {
                        if (quitPlayer(player)) {
                            onPlayerQuitSuccess(player);
                        }
                    }
                }
            }
        } else {
            diss = false;
        }

        return diss;
    }

    public CreateTableRes buildCreateTableRes(long userId) {
        return buildCreateTableRes(userId, false, false);
    }

    public abstract CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady);

    public BaiRenTableRes buildBaiRenTableRes(long userId) {
        return buildBaiRenTableRes(userId, false, false);
    }

    /**
     * 百人玩法必须实现此类
     *
     * @param userId
     * @param isrecover
     * @param isLastReady
     * @return
     */
    public BaiRenTableRes buildBaiRenTableRes(long userId, boolean isrecover, boolean isLastReady) {
        BaiRenTableRes.Builder res = BaiRenTableRes.newBuilder();
        return res.build();
    }

    public CreateTableRes buildCreateTableRes1(CreateTableRes.Builder res) {
        return buildCreateTableRes1(res, false);
    }

    public CreateTableRes buildCreateTableRes1(CreateTableRes.Builder res, boolean isLastReady) {
        res.setIsDaiKai(isDaikaiTable() ? 1 : 0);
        res.addExt(this.isAAConsume() ? 1 : 0);// 是否强制AA开房

        res.addExt("1".equals(roomModeMap.get("2")) ? 1 : 0);
        res.addExt("1".equals(roomModeMap.get("1")) ? 1 : 0);

        if (isLastReady) {
            res.addExt(1);
        } else {
            res.addExt(0);
        }

        buildCreateTableRes0(res);

        return res.build();
    }

    protected void buildCreateTableRes0(CreateTableRes.Builder res) {
        if (state == table_state.play) {
            for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
                int seat = kv.getKey().intValue();
                if (seat != kv.getValue().getSeat()) {
                    LogUtil.errorLog.warn("table user seat error1:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

                    kv.getValue().setSeat(seat);
                    kv.getValue().setPlayingTableId(id);
                    changePlayers();
                }
            }
        }

        //0普通房间1军团（俱乐部）2练习场3金币场4比赛场
        int tableType;
        if (isMatchRoom()) {
            tableType = 4;
        } else if (isGoldRoom()) {
            tableType = 3;
        } else if (StringUtils.isBlank(serverKey)) {
            tableType = 0;
        } else if (isGroupRoom() || NumberUtils.isDigits(serverKey)) {
            tableType = 1;
        } else {
            tableType = 2;
        }

        res.setTableType(tableType);
        res.setGroupProperty(tableType == 1 ? loadGroupMsg() : "");
        res.setMasterId(masterId+"");

        if(res.getStrExtList().size() == 0) {
            long tableStartTime = getTablePayStartTime();  //牌桌开始时间
            int time = (int) (System.currentTimeMillis() - tableStartTime) / 1000;
            if (tableStartTime != 0) {
                res.setDealDice(time); //游戏时间（秒）【发送牌桌游戏持续时间】
            } else {
                res.setDealDice(0);
            }
        }


        if(intParams != null){
            res.addAllIntParams(intParams);
        }
        if(strParams != null) {
            res.addAllStrParams(strParams);
        }

        if(creditMode == 1) {
            res.addCreditConfig(creditMode);                   //0
            res.addCreditConfig(creditJoinLimit);              //1
            res.addCreditConfig(creditDissLimit);              //2
            res.addCreditConfig(creditDifen);                  //3
            res.addCreditConfig(creditCommission);             //4
            res.addCreditConfig(creditCommissionMode1);        //5
            res.addCreditConfig(creditCommissionMode2);        //6
            res.addCreditConfig(creditCommissionLimit);        //7
            res.addCreditConfig(creditCommissionBaoDi);        //8
        }

        if (StringUtils.isNotBlank(roomName)) {
            res.setRoomName(roomName);
        }

        res.addGeneralExt(""+chatConfig);
    }

    public void setGroupTableConfig(GroupTableConfig groupTableConfig) {
        this.groupTableConfig = groupTableConfig;
    }

    public String loadGroupMsg() {
        if (NumberUtils.isDigits(serverKey)) {
            if (groupTableConfig == null) {
                synchronized (this) {
                    try {
                        if (groupTableConfig == null) {
                            GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(serverKey);
                            if (groupTable != null)
                                groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            }
            if (groupTableConfig != null) {
                return groupTableConfig.getParentGroup() + "," + groupTableConfig.getGroupId();
            }
        } else if (isGroupRoom()) {
            String[] strs = serverKey.split("_");
            return "0," + (strs.length >= 2 ? strs[0].substring(5) : serverKey.substring(5));
        }
        return "";
    }

    /**
     * 广播消息
     */
    public void broadMsg(GeneratedMessage message) {
        for (Player player : getPlayerMap().values()) {
            player.writeSocket(message);
        }
    }

    /**
     * 广播消息给旁观者
     */
    public void broadMsg0(GeneratedMessage message) {
        for (Player player : roomPlayerMap.values()) {
            player.writeSocket(message);
        }
    }

    public void broadMsg(String msg) {
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, msg);
        broadMsg(builder.build());
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (Player player : getSeatMap().values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }

        return num;
    }

    public boolean isRuning() {
        return isRuning;
    }

    public void setRuning(boolean isRuning) {
        this.isRuning = isRuning;
    }

    public boolean isOver() {
        return state == table_state.over;
    }

    public int getLastWinSeat() {
        return lastWinSeat;
    }

    public void setLastWinSeat(int lastWinSeat) {
        if(playBureau == 1 && tablePayStartTime == 0)  //当前玩的局数
        {
            this.tablePayStartTime = System.currentTimeMillis();
        }
        this.lastWinSeat = lastWinSeat;
        dbParamMap.put("lastWinSeat", this.lastWinSeat);
    }

    public long getGotyeRoomId() {
        return gotyeRoomId;
    }

    public void setGotyeRoomId(long gotyeRoomId) {
        this.gotyeRoomId = gotyeRoomId;
        dbParamMap.put("gotyeRoomId", this.gotyeRoomId);
    }

    /**
     * 人数未满或者人员离线
     *
     * @return 0 可以打牌 1人数未满 2人员离线
     */
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }

        for (Player player : getSeatMap().values()) {
            if (player.getIsEntryTable() != SharedConstants.table_online) {
                return 2;
            }
        }
        return 0;
    }

    public void addPlayLog(int disCardRound, int seat, String... o) {
        StringBuilder log = new StringBuilder().append(disCardRound).append("_").append(seat).append("_").append(StringUtil.implode(o, "_"));
        addPlayLog(log);
    }

    public void addPlayLog(int seat, String... o) {
        StringBuilder log = new StringBuilder().append(seat).append("_").append(StringUtil.implode(o, "_"));
        addPlayLog(log);
    }

    public void addPlayLog(int seat, List<?> list, String delimiter) {
        StringBuilder log = new StringBuilder().append(seat).append("_").append(StringUtil.implode(list, delimiter));
        addPlayLog(log);
    }

    public void addPlayLog(String playLog) {
        synchronized (this) {
            this.playLog = new StringBuilder().append(this.playLog).append(playLog).append(";").toString();
            dbParamMap.put("playLog", this.playLog);
        }
    }

    public void addPlayLog(StringBuilder playLog) {
        playLog.append(";");
        synchronized (this) {
            this.playLog = playLog.insert(0, this.playLog).toString();
            dbParamMap.put("playLog", this.playLog);
        }
    }

    public void clearPlayLog() {
        synchronized (this) {
            this.playLog = "";
            dbParamMap.put("playLog", "");
        }
    }

    public String getPlayLog() {
        return playLog;
    }

    public void broadIsOnlineMsg(Player player, int online) {
        if (online == SharedConstants.table_online) {
            player.setIsEntryTable(SharedConstants.table_online);
        } else {
            player.setIsEntryTable(SharedConstants.table_offline);
        }
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_isonlinetable, player.getSeat(), online, String.valueOf(player.getUserId()));

        GeneratedMessage msg = res.build();
        for (Player seatPlayer : getPlayerMap().values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            seatPlayer.writeSocket(msg);
        }

        broadMsg0(msg);
    }

    /**
     * 广播牌局在线消息
     *
     * @param userId
     * @param online
     */
    public void broadIsOnlineMsg(long userId, int online) {
        Player player = getPlayerMap().get(userId);
        if (player == null) {
            Player player0 = roomPlayerMap.get(userId);
            if (player0 != null && player0.getMyExtend().getPlayerStateMap().get("2") != null) {
                broadIsOnlineMsg(player0, online);
            }
        } else {
            broadIsOnlineMsg(player, online);
        }
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getDisCardSeat() {
        return disCardSeat;
    }

    public void setDisCardSeat(int disCardSeat) {
        this.disCardSeat = disCardSeat;
        dbParamMap.put("disCardSeat", this.disCardSeat);
    }

    public void setDisCardRound(int disCardRound) {
        this.disCardRound = disCardRound;
        dbParamMap.put("disCardRound", disCardRound);
    }

    public int getDisCardRound() {
        return disCardRound;
    }

    public void changeDisCardRound(int disCardRound) {
        this.disCardRound += disCardRound;
        dbParamMap.put("disCardRound", this.disCardRound);
    }

    public int getNowDisCardSeat() {
        return nowDisCardSeat;
    }

    public void setNowDisCardSeat(int nowDisCardSeat) {
        this.nowDisCardSeat = nowDisCardSeat;
        dbParamMap.put("nowDisCardSeat", nowDisCardSeat);
    }

    public abstract void setConfig(int index, int val);

    /**
     * 配置cofnig
     *
     * @param index
     * @return
     */
    public int getConifg(int index) {
        if (config == null || config.size() <= index) {
            return 0;
        }
        return config.get(index);
    }

    public List<Integer> getConfig() {
        return config;
    }

    /**
     * 发送结算消息
     */
    public abstract void sendAccountsMsg();

    /**
     * 牌桌最大人数
     *
     * @return
     */
    public abstract int getMaxPlayerCount();

    /**
     * 保存Log日志
     *
     * @return
     */
    public abstract void saveLog(boolean over, long winId, Object res);

    /**
     * 保存Log日志之后调用
     *
     * @param logId
     * @param over
     * @param playNo
     */
    public final void saveTableRecord(long logId, boolean over, int playNo) {
        if (NumberUtils.isDigits(serverKey) || isGroupRoom()) {
            try {
                String keyId = serverKey.contains("_") ? serverKey.split("_")[1] : serverKey;
                GroupTable groupTable;
                if (this.groupTable != null && keyId.equals(String.valueOf(this.groupTable.getKeyId()))) {
                    groupTable = this.groupTable;
                } else {
                    groupTable = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
                }

                if (groupTable != null) {
                    TableRecord tableRecord = new TableRecord();
                    tableRecord.setCreatedTime(new Date());
                    tableRecord.setGroupId(groupTable.getGroupId());
                    tableRecord.setInitMsg("");
                    tableRecord.setLogId(String.valueOf(logId));
                    tableRecord.setModeMsg(groupTable.getTableMsg());
                    tableRecord.setPlayNo(playNo);
                    tableRecord.setRecordType(over ? 1 : 0);
                    tableRecord.setResultMsg(over ? saveRecordResultMsg() : "");
                    tableRecord.setTableId(groupTable.getTableId());
                    tableRecord.setTableNo(groupTable.getKeyId());

                    GroupDao.getInstance().createTableRecord(tableRecord);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        } else if (isGoldRoom()) {
            try {
                for (Player player : getPlayerMap().values()) {
                    GoldRoomDao.getInstance().updateGoldRoomUser(id, player.getUserId(), player.loadScore(), "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1")) ? String.valueOf(logId) : "");
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    public String saveRecordResultMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("createTime", getCreateTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getCreateTime()));
        if (getSpecialDiss() == 1) {
            jsonObject.put("dissState", "1");//群主解散
        } else {
            if (answerDissMap != null && !answerDissMap.isEmpty()) {
                jsonObject.put("dissState", "2");//玩家申请解散
                StringBuilder str = new StringBuilder();
                for (Entry<Integer, Integer> entry : answerDissMap.entrySet()) {
                    Player player0 = getSeatMap().get(entry.getKey());
                    if (player0 != null) {
                        str.append(player0.getName()).append(",");
                    }
                }
                if (str.length() > 0) {
                    str.deleteCharAt(str.length() - 1);
                }
                jsonObject.put("dissPlayer", str.toString());
            } else {
                jsonObject.put("dissState", "0");//正常打完
            }
        }
        return jsonObject.toString();
    }

    /**
     * 大结算后调用
     *
     * @param player
     */
    public final void updatePlayerScore(Player player, int isWinner) {
        int userGroup = -1;
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            GroupUser groupUser = player.loadGroupUser(groupId);
            if (groupUser != null) {
                userGroup = groupUser.getUserGroup();
            }
        }
        if (NumberUtils.isDigits(serverKey)) {
            try {
                GroupDao.getInstance().updateTableUserScore(player.loadScore(), player.getUserId(), Long.parseLong(serverKey), isWinner, player.getWinLoseCredit(), player.getCommissionCredit(), userGroup);
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        } else if (isGroupRoom() && serverKey.contains("_")) {
            try {
                GroupDao.getInstance().updateTableUserScore(player.loadScore(), player.getUserId(), Long.parseLong(serverKey.split("_")[1]), isWinner, player.getWinLoseCredit(), player.getCommissionCredit(), userGroup);
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param objects
     * @throws Exception
     */
    public abstract void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception;

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param objects
     * @throws Exception
     */
    public abstract void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception;

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param params
     * @param strParams
     * @param saveDb
     * @throws Exception
     */
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return false;
    }

    public boolean createBaiRenTable(Player player, int play, List<Integer> params, List<String> strParams) throws Exception {
        return false;
    }

    public boolean saveSimpleTable() throws Exception {
        return false;
    }

    public abstract int getWanFa();

    public boolean isTest() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
    }

    public abstract void checkReconnect(Player player);

    public boolean isCompetition() {
        return isCompetition > 0;
    }

    /**
     * 托管
     */
    public void checkCompetitionPlay() {
        synchronized (this) {
            checkAutoPlay();
        }
    }

    public abstract void checkAutoPlay();

    public abstract Class<? extends Player> getPlayerClass();

    public String buildDissInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(DataMapUtil.explode(answerDissMap));
        sb.append("_");
        sb.append(sendDissTime);
        return sb.toString();
    }

    /**
     * 重连和加入牌桌时检查玩家是否合法
     *
     * @param player
     * @return
     */
    public boolean checkPlayer(Player player) {
        Map<Long, Player> playerMap = new HashMap<>(getPlayerMap());
        if (player != null && !playerMap.containsKey(player.getUserId()) && !roomPlayerMap.containsKey(player.getUserId())) {
            // 房间内没有找到玩家
            if (player.getPlayingTableId() == id) {
                // 玩家身上标记在这个房间
                player.setPlayingTableId(0);
                player.saveBaseInfo();
            }
            return false;
        }

        for (Player p : playerMap.values()) {
            if (!p.getClass().getName().equals(getPlayerClass().getName())) {
                if (p.isRobot() && p.getPlayingTableId() == 0) {
                    LogUtil.e("table checkplayer robot err-->" + playType + " PlayingTableId is 0" + p.getUserId());
                    diss();
                    return false;
                }
                if (playBureau == 1 && !isDaikaiTable()) {
                    // 还没有开始打牌
                    diss();
                }
                LogUtil.e("table checkplayer err-->" + playType + " not majiangplayer:" + p.getUserId());
                return false;
            }
        }

        return true;
    }

    public int getAnswerDissCount() {
        return answerDissMap.size();
    }

    /**
     * 消耗房卡
     *
     * @return
     */
    public boolean consumeCards() {
        return SharedConstants.consumecards && checkPay;
    }

    public List<List<Integer>> getZp() {
        return zp;
    }

    public void setZp(List<List<Integer>> zp) {
        this.zp = zp;
    }

    public void setZpMap(long zpUser, int zpValue) {
        zpMap.put(zpUser, zpValue);
    }

    /**
     * 出牌离自己是最近的
     */
    public int getNearSeat(int nowSeat, List<Integer> seatList) {
        if (seatList.contains(nowSeat)) {
            // 出牌离自己是最近的
            return nowSeat;
        }
        for (int i = 0; i < getPlayerCount() - 1; i++) {
            int seat = calcNextSeat(nowSeat);
            if (seatList.contains(seat)) {
                return seat;
            }
            nowSeat = seat;
        }
        return 0;
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        return seat + 1 > getMaxPlayerCount() ? 1 : seat + 1;
    }

    public int getNextSeat(int seat) {
        List<Integer> seatList = new ArrayList<>(getSeatMap().keySet());
        if (seatList.isEmpty()) {
            return 0;
        }
        Collections.sort(seatList);
        int findIndex = seatList.indexOf(seat);
        if (findIndex != -1) {
            int index = findIndex + 1 > seatList.size() - 1 ? 0 : findIndex + 1;
            return seatList.get(index);
        } else {
            return 0;
        }

    }

    public int getNextSeat(int seat, List<Integer> seatList) {
        if (seatList.isEmpty()) {
            return 0;
        }
        Collections.sort(seatList);
        int findIndex = seatList.indexOf(seat);
        if (findIndex != -1) {
            int index = findIndex + 1 > seatList.size() - 1 ? 0 : findIndex + 1;
            return seatList.get(index);
        } else {
            return 0;
        }

    }

    /**
     * 检查是否能解散房间
     */
    public synchronized boolean checkRoomDiss() {
        if (isTiqianDiss()) {
            return false;
        }
        long nowTime = TimeUtil.currentTimeMillis();
        Player applyPlayer = null;
        int applySeat = 0;
        List<Integer> tempList = null;
        if (sendDissTime > 0 && nowTime - sendDissTime >= getDissTimeout()) {
            tempList = new ArrayList<>(answerDissMap.keySet());
            if (tempList == null || tempList.size() <= 0) {
                return false;
            }
            applySeat = tempList.get(0);
            applyPlayer = getSeatMap().get(applySeat);
            if (applyPlayer == null) {
                return false;
            }

            // 解散前发送解散消息
            int groupId = 0;
            if (isGroupRoom()) {
                groupId = Integer.parseInt(loadGroupId());
            }

            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, playType, groupId, playedBureau);
            for (Player temp : getPlayerMap().values()) {
                if (temp != null) {
                    temp.writeSocket(com.build());
                }
            }

            LogUtil.monitor_i("table diss:" + getId() + " apply player:" + applyPlayer.getUserId() + " play:" + getPlayType() + " pb" + getPlayBureau() + " timeout 5 minute");
            try {
                sendAccountsMsg();
            } catch (Throwable e) {
                LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);

                GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                for (Player player : getPlayerMap().values()) {
                    player.writeSocket(errorMsg);
                }
            } finally {
                calcCreditNew();
                setTiqianDiss(true);
                return diss() > 0;
            }
        }
        return false;
    }

    // 是否为代开房间
    public boolean isDaikaiTable() {
        return daikaiTableId > 0 && GoldRoomUtil.isNotGoldRoom(daikaiTableId);
    }

    /**
     * 是否是无房号金币场房间
     *
     * @return
     */
    public boolean isGoldRoom() {
        return GoldRoomUtil.isGoldRoom(daikaiTableId) || GoldRoomUtil.isGoldRoom(id);
    }

    public long getCreateTableId(long userId, int playType) {
        long tableId;

        if (isDaikaiTable() || isGoldRoom()) {
            tableId = daikaiTableId;
        } else if (groupTable != null && groupTable.getTableId() != null && groupTable.getTableId().intValue() > 0) {
            tableId = groupTable.getTableId().intValue();
        } else {
            int tableType;
            if (groupTableConfig != null || groupTable != null || allowGroupMember > 0 || isGroupRoom()) {
                tableType = 1;
            } else {
                tableType = 0;
            }
            tableId = TableManager.getInstance().generateId(userId, playType, tableType);
        }

        return tableId;
    }

    public long getDaikaiTableId() {
        return daikaiTableId;
    }

    public void setDaikaiTableId(long daikaiTableId) {
        this.daikaiTableId = daikaiTableId;
        dbParamMap.put("daikaiTableId", daikaiTableId);
    }

    // 代开房间,没开局是不能解散的
    public boolean canDissTable(Player player) {
        if (isDaikaiTable() && state == table_state.ready && this.playBureau < 2) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_204));
            return false;
        }
        if (isGroupRoom()) {
            try {
                String[] strs = serverKey.split("_");
//                int groupId = Integer.parseInt(strs.length >= 2 ? strs[0].substring(5) : serverKey.substring(5));
                String groupTableKey = strs.length >= 2 ? strs[1] : null;

                GroupTable gt = groupTableKey == null ? GroupDao.getInstance().loadGroupTable(player.getUserId(), id) : GroupDao.getInstance().loadGroupTableByKeyId(groupTableKey);
                if (gt != null) {
                    String[] tempMsgs = new JsonWrapper(gt.getTableMsg()).getString("strs").split(";")[0].split("_");
//                    String payType = tempMsgs[0];
                    String userId = tempMsgs[1];
                    if (userId.equals(String.valueOf(player.getUserId())) && gt.getCurrentState().equals("0") && playedBureau <= 0 && (gt.getCurrentCount().intValue() <= 0 || (getPlayerMap().containsKey(player.getUserId())))) {
                        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, getPlayType(), gt.getGroupId().intValue(), playedBureau);

                        GeneratedMessage msg = com.build();
                        for (Player player0 : getSeatMap().values()) {
                            player0.writeSocket(msg);
                        }

                        for (Player player0 : getRoomPlayerMap().values()) {
                            player0.writeSocket(msg);
                        }
                        diss();
                    } else if ((gt.isPlaying() || gt.isOver()) && getPlayerMap().containsKey(player.getUserId())) {
                        return true;
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
            return false;
        }

        return true;
    }

    // build代开房间玩家的信息
    public String buildDaikaiPlayerInfo() {
        StringBuilder userInfo = new StringBuilder();
        for (Player player : getSeatMap().values()) {
            // userInfo.append(player.getUserId());
            // userInfo.append(",");
            userInfo.append(player.getName());
            userInfo.append(",");
            userInfo.append(player.getSex());
            userInfo.append(";");
        }

        return userInfo.toString();
    }

    // 修改代开房间的信息
    public void updateDaikaiTableInfo() {
        if (isDaikaiTable()) {
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("tableId", daikaiTableId);
            paramMap.put("createTime", new Date());
            paramMap.put("state", 1);
            paramMap.put("createFlag", 1);
            paramMap.put("playerInfo", buildDaikaiPlayerInfo());
            paramMap.put("extend", GameServerConfig.SERVER_ID);
            TableDao.getInstance().updateDaikaiTable(paramMap);
        }
    }

    // 修改代开房间玩家的信息
    public int updateDaikaiTablePlayer() {
        if (isDaikaiTable()) {
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("tableId", daikaiTableId);
            paramMap.put("playerInfo", buildDaikaiPlayerInfo());
            return TableDao.getInstance().updateDaikaiTable(paramMap);
        }
        return 0;
    }

    public void afterMakeOverMasterId(Player player) {

    }

    // 转让代开房间房主
    public boolean makeOverMasterId(Player player) {
        if (getPlayerMap().size() < 1) {
            setMasterId(0);
            return false;
        }

        List<Long> userIds = new ArrayList<>();
        for (Player temp : getPlayerMap().values()) {
            if (temp.getUserId() == player.getUserId()) {
                continue;
            }
            userIds.add(temp.getUserId());
        }
        if (userIds.size() > 0) {
            Long masterId2 = userIds.get(RandomUtils.nextInt(userIds.size()));
            setMasterId(masterId2);
            afterMakeOverMasterId(getPlayerMap().get(masterId2));
            return true;
        } else {
            setMasterId(0);
        }
        return false;
    }

    // 解散代开房间
    public int dissDaikaiTable() {
        if (!isDaikaiTable()) {
            return 0;
        }

        boolean needReturn = true;
        if (getPlayBureau() > 1) {
            needReturn = false;
        } else {
            if (isTiqianDiss()) {
                needReturn = false;
            }
        }
//		boolean needReturn = false;

        return TableDao.getInstance().dissDaikaiTable(getId(), needReturn);
    }

    public boolean isTiqianDiss() {
        return tiqianDiss;
    }

    public void setTiqianDiss(boolean tiqianDiss) {
        this.tiqianDiss = tiqianDiss;
    }

    public boolean isCanReady() {
        return true;
    }

    public long getSendDissTime() {
        return sendDissTime;
    }

    /**
     * 发送玩家状态信息
     */
    public void sendPlayerStatusMsg() {
        for (Entry<Long, Player> kv : getPlayerMap().entrySet()) {
            broadIsOnlineMsg(kv.getValue(), kv.getValue().getIsOnline() == 0 ? SharedConstants.table_offline : SharedConstants.table_online);
        }
    }

    public String getPlayerNameString() {
        StringBuilder names = new StringBuilder();
        Player master;
        if (masterId > 0 && (master = getPlayerMap().get(masterId)) != null) {
            names.append(",").append(master.getName());
            for (Player player : getPlayerMap().values()) {
                if (player.getUserId() != masterId) {
                    names.append(",").append(player.getName());
                }
            }
        } else {
            for (Player player : getPlayerMap().values()) {
                names.append(",").append(player.getName());
            }
        }

        if (names.length() > 0) {
            return names.substring(1);
        } else {
            return "";
        }
    }

    /**
     * 获得房间解散的状态（2：正常结束解散 3：未开局被解散 4：中途解散）
     */
    public String getDissCurrentState() {
        String currentState;
        if (playedBureau == 0) {
            currentState = "3";
        } else if (isCommonOver()) {
            currentState = "2";
        } else {
            currentState = "4";
        }
        return currentState;
    }

    /**
     * 房间是否正常打完结束
     */
    public boolean isCommonOver() {
        return playedBureau == totalBureau || !tiqianDiss;
    }

    /**
     * 房间是否正常打完结束
     */
    public boolean isNormalOver() {
        return playedBureau == totalBureau && !tiqianDiss;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public boolean joinWaitNext() {
        return true;
    }

    /**
     * 广播玩家在线状态消息
     */
    public void broadOnlineStateMsg() {
        for (Player player1 : getPlayerMap().values()) {
            if (player1.getIsOnline() == 0) {
                broadIsOnlineMsg(player1, SharedConstants.table_offline);
            } else {
                broadIsOnlineMsg(player1, SharedConstants.table_online);
            }
        }
    }

    /**
     * 所有人可以开始游戏
     */
    public boolean anyOneStart() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("anyOneStart", ""));
    }


    /**
     * 是否可以退出
     *
     * @param player
     * @return
     */
    public boolean canQuit(Player player) {
        if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取牌桌正常结束条件的最终值
     *
     * @return
     */
    public int loadOverValue() {
        return totalBureau;
    }

    /**
     * 获取牌桌正常结束条件的当前值
     *
     * @return
     */
    public int loadOverCurrentValue() {
        return playedBureau;
    }

    /**
     * 签名key
     *
     * @return
     */
    public String loadSignKey() {
        return LoginUtil.DEFAULT_KEY;
    }

    /**
     * 加载玩法配置，子类必须重写
     * todo
     */
    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
    }

//    public int getStartType(){
//        return 0;
//    }

    public int getCreditMode() {
        return creditMode;
    }

    public void setCreditMode(int creditMode) {
        this.creditMode = creditMode;
    }

    public int getCreditJoinLimit() {
        return creditJoinLimit;
    }

    public void setCreditJoinLimit(int creditJoinLimit) {
        this.creditJoinLimit = creditJoinLimit;
    }

    public int getCreditDissLimit() {
        return creditDissLimit;
    }

    public void setCreditDissLimit(int creditDissLimit) {
        this.creditDissLimit = creditDissLimit;
    }

    public int getCreditDifen() {
        return creditDifen;
    }

    public void setCreditDifen(int creditDifen) {
        this.creditDifen = creditDifen;
    }

    public int getCreditCommission() {
        return creditCommission;
    }

    public void setCreditCommission(int creditCommission) {
        this.creditCommission = creditCommission;
    }

    public int getCreditCommissionMode1() {
        return creditCommissionMode1;
    }

    public void setCreditCommissionMode1(int creditCommissionMode1) {
        this.creditCommissionMode1 = creditCommissionMode1;
    }

    public int getCreditCommissionMode2() {
        return creditCommissionMode2;
    }

    public void setCreditCommissionMode2(int creditCommissionMode2) {
        this.creditCommissionMode2 = creditCommissionMode2;
    }

    /**
     * 是否是信用房间
     *
     * @return
     */
    public boolean isCreditTable() {
        return isGroupRoom() && creditMode == 1;
    }

    /**
     * 是否是信用房间
     * ---根据玩法配置数据判断，各玩法自行实现
     *
     * @param params
     * @return
     */
    public boolean isCreditTable(List<Integer> params) {
        return false;
    }

    /**
     * 计算信用分,
     * 调用此接口的前提是：player.winLoseCredit和player.commissionCredit 已经计算出来
     */
    public void calcCredit() {
        if (!isCreditTable()) {
            return;
        }

        String groupId = loadGroupId();
        int totalCommissionCredit = 0;
        List<Player> dyjPlayers = new ArrayList<>();
        int dyjCredit = 0;

        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() == 0) {
                continue;
            }
            int updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), player.getWinLoseCredit());

            HashMap<String, Object> creditLog = new HashMap<>();
            creditLog.put("groupId", groupId);
            creditLog.put("userId", player.getUserId());
            creditLog.put("optUserId", 0);
            creditLog.put("tableId", getId());
            creditLog.put("credit", player.getWinLoseCredit());
            creditLog.put("type", Constants.CREDIT_LOG_TYPE_TABLE);
            creditLog.put("flag", updateResult);
            GroupDao.getInstance().insertGroupCreditLog(creditLog);

            totalCommissionCredit += player.getCommissionCredit();
            if (player.getWinLoseCredit() > dyjCredit) {
                dyjCredit = player.getWinLoseCredit();
                dyjPlayers.clear();
                dyjPlayers.add(player);
            } else if (player.getWinLoseCredit() == dyjCredit) {
                dyjPlayers.add(player);
            }
        }
        if (dyjCredit <= 0) {
            dyjPlayers.clear();
        }

        Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));

        long masterId = 0;
        try {
            GroupUser groupMaster = GroupDao.getInstance().loadGroupMaster(groupId);
            if (groupMaster != null) {
                masterId = groupMaster.getUserId();
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("loadGroupMaster|error|" + getId() + "|" + groupId, e);
        }

        Map<Integer, Integer> rateMap = new HashMap<>();
        Map<Integer, Long> leaderIdMap = new HashMap<>();
        if (totalCommissionCredit > 0) {
            for (Player player : getSeatMap().values()) {
                if (player.getCommissionCredit() <= 0) {
                    continue;
                }
                int commissionCredit = player.getCommissionCredit();
                long teamLeaderId = 0;
                int teamLeaderRate = 0;
                GroupUser groupUser = player.loadGroupUser(groupId);
                if (groupUser.getUserGroup() > 0) {
                    if (leaderIdMap.containsKey(groupUser.getUserGroup())) {
                        teamLeaderId = leaderIdMap.get(groupUser.getUserGroup());
                        teamLeaderRate = rateMap.get(groupUser.getUserGroup());
                    } else {
                        GroupRelation groupRelation = GroupDao.getInstance().getGroupRelation(groupUser.getUserGroup() + "");
                        if (groupRelation != null && groupRelation.getCreditCommissionRate() > 0) {
                            //小组长分成
                            teamLeaderRate = groupRelation.getCreditCommissionRate();
                            GroupUser teamLeader = GroupDao.getInstance().loadGroupTeamLeader(groupId, groupUser.getUserGroup() + "");
                            if (teamLeader != null) {
                                teamLeaderId = teamLeader.getUserId();
                                rateMap.put(groupUser.getUserGroup(), teamLeaderRate);
                                leaderIdMap.put(groupUser.getUserGroup(), teamLeaderId);
                            }
                        } else {
                            rateMap.put(groupUser.getUserGroup(), 0);
                            leaderIdMap.put(groupUser.getUserGroup(), 0l);
                        }
                    }
                }
                int masterCredit = commissionCredit;
                int teamLeaderCredit = 0;
                if (teamLeaderId > 0 && teamLeaderRate > 0) {
                    //小组长要分成,先算出群主的分数
                    masterCredit = new Double(Math.floor(commissionCredit * (100 - teamLeaderRate) / 100d)).intValue();
                    teamLeaderCredit = commissionCredit - masterCredit;
                }

                if (masterCredit > 0) {
                    int updateResult = updateGroupCredit(groupId, masterId, -1, masterCredit);
                    HashMap<String, Object> creditLog = new HashMap<>();
                    creditLog.put("groupId", groupId);
                    creditLog.put("optUserId", player.getUserId());
                    creditLog.put("userId", masterId);
                    creditLog.put("credit", masterCredit);
                    creditLog.put("type", Constants.CREDIT_LOG_TYPE_COMMSION);
                    creditLog.put("flag", updateResult);
                    creditLog.put("tableId", getId());
                    creditLog.put("userGroup", groupUser.getUserGroup());
                    GroupDao.getInstance().insertGroupCreditLog(creditLog);
                }

                if (teamLeaderCredit > 0) {
                    int updateResult = updateGroupCredit(groupId, teamLeaderId, -1, teamLeaderCredit);
                    HashMap<String, Object> creditLog = new HashMap<>();
                    creditLog.put("groupId", groupId);
                    creditLog.put("optUserId", player.getUserId());
                    creditLog.put("userId", teamLeaderId);
                    creditLog.put("credit", teamLeaderCredit);
                    creditLog.put("type", Constants.CREDIT_LOG_TYPE_COMMSION);
                    creditLog.put("flag", updateResult);
                    creditLog.put("tableId", getId());
                    creditLog.put("userGroup", groupUser.getUserGroup());
                    GroupDao.getInstance().insertGroupCreditLog(creditLog);
                }

                //信用分贡献榜
                DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", player.getCommissionCredit());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
            }
        }

        //信用分大赢家
        for (Player player : dyjPlayers) {
            DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCountCredit", 1);
            DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
        }

    }

    /**
     * 更新玩家信用分
     *
     * @param groupId
     * @param userId
     * @param seat
     * @param credit
     * @return
     */
    public int updateGroupCredit(String groupId, long userId, int seat, int credit) {
        int updateResult = 0;
        try {
            updateResult = GroupDao.getInstance().updateGroupCredit(groupId, userId, credit);
        } catch (Exception e) {
            LogUtil.errorLog.error("updateGroupCredit|error|2|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit, e);
        }
        if (updateResult == 0) {
            LogUtil.errorLog.error("updateGroupCredit|error|3|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
        } else {
            LogUtil.msgLog.info("updateGroupCredit|succ|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
        }
        return updateResult;
    }

    /**
     * 计算佣金
     *
     * @param player
     * @param dyjCredit 大赢家
     */
    public void calcCommissionCredit(Player player, int dyjCredit) {

        player.setCommissionCredit(0);
        int credit = player.getWinLoseCredit();
        if(credit <= 0 ){
            return;
        }
        if (creditCommissionMode2 == 1 && credit < dyjCredit) {
            return;
        }
        isBaoDiCommission = false;
        if (player.getWinLoseCredit() <= creditCommissionLimit) {
            if(creditCommissionBaoDi <= 0 ){
                return;
            }
            int baoDiCommission = credit > creditCommissionBaoDi ? creditCommissionBaoDi : credit;
            credit = credit > creditCommissionBaoDi ? credit - creditCommissionBaoDi : 0;
            player.setCommissionCredit(baoDiCommission);
            player.setWinLoseCredit(credit);
            isBaoDiCommission = true;
            return;
        }
        //佣金
        if (creditCommissionMode1 == 1) {
            //固定数量佣金
            if (creditCommissionMode2 == 1) {
                //大赢家
                if (credit >= dyjCredit && dyjCredit > 0) {
                    credit -= creditCommission;
                    player.setCommissionCredit(creditCommission);
                }
            } else {
                //全部赢家
                if (credit > 0) {
                    credit -= creditCommission;
                    player.setCommissionCredit(creditCommission);
                }
            }
        } else {
            //按比例交佣金
            if (creditCommissionMode2 == 1) {
                //大赢家
                if (credit >= dyjCredit && dyjCredit > 0) {
                    int commission = credit * creditCommission / 100;
                    credit -= commission;
                    player.setCommissionCredit(commission);
                }
            } else {
                //全部赢家
                if (credit > 0) {
                    int commission = credit * creditCommission / 100;
                    credit -= commission;
                    player.setCommissionCredit(commission);
                }
            }
        }
        player.setWinLoseCredit(credit);
    }

    /**
     * 发牌前检查玩家身上信用分是否满足最低信用分需求
     *
     * @return
     */
    public synchronized boolean checkCredit() {
        if (!isCreditTable()) {
            return true;
        }
        try {
            String dissPlayerNames = "";
            for (Player player : getSeatMap().values()) {
                String groupId = loadGroupId();
                GroupUser gu = player.loadGroupUser(groupId);
                if (gu == null || gu.getCredit() + player.getWinLoseCredit() < creditDissLimit) {
                    dissPlayerNames += player.getName() + ",";
                }
            }
            if (!dissPlayerNames.equals("")) {
                dissPlayerNames = dissPlayerNames.substring(0, dissPlayerNames.length() - 1);
                for (Player player : getSeatMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, dissPlayerNames, creditDissLimit);
                }
                for (Player player : getRoomPlayerMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, dissPlayerNames, creditDissLimit);
                }
                try {
                    sendAccountsMsg();
                } catch (Throwable e) {
                    LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                    for (Player player : getPlayerMap().values()) {
                        player.writeSocket(errorMsg);
                    }
                }
                setSpecialDiss(1);
                setTiqianDiss(true);
                calcCreditNew();
                diss();
                return false;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return true;
    }


    /**
     * 计算出现负分的情况：以输家可以输的总分，分给赢家（从赢分最大的开始，依次来分发）
     * 各子类按实现业务需求重写
     */
    public void calcNegativeCredit() {
        if (!isCreditTable()) {
            return;
        }
        calcWinCreditLimit();

        String groupId = loadGroupId();
        //计算负分
        int totalHave = 0;
        List<Player> winList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            GroupUser gu = player.loadGroupUser(groupId);
            int haveCredit = gu.getCredit();
            if (player.getWinLoseCredit() < 0) {
                if (haveCredit <= 0) {
                    totalHave += 0;
                    player.setWinLoseCredit(0);
                } else if (haveCredit + player.getWinLoseCredit() < 0) {
                    totalHave += haveCredit;
                    player.setWinLoseCredit(-haveCredit);
                } else {
                    totalHave += -player.getWinLoseCredit();
                }
            } else {
                winList.add(player);
            }
        }

        if (winList.size() == 0) {
            return;
        }
        // 赢分从大到小
        Collections.sort(winList, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return o2.getWinLoseCredit() - o1.getWinLoseCredit();
            }
        });

        for (Player player : winList) {
            if (player.getWinLoseCredit() < totalHave) {
                totalHave -= player.getWinLoseCredit();
            } else {
                player.setWinLoseCredit(totalHave);
                totalHave = 0;
            }
        }
    }

    /**
     * 比赛房赢分不能超过玩家本身进房间所带的比赛分
     */
    public void calcWinCreditLimit() {
        if (!isCreditTable()) {
            return;
        }
        String groupId = loadGroupId();
        int totalWin = 0;// 赢分的玩家总分
        int totalLose = 0;// 输分玩家的总分
        int losePlayerCount = 0; // 输分玩家数
        int maxLoseCredit = 0;  // 最大输分数

        List<Player> loseList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() > 0) {
                GroupUser gu = player.loadGroupUser(groupId);
                int haveCredit = gu.getCredit();
                if (player.getWinLoseCredit() > haveCredit) {
                    // 比赛房赢分不能超过玩家本身进房间所带的比赛分
                    player.setWinLoseCredit(haveCredit);
                }
                totalWin += player.getWinLoseCredit();
            } else {
                totalLose += player.getWinLoseCredit();
                losePlayerCount++;
                maxLoseCredit = player.getWinLoseCredit() < maxLoseCredit ? player.getWinLoseCredit() : maxLoseCredit;
                loseList.add(player);
            }
        }

        //赢的总分与输的总分不一至时 退还输分玩家多输的分数
        if (Math.abs(totalLose) > totalWin) {
            // 输分从小到大
            Collections.sort(loseList, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return o2.getWinLoseCredit() - o1.getWinLoseCredit();
                }
            });
//            int credit = (Math.abs(totalLose) - totalWin) / losePlayerCount;
//            int leftCredit = (Math.abs(totalLose) - totalWin) % losePlayerCount;
//            for (Player player : loseList) {
//                if (player.getWinLoseCredit() < 0) {
//                    if (leftCredit > 0 && player.getWinLoseCredit() == maxLoseCredit) {
//                        player.setWinLoseCredit(player.getWinLoseCredit() + leftCredit);
//                        leftCredit = 0;
//                    }
//                    player.setWinLoseCredit(player.getWinLoseCredit() + credit);
//                }
//            }

            int credit = totalWin / losePlayerCount;
            int leftCredit = totalWin % losePlayerCount;
            // 即若一个输家就都是退给他，如果是两个输家就相当于用赢分平均分摊给两个输家，前提为输家本身输的分>均摊分，若小于则只输本身输分，其他都退给另外一个输家
            for (Player player : loseList) {
                if (Math.abs(player.getWinLoseCredit()) < credit) {
                    totalWin -= Math.abs(player.getWinLoseCredit());
                    losePlayerCount--;
                    credit = totalWin / losePlayerCount;
                    leftCredit = totalWin % losePlayerCount;
                } else {
                    player.setWinLoseCredit(-credit);
                }
            }
            if (leftCredit > 0) {
                Player maxLoser = loseList.get(loseList.size() - 1);
                maxLoser.setWinLoseCredit(maxLoser.getWinLoseCredit() - leftCredit);
            }
        }
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }


    public long saveUserGroupPlaylog() {
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (Player player : getSeatMap().values()) {
            players += player.getUserId() + ",";
            score += player.getTotalPoint() + ",";
            diFenScore += player.getTotalPoint() + ",";
        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(getMaxPlayerCount());
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    public String getGameName() {
        return "";
    }

    /**
     * 广播消息给所有人
     */
    public void broadMsgToAll(GeneratedMessage message) {
        broadMsg(message, 0);
        broadMsgRoomPlayer(message);
    }

    /**
     * 广播消息
     */
    public void broadMsg(GeneratedMessage message, long userId) {
        for (Player player : getPlayerMap().values()) {
            if (player.getIsOnline() == 0 || userId == player.getUserId()) {
                continue;
            }
            player.writeSocket(message);
        }
    }

    /**
     * 广播消息给旁观者
     */
    public void broadMsgRoomPlayer(GeneratedMessage message) {
        for (Player player : roomPlayerMap.values()) {
            if (player.getIsOnline() == 0) {
                continue;
            }
            player.writeSocket(message);
        }
    }

    /**
     * 默认加入房间不分配位置
     * @return
     */
    public boolean isJoinPlayerAllotSeat(){
        return false;
    }

    /**
     * 计算信用分,
     */
    public void calcCreditNewOld() {
        if (!isCreditTable()) {
            return;
        }

        String groupId = loadGroupId();
        int totalCommissionCredit = 0;
        List<Player> dyjPlayers = new ArrayList<>();
        int dyjCredit = 0;

        // 保存玩家牌桌内输赢的信用分
        Map<Long, GroupUser> guMap = new HashMap<>();
        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() == 0) {
                continue;
            }
            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
            if (groupUser == null) {
                continue;
            }
            guMap.put(player.getUserId(), groupUser);
            int updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), player.getWinLoseCredit());
            HashMap<String, Object> log = new HashMap<>();
            log.put("groupId", groupId);
            log.put("userId", player.getUserId());
            log.put("optUserId", 0);
            log.put("tableId", getId());
            log.put("credit", player.getWinLoseCredit());
            log.put("type", Constants.CREDIT_LOG_TYPE_TABLE);
            log.put("flag", updateResult);
            log.put("userGroup", groupUser.getUserGroup());
            log.put("promoterId1", groupUser.getPromoterId1());
            log.put("promoterId2", groupUser.getPromoterId2());
            log.put("promoterId3", groupUser.getPromoterId3());
            log.put("promoterId4", groupUser.getPromoterId4());
            GroupDao.getInstance().insertGroupCreditLog(log);

            totalCommissionCredit += player.getCommissionCredit();
            if (player.getWinLoseCredit() > dyjCredit) {
                dyjCredit = player.getWinLoseCredit();
                dyjPlayers.clear();
                dyjPlayers.add(player);
            } else if (dyjCredit > 0 && player.getWinLoseCredit() == dyjCredit) {
                dyjPlayers.add(player);
            }
        }
        Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        for (Player player : dyjPlayers) {
            //信用分大赢家
            DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCountCredit", 1);
            DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
        }

        if (totalCommissionCredit <= 0) {
            return;
        }

        // 信用分分成
        try {
            int creditAllotMode = 1;
            long configId = 0;

            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.valueOf(groupId), 0);
            if (groupInfo != null) {
                creditAllotMode = groupInfo.getCreditAllotMode();
            }
            long masterId = 0;
            GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
            if (master != null) {
                masterId = master.getUserId();
            }

            GroupTable groupTable = getGroupTable();
            if (groupTable == null) {
                groupTable = GroupDao.getInstance().loadGroupTableByKeyId(loadGroupTableKeyId());
                setGroupTable(groupTable);
            }
            if (groupTable != null) {
                configId = groupTable.getConfigId();
            }

            List<CreditCommission> commList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                int commissionCredit = player.getCommissionCredit();
                if (commissionCredit <= 0) {
                    continue;
                }
                if (creditAllotMode == 1) { // 大赢家分成
                    GroupUser groupUser = guMap.get(player.getUserId());
                    if (groupUser == null) {
                        groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                        if (groupUser == null) {
                            continue;
                        }
                    }
                    commList.addAll(calcCommissionOld(groupUser, masterId, commissionCredit, configId));
                } else { // 参与分成
                    int tmp = commissionCredit % getPlayerCount();
                    if (tmp > 0) { // 除不尽的分给群主
                        GroupUser groupUser = guMap.get(player.getUserId());
                        if (groupUser == null) {
                            groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                            if (groupUser == null) {
                                continue;
                            }
                        }
                        commList.add(new CreditCommission(groupUser, masterId, tmp));
                    }
                    commissionCredit = commissionCredit / getPlayerCount();
                    for (Player player0 : getSeatMap().values()) {
                        GroupUser groupUser = guMap.get(player0.getUserId());
                        if (groupUser == null) {
                            groupUser = GroupDao.getInstance().loadGroupUser(player0.getUserId(), groupId);
                            if (groupUser == null) {
                                continue;
                            }
                        }
                        commList.addAll(calcCommissionOld(groupUser, masterId, commissionCredit, configId));
                    }
                }

                //信用分贡献榜
                DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", player.getCommissionCredit());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
            }
            for (CreditCommission comm : commList) {
                int updateResult = updateGroupCredit(String.valueOf(groupId), comm.getDestUserId(), -1, comm.getCredit());
                HashMap<String, Object> log = new HashMap<>();
                GroupUser groupUser = comm.getGroupUser();
                log.put("groupId", groupId);
                log.put("optUserId", groupUser.getUserId());
                log.put("userId", comm.getDestUserId());
                log.put("credit", comm.getCredit());
                log.put("type", Constants.CREDIT_LOG_TYPE_COMMSION);
                log.put("flag", updateResult);
                log.put("tableId", getId());
                log.put("userGroup", groupUser.getUserGroup());
                log.put("promoterId1", groupUser.getPromoterId1());
                log.put("promoterId2", groupUser.getPromoterId2());
                log.put("promoterId3", groupUser.getPromoterId3());
                log.put("promoterId4", groupUser.getPromoterId4());
                GroupDao.getInstance().insertGroupCreditLog(log);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("loadGroupMaster|error|" + getId() + "|" + groupId, e);
        }
    }

    /**
     * 计算信用分分成
     *
     * @param groupUser
     * @param commissionCredit
     * @param configId
     * @return
     * @throws Exception
     */
    private List<CreditCommission> calcCommissionOld(GroupUser groupUser, long masterId, int commissionCredit, long configId) throws Exception {

        List<CreditCommission> commList = new ArrayList<>();
        if (groupUser == null) {
            return commList;
        }
        String userGroup = groupUser.getUserGroup().toString();
        String groupId = groupUser.getGroupId().toString();
        int preCredit = commissionCredit; // 上级给自己分
        int configCredit;// 给下级配置的分
        long userId;     // 自己
        long nextUserId; // 下级
        int leftCredit;  // 自己留下的分

        //--------------------------------- 群主 ---------------------------------
        userId = masterId;
        if ("0".equals(userGroup)) {
            // 非小组成员，全部给群主
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        GroupUser teamLeader = GroupDao.getInstance().loadGroupTeamLeader(groupId, userGroup);
        if (teamLeader == null) {
            return commList;
        }
        nextUserId = teamLeader.getUserId();
        // 给小组长的分
        Integer masterConfigCredit = GroupCreditDao.loadConfigCreditValue(groupId, userId, nextUserId, configId);
        if (masterConfigCredit == null) {
            // 群主未设置,使用原来设置的比例分成
            GroupRelation groupRelation = GroupDao.getInstance().getGroupRelation(groupUser.getUserGroup() + "");
            if (groupRelation == null || groupRelation.getCreditCommissionRate() <= 0) {
                // 给群主
                commList.add(new CreditCommission(groupUser, userId, preCredit));
                return commList;
            }
            int teamLeaderRate = groupRelation.getCreditCommissionRate();
            //小组长要分成,先算出群主的分数
            int masterCredit = new Double(Math.floor(preCredit * (100 - teamLeaderRate) / 100d)).intValue();
            int teamLeaderCredit = preCredit - masterCredit;
            if (masterCredit > 0) {
                // 给群主
                commList.add(new CreditCommission(groupUser, userId, masterCredit));
            }
            if (teamLeaderCredit <= 0) {
                // 小组长分小于等于0，直接返回
                return commList;
            } else {
                // 小组长的分大于0，继续往下分成
                preCredit = teamLeaderCredit;
                configCredit = preCredit;
            }
        } else {
            configCredit = masterConfigCredit;
        }

        if (configCredit <= 0) {
            // 没有往下分
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }

        leftCredit = preCredit - configCredit;
        if (leftCredit > 0) {
            commList.add(new CreditCommission(groupUser, userId, leftCredit));
            preCredit = configCredit;
        }

        //--------------------------------- 小组长 ---------------------------------
        userId = teamLeader.getUserId();
        nextUserId = groupUser.getPromoterId1();
        if (nextUserId == 0) {
            // 没有下级，全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        // 给一级拉手的分
        configCredit = GroupCreditDao.getConfigCreditValue(groupId, userId, nextUserId, configId);
        if (configCredit <= 0) {
            // 没有往下分
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        leftCredit = preCredit - configCredit;
        if (leftCredit > 0) {
            commList.add(new CreditCommission(groupUser, userId, leftCredit));
            preCredit = configCredit;
        }

        //--------------------------------- 一级拉手 ---------------------------------
        userId = groupUser.getPromoterId1();
        nextUserId = groupUser.getPromoterId2();
        if (nextUserId == 0) {
            // 没有下级，全部给一级拉手
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        // 给二级拉手的分
        configCredit = GroupCreditDao.getConfigCreditValue(groupId, userId, nextUserId, configId);
        if (configCredit <= 0) {
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        leftCredit = preCredit - configCredit;
        if (leftCredit > 0) {
            commList.add(new CreditCommission(groupUser, userId, leftCredit));
            preCredit = configCredit;
        }

        //--------------------------------- 二级拉手 ---------------------------------
        userId = groupUser.getPromoterId2();
        nextUserId = groupUser.getPromoterId3();
        if (nextUserId == 0) {
            // 没有下级，全部给二级拉手
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        // 给三级拉手的分
        configCredit = GroupCreditDao.getConfigCreditValue(groupId, userId, nextUserId, configId);
        if (configCredit <= 0) {
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        leftCredit = preCredit - configCredit;
        if (leftCredit > 0) {
            commList.add(new CreditCommission(groupUser, userId, leftCredit));
            preCredit = configCredit;
        }

        //--------------------------------- 三级拉手 ---------------------------------
        userId = groupUser.getPromoterId3();
        nextUserId = groupUser.getPromoterId4();
        if (nextUserId == 0) {
            // 没有下级，全部给三级拉手
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        // 给四级拉手的分
        configCredit = GroupCreditDao.getConfigCreditValue(groupId, userId, nextUserId, configId);
        if (configCredit <= 0) {
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        leftCredit = preCredit - configCredit;
        if (leftCredit > 0) {
            commList.add(new CreditCommission(groupUser, userId, leftCredit));
            preCredit = configCredit;
        }

        //--------------------------------- 四级拉手 ---------------------------------
        userId = groupUser.getPromoterId4();
        if (configCredit > preCredit) {
            commList.add(new CreditCommission(groupUser, userId, preCredit));
        } else {
            commList.add(new CreditCommission(groupUser, userId, configCredit));
        }

        return commList;
    }

    /**
     * 计算信用分,
     */
    public void calcCreditNew() {
        if (!isCreditTable()) {
            return;
        }

        String groupId = loadGroupId();
        int totalCommissionCredit = 0;
        List<Player> dyjPlayers = new ArrayList<>();
        int dyjCredit = 0;

        // 保存玩家牌桌内输赢的信用分
        Map<Long, GroupUser> guMap = new HashMap<>();
        for (Player player : getSeatMap().values()) {
            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
            if (groupUser == null) {
                continue;
            }
            guMap.put(player.getUserId(), groupUser);
            int updateResult = 0;
            if (player.getWinLoseCredit() != 0) {
                updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), player.getWinLoseCredit());
            }
            HashMap<String, Object> log = new HashMap<>();
            log.put("groupId", groupId);
            log.put("userId", player.getUserId());
            log.put("optUserId", player.getUserId());
            log.put("tableId", getId());
            log.put("credit", player.getWinLoseCredit());
            log.put("type", Constants.CREDIT_LOG_TYPE_TABLE);
            log.put("flag", updateResult);
            log.put("userGroup", groupUser.getUserGroup());
            log.put("promoterId1", groupUser.getPromoterId1());
            log.put("promoterId2", groupUser.getPromoterId2());
            log.put("promoterId3", groupUser.getPromoterId3());
            log.put("promoterId4", groupUser.getPromoterId4());
            log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
            GroupDao.getInstance().insertGroupCreditLog(log);

            totalCommissionCredit += player.getCommissionCredit();
            if (player.getWinLoseCredit() > dyjCredit) {
                dyjCredit = player.getWinLoseCredit();
                dyjPlayers.clear();
                dyjPlayers.add(player);
            } else if (dyjCredit > 0 && player.getWinLoseCredit() == dyjCredit) {
                dyjPlayers.add(player);
            }
        }
        Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        for (Player player : dyjPlayers) {
            //信用分大赢家
            DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCountCredit", 1);
            DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
        }

        if (totalCommissionCredit <= 0) {
            return;
        }

        // 信用分分成
        try {
            int creditAllotMode = 1;

            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.valueOf(groupId), 0);
            if (groupInfo != null) {
                creditAllotMode = groupInfo.getCreditAllotMode();
            }
            long masterId = 0;
            GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
            if (master != null) {
                masterId = master.getUserId();
            }

            List<CreditCommission> commList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                int commissionCredit = player.getCommissionCredit();
                if (commissionCredit <= 0) {
                    continue;
                }
                GroupUser groupUser = guMap.get(player.getUserId());
                if (groupUser == null) {
                    continue;
                }
                if(isBaoDiCommission){
                    // 保低抽，只给群主
                    commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
                }else {
                    if (creditAllotMode == 1) { // 大赢家分成
                        if (groupUser == null) {
                            groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                            if (groupUser == null) {
                                continue;
                            }
                        }
                        commList.addAll(calcCommissionNew(groupUser, masterId, commissionCredit));
                    } else { // 参与分成
                        int tmp = commissionCredit % getPlayerCount();
                        if (tmp > 0) { // 除不尽的分给群主
                            if (groupUser == null) {
                                groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                                if (groupUser == null) {
                                    continue;
                                }
                            }
                            commList.add(new CreditCommission(groupUser, masterId, tmp));
                        }
                        commissionCredit = commissionCredit / getPlayerCount();
                        for (Player player0 : getSeatMap().values()) {
                            GroupUser groupUser0 = guMap.get(player0.getUserId());
                            if (groupUser0 == null) {
                                groupUser0 = GroupDao.getInstance().loadGroupUser(player0.getUserId(), groupId);
                                if (groupUser0 == null) {
                                    continue;
                                }
                            }
                            commList.addAll(calcCommissionNew(groupUser0, masterId, commissionCredit));
                        }
                    }
                }

                //信用分贡献榜
                DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", player.getCommissionCredit());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
            }
            for (CreditCommission comm : commList) {
                int updateResult = updateGroupCredit(String.valueOf(groupId), comm.getDestUserId(), -1, comm.getCredit());
                HashMap<String, Object> log = new HashMap<>();
                GroupUser groupUser = comm.getGroupUser();
                log.put("groupId", groupId);
                log.put("optUserId", groupUser.getUserId());
                log.put("userId", comm.getDestUserId());
                log.put("credit", comm.getCredit());
                log.put("type", Constants.CREDIT_LOG_TYPE_COMMSION);
                log.put("flag", updateResult);
                log.put("tableId", getId());
                log.put("userGroup", groupUser.getUserGroup());
                log.put("promoterId1", groupUser.getPromoterId1());
                log.put("promoterId2", groupUser.getPromoterId2());
                log.put("promoterId3", groupUser.getPromoterId3());
                log.put("promoterId4", groupUser.getPromoterId4());
                log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
                GroupDao.getInstance().insertGroupCreditLog(log);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("loadGroupMaster|error|" + getId() + "|" + groupId, e);
        }
    }

    /**
     * 计算信用分分成
     *
     * @param groupUser
     * @param commissionCredit
     * @return
     * @throws Exception
     */
    private List<CreditCommission> calcCommissionNew(GroupUser groupUser, long masterId, int commissionCredit) throws Exception {

        List<CreditCommission> commList = new ArrayList<>();
        if (groupUser == null) {
            return commList;
        }
        String userGroup = groupUser.getUserGroup().toString();
        String groupId = groupUser.getGroupId().toString();
        int preCredit = commissionCredit; // 上级留下来的分数
        long userId;     // 本级
        long nextUserId; // 下级
        GroupUser nextUser; //下级
        int nowCredit;   // 本级分数
        int rate;        // 给下级的比例
        int preRate = 100;// 上级的比例

        //--------------------------------- 群主 ---------------------------------
        userId = masterId;
        if ("0".equals(userGroup)) {
            // 非小组成员，全部给群主
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        nextUser = GroupDao.getInstance().loadGroupTeamLeader(groupId, userGroup);
        if (nextUser == null) {
            // 给群主
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        if (nextUser.getCreditCommissionRate() <= 0) {
            // 取旧数据,t_group_relation.creditCommissionRate
//            GroupRelation groupRelation = GroupDao.getInstance().getGroupRelation(groupUser.getUserGroup() + "");
//            if (groupRelation == null || groupRelation.getCreditCommissionRate() <= 0) {
//                // 给群主
//                commList.add(new CreditCommission(groupUser, userId, preCredit));
//                return commList;
//            }
//            rate = groupRelation.getCreditCommissionRate();
//            nowCredit = new Double(Math.floor(preCredit * (100 - rate) / 100d)).intValue();
//            if (nowCredit > 0) {
//                // 给群主
//                commList.add(new CreditCommission(groupUser, userId, nowCredit));
//            }
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        } else {
            rate = nextUser.getCreditCommissionRate();
            if (rate < preRate) {
                //小组长要分成,先算出群主的分数
                nowCredit = new Double(Math.floor(commissionCredit * (preRate - rate) / 100d)).intValue();
                if (nowCredit > 0) {
                    // 给群主
                    commList.add(new CreditCommission(groupUser, userId, nowCredit));
                }
            } else {
                //群主不拿分，全部给下级
                nowCredit = 0;
            }
        }
        preRate = rate;
        preCredit = preCredit - nowCredit;
        if (preCredit <= 0) {
            // 无剩余分数直接返回
            return commList;
        }

        //--------------------------------- 小组长 ---------------------------------
        userId = nextUser.getUserId();
        nextUserId = groupUser.getPromoterId1();
        if (nextUserId <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null || nextUser.getCreditCommissionRate() <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        rate = nextUser.getCreditCommissionRate();
        if (rate < preRate) {
            nowCredit = new Double(Math.floor(commissionCredit * (preRate - rate) / 100d)).intValue();
            if (nowCredit > 0) {
                // 给小组长
                commList.add(new CreditCommission(groupUser, userId, nowCredit));
            }
        } else {
            nowCredit = 0;
        }
        preRate = rate;
        preCredit = preCredit - nowCredit;
        if (preCredit <= 0) {
            // 无剩余分数直接返回
            return commList;
        }

        //--------------------------------- 一级拉手 ---------------------------------
        userId = nextUser.getUserId();
        nextUserId = groupUser.getPromoterId2();
        if (nextUserId <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null || nextUser.getCreditCommissionRate() <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        rate = nextUser.getCreditCommissionRate();
        if (rate < preRate) {
            nowCredit = new Double(Math.floor(commissionCredit * (preRate - rate) / 100d)).intValue();
            if (nowCredit > 0) {
                // 给小组长
                commList.add(new CreditCommission(groupUser, userId, nowCredit));
            }
        } else {
            nowCredit = 0;
        }
        preRate = rate;
        preCredit = preCredit - nowCredit;
        if (preCredit <= 0) {
            // 无剩余分数直接返回
            return commList;
        }

        //--------------------------------- 二级拉手 ---------------------------------
        userId = nextUser.getUserId();
        nextUserId = groupUser.getPromoterId3();
        if (nextUserId <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null || nextUser.getCreditCommissionRate() <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        rate = nextUser.getCreditCommissionRate();
        if (rate < preRate) {
            nowCredit = new Double(Math.floor(commissionCredit * (preRate - rate) / 100d)).intValue();
            if (nowCredit > 0) {
                // 给小组长
                commList.add(new CreditCommission(groupUser, userId, nowCredit));
            }
        } else {
            nowCredit = 0;
        }
        preRate = rate;
        preCredit = preCredit - nowCredit;
        if (preCredit <= 0) {
            // 无剩余分数直接返回
            return commList;
        }

        //--------------------------------- 三级拉手 ---------------------------------
        userId = nextUser.getUserId();
        nextUserId = groupUser.getPromoterId4();
        if (nextUserId <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null || nextUser.getCreditCommissionRate() <= 0) {
            //全部给小组长
            commList.add(new CreditCommission(groupUser, userId, preCredit));
            return commList;
        }
        rate = nextUser.getCreditCommissionRate();
        if (rate < preRate) {
            nowCredit = new Double(Math.floor(commissionCredit * (preRate - rate) / 100d)).intValue();
            if (nowCredit > 0) {
                // 给小组长
                commList.add(new CreditCommission(groupUser, userId, nowCredit));
            }
        } else {
            nowCredit = 0;
        }
        preRate = rate;
        preCredit = preCredit - nowCredit;
        if (preCredit <= 0) {
            // 无剩余分数直接返回
            return commList;
        }

        //--------------------------------- 四级拉手 ---------------------------------
        userId = nextUser.getUserId();
        commList.add(new CreditCommission(groupUser, userId, preCredit));
        return commList;
    }

    public void initCreditMsg(String creditMsg) {
        if (StringUtils.isBlank(creditMsg)) {
            return;
        }
        String[] params = creditMsg.split(",");
        if (params.length < 8) {
            return;
        }
        this.creditMode = StringUtil.getIntValue(params, 0, 0);
        this.creditJoinLimit = StringUtil.getIntValue(params, 1, 0);
        this.creditDissLimit = StringUtil.getIntValue(params, 2, 0);
        this.creditDifen = StringUtil.getIntValue(params, 3, 0);
        this.creditCommission = StringUtil.getIntValue(params, 4, 0);
        this.creditCommissionMode1 = StringUtil.getIntValue(params, 5, 1);
        this.creditCommissionMode2 = StringUtil.getIntValue(params, 6, 1);
        this.creditCommissionLimit = StringUtil.getIntValue(params, 7, 100);
        this.creditCommissionBaoDi = StringUtil.getIntValue(params, 8, 0);
        changeExtend();
    }

    public void updateGroupTableDealCount(){
        if (isGroupRoom()) {
            int keyId = Integer.parseInt(loadGroupTableKeyId());
            GroupDao.getInstance().addGroupTableDealCount(keyId);
        }
    }

    public List<Integer> getIntParams() {
        return intParams;
    }

    public void setIntParams(List<Integer> intParams) {
        this.intParams = intParams;
    }

    public List<String> getStrParams() {
        return strParams;
    }

    public void setStrParams(List<String> strParams) {
        this.strParams = strParams;
    }

    public void setReplenishParams(Player player,List<Integer> intParams,List<String> strParams){

    }

    public synchronized void checkAutoQuit() {
        if(getPlayedBureau() > 0){
            return;
        }
        if (this.getState() != table_state.ready) {
            return;
        }
        if (!this.isGroupRoom()) {
            return;
        }
        for (Player player : getSeatMap().values()) {
            if (player.getState() != player_state.entry) {
                continue;
            }
            if ((System.currentTimeMillis() - player.getJoinTime())/1000 < SharedConstants.getAutoQuitTimeOut()) {
                continue;
            }
            boolean quit = this.quitPlayer(player);
            if (quit) {
                this.onPlayerQuitSuccess(player);
                // 修改代开房间玩家的信息记录
                this.updateDaikaiTablePlayer();
                // 修改room表
                this.updateRoomPlayers();
                player.writeErrMsg(LangMsg.code_66);
            }
        }
    }

    public void calcDataStatisticsBjd() {
        if (!isGroupRoom()) {
            return;
        }
        String groupId = loadGroupId();
        //白金岛有效局数活动
        try {
            boolean bjdNewerActivity = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_bjdNewerActivity", SharedConstants.SWITCH_DEFAULT_OFF));
            if (bjdNewerActivity && !GameUtil.isPlayDtz(getPlayType()) && isCommonOver()) {
                // 有效玩家人数
                Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                int validCount = 0;
                for (Player player : getPlayerMap().values()) {
                    GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                    if (groupUser == null) {
                        continue;
                    }
                    boolean isValid = false;
                    long bindedGroupId = GroupDao.getInstance().loadIsNewBjdBindGroup(player.getUserId());
                    if (bindedGroupId > 0) {
                        isValid = groupId.equals(String.valueOf(bindedGroupId));
                    } else {
                        DataStatistics data = DataStatisticsDao.getInstance().loadMaxWzjsOfUser(player.getUserId());
                        if (data == null || data.getDataValue() < 19) {
                            // 验证玩家是否是该俱乐部的新人
                            isValid = true;
                        } else if (data.getDataValue() >= 19 && groupId.equals(data.getDataCode())) {
                            isValid = true;
                            if (data.getDataValue() == 19) {
                                // 将自己写成绑定用户
                                GroupDao.getInstance().bindIsNewBjd(groupId, player.getUserId());
                            }
                        }
                    }
                    if (isValid) {
                        validCount++;
                        DataStatistics dataStatistics = new DataStatistics(dataDate, groupId, String.valueOf(player.getUserId()), "0", "wzjsCount", 1);
                        DataStatisticsDao.getInstance().saveOrUpdateDataStatisticsBjd(dataStatistics);
                    }
                }
                // 牌局中新玩家数量大于等于总人数的一半时该牌局算成有效局数，即2人有1名新玩家，3人有2名新玩家，4人有2名新玩家
                boolean isValid = validCount * 1f / getMaxPlayerCount() * 1f >= 0.5f;
                if (isValid) {
                    DataStatistics dataStatistics = new DataStatistics(dataDate, groupId, groupId, "0", "jlbwzjsCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatisticsBjd(dataStatistics);
                }
            }
        } catch (Exception e) {
            LogUtil.e("calcDataStatistics|bjd|error|" + groupId + "|" + getId(), e);
        }
    }


    public int getLogGroupTableBureau(){
        return totalBureau;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
        changeExtend();
    }

    public void initGroupConfig(long groupId) {
        try {
            GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId, 0);
            if(group != null && StringUtils.isNotBlank(group.getExtMsg())){
                JSONObject json = JSONObject.parseObject(group.getExtMsg());
                String chatStr  = json.getString("chat");
                if(StringUtils.isNotBlank(chatStr)){
                    this.chatConfig = Integer.valueOf(chatStr);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("initGroupConfig|error|" + e.getMessage(), e);
        }
    }


    /**
     * 是否已开局
     * @return
     */
    public boolean isPlaying() {
        return playedBureau > 0 || playBureau > 1 || state != table_state.ready;
    }

    public void autoReady(Player player){
        if(playBureau > 1){
            if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                return;
            }
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
            GeneratedMessage playerReadyMsg = com.build();
            this.ready(player);
            for (Player seatPlayer : getSeatMap().values()) {
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
                for (Player tableplayer : getSeatMap().values()) {
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


    public JsonWrapper buildGeneralExtForPlaylog() {
        JsonWrapper json = new JsonWrapper("");
        json.putString("roomName", getRoomName());
        return json;
    }

    /**
     * 第一局发牌前检查
     * 发牌前检查玩家身上信用分是否满足最低信用分需求
     *
     * @return
     */
    public synchronized boolean checkCreditOnTableStart() {

        if (playBureau != 1) {
            return true;
        }
        if (!isCreditTable()) {
            return true;
        }
        try {
            // 重新查询亲友圈用户数据
            String disPlayerNames = "";
            String groupIdStr = loadGroupId();
            StringBuilder sb = new StringBuilder("checkCreditOnTableStart");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(creditJoinLimit);
            sb.append("|");
            for (Player player : getSeatMap().values()) {
                GroupUser gu = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupIdStr);
                if (gu == null || gu.getCredit() < creditJoinLimit) {
                    disPlayerNames += player.getName() + ",";
                    sb.append(gu.getUserId()).append(",").append(gu.getCredit()).append(";");
                }
            }
            if (!"".equals(disPlayerNames)) {
                LogUtil.msgLog.info(sb.toString());
                disPlayerNames = disPlayerNames.substring(0, disPlayerNames.length() - 1);
                for (Player player : getSeatMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, creditJoinLimit);
                }
                for (Player player : getRoomPlayerMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, creditJoinLimit);
                }

                // 推送解散消息
                int groupId = Integer.parseInt(groupIdStr);
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, playType, groupId, playedBureau);
                GeneratedMessage msg = com.build();
                for (Player player : getSeatMap().values()) {
                    player.writeSocket(msg);
                }
                for (Player player : roomPlayerMap.values()) {
                    player.writeSocket(msg);
                }

                setSpecialDiss(1);
                setTiqianDiss(true);
                LogUtil.msgLog.info("BaseTable|dissReason|checkCreditOnTableStart|1|" + getId() + "|" + getPlayBureau() + "|" + disPlayerNames);
                diss();
                return false;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return true;
    }


    public boolean checkGroupWarn(Player player, String groupId){
        if ("0".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_warn_switch"))) {
            LogUtil.msgLog.info("验证预警开关未开启，可以进入游戏");
            return true;
        }

        long curUserId = player.getUserId();
        while (curUserId > 0){
            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(curUserId,groupId);
//            GroupUser groupUser = GroupDao.getInstance().loadGroupUserForceMaster(curUserId,groupId);

            if(groupUser == null){
                LogUtil.msgLog.info("没找到亲友圈角色信息");
                break;
            }
            Long promoterId = getGroupSuperiorUserId(groupUser);
            if(promoterId <= 0){
                LogUtil.msgLog.info("没找到亲友圈角色上级信息");
                break;
            }
            GroupUser superUser = GroupDao.getInstance().loadGroupUser(promoterId,groupId);
            if(superUser == null){
                LogUtil.msgLog.info("没找到亲友圈角色上级信息");
                break;
            }
            List<GroupWarn> groupWarnList = GroupWarnDao.getInstance().getGroupWarnByUserIdAndGroupId(curUserId,Long.parseLong(groupId));
            GroupWarn gwarn = null;
            if(groupWarnList != null && groupWarnList.size() > 0){
                gwarn = groupWarnList.get(0);
            }
            if(gwarn != null && gwarn.getWarnSwitch() == 1){
                //查团队分
//                List<Map<String, Object>> groupWarnScores = GroupWarnDao.getInstance().selectGroupWarn(Long.parseLong(groupId), superUser.getPromoterLevel(), superUser.getUserId(), curUserId+"", 1, 10);
                List<Map<String, Object>> groupWarnScores = getGroupWarnList(superUser,Long.parseLong(groupId),  curUserId+"", 1, 10);
                if(groupWarnScores != null && groupWarnScores.size() > 0 ){
                    Map<String, Object> scoreMap = groupWarnScores.get(0);
                    if(scoreMap != null){
                        float sumCredit  = Long.parseLong(scoreMap.get("sumCredit").toString());
                        long warnScore   = Long.parseLong(scoreMap.get("warnScore").toString());
                        if(sumCredit< warnScore){
                            player.writeErrMsg(LangMsg.code_911,1);
                            return false;
                        }
                    }else{
                        player.writeErrMsg(LangMsg.code_911,2);
                        return false;
                    }
                }
            }
            Long spPromoterId = getGroupSuperiorUserId(superUser);
            if(superUser != null && spPromoterId > 0){
                curUserId = superUser.getUserId();
            }else{
                curUserId = 0;
            }

        }
        LogUtil.msgLog.info("验证通过，可以进入游戏");
        return true;
    }
    private List<Map<String, Object>> getGroupWarnList(GroupUser groupUser,long groupId,  String keyWord, int pageNo, int pageSize){
        try {
            if(groupUser.getUserRole() == 0 || groupUser.getUserRole()==1){
                return  GroupWarnDao.getInstance().selectGroupWarnListForMaster(groupId, keyWord, pageNo, pageSize);
            }else if (groupUser.getUserRole() == 10) {
                return  GroupWarnDao.getInstance().selectGroupWarnListForTeamLeader(groupId,groupUser.getUserGroup()+"", keyWord, pageNo, pageSize);
            }else if(groupUser.getUserRole() == 20){
                return  GroupWarnDao.getInstance().selectGroupWarnListForPromoter(groupId,groupUser.getUserGroup()+"",groupUser.getPromoterLevel(), keyWord, pageNo, pageSize);
            }
        }catch (Exception e) {
            LogUtil.errorLog.info("getGroupWarnList|error|" + groupUser.getUserId()+"|"+groupId+"|"+keyWord, e.getMessage(), e);
        }
        return new ArrayList<Map<String, Object>>();
    }

    /**
     * 获取亲友圈成员上级角色ID
     * @param groupUser
     * @return
     */
    public Long getGroupSuperiorUserId(GroupUser groupUser){

        if (groupUser.getUserRole() == 0  || groupUser.getUserRole() == 1) {
            return 0L;
        } else if(groupUser.getUserRole() == 10){
            try{
                GroupUser master = GroupDao.getInstance().loadGroupMaster(groupUser.getGroupId()+"");
                if (master != null) {
                    return master.getUserId();
                }
            }catch (Exception e) {
                LogUtil.errorLog.info("loadGroupMaster|error|" + groupUser.getUserId()+"|"+groupUser.getGroupId(), e.getMessage(), e);
            }

        } else{
            if(groupUser.getPromoterLevel() == 1){
                GroupUser teamLeader = GroupDao.getInstance().loadGroupTeamLeader(groupUser.getGroupId()+"", groupUser.getUserGroup() + "");
                if(teamLeader != null){
                    return teamLeader.getUserId();
                }else{
                    return 0L;
                }
            }else if (groupUser.getPromoterLevel() == 2) {
                return groupUser.getPromoterId1();
            } else if (groupUser.getPromoterLevel() == 3) {
                return groupUser.getPromoterId2();
            } else if (groupUser.getPromoterLevel() == 4) {
                return groupUser.getPromoterId3();
            } else if (groupUser.getPromoterLevel() == 5) {
                return groupUser.getPromoterId4();
            }
        }
        return 0L;
    }



}
