package com.roahacha.gogame;

import com.roahacha.gogame.Common.Stone;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public abstract class GUI extends Application {

    private final int SIZE = 19;
    private StackPane[][] cells = new StackPane[SIZE][SIZE];
    Client client;
    BorderPane root;

    private Label infoLabel = new Label("Łączenie...");
    private Label statusLabel = new Label("");

    private void windowSetUp() {
        client = new Client(this);

        root = new BorderPane();

        VBox top = new VBox(10, infoLabel, statusLabel);
        top.setAlignment(Pos.CENTER);
        top.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4;");
        root.setTop(top);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #DEB887; -fx-padding: 10;");

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = createCell(r, c);
                grid.add(cells[r][c], c, r);
            }
        }
        root.setCenter(grid);
    }

    public GUI () {
        windowSetUp();
    }

    /**
     * Tworzy pojedyncze pole (komórkę) na planszy.
     * @param r Indeks wiersza.
     * @param c Indeks kolumny.
     * @return Kontener typu StackPane reprezentujący pole na planszy.
     */
    private StackPane createCell(int r, int c) {
        StackPane cell = new StackPane();
        cell.setPrefSize(30, 30);
        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.BLACK);
        cell.getChildren().add(rect);

        //sending to the client
        cell.setOnMouseClicked(e -> client.sendMove(r, c));
        return cell;
    }

    /**
     * Aktualizuje zawartość konkretnej komórki na planszy (dodaje lub usuwa kamień).
     * @param r Indeks wiersza.
     * @param c Indeks kolumny.
     * @param stone Typ kamienia do wyświetlenia.
     */
    private void updateCell(int r, int c, Stone stone) {
        StackPane cell = cells[r][c];


        cell.getChildren().removeIf(n -> n instanceof Circle);


        if (stone == Stone.BLACK) {
            Circle circle = new Circle(13);
            circle.setFill(Color.BLACK);
            circle.setStroke(Color.GREY);
            cell.getChildren().add(circle);
        }
        else if (stone == Stone.WHITE) {
            Circle circle = new Circle(13);
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
            cell.getChildren().add(circle);
        }

    }

    /**
     * Odświeża widok całej planszy na podstawie dostarczonej macierzy kamieni.
     * @param grid Macierz stanów pól planszy.
     */
    public void refreshBoard(Stone[][] grid) {
        Platform.runLater(() -> {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    updateCell(r, c, grid[r][c]);
                }
            }
        });
    }

    /**
     * Aktualizuje górną etykietę informacyjną (np. o przydzielonym kolorze).
     * @param text Tekst do wyświetlenia.
     */
    public void updateInfo(String text) {
        Platform.runLater(() -> infoLabel.setText(text));
    }

    /**
     * Aktualizuje etykietę statusu gry (np. informacja o turze).
     * @param text Tekst do wyświetlenia.
     */
    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    /**
     * Wyświetla okno dialogowe z komunikatem o zakończeniu gry.
     * @param msg Treść komunikatu.
     */
    public void showEndMessage(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Koniec Gry");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.show();
        });
    }

    public abstract void start(Stage primaryStage);
}
