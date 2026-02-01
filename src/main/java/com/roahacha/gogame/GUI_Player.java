package com.roahacha.gogame;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Główna klasa graficznego interfejsu użytkownika (GUI) dla aplikacji gry Go.
 * <p>
 * Odpowiada za renderowanie planszy, obsługę zdarzeń myszy oraz wyświetlanie 
 * komunikatów systemowych i statusu gry.
 */
public class GUI_Player extends GUI {

    /**
     * Punkt wejścia aplikacji JavaFX.
     * @param args Argumenty linii poleceń.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Inicjalizuje główne okno aplikacji, buduje układ graficzny i uruchamia połączenie z serwerem.
     * @param primaryStage Główna scena (okno) aplikacji.
     */
    @Override
    public void start(Stage primaryStage) {

        Button passButton = new Button("PAS");
        passButton.setPrefWidth(100);
        passButton.setStyle("-fx-background-color: #87CEEB; -fx-cursor: hand;");

        Button surrenderButton = new Button("PODDAJ SIĘ");
        surrenderButton.setPrefWidth(100);
        surrenderButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-cursor: hand;");

        passButton.setOnAction(e -> client.sendPass());
        surrenderButton.setOnAction(e -> client.sendSurrender());

        HBox bottomPanel = new HBox(20, passButton, surrenderButton);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-padding: 15; -fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 1 0 0 0;");

        root.setBottom(bottomPanel);

        primaryStage.setScene(new Scene(root, 650, 750));primaryStage.setTitle("Go Game");
        primaryStage.setOnCloseRequest(e -> {
            client.close();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        //using Thread because of javafx
        new Thread(() -> client.connectToServer()).start();
    }
}