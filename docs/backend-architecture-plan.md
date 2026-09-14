# Backend architecture plan: from layered CRUD to a modular monolith

Status: validated (2026-09-13). ADRs 0004 to 0008 written. Phase 0 in progress.
Scope: `backend` (admin-api). The frontend is touched only where the API contract changes.
Companion docs: [architecture.md](architecture.md), [ADR 0001](adr/0001-keycloak-owns-identity.md),
[ADR 0002](adr/0002-layered-maven-modules.md), [ADR 0003](adr/0003-single-audit-path.md).

---

## 1. Decision summary

| # | Question | Decision |
|---|---|---|
| D1 | Module structure | Modular monolith on **Spring Modulith**, one package per business domain, `internal` sealed. Two Maven modules: `platform` (framework-agnostic library) and `app` (the deployable). The `persistence`, `business`, `presentation` and `test` modules are folded into `app`. Supersedes ADR 0002. |
| D2 | Controllers | **By use case, not by table.** One use-case class per business operation (verb-named, one `execute`), thin controllers grouped by domain. Generic CRUD survives only for reference data without rules. |
| D3 | Migration style | **Incremental, one domain per pull request**, green build between each. Maven collapse first (no package moves), then `git mv` domain by domain. |
| D4 | Response contract | Keep the success envelope in `/api/v1`. **Errors move to RFC 9457 `application/problem+json` now**, everywhere, including security entry points. Unwrapping success responses is deferred to a `/api/v2` if ever needed. |
| D5 | Authorization model | **Enforce the DB permission model.** Keycloak roles stay coarse (`ADMIN`, `USER`); fine-grained permissions are loaded into the principal at authentication and checked with `hasAuthority('<resource>:<action>')`. The permission, role-permission and menu-permission tables become real. |
| D6 | Audit | **Written inside the business transaction** by a synchronous domain-event listener. No `@Async`, no `REQUIRES_NEW`. Amends ADR 0003. |
| D7 | Distributed state | Redis-backed Spring Cache with event-driven invalidation on every replica; **ShedLock** on all scheduled jobs. |
| D8 | Identity per request | A `CurrentUser` principal built **once** in the authentication converter (JIT provisioning included). `UserProvisioningFilter` and `CurrentUserResolver` are removed. |
| D9 | Guarantees | Every architectural rule is a **build-breaking test that runs in CI**. CI is restored before any refactor starts. |

Everything already good is kept: Keycloak-only identity linked by subject, Flyway-owned schema with
`ddl-auto: validate`, optimistic locking with 409, partitioned audit table, Redis-backed rate limiting and
idempotency, RabbitMQ STOMP relay, OTel tracing, virtual threads, graceful shutdown, the ArchUnit rule that
every mutating endpoint declares `@PreAuthorize`.

---

## 2. Before: current architecture

```
backend/            Maven reactor, 5 modules split by TECHNICAL LAYER
├── common/               ApiResponse envelope, GlobalExceptionHandler, rate limit, idempotency,
│                         AuditAspect, CSV/PDF export, correlation id (auto-configured "platform")
├── persistence/          33 JPA entities, 33 Spring Data repositories, Flyway V1..V16
├── business/             33 CRUD services, 33 MapStruct mappers, ~55 DTOs,
│                         4 real use cases (usecase/rbac), 2 @Scheduled jobs, audit listener
├── presentation/         35 controllers, SecurityConfig, ApiKey + UserProvisioning filters,
│                         WebSocket/STOMP, OpenAPI, the only runnable jar
└── test/                 22 test classes: service unit tests, 3 Testcontainers ITs, ArchUnit

presentation -> business -> persistence -> common       (downward only, Maven + ArchUnit)

Request: filters (correlation, api-key | bearer JWT, JIT provisioning, rate limit, idempotency)
       -> @PreAuthorize on controller -> XxxService (@Transactional, @Audited) -> XxxRepository -> Postgres
       -> after commit: AuditEvent -> @Async listener -> audit_logs ; STOMP push to user
```

### Problems (each verified in code)

