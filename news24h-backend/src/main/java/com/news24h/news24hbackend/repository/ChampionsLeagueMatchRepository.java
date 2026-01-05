package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.ChampionsLeagueMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionsLeagueMatchRepository extends JpaRepository<ChampionsLeagueMatch, Long> {

    List<ChampionsLeagueMatch> findBySeasonOrderByMatchDateAsc(String season);

    List<ChampionsLeagueMatch> findBySeasonAndStatusOrderByMatchDateAsc(String season, String status);

    List<ChampionsLeagueMatch> findByMatchDateAfterOrderByMatchDateAsc(Instant date);

    Optional<ChampionsLeagueMatch> findByHomeTeamAndAwayTeamAndMatchTime(String homeTeam, String awayTeam, String matchTime);

    void deleteAllBySeason(String season);
}
