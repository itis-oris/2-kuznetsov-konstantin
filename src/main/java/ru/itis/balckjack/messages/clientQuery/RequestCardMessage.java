package ru.itis.balckjack.messages.clientQuery;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class RequestCardMessage extends Message {

    private final int playerID;

    public RequestCardMessage(int playerID) {
        super(MessageType.REQUESTCARD);
        this.playerID = playerID;
    }


    @Override
    public String toMessageString() {
        return "type:%s;attributes:{playerID:%s}".formatted(getType().getId(), playerID);
    }

}
