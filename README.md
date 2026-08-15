# ProjectAdmin — Full Stack

An admin management application: a layered Spring Boot 4 / Java 21 backend
(`project_admin/`) and a React 19 / Vite frontend (`frontend/`), authenticated
via Keycloak (OIDC).

- Backend details, module layout, tech stack: [`project_admin/README.md`](project_admin/README.md)
- Seeded local accounts (frontend login, Keycloak admin, Postgres): [`CREDENTIALS.md`](CREDENTIALS.md)
- Known gaps and planned improvements: [`NEXT_STEPS.md`](NEXT_STEPS.md)

---

## Running the full stack

There are **three docker-compose files** in this repo — each is for a
different situation. Pick the one that matches what you're doing:

| File | Use when | What it starts |
|---|---|---|
| `docker-compose.yml` (repo root) | You want the **whole app** running — frontend, backend, Keycloak, Postgres — with one command | All 4 services, frontend on :5173, backend on :8080 |
| `project_admin/compose.yaml` | You're **developing the backend** and want to run it from your IDE/`make run`, with only its infra dependencies containerized | Postgres + Keycloak only — you run the Spring Boot app yourself |
| `project_admin/compose.smoketest.yaml` | You want to verify a **built backend Docker image** actually boots and connects to a database, in isolation | A throwaway Postgres + the `admin-api:latest` image (no Keycloak, no frontend) |

### Full stack (most common)

```bash
cp .env.example .env    # if you haven't already — see CREDENTIALS.md for the seeded values
docker compose up -d
```

- Frontend: http://localhost:5173
- Backend Swagger UI: http://localhost:8080/swagger-ui.html
- Keycloak admin console: http://localhost:9090/admin

### Backend-only local dev

```bash
cd project_admin
docker compose -f compose.yaml up -d   # Postgres + Keycloak
make run                                # or: ./mvnw spring-boot:run -pl presentation -am
```

### Smoke-testing a built image

```bash
cd project_admin
docker build -t admin-api:latest .
docker compose -f compose.smoketest.yaml up -d
curl http://localhost:8080/actuator/health
```

---

## Repository layout

```
project_fullstack/
├── docker-compose.yml       full-stack compose (see table above)
├── CREDENTIALS.md           seeded local accounts
├── NEXT_STEPS.md            improvement roadmap
├── frontend/                React 19 + Vite + MUI
└── project_admin/           Spring Boot 4 backend, layered Maven multi-module
    ├── common/               cross-cutting infra (errors, rate limiting, export)
    ├── persistence/          JPA entities, repositories, Flyway migrations
    ├── business/              services, mappers, DTOs
    ├── presentation/         REST controllers, security config, app bootstrap
    ├── test/                 unit, integration and architecture tests
    └── keycloak/             realm import config
```

## CI

`.github/workflows/ci.yml` builds and tests both the backend (full Maven
reactor + architecture checks + Docker image build) and the frontend (lint,
unit tests, build) on every push/PR to `main`.
