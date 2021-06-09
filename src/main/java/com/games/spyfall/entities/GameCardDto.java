package com.games.spyfall.entities;

import com.games.spyfall.database.gamecards.GameCard;
import lombok.Data;

@Data
public class GameCardDto {
    private final String questionGranted;
    private final Card gameCard;
}
