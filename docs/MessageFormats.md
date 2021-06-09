# Messages Example

## Messages from client to server examples

Register new player to the current game:

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