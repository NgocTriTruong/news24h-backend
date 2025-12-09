package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.AuthResponse;
import com.news24h.news24hbackend.dto.LoginRequest;
import com.news24h.news24hbackend.dto.RegisterRequest;
import com.news24h.news24hbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

