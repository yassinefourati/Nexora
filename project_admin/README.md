# ProjectAdmin

**ProjectAdmin** is a layered Spring Boot 4 / Java 21 REST backend for an admin
domain (users, roles, permissions, organizations, menus, audit/security logs,
notifications, tags/comments/attachments, and more — 33 tables). Built as a
Maven multi-module project so each architectural layer is a physically
separate, independently versioned artifact — not just a package convention.

---

## Modules

| Module | Layer | Depends on |
|---|---|---|
| `common` | Cross-cutting platform infrastructure: error handling, rate limiting, audit events, CSV/PDF export, correlation IDs | — |
| `persistence` | JPA entities, Spring Data repositories, Flyway migrations | `common` |
| `business` | Services, MapStruct mappers, request/response DTOs | `persistence`, `common` |
| `presentation` | REST controllers, security/websocket/cache config, application bootstrap — produces the runnable jar | `business` (transitively all) |
| `test` | Unit, integration and architecture tests spanning every module | all |

Layering is enforced at build time by `ArchitectureTest` (ArchUnit) in
`test` — e.g. controllers can't call repositories directly, services
can't depend on the API layer, and every mutating endpoint must declare
`@PreAuthorize`.

### Dependency graph

```
        ┌───────────────┐
        │  presentation │  REST controllers, security/websocket/cache
        │               │  config, application bootstrap — the only
        │               │  module producing a runnable jar
        └───────┬───────┘
                │ depends on
        ┌───────▼───────┐
        │    business   │  services, MapStruct mappers, DTOs
        └───────┬───────┘
                │ depends on
        ┌───────▼───────┐
        │  persistence  │  JPA entities, repositories, Flyway migrations
        └───────┬───────┘
                │ depends on
        ┌───────▼───────┐
        │     common    │  error handling, rate limiting, audit events,
        │               │  export, correlation IDs — no dependency on
        │               │  any other module in this project
        └───────────────┘

        `test` depends on all four, exercising the full stack.
```

Dependencies only point downward. `ArchitectureTest` fails the build if that
ever gets violated (e.g. a repository importing a service, or `common`
depending on `persistence`).

---

## Tech stack

- Java 21, Spring Boot 4
- PostgreSQL + Flyway
- Spring Security (OAuth2 resource server / JWT, validated against Keycloak)
- MapStruct, Lombok
- Springdoc OpenAPI (Swagger UI)
- WebSocket (STOMP over SockJS) for live notifications
- ArchUnit, JUnit 5, Testcontainers

---

## Getting started

### Prerequisites

- Java 21
- Docker (for Postgres + Keycloak)

### Run locally

```bash
docker compose -f compose.yaml up -d      # starts Postgres + Keycloak
make run                                   # or: ./mvnw spring-boot:run -pl presentation -am
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Build

```bash
make build      # package every module, skip tests
make test       # run all tests across every module
make verify     # full build + tests + architecture checks
```

See the `Makefile` for the full list of targets.

### Docker

```bash
docker build -t admin-api:latest .
docker compose -f compose.smoketest.yaml up -d
```

The `Dockerfile` builds the full multi-module reactor and packages only
`presentation`'s jar (which bundles its module dependencies) into the
runtime image.
