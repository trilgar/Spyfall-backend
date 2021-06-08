package com.games.spyfall.entities;

import lombok.Data;

@Data
public class Question {
    private int number;
    private int idSource;
    private String question;
    private int idTarget;
    private String response;
}
