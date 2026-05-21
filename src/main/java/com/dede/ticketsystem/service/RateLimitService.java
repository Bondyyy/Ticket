package com.dede.ticketsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimitService {

    @Value("${rate-limit.lock-seat.max-requests:20}")
    private int maxRequests;

    @Value("${rate-limit.lock-seat.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    /**
     * Checks if a request from the given key is allowed under the rate limit.
     *
     * @param key maKH or client IP
     * @return true if request is allowed, false if limit exceeded
     */
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        // Get or create queue for this user/IP
        Deque<Long> timestamps = requestLogs.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Synchronize on the deque to prevent race conditions during updates
        synchronized (timestamps) {
            // Clean up old timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Dọn dẹp định kỳ 5 phút một lần để xoá các key rỗng hoặc quá hạn, chống rò rỉ bộ nhớ (Memory Leak).
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000)
    public void cleanUpExpiredRateLimits() {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        java.util.Iterator<java.util.Map.Entry<String, Deque<Long>>> iterator = requestLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<String, Deque<Long>> entry = iterator.next();
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }
}
