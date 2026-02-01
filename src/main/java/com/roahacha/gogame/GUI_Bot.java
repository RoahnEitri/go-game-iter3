package com.roahacha.gogame;

import javafx.application.Platform;
import javafx.stage.Stage;

public class GUI_Bot extends GUI {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Ustawienia okna
        primaryStage.setTitle("Go Game - BOT (Smart Client)");
        primaryStage.setScene(new javafx.scene.Scene(root, 650, 750));

        primaryStage.setOnCloseRequest(e -> {
            if (client != null) client.close();
            Platform.exit();
            System.exit(0);
        });

        // GUI utworzyło zwykłego klienta w konstruktorze
        // Nadpisujemy wersją dla bota.
        this.client = new BotClient(this);

        primaryStage.show();

        // Łączymy się z serwerem
        new Thread(() -> client.connectToServer()).start();
    }
}