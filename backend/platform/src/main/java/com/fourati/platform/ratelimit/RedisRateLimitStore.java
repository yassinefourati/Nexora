package com.fourati.platform.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Token bucket kept in Redis and updated atomically by a Lua script, so every
 * replica draws from the same budget. Semantics match the in-memory store:
 * refill at {@code rpm/60} tokens per second, burst capacity of {@code rpm}.
 *
 * Fails open: if Redis is unreachable the request is allowed and a warning is
 * logged, because a limiter outage must not become an API outage.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimitStore implements RateLimitStore {

    private static final String KEY_PREFIX = "ratelimit:";

    private static final RedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local rate = tonumber(ARGV[1])       -- tokens per second
            local capacity = tonumber(ARGV[2])   -- burst
            local now = tonumber(ARGV[3])        -- seconds, fractional
            local data = redis.call('HMGET', key, 'tokens', 'ts')
            local tokens = tonumber(data[1])
            local ts = tonumber(data[2])
            if tokens == nil then tokens = capacity; ts = now end
            local delta = math.max(0, now - ts)
            tokens = math.min(capacity, tokens + delta * rate)
            local allowed = 0
            if tokens >= 1 then tokens = tokens - 1; allowed = 1 end
            redis.call('HSET', key, 'tokens', tokens, 'ts', now)
            redis.call('EXPIRE', key, math.ceil(capacity / rate) * 2)
            return allowed
            """, Long.class);

    private final StringRedisTemplate redis;

    @Override
    public boolean tryConsume(String key, int rpm) {
        double ratePerSecond = Math.max(rpm, 1) / 60.0;
        String now = Double.toString(System.currentTimeMillis() / 1000.0);
        try {
            Long allowed = redis.execute(SCRIPT, List.of(KEY_PREFIX + key),
                    Double.toString(ratePerSecond), Integer.toString(Math.max(rpm, 1)), now);
            return allowed == null || allowed == 1L;
        } catch (RuntimeException ex) {
            log.warn("Rate limit store unavailable, allowing request: {}", ex.getMessage());
            return true;
        }
    }
}
