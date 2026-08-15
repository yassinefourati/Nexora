package com.fourati.platform.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotency filter — prevents duplicate mutating requests.
 *
 * How it works:
 *   1. Client sends  X-Idempotency-Key: <uuid>  on POST/PUT/PATCH
 *   2. First call: processes normally, caches response for 24 h
 *   3. Retry with same key: returns the exact same cached response (status + body)
 *
 * Why you need this:
 *   - Network glitch causes client to retry a payment → charged twice without this
 *   - User double-clicks a submit button → two orders created
 *   - Mobile app retries on timeout → duplicate record
 *
 * Production note:
 *   This uses an in-memory cache (lost on restart).
 *   For production, replace CachedResponse store with Redis:
 *     redisTemplate.opsForValue().set(key, response, 24, TimeUnit.HOURS)
 */

@Slf4j
@Order(2)
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Idempotency-Key";

    private static final Set<String> IDEMPOTENT_METHODS = Set.of(
        HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name()
    );

    private static final long TTL_SECONDS = 86_400L; // 24 hours

    private final Map<String, CachedResponse> store = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String key = request.getHeader(HEADER);

        // No key or non-mutating method → pass through
        if (key == null || !IDEMPOTENT_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Duplicate request — replay cached response
        CachedResponse cached = store.get(key);
        if (cached != null && !cached.isExpired()) {
            log.debug("Idempotency hit for key={} method={} uri={}", key, request.getMethod(), request.getRequestURI());
            response.setStatus(cached.status());
            response.setContentType(cached.contentType());
            response.getWriter().write(cached.body());
            return;
        }

        // First call — wrap response to capture it
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        // Cache the response
        String body = new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
        store.put(key, new CachedResponse(
            wrapper.getStatus(),
            wrapper.getContentType(),
            body,
            Instant.now().plusSeconds(TTL_SECONDS)
        ));
        wrapper.copyBodyToResponse();
        log.debug("Idempotency stored key={} status={}", key, wrapper.getStatus());
    }

    /** Purge expired entries every hour to prevent unbounded memory growth. */
    @Scheduled(fixedDelay = 3_600_000)
    public void evictExpired() {
        int before = store.size();
        store.entrySet().removeIf(e -> e.getValue().isExpired());
        int removed = before - store.size();
        if (removed > 0) log.debug("Idempotency cache: evicted {} expired entries", removed);
    }

    private record CachedResponse(int status, String contentType, String body, Instant expiresAt) {
        boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }
}
