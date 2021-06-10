package com.games.spyfall.services.websocket;

import com.games.spyfall.config.jwt.JwtProvider;
import com.games.spyfall.entities.*;
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
import java.util.Map;

@Slf4j
public class TextWebsocket extends TextWebSocketHandler {
    @Autowired
    private GameService gameService;

    @Autowired
    private Gson json;

    @Autowired
    JwtProvider jwtProvider;

    List<WebSocketSession> clients;

    public TextWebsocket() {
        this.clients = new ArrayList<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        clients.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Message parsedMessage = this.json.fromJson(message.getPayload().toString(), Message.class);
        log.info("parsed message: " + parsedMessage);
        if(parsedMessage.getEvent().equals(WsMessageType.PING)){
            session.sendMessage(new TextMessage(json.toJson(new ResponseMessage(WsResponseType.PING, "string", "PONG"))));
            return;
        }
        String token = parsedMessage.getToken();
        if (!jwtProvider.validateToken(token)) {
            session.sendMessage(new TextMessage(json.toJson(new ResponseMessage(WsResponseType.ERROR, "string", "Token auth error"))));
        }
        switch (parsedMessage.getEvent()) {
            case REGISTER: {
                gameService.addPlayer(token, session);
                break;
            }
            case STARTGAME: {
                gameService.startGame(token);
                break;
            }
            case QUESTION: {
                Map<String, String> data = (Map<String, String>) parsedMessage.getData();
                Question question = new Question();
                question.setSource(data.get("source"));
                question.setTarget(data.get("target"));
                question.setQuestion(data.get("question"));
                gameService.askQuestion(token, question);
                break;
            }
            case ANSWER: {
                Map<String, String> data = (Map<String, String>) parsedMessage.getData();
                Answer answer = new Answer();
                answer.setQuestion(data.get("question"));
                answer.setAnswer(data.get("answer"));
                gameService.answerQuestion(token, answer);
                break;
            }
            case SUSPECT: {
                Map<String, String> data = (Map<String, String>) parsedMessage.getData();
                Suspect suspect = new Suspect();
                suspect.setAction(SuspectAction.valueOf(data.get("suspectAction")));
                suspect.setSuspected(data.get("suspected"));
                suspect.setSuspecting(data.get("suspecting"));
                gameService.suspectPlayer(token, suspect);
                break;
            }
            case GUESSLOCATION: {
                gameService.spyGuess(token, parsedMessage.getData().toString());
                break;
            }
            default: {
                session.sendMessage(new TextMessage(json.toJson(new ResponseMessage(WsResponseType.ERROR, "string", "unexpected event type: " + parsedMessage.getEvent()))));
                break;
            }

        }
    }
}
