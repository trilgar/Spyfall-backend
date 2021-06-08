package com.games.spyfall.config;

import com.games.spyfall.entities.User;
import com.games.spyfall.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userService.findByLogin(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("login or password is incorrect");
        }
        System.out.println("USER"+ userEntity);
        return CustomUserDetails.fromUserEntityToCustomUserDetails(userEntity);
    }
}