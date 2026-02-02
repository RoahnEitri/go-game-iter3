package com.roahacha.gogame.database;

import jakarta.persistence.*;

@Entity
public class MoveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public int moveOrder; // Numer kolejny ruchu (np 1)
    public int rowC;      // Współrzędna wiersza
    public int colC;      // Współrzędna kolumny
    public String stoneColor; // "BLACK" lub "WHITE"

    @ManyToOne
    @JoinColumn(name = "game_id")
    public GameEntity game; // Do której gry należy ten ruch

    public MoveEntity() {}

    public MoveEntity(GameEntity game, int order, int row, int col, String color) {
        this.game = game;
        this.moveOrder = order;
        this.rowC = row;
        this.colC = col;
        this.stoneColor = color;
    }
}