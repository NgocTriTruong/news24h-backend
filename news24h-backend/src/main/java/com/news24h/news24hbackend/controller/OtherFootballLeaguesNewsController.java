package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.service.OtherFootballLeaguesNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/other-leagues")
public class OtherFootballLeaguesNewsController {
    private final OtherFootballLeaguesNewsService otherLeaguesService;

    public OtherFootballLeaguesNewsController(OtherFootballLeaguesNewsService otherLeaguesService) {
        this.otherLeaguesService = otherLeaguesService;
    }

    @PostMapping("/crawl")
    public ResponseEntity<?> crawlOtherLeaguesNews() {
        return ResponseEntity.ok(otherLeaguesService.crawlOtherLeaguesNews());
    }
}
