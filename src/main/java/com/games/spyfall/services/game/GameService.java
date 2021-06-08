package com.games.spyfall.services.game;


import com.games.spyfall.entities.Question;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

public interface GameService {
    void addPlayer(String token, WebSocketSession session);

    void setHostName(String token);

    void startGame(String token) throws IOException;

    WebSocketSession getSessionByName(String name);

    void sendMessageToUsers(List<WebSocketSession> sessions, Object payload);

    void sendMessageToAll(Object payload);

    Boolean getGameReadyStatus();

    void askQuestion(Question question);
}
