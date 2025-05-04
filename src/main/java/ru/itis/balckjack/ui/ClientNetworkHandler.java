package ru.itis.balckjack.ui;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.itis.balckjack.gamelogic.GameProcess;
import ru.itis.balckjack.gamelogic.model.Player;
import ru.itis.balckjack.messages.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientNetworkHandler {
    private final Logger logger = LogManager.getLogger(ClientNetworkHandler.class);
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    @Getter
    private Player player;
    @Getter
    private Socket socket;

    public ClientNetworkHandler(String host, int port) {
        connectToServer(host, port);
    }

    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::listenToServer).start(); // Запускаем поток для чтения сообщений
        } catch (Exception e) {
            logger.error("Ошибка подключения: {}", e.getMessage(), e);
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                logger.info("Получено от сервера: {}", message);
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка чтения сообщений: {}", e.getMessage(), e);
        }
    }

    public void sendCommand(Message message) {
        if (out != null) {
            out.println(message.toMessageString());
            System.out.println("Отправлено: %s".formatted(message.toMessageString()));
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }


    public interface MessageListener {
        void onMessageReceived(String message);
    }
}