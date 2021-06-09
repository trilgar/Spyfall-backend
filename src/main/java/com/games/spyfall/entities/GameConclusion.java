package com.games.spyfall.entities;

import lombok.Data;

@Data
public class GameConclusion {
    private Winner winner;
    private String conclusion;
    private String spyName;
    private String locationName;
}
