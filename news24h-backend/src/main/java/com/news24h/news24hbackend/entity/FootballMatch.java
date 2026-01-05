package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "football_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "league_code", nullable = false)
    private String leagueCode;

    @Column(name = "league_name")
    private String leagueName;

    @Column(name = "home_team", nullable = false)
    private String homeTeam;

    @Column(name = "away_team", nullable = false)
    private String awayTeam;

    @Column(name = "home_logo")
    private String homeLogo;

    @Column(name = "away_logo")
    private String awayLogo;

    @Column(name = "match_time")
    private String matchTime;

    @Column(name = "match_date")
    private Instant matchDate;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "status")
    private String status;  // scheduled, live, finished

    @Column(name = "round")
    private String round;

    @Column(name = "season")
    private String season;

    @Column(name = "match_url")
    private String matchUrl;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}
