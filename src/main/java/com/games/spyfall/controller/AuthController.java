package com.games.spyfall.controller;

import com.games.spyfall.config.jwt.JwtProvider;
import com.games.spyfall.entities.AuthRequest;
import com.games.spyfall.entities.AuthResponse;
import com.games.spyfall.entities.RegistrationRequest;
import com.games.spyfall.entities.User;
import com.games.spyfall.services.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
@Slf4j
@RestController
@CrossOrigin(origins = "${frontend.ulr}")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/register")
    public String registerUser(@RequestBody @Valid RegistrationRequest registrationRequest) {
        User u = new User();
        System.out.println("User in registerUser()" + u.toString());
        u.setPassword(registrationRequest.getPassword());
        u.setLogin(registrationRequest.getLogin());
        userService.saveUser(u);
        return "OK";
    }

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody AuthRequest request) {
        User userEntity = userService.findByLoginAndPassword(request.getLogin(), request.getPassword());
        if (userEntity == null) {
            log.info(request + " has wrong credentials");
            throw new AuthenticationCredentialsNotFoundException("nope");
        }
        String token = jwtProvider.generateToken(userEntity.getLogin());
        return new AuthResponse(token);
    }
}
