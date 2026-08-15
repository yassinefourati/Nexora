package com.fourati.platform.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration for platform infrastructure, bound from application.yml under "fourati.common".
 *
 * Usage in application.yml:
 *   fourati:
 *     common:
 *       rate-limit:
 *         rpm: 60
 *       error:
 *         base-url: https://errors.example.com/
 */
@ConfigurationProperties(prefix = "fourati.common")
@Validated
public class CommonProperties {

    @NotNull
    private RateLimit rateLimit = new RateLimit();

    @NotNull
    private Error error = new Error();

    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }

    public Error getError() { return error; }
    public void setError(Error error) { this.error = error; }

    public static class RateLimit {
        @Min(1) @Max(10_000)
        private int rpm = 60;

        public int getRpm() { return rpm; }
        public void setRpm(int rpm) { this.rpm = rpm; }
    }

    public static class Error {
        @NotBlank
        private String baseUrl = "https://errors.example.com/";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
