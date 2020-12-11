package com.sy599.game.qipai.bbtz.rule;

public enum CardType {
	/*** 单牌 */
	c1(1),
	/*** 对子 */
	c2(2),
	/*** 3张或3带12 */
	c32(3),
	/*** 4带3 */
	c43(4),
	/*** 连子 */
	c123(5),
	/*** 连队。 */
	c1122(6),
	/*** 飞机。 */
	c111222(7),
	/*** 510K */
	c510k(8),
	/*** 炸弹 */
	c4(9),
	/*** 同花顺 */
	c12345(10),
	/*** 地炸 */
	c11223344(11),
	/*** 天炸 */
	c517(12),
	/*** 不能出牌 */
	c0(0);
	
	private int type;
	
	private CardType(int type) {
		this.type=type;
	}
	
	public int getType() {
		return type;
	}


    public static CardType getCardType(int type){
        CardType[] values = CardType.values();
        for(CardType value : values){
            if(value.getType() == type){
                return value;
            }
        }
        return c0;
    }
	
}
