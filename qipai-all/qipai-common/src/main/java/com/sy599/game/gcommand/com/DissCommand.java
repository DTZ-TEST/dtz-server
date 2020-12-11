package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解散
 * 
 * @author lc
 */
public class DissCommand extends BaseCommand {
	@Override
	public void execute(Player player, MessageUnit message) throws Exception {

		if (player.isPlayingMatch()){
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
			return;
		}

		long tableId;
        long groupId = 0;
		if (message!=null){
			ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
//			List<Integer> params = req.getParamsList();
			List<String> strParams = req.getStrParamsList();
			if (strParams!=null&&strParams.size()>0&& NumberUtils.isDigits(strParams.get(0))){
                String tableIdStr = strParams.get(0);
				tableId=NumberUtils.toLong(tableIdStr);
				BaseTable table = TableManager.getInstance().getTable(tableId);
				GroupTable gt;
                // 是否俱乐部军团长外部解散
                boolean isGMOuterDiss = false;
				if (strParams.size()>1) {
                    String keyId = strParams.get(1);
					// 检查是否有这个俱乐部房间
                    gt = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
                    if (gt != null ) {
                        // 检查房间状态
                        if (gt.isOver()) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_8, gt.getTableId()));

							if (Redis.isConnected()){
								RedisUtil.zrem(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(),gt.loadGroupRoom()),gt.getKeyId().toString());
								RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(),gt.loadGroupRoom()),gt.getKeyId().toString());
							}

