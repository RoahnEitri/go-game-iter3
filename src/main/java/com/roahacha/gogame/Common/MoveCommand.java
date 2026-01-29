package com.roahacha.gogame.Common;

/**
 * Konkretna implementacja wzorca Polecenie (Command) odpowiadająca za wykonanie ruchu na planszy.
 * <p>
 * Przechowuje stan niezbędny do wykonania próby postawienia kamienia i pozwala na 
 * weryfikację, czy operacja zakończyła się sukcesem.
 */
public class MoveCommand implements Command {
    private GameBoard board;
    private int row, col;
    private Stone stone;
    private boolean success;

    /**
     * Konstruuje polecenie ruchu.
     * @param board Plansza, na której ma zostać wykonany ruch.
     * @param row Indeks wiersza (height).
     * @param col Indeks kolumny (length).
     * @param stone Kolor kamienia gracza wykonującego ruch.
     */
    public MoveCommand(GameBoard board, int row, int col, Stone stone) {
        this.board = board;
        this.row = row;
        this.col = col;
        this.stone = stone;
    }

    /**
     * Wykonuje próbę postawienia kamienia na planszy z uwzględnieniem zasad gry (np. KO).
     * Wynik operacji jest zapisywany w polu success.
     */
    @Override
    public void execute() {
        success = board.placeStone(row, col, stone, 1);
    }

    /**
     * Sprawdza, czy ruch został pomyślnie zaakceptowany przez logikę planszy.
     * @return {@code true} jeśli kamień został postawiony; {@code false} jeśli ruch był niedozwolony.
     */
    public boolean isSuccessful() {
        return success;
    }
    
}
