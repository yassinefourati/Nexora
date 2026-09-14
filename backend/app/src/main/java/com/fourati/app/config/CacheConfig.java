package com.fourati.app.config;

import com.fourati.app.cache.CacheInvalidator;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache manager selection follows {@code fourati.common.store} (ADR 0008), the
 * same switch that places rate-limit buckets and idempotency keys:
 *
 * <ul>
 *   <li>{@code memory}: Caffeine, local to the JVM. Correct only for a single replica.</li>
 *   <li>{@code redis}: Redis-backed. Every replica reads and clears the same entries,
 *       so an invalidation after a grant or revoke on one replica is seen by all.</li>
 * </ul>
 *
 * Caches in use: {@code effectivePermissions} (per role set, read by /me/permissions)
 * and {@code provisionedUsers} (per identity subject, read on every request). Both are
 * cleared through the matching {@link CacheInvalidator} by
 * {@code CacheInvalidationListener} on the events services publish; the five-minute
 * TTL is only a safety net.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    static final Duration TTL = Duration.ofMinutes(5);
    /** Every cache key is {@code KEY_PREFIX + cacheName + "::" + key}. */
    static final String KEY_PREFIX = "admin-api:cache:";

    @Bean
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "memory", matchIfMissing = true)
    CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(TTL.toMinutes(), TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        return manager;
    }

    @Bean
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "memory", matchIfMissing = true)
    CacheInvalidator localCacheInvalidator(CacheManager cacheManager) {
        return cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "redis")
    CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration
                .defaultCacheConfig(getClass().getClassLoader())
                .entryTtl(TTL)
                .prefixCacheNameWith(KEY_PREFIX)
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .build();
    }

    /**
     * Deletes the cache's keys from Redis synchronously. {@code RedisCache.clear()}
     * completes asynchronously in the current Spring Data Redis, which lets a
     * reader racing the commit observe the stale entry; SCAN + DEL here returns
     * only once the keys are gone for every replica.
     */
    @Bean
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "redis")
    CacheInvalidator redisCacheInvalidator(StringRedisTemplate redis) {
        return cacheName -> {
            ScanOptions scan = ScanOptions.scanOptions().match(KEY_PREFIX + cacheName + "::*").count(500).build();
            try (Cursor<String> cursor = redis.scan(scan)) {
                List<String> keys = cursor.stream().toList();
                if (!keys.isEmpty()) {
                    redis.delete(keys);
                }
            }
        };
    }
}
