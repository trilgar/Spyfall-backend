package com.games.spyfall.database.maps;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GameMapRepository extends JpaRepository<GameMap, Integer> {

    Optional<GameMap> findById(int id);

    Optional<GameMap> findByName(String name);

    @Query(value = "select image from maps where id = ?1", nativeQuery = true)
    byte[] getImageById(int id);

}
