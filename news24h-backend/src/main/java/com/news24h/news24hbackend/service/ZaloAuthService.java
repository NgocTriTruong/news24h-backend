package com.news24h.news24hbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news24h.news24hbackend.dto.ZaloMeResponse;
import com.news24h.news24hbackend.dto.ZaloTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ZaloAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${zalo.app-id}")
    private String appId;

    @Value("${zalo.secret-key}")
    private String secretKey;

    @Value("${zalo.redirect-uri}")
    private String redirectUri;

    @Value("${zalo.auth-url}")
    private String authUrl;

    @Value("${zalo.token-url}")
    private String tokenUrl;

    @Value("${zalo.me-url}")
    private String meUrl;

    public String buildLoginUrl(String state, String codeChallenge) {
        return authUrl
                + "?app_id=" + enc(appId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&state=" + enc(state)
                + "&code_challenge=" + enc(codeChallenge)
                + "&code_challenge_method=S256";
    }

    public ZaloTokenResponse exchangeCode(String code, String verifier) throws Exception {
        // Zalo token endpoint thường nhận params dạng form
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.set("secret_key", secretKey);

        String body =
                "code=" + enc(code) +
                "&app_id=" + enc(appId) +
                "&grant_type=authorization_code" +
                "&code_verifier=" + enc(verifier);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(tokenUrl, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Zalo token error");
        }
        return mapper.readValue(resp.getBody(), ZaloTokenResponse.class);
    }

    public ZaloMeResponse getMe(String accessToken) throws Exception {

        String url = meUrl
                + "?access_token=" + enc(accessToken)
                + "&fields=id,name,picture";

        ResponseEntity<String> resp =
                restTemplate.getForEntity(url, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Zalo me error");
        }

        JsonNode root = mapper.readTree(resp.getBody());

        if (root.has("error") && root.get("error").asInt() != 0) {
            throw new RuntimeException("Zalo API error: " + root.get("message").asText());
        }

        ZaloMeResponse me = new ZaloMeResponse();
        me.setId(root.path("id").asText(""));
        me.setName(root.path("name").asText(""));

        if (root.has("picture")) {
            me.setPicture(mapper.convertValue(root.get("picture"), Object.class));
        }

        return me;
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
