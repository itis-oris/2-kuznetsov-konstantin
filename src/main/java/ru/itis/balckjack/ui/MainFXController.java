package ru.itis.balckjack.ui;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.itis.balckjack.gamelogic.GameProcess;
import ru.itis.balckjack.gamelogic.model.Player;
import ru.itis.balckjack.messages.Message;
import ru.itis.balckjack.messages.MessageParser;
import ru.itis.balckjack.messages.clientQuery.*;
import ru.itis.balckjack.messages.serverAnswer.*;

import java.io.IOException;
import java.util.ArrayList;

public class MainFXController {
    private ClientNetworkHandler networkHandler;
    private Player currentPlayer;
    private Player otherPlayer;
    private ArrayList<Integer> dealerHand = new ArrayList<>();

    private boolean currentPlayerReady = false;
    private boolean otherPlayerReady = false;
    private Stage primaryStage;

    @FXML private Label messageLabel;
    @FXML private Button connectButton;
    @FXML private StackPane betBox;
    @FXML private TextField betField;
    @FXML private Button betButton;

    @FXML private HBox player1Cards;
    @FXML private HBox player2Cards;
    @FXML private HBox dealerCards;
    @FXML private VBox player1BalanceBox;
    @FXML private VBox player2BalanceBox;

    // Добавляем вспомогательные переменные
    private int currentBet = 0;
    private boolean isPlayerTurn = false;
    private boolean dealerHasHiddenCard = false;

    @FXML private VBox actionButtonsBox;
    @FXML private Button requestCardButton;
    @FXML private Button endMoveButton;

    private void updateUI() {
        updatePlayerBox(player1BalanceBox,
                currentPlayer.getId() == 0 ? currentPlayer : otherPlayer);
        updatePlayerBox(player2BalanceBox,
                currentPlayer.getId() == 1 ? currentPlayer : otherPlayer);
    }

    private void updatePlayerBox(VBox balanceBox, Player player) {
        Platform.runLater(() -> {
            ((Label) balanceBox.getChildren().get(1)).setText(String.valueOf(player.getBalance()));
            ((Label) balanceBox.getChildren().get(3)).setText(
                    player.getBet() != null ? String.valueOf(player.getBet()) : "0"
            );
            ((Label) balanceBox.getChildren().get(5)).setText(String.valueOf(player.score()));
        });
    }

