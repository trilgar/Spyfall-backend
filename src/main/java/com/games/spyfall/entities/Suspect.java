package com.games.spyfall.entities;

import lombok.Data;

@Data
public class Suspect {
    private SuspectAction action;
    private String suspecting;
    private String suspected;
}
