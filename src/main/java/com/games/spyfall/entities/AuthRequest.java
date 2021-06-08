package com.games.spyfall.entities;

import lombok.Data;

@Data
public class AuthRequest {
    private String login;
    private String password;
}