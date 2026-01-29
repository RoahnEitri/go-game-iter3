package com.roahacha.gogame;

import com.roahacha.gogame.Common.*;

import java.io.IOException;
import java.net.Socket;
import com.roahacha.gogame.Common.*;


/**
 * Zarządza pojedynczą sesją gry w Go między dwoma graczami.
 * <p>
 * Klasa ta działa w osobnym wątku (implementuje {@link Runnable}) i odpowiada za:
 * <ul>
 * <li>Utrzymywanie połączenia z dwoma graczami (Czarnym i Białym).</li>
 * <li>Synchronizację tur i przesyłanie ruchów.</li>
 * <li>Obsługę logiki gry (ruch, pasowanie, poddanie się).</li>
 * <li>Zliczanie punktów i wyłanianie zwycięzcy po zakończeniu gry.</li>
 * </ul>
 */
public class GameSession implements Runnable {

    private final PlayerController    firstPlayer;
    private final PlayerController    secondPlayer;
    private GameBoard gameBoard;

    /**
     * Inicjalizuje nową sesję gry.
     * <p>
     * Tworzy kontrolery dla obu graczy, przypisując pierwszemu socketowi kolor Czarny,
     * a drugiemu Biały. Inicjalizuje również pustą planszę gry.
     *
     * @param firstPlayer  Gniazdo połączenia dla pierwszego gracza (Czarne kamienie).
     * @param secondPlayer Gniazdo połączenia dla drugiego gracza (Białe kamienie).
     * @throws IOException Jeśli wystąpi błąd podczas tworzenia kontrolerów graczy.
     */
    public GameSession(Socket firstPlayer, Socket secondPlayer) throws IOException {
        this.firstPlayer = new PlayerController(firstPlayer, Stone.BLACK);
        this.secondPlayer = new PlayerController(secondPlayer, Stone.WHITE);
        this.gameBoard = new GameBoard();
    }

    /**
     * Główna pętla gry uruchamiana w nowym wątku.
     * <p>
     * Metoda ta:
     * <ol>
     * <li>Rozpoczyna grę, informując pierwszego gracza o jego turze.</li>
     * <li>W pętli oczekuje na akcję aktualnego gracza (Ruch, Pas, Poddanie).</li>
     * <li>Przełącza tury po poprawnym ruchu lub spasowaniu.</li>
     * <li>Kończy grę, gdy obaj gracze spasują (liczenie punktów) lub ktoś się podda/rozłączy.</li>
     * </ol>
     */
    @Override
    public void run() {
        try (firstPlayer; secondPlayer) {
            // Informacje o kamieniach dla gracza
            // 1 = Czarne, 2 = Białe
            //firstPlayer.sendAction(GameAction.GAME_STONE_BLACK);
            //secondPlayer.sendAction(GameAction.GAME_STONE_WHITE);
            firstPlayer.sendAction(GameAction.GAME_YOUR_TURN);

            boolean gameLoop =  true;
            boolean isBlackTurn = true;
            int playerPassCount = 0;

            // Main game loop
            while (gameLoop){
                PlayerController currentPlayer =    isBlackTurn ? firstPlayer : secondPlayer;
                PlayerController opponentPlayer =   isBlackTurn ? secondPlayer : firstPlayer;

                GameAction action = currentPlayer.reciveAction();

                switch (action) {
                    case PLAYER_MOVE:
                        playerPassCount = 0;
                        if (handleMove(currentPlayer, opponentPlayer))
                            isBlackTurn = !isBlackTurn;
                        break;
                    case PLAYER_PASS:
                        playerPassCount++;
                        opponentPlayer.sendAction(GameAction.GAME_YOUR_TURN);
                        isBlackTurn = !isBlackTurn;
                        break;
                    case PLAYER_SURRENDER:
                        playerPassCount = 0;
                        opponentPlayer.sendAction(GameAction.GAME_END_WIN);
                        currentPlayer.sendAction(GameAction.GAME_END_LOSS);
                        gameLoop = false;
                        break;
                    default:    // QUIT or UNKNOWN
                        opponentPlayer.sendAction(GameAction.GAME_END_WIN);
                        gameLoop = false;
                        break;
                }
                if (playerPassCount >= 2) {
                    gameLoop = false;
                    int points[] = gameBoard.calculatePoints();
                    if (points[0] > points[1]) {
                        firstPlayer.sendAction(GameAction.GAME_END_WIN);
                        secondPlayer.sendAction(GameAction.GAME_END_LOSS);
                    } else if (points[1] > points[0]) {
                        firstPlayer.sendAction(GameAction.GAME_END_LOSS);
                        secondPlayer.sendAction(GameAction.GAME_END_WIN);
                    } else {
                        firstPlayer.sendAction(GameAction.GAME_END_DRAW);
                        secondPlayer.sendAction(GameAction.GAME_END_DRAW);
                    }
                }
               
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Obsługuje logikę wykonania ruchu przez gracza.
     * <p>
     * Pobiera współrzędne ruchu, próbuje wykonać go na planszy (weryfikując poprawność zasad,
     * takich jak brak oddechów czy KO). Jeśli ruch jest poprawny, aktualizuje stan planszy
     * u obu graczy. W przeciwnym razie wysyła informację o błędzie.
     *
     * @param current  Kontroler gracza wykonującego ruch.
     * @param opponent Kontroler przeciwnika (do aktualizacji widoku).
     * @return {@code true} jeśli ruch był poprawny i został wykonany; {@code false} w przeciwnym razie.
     */
    private boolean handleMove(PlayerController current, PlayerController opponent) {
        int[] cords = current.reciveMove();
        int row = cords[0];
        int col = cords[1];

        MoveCommand command = new MoveCommand(gameBoard, row, col, current.getStone());
        command.execute();

        if (command.isSuccessful()) {
            current.sendAction(GameAction.GAME_SEND_GRID);
            current.sendBoardState(gameBoard);
            opponent.sendAction(GameAction.GAME_SEND_GRID);
            opponent.sendBoardState(gameBoard);
            opponent.sendAction(GameAction.GAME_YOUR_TURN);
            return true;
        }
        current.sendAction(GameAction.GAME_INCORRENT_MOVE);
        return false;
    }

}
