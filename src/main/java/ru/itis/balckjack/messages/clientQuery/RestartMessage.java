package ru.itis.balckjack.messages.clientQuery;

import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

public class RestartMessage extends Message {
    public RestartMessage() {
        super(MessageType.RESTART);
    }

    @Override
    public String toMessageString() {
        return "type:" + getType().getId() + ";";
    }
}
