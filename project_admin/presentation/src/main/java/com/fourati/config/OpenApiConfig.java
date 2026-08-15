package com.fourati.config;

import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                    Every endpoint returns the same wrapper:
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
                    All error responses follow the same shape with `"success": false` and an `error` block.

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

                // Reusable error responses
                .addResponses("400", errorResponse("Validation Error",
                    "validation", 400, "Validation failed",
                    """
                    "fieldErrors": { "name": ["must not be blank"] }"""))
                .addResponses("401", errorResponse("Unauthorized",
                    "unauthorized", 401, "Authentication is required", null))
                .addResponses("403", errorResponse("Forbidden",
                    "forbidden", 403, "You do not have permission", null))
                .addResponses("404", errorResponse("Not Found",
                    "not-found", 404, "Resource not found with id: 3fa85f64-...", null))
                .addResponses("409", errorResponse("Conflict",
                    "conflict", 409, "Resource already exists", null))
                .addResponses("422", errorResponse("Business Rule Violation",
                    "business-rule", 422, "Invoice has already been paid",
                    """
                    "errorCode": "INVOICE_ALREADY_PAID" """))
                .addResponses("500", errorResponse("Internal Server Error",
                    "internal", 500,
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
            add(responses, "401", "Unauthorized",
                "UNAUTHORIZED", "Missing or invalid JWT token. Add Bearer <token> to the Authorization header.", false);
            add(responses, "403", "Forbidden",
                "FORBIDDEN", "Authenticated but does not have the required role or scope.", false);
            add(responses, "429", "Too Many Requests",
                "RATE_LIMIT_EXCEEDED", "Rate limit exceeded. Slow down and retry after the reset window.", true);
            add(responses, "500", "Internal Server Error",
                "INTERNAL_ERROR", "Unexpected server error. Use 'requestId' to trace in logs.", false);
            add(responses, "503", "Service Unavailable",
                "EXTERNAL_SERVICE_UNAVAILABLE", "A downstream service is temporarily unavailable. Retry later.", true);

            //  Mutating methods (POST / PUT / PATCH / DELETE) 
            if (isMutating) {
                add(responses, "400", "Bad Request",
                    "VALIDATION_FAILED", "Validation failed — see 'violations' for field-level details.", false);
                add(responses, "415", "Unsupported Media Type",
                    "UNSUPPORTED_MEDIA_TYPE", "Send the request with Content-Type: application/json.", false);
            }

            //  Operations addressing a specific resource by ID 
            if (hasPathVar || isDelete) {
                add(responses, "404", "Not Found", "RESOURCE_NOT_FOUND", "Resource with the given ID does not exist.", false);
            }

            //  Creation endpoints 
            if (isPost) {
                add(responses, "409", "Conflict", "CONFLICT", "A resource with the same unique key already exists.", false);
                add(responses, "422", "Unprocessable Entity", "BUSINESS_RULE_VIOLATION", "Request is syntactically valid but violates a domain rule.", false);
                add(responses, "413", "Payload Too Large", "UPLOAD_TOO_LARGE", "Request body or upload exceeds the configured size limit.", false);
            }

            //  Update endpoints 
            if (isPut || isPatch) {
                add(responses, "409", "Conflict", "ILLEGAL_STATE", "Resource state does not permit this operation.", false);
                add(responses, "422", "Unprocessable Entity", "BUSINESS_RULE_VIOLATION", "Request is syntactically valid but violates a domain rule.", false);
            }

            return operation;
        };
    }

    /** Adds an inline ApiResponse only if the developer hasn't explicitly declared that code. */
    private void add(io.swagger.v3.oas.models.responses.ApiResponses responses, String code, String description, String errorCode, String detail, boolean retryable) {
        if (responses.containsKey(code)) 
        	return;

        String exampleJson = """
            {
              "status": %s,
              "error": {
                "code": "%s",
                "message": "%s",
                "retryable": %s
              },
              "request": {
                "id": "d993afa6-5486-4221-8085",
                "timestamp": "2026-06-05T09:00:00.000Z"
              }
            }""".formatted(code, errorCode, detail, retryable);

        Object exampleObj = parseJson(exampleJson);
        io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType().addExamples("example", new Example().value(exampleObj));
        io.swagger.v3.oas.models.media.Content content = new io.swagger.v3.oas.models.media.Content().addMediaType("application/problem+json", mt);

        responses.addApiResponse(code, new ApiResponse()
            .description(description)
            .content(content));
    }

    //  API Groups

    @Bean
    GroupedOpenApi domainGroup() {
        return GroupedOpenApi.builder()
            .group("1-domain")
            .displayName("Domain — Items")
            .pathsToMatch("/api/v1/items/**")
            .addOperationCustomizer(globalErrorResponses())
            .build();
    }

    @Bean
    GroupedOpenApi documentsGroup() {
        return GroupedOpenApi.builder()
            .group("2-documents")
            .displayName("Documents — Upload & Download")
            .pathsToMatch("/api/v1/documents/**")
            .addOperationCustomizer(globalErrorResponses())
            .build();
    }

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

    private ApiResponse errorResponse(String description, String code, int status, String message, String extraFields) {
        String extra = extraFields != null ? ",\n      " + extraFields : "";
        String exampleJson = """
            {
              "success": false,
              "status": %d,
              "message": "%s",
              "timestamp": "2026-06-04T12:00:00.000Z",
              "correlationId": "d993afa6-5486-4221-8085"%s,
              "error": {
                "code": "%s",
                "type": "https://api.example.com/errors/%s",
                "instance": "/api/v1/resource"
              }
            }""".formatted(status, message, extra, code, code);

        Object exampleObj = parseJson(exampleJson);
        io.swagger.v3.oas.models.media.MediaType mt = new io.swagger.v3.oas.models.media.MediaType().addExamples("example", new Example().value(exampleObj));
        io.swagger.v3.oas.models.media.Content content = new io.swagger.v3.oas.models.media.Content().addMediaType("application/json", mt);

        return new ApiResponse().description(description).content(content);
    }

    private Object parseJson(String json) {
        try {
            return MAPPER.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
