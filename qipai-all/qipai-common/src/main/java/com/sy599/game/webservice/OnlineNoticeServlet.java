package com.sy599.game.webservice;

import com.google.protobuf.GeneratedMessage;
import com.sy.general.GeneralHelper;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.ChatMessageDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineNoticeServlet extends HttpServlet {

    private final static String APP_KEY = "qweh#$*(_~)lpslot;589*/-+.-8&^%$#@!";

    private static final long serialVersionUID = 1L;

    public OnlineNoticeServlet() {
    }

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,String> params = UrlParamUtil.getParameters(request);
        String type = params.get("type");
        String userId = params.get("userId");
        String message = params.get("message");
        String timestamp = params.get("timestamp");
        String sign = params.get("sign");

        String ip = IpUtil.getIpAddr(request);

        LogUtil.msgLog.info("ip={},type={},userId={},message={},timestamp={},sign={}" ,ip,type,userId,message,timestamp,sign);
        try {
            if (IpUtil.isIntranet(ip) || "127.0.0.1".equals(ip) || (NumberUtils.isDigits(timestamp) && (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) <= 5 * 60 * 1000) && MD5Util.getMD5String(APP_KEY + type + userId + message + timestamp).equalsIgnoreCase(sign))) {
                if ("playerIsGroup".equals(type) && NumberUtils.isDigits(userId) && isDigits(message)) {
                    Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                    if (player != null) {
                    	int isGroup = Integer.parseInt(message);
                        player.setIsGroup(isGroup);
                        if(isGroup == 1){
                        	GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userId),null);
                        	player.setGroupUser(groupUser);
                        }else{
                        	player.setGroupUser(null);
                        }
                        writeMsg(response, "1");
                    } else {
                        writeMsg(response, "0");
                    }

                    return;
                } else if ("agencyDissRoom".equals(type)) {
                    String roomId = params.get("roomId");//房间id
                    String agencyId = params.get("agencyId");//代理id
                    String role = params.get("role");//代理权限
                    String serverId = params.get("serverId");//服id
                    if (String.valueOf(GameServerConfig.SERVER_ID).equals(serverId)) {
                        BaseTable table = TableManager.getInstance().getTable(Long.parseLong(roomId));
                        if (table != null) {
                            int playType = table.getPlayType();
                            boolean canDiss = true;
                            List<Player> players = new ArrayList<>(table.getPlayerMap().values());
                            List<Long> idList = new ArrayList<>();
                            for (Player player : players) {
                                if (!String.valueOf(player.getPayBindId()).equals(agencyId)) {
                                    canDiss = false;
                                }
                                idList.add(player.getUserId());
                            }
                            if ("0".equals(role)) {
                                if (table.getPlayBureau() > 1) {
                                    try {
                                        table.sendAccountsMsg();
                                    } catch (Throwable e) {
                                        LogUtil.errorLog.error("tableId=" + table.getId() + ",total calc Exception:" + e.getMessage(), e);
                                        GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + table.getId() + "被解散").build();
                                        for (Player player : players) {
                                            player.writeSocket(errorMsg);
                                        }
                                    }
                                }
                                table.setTiqianDiss(true);
                                int res = table.diss();
                                if (res == 1) {
                                    for (Player player : players) {
                                        if (player.getIsOnline() == 1) {
                                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_57, "0".equals(role)?"管理员":"代理"));
                                            player.writeComMessage(WebSocketMsgType.res_code_disstable, playType);
                                        }
                                    }
                                    Map<String, Object> paramMap = new HashMap<>();
                                    paramMap.put("roomId", roomId);
                                    paramMap.put("agencyId", agencyId);
                                    paramMap.put("serverId", serverId);
                                    paramMap.put("players", StringUtil.implode(idList));
                                    paramMap.put("createTime", TimeUtil.now());
                                    TableDao.getInstance().addDissInfo(paramMap);
                                }
                                // 0 解散失败 1解散成功
                                writeMsg(response, res + "");
                                if (res != 1) {
                                    LogUtil.e("agencyDissRoom-->roomId:" + roomId + ",agencyId:" + agencyId + ",res:" + res);
                                }
                                return;
                            } else {
                                // -3 当前房间有绑定其他代理邀请码的玩家参与，请联系客服处理！
                                writeMsg(response, "-3");
                            }
                        } else {
                            LogUtil.e("agencyDissRoom table is null-->roomId:"+roomId+",myServerId:"+GameServerConfig.SERVER_ID);
                            // -2 房间不存在
                            writeMsg(response, "-2");
                        }
                    } else {
                        LogUtil.e("agencyDissRoom serverId err-->rqServerId:"+serverId+",myServerId:"+GameServerConfig.SERVER_ID);
                        //-4 服id不正确
                        writeMsg(response, "-4");
                    }
                    return;
                }else if ("playerUserState".equals(type) && NumberUtils.isDigits(userId) && isDigits(message)) {
                    Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                    if (player != null) {
                        player.changeUserState(Integer.parseInt(message));
                        writeMsg(response, "1");
                    } else {
                        writeMsg(response, "0");
                    }

                    return;
                } else if ("playerCards".equals(type) && NumberUtils.isDigits(userId) && isDigits(message)) {
                    Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                    if (player != null) {
                        if ("1".equals(params.get("free"))) {
                            player.changeCards(Long.parseLong(message), 0, "1".equals(params.get("SAVEDB")), true, CardSourceType.unknown);
                        } else {
                            player.changeCards(0, Long.parseLong(message), "1".equals(params.get("SAVEDB")), true, CardSourceType.unknown);
                        }
                        writeMsg(response, "1");
                    } else {
                        writeMsg(response, "0");
                    }

                    return;
                } else if ("chat".equals(type) && isDigits(message)) {
                    writeMsg(response, "1");

                    HashMap<String, Object> msgMap = ChatMessageDao.getInstance().select(message);
                    if (msgMap != null && msgMap.size() > 0) {
                        long fromUser = (Long) msgMap.get("fromUser");
                        RegInfo regInfo = UserDao.getInstance().selectUserByUserId(fromUser);
                        msgMap.put("userName", regInfo == null ? fromUser : regInfo.getName());

                        long groupId = (Long) msgMap.get("groupId");
                        List<HashMap<String, Object>> list = GroupDao.getInstance().loadAllGroupUser(groupId);
                        if (list != null) {
                            ComMsg.ComRes.Builder comMsgBuilder0 = ComMsg.ComRes.newBuilder();
                            comMsgBuilder0.setCode(WebSocketMsgType.sc_chat);
                            comMsgBuilder0.addParams(0);
                            comMsgBuilder0.addParams(1);
                            comMsgBuilder0.addStrParams(JacksonUtil.writeValueAsString(msgMap));

                            GeneratedMessage msg0 = comMsgBuilder0.build();

                            for (HashMap<String, Object> map : list) {
                                long tempUserId = ((Number) map.get("userId")).longValue();
                                if (fromUser != tempUserId) {
                                    MyWebSocket myWebSocket = WebSocketManager.webSocketMap.get(tempUserId);
                                    if (myWebSocket != null) {
                                        myWebSocket.send(msg0);
                                    }
                                }
                            }
                        }
                    }

                    return;
                } else if("autoCreateGroupTable".equals(type) && NumberUtils.isDigits(userId) && isDigits(message)){
                    String gameType = params.get("gameType");
                    BaseTable table=TableManager.getInstance().getInstanceTable(NumberUtils.toInt(gameType,-1));
                    LogUtil.msgLog.info("service autoCreateGroupTable:userId="+userId+",groupId="+message+",gameType="+gameType);
                    String configId = params.get("configId");
                    
                    if (StringUtils.isBlank(configId)) {
                    	GameUtil.autoCreateGroupTable(message,table != null? table.getPlayerClass() : CommonPlayer.class);
                    }
                    else {
                    	GameUtil.autoCreateGroupTable(message,table != null? table.getPlayerClass() : CommonPlayer.class, Long.parseLong(configId));
                    }
                    

                    writeMsg(response, "1");
                    return;
                } else if ("SetIp".equals(type) && NumberUtils.isDigits(userId) && message != null && GeneralHelper.isStrIPAddress(message)){
                    Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
                    if (player!=null){
                        player.setIp(message,false);
                        writeMsg(response,"1");
                    }else{
                        writeMsg(response,"0");
                    }
                    return;
                } else if ("online".equals(type)){
                    OutputUtil.output(0,NettyUtil.channelUserMap.size(),request,response,false);
                    return;
                } else if ("marquee".equals(type)){
                    if (StringUtils.isNotBlank(message)){
                        if (CommonUtil.isPureNumber(userId)){
                            Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
                            if (player!=null) {
                                MarqueeManager.getInstance().sendMarquee(message,NumberUtils.toInt(params.get("round"),1),NumberUtils.toInt(params.get("msgType"),0),player);
                            }
                        }else{
                            MarqueeManager.getInstance().sendMarquee(message,NumberUtils.toInt(params.get("round"),1),NumberUtils.toInt(params.get("msgType"),0),null);
                        }
                    }
                    writeMsg(response, "1");
                    return;
                }else if ("groupApply".equals(type) && NumberUtils.isDigits(userId)) {
                    Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                    if (player != null) {
                    	player.writeComMessage(WebSocketMsgType.MULTI_CREATE_TABLE, 1, 0);
                        writeMsg(response, "1");
                    } else {
                        writeMsg(response, "0");
                    }

                    return;
                }else if ("commonApply".equals(type) && NumberUtils.isDigits(userId)) {
                    Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                    if (player != null) {
                    	player.writeComMessage(WebSocketMsgType.COMMON_FRESH_APPLY, 1, 0);
                        writeMsg(response, "1");
                    } else {
                        writeMsg(response, "0");
                    }

                    return;
                } 
                else if ("notifyChangCards".equals(type)) {
                    if (NumberUtils.isDigits(userId)) {
                        long cards = Long.parseLong(params.get("cards"));
                        long freeCards = Long.parseLong(params.get("freeCards"));
                        Player player = PlayerManager.getInstance().getPlayer(Long.parseLong(userId));
                        if (player != null) {
                            player.notifyChangeCards(cards, freeCards, CardSourceType.bjd_changeCards);
                            writeMsg(response, "1");
                        } else {
                            writeMsg(response, "0");
                        }
                    }
                    return;
                }

                writeMsg(response, "-1");
            } else {
                writeMsg(response, "ip or sign invalid");
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    private static void writeMsg(HttpServletResponse response, String msg) throws IOException {
        Writer writer = response.getWriter();
        writer.write(msg);
        writer.flush();
        writer.close();
    }

    /**
     * 判断是否是数字（正整数、0、负整数）
     *
     * @param str
     * @return
     */
    private static boolean isDigits(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        } else {
            if (str.charAt(0) == '-') {
                return NumberUtils.isDigits(str.substring(1));
            } else if (str.charAt(0) == '+') {
                return NumberUtils.isDigits(str.substring(1));
            } else {
                return NumberUtils.isDigits(str);
            }
        }
    }

}
