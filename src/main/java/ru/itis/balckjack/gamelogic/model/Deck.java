package ru.itis.balckjack.gamelogic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Integer> cards;

    public Deck() {
        cards = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            cards.add(i);
        }
        Collections.shuffle(cards);
    }

    public int drawCard() {
        return cards.remove(0);
    }
}
