package com.games.spyfall.database.gamecards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameCardEntityRepository extends JpaRepository<GameCard, Integer> {
    @Override
    Optional<GameCard> findById(Integer integer);

    @Query(value = "select id from game_card where name<>'шпион'", nativeQuery = true)
    List<Integer> getAllIds();

    GameCard findByName(String name);

    @Query(value = "select image from game_card where id = ?1", nativeQuery = true)
    byte[] getImageById(int id);

}
