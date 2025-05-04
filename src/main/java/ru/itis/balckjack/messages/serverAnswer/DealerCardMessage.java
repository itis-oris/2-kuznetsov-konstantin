package ru.itis.balckjack.messages.serverAnswer;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class DealerCardMessage extends Message {
    private final int cardID;

    public DealerCardMessage(int cardID) {
        super(MessageType.DEALERCARD);
        this.cardID = cardID;
    }

    @Override
    public String toMessageString() {
        return "type:%s;attributes:{cardID:%s}".formatted(getType().getId(), cardID);
    }
}
