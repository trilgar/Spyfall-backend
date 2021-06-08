package com.games.spyfall.controller;

import com.games.spyfall.config.jwt.JwtProvider;
import com.games.spyfall.services.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.ulr}")
public class WebsocketController {
    private final GameService gameService;
    private final JwtProvider jwtProvider;

}
