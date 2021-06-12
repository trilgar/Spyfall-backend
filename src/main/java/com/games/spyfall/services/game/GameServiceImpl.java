package com.games.spyfall.services.game;

import com.games.spyfall.config.jwt.JwtProvider;
import com.games.spyfall.database.gamecards.GameCard;
import com.games.spyfall.database.gamecards.GameCardEntityRepository;
import com.games.spyfall.entities.*;
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

@Slf4j
@Service
public class GameServiceImpl implements GameService {
    private Map<String, WebSocketSession> playerMap;
    private Map<String, Set<String>> suspectMap;

    private final JwtProvider jwtProvider;
    private final GameCardEntityRepository gameCardEntityRepository;
    private final Random random;
    private final Gson json;

    String hostUserName;
    String spyUserName;
    GameCard currentLocation;
    GameCard spyCard;
    Question currentQuestion;
    String questionGranted;
    String answeringPerson;
    boolean gameReadyStatus;
    boolean gameEnded;
    boolean spyGuessing;

    private static final String STRING_DATA_TYPE = "string";
    private static final String GAMECARD_DATA_TYPE = "gameCard";
    private static final String QUESTION_DTO_DATA_TYPE = "questionDto";
    private static final String ANSWER_DATA_TYPE = "answer";
    private static final String SUSPECT_MAP_DATA_TYPE = "suspectMap";
    private static final String GAME_CONCLUSION_DATA_TYPE = "conclusion";
    private static final String SPY_BUSTED_DATA_TYPE = "spyBusted";
    private static final String PLAYER_LIST_DATA_TYPE = "playerList";
    private static final String HOST_DATA_TYPE = "host";
    private static final String QUESTION_GRANTED_PERSON_DATA_TYPE = "questionGranted";

    @Autowired
    public GameServiceImpl(JwtProvider jwtProvider, GameCardEntityRepository gameCardEntityRepository, Gson json) {
        playerMap = new HashMap<>();
        suspectMap = new ConcurrentHashMap<>();
        random = new Random();
        this.jwtProvider = jwtProvider;
        this.gameCardEntityRepository = gameCardEntityRepository;
        this.gameReadyStatus = false;
        this.json = json;
        this.spyGuessing = false;
        this.gameEnded = false;
    }

