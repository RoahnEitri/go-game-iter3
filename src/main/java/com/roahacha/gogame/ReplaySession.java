package com.roahacha.gogame;

import com.roahacha.gogame.Common.GameAction;
import com.roahacha.gogame.Common.GameBoard;
import com.roahacha.gogame.Common.SocketFacade;
import com.roahacha.gogame.Common.Stone;
import com.roahacha.gogame.database.MoveEntity;

import java.net.Socket;
import java.util.List;

/**
 * Sesja odtwarzania (Replay).
 * Łączy się z klientem i "streamuje" mu zapisaną grę ruch po ruchu.
 */
public class ReplaySession implements Runnable {

    private final SocketFacade observer;
    private final GameService gameService;
    private final Long gameIdToReplay;
    private final GameBoard gameBoard;

    public ReplaySession(Socket clientSocket, GameService service, Long gameId) throws Exception {
        this.observer = new SocketFacade(clientSocket);
        this.gameService = service;
        this.gameIdToReplay = gameId;

        this.gameBoard = new GameBoard();
    }

    @Override
    public void run() {
        try {
            System.out.println(">>> Rozpoczynam REPLAY gry ID: " + gameIdToReplay);

            // Pobierz ruchy z bazy
            List<MoveEntity> moves = gameService.getMovesForGame(gameIdToReplay);

            if (moves.isEmpty()) {
                System.out.println("Brak ruchów dla tej gry lub gra nie istnieje.");
                observer.close();
                return;
            }

            // Wyślij sygnał startu (żeby klient otworzył planszę)
            // Udajemy, że klient jest Czarnym, żeby widział planszę
            observer.sendInt(GameAction.GAME_STONE_BLACK.getIndex());
            observer.sendInt(GameAction.GAME_YOUR_TURN.getIndex()); // Wyświetli komunikat gotowości

            // Pętla odtwarzania
            for (MoveEntity move : moves) {
                // Symulacja czasu (opóźnienie między ruchami dla efektu wizualnego)
                Thread.sleep(700); 

                // Aktualizujemy lokalną planszę na podstawie ruchu z bazy
                int r = move.rowC;
                int c = move.colC;
                Stone stone = Stone.valueOf(move.stoneColor); // Konwersja String -> Enum

                System.out.println("Replay: " + stone + " na [" + r + "," + c + "]");

                gameBoard.placeStone(c, r, stone, 0);

                // Wysyłamy zaktualizowaną planszę do klienta
                sendGridToClient();
            }

            // Koniec odtwarzania
            Thread.sleep(2000); // Chwila przerwy na koniec
            observer.sendInt(GameAction.GAME_END_DRAW.getIndex()); // Lub inny sygnał końca
            System.out.println(">>> Zakończono REPLAY.");

        } catch (Exception e) {
            System.err.println("Błąd podczas replayu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { observer.close(); } catch (Exception e) {}
        }
    }

    /**
     * Metoda pomocnicza wysyłająca całą planszę do klienta.
     * Zgodna z protokołem używanym w GameSession.
     */
    private void sendGridToClient() {
        observer.sendInt(GameAction.GAME_SEND_GRID.getIndex());
        Stone[][] currentGrid = gameBoard.getGrid();

        for (int r = 0; r < 19; r++) {
            for (int c = 0; c < 19; c++) {
                Stone s = currentGrid[r][c];
                if (s == Stone.NONE) observer.sendInt(0);
                else if (s == Stone.BLACK) observer.sendInt(1);
                else observer.sendInt(2);
            }
        }
    }
}