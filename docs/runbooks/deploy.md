# Runbook: deploy, verify, roll back

## What a deployment is

An image tag equal to a commit SHA that passed the full CI pipeline
(`.github/workflows/ci.yml`): backend `verify` including Testcontainers,
frontend lint/test/build, Playwright against the real compose stack, Trivy
image scan, cosign signature, SBOM. Images live in GHCR as
`admin-api:<sha>` and `admin-frontend:<sha>`.

## Deploy to staging (automatic on `main`)

1. CI job `deploy-staging` renders `deploy/k8s/overlays/staging` with the SHA
   and applies it when the `STAGING_KUBECONFIG` secret is set.
2. It waits for `rollout status` on both Deployments (5 min budget for the API).
3. Verify:
   ```bash
   kubectl -n project-admin get pods
   kubectl -n project-admin port-forward svc/admin-api 8081:8081 &
   curl -s localhost:8081/actuator/health | jq .status        # "UP"
   curl -s localhost:8081/actuator/health/readiness | jq .     # db, redis UP
   ```
4. Open Grafana "Admin API — Overview": 5xx rate flat, p95 under 300 ms.

## Deploy to prod (manual promotion)

```bash
cd deploy/k8s/overlays/prod
kustomize edit set image admin-api=ghcr.io/OWNER/REPO/admin-api:<sha> \
                         admin-frontend=ghcr.io/OWNER/REPO/admin-frontend:<sha>
kubectl apply -k .
kubectl -n project-admin rollout status deploy/admin-api --timeout=300s
```

Flyway migrations run at start-up of the first new pod; readiness stays false
until they finish, so traffic keeps flowing to the old pods. A migration that
fails leaves the new pod CrashLooping and the old ReplicaSet serving: fix
forward, do not roll back the database.

## Roll back

Run the **Rollback** workflow (`.github/workflows/rollback.yml`) with the
environment and the previous SHA, or by hand:

```bash
kustomize edit set image admin-api=...:<previous-sha> admin-frontend=...:<previous-sha>
kubectl apply -k .
kubectl -n project-admin rollout status deploy/admin-api
```

Safe only when the previous SHA's Flyway migrations are a prefix of the
current ones (they are additive by policy; check `git log
backend/persistence/src/main/resources/db/migration` between the SHAs).

## Secrets and configuration

- `admin-api-secrets`: datasource URL/user/password, Keycloak issuer,
  `APP_CORS_ALLOWED_ORIGINS`, `WS_RELAY_USERNAME/PASSWORD`, and in prod
  `REDIS_HOST`, `WS_RELAY_HOST`.
- `rabbitmq-secrets` (staging, in-cluster RabbitMQ only).
- The API refuses to start in `prod` if CORS still contains a wildcard.
- Health and metrics are on port 8081 (`management.server.port`), which the
  Ingress never routes. Prometheus scrapes that port unauthenticated.

## Local equivalents

| Goal | Command |
|---|---|
| Whole stack | `docker compose --profile app up -d --build` |
| Two API replicas behind nginx | `docker compose --profile scale up -d --build` |
| Dashboards | `docker compose --profile observability up -d` then http://localhost:3000 |
| Load test | `docker compose --profile loadtest run --rm k6` |
| Backend from the IDE | `cd backend && make run` (starts the `infra` profile) |
