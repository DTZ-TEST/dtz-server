package com.sy599.game.qipai.bbtz.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.bbtz.bean.BbtzPlayer;
import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.constant.BbtzConstants;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CardTool {
	/**
	 * @param playerCount
	 *            人数
	 * @param daiWang 是否带王
	 * @param zps
	 * @param checkBoom
	 * @return
	 */
	public static synchronized List<List<Integer>> fapai(int playerCount, boolean daiWang, List<List<Integer>> zps, boolean checkBoom) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy = new ArrayList<>(BbtzConstants.cardList_bbtz);
		Collections.shuffle(copy);
		if(!daiWang){
		    //是否带王
		    copy.remove(new Integer(517));
        }

		List<Integer> pai = new ArrayList<>();
		if (GameServerConfig.isDebug()) {
			if (zps != null && !zps.isEmpty()) {
				for (List<Integer> zp : zps) {
					list.add(findCardIIds(copy, zp, 0));
				}
			}

			if (list.size() == 3) {
				return list;
			}
		}

        int maxCount = copy.size() / playerCount;
        if (playerCount == 2) {
            //两人玩法发17张牌
            maxCount = 17;
        }
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
			}

		}
		list.add(pai);

		// 决定发牌里面的炸弹
//		List<Integer> boomList = checkBoom(list);
//		if (checkBoom && !boomList.isEmpty()) {
//			if (Math.random() > 0.4) {
//				return fapai(playerCount, playType, zps, false);
//
//			}
//		}
		return list;
	}

	public static synchronized List<List<Integer>> fapai(int playerCount, boolean daiWang, List<List<Integer>> zps) {
		return fapai(playerCount, daiWang, zps, true);
	}

	private static List<Integer> checkBoom(List<List<Integer>> lists) {
		List<Integer> boomList = new ArrayList<>();
		for (List<Integer> list : lists) {
			boomList.addAll(CardTypeTool.getBoomCount(list));
		}
		return boomList;

	}

	public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
		List<Integer> pai = new ArrayList<>();
		if (!vals.isEmpty()) {
			int i = 1;
			for (int zpId : vals) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					int paiVal=card;// % 100;
					if (paiVal == zpId) {
						pai.add(card);
						iterator.remove();
						break;
					}
				}
				if (cardNum != 0) {
					if (i >= cardNum) {
						break;
					}
					i++;
				}
			}
		}
		return pai;
	}

	/**
	 * 锤
	 * @param chuiVal 1 锤 2不锤
	 */
	public static final void chui(BbtzTable table, BbtzPlayer player, int chuiVal){
		if(table.getTableStatus() != BbtzConstants.TABLE_CHUI){
			return;
		}
		if(player.getChui()==0 && (chuiVal==1 || chuiVal==2)){
			player.setChui(chuiVal);
		}else{
			return;
		}
		table.setLastActionTime(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("chui");
        sb.append("|").append(chuiVal);
        LogUtil.msgLog.info(sb.toString());

		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_CHUI, player.getSeat(), chuiVal);
		table.broadMsg(build.build());
		table.checkDeal1();
	}
	/**
	 * 开枪
	 * @param kaiQiangVal 1 开枪 2投降
	 */
	public static final void kaiQiang(BbtzTable table, BbtzPlayer player, int kaiQiangVal){
		if(table.getTableStatus() != BbtzConstants.TABLE_KAIQIANG){
			return;
		}
		if(player.getKaiqiang()==0 && (kaiQiangVal==1 || kaiQiangVal==2) && player.getSeat() == table.getWangSeat()){
			player.setKaiqiang(kaiQiangVal);
		}else{
			return;
		}
		table.setLastActionTime(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("kaiQiang");
        sb.append("|").append(kaiQiangVal);
        LogUtil.msgLog.info(sb.toString());

		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_KAIQIANG, player.getSeat(), kaiQiangVal);
		table.broadMsg(build.build());
		if(player.getKaiqiang() == 1){//开枪
			table.setBankerSeat(player.getSeat());
			player.setBankerCount(player.getBankerCount()+1);
			ComMsg.ComRes.Builder build1 = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_BANKER, player.getSeat());
			table.broadMsg(build1.build());
			table.pushDou(player.getSeat());
		}else{//投降
			table.changeTableStatus(BbtzConstants.TABLE_ROB_BANKER, true, player.getSeat());
		}
	}
	/**
	 * 抢庄
	 * @param robBankerVal 1 抢庄 2不抢
	 */
	public static final void robBanker(BbtzTable table, BbtzPlayer player, int robBankerVal){
		if(table.getTableStatus() != BbtzConstants.TABLE_ROB_BANKER){
			return;
		}
		if(player.getRobBanker()==0 && (robBankerVal==1 || robBankerVal==2) && player.getSeat() != table.getWangSeat()){
			player.setRobBanker(robBankerVal);
		}else{
			return;
		}
		table.setLastActionTime(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("robBanker");
        sb.append("|").append(robBankerVal);
        LogUtil.msgLog.info(sb.toString());

		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_ROB_BANKER, player.getSeat(), robBankerVal);
		table.broadMsg(build.build());
		table.checkRobBankerOver();
	}
	/**
	 * 陡
	 * @param douVal 1 陡 2不陡
	 */
	public static final void dou(BbtzTable table, BbtzPlayer player, int douVal){
		if(table.getTableStatus() != BbtzConstants.TABLE_DOU){
			return;
		}
		if(player.getDou()==0 && (douVal==1 || douVal==2)){
			if(player.getSeat() == table.getBankerSeat()){//庄家
				int xianDouNum = 0;
				for(Player player1 : table.getSeatMap().values()){
					if(player1.getSeat() == player.getSeat()){
						continue;
					}
					BbtzPlayer p = (BbtzPlayer)player1;
					if(p.getDou()==1){
						xianDouNum++;
					}
				}
				if(table.getMaxPlayerCount() == 3 && xianDouNum == 0){//没有闲家陡
					return;
				}
			}else if(table.getZhuDou() == 1){//助陡
				int zaidanNum = CardTypeTool.getZaiDanNum(player.getHandPais());
				if(zaidanNum < 2){
					return;
				}
				if(douVal == 1){
					player.setZhudou(1);
					for(Player player1 : table.getSeatMap().values()){
						if(player1.getSeat() == player.getSeat() || player1.getSeat() == table.getBankerSeat()){
							continue;
						}
						BbtzPlayer p = (BbtzPlayer)player1;
						p.setDou(douVal);
						ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_DOU, p.getSeat(), douVal, 0);
						table.broadMsg(build.build());
					}
				}
			}
			player.setDou(douVal);
		}else{
			return;
		}
		table.setLastActionTime(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder("Bbtz");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("dou");
        sb.append("|").append(douVal);
        LogUtil.msgLog.info(sb.toString());

		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_BBTZ_DOU, player.getSeat(), douVal, player.getZhudou());
		table.broadMsg(build.build());
		BbtzPlayer bankerPlayer = (BbtzPlayer)table.getSeatMap().get(table.getBankerSeat());
		if(table.checkDouOver()){
			table.changeTableStatus(BbtzConstants.TABLE_PLAY, true, table.getNowDisCardSeat());
		}else if(player.getDou()==1 && bankerPlayer.getDou() == 0){
			bankerPlayer.writeComMessage(WebSocketMsgType.RES_BBTZ_STATE, table.getTableStatus(), bankerPlayer.getSeat(), 1);
		}
	}

    public static int loadCardValue(int id) {
        return id % 100;
    }

    public static List<Integer> loadCards(List<Integer> list, int val) {
        List<Integer> ret = new ArrayList<>(4);
        for (Integer integer : list) {
            if (val == loadCardValue(integer.intValue())) {
                ret.add(integer);
            }
        }
        return ret;
    }

    public static Map<Integer, Integer> loadCards(List<Integer> list) {
        Map<Integer, Integer> map = new TreeMap<>();
        for (Integer integer : list) {
            int val = loadCardValue(integer.intValue());
            int count = map.getOrDefault(val, 0);
            count++;
            map.put(val, count);
        }
        return map;
    }

    public static int loadCardHuaSe(int id) {
        return id / 100;
    }

}
