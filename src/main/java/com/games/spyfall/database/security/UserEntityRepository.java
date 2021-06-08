package com.games.spyfall.database.security;

import com.games.spyfall.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<User, Integer> {
    User findByLogin(String login);

}
