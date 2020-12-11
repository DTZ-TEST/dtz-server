package com.sy599.game.qipai.bbtz.compare;

import com.sy599.game.qipai.bbtz.bean.BbtzModel;
import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.rule.CardType;
import com.sy599.game.qipai.bbtz.tool.CardTypeTool;

import java.util.*;

public class CanPlayCompareFactory {

	public static final CanPlayCompareFactory factory = new CanPlayCompareFactory();
	public static final int PASS_CARD = 0;
	public static final int PLAY_CARD = 1;
	
	/**
	 * 单张
	 */
	private CanPlayCompare single = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			if(CardTypeTool.getValue(model.getCards().get(0)) > CardTypeTool.getValue(outCards.get(0))){
				return PLAY_CARD;
			}
			return isBomb(model);
		}
	};
	/**
	 * 对子
	 */
	private CanPlayCompare pair = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			int outMaxCard = CardTypeTool.getValue(outCards.get(0));
			for(Map.Entry<Integer, List<Integer>> entry : model.getSamePai().entrySet()){
				if(entry.getKey() == 1){
					continue;
				}
				if(entry.getKey() == 4){
					return PLAY_CARD;
				}
				for(Integer var : entry.getValue()){
					if(var > outMaxCard){
						return PLAY_CARD;
					}
				}
			}
			return isBomb(model);
		}
	};
	/**
	 * 三张或三带
	 */
	private CanPlayCompare threeWithone = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			Map<Integer,Integer> map = new HashMap<>();
			int outMaxCard = 0;
			for(int val : outCards){
				val = CardTypeTool.getValue(val);
				if(!map.containsKey(val)){
					map.put(val,1);
				}else{
					if(map.get(val)==2){
						outMaxCard = val;
						break;
					}
					map.put(val,map.get(val)+1);
				}
			}
			for(Map.Entry<Integer, List<Integer>> entry : model.getSamePai().entrySet()){
				if(entry.getKey() == 1 || entry.getKey() == 2){
					continue;
				}
				if(entry.getKey() == 4){
					return PLAY_CARD;
				}
				for(Integer var : entry.getValue()){
					if(var > outMaxCard){
						return PLAY_CARD;
					}
				}
			}
			return isBomb(model);
		}
	};
	/**
	 * 四带一二三
	 */
	private CanPlayCompare fourWithone = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			return isBomb(model);
		}
	};
	/**
	 * 单顺
	 */
	private CanPlayCompare straight = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			List<Integer> list =  CardTypeTool.toValueList(outCards);
			return straightTool(model, Collections.max(list), 1, outCards.size());
		}
	};
	/**
	 * 双顺
	 */
	private CanPlayCompare doubleStraight = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			List<Integer> list =  CardTypeTool.toValueList(outCards);
			return straightTool(model, Collections.max(list), 2, outCards.size()/2);
		}
	};
	/**
	 * 三顺含飞机
	 */
	private CanPlayCompare threeStraight = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			BbtzTable table = (BbtzTable)param[0];
			int len = table.getPlaneLength();
			Map<Integer,Integer> map = new HashMap<>();
			for(int val : outCards){
				val = CardTypeTool.getValue(val);
				if(!map.containsKey(val)){
					map.put(val,1);
				}else{
					map.put(val,map.get(val)+1);
				}
			}
			List<Integer> list = new ArrayList<Integer>();
			for(Map.Entry<Integer,Integer> entry : map.entrySet()){
				if(entry.getValue() >= 3){
					list.add(entry.getKey());
				}
			}
			int outMaxCard = Collections.max(list);
			if(list.size() != len){
				Collections.sort(list,Collections.reverseOrder());
				for(int i=0;i<list.size();i++){
					if(list.get(i)-len+1 == list.get(i+len-1)){
						outMaxCard = list.get(i);
						break;
					}
				}
			}
			return straightTool(model, outMaxCard, 3, len);
		}
	};
	/**
	 * 510k
	 */
	private CanPlayCompare bomb510k = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			BbtzTable table = (BbtzTable)param[0];
			if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
				return PLAY_CARD;
			}
			int maxColor = 0;
			Map<Integer, List<Integer>> colorPai = model.getColorPai();
			for(Map.Entry<Integer, List<Integer>> entry : colorPai.entrySet()){
				if(entry.getKey() < maxColor){
					continue;
				}
				List<Integer> crolorList = entry.getValue();
				if(crolorList.contains(5) && crolorList.contains(10) && crolorList.contains(13)){
					maxColor = entry.getKey();
				}
			}
			if(maxColor > 0){
				int outMaxColor = CardTypeTool.getColor(outCards.get(0));
				if(outMaxColor == CardTypeTool.getColor(outCards.get(1)) && outMaxColor == CardTypeTool.getColor(outCards.get(2))){
					if(table.getZheng510k() == 1 && maxColor > outMaxColor){
						return PLAY_CARD;
					}
				}else{
					return PLAY_CARD;
				}
			}
			if(CardTypeTool.bombNum(model.getSamePai()) > 0){
				return PLAY_CARD;
			}
			if(CardTypeTool.tongHuaBombNum(model.getColorPai()) > 0){
				return PLAY_CARD;
			}
			if(CardTypeTool.diBombNum(model.getSamePai()) > 0){
				return PLAY_CARD;
			}
			return PASS_CARD;
		}
	};
	/**
	 * 四炸
	 */
	private CanPlayCompare bomb = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
				return PLAY_CARD;
			}
			if(model.getSamePai().containsKey(4)){
				List<Integer> bombList = model.getSamePai().get(4);
				Collections.sort(bombList,Collections.reverseOrder());
				if(bombList.get(0)>CardTypeTool.getValue(outCards.get(0))){
					return PLAY_CARD;
				}
			}
			if(CardTypeTool.tongHuaBombNum(model.getColorPai()) > 0){
				return PLAY_CARD;
			}
			if(CardTypeTool.diBombNum(model.getSamePai()) > 0){
				return PLAY_CARD;
			}
			return PASS_CARD;
		}
	};
	/**
	 * 同花顺
	 */
	private CanPlayCompare tongHuaBomb = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
				return PLAY_CARD;
			}
			List<Integer> list =  CardTypeTool.toValueList(outCards);
			int len = outCards.size(), outMaxVal = Collections.max(list), outCrolor = CardTypeTool.getColor(outCards.get(0));
			Map<Integer, List<Integer>> colorPai = model.getColorPai();
			for(Map.Entry<Integer, List<Integer>> entry : colorPai.entrySet()){
				List<Integer> crolorList = entry.getValue();
				if(!crolorList.isEmpty() && crolorList.size() >= len){
					Collections.sort(crolorList,Collections.reverseOrder());
					for(int i=0;i<crolorList.size();i++){
						if(i+len > crolorList.size()){
							break;
						}
						int var = crolorList.get(i);
						if(var == 15){//2不算
							continue;
						}
						if(crolorList.size()>i+len && var-len == crolorList.get(i+len)){//长
							return PLAY_CARD;
						}
						if(var-len+1 == crolorList.get(i+len-1)){//一样长
							if(var > outMaxVal){//大
								return PLAY_CARD;
							}else if(var == outMaxVal){//一样大
								if(entry.getKey() > outCrolor){//花大
									return PLAY_CARD;
								}
							}
						}
					}
				}
			}
			if(CardTypeTool.diBombNum(model.getSamePai()) > 0){
				return PLAY_CARD;
			}
			return PASS_CARD;
		}
	};
	/**
	 * 地炸
	 */
	private CanPlayCompare diBomb = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
				return PLAY_CARD;
			}
			List<Integer> list =  CardTypeTool.toValueList(outCards);
			int len = outCards.size()/2, outMaxVal = Collections.max(list);
			Map<Integer, List<Integer>> samePai = model.getSamePai();
			List<Integer> cards = new ArrayList<>();
			if(samePai.containsKey(2)){
				cards.addAll(samePai.get(2));
			}
			if(samePai.containsKey(3)){
				cards.addAll(samePai.get(3));
			}
			if(samePai.containsKey(4)){
				cards.addAll(samePai.get(4));
			}
			if(!cards.isEmpty() && cards.size() >= len){
				Collections.sort(cards,Collections.reverseOrder()); 
				for(int i=0;i<cards.size();i++){
					if(i+len > cards.size()){
						break;
					}
					int var = cards.get(i);
					if(var == 15){//2不算
						continue;
					}
					if(cards.size()>i+len && var-len == cards.get(i+len)){//长
						return PLAY_CARD;
					}
					if(var-len+1 == cards.get(i+len-1)){//一样长
						if(var > outMaxVal){
							return PLAY_CARD;
						}
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 天炸
	 */
	private CanPlayCompare tianBomb = new CanPlayCompare() {
		@Override
		public int compareTo(BbtzModel model, List<Integer> outCards, Object... param) {
			return PASS_CARD;
		}
	};
	
	/**
	 * 得到比较器
	 * @return
	 */
	public CanPlayCompare getCompare(CardType cardType){
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
	private int isBomb(BbtzModel model){
		if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
			return PLAY_CARD;
		}
		if(CardTypeTool.bomb510kNum(model.getPaiVal()) > 0){
			return PLAY_CARD;
		}
		if(CardTypeTool.bombNum(model.getSamePai()) > 0){
			return PLAY_CARD;
		}
		if(CardTypeTool.tongHuaBombNum(model.getColorPai()) > 0){
			return PLAY_CARD;
		}
		if(CardTypeTool.diBombNum(model.getSamePai()) > 0){
			return PLAY_CARD;
		}
		return PASS_CARD;
	}
	/**
	 * 顺子
	 * @param model
	 * @param outMaxCard 最大顺牌
	 * @param num 2为双顺 3为三顺
	 * @param len 顺长度
	 * @return
	 */
	private int straightTool(BbtzModel model, int outMaxCard, int num, int len){
		Map<Integer, Integer> paiVal = model.getPaiVal();
		int maxCard = 14;
		List<Integer> list = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> entry : paiVal.entrySet()){
			if(entry.getValue() >= num){
				if(entry.getValue() == 4){  
					return PLAY_CARD;
				}
				if(entry.getKey() <= maxCard){
					list.add(entry.getKey());
				}
			}
		}
		ok:
		for(int val : list){
			if(val > outMaxCard){
				for(int i=1;i<len;i++){
					if(!paiVal.containsKey(val-i) || paiVal.get(val-i)<num){
						continue ok;
					}
				}
				return PLAY_CARD;
			}
		}
		return isBomb(model);
	}
}
