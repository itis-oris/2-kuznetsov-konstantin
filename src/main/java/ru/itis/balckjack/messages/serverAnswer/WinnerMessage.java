package ru.itis.balckjack.messages.serverAnswer;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class WinnerMessage extends Message {
    private final int playerID;
    private final int balance;

    public WinnerMessage(int playerID, int balance) {
        super(MessageType.WINNER);
        this.playerID = playerID;
        this.balance = balance;
    }


    @Override
    public String toMessageString() {
        return "type:%s;attributes:{playerID:%s;balance:%s}".formatted(
                getType().getId(),
                playerID,
                balance
        );
    }
}
