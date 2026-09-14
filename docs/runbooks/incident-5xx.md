# Runbook: "a user reports a 500" (or the 5xx alert fired)

Goal: from report to root cause in ten minutes, using only what this stack
already emits.

## 1. Get the correlation id (30 s)

Every response carries `X-Correlation-ID` and the JSON body's
`correlationId`; the frontend shows it in the error toast. Ask the user for it,
or take it from the alert's example trace. If there is none, use the time
window and the endpoint name instead.

## 2. Find the request in the logs (2 min)

Logs are JSON (`logstash-logback-encoder`) with `correlationId`, `traceId`,
`spanId` and `app` fields.

```bash
# Kubernetes
kubectl -n project-admin logs deploy/admin-api --since=30m | grep '"correlationId":"<id>"'
# compose
docker compose --profile app logs admin-api --since 30m | grep '<id>'
```

The `GlobalExceptionHandler` line for a 500 contains the exception class and
message; unexpected exceptions are logged at ERROR with the stack trace.
Business errors (4xx) are logged at WARN and are not incidents.

## 3. Open the trace (2 min)

Grafana → Explore → Tempo → search by `traceId` from the log line (or by
service `admin-api`, status error, time range). The trace shows the JDBC
statements, the JWKS call to Keycloak, and Redis/STOMP spans, each with
timing. A 500 with a long JDBC span is a lock or a slow query; a failing
Keycloak span is an identity outage; a `CallNotPermittedException` means the
JWKS circuit breaker is open (Keycloak was unreachable for 3+ calls).

## 4. Check the dashboard for blast radius (1 min)

"Admin API — Overview": is the 5xx rate global or one endpoint (panel
"Slowest endpoints")? Is `hikaricp_connections_pending` above zero (pool
exhausted)? Did "Instances up" drop (a pod restarting on OOM)?

## 5. Likely causes and the fix

| Symptom | Cause | Action |
|---|---|---|
| `OptimisticLockingFailure` in logs, 409 to clients | concurrent edits, expected | none; the client reloads |
| `CONCURRENT_MODIFICATION` for one user repeatedly | frontend sending a stale `version` | check the form dialog reloads after save |
| `Could not open JPA EntityManager` / pool pending | DB saturated or down | `kubectl describe pod postgres`, check connection count, scale pool only if DB has headroom |
| `Keycloak unavailable (circuit open)` | identity provider down | fix Keycloak; the breaker closes 20 s after it recovers |
| `Rate limit store unavailable` warnings | Redis down; limiter fails open | restore Redis; no user impact besides missing limits |
| WebSocket pushes missing for some users | STOMP relay disconnected | `rabbitmq-diagnostics status`; the API reconnects automatically |
| `Schema-validation` at start-up | migration and entity out of sync | roll back the image, fix forward with a new migration |
| OOMKilled pods | heap sized above the container limit | `MaxRAMPercentage=75` assumes the limit in `backend.yaml`; raise the limit, not the percentage |

## 6. Close the loop

- Add the correlation id and root cause to the incident note.
- If the cause was a missing rule (bad input reached a 500), add the
  `ErrorCode`, map it in `GlobalExceptionHandler`, and add the test that
  reproduces the input.
- If an alert did not fire but should have, adjust
  `deploy/observability/alert-rules.yml` in the same change.
