package ru.itis.balckjack.messages.clientQuery;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class BetMessage extends Message {
    private final int playerID;
    private final int amount;

    public BetMessage(int playerID, int amount) {
        super(MessageType.BET);
        this.playerID = playerID;
        this.amount = amount;
    }

    @Override
    public String toMessageString() {
        return "type:" + getType().getId() + ";attributes:{playerID:" + playerID + ";amount:" + amount + "}";
    }
}