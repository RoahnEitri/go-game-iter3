package com.roahacha.gogame;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
        // Scanner do pobierania komend z konsoli serwera
        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("    SERWER GO GAME - SPRING BOOT");
        System.out.println("=========================================");
        System.out.println("Wybierz tryb pracy serwera:");
        System.out.println("[1] Tryb Gry (Oczekiwanie na graczy)");
        System.out.println("[2] Tryb Odtwarzania (Replay z bazy)");
        System.out.print("Twój wybór: ");

        int mode = 0;
        if (scanner.hasNextInt()) {
            mode = scanner.nextInt();
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("\nSerwer uruchomiony na porcie " + port);

            if (mode == 2) {
                runReplayMode(serverSocket, scanner);
            } else {
                runGameMode(serverSocket);
            }

        } catch (IOException ex) {
            System.err.println("Krytyczny błąd serwera: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * TRYB 1: Standardowa rozgrywka.
     * Łączy graczy w pary i uruchamia GameSession.
     */
    private void runGameMode(ServerSocket serverSocket) throws IOException {
        System.out.println(">>> TRYB GRY AKTYWNY. Oczekiwanie na graczy...");

        while (true) {
            // Logika łączenia graczy w pary (PvP)
            // Jeśli chcesz użyć wersji z botem, zmień ten fragment zgodnie z poprzednimi instrukcjami
            System.out.println("Czekam na Gracza 1...");
            Socket firstPlayer = serverSocket.accept();
            System.out.println("Gracz 1 dołączył: " + firstPlayer.getInetAddress().getHostAddress());

            System.out.println("Czekam na Gracza 2...");
            Socket secondPlayer = serverSocket.accept();
            System.out.println("Gracz 2 dołączył: " + secondPlayer.getInetAddress().getHostAddress());

            System.out.println("Para skompletowana. Startuję sesję gry.");

            // Tworzymy sesję gry (PvP) z zapisem do bazy
            GameSession task = new GameSession(firstPlayer, secondPlayer, gameService);
            new Thread(task).start();
        }
    }

    /**
     * TRYB 2: Odtwarzanie historii.
     * Pyta o ID gry, a następnie wysyła powtórkę każdemu, kto się połączy.
     */
    private void runReplayMode(ServerSocket serverSocket, Scanner scanner) throws IOException {
        System.out.print("Podaj ID gry do odtworzenia (np. 1): ");
        Long gameId = 1L;
        if (scanner.hasNextLong()) {
            gameId = scanner.nextLong();
        }

        System.out.println(">>> TRYB REPLAY AKTYWNY dla gry ID: " + gameId);
        System.out.println("Oczekiwanie na widzów...");

        while (true) {
            // W trybie replay akceptujemy pojedynczych klientów (widzów)
            Socket spectator = serverSocket.accept();
            System.out.println("Dołączył widz: " + spectator.getInetAddress().getHostAddress());

            try {
                // Uruchamiamy sesję replay
                ReplaySession replayTask = new ReplaySession(spectator, gameService, gameId);
                new Thread(replayTask).start();
            } catch (Exception e) {
                System.err.println("Nie udało się uruchomić replayu: " + e.getMessage());
            }
        }
    }
}