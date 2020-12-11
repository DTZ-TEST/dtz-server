package com.sy599.game.qipai.bbtz.compare;

import com.sy599.game.qipai.bbtz.bean.BbtzModel;

import java.util.List;

public interface CanPlayCompare {

	/**
	 * 比较手牌与出的牌
	 * @param model
	 * @param outCards
	 * @param param
	 * @return
	 */
	int compareTo(BbtzModel model, List<Integer> outCards, Object... param);
	
}
