package com.games.spyfall.services.user;

import com.games.spyfall.entities.User;

public interface UserService {
    User saveUser(User user);

    User findByLogin(String login);

    User findByLoginAndPassword(String login, String password);
}