                            return;
                        }

                        // 检查是否群主
                        GroupUser groupUser = player.getGroupUser();
                        if (groupUser!=null&&groupUser.getGroupId().longValue()==gt.getGroupId().longValue()){
                        }else{
                            groupUser = player.loadGroupUser(gt.getGroupId().toString());
                        }
                        groupId = gt.getGroupId().longValue();
                        if (groupUser ==null || groupUser.getUserRole().intValue() != 0) {
                            if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("wzqf_admin_role"))){
                                if(groupUser.getUserRole().intValue() != 1){
                                    player.writeErrMsgs(LangHelp.getMsg(LangMsg.code_43));
                                    return;
                                }
                            }else{
                                player.writeErrMsgs(LangHelp.getMsg(LangMsg.code_43));
                                return;
                            }

                        }

                        // 如果房间不在本服 则通知其他服解散房间
                        int serverId = NumberUtils.toInt(gt.getServerId(), -1);
                        if (serverId > 0 && serverId != GameServerConfig.SERVER_ID) {
                            Map<String, String> map = new HashMap<>();
                            map.put("tableIds", tableIdStr);
                            map.put("keyIds", keyId);
                            map.put("specialDiss", "1");
                            int checkCode = player.getMsgCheckCode();
                            String res = HttpGameUtil.sendDissInfo(serverId, map);
                            LogUtil.msg("sendDissInfo-->serverId:" + serverId + ",infoMap:" + JSON.toJSONString(ServerManager.loadServer(serverId)) + ",res:" + res
							+",checkCode="+checkCode+":"+player.getMsgCheckCode());
                            if (tableIdStr.equals(res)) {
                                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, 0, gt.getGroupId().intValue(), 0);
                                GeneratedMessage msg2 = com.build();
                                player.writeSocket(msg2);
                                LogUtil.msgLog.info("gm outer diss group table success:userId="+player.getUserId()+",msg="+ JacksonUtil.writeValueAsString(gt));
                            } else {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_59));
                            }
                            return;
                        }

                        // 在本服则继续解散房间
						isGMOuterDiss = true;
                    }
				} else {
					gt = GroupDao.getInstance().loadGroupTable(player.getUserId(),tableId);
				}
				if (gt!=null){
					String[] tempMsgs=new JsonWrapper(gt.getTableMsg()).getString("strs").split(";")[0].split("_");
					String payType = tempMsgs[0];
					String userId = tempMsgs[1];
					if ((userId.equals(String.valueOf(player.getUserId())) &&(gt.getCurrentCount().intValue()<=0||table!=null)) || isGMOuterDiss){
						if (table!=null){
							if (isGMOuterDiss) {
								table.setSpecialDiss(1);
							}

							ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, table.getPlayType(), gt.getGroupId().intValue());
							GeneratedMessage msg = com.build();

							for (Player player0 : table.getSeatMap().values()) {
								player0.writeSocket(msg);
								player0.writeErrMsg(LangHelp.getMsg(table.isGroupMasterDiss()?LangMsg.code_60:LangMsg.code_8, table.getId()));
							}

							for (Player player0 : table.getRoomPlayerMap().values()) {
								player0.writeSocket(msg);
								player0.writeErrMsg(LangHelp.getMsg(table.isGroupMasterDiss()?LangMsg.code_60:LangMsg.code_8, table.getId()));
							}

							if (isGMOuterDiss && table.isDissSendAccountsMsg()) {
                                try {
                                    table.sendAccountsMsg();
                                } catch (Throwable e) {
                                    LogUtil.errorLog.error("tableId=" + table.getId() + ",total calc Exception:" + e.getMessage(), e);
                                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + table.getId() + "被解散").build();
                                    for (Player player0 : table.getPlayerMap().values()) {
                                        player0.writeSocket(errorMsg);
                                    }
                                }
								table.calcCreditNew();
								table.setTiqianDiss(true);
							}
							table.diss();
						}else{
							HashMap<String,Object> map=new HashMap<>();
							map.put("currentState","3");
							map.put("currentCount","0");
							map.put("keyId",gt.getKeyId().toString());

							if (Redis.isConnected()){
								RedisUtil.zrem(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(),gt.loadGroupRoom()),gt.getKeyId().toString());
								RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(),gt.loadGroupRoom()),gt.getKeyId().toString());
							}

							GroupDao.getInstance().updateGroupTableByKeyId(map);
							GroupDao.getInstance().deleteTableUser(gt.getKeyId().toString());

							LogUtil.msgLog.info("diss group table success:userId="+player.getUserId()+",msg="+ JacksonUtil.writeValueAsString(gt));

							//根据payType返回钻石currentState
							if (tempMsgs.length>=4){
								if("2".equals(payType)||"3".equals(payType)){
									CardSourceType sourceType;
									if("2".equals(payType)) {
										sourceType = CardSourceType.groupTable_diss_FZ;
									} else
										sourceType = CardSourceType.groupTable_diss_QZ;
									Player payPlayer= PlayerManager.getInstance().getPlayer(Long.valueOf(tempMsgs[2]));
									if (payPlayer!=null){
										payPlayer.changeCards(Integer.parseInt(tempMsgs[3]),0,true,sourceType);
									}else{
										RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(tempMsgs[2]));
										payPlayer = ObjectUtil.newInstance(player.getClass());
										payPlayer.loadFromDB(user);
										payPlayer.changeCards(Integer.parseInt(tempMsgs[3]),0,true,sourceType);

										if (payPlayer.getEnterServer()>0&&user.getIsOnLine()==1){
											Server server1= ServerManager.loadServer(payPlayer.getEnterServer());
											if (server1!=null){
												String url=server1.getIntranet();
												if (StringUtils.isBlank(url)){
													url=server1.getHost();
												}

												if (StringUtils.isNotBlank(url)){
													int idx=url.indexOf(".");
													if (idx>0){
														idx=url.indexOf("/",idx);
														if (idx>0){
															url=url.substring(0,idx);
														}
														url+="/online/notice.do?type=playerCards&userId="+payPlayer.getUserId();

														String noticeRet = HttpUtil.getUrlReturnValue(url+"&free=1&message="+tempMsgs[3],2);
														LogUtil.msgLog.info("notice result:url="+url+",ret="+noticeRet);
													}
												}
											}
										}
									}
								}
							}
                            GroupTableConfig config = GroupDao.getInstance().loadGroupTableConfig(gt.getConfigId());
							if(config != null){
                                BaseTable baseTable = TableManager.getInstance().getInstanceTable(config.getGameType());
                                if(baseTable != null){
                                    GameUtil.autoCreateGroupTable(gt.getGroupId().toString(),baseTable.getPlayerClass(),config.getKeyId());
                                }
                            }
						}

						if (isGMOuterDiss) {
							ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable, 0, gt.getGroupId().intValue(), 0);
							GeneratedMessage msg2 = com.build();
							player.writeSocket(msg2);
							LogUtil.msgLog.info("gm outer diss group table success:userId="+player.getUserId()+",msg="+ JacksonUtil.writeValueAsString(gt));
						}
					}else{
						player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
					}

					return;
				}
			}
		}

		if(GoldRoomUtil.isGoldRoom(player)){
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
			return;
		}

		BaseTable table = player.getPlayingTable();

		if (table == null) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1));
			return;
		}
		if (table.isCompetition()) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_19));
			return;
		}

		if (!table.getPlayerMap().containsKey(player.getUserId())&&(table.getServerKey()==null||!table.getServerKey().startsWith("group"))){
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
			return;
		}
		
		boolean canDissTable = table.canDissTable(player);
		if (!canDissTable) {
			return;
		}

        if (table.getGroupTable() != null) {
            groupId = table.getGroupTable().getGroupId();
        }
        GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupId, 0);
        if (groupInfo != null) {
            JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
            Integer obj = jsonObject.getInteger("dismissCount");
            if (obj != null && obj > 0) {
                if (player.getDissCount() >= obj) {
                    player.writeErrMsg("申请解散次数已超过限制");
                    return;
                } else {
                    player.addDissCount();
                }
            }
        }

		// 如果当前玩家是房主，另外两个人没进房间或离开房间时，无视牌局有没有开始，点击“解散房间”，房间解散，不退房卡
		// 如果当前玩家不是房主或房间里有两个以上的玩家时，无视牌局有没有开始，点击“解散房间”，
		// 提示其他玩家“是否同意解散房间”，必须都同意后，才解散房间，不退房卡。

		// MajiangPlayer majiang=(MajiangPlayer)player;
		// MajiangTool.isHu(majiang.getHandMajiang());
		// if (PdkConstants.isTest) {
		// return;
		// }
		// MajiangTable majiangTable=player.getPlayingTable(MajiangTable.class);
		// majiangTable.getActionSeatMap();
		table.clearAnswerDiss();
		table.answerDiss(player.getSeat(), 1);
		boolean diss = table.checkDiss(player);
		LogUtil.msgLog.info("table diss:" + table.getId() + " player:" + player.getUserId()+ " seat:" + player.getSeat() + " playType:" + table.getPlayType() + " pb:" + table.getPlayBureau() + " result:" + diss);

//		if (!diss) {
//			long nowTime = TimeUtil.currentTimeMillis();
//			long applyTime = table.getSendDissTime();
//			int countDown = (int) (300000 - (nowTime - applyTime));
//			
//			ComRes.Builder com = null;
//			
//			
//			for (Player tableplayer : table.getSeatMap().values()) {
//				com = SendMsgUtil.buildComRes(WebSocketMsgType.sc_code_senddisstable, countDown, table.getSeatMap().get(tableplayer.getSeat()));
//				tableplayer.writeSocket(com.build());
//			}
//		}


	}

	@Override
	public void setMsgTypeMap() {

	}

}
