package com.games.spyfall.config.websocket;

import com.games.spyfall.services.websocket.TextWebsocket;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Value("${frontend.ulr}")
    String frontUrl;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(textWebsocket(), "/game")
                .setAllowedOrigins(frontUrl);
    }

    @Bean
    public TextWebsocket textWebsocket() {
        return new TextWebsocket();
    }

    @Bean
    public Gson json() {
        return new Gson();
    }
}