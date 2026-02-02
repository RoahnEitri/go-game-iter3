package com.roahacha.gogame.database;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public LocalDateTime startedAt;


    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    public List<MoveEntity> moves;

    public GameEntity() {
        this.startedAt = LocalDateTime.now();
    }
}