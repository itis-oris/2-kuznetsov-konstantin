package ru.itis.balckjack.messages.serverAnswer;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class BetAcceptedMessage extends Message {
    private final int betPlayerID;
    private final int amount;


    public BetAcceptedMessage(int betPlayerID, int amount) {
        super(MessageType.BETACCEPTED);
        this.betPlayerID = betPlayerID;
        this.amount = amount;
    }

    @Override
    public String toMessageString() {
        return "type:%s;attributes:{betPlayerID:%s;amount:%s}".formatted(
                getType().getId(),
                betPlayerID,
                amount
        );
    }
}
