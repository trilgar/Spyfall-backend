package com.games.spyfall.services.websocket;

import com.games.spyfall.entities.Message;
import com.games.spyfall.services.game.GameService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TextWebsocket extends TextWebSocketHandler {
    @Autowired
    private GameService gameService;

    @Autowired
    private Gson json;

    List<WebSocketSession> clients;

    public TextWebsocket() {
        this.clients = new ArrayList<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        clients.add(session);
        session.sendMessage(new TextMessage("connection established"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Message parsedMessage = this.json.fromJson(message.getPayload().toString(), Message.class);
        log.info("parsed message" + parsedMessage.getData());
        switch (parsedMessage.getEvent()) {
            case REGISTER:{
                String token = parsedMessage.getData().toString();
                gameService.addPlayer(token, session);
            }

        }
    }
}
