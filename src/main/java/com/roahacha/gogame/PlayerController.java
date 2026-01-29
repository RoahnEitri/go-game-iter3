package com.roahacha.gogame;

import java.io.IOException;
import java.net.Socket;

import com.roahacha.gogame.Common.*;

/**
 * Klasa odpowiedzialna za kontrolę komunikacji z pojedynczym graczem po stronie serwera.
 * <p>
 * Pełni rolę pośrednika między logiką sesji gry ({@link GameSession}), a fizycznym 
 * połączeniem sieciowym obsługiwanym przez {@link SocketFacade}.
 */
public class PlayerController implements AutoCloseable {
    private final SocketFacade connection;
    private final Stone stone;

    /**
     * Inicjalizuje kontroler gracza i przesyła mu informację o przypisanym kolorze kamieni.
     * * @param socket Gniazdo sieciowe połączonego gracza.
     * @param stone Kolor kamieni przypisany do tego gracza ({@link Stone#BLACK} lub {@link Stone#WHITE}).
     * @throws IOException Jeśli wystąpi błąd podczas otwierania strumieni komunikacyjnych.
     * @throws IllegalArgumentException Jeśli kolor kamienia jest nieprawidłowy.
     */
    public PlayerController(Socket socket, Stone stone) throws IOException {
        this.connection = new SocketFacade(socket);
        this.stone = stone;
        if (stone == Stone.BLACK) {
            sendAction(GameAction.GAME_STONE_BLACK);
        } else if (stone == Stone.WHITE) {
            sendAction(GameAction.GAME_STONE_WHITE);
        } else {
            throw new IllegalArgumentException("Invalid stone color");
        }
    }

    /**
     * Wysyła określoną akcję (sygnał) do gracza.
     * * @param action Obiekt {@link GameAction} definiujący typ komunikatu.
     */
    public void sendAction(GameAction action) {
        connection.sendInt(action.getIndex());
    }

    /**
     * Odbiera akcję przesłaną przez gracza.
     * * @return Obiekt {@link GameAction} odpowiadający odebranemu indeksowi.
     */
    public GameAction reciveAction() {
        int actionIndex = connection.readInt();
        return GameAction.fromIndex(actionIndex);
    }

    /**
     * Serializuje i przesyła aktualny stan planszy do gracza.
     * <p>
     * Kamienie są przesyłane jako wartości całkowite: 0 dla braku, 1 dla czarnych, 2 dla białych.
     * * @param board Obiekt planszy, której stan ma zostać wysłany.
     */
    public void sendBoardState(GameBoard board) {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Stone stone = board.getStone(row, col);
                int stoneValue = (stone == Stone.NONE) ? 0 : (stone == Stone.BLACK ? 1 : 2);
                connection.sendInt(stoneValue);
            }
        }
    }

    /**
     * Odbiera współrzędne ruchu wykonanego przez gracza.
     * * @return Tablica dwuelementowa zawierająca współrzędne [x, y].
     */
    public int[] reciveMove() {
        int x = connection.readInt();
        int y = connection.readInt();
        return new int[] {x, y};
    }

    /**
     * Przesyła identyfikator gracza. Używane jednorazowo przy rozpoczęciu meczu.
     * * @param playerId Unikalny identyfikator gracza.
     */
    public void sendStoneInfo(int playerId) {
        connection.sendInt(playerId);
    }

    /**
     * Zwraca kolor kamieni przypisany do tego gracza.
     * * @return Obiekt {@link Stone} reprezentujący kolor.
     */
    public Stone getStone() {
        return stone;
    }

    /**
     * Zamyka połączenie sieciowe z graczem i zwalnia zasoby.
     * * @throws Exception Jeśli wystąpi błąd podczas zamykania połączenia.
     */
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
