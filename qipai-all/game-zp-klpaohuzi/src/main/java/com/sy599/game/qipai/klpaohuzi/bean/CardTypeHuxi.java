package com.sy599.game.qipai.klpaohuzi.bean;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.klpaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.klpaohuzi.tool.PaohuziTool;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CardTypeHuxi {
	private int action;
	private List<Integer> cardIds;
	private int hux;

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public List<Integer> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<Integer> cardIds) {
		this.cardIds = cardIds;
	}

	public int getHux() {
		return hux;
	}

	public void setHux(int hux) {
		this.hux = hux;
	}

	public boolean isHasCard(PaohzCard card) {
		return cardIds != null && cardIds.contains(card.getId());
	}

	public void init(String data) {
		if (!StringUtils.isBlank(data)) {
			String[] values = data.split("_");
			action = StringUtil.getIntValue(values, 0);
			String cards = StringUtil.getValue(values, 1);
			if (!StringUtils.isBlank(cards)) {
				cardIds = StringUtil.explodeToIntList(cards);
			}
			hux = StringUtil.getIntValue(values, 2);
		}
	}

	public String toStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(action).append("_");
		sb.append(StringUtil.implode(cardIds)).append("_");
		sb.append(hux).append("_");
		return sb.toString();
	}

	public Builder buildMsg() {
		return buildMsg(false);
	}

	public Builder buildMsg(boolean hideCards) {
		Builder msg = PhzHuCards.newBuilder();
		if (hideCards) {
			msg.addAllCards(PaohuziTool.toPhzCardZeroIds(cardIds));
		} else {
			msg.addAllCards(cardIds);
		}
		msg.setAction(action);
		msg.setHuxi(hux);
		return msg;
	}
}
