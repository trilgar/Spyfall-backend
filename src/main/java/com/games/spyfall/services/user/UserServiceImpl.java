package com.games.spyfall.services.user;

import com.games.spyfall.database.security.RoleEntityRepository;
import com.games.spyfall.database.security.UserEntityRepository;
import com.games.spyfall.entities.RoleEntity;
import com.games.spyfall.entities.User;
import com.games.spyfall.exceptions.NameAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.NameAlreadyBoundException;

@Service
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userEntityRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserEntityRepository userEntityRepository, RoleEntityRepository roleEntityRepository, PasswordEncoder passwordEncoder) {
        this.userEntityRepository = userEntityRepository;
        this.roleEntityRepository = roleEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(User user) {
        RoleEntity userRole = roleEntityRepository.findByName("ROLE_USER");
        user.setRoleEntity(userRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try{
            return userEntityRepository.save(user);
        }catch (DataIntegrityViolationException ex){
            ex.printStackTrace();
            throw new NameAlreadyExistException();
        }
    }

    @Override
    public User findByLogin(String login) {
        return userEntityRepository.findByLogin(login);
    }

    @Override
    public User findByLoginAndPassword(String login, String password) {
        User user = findByLogin(login);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }
}
