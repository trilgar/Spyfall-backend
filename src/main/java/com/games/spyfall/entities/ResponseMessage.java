package com.games.spyfall.entities;

import lombok.Data;

@Data
public class ResponseMessage {
    private final WsResponseType event;
    private final Object data;
}
