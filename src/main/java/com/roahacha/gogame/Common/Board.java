package com.roahacha.gogame.Common;

//              length
//          \ 0 1 2
//          0 ○
// height   1   ●
//          2 ○   ○
//
/**
 * Klasa Board reprezentuje fizyczny stan planszy Go.
 * Umożliwia podgląd rozmieszczenia kamieni oraz ich wizualizację tekstową.
 */
public class Board {
    /** Stała szerokość planszy (19x19). */
    public static final int gridWidth = 19;

    /** Macierz przechowująca stan każdego pola na planszy. */
    Stone[][] grid = new Stone[gridWidth][gridWidth];

    /**
     * Inicjalizuje nową, pustą planszę.
     * Wszystkie pola zostają wypełnione wartością {@link Stone#NONE}.
     */
    public Board() {
        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++)
                grid[i][j] = Stone.NONE;
    }

    /**
     * Pobiera kamień znajdujący się na określonej pozycji.
     * @param height Indeks wiersza (wysokość).
     * @param length Indeks kolumny (długość).
     * @return Obiekt {@link Stone} znajdujący się na podanych współrzędnych.
     */
    public Stone getStone(int height, int length) {
        return grid[height][length];
    }

    /**
     * Zwraca rozmiar boku planszy.
     * @return Wartość stałej {@link #gridWidth}.
     */
    public int getSize() {
        return gridWidth;
    }

    /**
     * Aktualizuje całą macierz pól planszy.
     * Metoda używana do synchronizacji stanu po wykonaniu ruchu.
     * @param arr Nowa tablica dwuwymiarowa kamieni do skopiowania.
     */
    public void updateGrid(Stone arr[][]) {
        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++)
                grid[i][j] = arr[i][j];
    }

    /**
     * Tworzy tekstową reprezentację planszy w formacie ASCII.
     * Wykorzystuje kody ANSI do kolorowania tła oraz znaki specjalne dla kamieni białych (○) i czarnych (●).
     * @return Sformatowany ciąg znaków reprezentujący widok planszy.
     */
    @Override
    public String toString() {
        final String changeColor = "\033[0;30m\033[43m";
        final String revert = "\33[0m";

        String output = "" + changeColor;
        output += "╔";
        for (int i = 0; i <= 2 * gridWidth; i++) output += "═";
        output += "╗" + revert + "\n";
        for (int i = 0; i < gridWidth; i++) {
            output += changeColor + "║ ";
            for (int j = 0; j < gridWidth; j++) {
                switch (grid[i][j]) {
                    case WHITE:
                        output += "○ ";     // ○ ◯
                        break;
                    case BLACK:
                        output += "● ";     // ● ⬤
                        break;
                    default:
                        output += "  ";
                        break;
                }
            }
            output += "║" + revert + "\n";
        }
        output += changeColor + "╚";
        for (int i = 0; i <= 2 * gridWidth; i++) output += "═";
        output += "╝" + revert + "\n";

        return output;
    }
}
