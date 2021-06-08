package com.games.spyfall.services.game;

import com.games.spyfall.config.jwt.JwtProvider;
import com.games.spyfall.database.gamecards.GameCard;
import com.games.spyfall.database.gamecards.GameCardEntityRepository;
import com.games.spyfall.entities.Question;
import com.games.spyfall.entities.ResponseMessage;
import com.games.spyfall.entities.WsResponseType;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class GameServiceImpl implements GameService {
    private final Map<String, WebSocketSession> playerMap;

    private final JwtProvider jwtProvider;
    private final GameCardEntityRepository gameCardEntityRepository;
    private final Random random;
    private final Gson json;

    String hostUserName;
    String spyUserName;
    GameCard currentLocation;
    GameCard spyCard;
    Question currentQuestion;
    private final String FRONT_WEBSOCKET_ENDPOINT = "/queue/game/changes";
    boolean gameReadyStatus;

    @Autowired
    public GameServiceImpl(JwtProvider jwtProvider, GameCardEntityRepository gameCardEntityRepository, Gson json) {
        playerMap = new ConcurrentHashMap<>();
        random = new Random();
        this.jwtProvider = jwtProvider;
        this.gameCardEntityRepository = gameCardEntityRepository;
        this.gameReadyStatus = false;
        this.json = json;
    }

    @Override
    public void addPlayer(String token, WebSocketSession session) {
        if (playerMap.isEmpty()) {
            setHostName(token);
        }
        String login = jwtProvider.getLoginFromToken(token);
        if (!playerMap.containsKey(login)) {
            playerMap.put(login, session);
            log.info("put new user:" + login);
        }
        sendMessageToAll(convert(new ResponseMessage(WsResponseType.INFO, "New player connected. Hi, " + login)));

    }

    @Override
    public void setHostName(String token) {
        hostUserName = jwtProvider.getLoginFromToken(token);
        log.info("host name set: " + jwtProvider.getLoginFromToken(token));
    }

    @Override
    public void startGame(String token) throws IOException {
        if (!jwtProvider.getLoginFromToken(token).equals(hostUserName)) {
            return;
        }
        currentLocation = getCurrentLocation();
        spyCard = gameCardEntityRepository.getByName("spy card");
        spyUserName = getSpyUserName();

        log.info("current location: " + currentLocation.getName());
        log.info("current spy: " + spyUserName);

        getSessionByName(spyUserName).sendMessage(convert(spyCard));
        List<WebSocketSession> otherUsersSessions = Stream.of(playerMap.keySet())
                .map(Object::toString)
                .filter(name -> !spyUserName.equals(name))
                .map(this::getSessionByName)
                .collect(Collectors.toList());
        sendMessageToUsers(otherUsersSessions, currentLocation);
        log.info("messages were sent to users");
        gameReadyStatus = true;
    }

    @Override
    public WebSocketSession getSessionByName(String name) {
        return playerMap.get(name);
    }

    @Override
    public void sendMessageToUsers(List<WebSocketSession> sessions, Object payload) {
        sessions.forEach(session -> {
            try {
                session.sendMessage(convert(payload));
            } catch (IOException ex) {
                log.error("error occurred during sending message to users: ", ex);
            }
        });

    }

    @Override
    public void sendMessageToAll(Object payload) {
        playerMap.values().forEach(session -> {
            try {
                session.sendMessage(convert(payload));
            } catch (IOException ex) {
                log.error("error occurred during sending", ex);
            }
        });
    }

    @Override
    public Boolean getGameReadyStatus() {
        return this.gameReadyStatus;
    }

    @Override
    public void askQuestion(Question question) {
        this.currentQuestion = question;
        sendMessageToUsers((List<WebSocketSession>) playerMap.values(), question);
        log.info("question" + question.getQuestion() + "sent to all users");
    }

    private GameCard getCurrentLocation() {
        List<Integer> gameCardIds = gameCardEntityRepository.getAllIds();
        return gameCardEntityRepository.getById(gameCardIds.get(random.nextInt(gameCardIds.size())));
    }

    private String getSpyUserName() {
        Set<String> playerUserNames = playerMap.keySet();
        int i = 0;
        int userNumber = random.nextInt(playerUserNames.size());
        for (String username : playerUserNames) {
            if (i == userNumber) {
                return username;
            }
            i++;
        }
        return playerUserNames.stream().findFirst().get();
    }

    private TextMessage convert(Object message) {
        return new TextMessage(json.toJson(message));
    }
}
