package com.roahacha.gogame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Główna klasa serwera gry Go, implementująca wzorzec Singleton.
 * <p>
 * Serwer odpowiada za nasłuchiwanie na określonym porcie, akceptowanie połączeń 
 * od graczy i łączenie ich w pary w ramach osobnych sesji gry.
 */
public class Server {
    /** Port, na którym serwer nasłuchuje połączeń przychodzących. */
    private static final int port = 12543;

    /** Jedyna instancja klasy Server (Singleton). */
    private static Server instance;

    /**
     * Prywatny konstruktor klasy Server, uniemożliwiający tworzenie instancji 
     * spoza klasy.
     */
    private Server() {}

    /**
     * Zwraca jedyną instancję serwera. Jeśli instancja nie istnieje, tworzy ją.
     * * @return Instancja klasy {@link Server}.
     */
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    /**
     * Główny punkt wejścia do aplikacji serwera.
     * * @param args Argumenty wiersza poleceń (nieużywane).
     */
    public static void main(String[] args) {
        Server.getInstance().start();
    }

    /**
     * Uruchamia logikę sieciową serwera.
     * <p>
     * Metoda otwiera gniazdo serwerowe ({@link ServerSocket}) i w nieskończonej pętli 
     * oczekuje na dwóch graczy. Po połączeniu pary graczy tworzy nową instancję 
     * {@link GameSession} i uruchamia ją w osobnym wątku.
     * * @see GameSession
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started at socket " + port);

            while (true) {
                System.out.println("Waiting for players to join...");

                //connection to player1
                Socket firstPlayer = serverSocket.accept();
                System.out.println("Player 1 joined. Player 1's IP address: " + firstPlayer.getInetAddress().getHostAddress());

                //connection to player2
                Socket secondPlayer = serverSocket.accept();
                System.out.println("Player 2 joined. Player 2's IP address: " + secondPlayer.getInetAddress().getHostAddress());

                GameSession task = new GameSession(firstPlayer, secondPlayer);
                Thread t1 = new Thread(task);
                t1.start();
            }
        } catch (IOException ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }
}