| # | Problem | Evidence |
|---|---|---|
| P1 | Layers instead of domains | A feature spans 6 packages in 4 modules. `business/service` is a flat bag of 38 classes; `RevokeUserAccess` injects 4 repositories directly. The module split guards layering (already habit) and nothing about domain coupling. |
| P2 | Table-driven API, anemic model | One controller/service/mapper/DTO per table, join tables included (`/user-roles`, `/role-permissions`). Zero enums in `domain/`; `User.status` is a `String` compared to literals in 9 places. Rules live in services. |
| P3 | `common` is heavy and leaks | Declared domain-agnostic but depends on spring-web, security, oauth2, redis, springdoc, openpdf, logstash. `persistence` transitively depends on the web stack. `business` imports `JwtAuthenticationToken` and `SimpMessagingTemplate`. |
| P4 | Multi-replica bugs (scale profile) | Caffeine local caches with `@CacheEvict(allEntries)` evict only on the replica that handled the write: stale effective permissions up to 5 min after a revoke. `@Scheduled` jobs run on every replica unlocked. Audit row written `@Async` after commit: lost on crash. |
| P5 | Three error shapes | Envelope from `GlobalExceptionHandler`; `application/problem+json` from `ApiKeyAuthenticationFilter`; empty body from the bearer entry point. |
| P6 | Identity resolved repeatedly | `UserProvisioningFilter` resolves subject -> user (cached), then `CurrentUserResolver` queries again in every ownership check, audit listener, session check and `/me`. `Authentication` carries only the JWT. |
| P7 | Two authorization models, one enforced | `hasRole('ADMIN')` from the Keycloak `roles` claim gates everything. Permission tables and `user_permissions_mv` are only read by `/me/permissions` for UI hints; the MV is refreshed every 5 min and read by nobody. |
| P8 | Thin, slow, unenforced tests | ~22 test classes vs ~340 production classes. No `@WebMvcTest` slices. Each IT starts its own Postgres. CI workflow removed in the last commit, so ArchUnit only runs on `make verify` locally. |
| P9 | Residue contradicting ADR 0001 | `password` on `CreateUserRequest`, BCrypt in `UserService`, lockout columns on `User`. `LoginHistoryController` accepts client POSTs. `@EnableScheduling` declared twice. `spring.profiles.active: dev` in base YAML. |

Scorecard today: **6.5 / 10 (13 / 20)**. Strong on identity, schema, observability and docs; weak on
structure, domain model, API shape, tests and replica correctness.

---

## 3. After: target architecture

```
admin-api/                        one Spring Boot 4 application, two Maven modules: platform + app
com.fourati.admin
├── platform/        ProblemDetail errors + codes, correlation, rate limit, idempotency, export, event base
│                    depends on Spring Core, validation, Jackson only (web adapters live in app/)
├── identity/        users, identity links + JIT provisioning, CurrentUser principal, api keys,
│                    sessions, login history, auth logs
├── access/          roles, permissions, grants, effective permissions, menus + menu permissions
├── organization/    organizations, members, departments, teams
├── configuration/   settings, feature flags, app modules, metadata kv
├── notification/    notifications, templates, user notifications, push port
├── audit/           audit logs, system events, error logs, partition + retention job, exports
├── content/         tags, entity tags, comments, attachments
└── app/             bootstrap, SecurityConfig, filters, exception handler, OpenAPI, actuator,
                     WebSocket transport, ShedLock, cache config

inside every domain module
<module>/                 PUBLIC: <Module>Facade, DTO records, published events   <- only thing others may import
<module>/internal/
    domain/               aggregates with behaviour, enums, value objects, domain events
    application/          use cases: one class per command or query, owns tx + authorization + @Audited
    persistence/          JPA entities, repositories, read-model queries (projections / JdbcClient)
    web/                  this module's controllers (thin adapters)
```

```
Request path after
JWT | X-Api-Key
  -> AppAuthenticationConverter builds CurrentUser{userId, subject, username, roles, permissions} ONCE
     (JIT provisioning + Redis-cached permission set, invalidated by AccessChanged events)
  -> controller binds request, calls ONE use case
  -> use case: @PreAuthorize("hasAuthority('users:suspend')") + @Transactional + @Audited
  -> aggregate method (User.suspend(), RoleGrant.of(...)) -> repository
  -> same transaction: AuditEntry insert via synchronous @EventListener
  -> after commit: integration events via Modulith event publication registry (durable, replayed on restart)
       -> notification push, cross-replica cache invalidation
  -> 2xx: ApiResponse envelope (v1)  |  4xx/5xx: application/problem+json {type,title,status,detail,
     instance, code, correlationId, violations[]}
```

