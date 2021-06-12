package com.games.spyfall.database.maps;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "maps")
public class GameMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Column(columnDefinition = "clob")
    private Byte[] image;
}
