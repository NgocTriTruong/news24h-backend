package com.news24h.news24hbackend.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

public class AiRateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, SimpleSlidingWindowRateLimiter> limiters = new ConcurrentHashMap<>();
    private final int rpm;
    private final long windowSeconds;

    public AiRateLimitInterceptor(int rpm, long windowSeconds) {
        this.rpm = rpm;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Key theo IP (đơn giản). Nếu bạn có userId/JWT => đổi key theo userId sẽ chuẩn hơn.
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();

        SimpleSlidingWindowRateLimiter limiter =
                limiters.computeIfAbsent(ip, k -> new SimpleSlidingWindowRateLimiter(rpm, windowSeconds));

        if (!limiter.allow()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write("{\"message\":\"Bạn thao tác quá nhanh. Vui lòng thử lại sau.\"}");
            return false;
        }
        return true;
    }
}
