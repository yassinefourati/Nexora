package com.fourati.platform.idempotency;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed store shared by all replicas. A claim is {@code SET NX} with the
 * in-flight TTL; completion overwrites the value with the serialized response
 * and the long TTL. If Redis is unreachable the filter falls back to processing
 * the request without idempotency protection rather than failing it.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "idempotency:";
    private static final String IN_FLIGHT = "__in_flight__";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Entry> claim(String key, Duration inFlightTtl) {
        String k = KEY_PREFIX + key;
        try {
            Boolean claimed = redis.opsForValue().setIfAbsent(k, IN_FLIGHT, inFlightTtl);
            if (Boolean.TRUE.equals(claimed)) {
                return Optional.empty();
            }
            String value = redis.opsForValue().get(k);
            if (value == null) {
                // expired between the two calls: try once more
                claimed = redis.opsForValue().setIfAbsent(k, IN_FLIGHT, inFlightTtl);
                return Boolean.TRUE.equals(claimed) ? Optional.empty() : Optional.of(new InFlight());
            }
            if (IN_FLIGHT.equals(value)) {
                return Optional.of(new InFlight());
            }
            return Optional.of(new Completed(objectMapper.readValue(value, CachedResponse.class)));
        } catch (Exception ex) {
            log.warn("Idempotency store unavailable, processing without protection: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void complete(String key, CachedResponse response, Duration ttl) {
        try {
            redis.opsForValue().set(KEY_PREFIX + key, objectMapper.writeValueAsString(response), ttl);
        } catch (Exception ex) {
            log.warn("Idempotency store write failed for key: {}", ex.getMessage());
        }
    }

    @Override
    public void release(String key) {
        try {
            String k = KEY_PREFIX + key;
            if (IN_FLIGHT.equals(redis.opsForValue().get(k))) {
                redis.delete(k);
            }
        } catch (RuntimeException ex) {
            log.debug("Idempotency release failed: {}", ex.getMessage());
        }
    }
}
