package ru.itis.balckjack.messages.clientQuery;

import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

public class ConnectedMessage extends Message {
    public ConnectedMessage() {
        super(MessageType.CONNECTED);
    }

    @Override
    public String toMessageString() {
        return "type:" + getType().getId() + ";";
    }
}
