package com.roahacha.gogame.Common;

/**
 * Typ wyliczeniowy reprezentujący rodzaje kamieni na planszy do gry w Go.
 */
public enum Stone {
    /** Czarny kamień należący do pierwszego gracza. */
    BLACK,
    /** Biały kamień należący do drugiego gracza. */
    WHITE,
    /** Oznaczenie braku kamienia na danym skrzyżowaniu (puste pole). */
    NONE;

    private Stone opposite;

    static {
        BLACK.opposite = WHITE;
        WHITE.opposite = BLACK;
        NONE.opposite = NONE;
    }

    /**
     * Zwraca kolor kamienia przeciwny do bieżącego.
     */
    public Stone oppositeStone() {
        return opposite;
    }
}