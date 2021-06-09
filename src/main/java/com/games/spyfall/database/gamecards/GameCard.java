package com.games.spyfall.database.gamecards;

import lombok.Data;
import org.hibernate.annotations.Type;

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

    @Column(columnDefinition = "clob")
    private Byte[] image;

}
