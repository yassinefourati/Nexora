package com.fourati.contract;

import com.fourati.app.security.ApiKeyAuthenticationFilter;
import com.fourati.app.security.AppJwtAuthenticationConverter;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.GlobalExceptionHandler;
import com.fourati.platform.error.Problems;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.idempotency.IdempotencyFilter;
import com.fourati.platform.properties.CommonProperties;
import com.fourati.platform.ratelimit.RateLimitFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The error contract every client relies on (ADR 0005), tested as a web slice:
 * no database, no Docker, no security filter chain. A fixture controller throws
 * each exception family and the test pins the HTTP status, the media type and
 * the RFC 9457 body produced by {@link GlobalExceptionHandler}:
 * {@code type, title, status, detail, instance} plus the extension members
 * {@code code, retryable, timestamp} and {@code violations} for validation.
 */
@WebMvcTest(
        controllers = ErrorContractTest.FixtureController.class,
        // The resource-server module registers itself for web slices but needs HttpSecurity from
        // the security slice, which this test does not load (filters are off anyway).
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                // Component-scanned filters and converters whose dependencies (stores, services) do
                // not exist in a web slice. The correlation filter has none and stays in.
                classes = {ApiKeyAuthenticationFilter.class, AppJwtAuthenticationConverter.class,
                        RateLimitFilter.class, IdempotencyFilter.class}))
