package com.games.spyfall.entities;

import com.games.spyfall.database.gamecards.GameCard;
import lombok.Data;

@Data
public class Card {
    private int id;
    private String name;
    private String pictureUrl;

    public Card(GameCard gameCard) {
        this.id = gameCard.getId();
        this.name = gameCard.getName();
        this.pictureUrl = "api/images/" + gameCard.getId();
    }
}
