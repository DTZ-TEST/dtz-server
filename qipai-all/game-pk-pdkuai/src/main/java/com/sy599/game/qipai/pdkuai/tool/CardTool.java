package com.sy599.game.qipai.pdkuai.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.pdkuai.constant.PdkConstants;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.util.GameUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author lc
 * 
 */
public final class CardTool {
	/**
	 * @param playerCount
	 *            人数
	 * @param playType
	 *            玩法 15和16张
	 * @param zps
	 * @param checkBoom
	 * @return
	 */
	public static List<List<Integer>> fapai(int playerCount, int playType, List<List<Integer>> zps, boolean checkBoom) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy;
		if (playType == GameUtil.play_type_15) {
			copy = new ArrayList<>(PdkConstants.cardList_15);
		} else {
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}

		Collections.shuffle(copy);

		int maxCount = copy.size() / 3;
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

		int j=0;
		int flag=0;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
				if(card==403)
					flag=j;
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
		}
		list.add(pai);
		//若黑桃三在抽牌中，则改变位置
		if(flag==2&&playerCount==2){
			List<List<Integer>> list1 = new ArrayList<>();
			int seat = new Random().nextInt(2);
			if (seat==0){
				list1.add(list.get(2));
				list1.add(list.get(0));
				list1.add(list.get(1));
			}else {
				list1.add(list.get(0));
				list1.add(list.get(2));
				list1.add(list.get(1));
			}
			list=list1;
		}


        // 决定发牌里面的炸弹
        if (checkBoom && (zps == null || zps.size() == 0)) {
            int boomCount = checkBoom(list);
            int temp;
            if (boomCount == 0) {
                temp = -1;
            } else if (boomCount == 1) {
                temp = 53;
            } else if (boomCount == 2) {
                temp = 70;
            } else {
                temp = 0;
            }
            if (temp >= 0 && new SecureRandom().nextInt(100) <= temp) {
                return fapai(playerCount, playType, zps, true);
            }
        }
		return list;
	}

	public static List<List<Integer>> fapaiNoBoom(int playerCount, int playType, List<List<Integer>> zps) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy;
		if (playType == GameUtil.play_type_15) {
			copy = new ArrayList<>(PdkConstants.cardList_15);
		} else {
			copy = new ArrayList<>(PdkConstants.cardList_16);
		}

		Collections.shuffle(copy);

		int maxCount = copy.size() / 3;
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

		int j=0;
		int flag=0;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
				if(card==403)
					flag=j;
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
		}
		list.add(pai);
		//若黑桃三在抽牌中，则改变位置
		if(flag==2&&playerCount==2){
			List<List<Integer>> list1 = new ArrayList<>();
			int seat = new Random().nextInt(2);
			if (seat==0){
				list1.add(list.get(2));
				list1.add(list.get(0));
				list1.add(list.get(1));
			}else {
				list1.add(list.get(0));
				list1.add(list.get(2));
				list1.add(list.get(1));
			}
			list=list1;
		}
		int boomCount = checkBoom(list);
		if(boomCount>0){
			return fapaiNoBoom(playerCount, playType, zps);
		}
		return list;
	}


	public static List<List<Integer>> fapai(int playerCount, int playType, List<List<Integer>> zps) {
		return fapai(playerCount, playType, zps, true);
	}

	private static int checkBoom(List<List<Integer>> lists) {
		int count = 0;
		for (List<Integer> list : lists) {
			Map<Integer,Integer> map = CardUtils.countValue(CardUtils.loadCards(list));
			for (Map.Entry<Integer,Integer> kv : map.entrySet()){
				if (kv.getValue().intValue()>=4){
					count++;
				}
			}
		}
		return count;

	}

    public static int loadCardValue(int id){
	    return id%100;
    }

    public static List<Integer> loadCards(List<Integer> list,int val){
	    List<Integer> ret = new ArrayList<>(4);
	    for (Integer integer:list){
            if (val==loadCardValue(integer.intValue())){
                ret.add(integer);
            }
        }
        return ret;
    }

    public static Map<Integer,Integer> loadCards(List<Integer> list){
        Map<Integer,Integer> map = new TreeMap<>();
        for (Integer integer:list){
            int val = loadCardValue(integer.intValue());
            int count = map.getOrDefault(val,0);
            count++;
            map.put(val,count);
        }
        return map;
    }

	public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
		List<Integer> pai = new ArrayList<>();
		if (!vals.isEmpty()) {
			int i = 1;
			for (int zpId : vals) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					int paiVal=card % 100;
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

    public static void main(String args[]) {
	    int count = 1000000;
	    int playerCount = 3;
//        calcBookRate(count,playerCount,false);
        calcBookRate(count,playerCount,true);
    }

    public static void calcBookRate(int count, int playerCount, boolean checkBoom) {
        Map<Integer, Integer> map = new HashMap<>();
        List<List<Integer>> list;
        for (int i = 0; i < count; i++) {
            list = fapai(playerCount, GameUtil.play_type_15, null, checkBoom);
            int boomCount = checkBoom(list);
            if (map.containsKey(boomCount)) {
                map.put(boomCount, map.get(boomCount) + 1);
            } else {
                map.put(boomCount, 1);
            }
        }
        System.out.println("统计总次数：" + count + ", " + playerCount + " 人玩法," + (checkBoom ? "炸弹限制开关：开启" : "炸弹限制：关闭"));
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() > 0) {
                System.out.println("炸蛋数：" + entry.getKey()+ ",概率：" + new BigDecimal(entry.getValue() * 1d / count).multiply(new BigDecimal(100)).setScale(4, RoundingMode.FLOOR) + " ,次数：" + entry.getValue() );
            }
        }
    }

}
