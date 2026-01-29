package com.roahacha.gogame;

import com.roahacha.gogame.Common.Stone;
import com.roahacha.gogame.Common.GameAction;

/**
 * Interfejs definiujący kontrakt dla obsługi zdarzeń gry otrzymywanych z serwera.
 * <p>
 * Jest to część wzorca Obserwator. Klasa implementująca ten interfejs (zazwyczaj kontroler klienta)
 * reaguje na asynchroniczne komunikaty z sieci, takie jak aktualizacja planszy,
 * rozpoczęcie gry czy komunikaty o błędach, w celu odświeżenia interfejsu użytkownika (GUI).
 */
public interface GameClientObserver {
    /**
     * Wywoływana w momencie rozpoczęcia gry.
     * <p>
     * Służy do poinformowania klienta o przydzielonym mu kolorze kamieni,
     * co pozwala ustalić, czy gracz wykonuje ruch jako pierwszy (Czarne).
     *
     * @param myColor Kolor kamieni przypisany do tego gracza ({@link Stone#BLACK} lub {@link Stone#WHITE}).
     */
    void onGameStart(Stone myColor);

    /**
     * Wywoływana, gdy stan planszy ulegnie zmianie.
     * <p>
     * Metoda ta otrzymuje pełny obraz planszy, co pozwala na jej przerysowanie
     * po wykonaniu ruchu lub po usunięciu zbitych kamieni.
     *
     * @param grid Dwuwymiarowa tablica reprezentująca aktualny układ kamieni na planszy.
     */
    void onBoardUpdate(Stone[][] grid);

    /**
     * Wywoływana w odpowiedzi na specyficzne zdarzenia lub statusy gry.
     * <p>
     * Obsługuje zdarzenia inne niż zmiana planszy, takie jak:
     * informacja o turze gracza ({@code GAME_YOUR_TURN}), błąd ruchu ({@code GAME_INCORRENT_MOVE})
     * lub zakończenie gry ({@code GAME_END_WIN}).
     *
     * @param action Typ zdarzenia (akcji) przesłany przez serwer.
     */
    void onGameAction(GameAction action);
}