### Use-case shape (reference)

```java
@Service @Transactional @RequiredArgsConstructor
public class SuspendUser {
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    @PreAuthorize("hasAuthority('users:suspend')")
    @Audited(action = "SUSPEND_USER", entityType = "user")
    public UserResponse execute(UUID userId, SuspendUserCommand cmd, CurrentUser actor) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.suspend(cmd.reason(), actor.userId());          // invariant enforced by the aggregate
        events.publishEvent(new UserSuspended(userId, actor.userId()));
        return UserResponse.from(user);
    }
}

@RestController @RequestMapping(ApiConstants.VERSION + "/users") @RequiredArgsConstructor
class UserLifecycleController {
    private final SuspendUser suspendUser;

    @PostMapping("/{id}:suspend")
    @PreAuthorize("hasAuthority('users:suspend')")
    ResponseEntity<ApiResponse<UserResponse>> suspend(@PathVariable UUID id,
            @Valid @RequestBody SuspendUserCommand cmd, @AuthenticationPrincipal CurrentUser actor) {
        return ApiResponse.ok(suspendUser.execute(id, cmd, actor), "User suspended");
    }
}
```

### Rules encoded as build-breaking tests

1. `ApplicationModules.of(AdminApiApplication.class).verify()`: no cross-module `internal` import, no cycles.
2. Nothing outside `platform` imports `jakarta.servlet` or `org.springframework.security` except `app/..` and `..internal.web..`.
3. Classes in `..internal.web..` depend on no `*Repository` and no `*Mapper`.
4. Use-case classes in `..internal.application..` expose exactly one public method.
5. Repositories are referenced only from their own module.
6. Every mutating endpoint declares `@PreAuthorize` (existing rule, kept).

---

## 4. What changed and why

| # | Change | Why | Trade-off |
|---|---|---|---|
| C1 | Layer modules -> domain modules on Spring Modulith | Boundaries follow the business; a feature lives in one folder; coupling checked on the axis that matters | Loses Maven's compile error on upward dependency; replaced by a verify test that needs CI |
| C2 | Use-case controllers | One operation = one class = one endpoint; authorization and audit travel with the operation | More classes than a generic CRUD service; deliberate |
| C3 | Enums + behaviour on aggregates | Invariants in one place; CHECK constraints in DB | Mapper churn; one migration per enum |
| C4 | Slim `platform` | Persistence and application code no longer depend on web/security/PDF | Filters and exception handler move to `app` |
| C5 | `CurrentUser` principal built once | One identity resolution per request; ownership checks need no DB | Custom `Authentication` type |
| C6 | Redis cache + event invalidation, ShedLock | Fixes stale permissions and duplicate job runs across replicas | One network hop per cached read; two-level cache if measured |
| C7 | Audit in-transaction | Atomic with the write; no lost rows; no second connection | `audit_logs_default` must exist so an insert never fails the write (it does) |
| C8 | RFC 9457 errors everywhere | One contract, framework-native | Breaking for clients; frontend keys errors on status and unwraps envelopes in one file, so contained |
| C9 | Task-based API cleanup | Log tables read-only, join tables via sub-resources, dead password path removed | Deprecated v1 endpoints carry `Deprecation`/`Sunset` headers for one release |
| C10 | Permissions enforced | The permission model stops being decorative | Changes propagate on cache invalidation, not instantly |
| C11 | Test pyramid rebuilt | Slices per controller, module tests, shared containers, OpenAPI diff | Investment before refactoring pays off |

---

## 5. Phases

Each phase is independently shippable. Phases 0 to 2 are worth doing even if the reorganisation is rejected.

