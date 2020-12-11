package com.sy599.game.qipai.ahddz.constant;

import com.sy599.game.GameServerConfig;
import com.sy599.game.util.JacksonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DdzConstants {
	public static boolean isTest = false;
	
	/** 确定地主日志**/
	public static int play_sureLandLord = 3;
	/** 公牌日志**/
	public static int play_publicCard = 4;

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
	
	/** 叫地主**/
	public static final int call_landlord = 3;
	/** 抢地主**/
	public static final int rob_landlord = 2;
	/** 不叫(不抢)**/
	public static final int no_rob = 1;

	/** 三人斗地主**/
	public static final int ddz_three = 91;
	/** 二人斗地主**/
	public static final int ddz_two = 92;
	/** 赖子玩法**/
	public static final int ddz_three_niggle  = 93;

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";
	// public static final int state_player_online=3;
	public static List<Integer> cardList_16 = new ArrayList<>();
	public static List<Integer> cardList_15 = new ArrayList<>();
	
	public static List<Integer> List_34 = new ArrayList<>(Arrays.asList(103,203,303,403,104,204,304,404));
	
	static {
		if (GameServerConfig.isDeveloper()) {
			 isTest = true;
		}
		// 方片 1 梅花2 洪涛3 黑桃4

		// ///////////////////////
		// 16张玩法 3-A 1-2
		for (int i = 1; i <= 4; i++) {
			for (int j = 3; j <= 15; j++) {
				int card = i * 100 + j;
				cardList_16.add(card);
			}
		}
		// 大小王
		cardList_16.add(516);
		cardList_16.add(517);
	}

	public static boolean isPlayDdz(int playType) {
		return playType == ddz_three || playType == ddz_two || playType == ddz_three_niggle ;
	}
	
	public static void main(String[] args) {
		System.out.println(cardList_16.size());
		System.out.println(JacksonUtil.writeValueAsString(cardList_16));
		List<Integer> copy = new ArrayList<>(cardList_16);
		Collections.shuffle(copy);
		System.out.println(copy);
	}
}
