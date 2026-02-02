package com.roahacha.gogame;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Główna klasa serwera gry Go, uruchamiana jako aplikacja Spring Boot.
 * <p>
 * Odpowiada za:
 * <ul>
 * <li>Inicjalizację kontekstu Springa (wstrzykiwanie zależności, obsługa bazy danych).</li>
 * <li>Nasłuchiwanie na określonym porcie TCP na połączenia przychodzące.</li>
 * <li>Łączenie graczy w pary i uruchamianie dla nich osobnych sesji gier ({@link GameSession}).</li>
 * </ul>
 * Implementacja interfejsu {@link CommandLineRunner} pozwala na wykonanie logiki serwera
 * (pętli akceptującej graczy) natychmiast po starcie kontekstu aplikacji.
 */
@SpringBootApplication
public class Server implements CommandLineRunner {

    /** Port, na którym serwer nasłuchuje połączeń przychodzących. */
    private static final int port = 12543;

    /**
     * Serwis logiki gry wstrzykiwany automatycznie przez Springa (@Autowired).
     * <p>
     * Jest niezbędny do komunikacji z bazą danych (tworzenie gier, zapisywanie ruchów).
     * Przekazywany jest do każdej nowej instancji {@link GameSession}.
     */
    @Autowired
    private GameService gameService;

    /**
     * Główny punkt wejścia do aplikacji.
     * <p>
     * Uruchamia framework Spring Boot, który konfiguruje aplikację, łączy się z bazą danych,
     * a następnie wywołuje metodę {@link #run(String...)}.
     *
     * @param args Argumenty wiersza poleceń.
     */
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    /**
     * Metoda uruchamiana automatycznie po załadowaniu kontekstu Springa.
     * Zawiera główną pętlę serwera akceptującą połączenia sieciowe.
     * <p>
     * Działanie:
     * <ol>
     * <li>Otwiera {@link ServerSocket} na zdefiniowanym porcie.</li>
     * <li>W nieskończonej pętli oczekuje na połączenie dwóch graczy (Gracz 1 i Gracz 2).</li>
     * <li>Po skompletowaniu pary tworzy obiekt {@link GameSession}, wstrzykując do niego serwis bazy danych.</li>
     * <li>Uruchamia sesję gry w nowym, osobnym wątku.</li>
     * </ol>
     *
     * @param args Argumenty przekazane przy starcie aplikacji.
     */
    @Override
    public void run(String... args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started (Spring Boot active) at socket " + port);


            // odkomentuj jeśli chcesz zobaczyć replay konkretnej gry przy starcie serwera----------------------------------------------------------------------------------------
            // gameService.replayGame(1L);
            //ważne!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            while (true) {
                System.out.println("Waiting for players to join...");


                Socket firstPlayer = serverSocket.accept();
                System.out.println("Player 1 joined: " + firstPlayer.getInetAddress().getHostAddress());


                Socket secondPlayer = serverSocket.accept();
                System.out.println("Player 2 joined: " + secondPlayer.getInetAddress().getHostAddress());


                GameSession task = new GameSession(firstPlayer, secondPlayer, gameService);
                Thread t1 = new Thread(task);
                t1.start();
            }
        } catch (IOException ex) {
            System.err.println("Critical server error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}