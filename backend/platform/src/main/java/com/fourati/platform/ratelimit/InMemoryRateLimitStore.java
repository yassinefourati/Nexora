package com.fourati.platform.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-JVM implementation. Correct for one replica only: with several
 * replicas each enforces its own budget, so the effective limit is
 * {@code rpm × replicas}. Use {@link RedisRateLimitStore} (fourati.common.store=redis)
 * when scaling out.
 */
@Slf4j
public class InMemoryRateLimitStore implements RateLimitStore {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryConsume(String key, int rpm) {
        return buckets.computeIfAbsent(key, k -> new TokenBucket(rpm)).tryConsume();
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    void evictStaleBuckets() {
        long cutoff = System.nanoTime() - TimeUnit.MINUTES.toNanos(10);
        int before = buckets.size();
        buckets.values().removeIf(b -> b.lastUsedNano() < cutoff);
        int removed = before - buckets.size();
        if (removed > 0) {
            log.debug("Rate limit store evicted {} stale bucket(s), remaining={}", removed, buckets.size());
        }
    }

    static final class TokenBucket {
        private final long nanosBetweenTokens;
        private final long maxBurstNanos;
        private final AtomicLong nextTokenNano;
        private volatile long lastUsed;

        TokenBucket(int rpm) {
            this.nanosBetweenTokens = 60_000_000_000L / Math.max(rpm, 1);
            // Cap accumulated credit at one minute's worth.
            this.maxBurstNanos = 60_000_000_000L;
            this.lastUsed = System.nanoTime();
            this.nextTokenNano = new AtomicLong(this.lastUsed);
        }

        boolean tryConsume() {
            long now = System.nanoTime();
            lastUsed = now;
            while (true) {
                long cur = nextTokenNano.get();
                if (cur > now) return false;
                long base = Math.max(cur, now - maxBurstNanos);
                if (nextTokenNano.compareAndSet(cur, base + nanosBetweenTokens)) return true;
            }
        }

        long lastUsedNano() { return lastUsed; }
    }
}
