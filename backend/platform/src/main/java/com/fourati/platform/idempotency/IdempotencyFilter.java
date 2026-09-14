package com.fourati.platform.idempotency;

import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.Problems;
import com.fourati.platform.idempotency.IdempotencyStore.CachedResponse;
import com.fourati.platform.idempotency.IdempotencyStore.Completed;
import com.fourati.platform.idempotency.IdempotencyStore.Entry;
import com.fourati.platform.idempotency.IdempotencyStore.InFlight;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Idempotency filter — prevents duplicate mutating requests.
 *
 * How it works:
 *   1. Client sends  X-Idempotency-Key: <uuid>  on POST/PUT/PATCH
 *   2. First call: key claimed as in-flight, processed normally, response stored for 24 h
 *   3. Retry with the same key: returns the exact same stored response (status + body)
 *   4. A concurrent call with the same key while the first is still in flight gets 409
 *
 * The key is scoped to (principal, method, path, client key). Keying on the
 * client-supplied value alone would let one user replay another user's response,
 * a data leak that also skips authorization on the replayed request.
 *
 * State lives in an {@link IdempotencyStore}: in-memory for one replica, Redis
 * when {@code fourati.common.store=redis}.
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Idempotency-Key";

    private static final Set<String> IDEMPOTENT_METHODS = Set.of(
        HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name()
    );

    static final Duration TTL = Duration.ofHours(24);
    static final Duration IN_FLIGHT_TTL = Duration.ofSeconds(60);

    private final IdempotencyStore store;
    private final Problems problems;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String clientKey = request.getHeader(HEADER);

        // No key or non-mutating method → pass through
        if (clientKey == null || clientKey.isBlank() || !IDEMPOTENT_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String key = scopedKey(request, clientKey);
        Optional<Entry> existing = store.claim(key, IN_FLIGHT_TTL);

        if (existing.isPresent()) {
            if (existing.get() instanceof InFlight) {
                log.debug("Idempotency key={} still in flight for {} {}", clientKey, request.getMethod(), request.getRequestURI());
                problems.write(response, problems.of(ErrorCode.IDEMPOTENT_REQUEST_IN_FLIGHT,
                    "A request with this idempotency key is still being processed.", request.getRequestURI()));
                return;
            }
            CachedResponse cached = ((Completed) existing.get()).response();
            log.debug("Idempotency hit for key={} method={} uri={}", clientKey, request.getMethod(), request.getRequestURI());
            response.setStatus(cached.status());
            if (cached.contentType() != null) response.setContentType(cached.contentType());
            response.getWriter().write(cached.body());
            return;
        }

        // First call — wrap response to capture it
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        boolean stored = false;
        try {
            chain.doFilter(request, wrapper);
            String body = new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            store.complete(key, new CachedResponse(wrapper.getStatus(), wrapper.getContentType(), body), TTL);
            stored = true;
            wrapper.copyBodyToResponse();
            log.debug("Idempotency stored key={} status={}", clientKey, wrapper.getStatus());
        } finally {
            // Never leave a dangling in-flight marker if the chain threw
            if (!stored) store.release(key);
        }
    }

    private static String scopedKey(HttpServletRequest request, String clientKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";
        return principal + "|" + request.getMethod() + "|" + request.getRequestURI() + "|" + clientKey;
    }
}