    @Override
    public void addPlayer(String token, WebSocketSession session) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (playerMap.isEmpty()) {
            setHostName(token);
        }
        if (playerMap.containsKey(username)) {
            putPlayer(username, session);
            return;
        }
        if (gameReadyStatus && !playerMap.containsKey(username)) {
            log.info("GAME ALREADY STARTED. DECLINING new register " + username);
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Game already started.")));
            return;
        }
        putPlayer(username, session);
        log.info("put new user:" + username);
        log.info("new playerMap" + playerMap.keySet().toString());
        sendMessageToAll(new ResponseMessage(WsResponseType.INFO, STRING_DATA_TYPE, "New player connected. Hi, " + username));
        sendToAllRenewedPlayerMap();
        log.info("sending renewed players list");

    }

    private synchronized void putPlayer(String username, WebSocketSession session) {
        playerMap.put(username, session);
    }

    private void sendToAllRenewedPlayerMap() throws IOException {
        for (WebSocketSession webSocketSession : playerMap.values()) {
            sendConnected(webSocketSession);
        }
    }

    @Override
    public void setHostName(String token) {
        hostUserName = jwtProvider.getLoginFromToken(token);
        log.info("host name set: " + jwtProvider.getLoginFromToken(token));
    }

    @Override
    public void startGame(String token) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (playerMap.size() == 1) {
            //todo change limit to 3 players
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Need at least 2 players to start")));
            return;
        }
        if (!username.equals(hostUserName)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "You are not the host.")));
            return;
        }
        if (gameReadyStatus) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Game already started.")));
            return;
        }
        currentLocation = getCurrentLocation();
        spyCard = gameCardEntityRepository.findByName("шпион");
        spyUserName = getRandomUserName();
        questionGranted = getRandomUserName();

        log.info("current location: " + currentLocation.getName());
        log.info("current spy: " + spyUserName);

        GameCardDto gameCardDto = new GameCardDto(questionGranted, new Card(spyCard));

        getSessionByName(spyUserName).sendMessage(convert(new ResponseMessage(WsResponseType.ENTITY, GAMECARD_DATA_TYPE, gameCardDto)));
        List<WebSocketSession> otherUsersSessions = playerMap.keySet().stream()
                .filter(name -> !spyUserName.equals(name))
                .map(this::getSessionByName)
                .collect(Collectors.toList());
        gameCardDto = new GameCardDto(questionGranted, new Card(currentLocation));
        sendMessageToUsers(otherUsersSessions, new ResponseMessage(WsResponseType.ENTITY, GAMECARD_DATA_TYPE, gameCardDto));
        sendMessageToAll(new ResponseMessage(WsResponseType.ENTITY, QUESTION_GRANTED_PERSON_DATA_TYPE, questionGranted));
        log.info("messages were sent to users");
        gameReadyStatus = true;
    }

    @Override
    public void restartGame(String token) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (!username.equals(hostUserName)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Only host can restart game.")));
            return;
        }
        if (gameEnded) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "There is no need to restart game.")));
            return;
        }
        log.info("Starting restart: ");
        suspectMap = new ConcurrentHashMap<>();
        List<String> playersToRemove = new ArrayList<>();
        log.info("players to remove: " + playersToRemove.toString());
        playerMap.forEach((key, value) -> {
            if (!key.equals(hostUserName)) {
                playersToRemove.add(key);
            }
        });
        playersToRemove.forEach(name -> playerMap.remove(name));
        this.gameReadyStatus = false;
        this.spyGuessing = false;
        this.gameEnded = false;
        this.answeringPerson = "";
        this.questionGranted = "";
        this.spyGuessing = false;

        playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.INFO, STRING_DATA_TYPE,
                "Game successfully restarted. Start new game or wait for new players to come.")));
        log.info("Restart successful");
    }

    @Override
    public void askQuestion(String token, Question question) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (!gameReadyStatus) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait until game starts.")));
            return;
        }
        if (!username.equals(questionGranted)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "It`s not your turn to ask a question.")));
            return;
        }
        if (spyGuessing) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait for the spy to take his decision.")));
            return;
        }


        QuestionDto questionDto = new QuestionDto();
        questionDto.setMessage("You are asked the question. You can answer whatever you want.");
        questionDto.setQuestion(question);
        ResponseMessage responseMessage = new ResponseMessage(WsResponseType.ENTITY, QUESTION_DTO_DATA_TYPE, questionDto);

        getSessionByName(question.getTarget()).sendMessage(convert(responseMessage));

        questionDto.setMessage("");
        responseMessage = new ResponseMessage(WsResponseType.ENTITY, QUESTION_DTO_DATA_TYPE, questionDto);
        List<WebSocketSession> otherUsersSessions = playerMap.keySet().stream()
                .filter(name -> !question.getTarget().equals(name))
                .map(this::getSessionByName)
                .collect(Collectors.toList());
        sendMessageToUsers(otherUsersSessions, responseMessage);

        answeringPerson = question.getTarget();
    }

    @Override
    public void answerQuestion(String token, Answer answer) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (!gameReadyStatus) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait until game starts.")));
            return;
        }
        if (!username.equals(answeringPerson)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Question is not addressed to you.")));
            return;
        }
        if (spyGuessing) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait for the spy to take his decision.")));
            return;
        }

        sendMessageToAll(new ResponseMessage(WsResponseType.ENTITY, ANSWER_DATA_TYPE, answer));

        questionGranted = answeringPerson;
        sendMessageToAll(new ResponseMessage(WsResponseType.ENTITY, QUESTION_GRANTED_PERSON_DATA_TYPE, questionGranted));
        answeringPerson = "";
    }

    @Override
    public void suspectPlayer(String token, Suspect suspect) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (!gameReadyStatus) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait until game starts.")));
            return;
        }
        if (!username.equals(suspect.getSuspecting())) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "You cannot vote from other name")));
            return;
        }
        if (spyGuessing) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait for the spy to take his decision.")));
            return;
        }
        switch (suspect.getAction()) {
            case SET: {
                Set<String> suspects = suspectMap.computeIfAbsent(suspect.getSuspected(), (key) -> new TreeSet<>());
                suspects.add(suspect.getSuspecting());

                if (playerMap.size() - 1 == suspects.size()) {
                    endGame(suspect.getSuspected());
                    return;
                }
                suspectMap.put(suspect.getSuspected(), suspects);
                break;
            }
            case REMOVE: {
                Set<String> suspects = suspectMap.computeIfAbsent(suspect.getSuspected(), (key) -> new TreeSet<>());
                suspects.remove(suspect.getSuspecting());
                suspectMap.put(suspect.getSuspected(), suspects);
                break;
            }
            default: {
                playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Not defined Action (use SET or REMOVE)")));
                return;
            }
        }
        sendToAllRenewedPlayerMap();

    }

    @Override
    public void endGame(String suspectedSpyName) throws IOException {
        GameConclusion gameConclusion = new GameConclusion();
        if (!spyUserName.equals(suspectedSpyName)) {
            gameConclusion.setWinner(Winner.SPY);
            gameConclusion.setConclusion("Spy won the game. All players voted for innocent.");
            gameConclusion.setSpyName(spyUserName);
            gameConclusion.setLocationName(currentLocation.getName());
            sendMessageToAll(new ResponseMessage(WsResponseType.ENTITY, GAME_CONCLUSION_DATA_TYPE, gameConclusion));
            gameReadyStatus = false;
            gameEnded = true;
        } else {
            List<WebSocketSession> otherUsersSessions = playerMap.keySet().stream()
                    .filter(name -> !spyUserName.equals(name))
                    .map(this::getSessionByName)
                    .collect(Collectors.toList());
            sendMessageToUsers(otherUsersSessions, new ResponseMessage(WsResponseType.INFO, SPY_BUSTED_DATA_TYPE, "Citizens found a spy! He has the last chance to win!"));
            getSessionByName(spyUserName).sendMessage(convert(new ResponseMessage(WsResponseType.INFO, SPY_BUSTED_DATA_TYPE, "You are busted! You have the last chance to guess the location!")));
            spyGuessing = true;
        }
    }

    @Override
    public void spyGuess(String token, String locationName) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        if (!gameReadyStatus) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "Wait until game starts.")));
            return;
        }
        if (!username.equals(spyUserName)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "You are not a spy!")));
            return;
        }
        GameConclusion gameConclusion = new GameConclusion();
        if (locationName.equals(currentLocation.getName())) {
            gameConclusion.setWinner(Winner.SPY);
            gameConclusion.setConclusion("Spy won! He guessed right location!");
        } else {
            gameConclusion.setWinner(Winner.CITIZENS);
            gameConclusion.setConclusion("Citizens won! Spy Named wrong location!");
        }
        gameConclusion.setLocationName(currentLocation.getName());
        gameConclusion.setSpyName(spyUserName);
        sendMessageToAll(new ResponseMessage(WsResponseType.ENTITY, GAME_CONCLUSION_DATA_TYPE, gameConclusion));
        gameReadyStatus = false;
        gameEnded = true;
    }

    @Override
    public void getHost(WebSocketSession session) throws IOException {
        session.sendMessage(convert(new ResponseMessage(WsResponseType.ENTITY, HOST_DATA_TYPE, hostUserName)));
        session.sendMessage(convert(new ResponseMessage(WsResponseType.ENTITY, QUESTION_GRANTED_PERSON_DATA_TYPE, questionGranted)));
        log.info("host " + hostUserName + " was sent");
    }

    @Override
    public void getGameCard(String token) throws IOException {
        String username = jwtProvider.getLoginFromToken(token);
        log.info("SENDING LOCATION");
        if (!gameReadyStatus) {
            return;
        }
        if (!playerMap.containsKey(username)) {
            playerMap.get(username).sendMessage(convert(new ResponseMessage(WsResponseType.ERROR, STRING_DATA_TYPE, "You are not participating in game.")));
            return;
        }
        GameCardDto gameCardDto;
        if (username.equals(spyUserName)) {
            gameCardDto = new GameCardDto(questionGranted, new Card(spyCard));
        } else {
            gameCardDto = new GameCardDto(questionGranted, new Card(currentLocation));
        }
        getSessionByName(username).sendMessage(convert(new ResponseMessage(WsResponseType.ENTITY, GAMECARD_DATA_TYPE, gameCardDto)));
        log.info("location sent");
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

    @Override
    public void sendConnected(WebSocketSession session) throws IOException {
        List<Player> players = playerMap.keySet().stream().map(username -> {
            Player pLayer = new Player();
            pLayer.setUsername(username);
            pLayer.setSuspecting(suspectMap.computeIfAbsent(username, (key) -> new TreeSet<>()));
            return pLayer;
        }).collect(Collectors.toList());

        session.sendMessage(convert(new ResponseMessage(WsResponseType.ENTITY, PLAYER_LIST_DATA_TYPE, players)));
    }

    private GameCard getCurrentLocation() {
        List<Integer> gameCardIds = gameCardEntityRepository.getAllIds();
        Optional<GameCard> gameCard = gameCardEntityRepository.findById(gameCardIds.get(random.nextInt(gameCardIds.size())));
        return gameCard.orElse(null);
    }

    private String getRandomUserName() {
        Set<String> playerUserNames = playerMap.keySet();
        int i = 0;
        int userNumber = random.nextInt(playerUserNames.size());
        for (String username : playerUserNames) {
            if (i == userNumber) {
                return username;
            }
            i++;
        }
        return playerUserNames.stream().findFirst().orElse(null);
    }

    private TextMessage convert(Object message) {
        return new TextMessage(json.toJson(message));
    }
}
