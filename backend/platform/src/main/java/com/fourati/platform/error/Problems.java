package com.fourati.platform.error;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one factory for RFC 9457 problem-details bodies (ADR 0005). Every error
 * the API emits, whether from the MVC exception handler, a Spring Security
 * entry point or a servlet filter, is built here so the shape cannot drift.
 *
 * Standard members: {@code type}, {@code title}, {@code status}, {@code detail},
 * {@code instance}. Extension members: {@code code} (the stable {@link ErrorCode}),
 * {@code correlationId}, {@code retryable}, {@code timestamp} and, for validation
 * failures, {@code violations}.
 */
public class Problems {

    public static final String CODE = "code";
    public static final String CORRELATION_ID = "correlationId";
    public static final String RETRYABLE = "retryable";
    public static final String TIMESTAMP = "timestamp";
    public static final String VIOLATIONS = "violations";

    /** A single field-level constraint violation (present on VALIDATION_FAILED and TYPE_MISMATCH). */
    public record Violation(String field, String code, String message, Object rejectedValue) {
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String typeBaseUrl;

    /** @param errorBaseUrl base of the {@code type} URI, for example {@code https://errors.example.com/}. */
    public Problems(String errorBaseUrl) {
        this.typeBaseUrl = errorBaseUrl.endsWith("/") ? errorBaseUrl : errorBaseUrl + "/";
    }

    public ProblemDetail of(ErrorCode code, String detail, String instance) {
        return of(code, detail, instance, null);
    }

    public ProblemDetail of(ErrorCode code, String detail, String instance, List<Violation> violations) {
        return of(code.status(), code.code(), code.title(), detail, code.retryable(), instance, violations);
    }

    /**
     * Builds a problem for a code that is not in the {@link ErrorCode} catalog
     * (for example a status name coming from a {@code ResponseStatusException}).
     */
    public ProblemDetail of(HttpStatusCode status, String code, String title, String detail, boolean retryable,
                            String instance, List<Violation> violations) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(typeBaseUrl + code.toLowerCase().replace('_', '-')));
        problem.setTitle(title != null ? title : reasonPhrase(status));
        problem.setDetail(detail);
        if (instance != null) {
            try {
                problem.setInstance(URI.create(instance));
            } catch (IllegalArgumentException ignored) {
                // an unparseable request path is not worth failing the error response over
            }
        }
        problem.setProperty(CODE, code);
        problem.setProperty(RETRYABLE, retryable);
        problem.setProperty(TIMESTAMP, Instant.now().toString());
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty(CORRELATION_ID, correlationId);
        }
        if (violations != null && !violations.isEmpty()) {
            problem.setProperty(VIOLATIONS, violations);
        }
        return problem;
    }

    /** Serializes exactly what Spring MVC would serialize for the same {@link ProblemDetail}. */
    public String toJson(ProblemDetail problem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", problem.getType().toString());
        body.put("title", problem.getTitle());
        body.put("status", problem.getStatus());
        if (problem.getDetail() != null) {
            body.put("detail", problem.getDetail());
        }
        if (problem.getInstance() != null) {
            body.put("instance", problem.getInstance().toString());
        }
        if (problem.getProperties() != null) {
            body.putAll(problem.getProperties());
        }
        return JSON.writeValueAsString(body);
    }

    /** Writes the problem as the complete response: status, {@code application/problem+json}, body. */
    public void write(HttpServletResponse response, ProblemDetail problem) throws IOException {
        response.setStatus(problem.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(toJson(problem));
        response.getWriter().flush();
    }

    private static String reasonPhrase(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.getReasonPhrase() : String.valueOf(status.value());
    }
}
