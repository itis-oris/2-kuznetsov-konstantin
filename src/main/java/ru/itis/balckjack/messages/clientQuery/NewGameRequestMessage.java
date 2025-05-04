package ru.itis.balckjack.messages.clientQuery;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class NewGameRequestMessage extends Message {
    private final int playerID;

    public NewGameRequestMessage(int playerID) {
        super(MessageType.NEWGAMEREQUEST);
        this.playerID = playerID;
    }

    @Override
    public String toMessageString() {
        return "type:%s;attributes:{playerID:%s}".formatted(getType().getId(), playerID);
    }
}