    private Player resolvePlayer(int playerId) {
        return playerId == currentPlayer.getId() ? currentPlayer : otherPlayer;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // Добавляем обработчик закрытия окна
        primaryStage.setOnCloseRequest(event -> {
            if (networkHandler != null) {
                try {
                    networkHandler.sendCommand(new RestartMessage());
                    networkHandler.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.exit();
            System.exit(0);
        });
    }

    @FXML
    private void handleConnect() {
        networkHandler = new ClientNetworkHandler("194.67.84.100", 12345);
        networkHandler.setMessageListener(this::handleServerMessage);
        networkHandler.sendCommand(new ConnectedMessage());
    }

    @FXML
    private void handleBet() {
        String betValue = betField.getText();
        if (!betValue.isEmpty()) {
            int bet = Integer.parseInt(betValue);
            if (bet >= 1 && bet <= currentPlayer.getBalance()) {
                networkHandler.sendCommand(new BetMessage(currentPlayer.getId(), bet));
                showAlert("Ставка принята", "Ваша ставка в размере " + bet + " принята.", Alert.AlertType.INFORMATION);
                currentBet = bet;
                betButton.setDisable(true);
                betField.setDisable(true);
                updateUI();
            } else {
                showAlert("Ошибка", "Некорректная ставка! Введите сумму от 1 до " + currentPlayer.getBalance() + ".", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Предупреждение", "Введите сумму ставки!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRequestCard() {
        networkHandler.sendCommand(new RequestCardMessage(currentPlayer.getId()));
    }

    @FXML
    private void handleEndMove() {
        networkHandler.sendCommand(new EndMoveMessage(currentPlayer.getId()));
        actionButtonsBox.setVisible(false);
    }

    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            if ("SERVER_SHUTDOWN".equals(message)) {
                showAlert("Сервер остановлен", "Игра завершена. Игрок покинул комнату", Alert.AlertType.INFORMATION);

                Platform.exit();
                System.exit(0);
                return;
            }
            Message parsedMessage = MessageParser.parse(message);
            switch (parsedMessage.getType()) {
                case CONNECTIONACCEPTED:
                    ConnectionAcceptedMessage cam = (ConnectionAcceptedMessage) parsedMessage;
                    if (currentPlayer == null) {
                        currentPlayer = new Player(cam.getCurrentPlayerID(), 1000, null);
                        currentPlayer.setHand(new ArrayList<>());
                        otherPlayer = new Player(cam.getCurrentPlayerID() == 0 ? 1 : 0, 1000, null);
                        otherPlayer.setHand(new ArrayList<>());
                    }
                    if (cam.getOtherPlayerID() != null) {
                        loadScene("/main-scene.fxml");
                    } else {
                        loadScene("/wait-scene.fxml");
                    }
                    break;
                case BETACCEPTED:
                    BetAcceptedMessage bam = (BetAcceptedMessage) parsedMessage;
                    if (bam.getBetPlayerID() == currentPlayer.getId()) {
                        currentPlayerReady = true;
                        betBox.setVisible(false);
                        if (currentPlayer.getId() == 0) {
                            player1Cards.setOpacity(1.);
                        } else {
                            player2Cards.setOpacity(1.);
                        }
                    } else {
                        otherPlayerReady = true;
                    }
                    if (currentPlayerReady && otherPlayerReady) actionButtonsBox.setVisible(true);

                    Player targetPlayer = resolvePlayer(bam.getBetPlayerID());
                    targetPlayer.setBet(bam.getAmount());
                    targetPlayer.setBalance(targetPlayer.getBalance()-bam.getAmount());
                    updateUI();

                    break;
                case RECEIVEDCARD:
                    ReceivedCardMessage rcm = (ReceivedCardMessage) parsedMessage;
                    handleReceivedCard(rcm.getPlayerID(), rcm.getCardID());
                    break;
                case DEALERFIRSTCARD:
                    DealerFirstCardMessage dfcm = (DealerFirstCardMessage) parsedMessage;
                    handleDealerFirstCard(dfcm.getCardID());
                    break;

                case DEALERCARD:
                    DealerCardMessage dcm = (DealerCardMessage) parsedMessage;
                    handleDealerCard(dcm.getCardID());
                    break;

                case WINNER:
                    WinnerMessage wm = (WinnerMessage) parsedMessage;
                    handleWinner(wm.getPlayerID(), wm.getBalance());
                    break;

                case LOOSER:
                    LooserMessage lm = (LooserMessage) parsedMessage;
                    handleLoser(lm.getPlayerID(), lm.getBalance());
                    break;

                case NEWGAME:
                    resetGame();
                    break;
            }
        });
    }

    private void handleWinner(int playerId, int newBalance) {
        if (playerId == currentPlayer.getId()) {
            currentPlayer.setBalance(newBalance);
            showGameResultDialog(true, currentPlayer.score(), calculateDealerScore());
        } else {
            otherPlayer.setBalance(newBalance);
        }
        updateUI();
    }

    private void handleLoser(int playerId, int newBalance) {
        if (playerId == currentPlayer.getId()) {
            currentPlayer.setBalance(newBalance);
            showGameResultDialog(false, currentPlayer.score(), calculateDealerScore());
        } else {
            otherPlayer.setBalance(newBalance);
        }
        updateUI();
    }

    private int calculateDealerScore() {
        int score = 0;
        int aces = 0;
        for (int cardID : dealerHand) {
            int rank = (cardID / 4) + 2; // Получаем номинал карты (2-14, где 14 - туз)

            if (rank >= 2 && rank <= 10) {
                score += rank;
            } else if (rank >= 11 && rank <= 13) { // Валет, дама, король
                score += 10;
            } else { // Туз (rank == 14)
                aces++;
                score += 11;
            }
        }
        // Корректируем тузы, если перебор
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    private void showGameResultDialog(boolean isWin, int playerScore, int dealerScore) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(isWin ? "Победа!" : "Поражение");
        alert.setHeaderText(null);
        alert.setContentText(String.format("%s%nВаши очки: %d%nОчки дилера: %d",
                isWin ? "ТЫ ПОБЕДИЛ!!!" : "Ты проиграл, повезет в следующий раз!",
                playerScore,
                dealerScore
        ));

        ButtonType confirmButton = new ButtonType("Новая игра", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(confirmButton);

        alert.showAndWait().ifPresent(buttonType -> {
            networkHandler.sendCommand(new NewGameRequestMessage(currentPlayer.getId()));
        });
    }

    private void resetGame() {
        // Сброс состояния игры
        currentPlayer.getHand().clear();
        otherPlayer.getHand().clear();
        dealerHand.clear();

        currentPlayerReady = false;
        otherPlayerReady = false;

        player1Cards.getChildren().clear();
        player2Cards.getChildren().clear();
        dealerCards.getChildren().clear();

        currentPlayer.setBet(null);
        otherPlayer.setBet(null);

        betField.setDisable(false);
        betButton.setDisable(false);
        actionButtonsBox.setVisible(false);

        betBox.setVisible(true);

        updateUI();
    }

    private void handleDealerFirstCard(int cardId) {
        // Добавляем открытую карту
        addDealerCard(cardId);

        // Добавляем рубашкой вверх
        ImageView hiddenCard = new ImageView(new Image(getClass().getResourceAsStream("/images/rect_cards/card_back.png")));

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), hiddenCard);
        rotateTransition.setByAngle(360); // Поворот на 360 градусов
        rotateTransition.setCycleCount(1); // Выполнить один раз
        rotateTransition.setAutoReverse(false); // Не возвращаться в исходное положение
        rotateTransition.play();

        hiddenCard.setUserData("HIDDEN_CARD");
        hiddenCard.setFitWidth(60);
        hiddenCard.setPreserveRatio(true);
        dealerCards.getChildren().add(hiddenCard);
        dealerHasHiddenCard = true;

    }

    private void handleDealerCard(int cardId) {
        // Удаляем все скрытые карты
        if(dealerHasHiddenCard) {
            dealerCards.getChildren().removeIf(node ->
                    node instanceof ImageView &&
                    "HIDDEN_CARD".equals(((ImageView) node).getUserData())
            );
            dealerHasHiddenCard = false;
        }

        // Добавляем новую открытую карту
        addDealerCard(cardId);
    }

    private void addDealerCard(int cardId) {
        dealerHand.add(cardId);
        String imagePath = convertCardIdToImagePath(cardId);
        ImageView cardImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        cardImage.setFitWidth(60);
        cardImage.setPreserveRatio(true);
        dealerCards.getChildren().add(cardImage);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), cardImage);
        rotateTransition.setByAngle(360); // Поворот на 360 градусов
        rotateTransition.setCycleCount(1); // Выполнить один раз
        rotateTransition.setAutoReverse(false); // Не возвращаться в исходное положение
        rotateTransition.play();
    }

