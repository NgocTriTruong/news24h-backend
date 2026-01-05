package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.FootballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FootballMatchRepository extends JpaRepository<FootballMatch, Long> {

    List<FootballMatch> findByLeagueCodeAndSeasonOrderByMatchDateDesc(String leagueCode, String season);

    List<FootballMatch> findByLeagueCodeAndStatusOrderByMatchDateAsc(String leagueCode, String status);

    List<FootballMatch> findByLeagueCodeAndMatchDateAfterOrderByMatchDateAsc(String leagueCode, Instant date);

    Optional<FootballMatch> findByLeagueCodeAndHomeTeamAndAwayTeamAndMatchTime(String leagueCode, String homeTeam, String awayTeam, String matchTime);

    void deleteAllByLeagueCodeAndSeason(String leagueCode, String season);
}
