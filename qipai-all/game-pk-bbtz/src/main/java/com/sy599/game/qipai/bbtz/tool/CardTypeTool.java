package com.sy599.game.qipai.bbtz.tool;

import com.sy599.game.qipai.bbtz.bean.BbtzModel;
import com.sy599.game.qipai.bbtz.bean.BbtzPlayer;
import com.sy599.game.qipai.bbtz.bean.BbtzTable;
import com.sy599.game.qipai.bbtz.bean.Card_index;
import com.sy599.game.qipai.bbtz.compare.CanPlayCompare;
import com.sy599.game.qipai.bbtz.compare.CanPlayCompareFactory;
import com.sy599.game.qipai.bbtz.compare.CardTypeCompare;
import com.sy599.game.qipai.bbtz.compare.CardTypeCompareFactory;
import com.sy599.game.qipai.bbtz.rule.CardType;
import com.sy599.game.qipai.bbtz.util.CardUtils;
import com.sy599.game.qipai.bbtz.util.CardValue;

import java.util.*;

/**
 * 规则 查询牌型 等等
 * 
 * @author
 * 
 */
public class CardTypeTool {


	public static int getPlaneLength(BbtzPlayer player, List<Integer> cards, CardType cardType) {
		return 0;
	}

	/**
	 * 判断牌型
	 * 
	 * @param list
	 * @return
	 */
	public static CardType jugdeType(List<Integer> list, BbtzTable table) {
		if (list == null || list.isEmpty()) {
			return CardType.c0;
		}
		setOrder(list);
		int len = list.size();
		// 天炸
		if (len == 1 && CardTypeTool.getValue(list.get(0)) == 17) {
			return CardType.c517;
		}
		// 单牌,对子，3不带，4个一样炸弹
		if (len <= 4) { // 如果第一个和最后个相同，说明全部相同
			if (list.size() > 0 && CardTypeTool.getValue(list.get(0)) == CardTypeTool.getValue(list.get(len - 1))) {
				switch (len) {
				case 1:
					return CardType.c1;
				case 2:
					return CardType.c2;
				case 3:
					return CardType.c32;
				case 4:
					return CardType.c4;
				}
			}
			if(len == 3 && CardTypeTool.getValue(list.get(0)) == 13 && CardTypeTool.getValue(list.get(1)) == 10 && CardTypeTool.getValue(list.get(2)) == 5){
				return CardType.c510k;
			}
			// 当第一个和最后个不同时,3带1
			if (len == 4 && ((CardTypeTool.getValue(list.get(0)) == CardTypeTool.getValue(list.get(len - 2))) || CardTypeTool.getValue(list.get(1)) == CardTypeTool.getValue(list.get(len - 1)))) {
				return CardType.c32;
			}

		}
		// 当5张以上时，连字，3带2，飞机，2顺，4带2等等
		if (len >= 4) {// 现在按相同数字最大出现次数
			Card_index card_index = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index, list); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
			// 3带2 -----必含重复3次的牌
			if (card_index.a[2].size() == 1 && (len == 4 || len == 5)) {
				return CardType.c32;
			}
            if (card_index.a[3].size() == 1 && (len == 5 || len == 6 || len == 7)) {
                if (len == 5 && table.getRoundCardType() == CardType.c0) {
                    // 第一个出牌，作为3带2
                    return CardType.c32;
                } else if (len == 5 && table.getRoundCardType() == CardType.c32) {
                    // 本轮是3带2，自己的也作3带2
                    return CardType.c32;
                } else if (table.getIs4_3() == 1) {
                    return CardType.c43;
                }
            }
			// 单连,最大到A
			if ((CardTypeTool.getValue(list.get(0)) <= 14) && (card_index.a[0].size() == len) && (CardTypeTool.getValue(list.get(0)) - CardTypeTool.getValue(list.get(len - 1)) == len - 1)) {
				if(CardTypeTool.isTonghua(list)){
					return CardType.c12345;//同花顺
				}else{
					return CardType.c123;
				}
			}
			// 连队
			if (card_index.a[1].size() == len / 2 && len % 2 == 0 && len / 2 >= 2 && (CardTypeTool.getValue(list.get(0)) - CardTypeTool.getValue(list.get(len - 1)) == (len / 2 - 1)) && CardTypeTool.getValue(list.get(0)) <= 14) {
				if(len / 2 >= 4){
					return CardType.c11223344;//地炸
				}else{
					return CardType.c1122;
				}
			}
			// 飞机带n单,n/2对
			List<Integer> threeList = new ArrayList<>();
			threeList.addAll(card_index.a[2]);
			if (!card_index.a[3].isEmpty()) {
				threeList.addAll(card_index.a[3]);
				Collections.sort(threeList);
			}
			int near = getLianCount(threeList);
			if (near >= 2 && len <= (near * 3) + (near * 2)) {
				if (near == 0) {
					return CardType.c0;
				} else {
					return CardType.c111222;
				}
			}
			// 飞机带n双
//			if (card_index.a[2].size() >= 2 && card_index.a[2].size() == len / 5 && card_index.a[2].size() == len / 5
//					&& ((Integer) (card_index.a[2].get(len / 5 - 1)) - (Integer) (card_index.a[2].get(0)) == len / 5 - 1) && len == card_index.a[2].size() * 5) {
//				return CardType.c1112223344;
//			}
		}
		return CardType.c0;
	}

	public static int getLianCount(List<Integer> list) {
		if (list.size() == 1) {
			return 1;
		}
		int maxNear = 0;
		int near = 1;
		for (int i = 0; i < list.size(); i++) {
			if (i + 1 >= list.size()) {
				break;
			}
			int left = list.get(i);
			int right = list.get(i + 1);
			if (right - left == 1) {
				near++;
				if (near > maxNear) {
					maxNear = near;
				}
			} else {
				near = 1;
			}

		}
		return maxNear;
	}

	/**
	 * 返回值
	 * 
	 * @param card
	 * @return
	 */
	public static int getValue(int card) {
		int i = card % 100;
		return i;
	}
	public static List<Integer> toValueList(List<Integer> pokers) {
		List<Integer> values = new ArrayList<>();
		for (int card : pokers) {
			values.add(getValue(card));
		}
		return values;
	}

	/**
	 * 返回花色
	 * 
	 * @param card
	 * @return
	 */
	public static int getColor(int card) {
		return card / 100;
	}

	/**
	 * 得到最大相同数
	 * 
	 * @param card_index
	 * @param list
	 */
	public static void getMax(Card_index card_index, List<Integer> list) {
		int count[] = new int[17];// 1-16各算一种,王算第16种
		for (int i = 0; i < 17; i++) {
			count[i] = 0;
		}
		for (int i = 0, len = list.size(); i < len; i++) {
			if (CardTypeTool.getColor(list.get(i)) == 5) {
				count[16]++;
			} else {
				count[CardTypeTool.getValue(list.get(i)) - 1]++;
			}
		}
		for (int i = 0; i < 17; i++) {
			switch (count[i]) {
			case 1:
				card_index.a[0].add(i + 1);
				break;
			case 2:
				card_index.a[1].add(i + 1);
				break;
			case 3:
				card_index.a[2].add(i + 1);
				break;
			case 4:
				card_index.a[3].add(i + 1);
				break;
			}
		}
	}
	public static Map<Integer, List<Integer>> getSameCard(List<Integer> list) {
		Map<Integer, List<Integer>> sameCard = new HashMap<Integer, List<Integer>>();
		int count[] = new int[17];// 1-16各算一种,王算第16种
		for (int i = 0; i < 17; i++) {
			count[i] = 0;
		}
		for (int i = 0, len = list.size(); i < len; i++) {
			if (CardTypeTool.getColor(list.get(i)) == 5) {
				count[16]++;
			} else {
				count[CardTypeTool.getValue(list.get(i)) - 1]++;
			}
		}
		for (int i = 0; i < 17; i++) {
			if(count[i]>0){
				if(!sameCard.containsKey(count[i])){
					sameCard.put(count[i], new ArrayList<Integer>());
				}
				sameCard.get(count[i]).add(i+1);
			}
		}
		return sameCard;
	}

	/**
	 * 设定牌的顺序
	 * 
	 * @param list
	 */
	public static void setOrder(List<Integer> list) {
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				int a1 = CardTypeTool.getColor(o1);// 花色
				int a2 = CardTypeTool.getColor(o2);
				int b1 = CardTypeTool.getValue(o1);// 数值
				int b2 = CardTypeTool.getValue(o2);
				int flag = 0;
				flag = b2 - b1;
				if (flag == 0) {
					return a2 - a1;
				} else {
					return flag;
				}
			}
		});
	}

	/**
	 * 获取拥有4张一个
	 * 
	 * @param cards
	 * @return
	 */
	public static List<Integer> getBoomCount(List<Integer> cards) {
		setOrder(cards);
		List<Integer> boomCount = new ArrayList<>();
		List<Integer> boomTemp = new ArrayList<>();
		int nowValue = 0;
		for (int card : cards) {
			int value = getValue(card);
			if (nowValue == value) {
				if (!boomTemp.contains(nowValue)) {
					boomTemp.add(nowValue);

				}
				boomTemp.add(value);
			} else {
				boomTemp.clear();
			}
			nowValue = value;
			if (boomTemp.size() == 4) {
				// 4张一样的牌是炸弹
				boomCount.addAll(boomTemp);
			}

		}
		return boomCount;
	}

	/**
	 * 最大的牌
	 * 
	 * @param cards
	 * @return
	 */
	public static int getMax(List<Integer> cards) {
		int max = 0;
		for (int card : cards) {
			int value = getValue(card);
			if (value > getValue(max)) {
				max = card;
			}
		}
		return max;
	}
	/**
	 * 获取炸弹数
	 * @param cards
	 * @return
	 */
	public static int getZaiDanNum(List<Integer> cards){
		int num = 0;
		BbtzModel model = CardTypeTool.getBbtzModel(cards);
		if(CardTypeTool.getValue(model.getCards().get(0)) == 17){
			num++;
		}
		num += CardTypeTool.bomb510kNum(model.getPaiVal());
		num += CardTypeTool.bombNum(model.getSamePai());
		num += CardTypeTool.tongHuaBombNum(model.getColorPai());
		num += CardTypeTool.diBombNum(model.getSamePai());
		return num;
	}
	public static int getBombNum(List<Integer> cards){
		int num = 0;
		BbtzModel model = CardTypeTool.getBbtzModel(cards);
		int bombTianNum = CardTypeTool.getValue(model.getCards().get(0)) == 17 ? 1 : 0;
		int bomb510kNum = CardTypeTool.bomb510kNum(model.getPaiVal());
		int bombNum = CardTypeTool.bombNum(model.getSamePai());
		int bombTongHua = CardTypeTool.tongHuaBombNum(model.getColorPai());
		int bombDiNum = CardTypeTool.diBombNum(model.getSamePai());
		int conflictNum = 0;
		num = bombTianNum + bomb510kNum + bombNum + bombTongHua + bombDiNum;
		if(num >= 2){
			if(bombTianNum == 1){
				return num;
			}
			if(bombNum > 0){
				List<Integer> bombList = model.getSamePai().get(4);
				if(bomb510kNum > 0){
					int _bomb = 0,_bomb510k = 0;
					if(bombList.contains(5)){
						_bomb++;
					}
					if(bombList.contains(10)){
						_bomb++;
					}
					if(bombList.contains(13)){
						_bomb++;
					}
					if(_bomb > 0){
						_bomb510k = bomb510kNum;
					}
				}
				if(bombTongHua > 0 || bombDiNum > 0){
					for(int i : bombList){
						List<Integer> temp = new ArrayList<>(cards);
						List<Integer> rem = Arrays.asList(i+100,i+200,i+300,i+400);
						temp.removeAll(rem);
						BbtzModel tempModel = CardTypeTool.getBbtzModel(temp);
						
					}
				}
			}
		}
		return num;
	}
	/**
	 * 是否同花
	 * @param cards
	 * @return
	 */
	public static boolean isTonghua(List<Integer> cards){
		int color = 0;
		for(int card : cards){
			if(color == 0){
				color = CardTypeTool.getColor(card);
			}else if(CardTypeTool.getColor(card) != color){
				return false;
			}
		}
		return true;
	}
	/**
	 * 比较牌型大小
	 * @param outCards 打出的牌
	 * @param outCardType
	 * @param beforeOut 前一次出的牌
	 * @param table
	 * @return 0 打不起 1 打得起
	 */
	public static int cardTypeCompare(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, BbtzTable table){
		CardType beforeCardType = CardTypeTool.jugdeType(beforeOut, table);
		CardTypeCompare compare = CardTypeCompareFactory.factory.getCompare(beforeCardType);
		int code = 0;
		if(compare != null){
			code = compare.compareTo(outCards, outCardType, beforeOut, table);
		}
		return code;
	}
	/**
	 * 筛选分牌
	 * @param cards
	 * @return
	 */
	public static List<Integer> getScoreCard(List<Integer> cards){
		List<Integer> scoreCard = new ArrayList<>();
		for(int card : cards){
			int val = getValue(card);
			if(val == 5 || val == 10 || val == 13){
				scoreCard.add(card);
			}
		}
		return scoreCard;
	}
	/**
	 * 计算分数
	 * @param cards
	 * @return
	 */
	public static int calcScore(List<Integer> cards){
		int score = 0;
		for(Integer card : cards){
			int val = getValue(card);
			if(val == 5){
				score += 5;
			}else if(val == 10 || val == 13){
				score += 10;
			}
		}
		return score;
	}
	/**
	 * 获得飞机长度
	 * @param cards
	 * @return
	 */
	public static int getPlaneLen(List<Integer> cards){
		Card_index card_index = new Card_index();
		for (int i = 0; i < 4; i++) {
			card_index.a[i] = new Vector<Integer>();
		}
		// 求出各种数字出现频率
		CardTypeTool.getMax(card_index, cards);
		
		List<Integer> threeList = new ArrayList<>();
		threeList.addAll(card_index.a[2]);
		if (!card_index.a[3].isEmpty()) {
			threeList.addAll(card_index.a[3]);	
		}
		Collections.sort(threeList);
		return getLianCount(threeList);
	}
	/**
	 * 统计牌
	 * @param cards
	 * @return
	 */
	public static BbtzModel getBbtzModel(List<Integer> cards){
		BbtzModel model = new BbtzModel();
		if(cards != null && !cards.isEmpty()){
			setOrder(cards);
			model.setCards(cards);
			for(int card : cards){
				int val = getValue(card);
				if(model.getPaiVal().containsKey(val)){
					model.getPaiVal().put(val, model.getPaiVal().get(val) + 1);
				}else{
					model.getPaiVal().put(val, 1);
				}
				int color = getColor(card);
				if(!model.getColorPai().containsKey(color)){
					model.getColorPai().put(color, new ArrayList<Integer>());
				}
				model.getColorPai().get(color).add(val);
			}
			model.setSamePai(CardTypeTool.getSameCard(cards));
		}
		return model;
	}
	/**
	 * 是否打得起牌
	 * @param handCards
	 * @param outCards
	 * @param outCardType
	 * @param table
	 * @return
	 */
	public static int canPlayCompare(List<Integer> handCards, List<Integer> outCards, CardType outCardType, BbtzTable table){
		int code = 0;
		CanPlayCompare compare = CanPlayCompareFactory.factory.getCompare(outCardType);
		if(compare != null){
			BbtzModel model = CardTypeTool.getBbtzModel(handCards);
			code = compare.compareTo(model, outCards, table);
		}
		return code;
	}
	/**
	 * 510k个数
	 * @param paiVal
	 * @return
	 */
	public static int bomb510kNum(Map<Integer, Integer> paiVal){
		//Map<Integer, Integer> paiVal = model.getPaiVal();
		int pai5Num = paiVal.containsKey(5) ? paiVal.get(5) : 0;
		int pai10Num = paiVal.containsKey(10) ? paiVal.get(10) : 0;
		int paiKNum = paiVal.containsKey(13) ? paiVal.get(13) : 0;
		int num = pai5Num;
		if(num > pai10Num){
			num = pai10Num;
		}
		if(num > paiKNum){
			num = paiKNum;
		}
		return num;
	}
	/**
	 * 4炸个数
	 * @param samePai
	 * @return
	 */
	public static int bombNum(Map<Integer, List<Integer>> samePai){
		int num = 0;
		if(samePai.containsKey(4)){
			return samePai.get(4).size();
		}
		return num;
	}
	/**
	 * 同花顺个数
	 * @param colorPai
	 * @return
	 */
	public static int tongHuaBombNum(Map<Integer, List<Integer>> colorPai){
		int num = 0;
		//Map<Integer, List<Integer>> colorPai = model.getColorPai();
		for(Map.Entry<Integer, List<Integer>> entry : colorPai.entrySet()){
			List<Integer> crolorList = entry.getValue();
			if(!crolorList.isEmpty() && crolorList.size() >= 5){
				Collections.sort(crolorList,Collections.reverseOrder()); 
				for(int i=0;i<crolorList.size();i++){
					if(i+5 > crolorList.size()){
						break;
					}
					int var = crolorList.get(i);
					if(var == 15){//2不算
						continue;
					}
					if(var - 4 == crolorList.get(i+4)){
						num++;
						i+=4;
					}
				}
			}
		}
		return num;
	}
	/**
	 * 地炸个数
	 * @param samePai
	 * @return
	 */
	public static int diBombNum(Map<Integer, List<Integer>> samePai){
		int num = 0;
		//Map<Integer, List<Integer>> samePai = model.getSamePai();
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
		if(!cards.isEmpty() && cards.size() >= 4){
			Collections.sort(cards,Collections.reverseOrder()); 
			for(int i=0;i<cards.size();i++){
				if(i+4 > cards.size()){
					break;
				}
				int var = cards.get(i);
				if(var == 15){//2不算
					continue;
				}
				if(var - 3 == cards.get(i+3)){
					num++;
					i+=3;
				}
			}
		}
		return num;
	}

    /**
     * 别人打出牌，计算接牌
     * @param paiList
     * @param oppo
     * @param nextDan
     * @param table
     * @return
     */
    public static List<Integer> calcAutoJiePai(List<Integer> paiList, List<Integer> oppo, boolean nextDan, BbtzTable table) {
        if (paiList == null || paiList.size() == 0) {
            return Collections.emptyList();
        }
        List<Integer> retList = new ArrayList<>();
        List<Integer> selfPaiList = new ArrayList<>(paiList);
        List<List<Integer>> boomList = removeAllBoom(selfPaiList);
        if(nextDan && boomList.size() > 0){
            // 对方报单，优先出最大的牌
            for (int i = 0; i <boomList.size(); i++) {
                List<Integer> list = boomList.get(i);
                CardType cardType = CardTypeTool.jugdeType(oppo, table);
                int isLet = CardTypeTool.canPlayCompare(list, oppo, cardType, table);
                if (isLet == 1) {
                    retList = list;
                    return retList;
                }
            }
        }
        if (selfPaiList.size() > 0) {
            CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(oppo), 0);
            if (result.getType() > 0) {
                List<CardValue> cardValueList = CardUtils.searchBiggerCardValues(CardUtils.loadCards(selfPaiList), result);
                if (cardValueList != null && cardValueList.size() > 0) {
                    result = CardUtils.calcCardValue(cardValueList, 0);
                    if ((result.getType() == 11 || result.getType() == 22 || result.getType() == 33) && result.getMax() >= 15) {
                        return retList;
                    } else {
                        retList = CardUtils.loadCardIds(cardValueList);
                    }
                }
            }
        }
        if (retList.size() > 0) {
            CardType cardType = CardTypeTool.jugdeType(oppo, table);
            int isLet = CardTypeTool.canPlayCompare(retList, oppo, cardType, table);
            if (isLet != 1) {
                retList = Collections.emptyList();
            }
        }
        if (retList.size() == 0 && boomList.size() > 0) {
            for (int i = boomList.size() - 1; i >= 0; i--) {
                List<Integer> list = boomList.get(i);
                CardType cardType = CardTypeTool.jugdeType(oppo, table);
                int isLet = CardTypeTool.canPlayCompare(list, oppo, cardType, table);
                if (isLet == 1) {
                    retList = list;
                    break;
                }
            }
        }
        return retList;
    }

    /**
     * 自动出牌
     *
     * @param paiList
     * @param nextDan
     * @param table
     * @return
     */
    public static List<Integer> calcAutoChuPai(List<Integer> paiList, boolean nextDan, BbtzTable table) {
        List<Integer> curList = new ArrayList<>(paiList);
        List<List<Integer>> boomList = removeAllBoom(curList);

        if (curList.size() > 0) {
            if (curList == null || curList.size() == 0) {
                return Collections.emptyList();
            }

            List<Integer> retList = new ArrayList<>();
            Map<Integer, Integer> map = CardTool.loadCards(curList);
            int val = 0;
            int count = map.size();
            if (count == 1) {
                retList.addAll(curList);
                return retList;
            }

            int size = curList.size();
            switch (size) {
                case 2:
                    if (nextDan) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    } else {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            break;
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    }
                    break;
                case 3:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 2) {
                            val = kv.getKey().intValue();
                            retList.addAll(CardTool.loadCards(curList, val));
                            break;
                        }
                    }
                    if (retList.size() == 0) {
                        if (nextDan) {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        } else {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                                break;
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    }
                    break;
                case 4:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            retList.addAll(curList);
                            break;
                        }
                    }
                    if (retList.size() == 0) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                case 5:
                    if (count == 2) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 3) {
                                retList.addAll(curList);
                                break;
                            } else if (kv.getValue().intValue() == 4) {
                                if (nextDan) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                }
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                val = kv.getKey().intValue();
                                break;
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    } else if (count == 5) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }

                        if (isShun) {
                            retList.addAll(curList);
                        } else {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                if (kv.getValue().intValue() == 2) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                    break;
                                }
                            }
                            if (retList.size() == 0) {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                default:
                    if (count == size) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    } else if (count * 2 == size) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() != 2) {
                                isShun = false;
                                break;
                            }
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    }

                    val = 0;
                    if (retList.size() == 0) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            int c = kv.getValue().intValue();
                            if (c == 3) {
                                val = kv.getKey().intValue();
                                break;
                            }
                        }
                        if (val > 0) {
                            List<Integer> daiValues = new ArrayList<>();
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                int c = kv.getValue().intValue();
                                int val0 = kv.getKey().intValue();
                                if (c == 1) {
                                    daiValues.add(val0);
                                }
                            }
                            if (daiValues.size() >= 2) {
                                retList.addAll(CardTool.loadCards(curList, val));
                                retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    int c = kv.getValue().intValue();
                                    int val0 = kv.getKey().intValue();
                                    if (c == 2) {
                                        daiValues.add(val0);
                                    }
                                }
                                if (daiValues.size() >= 2) {
                                    retList.addAll(CardTool.loadCards(curList, val));
                                    retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                    retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        int c = kv.getValue().intValue();
                                        int val0 = kv.getKey().intValue();
                                        if (c == 3 && val != val0) {
                                            daiValues.add(val0);
                                        }
                                    }

                                    if (daiValues.size() < 2) {
                                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                            int c = kv.getValue().intValue();
                                            int val0 = kv.getKey().intValue();
                                            if (c == 4) {
                                                retList = CardTool.loadCards(curList, val0);
                                            }
                                        }
                                    }

                                    if (retList.size() == 0) {
                                        retList.addAll(CardTool.loadCards(curList, val));

                                        if (daiValues.size() > 1) {
                                            retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
                                            if (daiValues.size() > 2) {
                                                retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (retList.size() == 0) {
                            val = 0;
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                int c = kv.getValue().intValue();
                                if (c == 2) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                            }
                            if (val == 0) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    int c = kv.getValue().intValue();
                                    if (c == 3) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                }
                            }
                            if (val > 0) {
                                List<Integer> list0 = CardTool.loadCards(curList, val);
                                retList.add(list0.get(0));
                                retList.add(list0.get(1));
                            } else {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        if (kv.getValue().intValue() == 4) {
                                            val = kv.getKey().intValue();
                                            retList.addAll(CardTool.loadCards(curList, val));
                                            break;
                                        } else {
                                            val = kv.getKey().intValue();
                                        }
                                    }
                                    if (retList.size() == 0) {
                                        retList.add(CardTool.loadCards(curList, val).get(0));
                                    }
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    }
            }
            return retList;
        } else {
            if (boomList.size() > 0) {
                return boomList.get(boomList.size() - 1);
            } else {
                return null;
            }
        }
    }

    /**
     * 找到并移除天炸
     *
     * @param paiList
     * @return
     */
    public static List<List<Integer>> removeTianZha(List<Integer> paiList) {
        List<List<Integer>> res = new ArrayList<>();
        if (paiList == null || paiList.size() == 0) {
            return res;
        }

        Iterator<Integer> iterator = paiList.iterator();
        while (iterator.hasNext()) {
            Integer pai = iterator.next();
            int val = CardTool.loadCardValue(pai);
            if (val == 17) {
                List<Integer> tianZha = new ArrayList<>();
                tianZha.add(pai);
                res.add(tianZha);
                iterator.remove();
            }
        }
        return res;
    }

    /**
     * 找到并移除地炸
     *
     * @param paiList
     * @return
     */
    public static List<List<Integer>> removeDiZha(List<Integer> paiList) {
        List<List<Integer>> res = new ArrayList<>();
        if (paiList == null || paiList.size() == 0 || paiList.size() < 8) {
            return res;
        }
        // 找出两张以上的牌
        int[] cardArr = new int[18];
        int count = 0;
        int paiVal;
        for (Integer pai : paiList) {
            paiVal = CardTool.loadCardValue(pai);
            cardArr[paiVal]++;
            if (cardArr[paiVal] == 2) {
                count++;
            }
        }
        if (count < 4) {
            return res;
        }
        List<Integer> cards = new ArrayList<>();
        for (paiVal = 0; paiVal < cardArr.length; paiVal++) {
            if (cardArr[paiVal] >= 2 && paiVal != 15) {
                cards.add(paiVal);
            }
        }
        // 倒序
        Collections.sort(cards, Collections.reverseOrder());

        List<Integer> diZha = new ArrayList<>();
        List<int[]> arrList = new ArrayList<>();
        List<Integer> cards4 = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            paiVal = cards.get(i);
            if (diZha.size() == 0) {
                diZha.add(paiVal);
                continue;
            }
            if (diZha.get(diZha.size() - 1) == paiVal + 1) {
                // 上一张牌比当前牌大一
                diZha.add(paiVal);
                if (i == cards.size() - 1 && diZha.size() >= 4) {
                    // 取到最后一张，且能组成地炸
                    int[] valArr = new int[18];
                    for (Integer val : diZha) {
                        valArr[val] += 2;
                    }
                    arrList.add(valArr);
                    diZha.clear();
                }
            } else {
                if (diZha.size() >= 4) {
                    int[] valArr = new int[18];
                    for (Integer val : diZha) {
                        valArr[val] += 2;
                    }
                    arrList.add(valArr);
                }
                diZha.clear();
                diZha.add(paiVal);
            }
            if (cardArr[paiVal] == 4) {
                // 有4张的牌
                cards4.add(paiVal);
            }
        }
        diZha.clear();
        // 4张抽出两张后，剩下的牌，再计算地炸
        if (cards4.size() > 4) {
            for (int i = 0; i < cards4.size(); i++) {
                paiVal = cards4.get(i);
                if (diZha.size() == 0) {
                    diZha.add(paiVal);
                    continue;
                }
                if (diZha.get(diZha.size() - 1) == paiVal + 1) {
                    // 上一张牌比当前牌大一
                    diZha.add(paiVal);
                    if (i == cards4.size() - 1 && diZha.size() >= 4) {
                        // 取到最后一张，且能组成地炸
                        int[] valArr = new int[18];
                        for (Integer val : diZha) {
                            valArr[val] += 2;
                        }
                        arrList.add(valArr);
                        diZha.clear();
                    }
                } else {
                    if (diZha.size() >= 4) {
                        int[] valArr = new int[18];
                        for (Integer val : diZha) {
                            valArr[val] += 2;
                        }
                        arrList.add(valArr);
                    }
                    diZha.clear();
                    diZha.add(paiVal);
                }
            }
        }

        if (arrList.size() == 0) {
            return res;
        }

        // 从手牌中找出，并移除
        for (int i = 0; i < arrList.size(); i++) {
            res.add(new ArrayList<>());
        }
        Iterator<Integer> iterator = paiList.iterator();
        while (iterator.hasNext()) {
            int pai = iterator.next();
            paiVal = CardTool.loadCardValue(pai);
            for (int i = 0, length = arrList.size(); i < length; i++) {
                int[] diZhaArr = arrList.get(i);
                if (diZhaArr[paiVal] > 0) {
                    diZhaArr[paiVal]--;
                    res.get(i).add(pai);
                    iterator.remove();
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 找到并移除同花
     *
     * @param paiList
     * @return
     */
    public static List<List<Integer>> removeTongHua(List<Integer> paiList) {
        List<List<Integer>> res = new ArrayList<>();
        if (paiList == null || paiList.size() == 0 || paiList.size() < 5) {
            return res;
        }
        // 倒序
        Collections.sort(paiList, Collections.reverseOrder());

        List<Integer> tongHua = new ArrayList<>();
        List<List<Integer>> tongHuaList = new ArrayList<>();
        int pai;
        for (int i = 0; i < paiList.size(); i++) {
            pai = paiList.get(i);
            if (CardTool.loadCardValue(pai) == 15) {//2不算
                continue;
            }
            if (tongHua.size() == 0) {
                tongHua.add(pai);
                continue;
            }
            // 上一张牌比当前牌大一
            if (tongHua.get(tongHua.size() - 1) == pai + 1) {
                tongHua.add(pai);
                if (i == paiList.size() - 1 && tongHua.size() >= 5) {
                    // 取到最后一张，且能组成同花
                    tongHuaList.add(new ArrayList<>(tongHua));
                    tongHua.clear();
                }
            } else {
                if (tongHua.size() >= 5) {
                    tongHuaList.add(new ArrayList<>(tongHua));
                }
                tongHua.clear();
                tongHua.add(pai);
            }
        }

        if (tongHuaList.size() == 0) {
            return res;
        }

        // 从手牌中找出，并移除
        for (int i = 0; i < tongHuaList.size(); i++) {
            res.add(new ArrayList<>());
        }
        Iterator<Integer> iterator = paiList.iterator();
        while (iterator.hasNext()) {
            pai = iterator.next();
            for (int i = 0, length = tongHuaList.size(); i < length; i++) {
                if (tongHuaList.get(i).contains(pai)) {
                    res.get(i).add(pai);
                    iterator.remove();
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 找到并移除4炸
     *
     * @param paiList
     * @return
     */
    public static List<List<Integer>> remove4Zha(List<Integer> paiList) {
        List<List<Integer>> res = new ArrayList<>();
        if (paiList == null || paiList.size() == 0 || paiList.size() < 4) {
            return res;
        }
        // 倒序
        Collections.sort(paiList, Collections.reverseOrder());

        int[] cardArr = new int[18];
        Map<Integer, List<Integer>> val2PaiMap = new HashMap<>();
        List<List<Integer>> zhaList = new ArrayList<>();
        for (Integer pai : paiList) {
            int paiVal = CardTool.loadCardValue(pai);
            cardArr[paiVal]++;

            List<Integer> pais = val2PaiMap.get(paiVal);
            if (pais == null) {
                pais = new ArrayList<>();
                val2PaiMap.put(paiVal, pais);
            }
            pais.add(pai);

            if (cardArr[paiVal] == 4) {
                zhaList.add(pais);
            }
        }
        if (zhaList.size() == 0) {
            return res;
        }

        // 从手牌中找出，并移除
        for (int i = 0; i < zhaList.size(); i++) {
            res.add(new ArrayList<>());
        }
        Iterator<Integer> iterator = paiList.iterator();
        while (iterator.hasNext()) {
            Integer pai = iterator.next();
            for (int i = 0, length = zhaList.size(); i < length; i++) {
                List<Integer> zha = zhaList.get(i);
                if (zha.contains(pai)) {
                    res.get(i).add(pai);
                    iterator.remove();
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 找到并移除510K
     *
     * @param paiList
     * @return
     */
    public static List<List<Integer>> remove510K(List<Integer> paiList) {
        List<List<Integer>> res = new ArrayList<>();
        if (paiList == null || paiList.size() == 0 || paiList.size() < 3) {
            return res;
        }
        // 倒序
        Collections.sort(paiList, Collections.reverseOrder());

        List<List<Integer>> pai510kList = new ArrayList<>();
        List<Integer> list510K = new ArrayList<>();
        List<Integer> remainPaiList = new ArrayList<>();

        // 先取同花色510K
        int curHuaSe = 4;
        int[] remain510KArr = new int[14];
        int paiVal;
        int pai;
        for (int i = 0, length = paiList.size(); i < length; i++) {
            pai = paiList.get(i);
            paiVal = CardTool.loadCardValue(pai);
            if (paiVal != 5 && paiVal != 10 && paiVal != 13) {
                continue;
            }
            int paiHuaSe = CardTool.loadCardHuaSe(pai);
            if (list510K.size() == 0) {
                list510K.add(pai);
                curHuaSe = paiHuaSe;
                continue;
            }

            if (paiHuaSe != curHuaSe) {
                // 换花色了
                for (Integer tmp : list510K) {
                    paiVal = CardTool.loadCardValue(tmp);
                    remain510KArr[paiVal]++;
                }
                remainPaiList.addAll(list510K);
                curHuaSe = paiHuaSe;
                list510K.clear();
            }
            list510K.add(pai);
            if (list510K.size() == 3) {
                // 做出正五10K
                pai510kList.add(new ArrayList<>(list510K));
                list510K.clear();
            }
        }
        if (list510K.size() > 0) {
            for (Integer tmp : list510K) {
                paiVal = CardTool.loadCardValue(tmp);
                remain510KArr[paiVal]++;
            }
            remainPaiList.addAll(list510K);
            list510K.clear();
        }
        if (pai510kList.size() > 0) {
            // 从手牌中找出，并移除
            for (int i = 0; i < pai510kList.size(); i++) {
                res.add(new ArrayList<>());
            }
            Iterator<Integer> iterator = paiList.iterator();
            while (iterator.hasNext()) {
                pai = iterator.next();
                paiVal = CardTool.loadCardValue(pai);
                if (paiVal != 5 && paiVal != 10 && paiVal != 13) {
                    continue;
                }
                for (int i = 0, length = pai510kList.size(); i < length; i++) {
                    List<Integer> zha = pai510kList.get(i);
                    if (zha.contains(pai)) {
                        res.get(i).add(pai);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        //剩余牌再做510K
        List<int[]> val510KList = new ArrayList<>();
        while (remain510KArr[5] > 0 && remain510KArr[10] > 0 && remain510KArr[13] > 0) {
            remain510KArr[5]--;
            remain510KArr[10]--;
            remain510KArr[13]--;
            int[] tmp = new int[14];
            tmp[13] = 1;
            tmp[10] = 1;
            tmp[5] = 1;
            val510KList.add(tmp);
        }
        if (val510KList.size() > 0) {
            int preSize = res.size();
            for (int i = 0; i < val510KList.size(); i++) {
                res.add(new ArrayList<>());
            }
            Iterator<Integer> iterator = paiList.iterator();
            while (iterator.hasNext()) {
                pai = iterator.next();
                paiVal = CardTool.loadCardValue(pai);
                if (paiVal != 5 && paiVal != 10 && paiVal != 13) {
                    continue;
                }
                for (int i = 0, length = val510KList.size(); i < length; i++) {
                    int[] diZhaArr = val510KList.get(i);
                    if (diZhaArr[paiVal] > 0) {
                        diZhaArr[paiVal]--;
                        res.get(i + preSize).add(pai);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return res;
    }

    public static List<List<Integer>> removeAllBoom(List<Integer> paiIdList){
        List<List<Integer>> res = new ArrayList<>();
        res.addAll(removeTianZha(paiIdList));
        res.addAll(removeDiZha(paiIdList));
        res.addAll(removeTongHua(paiIdList));
        res.addAll(remove4Zha(paiIdList));
        res.addAll(remove510K(paiIdList));
        return res;
    }


    public static void main(String[] args) {

        String str = "517,414,314,214,114,413,313,213,113,412,312,212,112,411,311,211,111,410,310,409,309,408,308,407,307,406,306,210,209,208,207,206,204,203,403,303,103";
        str = "413,410,409,405,310,305,213,210,113,105";
        String[] paiStrArr = str.split(",");
        List<Integer> pais = new ArrayList<>();
        for (String paiStr : paiStrArr) {
            pais.add(Integer.valueOf(paiStr));
        }
        List<List<Integer>> tianZha = removeTianZha(pais);
        List<List<Integer>> diZha = removeDiZha(pais);
        List<List<Integer>> tongHua = removeTongHua(pais);
        List<List<Integer>> zha = remove4Zha(pais);
        List<List<Integer>> list510K = remove510K(pais);
        System.out.println();

    }


}