// The fixture controller is nested in a test class, which Boot's TestTypeExcludeFilter keeps out
// of component scanning on purpose (so it never leaks into a real context or the OpenAPI
// snapshot); it is registered explicitly here instead.
@Import({GlobalExceptionHandler.class, ErrorContractTest.FixtureController.class, ErrorContractTest.SliceConfig.class})
@EnableConfigurationProperties(CommonProperties.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ErrorContractTest extends AbstractContractTest {

    private static final String BASE = "/fixture";

    @TestConfiguration
    static class SliceConfig {
        @Bean
        Problems problems(CommonProperties props) {
            return new Problems(props.error().baseUrl());
        }
    }

    // --- security ---------------------------------------------------------------

    @Test
    void accessDenied_is403_notA500() throws Exception {
        // A @PreAuthorize denial surfaces as AuthorizationDeniedException (an AccessDeniedException).
        // Before this handler existed the catch-all turned every 403 into a 500.
        expectProblem(mvc.perform(get(BASE + "/forbidden")), ErrorCode.FORBIDDEN, BASE + "/forbidden");
    }

    // --- domain -----------------------------------------------------------------

    @Test
    void resourceNotFound_is404_withStableCode() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/not-found")), ErrorCode.RESOURCE_NOT_FOUND, BASE + "/not-found")
                .andExpect(jsonPath("$.detail").value("Widget not found with id: 42"));
    }

    @Test
    void conflict_is409() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/conflict")), ErrorCode.CONFLICT, BASE + "/conflict");
    }

    @Test
    void businessRule_is422_withTheRuleCodeAndCatalogTitle() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/rule")), ErrorCode.USER_NOT_ACTIVE, BASE + "/rule")
                .andExpect(jsonPath("$.detail").value("User is suspended"));
    }

    @Test
    void staleVersion_is409_concurrentModification_andRetryable() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/stale")), ErrorCode.CONCURRENT_MODIFICATION, BASE + "/stale")
                .andExpect(jsonPath("$.retryable").value(true));
    }

    // --- validation -------------------------------------------------------------

    @Test
    void invalidBody_is400_withFieldViolations() throws Exception {
        String body = """
                {"name":"","email":"not-an-email"}
                """;
        expectProblem(mvc.perform(post(BASE + "/validate").contentType(MediaType.APPLICATION_JSON).content(body)),
                ErrorCode.VALIDATION_FAILED, BASE + "/validate")
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[*].field").exists())
                .andExpect(jsonPath("$.violations[*].code").exists())
                .andExpect(jsonPath("$.violations[*].message").exists());
    }

    @Test
    void malformedJson_is400() throws Exception {
        expectProblem(mvc.perform(post(BASE + "/validate").contentType(MediaType.APPLICATION_JSON).content("{not json")),
                ErrorCode.MALFORMED_REQUEST_BODY, BASE + "/validate");
    }

    @Test
    void wrongPathVariableType_is400_typeMismatch() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/typed/not-a-uuid")), ErrorCode.TYPE_MISMATCH, BASE + "/typed/not-a-uuid")
                .andExpect(jsonPath("$.violations[0].field").value("id"));
    }

    @Test
    void missingRequiredParameter_is400() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/needs-q")), ErrorCode.MISSING_PARAMETER, BASE + "/needs-q")
                .andExpect(jsonPath("$.violations[0].field").value("q"));
    }

    // --- routing ----------------------------------------------------------------

    @Test
    void unknownPath_is404_endpointNotFound() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/does-not-exist")), ErrorCode.ENDPOINT_NOT_FOUND, BASE + "/does-not-exist");
    }

    @Test
    void wrongMethod_is405() throws Exception {
        expectProblem(mvc.perform(post(BASE + "/not-found")), ErrorCode.METHOD_NOT_ALLOWED, BASE + "/not-found");
    }

    @Test
    void unsupportedContentType_is415() throws Exception {
        expectProblem(mvc.perform(post(BASE + "/validate").contentType(MediaType.TEXT_PLAIN).content("x")),
                ErrorCode.UNSUPPORTED_MEDIA_TYPE, BASE + "/validate");
    }

    // --- catch-all --------------------------------------------------------------

    @Test
    void unexpectedException_is500_withoutLeakingTheMessage() throws Exception {
        expectProblem(mvc.perform(get(BASE + "/boom")), ErrorCode.INTERNAL_ERROR, BASE + "/boom")
                .andExpect(jsonPath("$.detail", not(containsString("secret"))));
    }

    // --- helpers ----------------------------------------------------------------

    /** Every error, whatever the family, is a problem-details document with these members. */
    private ResultActions expectProblem(ResultActions actions, ErrorCode code, String instance) throws Exception {
        return actions
                .andExpect(status().is(code.status().value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", endsWith("/" + code.typeSlug())))
                .andExpect(jsonPath("$.title").value(code.title()))
                .andExpect(jsonPath("$.status").value(code.status().value()))
                .andExpect(jsonPath("$.detail").isString())
                .andExpect(jsonPath("$.instance").value(instance))
                .andExpect(jsonPath("$.code").value(code.code()))
                .andExpect(jsonPath("$.retryable").value(code.retryable()))
                .andExpect(jsonPath("$.timestamp").isString())
                // the success envelope never wraps an error
                .andExpect(jsonPath("$.success").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // --- fixture ----------------------------------------------------------------

    record ValidatedBody(@NotBlank String name, @Email String email) {
    }

    @RestController
    @RequestMapping(BASE)
    static class FixtureController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Widget", 42);
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new ConflictException("Widget already exists");
        }

        @GetMapping("/rule")
        void rule() {
            throw new BusinessRuleException(ErrorCode.USER_NOT_ACTIVE, "User is suspended");
        }

        @GetMapping("/stale")
        void stale() {
            throw new ObjectOptimisticLockingFailureException(Object.class, 42);
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AuthorizationDeniedException("Access Denied");
        }

        @PostMapping("/validate")
        String validate(@Valid @RequestBody ValidatedBody body) {
            return "ok";
        }

        @GetMapping("/typed/{id}")
        String typed(@PathVariable UUID id) {
            return id.toString();
        }

        @GetMapping("/needs-q")
        String needsQ(@RequestParam String q) {
            return q;
        }

        @GetMapping("/boom")
        void boom() {
            throw new RuntimeException("secret internal detail");
        }
    }
}