### Phase 0: guardrails (prerequisite)
- [x] Pre-push hook running backend `verify` (`scripts/git-hooks/pre-push`, installed with `make hooks`). GitHub Actions stays off by choice; re-enable later with the same `verify` as a required check.
- [x] `AbstractIntegrationTest`: one singleton Postgres container via `@ServiceConnection`, one shared context, MockMvc with the real filter chain. The three existing ITs extend it.
- [x] `ErrorContractTest`: `@WebMvcTest` slice pinning status, media type and body shape for every error family (no Docker).
- [x] `SecurityContractIntegrationTest`: 401, 403, invalid API key, CORS preflight, actuator split, through the real chain.
- [x] `OpenApiContractIntegrationTest`: `/v3/api-docs` compared with the committed snapshot `test/src/test/resources/contract/openapi.json`; refresh with `make openapi-update`.
- [ ] Redis and RabbitMQ containers join the base when Phase 2 needs them.
- Done when: `make verify` green locally with the hook installed, ITs share one container, error and security contracts covered.
- Outcome (2026-09-13): 123 tests green in ~23 s with one Postgres container. Two fixes fell out of the new guardrails:
  - Testcontainers moved from the pinned 1.20.4 to the Boot-managed 2.0.5; 1.x cannot talk to Docker Engine 29 (API 1.40 minimum), so every IT had been failing on current Docker.
  - A `@PreAuthorize` denial was returned as **500** instead of 403: `AuthorizationDeniedException` fell into the catch-all handler. `GlobalExceptionHandler` now maps `AccessDeniedException` to 403 `FORBIDDEN` and `AuthenticationException` to 401 `UNAUTHORIZED`.

### Phase 1: errors and identity
- [x] RFC 9457 `ProblemDetail` for every error path through one factory (`platform.error.Problems`): `GlobalExceptionHandler`, `ProblemAuthenticationEntryPoint` (401 with `WWW-Authenticate: Bearer`), `ProblemAccessDeniedHandler` (403), `ApiKeyAuthenticationFilter`, `RateLimitFilter` (429), `IdempotencyFilter` (409). Extension members: `code`, `retryable`, `timestamp`, `correlationId`, `violations`.
- [x] `ErrorCode` now carries status, title, description and retryability; `ErrorCatalogController` is generated from it; `BusinessRuleException` takes its status from the catalog. `ApiResponse` lost its error branch and is the success envelope only.
- [x] `CurrentUser` principal (`platform.security`) built once by `AppJwtAuthenticationConverter` (JIT provisioning inside, degrades to a principal without id when provisioning fails) and by the API-key filter (`apiKey = true`, refused by ownership checks). `UserProvisioningFilter` and `CurrentUserResolver` deleted; ownership, session, audit and `/me` read the principal with no database call. `AuditEvent` carries `actorUserId`.
- [x] Frontend: `core/api/envelope.ts` exposes `ProblemDetail`, `isProblemDetail`, `problemOf`; MSW `problem()` helper mirrors the backend body. Status-based retry logic unchanged.
- [ ] Regenerate `frontend/src/shared/types/api.generated.ts` from a running backend (`npm run generate-types`), which needs the API up.
- Done when: one error shape in all tests (ErrorContractTest, SecurityContractIntegrationTest green); ownership checks make no database call (OwnershipSecurityTest verifies no repository interaction for the principal itself).
- Outcome (2026-09-13): backend 130 tests green, frontend lint and 79 tests green, OpenAPI snapshot refreshed with the problem-details schemas.

### Phase 2: multi-replica correctness
- [x] ShedLock (JDBC, `shedlock` table in V17) on `AuditLogPartitionJob` (one lock name for the nightly and the start-up pass; `PermissionsViewRefreshJob` was also locked until Phase 5 retired it); `SchedulingConfig` uses database time.
- [x] `CacheConfig` follows `fourati.common.store`: Caffeine for `memory`, `RedisCacheManager` for `redis` (JDK serialization, 5 min TTL, `admin-api:cache:` prefix). Services publish `AccessChanged` / `IdentityChanged`; `CacheInvalidationListener` clears after commit through a `CacheInvalidator` port whose Redis adapter deletes keys synchronously (Spring Data Redis 4 `RedisCache.clear()` completes asynchronously). The scattered `@CacheEvict` annotations are gone.
- [x] Audit row written in the business transaction: `AuditLogEventListener` is a BEFORE_COMMIT listener, `AuditConfig` pins the transaction advisor outside the audit aspect, no `@Async`, no `REQUIRES_NEW`. ADR 0003 amended.
- [x] Proof: `AuditTransactionIntegrationTest` (row visible on return, rollback leaves nothing, actor is the principal) and `DistributedStateIntegrationTest` (stores and cache are Redis-backed, invalidation is absent before commit and present in the shared store after, a second runner on another thread cannot take a held lock). The integration base now starts Redis alongside Postgres with `fourati.common.store=redis`.
- Outcome (2026-09-13): 137 backend tests green in ~42 s. OpenAPI snapshot unchanged, as expected for a phase with no contract change.

