package com.roahacha.gogame.Common;

/**
 * Klasa GameBoard zarządza logiką gry Go po stronie serwera. 
 * Odpowiada za weryfikację zasad, wykonywanie ruchów oraz obliczanie punktacji.
 */
public class GameBoard extends Board {
    /** Tablica pomocnicza śledząca pola odwiedzone podczas algorytmów rekurencyjnych. */
    boolean[][] tileUsed = new boolean[gridWidth][gridWidth];

    /** Kopie stanu planszy wykorzystywane do weryfikacji reguły KO. */
    private Stone[][] previousGrid = null;
    private Stone[][] morePreviousGrid = null;

    /**
     * Prosty konstruktor klasy GameBoard.
     * Inicjalizuje planszę oraz czyści tablicę pomocniczą.
     */
    public GameBoard() {
        clearTileUsed();
    }

    /**
     * Zwraca głęboką kopię aktualnego stanu planszy.
     * @return Dwuwymiarowa tablica reprezentująca grid.
     */
    public Stone[][] getGrid() {
        Stone[][] arr = new Stone[gridWidth][gridWidth];
        for (int i = 0; i < gridWidth * gridWidth; i++)
            arr[i/gridWidth][i%gridWidth] = grid[i/gridWidth][i%gridWidth];
        return arr;
    }

