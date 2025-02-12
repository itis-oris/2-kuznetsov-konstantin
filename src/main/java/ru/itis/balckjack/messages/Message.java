package ru.itis.balckjack.messages;

import lombok.Getter;

@Getter
public abstract class Message {
    private final MessageType type;

    public Message(MessageType type) {
        this.type = type;
    }

    public abstract String toMessageString();

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                '}';
    }
}