### Phase 3: Modulith reorganisation (no behaviour change)
- [x] Maven reactor collapsed to `platform` (library) + `app` (deployable); Dockerfile, Makefile and docs follow. Slimming `platform`'s web and security dependencies is deferred to Phase 6 (its filters, handler and principal still need them).
- [x] Spring Modulith 2.0.8 with `ModularityTest` (`ApplicationModules.verify()` + generated module docs in `app/target/spring-modulith-docs`). The legacy-package ignore list started full and is now empty.
- [x] Every class moved by domain, four batches with a green build between each: `content`; `configuration` + `organization`; `notification` + `audit`; `access` + `identity` + `app`. A `shared` open kernel holds the base entities, `VersionCheck`, `TextSearch` and `ApiConstants`; `ErrorCode` and `BusinessRuleException` moved into `platform.error`.
- [x] Cross-module JPA relations replaced by id columns (`Setting`/`FeatureFlag` → organization, `OrganizationMember`/`UserNotification`/`AuditLog`/`UserRole`/`Comment`/`Attachment` → user). Cross-module calls go through facades: `IdentityFacade` (provision, API-key auth, exists, summary, session ownership, actor resolution, revoke credentials), `AccessFacade`, `OrganizationFacade`, `ContentFacade`, `NotificationFacade`. Events `AccessChanged` and `IdentityChanged` are public types of their modules; the cache listener lives in the app shell.
- [x] Ownership beans (`OwnershipSecurity`, `SessionSecurity`) and the security adapters live in `app.security`; `/me/permissions` moved to the access module so identity does not depend on access. The three cycles the verification found (identity ↔ access, identity ↔ content, identity ↔ notification) were all this.
- [x] Tests moved next to the code they cover; `ArchitectureTest` rewritten to the module layout (thin controllers, framework-free domain and persistence, platform independent of the app, repositories private to their module, `@PreAuthorize` on every mutating endpoint).
- [ ] `@ApplicationModuleTest` slices per module and the Modulith event publication registry for durable cross-module events (ADR 0007) remain for Phase 4.
- Outcome (2026-09-13): 138 backend tests green on a clean build, Modulith verification green across 10 modules, OpenAPI snapshot unchanged.

### Phase 4: domain and API hardening
- [x] `UserStatus` and `OrganizationStatus` enums (lowercase on the wire and in the database through JPA converters); V18 tightens the users check constraint. Behaviour on the aggregates: `User.suspend/activate/deactivate/changeStatus/softDelete`, `ApiKey.revoke/isLive`, `Session.revoke/isLive`, `UserNotification.markRead`. ArchUnit now refuses a `String status` field in any domain package.
- [x] Identity users are use cases: `CreateUser`, `UpdateUser`, `DeleteUser`, `SuspendUser`, `ActivateUser`, `FindUser`, `SearchUsers` replace `UserService`; `UserController` is a thin adapter with two new operations, `POST /users/{id}:suspend` and `:activate`. ArchUnit enforces one public method per use case (a `@Service` in `internal.application` not named `*Service`).
- [x] Access writes are use cases only: `RemoveRoleFromUser` and `RevokePermissionFromRole` join the existing four; `DELETE /users/{userId}/roles/{roleId}` and `DELETE /roles/{roleId}/permissions/{permissionId}` are the write path. The join-table writes (`POST`/`DELETE` on `/user-roles` and `/role-permissions`) delegate to the use cases and carry RFC 9745 `Deprecation`, RFC 8594 `Sunset` (2027-03-31) and `Link rel="successor-version"` headers through `@SunsetEndpoint`; `DeprecatedEndpointsIntegrationTest` proves it.
- [x] Log tables read-only from the outside: login history lost its `POST` and `PATCH` (audit, auth, error and system logs already had none).
- [x] ADR 0001 closed: `password`, `failedLoginAttempts`, `lockedUntil`, `passwordChangedAt` gone from the entity, the DTOs and the frontend; V18 drops the columns and the V13 PL/pgSQL helpers that used them; the BCrypt bean is gone.
- [ ] Use cases for the remaining domains (their generic `*Service` classes still serve CRUD), read models through projections or `JdbcClient`, and entity graphs remain for a later pass.
- Outcome (2026-09-13): 137 backend tests green, 10 architecture rules green, Modulith green, frontend lint green and tests green after the schema test update. OpenAPI snapshot refreshed: two new user operations, two new access operations, four deprecated operations, two removed login-history operations, `status` is an enum, `UserResponse` lost four fields.

