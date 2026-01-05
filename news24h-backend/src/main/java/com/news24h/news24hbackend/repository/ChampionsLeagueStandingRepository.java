package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.ChampionsLeagueStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionsLeagueStandingRepository extends JpaRepository<ChampionsLeagueStanding, Long> {

    List<ChampionsLeagueStanding> findBySeasonOrderByPositionAsc(String season);

    Optional<ChampionsLeagueStanding> findByTeamNameAndSeason(String teamName, String season);

    void deleteAllBySeason(String season);
}
