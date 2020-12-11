package com.sy599.game.qipai.bbtz.compare;

import com.sy599.game.qipai.bbtz.rule.CardType;

import java.util.List;

public interface CardTypeCompare {

	/**
	 * 比较前一次出的牌
	 * @param outCards
	 * @param outCardType
	 * @param beforeOut
	 * @param param 0 桌子 1飞机长度
	 * @return
	 */
	int compareTo(List<Integer> outCards, CardType outCardType, List<Integer> beforeOut, Object... param);
}
