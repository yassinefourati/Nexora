# Contributing

## Before you start

1. Read [`README.md`](README.md) for the full-stack layout and how to run it locally.
2. Read [`backend/README.md`](backend/README.md) for the backend layout: a `platform` library and one `app` module whose business domains are Spring Modulith modules. `ModularityTest` fails the build on a cross-module `internal` import or a cycle; if your change needs one, the design needs rethinking, not a suppressed test.

## Making a change

### Backend (`backend/`)

```bash
cd backend
make hooks                          # once per clone: pre-push hook that runs `make verify` (ADR 0008)
./mvnw compile -DskipTests          # fast sanity check while iterating
make test-unit                      # unit + architecture + contract slices (no Docker)
make test-integration               # Testcontainers suites (Docker)
make verify                         # everything the pre-push hook runs
make openapi-update                 # refresh the committed OpenAPI snapshot after an intentional API change
make check                          # backend verify + frontend lint/test/build
```

- New code goes in the domain module it belongs to (see `backend/README.md`
  and the plan). If you are not sure which domain, ask —
  do not guess and let the Modulith verification catch it after the fact.
- Every mutating endpoint (`POST`/`PUT`/`PATCH`/`DELETE`) must declare
  `@PreAuthorize`, at the method or class level — enforced by
  `ArchitectureTest.mutating_controller_endpoints_must_declare_preAuthorize()`.
- Integration tests extend `AbstractIntegrationTest`
  (`app/src/test/java/com/fourati/integration/`): one Postgres container and
  one application context shared by every suite, `MockMvc` with the real
  security filter chain. Do not declare your own container or
  `@DynamicPropertySource`.
- The error contract is pinned by `ErrorContractTest` (a web slice, no Docker)
  and the security entry points by `SecurityContractIntegrationTest`. A new
  error family gets a case in the slice.
- The OpenAPI document is pinned by `OpenApiContractIntegrationTest` against
  `app/src/test/resources/contract/openapi.json`. An API change fails the
  test until you run `make openapi-update` and commit the snapshot with the
  change; the diff is part of the review.
- Testcontainers is the version managed by the Spring Boot parent (2.x). Do
  not pin 1.x: its Docker client is refused by Docker Engine 29+.
- Dependency bumps arrive as Dependabot pull requests (`.github/dependabot.yml`);
  no workflow checks them while CI is off, so pull the branch, run `make check`,
  then merge. The image runs on Java 25 (`make image`); the source level stays 21
  until JDK 25 is on every developer machine (`java.version` in `pom.xml`).
- If the IDE (VS Code Java, Eclipse) is open, it compiles into the same
  `target/` directories as Maven. Let it settle after a save before running
  Maven; a `Name for argument ... '-parameters' flag` error in a test run
  means IDE classes were picked up, not that your change is broken. Re-run
  with `./mvnw clean ...`.

### Frontend (`frontend/`)

```bash
cd frontend
npm ci --legacy-peer-deps
npm run lint          # includes the i18n and layering rules below
npm run test:run
npm run build
```

Conventions the linter enforces:

- **Every list screen is `CrudPage`** (`src/shared/components/crud/CrudPage.tsx`):
  pass `resource`, `useList`, `columns`, optionally `useRemove`, `renderDialog`,
  `permission`, `rowActions`. Search, sort, pagination, loading skeleton, error
  state, empty state, permission gating and delete confirmation come from the
  component; pages contain only columns and resource-specific behaviour.
- **Search is server-side.** `CrudPage` sends `?q=`; every backend list endpoint
  honours it (`TextSearch` over the entity's text columns). Never filter the
  loaded page in the browser.
- **No literal copy in UI code** (`i18next/no-literal-string` on `features/`,
  `layout/`, `shared/components/`). Add keys to `src/core/i18n/locales/en.ts`
  and `fr.ts`; `locales.test.ts` fails if the two trees differ or a `t()` key
  does not exist. Resource labels live under `resources.<name>`, field labels
  under `fields`, everything else under `ui`.
- **UI never calls the API client** (`no-restricted-imports` on pages,
  components, layout). Data access lives in `features/<x>/api` and
  `features/<x>/hooks`.
- **Developer reference pages** (`devdocs`, `uidocs`, `guide`) are routed only
  in dev builds and are excluded from the production bundle.

## Before opening a PR

- [ ] `make check` passes. The pre-push hook (`make hooks`) runs the backend
      `verify` before every push; `SKIP_VERIFY=1 git push` bypasses it once and
      should be explained in the PR.
- [ ] A new 4xx has an `ErrorCode`, a case in `ErrorContractTest` and, if the
      OpenAPI document changed, a refreshed snapshot; a new mutating endpoint has
      an `@PreAuthorize` and, if user-scoped, an `@ownership.*` check with a test
      in `OwnershipAuthorizationIntegrationTest`
- [ ] If you touched the module structure, package names, or renamed
      anything broadly, you did it in one deliberate pass — not iteratively.
- [ ] New backend behavior has a test at the appropriate level: unit test
      for a service method, integration test for a full request→DB flow,
      architecture test if you're introducing a new layering rule.

## Commit messages

Explain *why*, not just *what* — the diff already shows what changed.
