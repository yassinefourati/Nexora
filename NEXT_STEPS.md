# Next Steps / Improvement Roadmap

This file tracks the gap between where the project is now and where it should
be. Written after a live restructuring session (backend merged into
`project_admin/` as a layered Maven multi-module project) — see the honest
assessment below for why these items exist.

---

## Why this list exists

The current structure (`project_admin/common → persistence → business →
presentation`, enforced by `ArchitectureTest`) is architecturally sound — the
layering is real (Maven module boundaries, not just package convention) and
backed by build-time checks. But it was built through several live renames in
one sitting (`siainnovations→fourati`, `admin-backend→admin-api`, package
flatten, `vanilla_*→project_*→` bare names), which left rough edges. This
list is the cleanup pass.

---

## Done

- [x] **Module naming redundancy fixed.** `project_admin/project_common` →
      `project_admin/common` (and same for `persistence`, `business`,
      `presentation`, `test`). Parent POM, sibling dependencies, Dockerfile,
      Makefile all updated and verified compiling.
- [x] **CI restored.** `.github/workflows/ci.yml` — backend job runs the full
      Maven reactor + `ArchitectureTest` + Docker build; frontend job runs
      lint/test/build. Runs on push/PR to `main`.
- [x] **Compose files documented.** Root `README.md` now has a table
      explaining which of the three compose files (`docker-compose.yml`,
      `project_admin/compose.yaml`, `project_admin/compose.smoketest.yaml`)
      to use for what. All three are still needed — they cover genuinely
      different situations (full stack / backend-only dev / image smoke
      test) — so none were removed.
- [x] **Module dependency graph documented** in `project_admin/README.md`
      (diagram + explanation), not just implied by `ArchitectureTest`.
- [x] **Integration test coverage grown.** Added
      `UserCrudIntegrationTest` (create/update/delete/duplicate-conflict
      against a real Testcontainers Postgres) alongside the existing
      `ApiKeyAuditIntegrationTest`. Verified it compiles; could not verify it
      *passes* locally — see the Testcontainers item below, which is why.
- [x] **Stray files removed**: `vanilla-project-main.zip` (superseded
      scaffold archive, confirmed redundant before deletion) and the earlier
      dot-less `env`/`env.example` duplicates.
- [x] **CONTRIBUTING.md added** — now that CI exists, there's a checklist to
      point contributors at.

## Investigated, not resolved

### Testcontainers fails on this Windows/Docker Desktop setup
`ApiKeyAuditIntegrationTest` and the new `UserCrudIntegrationTest` cannot run
locally in this environment. Root-caused (not just "Docker isn't running" —
Docker itself works fine for normal builds):

- Docker Desktop's active context is `desktop-linux`
  (`npipe:////./pipe/dockerDesktopLinuxEngine`), while Testcontainers'
  default strategy tries the older `npipe:////./pipe/docker_engine` path.
- Explicitly setting `DOCKER_HOST` to the `desktop-linux` pipe did **not**
  fix it — the `docker-java` client still receives a `400 Bad Request` with
  an empty/stubbed JSON body from Docker Desktop's engine proxy, regardless
  of which pipe is targeted. This points to a `docker-java` client
  version / Docker Desktop API version mismatch, not a simple
  misconfiguration.
- **Not a code problem**: CI (GitHub Actions `ubuntu-latest`) has a real
  Docker daemon at `/var/run/docker.sock` with no proxy layer, so these
  tests are expected to run correctly there. This is specifically a local
  Windows dev-experience gap.

**Suggested next step** (untried, needs someone with admin access to Docker
Desktop settings to verify): enable Docker Desktop's *"Expose daemon on
tcp://localhost:2375 without TLS"* option, then set
`DOCKER_HOST=tcp://localhost:2375` when running Maven locally on Windows.
This bypasses the npipe proxy entirely. Alternatively, upgrade the
`testcontainers.version` property in the parent POM past `1.20.4` in case a
newer release has already fixed this against current Docker Desktop
releases.

---

## Needs a decision (not done unilaterally)

These two items involve real trade-offs and would mean *another* structural
rename on top of several that already happened this session — deliberately
not doing them without explicit sign-off, per the process note below.

### Reconsider the flattened package structure
Every class lives directly under `com.fourati.*` with layer-named
subpackages (`service`, `repository`, `domain`, `dto`, `mapper`, `api`,
`config`, `platform`). This means the Java package alone doesn't indicate
which Maven module (which JAR) a class ships in.

- **Option A**: leave as-is — it's a valid, common pattern for small teams,
  and the module↔package mapping is now documented in
  `project_admin/README.md`.
- **Option B**: reintroduce a module-qualifying segment (e.g.
  `com.fourati.persistence.domain.User`,
  `com.fourati.business.service.UserService`), so `import` statements alone
  tell you the module. Bigger mechanical change, touches all 334 files again.

### Rate-limit / query-batching trade-off
`RATE_LIMIT_RPM` in `.env` is `600` (raised from the production-accurate
`60`) purely to stop the frontend Dashboard's several simultaneous initial
queries from tripping 429s in local dev.

- **Option A**: leave the inflated local rate limit as a pragmatic dev-only
  setting (already documented in `.env.example`).
- **Option B**: fix it at the source — batch/dedupe the Dashboard's initial
  queries (a combined endpoint, or React Query `staleTime` tuning) so local
  dev doesn't need a non-production-accurate rate limit to function. This is
  a frontend data-fetching change, not a config tweak.

---

## Lower priority / not started

- [ ] Prometheus/Grafana: `micrometer-registry-prometheus` is wired into the
      backend but no compose service scrapes it. Either add one or drop the
      dependency if it's not being used.
- [ ] Playwright e2e smoke test wired into CI (login → dashboard → one CRUD
      flow) against the docker-compose full stack.

---

## Process note for next time

Several renames happened live in one session (branding, package structure,
module names) instead of being planned once upfront. Each was individually
reasonable, but stacked together they produced visible churn in the final
artifact, and one incident where `.git` itself disappeared mid-session
(cause unknown, recovered via a fresh baseline commit — see `git log`).

This round intentionally stopped short of two more renames (package
structure, rate-limit fix) that would have compounded that same pattern —
they're flagged above as decisions, not silently executed.

Next time a restructuring like this is needed: decide the target names
(package, groupId/artifactId, module names) **before** starting the move, not
iteratively — cheaper to rename on paper than to rename code four times.
