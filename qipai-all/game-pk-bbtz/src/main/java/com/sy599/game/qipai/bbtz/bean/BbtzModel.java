package com.sy599.game.qipai.bbtz.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BbtzModel {

	/* 牌集合 */
	private List<Integer> cards;
	/* 统计 <牌值，牌个数> */
	private Map<Integer, Integer> paiVal = new HashMap<Integer, Integer>();
	/* 统计 <牌数量，牌值> */
	private Map<Integer, List<Integer>> samePai = new HashMap<Integer, List<Integer>>();
	/* 统计 <花色，牌值> */
	private Map<Integer, List<Integer>> colorPai = new HashMap<Integer, List<Integer>>();
	
	public Map<Integer, List<Integer>> getColorPai() {
		return colorPai;
	}
	public void setColorPai(Map<Integer, List<Integer>> colorPai) {
		this.colorPai = colorPai;
	}
	public Map<Integer, List<Integer>> getSamePai() {
		return samePai;
	}
	public void setSamePai(Map<Integer, List<Integer>> samePai) {
		this.samePai = samePai;
	}
	public List<Integer> getCards() {
		return cards;
	}
	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}
	public Map<Integer, Integer> getPaiVal() {
		return paiVal;
	}
	public void setPaiVal(Map<Integer, Integer> paiVal) {
		this.paiVal = paiVal;
	}
}
