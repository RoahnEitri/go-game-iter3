package com.roahacha.gogame;

import com.roahacha.gogame.Common.GameAction;
import java.util.Random;

/**
 * Specjalna wersja Klienta dedykowana dla Bota.
 * Przechwytuje zdarzenia sieciowe i reaguje na nie automatycznie.
 */
public class BotClient extends Client {

    private final Random random = new Random();
    private final int SIZE = 19;

    public BotClient(GUI gui) {
        super(gui);
    }

    /**
     * Nadpisujemy metodę obsługi akcji z serwera.
     * To tutaj "mózg" bota decyduje co zrobić.
     */
    @Override
    public void onGameAction(GameAction action) {
        // Najpierw wywołaj oryginalną logikę (aktualizacja GUI, komunikatów)
        // Dzięki temu na ekranie nadal widać co się dzieje.
        super.onGameAction(action);

        // Logika Bota
        // Reagujemy tylko na konkretne sygnały z Enuma GameAction
        if (action == GameAction.GAME_YOUR_TURN || action == GameAction.GAME_INCORRENT_MOVE) {

            // Uruchamiamy wątek, żeby nie zamrozić GUI podczas "myślenia" (sleep)
            new Thread(() -> {
                try {
                    // Małe opóźnienie dla naturalności rozgrywki
                    Thread.sleep(500); 
                    makeRandomMove();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void makeRandomMove() {
        int r = random.nextInt(SIZE);
        int c = random.nextInt(SIZE);

        System.out.println("[BOT] Próba ruchu: " + r + ", " + c);
        // Korzystamy z metody sendMove dostępnej w klasie rodzica (Client)
        this.sendMove(r, c);
    }
}