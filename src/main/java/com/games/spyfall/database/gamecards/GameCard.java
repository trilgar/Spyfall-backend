package com.games.spyfall.database.gamecards;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "game_card")
@Data
public class GameCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Lob
    @Column(columnDefinition = "BLOB NOT NULL")
    private Byte[] image;

}
