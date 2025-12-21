package com.news24h.news24hbackend.infra;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class SimpleSlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowSeconds;
    private final Deque<Long> timestamps = new ArrayDeque<>();

    public SimpleSlidingWindowRateLimiter(int maxRequests, long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public synchronized boolean allow() {
        long now = Instant.now().getEpochSecond();
        long min = now - windowSeconds;

        while (!timestamps.isEmpty() && timestamps.peekFirst() < min) {
            timestamps.pollFirst();
        }
        if (timestamps.size() >= maxRequests) return false;

        timestamps.addLast(now);
        return true;
    }
}
