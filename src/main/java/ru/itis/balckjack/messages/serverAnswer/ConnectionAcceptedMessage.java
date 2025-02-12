package ru.itis.balckjack.messages.serverAnswer;

import lombok.Getter;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

@Getter
public class ConnectionAcceptedMessage extends Message {
    private final int currentPlayerID;
    private Integer otherPlayerID = null;

    public ConnectionAcceptedMessage(int currentPlayerID, int otherPlayerID) {
        super(MessageType.CONNECTIONACCEPTED);
        this.currentPlayerID = currentPlayerID;
        this.otherPlayerID = otherPlayerID;
    }

    public ConnectionAcceptedMessage(int currentPlayerID) {
        super(MessageType.CONNECTIONACCEPTED);
        this.currentPlayerID = currentPlayerID;
    }

    @Override
    public String toMessageString() {
        if (otherPlayerID == null)
            return "type:" + getType().getId() + ";attributes:{currentPlayerID:" + currentPlayerID + "}";

        return "type:" + getType().getId() + ";attributes:{currentPlayerID:" + currentPlayerID + ";" +
                "otherPlayerID:" + otherPlayerID + "}";
    }
}
