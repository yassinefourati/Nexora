package com.fourati.app.config;

import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourati.platform.error.ErrorCode;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * Produces:
 *   - Rich API info with contact, license and markdown description
 *   - Server entries for local, dev and prod environments
 *   - JWT Bearer authentication globally wired
 *   - X-Correlation-ID header documented on every operation
 *   - Reusable error response schemas (400, 404, 409, 500)
 *   - Organised tag groups: Domain, Documents, System
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            //  Info 
            .info(new Info()
                .title("Admin API")
                .version("1.0.0")
                .description("""
                    ## Overview
                    REST API built on **Spring Boot 4** · **Java 21**.

                    ### Response envelope
                    Every successful (2xx) endpoint returns the same wrapper:
                    ```json
                    {
                      "success":       true,
                      "status":        200,
                      "message":       "Operation completed",
                      "timestamp":     "2026-06-04T12:00:00.000Z",
                      "correlationId": "d993afa6-...",
                      "data":          { ... },
                      "pagination":    { ... }
                    }
                    ```

                    ### Errors
                    Every error (4xx/5xx) is an RFC 9457 problem-details document
                    (`application/problem+json`), never wrapped in the envelope:
                    ```json
                    {
                      "type":          "https://errors.example.com/resource-not-found",
                      "title":         "Resource not found",
                      "status":        404,
                      "detail":        "User not found with id: abc",
                      "instance":      "/api/v1/users/abc",
                      "code":          "RESOURCE_NOT_FOUND",
                      "retryable":     false,
                      "timestamp":     "2026-06-04T12:00:00.000Z",
                      "correlationId": "d993afa6-...",
                      "violations":    [ { "field": "name", "code": "NOT_BLANK", "message": "must not be blank" } ]
                    }
                    ```
                    Branch on `code` (see `GET /api/v1/errors`), never on `detail`.

                    ### Authentication
                    Click **Authorize** and paste your JWT Bearer token.
                    The token is persisted across browser refreshes.

                    ### Idempotency
                    Send `X-Idempotency-Key: <uuid>` on POST/PUT/PATCH to prevent duplicates on retry.
                    """)
                .contact(new Contact()
                    .name("Yassine Fourati — Engineering")
                    .email("yassine.fourati1994@gmail.com")
                    .url("https://fourati.dev"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))

            //  Servers 
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local")
            ))

            //  Security 
            .addSecurityItem(new SecurityRequirement().addList(BEARER))

            //  Tags (shown as sections in Swagger UI)
            // Only define tags here that have no @Tag-annotated controller (e.g. actuator).
            // Controller-owned tags are declared via @Tag on the controller class.
            .tags(List.of(
                new Tag().name("System")
                    .description("Health probes, metrics and operational endpoints.")
            ))

            //  Reusable components 
            .components(new Components()

                // Security scheme
                .addSecuritySchemes(BEARER, new SecurityScheme()
                    .name(BEARER)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Paste your JWT access token. The `Bearer ` prefix is added automatically."))

                // Global headers
                .addHeaders("X-Correlation-ID", new Header()
                    .description("Request correlation ID — present on every response. "
                        + "Pass it on requests to carry the ID across service boundaries.")
                    .schema(new StringSchema().example("d993afa6-5486-4221-8085-dbcf19fce65f")))

                // Reusable error responses (RFC 9457 problem details, ADR 0005)
                .addResponses("400", errorResponse("Validation Error", ErrorCode.VALIDATION_FAILED,
                    "Request validation failed. See 'violations' for field-level details.",
                    """
                    "violations": [ { "field": "name", "code": "NOT_BLANK", "message": "must not be blank" } ]"""))
                .addResponses("401", errorResponse("Unauthorized", ErrorCode.UNAUTHORIZED,
                    "Authentication is required. Provide a valid Bearer token or X-Api-Key.", null))
                .addResponses("403", errorResponse("Forbidden", ErrorCode.FORBIDDEN,
                    "You do not have permission to perform this action.", null))
                .addResponses("404", errorResponse("Not Found", ErrorCode.RESOURCE_NOT_FOUND,
                    "Resource not found with id: 3fa85f64-...", null))
                .addResponses("409", errorResponse("Conflict", ErrorCode.CONFLICT,
                    "Resource already exists", null))
                .addResponses("422", errorResponse("Business Rule Violation", ErrorCode.BUSINESS_RULE_VIOLATION,
                    "The request is syntactically valid but violates a domain rule.", null))
                .addResponses("500", errorResponse("Internal Server Error", ErrorCode.INTERNAL_ERROR,
                    "An unexpected error occurred. Reference: d993afa6-...", null))
            );
    }

    //  Global operation customizer
    //
    // Automatically injects error responses based on what each operation does.
    // Controllers only need to document success responses (200/201/204).
    //
    // Rules (never overwrites an explicitly declared response):
    //   ALL operations          → 401, 403, 500
    //   POST / PUT / PATCH      → 400  (validation)
    //   Operations with @PathVariable → 404 (resource not found)
    //   POST                    → 409  (conflict / duplicate)
    //   DELETE                  → 404  (not found before delete)

    @Bean
    OperationCustomizer globalErrorResponses() {
        return (operation, handlerMethod) -> {
            var responses = operation.getResponses();
            if (responses == null) {
                responses = new io.swagger.v3.oas.models.responses.ApiResponses();
                operation.setResponses(responses);
            }

//            boolean isGet = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.GetMapping.class);
            boolean isPost = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.PostMapping.class);
            boolean isPut = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.PutMapping.class);
            boolean isPatch = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.PatchMapping.class);
            boolean isDelete = handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class);
            boolean hasPathVar = java.util.Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(org.springframework.web.bind.annotation.PathVariable.class));
            boolean isMutating = isPost || isPut || isPatch || isDelete;

            //  Always present
            add(responses, ErrorCode.UNAUTHORIZED, "Missing or invalid JWT token. Add Bearer <token> to the Authorization header.");
            add(responses, ErrorCode.FORBIDDEN, "Authenticated but does not have the required role, scope or ownership.");
            add(responses, ErrorCode.RATE_LIMIT_EXCEEDED, "Rate limit exceeded. Slow down and retry after the Retry-After delay.");
            add(responses, ErrorCode.INTERNAL_ERROR, "Unexpected server error. Use 'correlationId' to trace in logs.");
            add(responses, ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, "A downstream service is temporarily unavailable. Retry later.");

            //  Mutating methods (POST / PUT / PATCH / DELETE)
            if (isMutating) {
                add(responses, ErrorCode.VALIDATION_FAILED, "Validation failed — see 'violations' for field-level details.");
                add(responses, ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Send the request with Content-Type: application/json.");
            }

            //  Operations addressing a specific resource by ID
            if (hasPathVar || isDelete) {
                add(responses, ErrorCode.RESOURCE_NOT_FOUND, "Resource with the given ID does not exist.");
            }

            //  Creation endpoints
            if (isPost) {
                add(responses, ErrorCode.CONFLICT, "A resource with the same unique key already exists.");
                add(responses, ErrorCode.BUSINESS_RULE_VIOLATION, "Request is syntactically valid but violates a domain rule.");
                add(responses, ErrorCode.CONTENT_TOO_LARGE, "Request body or upload exceeds the configured size limit.");
            }

            //  Update endpoints
            if (isPut || isPatch) {
                add(responses, ErrorCode.ILLEGAL_STATE, "Resource state does not permit this operation.");
                add(responses, ErrorCode.BUSINESS_RULE_VIOLATION, "Request is syntactically valid but violates a domain rule.");
            }

            return operation;
        };
    }

    /** Adds an inline problem-details response only if the developer hasn't explicitly declared that status. */
    private void add(io.swagger.v3.oas.models.responses.ApiResponses responses, ErrorCode errorCode, String detail) {
        String status = String.valueOf(errorCode.status().value());
        if (responses.containsKey(status))
            return;

        io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType()
            .addExamples("example", new Example().value(parseJson(problemExample(errorCode, detail, null))));
        io.swagger.v3.oas.models.media.Content content = new io.swagger.v3.oas.models.media.Content()
            .addMediaType("application/problem+json", mt);

        responses.addApiResponse(status, new ApiResponse()
            .description(errorCode.status().getReasonPhrase())
            .content(content));
    }

    //  API Groups

    @Bean
    GroupedOpenApi systemGroup() {
        return GroupedOpenApi.builder()
            .group("3-system")
            .displayName("System — Health & Metrics")
            .pathsToMatch("/actuator/**")
            .addOperationCustomizer(globalErrorResponses())
            .build();
    }

    @Bean
    GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder()
            .group("0-all")
            .displayName("All endpoints")
            .pathsToMatch("/api/**", "/actuator/**")
            .addOperationCustomizer(globalErrorResponses())
            .build();
    }

    //  Helper 

    private ApiResponse errorResponse(String description, ErrorCode code, String detail, String extraFields) {
        io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType()
            .addExamples("example", new Example().value(parseJson(problemExample(code, detail, extraFields))));
        io.swagger.v3.oas.models.media.Content content = new io.swagger.v3.oas.models.media.Content()
            .addMediaType("application/problem+json", mt);

        return new ApiResponse().description(description).content(content);
    }

    /** An RFC 9457 example body exactly as {@code Problems} produces it. */
    private static String problemExample(ErrorCode code, String detail, String extraFields) {
        String extra = extraFields != null ? ",\n      " + extraFields : "";
        return """
            {
              "type": "https://errors.example.com/%s",
              "title": "%s",
              "status": %d,
              "detail": "%s",
              "instance": "/api/v1/resource",
              "code": "%s",
              "retryable": %s,
              "timestamp": "2026-06-04T12:00:00.000Z",
              "correlationId": "d993afa6-5486-4221-8085"%s
            }""".formatted(code.typeSlug(), code.title(), code.status().value(), detail, code.code(), code.retryable(), extra);
    }

    private Object parseJson(String json) {
        try {
            return MAPPER.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
