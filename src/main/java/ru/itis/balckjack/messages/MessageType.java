package ru.itis.balckjack.messages;

import lombok.Getter;

@Getter
public enum MessageType {
    CONNECTED(0), //Клиент подает при подключении
    CONNECTIONACCEPTED(1), //  <currentPlayerID> [otherPlayerID] //Сервер отвечает при подключении (посылается всем)
    BET(2), // <playerID> <amount> //Ставка игрока
    BETACCEPTED(3), // <betPlayerID> <amount> //Принятие ставки другого игрока (посылается всем)
    DEALERFIRSTCARD(4), // <cardID> //Первая карта дилера, визуально показывается вторая закрытая
    RECEIVEDCARD(5), // <playerID> <cardID> //Сервер посылает карту на руку конкретному игроку (показывается всем игрокам)
    REQUESTCARD(6), // <playerID> //Клиент просит карту на руку
    ENDMOVE(7), // <playerID> //Закончить свой ход
    DEALERCARD(8), // <cardID> //Дилер в конце выдает себе карты, пока не станет больше 17
    WINNER(9), // <playerID> <balance> //для каждого победившего игрока просчитывается его баланс, и что он победил (посылается всем)
    NEWGAME(10), //Новая игра
    LOOSER(11),
    NEWGAMEREQUEST(12),
    RESTART(13);

    private final int id;

    MessageType(int id) {
        this.id = id;
    }

    public static MessageType fromId(int id) {
        for (MessageType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MessageType ID: " + id);
    }
}