    /**
     * Czyści tablicę tileUsed, ustawiając wszystkie wartości na false.
     */
    private void clearTileUsed() {
        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++)
                tileUsed[i][j] = false;
    }

    /**
     * Przesuwa historię stanów planszy o jeden krok wstecz w celu obsługi zasady KO.
     */
    private void updatePreviousGrids() {
        morePreviousGrid = previousGrid;
        previousGrid = getGrid();
    }

    /**
     * Oblicza liczbę "oddechów" (wolnych skrzyżowań) dla grupy kamieni połączonych z punktem (height, length).
     * Wykorzystuje algorytm Flood Fill do przeszukania całej grupy.
     * @param height Wiersz na planszy.
     * @param length Kolumna na planszy.
     * @param stone Kolor sprawdzanej grupy kamieni.
     * @return Całkowita liczba oddechów dla grupy.
     */
    public int numOfBreaths(int height, int length, Stone stone) {

        if (height < 0 || height >= gridWidth || length < 0 || length >= gridWidth)
            return 0;

        if (tileUsed[height][length]) return 0;

        tileUsed[height][length] = true;

        if (grid[height][length] == Stone.NONE) {
            return 1;
        }

        if (grid[height][length] != stone) {
            return 0;
        }

        int breaths = 0;
        int[][] directions = { {-1,0}, {0,1}, {1,0}, {0,-1} };

        for (int[] dir : directions) {
            int newHeight = height + dir[0];
            int newLength = length + dir[1];

            breaths += numOfBreaths(newHeight, newLength, stone);
        }

        return breaths;
    }

    /**
     * Sprawdza, czy postawienie kamienia na danym polu spowoduje zbicie kamieni przeciwnika.
     * @return true, jeśli ruch skutkuje przechwyceniem kamieni oponenta.
     */
    private boolean checkForCapture(int height, int length, Stone stone) {
        clearTileUsed();
        // set temporarily to count breaths of opponent's chains
        // if this move were to happen
        grid[height][length] = stone;
        int[][] directions = { {-1,0}, {0,1}, {1,0}, {0,-1} }; // up, right, down, left
        for (int[] dir : directions) {
            int newHeight = height + dir[0];
            int newLength = length + dir[1];
            if (newHeight >= 0 && newHeight < gridWidth &&
                newLength >= 0 && newLength < gridWidth) {
                    if (numOfBreaths(newHeight, newLength, stone.oppositeStone()) == 0) {
                        grid[height][length] = Stone.NONE;
                        return true;
                    }
            }
            clearTileUsed();
        }
        grid[height][length] = Stone.NONE;
        return false;
    }

    /**
     * Weryfikuje, czy dany ruch jest poprawny pod kątem granic planszy, 
     * dostępności pola oraz zasady samobójstwa.
     * @return true, jeśli ruch spełnia podstawowe wymogi logiczne.
     */
    private boolean checkMoveValidity(int height, int length, Stone stone) {
        if (height < 0 || height >= gridWidth)      return false;
        if (length < 0 || length >= gridWidth)      return false;
        if (grid[height][length] != Stone.NONE)     return false;

        grid[height][length] = stone;
        if (numOfBreaths(height, length, stone) == 0 && !checkForCapture(height, length, stone)) {
            clearTileUsed();
            grid[height][length] = Stone.NONE;
            return false;
        }
        grid[height][length] = Stone.NONE;
        clearTileUsed();
        return true;
    }

    /**
     * Sprawdza, czy ruch narusza regułę KO (zakaz natychmiastowego powtórzenia stanu planszy sprzed 2 ruchów).
     * Wykonuje głęboką kopię planszy w celu symulacji skutków ruchu przed jego zatwierdzeniem.
     * @return true, jeśli ruch narusza zasadę KO.
     */
    private boolean checkKO(int height, int length, Stone stone) {
        // [DIAGNOSTYKA] Sprawdzamy czy mamy historię
        if (morePreviousGrid == null) {
            System.out.println("KO CHECK: Brak historii (morePreviousGrid is null). Ruch dozwolony.");
            return false;
        }

        // 1. Create a DEEP COPY of the current grid to simulate the move
        Stone[][] simulationGrid = new Stone[gridWidth][gridWidth];
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridWidth; j++) {
                simulationGrid[i][j] = this.grid[i][j];
            }
        }

        Stone[][] realGrid = this.grid; // Backup real grid

        // Swap in simulation grid to use existing logic safely
        this.grid = simulationGrid;

        try {
            // Simulate placing the stone
            this.grid[height][length] = stone;

            // Simulate capturing opponents
            if (height - 1 >= 0)            removeIfInvalid(height - 1, length, stone.oppositeStone());
            if (length + 1 < gridWidth)     removeIfInvalid(height, length + 1, stone.oppositeStone());
            if (height + 1 < gridWidth)     removeIfInvalid(height + 1, length, stone.oppositeStone());
            if (length - 1 >= 0)            removeIfInvalid(height, length - 1, stone.oppositeStone());

            // 4. Compare the simulation result with the state from 2 moves ago
            for (int i = 0; i < gridWidth; i++) {
                for (int j = 0; j < gridWidth; j++) {
                    if (this.grid[i][j] != morePreviousGrid[i][j]) {
                        // [DIAGNOSTYKA] Jeśli znajdzie różnicę, wypisze to w konsoli serwera

                        return false; // The layout is different -> No KO -> Move allowed
                    }
                }
            }

            System.out.println("KO CHECK: Wykryto powtórzenie planszy! (KO VIOLATION)");
            return true; // The layout is identical -> KO -> Move forbidden!

        } finally {
            // 5. VERY IMPORTANT: Restore the real board
            this.grid = realGrid;
        }
    }

    /**
     * Usuwa z planszy łańcuchy kamieni o zerowej liczbie oddechów.
     */
    private void removeIfInvalid(int height, int length, Stone stone) {
        if (grid[height][length] != stone) return;

        // chain is still valid
        if (numOfBreaths(height, length, stone) != 0) {
            clearTileUsed();
            return;
        }

        // chain has 0 breaths
        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++) {
                if (tileUsed[i][j] && grid[i][j] == stone)
                    grid[i][j] = Stone.NONE;
            }

        clearTileUsed();
    }

    /**
     * Główna metoda realizująca postawienie kamienia na planszy.
     * Przeprowadza pełną walidację (zasady podstawowe, KO) i aktualizuje historię planszy.
     * @param KORecursionGuard Flaga (zazwyczaj > 0) aktywująca sprawdzanie poprawności i aktualizację historii.
     * @return true, jeśli kamień został pomyślnie umieszczony.
     */
    public boolean placeStone(int height, int length, Stone stone, int KORecursionGuard) {
        if (KORecursionGuard > 0 &&!checkMoveValidity(height, length, stone))   return false;
        if (KORecursionGuard > 0 && checkKO(height, length, stone))             return false;
        grid[height][length] = stone;

        if (height - 1 >= 0)            removeIfInvalid(height - 1, length, stone.oppositeStone());
        if (length + 1 < gridWidth)     removeIfInvalid(height, length + 1, stone.oppositeStone());
        if (height + 1 < gridWidth)     removeIfInvalid(height + 1, length, stone.oppositeStone());
        if (length - 1 >= 0)            removeIfInvalid(height, length - 1, stone.oppositeStone());

        if (KORecursionGuard > 0) updatePreviousGrids();
        return true;
    }

    /**
     * Oblicza punkty dla obu graczy na podstawie terytorium i obecności kamieni.
     * Wykorzystuje stos do identyfikacji zamkniętych obszarów pustych pól.
     * @return Tablica dwuelementowa: [punkty czarnych, punkty białych].
     */
    public int[] calculatePoints() {
        int blackPoints = 0;
        int whitePoints = 0;
        boolean[][] visited = new boolean[gridWidth][gridWidth];
        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++)
                visited[i][j] = false;

        for (int i = 0; i < gridWidth; i++)
            for (int j = 0; j < gridWidth; j++) {
                if (visited[i][j]) continue;
                if (grid[i][j] == Stone.BLACK) {
                    blackPoints++;
                } else if (grid[i][j] == Stone.WHITE) {
                    whitePoints++;
                } else {
                    // empty tile, check surrounding stones
                    boolean touchesBlack = false;
                    boolean touchesWhite = false;
                    int territorySize = 0;

                    java.util.Stack<int[]> stack = new java.util.Stack<>();
                    stack.push(new int[] {i, j});
                    visited[i][j] = true;
                    territorySize++;

                    while (!stack.isEmpty()) {
                        int[] pos = stack.pop();
                        int height = pos[0];
                        int length = pos[1];
                        int[][] directions = { {-1,0}, {0,1}, {1,0}, {0,-1} }; // up, right, down, left

                        for (int[] dir : directions) {
                            int newHeight = height + dir[0];
                            int newLength = length + dir[1];
                            if (newHeight >= 0 && newHeight < gridWidth &&
                                newLength >= 0 && newLength < gridWidth) {
                                    if (grid[newHeight][newLength] == Stone.BLACK) {
                                        touchesBlack = true;
                                    } else if (grid[newHeight][newLength] == Stone.WHITE) {
                                        touchesWhite = true;
                                    } else  if (!visited[newHeight][newLength]) {
                                        visited[newHeight][newLength] = true;
                                        territorySize++;
                                        stack.push(new int[] {newHeight, newLength});
                                    }
                                }
                        }
                    }
                    if (touchesBlack && !touchesWhite) {
                        blackPoints += territorySize;
                    } else if (touchesWhite && !touchesBlack) {
                        whitePoints += territorySize;
                    }
                }
            }
        return new int[] {blackPoints, whitePoints};
    }

}
