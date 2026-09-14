package com.fourati.integration;

import com.fourati.platform.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The security entry points as a client sees them, through the real filter
 * chain: anonymous access, wrong role, invalid API key, CORS preflight and the
 * actuator split between the public and the management port.
 *
 * Every refusal is an RFC 9457 problem-details body with a stable {@code code}
 * (ADR 0005), whether it comes from Spring Security's entry point, the API-key
 * filter or a {@code @PreAuthorize} denial inside a controller.
 */
class SecurityContractIntegrationTest extends AbstractIntegrationTest {

    @Test
    void anonymous_onProtectedEndpoint_is401_problem_withBearerChallenge() throws Exception {
        expectProblem(mvc.perform(get("/api/v1/users")), ErrorCode.UNAUTHORIZED, "/api/v1/users")
           .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    void authenticatedUser_canReadSharedResources() throws Exception {
        mvc.perform(get("/api/v1/users").with(TestAuthentications.asPerson("reader", "user")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void authenticatedUser_withoutAdminRole_is403_problem_withCorrelationId() throws Exception {
        String body = """
                {"username":"x","email":"x@example.com","firstName":"X","lastName":"Y"}
                """;
        expectProblem(mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("X-Correlation-ID", "corr-123")
                        .with(TestAuthentications.asPerson("reader", "user"))),
                ErrorCode.FORBIDDEN, "/api/v1/users")
           .andExpect(jsonPath("$.correlationId").value("corr-123"))
           .andExpect(header().string("X-Correlation-ID", "corr-123"));
    }

    @Test
    void invalidApiKey_is401_problem() throws Exception {
        expectProblem(mvc.perform(get("/api/v1/users").header("X-Api-Key", "fourati_not_a_real_key")),
                ErrorCode.UNAUTHORIZED, "/api/v1/users")
           .andExpect(jsonPath("$.detail").value("Invalid, revoked or expired API key."));
    }

    @Test
    void corsPreflight_isAlwaysAllowed() throws Exception {
        mvc.perform(options("/api/v1/users")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
           .andExpect(status().isOk())
           .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void health_isPublic() throws Exception {
        mvc.perform(get("/actuator/health"))
           .andExpect(status().isOk());
    }

    @Test
    void prometheus_onThePublicPort_requiresAuthentication() throws Exception {
        // Anonymous scraping is only permitted on the dedicated management port,
        // which MockMvc never simulates: on the public port it must be refused.
        expectProblem(mvc.perform(get("/actuator/prometheus")), ErrorCode.UNAUTHORIZED, "/actuator/prometheus");
    }

    @Test
    void openApiDocument_isPublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.openapi").exists());
    }

    private static ResultActions expectProblem(ResultActions actions, ErrorCode code, String instance) throws Exception {
        return actions
                .andExpect(status().is(code.status().value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(code.status().value()))
                .andExpect(jsonPath("$.title").value(code.title()))
                .andExpect(jsonPath("$.code").value(code.code()))
                .andExpect(jsonPath("$.instance").value(instance))
                .andExpect(jsonPath("$.success").doesNotExist());
    }
}
