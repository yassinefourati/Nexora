# Load test

`k6-api.js` drives the five most used read endpoints plus a create/update/delete
write path against whatever answers on `:8080`, with a token from the dev-only
`load-test` Keycloak client. Thresholds are the SLO this project commits to;
k6 exits non-zero when one fails, so the run doubles as a regression gate.

| Threshold | Value |
|---|---|
| Error rate (`http_req_failed`) | < 1 % |
| p95 `GET /me`, `GET /me/permissions` | < 150 ms |
| p95 `GET /users`, `GET /roles` | < 300 ms |
| p95 `GET /audit-logs` | < 400 ms |
| p95 tag create/update/delete | < 500 ms |
| Rate-limited responses | < 0.1 % |

## Run it

```bash
# whole product, two API replicas behind nginx (Redis + RabbitMQ backed)
RATE_LIMIT_RPM=100000 docker compose --profile scale up -d --build
docker compose --profile loadtest run --rm k6          # 20 VUs for 60 s by default
K6_VUS=50 K6_DURATION=5m docker compose --profile loadtest run --rm k6
```

`RATE_LIMIT_RPM` is raised because the scenario replays one user; the default
budget of 60 requests per minute per user would (correctly) throttle it.
Results land in `results/summary.json`.

## Recorded runs

Machine: Windows 11, Docker Desktop, 2 × `admin-api` (1.5 CPU / 1280 MB each),
Postgres 17, Redis 7, RabbitMQ 3.13, nginx round-robin. 20 virtual users, 60 s.

### 2026-09-13, two replicas, after the async audit fix

| Metric | Result |
|---|---|
| Requests | 9 233 (151 req/s) |
| Failed | 0 |
| p95 all endpoints | 12.1 ms (max 122 ms) |
| p95 `GET /users` | 9.1 ms |
| p95 `GET /me` | 9.7 ms |
| p95 tag write | 14.4 ms |
| Rate limited | 0 |
| Audit rows written (async, after commit) | 5 638, all with an actor |
| Requests served per replica (audited creates) | 292 / 240 in the previous run, both replicas active |

### 2026-09-13, two replicas, before the fix (kept for the record)

Same scenario: thresholds passed, but `http_server_requests_seconds_max` on
`POST/PUT/DELETE /tags` was 30.2 s and 4 requests returned 500. Cause: the
after-commit audit listener ran synchronously with `REQUIRES_NEW` while the
request's connection was still bound, so each write briefly needed two pooled
connections; at 20 concurrent writers per 10-connection pool this exhausted
the pool and stalled for the Hikari connection timeout. Fix: the listener is
`@Async` (ADR 0003). Max dropped to 0.3 s on the next run.

### Shared rate limit across replicas (proof)

With `RATE_LIMIT_RPM=120` and 300 burst requests through the load balancer:
127 × 200 and 173 × 429, rejections logged by both replicas (78 / 95), one
Redis bucket for the user. A per-JVM limiter would have allowed about 240.
