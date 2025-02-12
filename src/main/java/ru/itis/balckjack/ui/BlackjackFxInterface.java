package ru.itis.balckjack.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlackjackFxInterface extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/start-scene.fxml"));
        Parent root = loader.load();
        MainFXController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Blackjack Online");
        primaryStage.setScene(new Scene(root, 680, 510));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}