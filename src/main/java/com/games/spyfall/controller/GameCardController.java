package com.games.spyfall.controller;

import com.games.spyfall.database.gamecards.GameCardEntityRepository;
import com.games.spyfall.database.maps.GameMap;
import com.games.spyfall.database.maps.GameMapRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

import static io.jsonwebtoken.lang.Classes.getResourceAsStream;

@RestController
@RequiredArgsConstructor
public class GameCardController {
    private final GameCardEntityRepository gameCardEntityRepository;
    private final GameMapRepository gameMapRepository;

    @GetMapping(value = "/api/images/{cardId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getGameCard(@PathVariable int cardId) {
        return gameCardEntityRepository.getImageById(cardId);
    }

    @GetMapping(value = "/api/images/map", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getMap() throws IOException {
        return gameMapRepository.getImageById(1);
    }
}