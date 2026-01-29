package com.roahacha.gogame;

import com.roahacha.gogame.Common.*;
import javafx.application.Platform;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;

/**
 * Zarządzanie klientem gry Go.
 * <p>
 * Działa jako kontroler łączący interfejs graficzny ({@link GUI})
 * i warstwę sieciową ({@link ServerProxy}). Obsługuje walidację danych wejściowych (na podstawie tur),
 * wysyła komendy do serwera i aktualizuje widok na podstawie odpowiedzi serwera.
 */
public class Client implements GameClientObserver {
    final int port = 12543;
    final String host = "localhost";

    private ServerProxy server;
    private GUI gui; // Referencja do okna
    private Stone myStone;
    private boolean myTurn = false;

    /**
     * Tworzy nową instancję klienta.
     *
     * @param gui Główne GUI użytkownika.
     */
    public Client(GUI gui) {
        this.gui = gui;
    }

    /**
     * Tworzy połączenie z serwerem gry.
     * <p>
     * Tworzy {@link ServerProxy} i nasłuchuje.
     */
    public void connectToServer() {
        try {
            Socket socket = new Socket(host, port);
            server = new ServerProxy(new SocketFacade(socket), this);
        } catch (IOException e) {
            System.err.println("Błąd połączenia: " + e.getMessage());
            e.printStackTrace();
            // Informujemy GUI o błędzie
            gui.updateInfo("Błąd połączenia!");
            gui.updateStatus("Nie można połączyć z serwerem.");
        }
    }

    /**
     * Wysyła prośbe o ruch do serwera.
     *
     * @param row Wysokość na planszy (0-18).
     * @param col Długość na planszy (0-18).
     */
    public void sendMove(int row, int col) {
        // Pozwalamy na ruch tylko, jeśli mamy połączenie i jest nasza tura
        if (server != null && myTurn) {
            server.sendMove(row, col);

            myTurn = false;
            gui.updateStatus("Weryfikacja ruchu...");
        }
    }
    /**
     * Wysyła akcje "PASS" do serwera, pomija ture gracza.
     */
    public void sendPass() {

        if (server != null && myTurn) {
            System.out.println("Wysyłam PAS");
            server.sendAction(GameAction.PLAYER_PASS);

            myTurn = false;
            gui.updateStatus("Spasowałeś. Czekaj na ruch przeciwnika...");
        } else {

            gui.updateStatus("Nie możesz teraz spasować (nie twoja tura).");
        }
    }

    /**
     * Wysyła akcje "SURRENDER" do serwera, koniec gry.
     */
    public void sendSurrender() {
        if (server != null) {
            System.out.println("Wysyłam PODDANIE");
            server.sendAction(GameAction.PLAYER_SURRENDER);

        }
    }

    /**
     * Zamyka połączenie.
     */
    public void close() {
        try {
            if (server != null) server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wywołane na starcie gry. Ustawia kolor gracza i ustala pierwszą ture.
     *
     * @param myColor Kolor przydzielony do gracza (BLACK or WHITE).
     */
    @Override
    public void onGameStart(Stone myColor) {
        this.myStone = myColor;
        this.myTurn = (myColor == Stone.BLACK); // Czarne zaczynają

        gui.updateInfo("Grasz jako: " + (myColor == Stone.BLACK ? "CZARNE" : "BIAŁE"));
        gui.updateStatus(myTurn ? "Twoja tura! Postaw kamień." : "Ruch przeciwnika...");
    }

    /**
     * Aktualizuje GUI z nową planszą.
     *
     * @param grid Nowa plansza.
     */
    @Override
    public void onBoardUpdate(Stone[][] grid) {
        // Upadeting board
        gui.refreshBoard(grid);
        Platform.runLater(() -> {
            if (!myTurn) {
                gui.updateStatus("Ruch przeciwnika...");
            }
        });
    }

    /**
     * Obsługuje różne akcje gry otrzymane z serwera.
     *
     * @param action Syngał otrzymany z serwera.
     */
    @Override
    public void onGameAction(GameAction action) {
        // scenarios
        Platform.runLater(() -> {
            switch (action) {
                case GAME_YOUR_TURN:
                    myTurn = true;
                    gui.updateStatus("TWOJA TURA! Wykonaj ruch lub spasuj.");
                    break;

                case GAME_INCORRENT_MOVE:
                    myTurn = true;
                    gui.updateStatus("Ruch niedozwolony! Spróbuj w innym miejscu.");
                    break;

                case GAME_END_WIN:
                    myTurn = false;
                    gui.updateStatus("WYGRANA!");
                    gui.showEndMessage("Gratulacje! Wygrałeś grę.");
                    break;

                case GAME_END_LOSS:
                    myTurn = false;
                    gui.updateStatus("PRZEGRANA...");
                    gui.showEndMessage("Niestety, przegrałeś.");
                    break;

                case GAME_END_DRAW:
                    myTurn = false;
                    gui.updateStatus("REMIS.");
                    gui.showEndMessage("Gra zakończona remisem.");
                    break;

                case PLAYER_QUIT:
                    myTurn = false;
                    gui.updateStatus("Przeciwnik rozłączył się.");
                    gui.showEndMessage("Przeciwnik opuścił grę.");
                    break;

                default:
                    System.out.println("Nieobsłużona akcja: " + action);
                    break;
            }
        });
    }
}