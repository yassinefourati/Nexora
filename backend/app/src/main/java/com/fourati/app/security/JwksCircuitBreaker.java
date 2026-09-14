package com.fourati.app.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker around the HTTP client that fetches Keycloak's JWKS and
 * OpenID metadata. When Keycloak is down, the breaker opens after
 * {@code failureThreshold} consecutive failures and JWT validation fails fast
 * (401) instead of every request waiting for a connect timeout. Cached keys
 * keep working: Nimbus only calls out on cache miss or key rotation.
 *
 * States: CLOSED (normal) → OPEN after N consecutive failures → HALF_OPEN after
 * {@code openDuration}, where one probe call is let through; success closes the
 * circuit, failure re-opens it. Deliberately dependency-free.
 */
@Slf4j
public final class JwksCircuitBreaker implements ClientHttpRequestInterceptor {

    enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final Duration openDuration;
    private final Clock clock;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private volatile Instant openedAt = Instant.EPOCH;

    public JwksCircuitBreaker() {
        this(3, Duration.ofSeconds(20), Clock.systemUTC());
    }

    JwksCircuitBreaker(int failureThreshold, Duration openDuration, Clock clock) {
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
        this.clock = clock;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!allowCall()) {
            throw new IOException("Keycloak unavailable (circuit open): " + request.getURI());
        }
        try {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is5xxServerError()) {
                onFailure();
            } else {
                onSuccess();
            }
            return response;
        } catch (IOException | RuntimeException ex) {
            onFailure();
            throw ex;
        }
    }

    private boolean allowCall() {
        State current = state.get();
        if (current == State.CLOSED) {
            return true;
        }
        if (current == State.OPEN && Duration.between(openedAt, clock.instant()).compareTo(openDuration) >= 0) {
            if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                log.info("Keycloak JWKS circuit OPEN -> HALF_OPEN, letting one probe through");
                return true;
            }
        }
        return false; // OPEN and not yet time, or HALF_OPEN with a probe already in flight
    }

    private void onSuccess() {
        consecutiveFailures.set(0);
        State previous = state.getAndSet(State.CLOSED);
        if (previous != State.CLOSED) {
            log.info("Keycloak JWKS circuit {} -> CLOSED", previous);
        }
    }

    private void onFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        State current = state.get();
        if (current == State.HALF_OPEN || (current == State.CLOSED && failures >= failureThreshold)) {
            openedAt = clock.instant();
            state.set(State.OPEN);
            log.warn("Keycloak JWKS circuit {} -> OPEN after {} consecutive failure(s); retrying in {}s",
                    current, failures, openDuration.toSeconds());
        }
    }

    State state() {
        return state.get();
    }
}