### Phase 5: authorization unification
- [x] Permission set resolved at authentication: `AppJwtAuthenticationConverter` asks `AccessFacade.effectivePermissionCodes(userId, jwtRoles)`, backed by the `UserPermissionsQuery` use case (union of the unexpired `user_roles` grants and the token roles, resolved through `role_permissions`). The codes ride on `CurrentUser.permissions()` and are granted as authorities, so `hasAuthority('user.write')` is the fine-grained check. Cached per `(userId, roles)` in the shared store (`userPermissions`), cleared by `CacheInvalidationListener` on every `AccessChanged`, so a revoked grant is denied on the next request on every replica. API keys keep `SCOPE_` authorities only.
- [x] Guards named in one place, `shared.security.Authorize`: `ADMIN` stays the coarse short-circuit and every constant reads `hasRole('ADMIN') or hasAuthority('<resource>.<action>')` (the seeded codes use a dot, so the ADR 0006 colon form was not introduced). Mapping: `CreateUser` → `user.write`; `UpdateUser`, `SuspendUser`, `ActivateUser` → `user.edit`; `DeleteUser`, `RevokeUserAccess` → `user.delete`; `AssignRoleToUser`, `RemoveRoleFromUser`, `GrantPermissionsToRole`, `RevokePermissionFromRole` → `role.edit`. The same expressions sit on the corresponding endpoints (`UserController`, `RbacController` per method, the deprecated join-table writes); `GET /me/permissions` reports the same set the guards use.
- [x] `user_permissions_mv`, `refresh_user_permissions_mv()`, `UserPermissionsView(Id)`, its repository and `PermissionsViewRefreshJob` are gone (V19). ADR 0006 marked implemented.
- [x] ArchUnit rule 11, `mutating_use_cases_declare_the_permission_they_need`: a `` in `internal.application` that is not a `*Service`, `*Query`, `Find*` or `Search*` must carry `` on its public method.
- [x] Proof: `PermissionAuthorizationIntegrationTest` provisions a person whose token carries only the `user` role, grants them the seeded `manager` role in the database through `AssignRoleToUser`, and through the real converter they can `POST /users` (201) but not `DELETE /users/{id}` (403, managers lack `user.delete`); after `RemoveRoleFromUser` the next authentication has lost `user.write` and `POST /users` is 403. `AppJwtAuthenticationConverterTest` covers the permission authorities and the degrade paths (provisioning down, permission lookup down). Integration tests that call guarded use cases directly now run under an admin security context.
- Done when: every mutating use case names a permission; ArchUnit checks it.
- Outcome (2026-09-13): 141 backend tests green on a clean build, 11 architecture rules green, Modulith green, OpenAPI snapshot unchanged (the contract did not move; only who may call it did). Not done here: `Authorize` covers users and roles only, the other domains still use `hasRole('ADMIN')` on their controllers and will pick up their codes when they become use cases.

