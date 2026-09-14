# Quick wins

Small, low-risk, roughly ordered by effort (smallest first). None of these
need a design decision — just pick a slice of an afternoon.

1. **Delete the confirmed-dead code** — see `01-dead-code-to-delete.md`,
   it's four `git rm` commands.

2. **Fix `backend/.env.example`** — still describes itself as a
   generic "template" and references a Redis config / variable name that
   doesn't match what the app actually reads. Either rewrite it to match
   `fourati.common.rate-limit.rpm` and the actual profile variables, or
   delete it since the root `.env.example` already documents the real Docker
   variables.

3. **Decide on `SqlInjectionFilter`** — currently fully built but disabled
   (`fourati.common.sql-injection.enabled: false`). Either delete it (the
   app is 100% parameterized JPA, so it adds no real protection and the
   regex false-positives on legitimate text) or flip it to `LOG_ONLY` in
   prod for a monitoring signal. Fifteen minutes either way, mostly deciding
   which.

4. **Re-add a `staging` Spring profile** that behaves like `prod` for
   Swagger/actuator exposure but keeps other dev conveniences (verbose
   logging, relaxed rate limits) — useful if you ever run a semi-public demo
   environment. `application-prod.yml` and `application-dev.yml` already
   show the exact shape to copy.

5. **Add `Retry-After` handling awareness to the frontend** — I added a
   `Retry-After: 60` header on the backend's 429 response. Nothing in
   `client.ts`'s interceptor reads it yet; a small addition to show "try
   again in N seconds" instead of a generic error would use information
   that's already being sent.

6. **CI workflow** (bigger than the others but still contained) — see
   `06-devops-and-ci.md` for the shape. I can build this on request; I held
   off because you removed it deliberately last time and I don't want to
   silently undo that call.

7. **Wire `ExportService`** onto at least one real endpoint (audit logs are
   the obvious candidate — `GET /audit-logs/export?format=csv`) or delete
   it. It's fully implemented and unused, which is the "half-built feature"
   pattern worth resolving one way or the other. See
   `01-dead-code-to-delete.md`.

8. **Frontend: read the `Retry-After` header** and surface it in the toast
   shown by `useAppStore.notify` on a 429 response, instead of a generic
   error message.

9. **`AppProperties.Cors.allowCredentials` default is `false`** but
   `application.yml` sets it to `true` explicitly with a detailed comment
   explaining why (SockJS). Worth double-checking this override still
   applies correctly after the CORS wildcard-in-prod guard I added — the
   `@PostConstruct` check in `SecurityConfig` only fires when
   `allow-credentials: true` *and* a wildcard origin *and* the `prod`
   profile are all true together, so it shouldn't interfere with the
   dev/docker profiles where the wildcard is intentional. Worth a quick
   manual boot-test of the `prod` profile with a real
   `APP_CORS_ALLOWED_ORIGINS` value to confirm the guard doesn't false-fire.
