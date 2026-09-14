# ProjectAdmin backend (`admin-api`)

Modular Spring Boot 4 REST API (Java 21 bytecode on a Java 25 runtime image) for the admin domain: users, roles,
permissions, organizations, menus, audit and security logs, notifications,
tags, comments, attachments (33 tables). Authentication is delegated to
Keycloak (OIDC); this service validates JWTs and API keys, links every
identity-provider subject to a local user, and enforces authorization per
endpoint.

## Modules

Two Maven modules (ADR 0004):

| Module | Content | Depends on |
|---|---|---|
| `platform` | Cross-cutting library: RFC 9457 problem details and `ErrorCode` catalog, `CurrentUser` principal, rate limiting and idempotency (in-memory or Redis stores), audit aspect, CSV/PDF export, correlation ids. Domain-agnostic, an *open* Spring Modulith module; declares APIs only, never a Boot starter. | — |
| `app` | The application: business domains as Spring Modulith modules (entities, repositories, use cases, controllers), Flyway migrations (V1–V19), bootstrap, security and web adapters, and every test (unit, contract slices, Testcontainers integration, ArchUnit, Modulith verification). The only runnable jar. | `platform` |

Inside `app`, each business domain is a top-level package under `com.fourati`
(`identity`, `access`, `organization`, `configuration`, `content`, `notification`,
`audit`) with a public root package (records, facade, events) and an `internal`
package (`domain`, `application`, `persistence`, `web`); `app` is the application
shell (security, config, cache, error catalog) and `shared` the open kernel.
Modules talk only through facades and events; users and organizations are
referenced by id from other modules. Generated module diagrams land in
`app/target/spring-modulith-docs` on every test run. `ModularityTest`
fails the build on a cross-module `internal` import or a cycle; `ArchitectureTest`
keeps controllers thin, the domain free of the web stack, repositories private to
their module, and `@PreAuthorize` on every mutating endpoint.

## Run

```bash
make hooks          # once per clone: pre-push hook running `make verify` (see ADR 0008)
make run            # dev profile; starts Postgres, Keycloak, Redis, RabbitMQ via ../docker-compose.yml (profile infra)
make test-unit      # unit + architecture + contract slice tests, no Docker
make test-integration
make verify         # everything the pre-push hook runs
make openapi-update # refresh app/src/test/resources/contract/openapi.json after an intentional API change
make check          # backend verify + frontend lint/test/build
make image          # admin-api image: Java 25 runtime, layered, CDS-trained, SBOM inside (tags latest + git sha)
make sbom           # CycloneDX SBOM to app/target/classes/META-INF/sbom/application.cdx.json
```

- Swagger UI: http://localhost:8080/swagger-ui.html (dev/docker only)
- Health: http://localhost:8080/actuator/health (dev) or :8081 where a management port is configured
- SBOM: http://localhost:8081/actuator/sbom/application on the management port (authenticated on :8080)

## Configuration that matters

| Property / env | Purpose | Default |
|---|---|---|
| `fourati.common.store` / `STORE` | `memory` (one replica) or `redis` (shared rate-limit buckets, idempotency keys and cache; required for more than one replica) | `memory`; `redis` in dev/docker/staging/prod |
| `app.websocket.relay.enabled` / `WS_RELAY_ENABLED` | RabbitMQ STOMP relay for WebSocket push across replicas | off; on in dev/docker/staging/prod |
| `management.server.port` / `MANAGEMENT_PORT` | Health and metrics on a port the ingress never routes; `/actuator/prometheus` is anonymous only there | 8081 in docker/staging/prod |
| `management.tracing.export.enabled` / `TRACING_EXPORT_ENABLED` | OTLP traces to `OTEL_EXPORTER_OTLP_ENDPOINT` | off; on in docker/staging/prod |
| `fourati.common.rate-limit.rpm` / `RATE_LIMIT_RPM` | Requests per minute per user (per IP when anonymous) | 60 |
| `fourati.common.rate-limit.trust-forwarded-headers` | Honour `X-Forwarded-For`; only behind a proxy that overwrites it | false |
| `app.cors.allowed-origins` / `APP_CORS_ALLOWED_ORIGINS` | Explicit origins; the app refuses to start in `prod` with a wildcard | `*` (dev) |
| `app.audit.retention-months`, `app.audit.partitions-ahead` | `audit_logs` monthly partitions kept / pre-created | 24 / 2 |
| `app.export.max-rows` | Cap for `GET /audit-logs/export` | 10000 |

## Security model in one paragraph

A request is authenticated by `X-Api-Key` (scoped authorities, never admin) or
by a Keycloak JWT. `AppJwtAuthenticationConverter` resolves (and provisions) the backend user row
linked to the JWT subject into the `CurrentUser` principal, once per request. `RateLimitFilter` and `IdempotencyFilter` key on that
principal. Method security decides: `hasRole('ADMIN')` for administration,
`isAuthenticated()` for shared reads, and `@ownership.*(#id, authentication)`
for user-owned rows. Every 4xx carries a stable `ErrorCode`. Updates carry
`version` and a stale one is refused with 409. Audit rows are written in the same
transaction as the change, attributed to the `CurrentUser` principal (ADR 0006, ADR 0007).

## Tech stack

Java 21 source level on a Java 25 runtime image, Spring Boot 4, PostgreSQL 17 + Flyway, Spring Security (OAuth2
resource server), Spring Data Redis, WebSocket (STOMP over SockJS, RabbitMQ
relay), Micrometer + OpenTelemetry, Resilience4j, MapStruct, Lombok, Springdoc,
ArchUnit, JUnit 5, Testcontainers.