### Phase 6: ops polish
- [x] Image (`Dockerfile`): built on JDK 25, run on JRE 25 (`eclipse-temurin:25`), non-root. The fat jar is exploded into Docker layers with `jarmode=tools extract --layers`, so a code change re-ships under 1 MB. A CDS archive is trained during the build (`application-cds.yml`: the whole context, no infrastructure, `spring.context.exit=onRefresh`) and loaded by the entrypoint. OCI labels from the `GIT_SHA`, `BUILD_DATE` and `VERSION` build args; `make image`. The collector moved from ZGC to G1: CDS maps archived heap objects only with compressed oops, which ZGC disables. Measured on the image, G1 starts warm in about 3.9 s against 6.1 s cold; ZGC manages 4.9 s and logs an error per start. One flag (and the training run) reverts it.
- [x] SBOM: `cyclonedx-maven-plugin` (configured by the Boot parent) writes `META-INF/sbom/application.cdx.json` into the jar on every build. The actuator serves it at `/actuator/sbom` (anonymous only on the management port, like `/actuator/prometheus`); the image carries a copy at `/app/sbom/admin-api.cdx.json` (label `io.fourati.sbom`); `make sbom`; `SbomContractIntegrationTest`.
- [x] Configuration properties as records: `CommonProperties` joins `AppProperties` (constructor binding, `@DefaultValue`, validated, immutable).
- [x] `platform` POM slimmed: a library declares the APIs it compiles against (`spring-webmvc`, `spring-security-core`, `spring-data-redis`, `jakarta.validation-api`, `swagger-annotations-jakarta`, Jackson 3, `spring-boot-autoconfigure`) and no Boot starter; the runtime starters (validation, data-redis) and the logging encoder moved to `app`. The platform is Jackson 3 only now (`JsonUtils`, the Redis idempotency store and the JSON export use the application's `JsonMapper`). The audit aspect lost its raw-JWT fallback, `CurrentUser` being the only principal the converters produce, which took the OAuth2 modules out of the library.
- [x] Dependency updates: `.github/dependabot.yml` (Maven, npm, Docker, compose, Actions; weekly; minor and patch grouped, majors alone). Dependabot opens pull requests without spending Actions minutes; with CI off, `make check` on the branch is the gate before merging.
- [~] Java 25: the image builds and runs on 25 (the CDS training run boots the whole context on it). The source level stays 21 in `pom.xml` because the developer machine and the pre-push hook run JDK 21; bump `java.version` once JDK 25 is installed locally.
- Done when: the image builds and starts from its CDS archive, the SBOM is served, Dependabot is configured.
- Outcome (2026-09-13): 143 backend tests green (two new for the SBOM), image built and warm-started on Java 25 with `-Xshare:on`. Image layers total 619 MB, of which the CDS archive is 132 MB and the dependencies 113 MB.

---

## 6. ADRs to write once validated

| ADR | Title | Supersedes / amends |
|---|---|---|
| 0004 | Modular monolith on Spring Modulith, use-case application layer | supersedes 0002 |
| 0005 | RFC 9457 problem details for all errors; success envelope kept in v1 | new |
| 0006 | DB permission model enforced through principal authorities | new |
| 0007 | Audit written in the business transaction; durable integration events via Modulith registry | amends 0003 |
| 0008 | Distributed cache and scheduler locking for multi-replica deployments | new |

---

## 7. Target scorecard

| Area | Today | After phases 0-2 | After all phases |
|---|---|---|---|
| Documentation and ADRs | 9 | 9 | 10 |
| Security and identity | 8 | 9 | 10 |
| Schema and data | 8 | 8 | 10 |
| Observability and ops | 8 | 8 | 10 |
| Cross-cutting platform | 6 | 7 | 10 |
| Module structure | 5 | 5 | 10 |
| API design | 5 | 5 | 10 |
| Error contract | 5 | 10 | 10 |
| Tests | 5 | 8 | 10 |
| Multi-replica correctness | 4 | 10 | 10 |
| Domain model | 4 | 4 | 10 |
| **Overall /20** | **13** | **~16** | **20** |

---

## 8. Open points for the review meeting

1. Confirm the domain list in section 3 (in particular: menus under `access` or `configuration`; notification templates under `notification`).
2. Confirm D5 (enforce DB permissions) versus deleting the permission tables. The plan assumes enforce.
3. Confirm the deprecation window for retired v1 endpoints (proposal: one release).
4. Agree who owns the frontend change for the error contract (Phase 1) and the endpoint moves (Phase 4).
