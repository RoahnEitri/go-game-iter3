package com.roahacha.gogame;

import java.io.*;
import java.net.Socket;

import com.roahacha.gogame.Common.GameAction;
import com.roahacha.gogame.Common.SocketFacade;
import com.roahacha.gogame.Common.Stone;

import javafx.application.Platform;

/**
 * Klasa ServerProxy odpowiada za komunikację klienta z serwerem gry.
 * Zarządza strumieniami danych i nasłuchuje komunikatów serwera w osobnym wątku.
 */
public class ServerProxy implements AutoCloseable {
    /** Stały rozmiar planszy odbierany z serwera. */
    private static final int gridSize = 19;
    private final SocketFacade connection;
    private final GameClientObserver observer;

    /**
     * Inicjalizuje proxy, ustawia połączenie i uruchamia pętlę nasłuchującą w nowym wątku.
     * @param connection Obiekt SocketFacade zarządzający gniazdem.
     * @param observer Obiekt implementujący GameClientObserver, reagujący na zmiany stanu gry.
     */
    public ServerProxy(SocketFacade connection, GameClientObserver observer) {
        this.connection = connection;
        this.observer = observer;
        new Thread(this::listenLoop).start();
    }

    /** Wysyła akcję typu GameAction do serwera. */
    public void sendAction(GameAction action) {
        connection.sendInt(action.getIndex());
    }

    /** Odbiera akcję typu GameAction od klienta. */
    public GameAction reciveAction() {
        int actionIndex = connection.readInt();
        return GameAction.fromIndex(actionIndex);
    }

    /** Wysyła żądanie wykonania ruchu na określonych współrzędnych. */
    public void sendMove(int row, int col) {
        sendAction(GameAction.PLAYER_MOVE);
        connection.sendInt(row);
        connection.sendInt(col);
    }

    /**
     * Odbiera od serwera stan całej planszy.
     * @param size Rozmiar planszy do zainicjalizowania.
     * @return Dwuwymiarowa tablica kamieni Stone[][].
     */
    public Stone[][] reciveBoardState(int size) {
        Stone[][] grid = new Stone[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int stoneValue = connection.readInt();
                if (stoneValue == 0) {
                    grid[row][col] = Stone.NONE;
                } else if (stoneValue == 1) {
                    grid[row][col] = Stone.BLACK;
                } else if (stoneValue == 2) {
                    grid[row][col] = Stone.WHITE;
                }
            }
        }
        return grid;
    }

    /**
     * Prywatna pętla nasłuchująca działająca w tle. 
     * Przetwarza nadchodzące komunikaty i aktualizuje interfejs użytkownika przez obserwatora.
     */
    private void listenLoop() {
        try {
            GameAction startAction = reciveAction();
            System.out.println("Start Action: " + startAction);

            Stone myStone;
            switch (startAction) {
                case GAME_STONE_BLACK:
                    myStone = Stone.BLACK;
                    break;
                case GAME_STONE_WHITE:
                    myStone = Stone.WHITE;
                    break;
                default:
                    myStone = null;
                    break;
            }
            Platform.runLater(() -> observer.onGameStart(myStone));

            while(true) {
                GameAction action = reciveAction();

                // Opcjonalnie: odkomentuj linię niżej, żeby widzieć w konsoli co przychodzi
                // System.out.println("Otrzymano akcję: " + action);

                switch (action) {
                    case GAME_END_DRAW:
                    case GAME_END_WIN:
                    case GAME_END_LOSS:
                        Platform.runLater(() -> observer.onGameAction(action));
                        return;

                    case GAME_SEND_GRID:
                        Stone[][] grid = reciveBoardState(gridSize);
                        Platform.runLater(() -> observer.onBoardUpdate(grid));
                        break;

                    case GAME_YOUR_TURN:
                        Platform.runLater(() -> observer.onGameAction(action));
                        break;

                    // [TO JEST KLUCZOWA POPRAWKA]
                    // Musisz przekazać informację o błędzie do Obserwatora (Clienta)
                    case GAME_INCORRENT_MOVE: // Upewnij się, że pisownia zgadza się z Enumem GameAction!
                        Platform.runLater(() -> observer.onGameAction(action));
                        break;

                    case PLAYER_QUIT: // Warto też dodać obsługę wyjścia przeciwnika
                        Platform.runLater(() -> observer.onGameAction(action));
                        return;

                    case GAME_STONE_BLACK:
                    case GAME_STONE_WHITE:
                    default:
                        // Ignorujemy inne, nieoczekiwane komunikaty
                        break;
                }
            }
        } catch (Exception e) {
            // W razie zerwania połączenia powiadamiamy klienta
            Platform.runLater(() -> observer.onGameAction(GameAction.PLAYER_QUIT));
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
