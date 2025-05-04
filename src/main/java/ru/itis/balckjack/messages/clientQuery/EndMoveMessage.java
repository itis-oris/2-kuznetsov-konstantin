package ru.itis.balckjack.messages.clientQuery;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class EndMoveMessage extends Message {
    private final int playerID;

    public EndMoveMessage(int playerID) {
        super(MessageType.ENDMOVE);
        this.playerID = playerID;
    }

    @Override
    public String toMessageString() {
        return "type:%s;attributes:{playerID:%s}".formatted(getType().getId(), playerID);
    }
}