    private void handleReceivedCard(int playerId, int cardId) {
        // Добавляем карту игроку в модель
        Player targetPlayer = resolvePlayer(playerId);
        targetPlayer.addCard(cardId);
        updateUI();

        // Получаем путь к изображению карты
        String imagePath = convertCardIdToImagePath(cardId);

        // Создаем элемент для отображения карты
        ImageView cardImage = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        cardImage.setFitWidth(60);
        cardImage.setPreserveRatio(true);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), cardImage);
        rotateTransition.setByAngle(360); // Поворот на 360 градусов
        rotateTransition.setCycleCount(1); // Выполнить один раз
        rotateTransition.setAutoReverse(false); // Не возвращаться в исходное положение
        rotateTransition.play();

        // Добавляем в соответствующий HBox
        if (playerId == 0) {
            if (!player1Cards.getChildren().isEmpty()) {
                cardImage.getStyleClass().add("card-overlap");
            }
            player1Cards.getChildren().add(cardImage);
        } else if (playerId == 1) {
            if (!player2Cards.getChildren().isEmpty()) {
                cardImage.getStyleClass().add("card-overlap");
            }
            player2Cards.getChildren().add(cardImage);
        }

        if (targetPlayer == currentPlayer && targetPlayer.score() > 21) {
            handleBust();
        }
    }

    private void handleBust() {
        handleEndMove();
        showAlert("Перебор!", "Сумма очков: " + currentPlayer.score(), Alert.AlertType.WARNING);
    }

    private String convertCardIdToImagePath(int cardId) {
        String[] suits = {"club", "diamond", "heart", "spade"};
        String suit = suits[cardId % 4];
        int valueIndex = cardId / 4;

        String value;
        if (valueIndex < 9) {
            value = String.valueOf(valueIndex + 2);
        } else {
            String[] faceCards = {"J", "Q", "K", "A"};
            value = faceCards[valueIndex - 9];
        }

        return "/images/rect_cards/" + suit + "/" + value + ".png";
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            MainFXController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setNetworkHandler(networkHandler);
            controller.setPlayers(currentPlayer, otherPlayer);
            networkHandler.setMessageListener(controller::handleServerMessage);

            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNetworkHandler(ClientNetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    public void setPlayers(Player currentPlayer, Player otherPlayer) {
        this.currentPlayer = currentPlayer;
        this.otherPlayer = otherPlayer;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}