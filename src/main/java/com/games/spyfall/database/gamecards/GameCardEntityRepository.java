package com.games.spyfall.database.gamecards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameCardEntityRepository extends JpaRepository<GameCard, Integer> {
    GameCard getById(int id);

    @Query(value = "select id from game_card", nativeQuery = true)
    List<Integer> getAllIds();

    GameCard getByName(String name);

    @Query(value = "select image from game_card where id = ?1", nativeQuery = true)
    byte[] getImageById(int id);

}
