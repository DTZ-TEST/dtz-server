package com.sy599.game.qipai.bbtz.compare;

import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.bean.Card_index;
import com.sy599.game.qipai.bbtz.rule.CardType;
import com.sy599.game.qipai.bbtz.tool.CardTypeTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class CardTypeCompareFactory {
	
	public static final CardTypeCompareFactory factory = new CardTypeCompareFactory();
	public static final int WIN = 1;
	public static final int LOSS = 0;
	public static final int BOMB_ORIGIN = 8;

	/**
	 * 单张
	 */
	private CardTypeCompare single = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c1 && CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
				return WIN;
			}
			return LOSS;
		}
	};
	/**
	 * 对子
	 */
	private CardTypeCompare pair = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c2 && CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
				return WIN;
			}
			return LOSS;
		}
	};
	/**
	 * 三张或三带
	 */
	private CardTypeCompare threeWithone = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c32){
				int maxOutVal = 0,maxBeforeVal = 0,num = 1;
				CardTypeTool.setOrder(outCards);
				for(int card : outCards){
					int val = CardTypeTool.getValue(card);
					if(maxOutVal != val){
						maxOutVal = val;
					}else{
						num++;
					}
					if(num == 3){
						break;
					}
				}
				num = 1;
				CardTypeTool.setOrder(beforeOut);
				for(int card : beforeOut){
					int val = CardTypeTool.getValue(card);
					if(maxBeforeVal != val){
						maxBeforeVal = val;
					}else{
						num++;
					}
					if(num == 3){
						break;
					}
				}
				if(maxOutVal > maxBeforeVal){
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 四带一二三
	 */
	private CardTypeCompare fourWithone = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c43){
				int maxOutVal = 0,maxBeforeVal = 0,num = 1;
				CardTypeTool.setOrder(outCards);
				for(int card : outCards){
					int val = CardTypeTool.getValue(card);
					if(maxOutVal != val){
						maxOutVal = val;
					}else{
						num++;
					}
					if(num == 4){
						break;
					}
				}
				num = 1;
				CardTypeTool.setOrder(beforeOut);
				for(int card : beforeOut){
					int val = CardTypeTool.getValue(card);
					if(maxBeforeVal != val){
						maxBeforeVal = val;
					}else{
						num++;
					}
					if(num == 4){
						break;
					}
				}
				if(maxOutVal > maxBeforeVal){
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 单顺
	 */
	private CardTypeCompare straight = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c123 && outCards.size() == beforeOut.size()){
				CardTypeTool.setOrder(outCards);
				CardTypeTool.setOrder(beforeOut);
				if(CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 双顺
	 */
	private CardTypeCompare doubleStraight = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c1122 && outCards.size() == beforeOut.size()){
				CardTypeTool.setOrder(outCards);
				CardTypeTool.setOrder(beforeOut);
				if(CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 三顺含飞机
	 */
	private CardTypeCompare threeStraight = new CardTypeCompare() {
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() >= BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c111222){
				int maxOutVal = 0,maxBeforeVal = 0;
				Card_index card_index = new Card_index();
				for (int i = 0; i < 4; i++) {
					card_index.a[i] = new Vector<Integer>();
				}
				CardTypeTool.getMax(card_index, outCards);
				List<Integer> threeList = new ArrayList<>();
				threeList.addAll(card_index.a[2]);
				if (!card_index.a[3].isEmpty()) {
					threeList.addAll(card_index.a[3]);
					Collections.sort(threeList);
				}
				int outNear = CardTypeTool.getLianCount(threeList);
				Collections.sort(threeList,Collections.reverseOrder());
				for(int i=0;i<threeList.size();i++){
					if(threeList.get(i)-outNear+1 == threeList.get(i+outNear-1)){
						maxOutVal = threeList.get(i);
						break;
					}
				}
				
				Card_index card_index2 = new Card_index();
				for (int i = 0; i < 4; i++) {
					card_index2.a[i] = new Vector<Integer>();
				}
				CardTypeTool.getMax(card_index2, beforeOut);
				List<Integer> threeList2 = new ArrayList<>();
				threeList2.addAll(card_index2.a[2]);
				if (!card_index2.a[3].isEmpty()) {
					threeList2.addAll(card_index2.a[3]);
					Collections.sort(threeList2);
				}
				int beforeNear = CardTypeTool.getLianCount(threeList2);
				Collections.sort(threeList2,Collections.reverseOrder());
				for(int i=0;i<threeList2.size();i++){
					if(threeList2.get(i)-beforeNear+1 == threeList2.get(i+beforeNear-1)){
						maxBeforeVal = threeList2.get(i);
						break;
					}
				}
				BbtzTable table = (BbtzTable)param[0];
				int len = table.getPlaneLength();//需要传进第一个飞机长度 
				if(outNear >= len && maxOutVal > maxBeforeVal && outCards.size()-len * 3 <= len * 2){ 
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 510k
	 */
	private CardTypeCompare bomb510k = new CardTypeCompare(){
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType.getType() > BOMB_ORIGIN){
				return WIN;
			}else if(outCardType == CardType.c510k && CardTypeTool.isTonghua(outCards)){
				boolean isBeforeTonghua = CardTypeTool.isTonghua(beforeOut);
				if(isBeforeTonghua){
					BbtzTable table = (BbtzTable)param[0];
					if(table.getZheng510k() == 1 && CardTypeTool.getColor(outCards.get(0)) > CardTypeTool.getColor(beforeOut.get(0))){
						return WIN;
					}
				}else{
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 四炸
	 */
	private CardTypeCompare bomb = new CardTypeCompare(){
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType == CardType.c12345 || outCardType == CardType.c11223344 || outCardType == CardType.c517){
				return WIN;
			}else if(outCardType == CardType.c4){
				if(CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
					return WIN;
				}
			}
			return LOSS;
		}
	};
	/**
	 * 同花顺
	 */
	private CardTypeCompare tongHuaBomb = new CardTypeCompare(){
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType == CardType.c11223344 || outCardType == CardType.c517){
				return WIN;
			}else if(outCardType == CardType.c12345){
				if(outCards.size() > beforeOut.size()){
					return WIN;
				}else if(outCards.size() == beforeOut.size()){
					CardTypeTool.setOrder(outCards);
					CardTypeTool.setOrder(beforeOut);
					if(CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
						return WIN;
					}else if(CardTypeTool.getValue(outCards.get(0)) == CardTypeTool.getValue(beforeOut.get(0))){
						if(CardTypeTool.getColor(outCards.get(0)) > CardTypeTool.getColor(beforeOut.get(0))){
							return WIN;
						}
					}
				}
			}
			return LOSS;
		}
	};
	/**
	 * 地炸
	 */
	private CardTypeCompare diBomb = new CardTypeCompare(){
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			if(outCardType == CardType.c517){
				return WIN;
			}else if(outCardType == CardType.c11223344){
				if(outCards.size() > beforeOut.size()){
					return WIN;
				}else if(outCards.size() == beforeOut.size()){
					CardTypeTool.setOrder(outCards);
					CardTypeTool.setOrder(beforeOut);
					if(CardTypeTool.getValue(outCards.get(0)) > CardTypeTool.getValue(beforeOut.get(0))){
						return WIN;
					}
				}
			}
			return LOSS;
		}
	};
	/**
	 * 天炸
	 */
	private CardTypeCompare tianBomb = new CardTypeCompare(){
		@Override
		public int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param) {
			return LOSS;
		}
	};
	/**
	 * 得到比较器
	 * @return
	 */
	public CardTypeCompare getCompare(CardType cardType){
		if(cardType  == null){
			return null;
		}
		switch(cardType){
			case c1:
				return single;
			case c2:
				return pair;	
			case c32:
				return threeWithone;
			case c43:
				return fourWithone;
			case c123:
				return straight;	
			case c1122:
				return doubleStraight;	
			case c111222:
				return threeStraight;
			case c510k:
				return bomb510k;
			case c4:
				return bomb;
			case c12345:
				return tongHuaBomb;
			case c11223344:
				return diBomb;
			case c517:
				return tianBomb;
			default : return null;	
		}
	}
}
