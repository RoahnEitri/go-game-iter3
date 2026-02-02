package com.roahacha.gogame;

import com.roahacha.gogame.Common.Stone;
import com.roahacha.gogame.database.GameEntity;
import com.roahacha.gogame.database.GameRepository;
import com.roahacha.gogame.database.MoveEntity;
import com.roahacha.gogame.database.MoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Serwis domeny biznesowej (Service Layer) odpowiedzialny za logikę zapisu i odczytu historii gier.
 * <p>
 * Klasa ta pełni rolę pośrednika między logiką gry (GameSession) a warstwą danych (Repositories).
 * Oznaczona adnotacją {@link Service}, co sprawia, że jest zarządzana przez kontener Springa
 * i może być wstrzykiwana do innych komponentów (np. do Servera/GameSession).
 */
@Service
public class GameService {

    /** Repozytorium do zarządzania encjami gry (tabela GameEntity). */
    @Autowired
    private GameRepository gameRepository;

    /** Repozytorium do zarządzania encjami ruchów (tabela MoveEntity). */
    @Autowired
    private MoveRepository moveRepository;

    /**
     * Tworzy nowy wpis o grze w bazie danych.
     * <p>
     * Metoda wywoływana jest na początku nowej sesji gry. Inicjalizuje obiekt {@link GameEntity},
     * zapisuje go w bazie (co nadaje mu unikalne ID i czas rozpoczęcia), a następnie zwraca to ID.
     *
     * @return Unikalny identyfikator (Primary Key) nowo utworzonej gry.
     */
    public Long startNewGame() {
        GameEntity game = new GameEntity();
        game = gameRepository.save(game);
        System.out.println(">>> BAZA DANYCH: Rozpoczęto nową grę ID: " + game.id);
        return game.id;
    }

    /**
     * Zapisuje pojedynczy ruch gracza w bazie danych.
     * <p>
     * Metoda wyszukuje grę o podanym ID. Jeśli gra istnieje, tworzy nową encję {@link MoveEntity}
     * zawierającą szczegóły ruchu (kolejność, współrzędne, kolor kamienia) i zapisuje ją w tabeli ruchów.
     *
     * @param gameId    Identyfikator gry, do której należy ruch.
     * @param moveOrder Numer kolejny ruchu (dla zachowania chronologii).
     * @param row       Współrzędna wiersza na planszy.
     * @param col       Współrzędna kolumny na planszy.
     * @param stone     Kolor kamienia (Stone.BLACK lub Stone.WHITE).
     */
    public void saveMove(Long gameId, int moveOrder, int row, int col, Stone stone) {

        gameRepository.findById(gameId).ifPresent(game -> {
            MoveEntity move = new MoveEntity(game, moveOrder, row, col, stone.toString());
            moveRepository.save(move);
            System.out.println(">>> BAZA DANYCH: Zapisano ruch nr " + moveOrder);
        });
    }

    /**
     * Odtwarza przebieg zakończonej gry na podstawie danych z bazy.
     * <p>
     * Pobiera wszystkie ruchy powiązane z danym ID gry, sortuje je chronologicznie
     * i wypisuje w konsoli serwera.
     * <p>
     * Adnotacja {@link Transactional} jest wymagana, aby zapewnić spójność transakcji
     * podczas pobierania powiązanych rekordów (List&lt;MoveEntity&gt;), które mogą być ładowane leniwie (Lazy Loading).
     *
     * @param gameId Identyfikator gry do odtworzenia.
     */
    @Transactional
    public void replayGame(Long gameId) {
        GameEntity game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            System.out.println(">>> REPLAY: Nie znaleziono gry o ID: " + gameId);
            return;
        }
        System.out.println("=========================================");
        System.out.println(">>> REPLAY GRY ID: " + gameId + " (Data: " + game.startedAt + ")");

        List<MoveEntity> moves = game.moves;

        moves.sort(Comparator.comparingInt(m -> m.moveOrder));

        for (MoveEntity m : moves) {
            System.out.println("Ruch " + m.moveOrder + ": Gracz " + m.stoneColor + " stawia na [" + m.rowC + ", " + m.colC + "]");

        }
        System.out.println("=========================================");
    }
}