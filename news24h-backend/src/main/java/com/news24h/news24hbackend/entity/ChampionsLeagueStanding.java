package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "champions_league_standings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionsLeagueStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position")
    private Integer position;  // Thứ tự

    @Column(name = "team_name", nullable = false)
    private String teamName;  // Tên đội

    @Column(name = "team_logo")
    private String teamLogo;  // Logo đội

    @Column(name = "matches_played")
    private Integer matchesPlayed;  // Số trận đã chơi

    @Column(name = "wins")
    private Integer wins;  // Thắng

    @Column(name = "draws")
    private Integer draws;  // Hòa

    @Column(name = "losses")
    private Integer losses;  // Thua

    @Column(name = "goals_for")
    private Integer goalsFor;  // Bàn thắng

    @Column(name = "goals_against")
    private Integer goalsAgainst;  // Bàn thua

    @Column(name = "goal_difference")
    private Integer goalDifference;  // Hiệu số

    @Column(name = "points")
    private Integer points;  // Điểm

    @Column(name = "recent_form")
    private String recentForm;  // Kết quả 5 trận gần nhất (W-W-W-D-L)

    @Column(name = "season")
    private String season;  // Mùa giải (2025/2026)

    @Column(name = "updated_at")
    private Instant updatedAt;

}
