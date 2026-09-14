package com.fourati.platform.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe, immutable configuration for the platform infrastructure, bound
 * from application.yml under {@code fourati.common}:
 *
 * <pre>
 *   fourati:
 *     common:
 *       rate-limit:
 *         rpm: 60
 *         trust-forwarded-headers: false   # true ONLY behind a proxy that overwrites X-Forwarded-For
 *       error:
 *         base-url: https://errors.example.com/
 * </pre>
 *
 * A record: bound once through its constructor, validated, never mutated
 * afterwards. {@code fourati.common.store} is read directly by the
 * auto-configuration conditions and has no component here.
 */
@ConfigurationProperties(prefix = "fourati.common")
@Validated
public record CommonProperties(
        @DefaultValue @Valid RateLimit rateLimit,
        @DefaultValue @Valid Error error) {

    public record RateLimit(
            @DefaultValue("60") @Min(1) @Max(10_000) int rpm,
            /**
             * Whether the first X-Forwarded-For value may be used as the client key.
             * Defaults to false: a client can set that header itself and get a fresh
             * bucket per request, which bypasses the limiter entirely. Enable only when
             * the app sits behind a trusted reverse proxy that strips client-supplied
             * values and appends the real address.
             */
            @DefaultValue("false") boolean trustForwardedHeaders) {
    }

    public record Error(@DefaultValue("https://errors.example.com/") @NotBlank String baseUrl) {
    }
}
