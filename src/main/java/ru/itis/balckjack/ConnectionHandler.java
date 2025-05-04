package ru.itis.balckjack;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private volatile boolean running = true;

    private final Socket clientSocket;
    private final BlackjackServer server;
    private BufferedReader in;
    private PrintWriter out;
    @Setter
    private int playerId;

    private final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    public ConnectionHandler(Socket client, BlackjackServer blackjackServer) {
        this.clientSocket = client;
        server = blackjackServer;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            // Этап подключения

            String message;
            while (running) {
                try {
                    message = in.readLine();
                    if (message == null) break; // Если поток закрыт, завершаем цикл
                    logger.info("Получено сообщение: {}", message);
                    server.handleMessage(message, this);
                } catch (IOException e) {
                    if (running) { // Если сервер не выключается - логируем ошибку
                        logger.error("Ошибка связи с клиентом: {}", e.getMessage());
                    }
                    break;
                }
            }

            close();

        } catch (Exception e) {
            logger.error("Ошибка связи с клиентом: {}", e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                logger.error("Ошибка закрытия соединения: {}", e.getMessage(), e);
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.printf("Отправлено клиенту id=%s: %s%n", playerId, message);
        }
    }

    public void stopHandler() {
        running = false;
    }


    public void close() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    public Socket getSocket() {
        return clientSocket;
    }
}