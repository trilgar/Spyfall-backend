package com.games.spyfall.entities;

import lombok.Data;

@Data
public class Message {
    private WsMessageType event;
    private String token;
    private Object data;
}
