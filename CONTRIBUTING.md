# Contributing

## Before you start

1. Read [`README.md`](README.md) for the full-stack layout and how to run it locally.
2. Read [`project_admin/README.md`](project_admin/README.md) for the backend's module structure — dependencies only point downward (`presentation → business → persistence → common`), enforced at build time by `ArchitectureTest`. If your change needs to break that direction, the design needs rethinking, not a suppressed test.

## Making a change

### Backend (`project_admin/`)

```bash
cd project_admin
./mvnw compile -DskipTests          # fast sanity check while iterating
./mvnw test -pl test -am            # run the full test suite (unit + architecture + integration)
./mvnw verify                       # what CI runs
```

- New code goes in the module matching its layer (see the dependency graph
  in `project_admin/README.md`). If you're not sure which module, ask —
  don't guess and let ArchUnit catch it after the fact.
- Every mutating endpoint (`POST`/`PUT`/`PATCH`/`DELETE`) must declare
  `@PreAuthorize`, at the method or class level — enforced by
  `ArchitectureTest.mutating_controller_endpoints_must_declare_preAuthorize()`.
- Integration tests use Testcontainers against a real Postgres — see
  `test/src/test/java/com/fourati/integration/` for examples. On Windows with
  Docker Desktop, these may fail locally with a Docker-client connection
  error unrelated to your code — CI runs them on Linux runners where this
  doesn't occur, so a local failure here isn't necessarily a signal your
  change is broken.

### Frontend (`frontend/`)

```bash
cd frontend
npm run lint
npm run test:run
npm run build
```

## Before opening a PR

- [ ] `./mvnw verify` passes (or you've noted in the PR description that the
      only failure is the known local Testcontainers issue)
- [ ] `npm run lint && npm run test:run && npm run build` passes
- [ ] If you touched the module structure, package names, or renamed
      anything broadly, you did it in one deliberate pass — not iteratively.
- [ ] New backend behavior has a test at the appropriate level: unit test
      for a service method, integration test for a full request→DB flow,
      architecture test if you're introducing a new layering rule.

## Commit messages

Explain *why*, not just *what* — the diff already shows what changed.
