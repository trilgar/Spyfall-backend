package com.games.spyfall.database.security;

import com.games.spyfall.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleEntityRepository extends JpaRepository<RoleEntity, Integer> {
    RoleEntity findByName(String name);
}
