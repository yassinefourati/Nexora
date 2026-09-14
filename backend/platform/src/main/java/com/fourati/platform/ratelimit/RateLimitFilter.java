package com.fourati.platform.ratelimit;

import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.Problems;
import com.fourati.platform.properties.CommonProperties;
import com.fourati.platform.security.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Token-bucket rate limiter. Bucket state lives in a {@link RateLimitStore}:
 * in-memory for a single replica, Redis when {@code fourati.common.store=redis}.
 *
 * Bucket key, in order of preference:
 *   1. the authenticated principal (JWT subject, API-key owner) — one budget per user, regardless of IP
 *   2. the client IP as seen by the servlet container
 *
 * X-Forwarded-For is only consulted when {@code fourati.common.rate-limit.trust-forwarded-headers}
 * is true. A client can set that header itself, so trusting it unconditionally lets anyone obtain a
 * fresh bucket per request and bypass the limiter.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final CommonProperties commonProperties;
    private final RateLimitStore store;
    private final Problems problems;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String key = clientKey(req);
        int rpm = commonProperties.rateLimit().rpm();
        if (store.tryConsume(key, rpm)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded for key={} path={}", key, req.getRequestURI());
            res.setHeader("Retry-After", "60");
            problems.write(res, problems.of(ErrorCode.RATE_LIMIT_EXCEEDED,
                "Rate limit exceeded. Retry after the Retry-After delay.", req.getRequestURI()));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        // /ws is excluded: a single STOMP/SockJS session generates many rapid
        // HTTP requests on its own (the info probe, XHR-streaming/polling
        // chunks, periodic re-polls) that have nothing to do with a client
        // hammering the REST API. Auth for /ws happens at the STOMP CONNECT
        // frame (WebSocketAuthChannelInterceptor), not here.
        return p.startsWith("/actuator") || p.startsWith("/v3/api-docs") || p.startsWith("/swagger-ui")
            || p.startsWith("/ws");
    }

    private String clientKey(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<CurrentUser> user = CurrentUser.from(auth);
        if (user.isPresent()) {
            CurrentUser u = user.get();
            if (u.subject() != null) return "sub:" + u.subject();
            if (u.userId() != null) return "user:" + u.userId();
            return "user:" + u.username();
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        return "ip:" + clientIp(req);
    }

    private String clientIp(HttpServletRequest req) {
        if (commonProperties.rateLimit().trustForwardedHeaders()) {
            String fwd = req.getHeader("X-Forwarded-For");
            if (fwd != null && !fwd.isBlank()) {
                return fwd.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }
}
