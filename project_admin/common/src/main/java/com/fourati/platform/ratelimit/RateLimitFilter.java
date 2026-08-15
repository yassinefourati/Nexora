package com.fourati.platform.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fourati.platform.properties.CommonProperties;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token-bucket rate limiter keyed by client IP.
 * No external dependencies.
 *
 * When you add the security module, inject SecurityContextHolder here
 * to key on the authenticated user instead of the IP.
 *
 * Configure in application.yml:
 *   app.rate-limit.rpm: 60   # requests per minute per IP
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final CommonProperties CommonProperties;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String key = clientIp(req);
        int rpm = CommonProperties.getRateLimit().getRpm();
        if (buckets.computeIfAbsent(key, k -> new TokenBucket(rpm)).tryConsume()) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded for ip={} path={}", key, req.getRequestURI());
            String typeUrl = CommonProperties.getError().getBaseUrl();
            if (!typeUrl.endsWith("/")) typeUrl += "/";
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType("application/problem+json");
            res.getWriter().write("""
                {"type":"%srate-limit","title":"Too Many Requests","status":429, "detail":"Rate limit exceeded. Please slow down."}
            """.formatted(typeUrl));
        }
    }

    /** Remove buckets that have been idle for more than 10 minutes to prevent unbounded growth. */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    void evictStaleBuckets() {
        long cutoff = System.nanoTime() - TimeUnit.MINUTES.toNanos(10);
        int before = buckets.size();
        buckets.values().removeIf(b -> b.lastUsedNano() < cutoff);
        int removed = before - buckets.size();
        if (removed > 0) {
            log.debug("RateLimitFilter evicted {} stale bucket(s), remaining={}", removed, buckets.size());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        // /ws is excluded: a single STOMP/SockJS session generates many rapid
        // HTTP requests on its own (the info probe, XHR-streaming/polling
        // chunks, periodic re-polls) that have nothing to do with a client
        // hammering the REST API — counting them against the same per-IP
        // budget as real API calls made the WebSocket connection itself
        // trip the rate limiter under normal use. Auth for /ws happens at the
        // STOMP CONNECT frame (WebSocketAuthChannelInterceptor), not here.
        return p.startsWith("/actuator") || p.startsWith("/v3/api-docs") || p.startsWith("/swagger-ui")
            || p.startsWith("/ws");
    }

    private String clientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return fwd != null && !fwd.isBlank() ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }

    static final class TokenBucket {
        private final long nanosBetweenTokens;
        private final AtomicLong nextTokenNano;
        private volatile long lastUsed;

        TokenBucket(int rpm) {
            this.nanosBetweenTokens = 60_000_000_000L / Math.max(rpm, 1);
            this.lastUsed = System.nanoTime();
            this.nextTokenNano = new AtomicLong(this.lastUsed);
        }

        boolean tryConsume() {
            long now = System.nanoTime();
            lastUsed = now;
            while (true) {
                long cur = nextTokenNano.get();
                if (cur > now) return false;
                if (nextTokenNano.compareAndSet(cur, cur + nanosBetweenTokens)) return true;
            }
        }

        long lastUsedNano() { return lastUsed; }
    }
}