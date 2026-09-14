# ProjectAdmin

An administration product: a React 19 single-page application, a layered
Spring Boot 4 API on a Java 25 runtime, Keycloak for identity, PostgreSQL for data, and
Redis + RabbitMQ so the API runs as interchangeable replicas.

- Architecture, request path, environments: [`docs/architecture.md`](docs/architecture.md)
- Decisions: [`docs/adr/`](docs/adr/)
- Generating new use cases (proposal): [`docs/use-case-scaffolding.md`](docs/use-case-scaffolding.md)
- Runbooks: [deploy and roll back](docs/runbooks/deploy.md), [a user reports a 500](docs/runbooks/incident-5xx.md), [backup and restore](docs/runbooks/backup-restore.md)
- Backend modules and build: [`backend/README.md`](backend/README.md)
- Frontend conventions (CrudPage, i18n, layering rules): [`CONTRIBUTING.md`](CONTRIBUTING.md#frontend-frontend)
- Seeded local accounts: [`CREDENTIALS.md`](CREDENTIALS.md)
- Contributing: [`CONTRIBUTING.md`](CONTRIBUTING.md)

## Run it

One compose file, selected by profiles. Copy `.env.example` to `.env` first.

| Goal | Command | Then |
|---|---|---|
| Whole product | `docker compose --profile app up -d --build` | http://localhost:5173 (frontend), http://localhost:8080/swagger-ui.html, http://localhost:9090/admin (Keycloak) |
| Two API replicas behind nginx | `docker compose --profile scale up -d --build` | same URLs; proves Redis/RabbitMQ-backed state |
| Dashboards and traces | `docker compose --profile observability up -d` | http://localhost:3000 (Grafana, admin/admin), Prometheus :9091 |
| Load test with SLO thresholds | `docker compose --profile loadtest run --rm k6` | `deploy/loadtest/results/summary.json` |
| Backend from the IDE | `cd backend && make run` | starts Postgres, Keycloak, Redis, RabbitMQ (profile `infra`) itself |
| Frontend with hot reload | `cd frontend && npm ci --legacy-peer-deps && npm run dev` | http://localhost:5173 against :8080 |
| Everything CI checks | `cd backend && make check` | backend `verify` + frontend lint, tests, build |

Profiles combine: `docker compose --profile app --profile observability up -d`.
Stop everything with `cd backend && make down`.

## What is where

```
project_fullstack/
├── docker-compose.yml       profiles: infra | app | scale | observability | loadtest
├── frontend/                React 19 + Vite + MUI, PKCE via oidc-client-ts, Vitest + Playwright
├── backend/           Spring Boot 4 backend: `platform` library + `app` (business domains as Spring Modulith modules)
├── deploy/
│   ├── k8s/                 Kustomize base + dev/staging/prod overlays
│   ├── observability/       OTel collector, Tempo, Prometheus, alert rules, Grafana dashboard
│   ├── loadtest/            k6 scenario with p95/error-rate thresholds, nginx for the scale profile
│   ├── rabbitmq/            STOMP plugin for the WebSocket relay
│   └── keycloak/            realm export imported on first start (seeded users, clients, roles)
├── scripts/                 db-backup.sh, db-restore.sh (pg_dump custom format)
├── docs/                    architecture, ADRs, runbooks
└── .github/workflows/       ci.yml (verify, lint/test/build, Trivy, e2e against the real stack, signed images, staging deploy), rollback.yml
```

## Guarantees the build enforces

- Business domains are Spring Modulith modules: no cross-module `internal`
  import, no cycles (`ModularityTest`), and every mutating endpoint declares
  `@PreAuthorize` (ArchUnit, `backend/app`).
- The schema is owned by Flyway; Hibernate validates and never mutates it.
- User-owned resources are guarded by ownership checks proven at HTTP level
  (`OwnershipAuthorizationIntegrationTest`, real Postgres via Testcontainers).
- Updates carry a `version`; a stale write is refused with 409 `CONCURRENT_MODIFICATION`.
- The API refuses to start in `prod` with a wildcard CORS origin.
- Every error family has a pinned status, media type and body shape
  (`ErrorContractTest`), and the security entry points are pinned through the
  real filter chain (`SecurityContractIntegrationTest`).
- The OpenAPI document is compared with a committed snapshot
  (`OpenApiContractIntegrationTest`); an API change is a visible diff, refreshed
  with `make openapi-update`.
- All of the above run before every push through `scripts/git-hooks/pre-push`
  (`make hooks` installs it). The roadmap for the backend is in
  [`docs/backend-architecture-plan.md`](docs/backend-architecture-plan.md).
