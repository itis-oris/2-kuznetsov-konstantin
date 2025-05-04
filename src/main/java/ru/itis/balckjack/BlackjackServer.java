package ru.itis.balckjack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.itis.balckjack.exceptions.PlayersLimitException;
import ru.itis.balckjack.gamelogic.GameProcess;
import ru.itis.balckjack.gamelogic.model.Player;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageParser;
import ru.itis.balckjack.messages.clientQuery.BetMessage;
import ru.itis.balckjack.messages.clientQuery.EndMoveMessage;
import ru.itis.balckjack.messages.clientQuery.NewGameRequestMessage;
import ru.itis.balckjack.messages.clientQuery.RequestCardMessage;
import ru.itis.balckjack.messages.serverAnswer.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackjackServer implements Runnable {

    private final Logger logger = LogManager.getLogger(BlackjackServer.class);
    private ArrayList<ConnectionHandler> connections;
    private GameProcess gameProcess;
    
    private ServerSocket server;

    public static void main(String[] args) {

        BlackjackServer server = new BlackjackServer();
        server.run();
    }

    public BlackjackServer() {
        connections = new ArrayList<>();
    }

    @Override
    public void run() {
        if (gameProcess == null)
            gameProcess = new GameProcess();
        gameProcess.start();

        while (true) {
            try (ExecutorService executor = Executors.newCachedThreadPool()) {
                server = new ServerSocket(12345);
                logger.info("Сервер запущен на порту 12345...");

                while (!gameProcess.isGameFinished()) {
                    Socket client = server.accept();
                    logger.info("Новое подключение: {}", client.getInetAddress());
                    ConnectionHandler connection = new ConnectionHandler(client, this);
                    connections.add(connection);
                    executor.execute(connection);
                }

            } catch (IOException e) {
                logger.error("Ошибка сервера: {}", e.getMessage());
            } finally {
                shutdownServer(); // Закрываем соединения
                break; // Выходим из цикла
            }
        }
    }

    public void shutdownServer() {
        if (!gameProcess.isGameFinished()) {
            logger.info("Попытка закрыть сервер, но игра еще не завершена!");
            return;
        }

        logger.info("Закрытие всех соединений...");

        for (ConnectionHandler connection : connections) {
            if (connection != null) {
                connection.sendMessage("SERVER_SHUTDOWN");
                connection.stopHandler(); // Остановить поток перед закрытием
                connection.close();
            }
        }
        for (ConnectionHandler connection : connections) {
            connection.close();
        }
    }


    public void broadcast(String message) {
        for (ConnectionHandler connection : connections) {
            if (connection != null)
                connection.sendMessage(message);
        }
    }

    public void handleMessage(String message, ConnectionHandler handler) throws PlayersLimitException {
        Message parsedMessage = MessageParser.parse(message);
        switch (parsedMessage.getType()) {
            case CONNECTED -> {
                ConnectionAcceptedMessage connectionAcceptedMessage;
                System.out.println(gameProcess.playersCount());
                if (gameProcess.playersCount() < 2) {
                    Player player = new Player(
                            gameProcess.playersCount(),
                            1000,
                            handler.getSocket()
                    );
                    gameProcess.initNewPlayer(player);
                    handler.setPlayerId(player.getId());
                    if (gameProcess.playersCount() == 1)
                        connectionAcceptedMessage = new ConnectionAcceptedMessage(0);
                    else
                        connectionAcceptedMessage = new ConnectionAcceptedMessage(1, 0);
                } else throw new PlayersLimitException();
                broadcast(connectionAcceptedMessage.toMessageString());
                logger.info("Подключен игрок");
            }

            case BET -> {
                BetMessage betMessage = (BetMessage) parsedMessage;
                int playerID = betMessage.getPlayerID();
                int amount = betMessage.getAmount();

                // Обрабатываем ставку
                if (gameProcess.placeBet(playerID, amount)) {
                    // Отправляем подтверждение всем игрокам
                    broadcast(new BetAcceptedMessage(playerID, amount).toMessageString());
                    gameProcess.getPlayer(playerID).setBet(amount);
                    logger.info("Ставка игрока {} принята: {}", playerID, amount);

                    // Проверяем, сделали ли оба игрока ставки
                    if (gameProcess.areAllBetsPlaced()) {
                        // Переход к следующему состоянию игры
                        int dealerCardID = gameProcess.getCard();
                        DealerFirstCardMessage dealerCardMessage = new DealerFirstCardMessage(dealerCardID);
                        gameProcess.addDealerCard(dealerCardID);
                        broadcast(dealerCardMessage.toMessageString());
                        for (Player player : gameProcess.players()) {
                            int cardID = gameProcess.getCard();
                            player.addCard(cardID);
                            broadcast(
                                    new ReceivedCardMessage(player.getId(), cardID).toMessageString()
                            );
                            cardID = gameProcess.getCard();
                            player.addCard(cardID);
                            broadcast(
                                    new ReceivedCardMessage(player.getId(), cardID).toMessageString()
                            );
                        }
                    }
                } else {
                    handler.sendMessage("Ошибка: недостаточно средств или неверный ID игрока");
                }
            }
            case REQUESTCARD -> {
                RequestCardMessage requestCardMessage = (RequestCardMessage) parsedMessage;
                int cardID = gameProcess.getCard();
                gameProcess.getPlayer(requestCardMessage.getPlayerID()).addCard(cardID);
                broadcast(new ReceivedCardMessage(
                        requestCardMessage.getPlayerID(),
                        cardID
                ).toMessageString());
            }
            case ENDMOVE -> {
                EndMoveMessage endMoveMessage = (EndMoveMessage) parsedMessage;
                gameProcess.playerFinished(endMoveMessage.getPlayerID());
                if (gameProcess.areAllPlayersMoved()) {
                    while (gameProcess.dealerScore() <= 17) {
                        int cardID = gameProcess.getCard();
                        DealerCardMessage dealerCardMessage = new DealerCardMessage(cardID);
                        gameProcess.addDealerCard(cardID);
                        broadcast(dealerCardMessage.toMessageString());
                    }

                    System.out.println(gameProcess.dealerScore());
                    for (Player player : gameProcess.players()) {
                        if (player.score() > 21) {
                            LooserMessage looserMessage = new LooserMessage(player.getId(), player.getBalance());
                            broadcast(looserMessage.toMessageString());
                        }
                        else if (gameProcess.dealerScore() > 21) {
                            player.increaseBalance();
                            WinnerMessage winnerMessage = new WinnerMessage(player.getId(), player.getBalance());
                            broadcast(winnerMessage.toMessageString());
                        }
                        else if (player.score() > gameProcess.dealerScore()) {
                            player.increaseBalance();
                            WinnerMessage winnerMessage = new WinnerMessage(player.getId(), player.getBalance());
                            broadcast(winnerMessage.toMessageString());
                        }
                        else if (player.score() == gameProcess.dealerScore()) {
                            player.saveBalance();
                            WinnerMessage winnerMessage = new WinnerMessage(player.getId(), player.getBalance());
                            broadcast(winnerMessage.toMessageString());
                        } else {
                            LooserMessage looserMessage = new LooserMessage(player.getId(), player.getBalance());
                            broadcast(looserMessage.toMessageString());
                        }
                    }
                }
            }
            case NEWGAMEREQUEST -> {
                NewGameRequestMessage newGameRequestMessage = (NewGameRequestMessage) parsedMessage;
                gameProcess.reset();
                gameProcess.playerRequestNewGame(newGameRequestMessage.getPlayerID());
                if (gameProcess.allPlayersReady()) {
                    gameProcess.reset();
                    gameProcess.clearRequests();
                    broadcast(new NewGameMessage().toMessageString());
                }
            }
            case RESTART -> {
                broadcast("SERVER_SHUTDOWN"); // Уведомляем клиентов перед закрытием
                shutdownServer(); // Корректно закрываем сервер
                gameProcess.finishGame(); // Завершаем игру
                connections = new ArrayList<>();
                gameProcess = new GameProcess();
            }


        }
    }
}