package com.news24h.news24hbackend.config;

import com.news24h.news24hbackend.infra.AiRateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class AiInterceptorConfig implements WebMvcConfigurer {

    @Value("${ai.ratelimit.rpm:10}")
    private int rpm;

    @Value("${ai.ratelimit.window-seconds:60}")
    private long windowSeconds;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AiRateLimitInterceptor(rpm, windowSeconds))
                .addPathPatterns("/api/ai/**");
    }
}
