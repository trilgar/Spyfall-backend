# General information
- This project is a website-realization of card game "Spyfall"  
  (see https://en.wikipedia.org/wiki/Spyfall_(card_game))
- Main gameplay and all interactions with server performed via websocket connection.  
Server handles connections using pure websocket library.  
- All messages from server to client and from client to server are transported in JSON format  
(see https://en.wikipedia.org/wiki/JSON).  
- Connections are secured via JWT token authorization (see https://jwt.io/introduction).
# Message Format
All messages from client to server must have the following pattern: 
```json
{
  "event": "SOME EVENT",
  "token": "your token",
  "data": "payload"
}
```
## Event
By the "event" server determines what action needs to be performed and what response client waits for.  
There are several events which server can process:
```java
public enum WsMessageType {
    REGISTER,
    STARTGAME,
    RESTART,
    QUESTION,
    ANSWER,
    SUSPECT,
    GUESSLOCATION,
    PING,
    CONNECTED,
    GETHOST,
    GETLOCATION
}
```
Examples of messages with different events, and their description you can see below.
## Token
All messages via websockets are secured by JWT token authorisation (see https://jwt.io/introduction).  
You should pass valid token to every websocket message, otherwise you will get ERROR as a response.
## Data
Data in websocket message contains main payload of message which server needs to process.  

Examples of different messages you can see below.
# Messages Examples

## Messages from client to server examples

Register new player to the current game. First player who enters the game becomes a host.

```json
{
  "event": "REGISTER",
  "token": "your token"
}
```

Start game (only available if you are the host)

```json
{
  "event": "STARTGAME",
  "token": "your token"
}
```
Ask question (only available to questionGranted person)

```json
{
  "event": "QUESTION",
  "token": "your token",
  "data": {
    "source": "your nickname",
    "target": "nickname of questioned person",
    "question": "text of the question"
  }
}
```

Answer question (only available for questioned player)

```json
{
  "event": "ANSWER",
  "token": "your token",
  "data": {
    "question": "text of the question addressed to you",
    "answer": "your answer for a question"
  }
}
```

Suspect player or remove suspicion (if all players besides himself will vote for one player, he will be considered as a spy)
```json
{
  "event": "SUSPECT",
  "token": "your token",
  "data": {
    "suspectAction": "SET or REMOVE",
    "suspecting": "your nickname",
    "suspected": "suspect nickname"
  }
}
```
Guess the current location (only available for spy)
```json
{
  "event": "GUESSLOCATION",
  "token": "your token",
  "data": "location name"
}
```
Start new game (only available for host). It deletes all data of previous game.
```json
{
  "event": "RESTART",
  "token": "your token"
}
```
Request all connected players with their information.
```json
{
  "event": "CONNECTED",
  "token": "your token"
}
```
Request host name.
```json
{
  "event": "GETHOST",
  "token": "your token"
}
```

Get information about current location (if you are a spy you get info about spy, not location).
```json
{
  "event": "GETLOCATION",
  "token": "your token"
}
```
Ping server (needs to hold websocket connection in connected state).  
Recommended pinging server at least 1 time per 30 seconds.
```json
{
  "event": "PING",
  "token": "your token"
}
```

# Response Format
After processing data and performing game handling action server gives a response to client.  
Here is a model of response: 
```json
{
  "event": "SOME EVENT",
  "dataType": "data type info",
  "data": "payload"
}
```
## Event
Event of response specifies type of message. Depending on the "event" field client can differ types of messages.  
Event has the following types: 
```java
public enum WsResponseType {
    INFO,
    ERROR,
    ENTITY,
    PING
} 
```
- Messages with "event" field set to "INFO" need to be treated as support messages.  
  They can be ignored if you don`t need them. They always have "dataType" = "string" and payload is always a string literal containing some information.
- ERROR messages contain errors of server while interacting with it. They always have "dataType" = "string" and payload is always a string literal containing error information.
- ENTITY messages contain payload as real objects. They contain data which needs to be sent to client. Messages with this type have the greatest value for client.
- PING messages are used to keep connection with client. They are sent as a response for PING message from client.
## DataType
DataType helps client to determine entity of which type is transported via websocket.  
There are several dataTypes:
```java
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
```
## Data
"data" field contains payload which is needed by client to display and process gameplay.

# Response Messages Examples
Game card, which contains information about your rol in game and location name.
```java
public class GameCardDto {
    private final String questionGranted;
    private final Card gameCard;
}

public class Card {
  private int id;
  private String name;
  private String pictureUrl;
}
```
Question from one person to another.
```java
public class Question {
    private String source;
    private String target;
    private String question;
}
```
Answer for the question.
```java
public class Answer {
    private String question;
    private String answer;
}

```
Map containing players and list of players who suspect them (Returns list of objects with type Player) 
```java
public class Player {
    private String username;
    private Set<String> suspecting;
}
```
Conclusion contains game info and winner information. It is sent when game ends.
```java
public class GameConclusion {
    private Winner winner;
    private String conclusion;
    private String spyName;
    private String locationName;
}

public enum Winner {
  SPY,
  CITIZENS
}
```
