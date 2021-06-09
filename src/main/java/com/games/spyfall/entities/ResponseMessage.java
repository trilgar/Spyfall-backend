package com.games.spyfall.entities;

import lombok.Data;

@Data
public class ResponseMessage {
    private final WsResponseType event;
    private final String dataType;
    private final Object data;
}
