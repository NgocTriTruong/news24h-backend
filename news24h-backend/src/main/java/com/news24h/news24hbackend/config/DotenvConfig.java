package com.news24h.news24hbackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        set("spring.datasource.username", dotenv.get("DB_USERNAME"));
        set("spring.datasource.password", dotenv.get("DB_PASSWORD"));
        set("app.jwt.secret", dotenv.get("JWT_SECRET"));
        set("gemini.api-key", dotenv.get("GEMINI_API_KEY"));
    }

    private void set(String key, String value) {
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
            System.out.println("Loaded ENV: " + key);
        } else {
            System.out.println("ENV NOT FOUND: " + key);
        }
    }
}

