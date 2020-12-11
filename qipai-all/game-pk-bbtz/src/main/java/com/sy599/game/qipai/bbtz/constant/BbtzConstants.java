package com.sy599.game.qipai.bbtz.constant;

import java.util.ArrayList;
import java.util.List;

public class BbtzConstants {
	public static boolean isTest = false;

	public static final int state_player_ready = 1;
	public static final int state_player_offline = 2;
	public static final int state_player_diss = 3;

	/** 牌局离线 **/
	public static final int table_offline = 1;
	/** 牌局在线 **/
	public static final int table_online = 2;
	/** 牌局暂离 **/
	public static final int table_afk = 3;
	/** 牌局暂离回来 **/
	public static final int table_afkback = 4;
	/** 半边天炸玩法 **/
	public static final int play_type_bbtz = 131;
	
	public static final int TABLE_READY = 1;//准备
	public static final int TABLE_CHUI = 2;//锤
	public static final int TABLE_KAIQIANG = 3;//开枪
	public static final int TABLE_ROB_BANKER = 4;//强庄
	public static final int TABLE_DOU = 5;//陡
	public static final int TABLE_PLAY = 6;//玩
	public static final int TABLE_OVER = 7;//结束

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";

	public static List<Integer> cardList_bbtz = new ArrayList<>();
	
	static {
		// 方片 1 梅花2 红桃3 黑桃4
		for (int i = 1; i <= 4; i++) {
			for (int j = 3; j <= 14; j++) {
				int card = i * 100 + j;
				cardList_bbtz.add(card);
			}
		}
		cardList_bbtz.add(315);
		cardList_bbtz.add(415);
		cardList_bbtz.add(517);
	}

	public static boolean isPlayBbtz(int playType) {
		if (playType == play_type_bbtz) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
	}
}
