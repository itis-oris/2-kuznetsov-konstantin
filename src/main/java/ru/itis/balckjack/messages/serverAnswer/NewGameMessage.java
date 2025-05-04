package ru.itis.balckjack.messages.serverAnswer;

import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageType;

public class NewGameMessage extends Message {
    public NewGameMessage() {
        super(MessageType.NEWGAME);
    }

    @Override
    public String toMessageString() {
        return "type:" + getType().getId() + ";";
    }
}
