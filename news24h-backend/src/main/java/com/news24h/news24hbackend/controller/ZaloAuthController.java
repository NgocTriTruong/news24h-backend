package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.ZaloMeResponse;
import com.news24h.news24hbackend.dto.ZaloTokenResponse;
import com.news24h.news24hbackend.entity.User;
import com.news24h.news24hbackend.repository.UserRepository;
import com.news24h.news24hbackend.security.JwtService;
import com.news24h.news24hbackend.service.ZaloAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth/zalo")
@RequiredArgsConstructor
public class ZaloAuthController {

    private final ZaloAuthService zaloAuthService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final Map<String, String> verifierStore = new ConcurrentHashMap<>();

    @Value("${app.frontend.redirect}")
    private String frontendRedirect;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws Exception {
        String state = UUID.randomUUID().toString();

        String verifier = generateCodeVerifier();
        String challenge = generateCodeChallenge(verifier);

        verifierStore.put(state, verifier);

        String url = zaloAuthService.buildLoginUrl(state, challenge);
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response
    ) throws Exception {

        if (code == null || code.isBlank() || state == null) {
            response.sendRedirect(frontendRedirect + "?error=zalo_missing_code");
            return;
        }

        String verifier = verifierStore.remove(state);
        if (verifier == null) {
            response.sendRedirect(frontendRedirect + "?error=invalid_state");
            return;
        }

        ZaloTokenResponse token = zaloAuthService.exchangeCode(code, verifier);
        if (token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            response.sendRedirect(frontendRedirect + "?error=" + enc("Không lấy được access_token từ Zalo"));
            return;
        }

        ZaloMeResponse me = zaloAuthService.getMe(token.getAccessToken());
        if (me.getId() == null || me.getId().isBlank()) {
            response.sendRedirect(frontendRedirect + "?error=" + enc("Không lấy được thông tin người dùng Zalo"));
            return;
        }

        String provider = "zalo";
        String providerId = me.getId();

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> User.builder()
                        .createdAt(Instant.now())
                        .provider(provider)
                        .providerId(providerId)
                        .build());

        user.setName(me.getName());
        // Zalo thường không luôn có email public
        userRepository.save(user);

        Map<String, Object> claims = Map.of(
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "name", user.getName(),
                "provider", provider
        );

        String jwt = jwtService.generateToken(
                user.getId(),
                claims
        );

        String redirect = frontendRedirect
                + "?token=" + enc(jwt)
                + "&provider=zalo";

        response.sendRedirect(redirect);
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

}
