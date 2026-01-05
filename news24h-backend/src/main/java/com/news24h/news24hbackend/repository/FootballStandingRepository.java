package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.FootballStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FootballStandingRepository extends JpaRepository<FootballStanding, Long> {

    List<FootballStanding> findByLeagueCodeAndSeasonOrderByPositionAsc(String leagueCode, String season);

    Optional<FootballStanding> findByLeagueCodeAndTeamNameAndSeason(String leagueCode, String teamName, String season);

    void deleteAllByLeagueCodeAndSeason(String leagueCode, String season);
}
