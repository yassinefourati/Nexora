package com.fourati.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The breaker protects JWT validation from a slow or dead Keycloak: after N
 * consecutive failures calls fail fast, one probe is allowed after the open
 * window, and a successful probe closes the circuit again.
 */
class JwksCircuitBreakerTest {

    /** Mutable clock so the open window can be advanced without sleeping. */
    static final class StepClock extends Clock {
        Instant now = Instant.parse("2026-09-13T12:00:00Z");
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void advance(Duration d) { now = now.plus(d); }
    }

    private final StepClock clock = new StepClock();
    private final JwksCircuitBreaker breaker = new JwksCircuitBreaker(3, Duration.ofSeconds(20), clock);
    private final HttpRequest request = new MockClientHttpRequest(org.springframework.http.HttpMethod.GET, URI.create("http://keycloak/certs"));
    private final AtomicInteger executions = new AtomicInteger();

    private ClientHttpRequestExecution failing() {
        return (req, body) -> { executions.incrementAndGet(); throw new IOException("connect timeout"); };
    }

    private ClientHttpRequestExecution succeeding() {
        return (req, body) -> { executions.incrementAndGet(); return new MockClientHttpResponse(new byte[0], HttpStatus.OK); };
    }

    private void callExpectingFailure(ClientHttpRequestExecution exec) {
        assertThatThrownBy(() -> breaker.intercept(request, new byte[0], exec)).isInstanceOf(IOException.class);
    }

    @Test
    void staysClosed_belowThreshold_andResetsOnSuccess() throws IOException {
        callExpectingFailure(failing());
        callExpectingFailure(failing());
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.CLOSED);

        ClientHttpResponse ok = breaker.intercept(request, new byte[0], succeeding());
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.CLOSED);

        // the counter reset: two more failures still do not open it
        callExpectingFailure(failing());
        callExpectingFailure(failing());
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.CLOSED);
    }

    @Test
    void opensAfterThreshold_andFailsFastWithoutCallingKeycloak() {
        for (int i = 0; i < 3; i++) callExpectingFailure(failing());
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.OPEN);

        int before = executions.get();
        assertThatThrownBy(() -> breaker.intercept(request, new byte[0], failing()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("circuit open");
        assertThat(executions.get()).isEqualTo(before); // no network call while open
    }

    @Test
    void halfOpenProbe_closesOnSuccess_reopensOnFailure() throws IOException {
        for (int i = 0; i < 3; i++) callExpectingFailure(failing());
        clock.advance(Duration.ofSeconds(21));

        // first probe fails -> straight back to OPEN
        callExpectingFailure(failing());
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.OPEN);

        clock.advance(Duration.ofSeconds(21));
        AtomicReference<ClientHttpResponse> res = new AtomicReference<>();
        res.set(breaker.intercept(request, new byte[0], succeeding()));
        assertThat(res.get().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.CLOSED);
    }

    @Test
    void serverErrorsCountAsFailures() {
        ClientHttpRequestExecution http503 = (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.SERVICE_UNAVAILABLE);
        for (int i = 0; i < 3; i++) {
            try { breaker.intercept(request, new byte[0], http503); } catch (IOException ignored) { }
        }
        assertThat(breaker.state()).isEqualTo(JwksCircuitBreaker.State.OPEN);
    }
}
