package com.games.spyfall.controller;

import com.games.spyfall.database.gamecards.GameCardEntityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameCardController {
    private final GameCardEntityRepository gameCardEntityRepository;

    @GetMapping(value = "/api/{cardId}",  produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getGameCard(@PathVariable int cardId) {
        return gameCardEntityRepository.getImageById(cardId);
    }

}