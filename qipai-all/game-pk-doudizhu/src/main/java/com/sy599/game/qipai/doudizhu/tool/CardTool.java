package com.sy599.game.qipai.doudizhu.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.doudizhu.constant.DdzConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author hyz
 * 
 */
public class CardTool {
	/**
	 * 发给定牌集
	 * @param cardIds
	 * @param zps
	 */
	public static synchronized List<List<Integer>> fapai(List<Integer> cardIds, int playerCount, List<List<Integer>> zps) {
		List<List<Integer>> list = new ArrayList<>();

		if(cardIds == null || cardIds.isEmpty()) {
			return list;
		}
		
		List<Integer> copy = null;
		copy = new ArrayList<>(cardIds);

		int maxCount = 0;
		maxCount = (copy.size() - 3) / playerCount;
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

		return list;
	}
	
	/**
	 * @param playerCount
	 *            人数
	 * @param playType
	 * @param zps
	 * @param checkBoom
	 * @return
	 */
	public static synchronized List<List<Integer>> fapai(int playerCount, int playType, List<List<Integer>> zps, boolean checkBoom) {
		List<List<Integer>> list = new ArrayList<>();

		List<Integer> copy = null;
		if (playType == DdzConstants.ddz_three || playType == DdzConstants.ddz_three_niggle) {
			copy = new ArrayList<>(DdzConstants.cardList_16);
		} else {
		}
		Collections.shuffle(copy);

		int maxCount = (copy.size() - 3) / playerCount;
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

		// 检查发牌里面的炸弹
		List<Integer> boomList = checkBoom(list);
		if (checkBoom && !boomList.isEmpty()) {
			if (Math.random() > 0.4) {
				return fapai(playerCount, playType, zps, false);

			}
		}
		return list;
	}

	public static synchronized List<List<Integer>> fapai(int playerCount, int playType, List<List<Integer>> zps) {
		return fapai(playerCount, playType, zps, true);
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
					int paiVal=card;
					if (paiVal == zpId) {
						pai.add(card);
						iterator.remove();
//						break;
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

	public static void main(String[] args) {
		int boomCount = 0;
		int totalCount = 0;
		for (int i = 0; i < 100000; i++) {
			List<List<Integer>> list = fapai(3, 16, null);
			List<Integer> boomList = checkBoom(list);
			if (!boomList.isEmpty()) {
				boomCount++;
				totalCount += boomList.size() / 4;
				System.out.println("boom:" + boomCount + " totalCount:" + totalCount + " " + boomList);
			}

		}
		System.out.println(fapai(3, 16, null));
	}

	public static List<Integer> getValue(List<Integer> copy) {
		if(copy == null || copy.isEmpty()) {
			return new ArrayList<Integer>();
		}
		List<Integer> list = new ArrayList<>();
		for(int card : copy) {
			list.add(card % 100);
		}
		return list;
	}
}
