package com.sy599.game.qipai.doudizhu.rule;

public enum CardType {
	/*** 单牌 */
	c1(1),
	/*** 对子 */
	c2(2),
	/*** 3不带 */
	c3(3),
	/*** 炸弹 */
	c4(4),
	/*** 3带1 */
	c31(5),
	/*** 3带2 */
	c32(6),
	/*** 4带2 */
	c411(7),
	/*** 4带2对 */
	c422(8),
	/*** 连子 */
	c123(9),
	/*** 连对。 */
	c1122(10),
	/*** 飞机。 */
	c111222(11),
	/*** 飞机带单牌. */
	c11122234(12),
	/*** 飞机带对子. */
	c1112223344(13),
	/*** 癞子炸弹*/
	c666(14),
	/*** 王炸*/
	c1617(20),
	/*** 不能出牌 */
	c0(0);
	
	private int type;
	
	private CardType(int type) {
		this.type=type;
	}

	public int getType() {
		return type;
	}
	
}
