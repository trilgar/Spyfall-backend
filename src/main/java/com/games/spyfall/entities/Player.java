package com.games.spyfall.entities;

import lombok.Data;

import java.util.Set;

@Data
public class Player {
    private String username;
    private Set<String> suspecting;
}
