package com.roahacha.gogame.Common;

/**
 * Typ wyliczeniowy definiujący protokół komunikacyjny między klientem a serwerem.
 * <p>
 * Zawiera akcje gracza (PLAYER_*) oraz komunikaty statusowe serwera (GAME_*).
 */
public enum GameAction {
    /** Żądanie wykonania ruchu przez gracza. */
    PLAYER_MOVE(1),
    /** Rezygnacja z ruchu w danej turze. */
    PLAYER_PASS(2),
    /** Poddanie partii przez gracza. */
    PLAYER_SURRENDER(3),
    /** Informacja o wyjściu gracza z aplikacji. */
    PLAYER_QUIT(4),
    /** Sygnał serwera przypisujący graczowi kolor czarny. */
    GAME_STONE_BLACK(5),
    /** Sygnał serwera przypisujący graczowi kolor biały. */
    GAME_STONE_WHITE(6),
    /** Polecenie aktualizacji widoku planszy u klienta. */
    GAME_SEND_GRID(7),
    /** Informacja o wygranej. */
    GAME_END_WIN(8),
    /** Informacja o przegranej. */
    GAME_END_LOSS(9),
    /** Informacja o remisie. */
    GAME_END_DRAW(10),
    /** Sygnał informujący gracza, że nadeszła jego tura. */
    GAME_YOUR_TURN(11),
    /** Powiadomienie o próbie wykonania niedozwolonego ruchu. */
    GAME_INCORRENT_MOVE(12),
    /** Status dla nierozpoznanych komend. */
    UNKNOWN(-1);

    private final int index;

    GameAction(int index) {
        this.index = index;
    }
    
    /** @return Indeks liczbowy akcji przesyłany przez sieć. */
    public int getIndex() {
        return index;
    }

    /**
     * Konwertuje indeks liczbowy na odpowiadający mu obiekt GameAction.
     * @param index Liczba odebrana z sieci.
     * @return Obiekt GameAction lub UNKNOWN, jeśli indeks nie pasuje.
     */
    public static GameAction fromIndex(int index) {
        for (GameAction action : values()) {
            if (action.index == index) {
                return action;
            }
        }
        return UNKNOWN;
    }
}