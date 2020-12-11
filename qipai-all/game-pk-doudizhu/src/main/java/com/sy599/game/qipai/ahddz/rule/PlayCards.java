package com.sy599.game.qipai.ahddz.rule;

import java.util.List;

public class PlayCards {
    CardType cardType;
    List<Integer> cards;

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }
}
