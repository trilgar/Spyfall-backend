package com.games.spyfall.services.game;


import com.games.spyfall.entities.Answer;
import com.games.spyfall.entities.Question;
import com.games.spyfall.entities.Suspect;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

public interface GameService {
    void addPlayer(String token, WebSocketSession session) throws IOException;

    void setHostName(String token);

    void startGame(String token) throws IOException;

    void restartGame(String token) throws IOException;

    void askQuestion(String token, Question question) throws IOException;

    void answerQuestion(String token, Answer answer) throws IOException;

    void suspectPlayer(String token, Suspect suspect) throws IOException;

    void endGame(String suspectedSpyName) throws IOException;

    void spyGuess(String token, String locationName) throws IOException;

    WebSocketSession getSessionByName(String name);

    void sendMessageToUsers(List<WebSocketSession> sessions, Object payload);

    void sendMessageToAll(Object payload);

    Boolean getGameReadyStatus();

    void askQuestion(Question question);

    void sendConnected(WebSocketSession session) throws IOException;
}
