# ADR 0002: Physical layering as Maven modules, enforced by ArchUnit

Status: superseded by [ADR 0004](0004-modular-monolith-spring-modulith.md) (2026-09-13). Originally accepted 2026-09-13 to document an existing decision.

## Context

The backend is one deployable but has clear layers: HTTP, application
services, persistence, and cross-cutting platform code. Package conventions
alone erode: a controller that reaches into a repository compiles fine.

## Decision

Each layer is a Maven module with an explicit, downward-only dependency graph:

```
presentation -> business -> persistence -> common
```

- `common` has no dependency on any other module and is domain-agnostic
  (error envelope, rate limiting, idempotency, audit aspect, export, correlation
  ids). It is wired through Spring Boot auto-configuration so a host application
  can override any bean.
- `persistence` owns entities, repositories and Flyway migrations; the schema is
  owned by Flyway (`ddl-auto: validate`), never by Hibernate.
- `business` owns services, mappers, DTOs and use cases. Generic per-table CRUD
  services remain for reference data; operations with rules live under
  `usecase/` (for example `usecase/rbac`).
- `presentation` owns controllers, security and application bootstrap and is
  the only module producing a runnable jar.
- `test` depends on all four and holds unit, integration and architecture tests.

`ArchitectureTest` (ArchUnit) fails the build on any upward dependency and on
any mutating endpoint without `@PreAuthorize`.

## Consequences

- A layering mistake is a compile or build failure, not a code-review catch.
- The cost is one extra `pom.xml` per layer and slightly slower incremental
  builds; both are acceptable for the guarantee.
- Rejected alternative: a single module with package rules. Cheaper, but the
  guarantee would rest on ArchUnit alone rather than on the compiler.
