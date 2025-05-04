package ru.itis.balckjack.messages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.itis.balckjack.messages.clientQuery.*;
import ru.itis.balckjack.messages.serverAnswer.*;

import java.util.HashMap;
import java.util.Map;

public class MessageParser {

    private final static Logger logger = LogManager.getLogger(MessageParser.class);

    public static Message parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            throw new IllegalArgumentException("Received empty message!");
        }

        String[] parts = rawMessage.split(";", 2);
        Map<String, String> attributes = new HashMap<>();
        Integer typeID = null;

        for (String part : parts) {
            if (part.startsWith("type:")) {
                try {
                    typeID = Integer.parseInt(part.split(":")[1]);
                } catch (NumberFormatException e) {
                    logger.error("Invalid type ID format in message: {}", rawMessage, e);
                }
            } else if (part.startsWith("attributes:{") && part.endsWith("}")) {
                String attrData = part.substring(12, part.length() - 1);
                for (String attr : attrData.split(";")) {
                    String[] keyValue = attr.split(":");
                    if (keyValue.length == 2) {
                        attributes.put(keyValue[0], keyValue[1]);
                    } else {
                        logger.error("Invalid attribute format: {} in message: {}", attr, rawMessage);
                    }
                }
            }
        }

        if (typeID == null) {
            logger.error("Message type is missing in: {}", rawMessage);
        }

        MessageType type = MessageType.fromId(typeID);

        return switch (type) {
            case CONNECTED -> new ConnectedMessage();
            case BET -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    int amount = Integer.parseInt(attributes.get("amount"));
                    yield new BetMessage(playerID, amount);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case BETACCEPTED -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("betPlayerID"));
                    int amount = Integer.parseInt(attributes.get("amount"));
                    yield new BetAcceptedMessage(playerID, amount);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case CONNECTIONACCEPTED -> {
                try {
                    int currentPlayerID = Integer.parseInt(attributes.get("currentPlayerID"));
                    if (attributes.containsKey("otherPlayerID")) {
                        int otherPlayerID = Integer.parseInt(attributes.get("otherPlayerID"));
                        yield new ConnectionAcceptedMessage(currentPlayerID, otherPlayerID);
                    } else {
                        yield new ConnectionAcceptedMessage(currentPlayerID);
                    }

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case DEALERFIRSTCARD -> {
                try {
                    int cardID = Integer.parseInt(attributes.get("cardID"));
                    yield new DealerFirstCardMessage(cardID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
            case RECEIVEDCARD -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    int cardID = Integer.parseInt(attributes.get("cardID"));
                    yield new ReceivedCardMessage(playerID, cardID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
            case REQUESTCARD -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    yield new RequestCardMessage(playerID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
            case ENDMOVE -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    yield new EndMoveMessage(playerID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
            case DEALERCARD -> {
                try {
                    int cardID = Integer.parseInt(attributes.get("cardID"));
                    yield new DealerCardMessage(cardID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }
            case WINNER -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    int balance = Integer.parseInt(attributes.get("balance"));
                    yield new WinnerMessage(playerID, balance);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case NEWGAME -> new NewGameMessage();
            case NEWGAMEREQUEST -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    yield new NewGameRequestMessage(playerID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case LOOSER -> {
                try {
                    int playerID = Integer.parseInt(attributes.get("playerID"));
                    int balance = Integer.parseInt(attributes.get("balance"));
                    yield new LooserMessage(playerID, balance);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format in BET message: " + rawMessage, e);
                }
            }
            case RESTART -> new RestartMessage();
        };
    }
}
