package ru.itis.balckjack.gamelogic.model;


import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.util.ArrayList;

@Setter
@Getter
public class Player {
    private int id;

    private Integer bet = null;
    private int balance;
    private Socket socket;
    private ArrayList<Integer> hand;

    public Player(int id, int balance, Socket socket) {
        this.id = id;
        this.balance = balance;
        this.socket = socket;
        hand = new ArrayList<>();
    }

    public int score() {
        int score = 0;
        int aces = 0;
        for (int cardID : hand) {
            int rank = (cardID / 4) + 2; // Получаем номинал карты (2-14, где 14 - туз)

            if (rank >= 2 && rank <= 10) {
                score += rank;
            } else if (rank >= 11 && rank <= 13) { // Валет, дама, король
                score += 10;
            } else { // Туз (rank == 14)
                aces++;
                score += 11;
            }
        }
        // Корректируем тузы, если перебор
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    public void addCard(int cardID) {
        hand.add(cardID);
    }

    public void reduceBalance() {
        balance -= bet;
    }

    public void increaseBalance() {
        balance += bet * 2;
    }

    public void saveBalance() {
        balance += bet;
    }
}
