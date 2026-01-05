package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "football_standings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "league_code", nullable = false)
    private String leagueCode;  // cup-c1, ngoai-hang-anh, la-liga, etc.

    @Column(name = "league_name")
    private String leagueName;

    @Column(name = "position")
    private Integer position;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "team_logo")
    private String teamLogo;

    @Column(name = "matches_played")
    private Integer matchesPlayed;

    @Column(name = "wins")
    private Integer wins;

    @Column(name = "draws")
    private Integer draws;

    @Column(name = "losses")
    private Integer losses;

    @Column(name = "goals_for")
    private Integer goalsFor;

    @Column(name = "goals_against")
    private Integer goalsAgainst;

    @Column(name = "goal_difference")
    private Integer goalDifference;

    @Column(name = "points")
    private Integer points;

    @Column(name = "recent_form")
    private String recentForm;

    @Column(name = "season")
    private String season;

    @Column(name = "updated_at")
    private Instant updatedAt;

}
