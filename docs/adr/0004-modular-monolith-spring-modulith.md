# ADR 0004: Modular monolith on Spring Modulith with a use-case application layer

Status: accepted (2026-09-13). Supersedes ADR 0002.

## Context

ADR 0002 split the backend into Maven modules by technical layer
(`presentation -> business -> persistence -> common`). The split is clean and
the downward rule is respected, but it guards the wrong axis:

- A feature is cut into six packages across four modules. Every change to
  one domain touches every module.
- Inside `business`, 38 services sit in one flat package with no rule about
  who may depend on whom. `RevokeUserAccess` reaches into four repositories
  of three different domains. The module boundary is blind to this.
- `common` is declared domain-agnostic but depends on Spring Web, Security,
  Redis, springdoc and OpenPDF, so even `persistence` transitively depends on
  the web stack. `business` imports Spring Security token types and the STOMP
  messaging template.
- A separate `test` module keeps tests away from the code they cover and
  forces the full classpath onto every unit test.

The API is organised by table: one controller, service, mapper and DTO pair
per table, join tables included. Only four operations (`usecase/rbac`) are
expressed as business use cases.

## Decision

1. The backend becomes a **modular monolith on Spring Modulith**. Business
   domains are top-level packages under `com.fourati.admin`:
   `identity`, `access`, `organization`, `configuration`, `notification`,
   `audit`, `content`, plus `platform` (cross-cutting, framework-agnostic) and
   `app` (bootstrap, security, web adapters).
2. Each domain module exposes a **public API** at its root package (a facade
   interface, DTO records, published events) and keeps everything else under
   `internal/` with the sub-packages `domain`, `application`, `persistence`
   and `web`. Other modules may only import the root package or consume
   events.
3. The **application layer is organised by use case**: one class per business
   operation, named as a verb (`SuspendUser`, `AssignRoleToUser`), exposing a
   single `execute` method that owns the transaction, the authorization rule
   and the `@Audited` annotation. Controllers are thin adapters grouped by
   domain and never touch repositories or mappers. Generic CRUD survives only
   for rule-free reference data.
4. The Maven reactor collapses to **two modules**: `platform` (library, no web
   or security dependency) and `app` (the deployable). Tests live in the
   module they cover.
5. Boundaries are enforced by build-breaking tests:
   - `ApplicationModules.of(AdminApiApplication.class).verify()`: no
     cross-module `internal` import, no cycles.
   - Nothing outside `platform` imports `jakarta.servlet` or
     `org.springframework.security` except `app/..` and `..internal.web..`.
   - Classes in `..internal.web..` depend on no repository and no mapper.
   - Use-case classes in `..internal.application..` expose one public method.
   - Repositories are referenced only from their own module.
   - Every mutating endpoint declares `@PreAuthorize` (kept from ADR 0002).
6. Migration is incremental: Maven collapse first with no package moves, then
   one domain per pull request in the order `content`, `configuration`,
   `organization`, `notification`, `audit`, `access`, `identity`, with a green
   build between each.

## Consequences

- Boundaries follow the business. A feature lives in one folder and the
  verify test fails on the coupling that actually hurts.
- The compile-time barrier of ADR 0002 is replaced by a test. This is only a
  guarantee when the test runs on every push, so the pre-push hook and CI
  from ADR 0008 are a prerequisite, not an option.
- Any domain package can later be promoted to its own Maven module, or
  extracted to a service, without renaming a package.
- More classes than a generic CRUD service. This is deliberate: an operation
  with no rules is cheap to write and an operation with rules finally has a
  home.
- Rejected alternative: keep the layer modules and add package rules inside
  `business`. Cheaper, but the feature-in-four-places cost stays forever and
  the platform leak is not fixed.
- Rejected alternative: microservices. The domains share one database and one
  identity provider and are operated by one team; the network boundary would
  add cost without adding isolation the modulith does not already provide.
