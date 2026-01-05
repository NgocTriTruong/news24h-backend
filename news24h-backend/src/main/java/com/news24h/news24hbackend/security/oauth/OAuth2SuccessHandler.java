package com.news24h.news24hbackend.security.oauth;

import com.news24h.news24hbackend.entity.User;
import com.news24h.news24hbackend.repository.UserRepository;
import com.news24h.news24hbackend.security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend.redirect}")
    private String frontendRedirect;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) authentication;

        String provider =
                token.getAuthorizedClientRegistrationId(); // google / facebook

        OAuth2User oAuth2User = token.getPrincipal();

        String email = safeString(oAuth2User.getAttribute("email"));
        String name = safeString(oAuth2User.getAttribute("name"));

        String providerId = safeString(oAuth2User.getAttribute("sub"));
        if (providerId.isBlank()) {
            providerId = safeString(oAuth2User.getAttribute("id"));
        }

        String avatarUrl = "";
        Object pic = oAuth2User.getAttribute("picture");
        if (pic instanceof String s) {
            avatarUrl = s;
        }

        String finalProviderId = providerId;
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> User.builder()
                        .createdAt(Instant.now())
                        .provider(provider)
                        .providerId(finalProviderId)
                        .build());

        user.setEmail(email);
        user.setName(name);
        user.setAvatarUrl(avatarUrl);

        userRepository.save(user);

        // ===== JWT =====
        Map<String, Object> claims = Map.of(
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "name", user.getName(),
                "provider", provider
        );

        String jwt = jwtService.generateToken(
                user.getId().toString(),
                claims
        );

        String redirect = frontendRedirect
                + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8)
                + "&provider=" + URLEncoder.encode(provider, StandardCharsets.UTF_8);

        response.sendRedirect(redirect);
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
